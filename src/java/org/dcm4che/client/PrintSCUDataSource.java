/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Joe Foraci <jforaci@users.sourceforge.net>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che.client;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.image.PixelDataDescription;
import org.dcm4che.image.PixelDataFactory;
import org.dcm4che.image.PixelDataReader;
import org.dcm4che.image.PixelDataWriter;
import org.dcm4che.net.DataSource;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author jforaci
 * @since Jun 22, 2003
 * @version $Revision: 15102 $ $Date: 2011-03-14 15:49:29 +0100 (Mo, 14 Mär 2011) $
 *
 */
class PrintSCUDataSource implements DataSource
{
    private static final Logger log = Logger.getLogger(PrintSCUDataSource.class);
 
    /**
     * Simple overlay representation
     * @author jforaci
     */
    private static final class Overlay
    {
        public int cols, rows;
        public int x, y;
        public byte[] data;
        public Overlay (int group, Dataset ds)
        {
            final int OvRows = 0x10, OvCols = 0x11;
            final int OvOrigin = 0x50, OvData = 0x3000;
            data = ds.getByteBuffer(group | OvData).array();
            if (data == null)
                throw new IllegalArgumentException("no overlay data");
            int[] origin = ds.getInts(group | OvOrigin);
            x = origin[0] - 1;
            y = origin[1] - 1;
            cols = ds.getInt(group | OvCols, 0);
            rows = ds.getInt(group | OvRows, 0);
            if (cols == 0 || rows == 0)
                throw new IllegalArgumentException("bad/no cols/rows in overlay");
        }
    };

	private static final DcmParserFactory parserFact =
		DcmParserFactory.getInstance();
	private final PrintSCU printSCU;
	private final Dataset imageBox;
	private final File file, psFile;
    private final boolean burnInOverlays;
    private final boolean autoScale;
	
	/**
	 * @param printSCU the calling <code>PrintSCU</code>
	 * @param imageBox the <code>Dataset</code> containing the Image Box header
     *        (minus any image pixel module-specific attributes)
	 * @param file the path to the local file containing the image to be sent
     * @param burnInOverlays specifies whether to "burn-in" and Overlay Planes
     *        found within the DICOM image
	 */
	public PrintSCUDataSource(PrintSCU printSCU, Dataset imageBox, File file,
                              File psFile, boolean burnInOverlays,
                              boolean autoScale) {
		this.printSCU = printSCU;
		this.imageBox = imageBox;
		this.file = file;
        this.psFile = psFile;
        this.burnInOverlays = burnInOverlays;
        this.autoScale = autoScale;
	}

    private static final PixelDataFactory pdFact = PixelDataFactory.getInstance();
	private static final Dataset IMAGE_MODULE =
		PrintSCU.dcmFact.newDataset();
	static {
		IMAGE_MODULE.putUS(Tags.SamplesPerPixel);
		IMAGE_MODULE.putCS(Tags.PhotometricInterpretation);
		IMAGE_MODULE.putUS(Tags.PlanarConfiguration);
		IMAGE_MODULE.putUS(Tags.Rows);
		IMAGE_MODULE.putUS(Tags.Columns);
		IMAGE_MODULE.putIS(Tags.PixelAspectRatio);
		IMAGE_MODULE.putUS(Tags.BitsAllocated);
		IMAGE_MODULE.putUS(Tags.BitsStored);
		IMAGE_MODULE.putUS(Tags.HighBit);
		IMAGE_MODULE.putUS(Tags.PixelRepresentation);
	}

	/**
     * TODO Add support for multiframe images (add support in PixelDataWriter)
	 * @see org.dcm4che.net.DataSource#writeTo(java.io.OutputStream, java.lang.String)
	 */
	public void writeTo(OutputStream out, String tsUID)
        throws IOException
    {
		InputStream in = new BufferedInputStream(
			new FileInputStream(file));
		final Dataset ds = PrintSCU.dcmFact.newDataset();
        final Dataset ps = (psFile != null) ? PrintSCU.dcmFact.newDataset() : ds;
        final int toBitsStored, toBitsAllocated;
        
		try {
			DcmParser parser = parserFact.newDcmParser(in);
			parser.setDcmHandler(ds.getDcmHandler());
			parser.parseDcmFile(null, Tags.PixelData);
            if (psFile != null) {
                DcmParser psParser = parserFact.newDcmParser(new BufferedInputStream(
                    new FileInputStream(psFile)));
                psParser.setDcmHandler(ps.getDcmHandler());
                psParser.parseDcmFile(null, -1);
            }
            //err check for PixelData
			if (parser.getReadTag() != Tags.PixelData) {
				throw new IOException("No Pixel Data in file - " + file);
			}
            DcmDecodeParam decodeParam = parser.getDcmDecodeParam();
            DcmEncodeParam encodeParam = DcmDecodeParam.valueOf(tsUID);
            PixelDataDescription toDesc = null;
            PixelDataDescription fromDesc = new PixelDataDescription(ds,
                decodeParam.byteOrder, parser.getReadVR());
            if (fromDesc.getNumberOfFrames() > 1)
                throw new IOException("multi-frame images are not currently supported");
            //grab some common pixel module attributes
            final int bitsAlloc = ds.getInt(Tags.BitsAllocated, 8);
            final int bitsStored = ds.getInt(Tags.BitsStored, 8);
            final int highBit = ds.getInt(Tags.HighBit, bitsStored - 1);
            final int cols = ds.getInt(Tags.Columns, 0);
            final int rows = ds.getInt(Tags.Rows, 0);
            final int spp = ds.getInt(Tags.SamplesPerPixel, 1);
            final boolean signed = (ds.getInt(Tags.PixelRepresentation, 0) == 1);
            final String pmi = ds.getString(Tags.PhotometricInterpretation, "MONOCHROME2");
            //err check on bit depth
            if (bitsStored == 32 && !signed) {
                throw new IOException("conversion from " + bitsStored
                    + " bits stored, unsigned is not currently supported");
            }
            //err check Photometric Interpretation
			if (printSCU.isColorPrint()) {
				if (!"RGB".equals(pmi)) { //&& !"MONOCHROME1".equals(pmi)
					throw new IOException("Conversion from " + pmi + " to RGB not currently supported");
				}
			}
            else if (!"MONOCHROME2".equals(pmi) && !"MONOCHROME1".equals(pmi)) {
                throw new IOException("Conversion from " + pmi + " to MONOCHROME not currently supported");
			}
            //calculate whether we need 8/8 or 12/16 and generate a new
            // PixelDataDescription for writing to convert the sample values to
            // format supported in the Image Box Pixel Representation Module:
            // - the original Bits Stored or Bits Allocated is != needed value
            // - the original Pixel Data is signed
            // - the original pixel samples hava a High Bit larger than the
            //   (new Bits Stored value - 1)
            // - the Planar Configuration is by-pixel instead of by-plane
            // Note: the pixel data may need to be read (in one big chunk) by a
            // pixel data reader, anyway, if the pixel data must still be scaled
            // because of the presence of a window, M-LUT, VOI-LUT, P-LUT, overlay
            // plane data, etc!
            if (bitsStored > 8) {
                toBitsStored = 12;
                toBitsAllocated = 16;
            }
            else {
                toBitsStored = 8;
                toBitsAllocated = 8;
            }
            if (bitsStored != toBitsStored || bitsAlloc != toBitsAllocated
                || highBit != bitsStored - 1 || signed
                || (!fromDesc.isByPlane() && spp > 1)) {
                //should we auto-scale?
                if (!autoScale)
                    throw new IllegalArgumentException("The image's pixel format"
                        + "is incompatable for an Image Box and auto-scale is disabled");
                toDesc = new PixelDataDescription(fromDesc, encodeParam,
                    toBitsAllocated, toBitsStored, false, true);
                ds.putUS(Tags.BitsStored, toBitsStored);
                ds.putUS(Tags.BitsAllocated, toBitsAllocated);
                ds.putUS(Tags.HighBit, toBitsStored - 1);
                ds.putUS(Tags.PixelRepresentation, 0);
                ds.putUS(Tags.PlanarConfiguration, 1);
            }
            else
                toDesc = fromDesc;

                log.debug("readlen=" + parser.getReadLength() + "calclen=" + toDesc.calcPixelDataLength());
            
            //write image box
			imageBox.writeDataset(out, encodeParam);
			ds.writeHeader(out, encodeParam,
				printSCU.isColorPrint() ? Tags.BasicColorImageSeq
				                        : Tags.BasicGrayscaleImageSeq,
				VRs.SQ, -1);
			ds.writeHeader(out, encodeParam,
				Tags.Item, VRs.NONE, -1);
                
			int[][] buff = null;
			PixelDataReader pd = null;
            
            //check if any overlays exist and if they are to be burned into the image
            if (burnInOverlays) {
                DcmElement el;
                int group = 0x60000000, cntr = 16;
                List list = new Vector(16);
                while (cntr > 0) {
                    if ((el = ds.get(group | 0x3000)) != null)
                        list.add(new Overlay(group, ds));
                    if (ps != ds && (el = ps.get(group | 0x3000)) != null)
                        list.add(new Overlay(group, ps));
                    cntr--;
                    group += 0x20000;
                }
                Overlay[] overlays = (Overlay[])list.toArray(new Overlay[0]);
                list = null;
                final int burnValue = (signed) ? (1 << highBit) - 1 : -1;
                
                if (overlays.length > 0) {
                    if (buff == null)
                        buff = (pd = readPixelData(fromDesc, in)).getPixelDataArray(0);
                    
                    Overlay ovl;
                    
                    //iterate over each overlay
                    for (int i = 0; i < overlays.length; i++) {
                        try {
                            ovl = overlays[i];
                            final int colstart = Math.max(0, ovl.x);
                            final int colend = Math.min(ovl.x + ovl.cols, cols);
                            final int rowstart = Math.max(0, ovl.y);
                            final int rowend = Math.min(ovl.y + ovl.rows, rows);
                            final int x = (ovl.x >= 0) ? 0 : -ovl.x;
                            final int y = (ovl.y >= 0) ? 0 : -ovl.y;
                            int mask = 1;
                            int ind = x + y * ovl.cols;
                            for (int j = rowstart; j < rowend; j++) {
                                for (int k = colstart; k < colend; k++) {
                                    if ((ovl.data[ind >> 3] & mask) > 0) {
                                        for (int s = 0; s < spp; s++)
                                        	buff[s][(j * cols) + k] = burnValue;
                                    }
                                    ind++;
                                    if (mask == 0x80)
                                        mask = 1;
                                    else
                                        mask = mask << 1;
                                }
                                ind = x + (y + j + 1) * ovl.cols;
                            }
                        }
                        catch (IndexOutOfBoundsException e) {
                            log.error("Bad overlay plane data (" + i + "), not enough data");
                            //e.printStackTrace();
                        }
                    }
                }
                
                //iterate over pixel data for overlay data in pixel data itself
                if (bitsAlloc > bitsStored) {
                    if (buff == null)
                        buff = (pd = readPixelData(fromDesc, in)).getPixelDataArray(0);
                    
                    final int mask = ~(((1 << bitsStored) - 1) << (highBit - bitsStored + 1));
                    for (int s = 0; s < spp; s++)
                        for (int i = 0; i < buff[s].length; i++)
                            if ((buff[s][i] & mask) != 0)
                                buff[s][i] = burnValue;
                }
            }
            
            //if a window exists, scale pixel data to window range
            final float rs = ps.getFloat(Tags.RescaleSlope, 1F);
            final float ri = ps.getFloat(Tags.RescaleIntercept, 0F);
            int winTop = 0, winBot = 0;
            Dataset voi = getVoiDataset(ds, ps);
            final boolean windowPresent = voi.contains(Tags.WindowCenter) && voi.contains(Tags.WindowWidth);
            if (windowPresent) {
                winTop = (int)Math.ceil(((voi.getFloat(Tags.WindowCenter, 0F)
                    + voi.getFloat(Tags.WindowWidth, 0F) / 2) - ri) / rs);
                winBot = (int)Math.floor(((voi.getFloat(Tags.WindowCenter, 0F)
                    - voi.getFloat(Tags.WindowWidth, 0F) / 2) - ri) / rs);
            }
            
            //if a Modality LUT and/or VOI LUT exists combine the LUTs into one
            LutBuffer mlut = null, vlut = null, plut = null, lut = null;
            Dataset item;
            if ("MONOCHROME1".equals(pmi) || "MONOCHROME2".equals(pmi)) {
                float mpv = (rs >= 0) ? fromDesc.minPossibleStoredValue() * rs + ri
                                      : fromDesc.maxPossibleStoredValue() * rs + ri;
                if ((item = ps.getItem(Tags.ModalityLUTSeq)) != null)
                    mlut = new LutBuffer(item.getByteBuffer(Tags.LUTData),
                        item.getInts(Tags.LUTDescriptor),
                        (signed) ? VRs.SS : VRs.US);
                if ((item = voi.getItem(Tags.VOILUTSeq)) != null)
                    vlut = new LutBuffer(item.getByteBuffer(Tags.LUTData),
                        item.getInts(Tags.LUTDescriptor),
                        ((mlut != null)
                            ? (VRs.US)
                            : ((mpv < 0) ? VRs.SS : VRs.US)));
                if (mlut != null && vlut != null)
                    lut = combineLuts(mlut, vlut);
                else if (mlut != null)
                    lut = mlut;
                else if (vlut != null)
                    lut = vlut;
                if ((item = ps.getItem(Tags.PresentationLUTSeq)) != null) {
                    plut = new LutBuffer(item.getByteBuffer(Tags.LUTData),
                        item.getInts(Tags.LUTDescriptor),
                        VRs.US); //always US
                    ds.putCS(Tags.PhotometricInterpretation,"MONOCHROME2");
                }
                else if ("INVERSE".equals(ps.getString(Tags.PresentationLUTShape))) {
                    ds.putCS(Tags.PhotometricInterpretation,
                        ("MONOCHROME2".equals(pmi)) ? "MONOCHROME1" : "MONOCHROME2");
                }
            }
            
            //write image box pixel representation attributes
            ds.subSet(IMAGE_MODULE).writeDataset(out, encodeParam);
            //write Pixel Data element header with the proper length
            ds.writeHeader(out, encodeParam,
                parser.getReadTag(), //"Pixel Data"
                toDesc.getPixelDataVr(),
                (int)toDesc.calcPixelDataLength());
            
            //apply any Rescale Slope/Intercept or LUTs (M-LUT and/or VOI-LUT),
            //P-LUT, and scale pixel samples to proper new range for the Hard
            //Copy
            if (lut != null) {
                if (buff == null)
                    buff = (pd = readPixelData(fromDesc, in)).getPixelDataArray(0);
                if (plut != null)
                    scaleToRangeWithLUTAndPLut(buff, fromDesc, toBitsStored, false,
                        rs, ri, lut, plut, winBot, winTop);
                else
                    scaleToRangeWithLUT(buff, fromDesc, toBitsStored, false, rs, ri,
                        lut, winBot, winTop);
            }
            else if (plut != null) {
                if (buff == null)
                    buff = (pd = readPixelData(fromDesc, in)).getPixelDataArray(0);
                scaleToRangeWithPLut(buff, fromDesc, toBitsStored, false, plut,
                    winBot, winTop);
            }
            else if (toDesc != fromDesc || windowPresent) {
                if (buff == null)
                    buff = (pd = readPixelData(fromDesc, in)).getPixelDataArray(0);
                scaleToRange(buff, fromDesc, toBitsStored, false, winBot, winTop);
            }
            
            //check if we needed to read entire chunk of pxiel data and write the
            //modified samples to the output stream
            if (pd != null) {
                //for the writing we're going on the assumption that the print server
                // will ignore the overlay bits in pixel cell (in the case of bs=12)
                int[][][] tmp = new int[1][0][0];
                tmp[0] = buff;
                PixelDataWriter pdWriter = pdFact.newWriter(tmp, false, toDesc,
                    new DataSourceImageOutputStream(out));
				pdWriter.writePixelData();
            }
            else {
    			copy(in, out, parser.getReadLength());
            }
            
            //write out the closing item and sequence delimiters
			ds.writeHeader(out, encodeParam,
				Tags.ItemDelimitationItem, VRs.NONE, 0);
			ds.writeHeader(out, encodeParam,
				Tags.SeqDelimitationItem, VRs.NONE, 0);
		}
        finally {
			try {
				in.close();
			}
            catch (IOException ignore) {}
		}
	}

	private Dataset getVoiDataset(Dataset ds, Dataset ps)
    {
        DcmElement scVoiLutSeq = ps.get(Tags.SoftcopyVOILUTSeq);
        
        if (ds == ps || scVoiLutSeq == null)
            return ds;
        
        String iuid = ds.getString(Tags.SOPInstanceUID);
        Dataset item;
        DcmElement ris;
        final int n = scVoiLutSeq.countItems();
        
        for (int i = 0; i < n; i++) {
            item = scVoiLutSeq.getItem(i);
            ris = item.get(Tags.RefImageSeq);
            if (ris != null) {
                for (int j = 0; j < ris.countItems(); j++)
                    if (ris.getItem(j).getString(Tags.RefSOPInstanceUID, "")
                        .equals(iuid))
                        return item;
            }
            else {
                return item;
            }
        }
        
		return ds;
	}

	private PixelDataReader readPixelData(PixelDataDescription desc,
        InputStream in)
        throws IOException
    {
        PixelDataReader reader = pdFact.newReader(desc, ImageIO.createImageInputStream(in));
        reader.readPixelData(true);
        return reader;
    }

    private LutBuffer combineLuts(LutBuffer lut0, LutBuffer lut1)
    {
        return combineLuts(new LutBuffer[] {lut0, lut1});
    }

    private LutBuffer combineLuts(LutBuffer[] luts)
    {
        final int n = luts[0].getLutSize();
        byte[] olut = new byte[n * 2];
        int olutInd = 0;
        int curVal;
        for (int i = 0, j; i < n; i++) {
            curVal = luts[0].getEntry(i);
            for (j = 1; j < luts.length; j++) {
                curVal = luts[j].getEntryFromInput(curVal);
            }
            olut[olutInd++] = (byte)curVal;
            olut[olutInd++] = (byte)(curVal >> 8);
        }
        ByteBuffer wrapped = ByteBuffer.wrap(olut);
        wrapped.order(ByteOrder.LITTLE_ENDIAN);
        return new LutBuffer(wrapped, n, luts[0].getFirstValueMapped(),
            luts[luts.length - 1].getDepth(), LutBuffer.TYPE_WORD);
    }

    private void scaleToRangeWithPLut(int[][] pixelData,
        PixelDataDescription from,
        int toBitDepth, boolean toSigned,
        LutBuffer plut,
        int start, int end)
    {
        final int fromBitDepth = from.getBitsStored();
        final int min = (from.isSigned()) ? -(1 << (fromBitDepth - 1)) : 0;
        final int max = (from.isSigned()) ? (1 << (fromBitDepth - 1)) - 1
                                 : (1 << fromBitDepth) - 1;
        if (start == end) {
            start = min;
            end = max;
        }
        final int rngOrig = end - start;
        final int newMin = (toSigned) ? -(1 << (toBitDepth - 1)) : 0;
        final int newMax = (toSigned) ? (1 << (toBitDepth - 1)) - 1
                                      : (1 << toBitDepth) - 1;
        final int rngNew = newMax - newMin;
        final int plutRng = (1 << plut.getDepth()) - 1;
        final int plutInputUB = plut.getLutSize() - 1;
        final float f1 = (float)(plut.getLutSize() - 1) / rngOrig;
        final float f2 = (float)rngNew / plutRng;
        final int leftShift = 32 - from.getHighBit() - 1;
        final int rightShift = 32 - from.getBitsStored();
        final int plutMinMappedValue = (int)(plut.getEntry(0) * f2 + 0.5F) - newMin;
        final int plutMaxMappedValue = (int)(plut.getEntry(plutInputUB) * f2 + 0.5F) - newMin;
        double tmp;

        if (from.isSigned()) {
            for (int s = 0; s < pixelData.length; s++)
                for (int i = 0; i < pixelData[s].length; i++) {
                    tmp = (((pixelData[s][i] << leftShift) >> rightShift) - start) * f1;
                    if (tmp < 0)
                        pixelData[s][i] = plutMinMappedValue;
                    else if (tmp > plutInputUB)
                        pixelData[s][i] = plutMaxMappedValue;
                    else
                        pixelData[s][i] = (int)(plut.getEntry((int)(tmp + 0.5F)) * f2 + 0.5F) - newMin;
                }
        }
        else {
            for (int s = 0; s < pixelData.length; s++)
                for (int i = 0; i < pixelData[s].length; i++) {
                    tmp = (((pixelData[s][i] << leftShift) >>> rightShift) - start) * f1;
                    if (tmp < 0)
                        pixelData[s][i] = plutMinMappedValue;
                    else if (tmp > plutInputUB)
                        pixelData[s][i] = plutMaxMappedValue;
                    else
                        pixelData[s][i] = (int)(plut.getEntry((int)(tmp + 0.5F)) * f2 + 0.5F) - newMin;
                }
        }
    }

    private void scaleToRangeWithLUTAndPLut(int[][] pixelData,
        PixelDataDescription from,
        int toBitDepth, boolean toSigned,
        final float rescaleSlope, final float rescaleInt,
        LutBuffer lut, LutBuffer plut,
        int start, int end)
    {
        final int fromBitDepth = lut.getDepth();
        final int min = (false) ? -(1 << (fromBitDepth - 1)) : 0;
        final int max = (false) ? (1 << (fromBitDepth - 1)) - 1
                                 : (1 << fromBitDepth) - 1;
        if (start == end) {
            start = min;
            end = max;
        }
        final int rngOrig = end - start;
        final int newMin = (toSigned) ? -(1 << (toBitDepth - 1)) : 0;
        final int newMax = (toSigned) ? (1 << (toBitDepth - 1)) - 1
                                      : (1 << toBitDepth) - 1;
        final int rngNew = newMax - newMin;
        final int plutRng = (1 << plut.getDepth()) - 1;
        final int plutInputUB = plut.getLutSize() - 1;
        final float f1 = (float)(plut.getLutSize() - 1) / rngOrig;
        final float f2 = (float)rngNew / plutRng;
        final int leftShift = 32 - from.getHighBit() - 1;
        final int rightShift = 32 - from.getBitsStored();
        final int plutMinMappedValue = (int)(plut.getEntry(0) * f2 + 0.5F) - newMin;
        final int plutMaxMappedValue = (int)(plut.getEntry(plutInputUB) * f2 + 0.5F) - newMin;
        double tmp;

        if (from.isSigned()) {
            for (int s = 0; s < pixelData.length; s++)
                for (int i = 0; i < pixelData[s].length; i++) {
                    tmp = ((pixelData[s][i] << leftShift) >> rightShift) * rescaleSlope + rescaleInt;
                    tmp = lut.getEntryFromInput((int)(tmp + 0.5F));
                    tmp = (tmp - start) * f1;
                    if (tmp < 0)
                        pixelData[s][i] = plutMinMappedValue;
                    else if (tmp > plutInputUB)
                        pixelData[s][i] = plutMaxMappedValue;
                    else
                        pixelData[s][i] = (int)(plut.getEntry((int)(tmp + 0.5F)) * f2 + 0.5F) - newMin;
                }
        }
        else {
            for (int s = 0; s < pixelData.length; s++)
                for (int i = 0; i < pixelData[s].length; i++) {
                    tmp = ((pixelData[s][i] << leftShift) >>> rightShift) * rescaleSlope + rescaleInt;
                    tmp = lut.getEntryFromInput((int)(tmp + 0.5F));
                    tmp = (tmp - start) * f1;
                    if (tmp < 0)
                        pixelData[s][i] = plutMinMappedValue;
                    else if (tmp > plutInputUB)
                        pixelData[s][i] = plutMaxMappedValue;
                    else
                        pixelData[s][i] = (int)(plut.getEntry((int)(tmp + 0.5F)) * f2 + 0.5F) - newMin;
                }
        }
    }

    private void scaleToRangeWithLUT(int[][] pixelData,
        PixelDataDescription from,
        int toBitDepth, boolean toSigned,
        final float rescaleSlope, final float rescaleInt,
        LutBuffer lut,
        int start, int end)
    {
        final int fromBitDepth = lut.getDepth();
        final int min = (false) ? -(1 << (fromBitDepth - 1)) : 0;
        final int max = (false) ? (1 << (fromBitDepth - 1)) - 1
                                 : (1 << fromBitDepth) - 1;
        if (start == end) {
            start = min;
            end = max;
        }
        final int rngOrig = end - start;
        final int newMin = (toSigned) ? -(1 << (toBitDepth - 1)) : 0;
        final int newMax = (toSigned) ? (1 << (toBitDepth - 1)) - 1
                                      : (1 << toBitDepth) - 1;
        final int rngNew = newMax - newMin;
        final float f = (float)rngNew / rngOrig;
        final int leftShift = 32 - from.getHighBit() - 1;
        final int rightShift = 32 - from.getBitsStored();
        double tmp;

        if (from.isSigned()) {
            for (int s = 0; s < pixelData.length; s++)
                for (int i = 0; i < pixelData[s].length; i++) {
                    tmp = ((pixelData[s][i] << leftShift) >> rightShift) * rescaleSlope + rescaleInt;
                    tmp = lut.getEntryFromInput((int)(tmp + 0.5F));
                    tmp = (tmp - start) * f;
                    if (tmp < 0)
                        pixelData[s][i] = newMin;
                    else if (tmp > rngNew)
                        pixelData[s][i] = newMax;
                    else
                        pixelData[s][i] = (int)(tmp + 0.5F) - newMin;
                }
        }
        else {
            for (int s = 0; s < pixelData.length; s++)
                for (int i = 0; i < pixelData[s].length; i++) {
                    tmp = ((pixelData[s][i] << leftShift) >>> rightShift) * rescaleSlope + rescaleInt;
                    tmp = lut.getEntryFromInput((int)(tmp + 0.5F));
                    tmp = (tmp - start) * f;
                    if (tmp < 0)
                        pixelData[s][i] = newMin;
                    else if (tmp > rngNew)
                        pixelData[s][i] = newMax;
                    else
                        pixelData[s][i] = (int)(tmp + 0.5F) - newMin;
                }
        }
    }

    /**
     * Scales the range [<code>start..end</code>] in pixelData described by
     * <code>from</code> to the specified bit depth and sign representation.
     * The pixel data is assumed to contain the <i>entire cell</i>, and
     * loses any overlay bits that may be present.
     */
    private void scaleToRange(int[][] pixelData, PixelDataDescription from,
        int toBitDepth, boolean toSigned, int start, int end)
    {
        final int fromBitDepth = from.getBitsStored();
        final int min = (from.isSigned()) ? -(1 << (fromBitDepth - 1)) : 0;
        final int max = (from.isSigned()) ? (1 << (fromBitDepth - 1)) - 1
                                 : (1 << fromBitDepth) - 1;
        if (start == end) {
            start = min;
            end = max;
        }
        final int rngOrig = end - start;
        final int newMin = (toSigned) ? -(1 << (toBitDepth - 1)) : 0;
        final int newMax = (toSigned) ? (1 << (toBitDepth - 1)) - 1
                                      : (1 << toBitDepth) - 1;
        final int rngNew = newMax - newMin;
        final float f = (float)rngNew / rngOrig;
        final int leftShift = 32 - from.getHighBit() - 1;
        final int rightShift = 32 - fromBitDepth;
        float tmp;

        if (from.isSigned()) {
            for (int s = 0; s < pixelData.length; s++)
                for (int i = 0; i < pixelData[s].length; i++) {
                    tmp = (((pixelData[s][i] << leftShift) >> rightShift) - start) * f;
                    if (tmp < 0)
                        pixelData[s][i] = newMin;
                    else if (tmp > rngNew)
                        pixelData[s][i] = newMax;
                    else
                        pixelData[s][i] = (int)(tmp + 0.5F) - newMin;
                }
        }
        else {
            for (int s = 0; s < pixelData.length; s++)
                for (int i = 0; i < pixelData[s].length; i++) {
                    tmp = (((pixelData[s][i] << leftShift) >>> rightShift) - start) * f;
                    if (tmp < 0)
                        pixelData[s][i] = newMin;
                    else if (tmp > rngNew)
                        pixelData[s][i] = newMax;
                    else
                        pixelData[s][i] = (int)(tmp + 0.5F) - newMin;
                }
        }
    }
    
	private void copy(InputStream in, OutputStream out, int len) throws IOException {
		int c, remain = len;
		byte[] buffer = printSCU.getBuffer();
		while (remain > 0) {
			c = in.read(buffer, 0, Math.min(buffer.length, remain));
			if (c == -1) {
				throw new EOFException("EOF during read of pixel data");
			}
			out.write(buffer, 0, c);
			remain -= c;
		}
	}

}
