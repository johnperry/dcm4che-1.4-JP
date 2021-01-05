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
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package org.dcm4cheri.image;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Stack;

import javax.imageio.stream.ImageInputStream;

import org.dcm4che.dict.VRs;
import org.dcm4che.image.PixelDataDescription;
import org.dcm4che.image.PixelDataReader;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since July 2003
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 * @see "DICOM Part 5: Data Structures and Encoding, Section 8. 'Encoding of Pixel,
 *      Overlay and Waveform Data', Annex D"
 */
public class PixelDataReaderImpl
    implements PixelDataReader
{
    private final int sampleMaskLS;
    private final int sampleMaskRS;
    private final int hOvlMask;
    private final int hOvlMaskRS;
    private final int lOvlMask;
    private final ImageInputStream in;
    private int[][][] data;
    private final long len = -1L;

    //readSample(), stream-related fields
    private final long initialReadPos;
    private final PixelDataStreamMark initialStreamState;
    private int bOff;  //bit off within cw
    private int bMask;  //mask of bits 'bOff' to 16 (inclusive) of 'cw'
    private int cw;  //current word (last one read)
    private long samplesPassed;

    //mark-related fields
    private final static class PixelDataStreamMark
    {
        public final int offset, currentWord, mask;
        public final long samplesPassed;
        PixelDataStreamMark(int offset, int currentWord, int mask, long samplesPassed)
        {
            this.offset = offset;
            this.currentWord = currentWord;
            this.mask = mask;
            this.samplesPassed = samplesPassed;
        }
    }
    private final Stack markStack = new Stack();

    private final PixelDataDescription pdDesc;
    //fields copied from the initialized pixel data description
    private final int cols;
    private final int rows;
    private final int nf;
    private final int frameSize;
    private final int size;
    private final int samplesPerFrame;
    private final int ba;
    private final int bs;
    private final int hb;
    private final int spp;
    private final boolean signed;
    private final boolean byPlane;
    private final String pmi;
    private final ByteOrder byteOrder;
    private final int pixelDataVr;

    PixelDataReaderImpl(PixelDataDescription desc, ImageInputStream in)
    {
        pdDesc = desc;
        //attributes from pixel data description
        cols = desc.getCols();
        rows = desc.getRows();
        nf = desc.getNumberOfFrames();
        frameSize = desc.getFrameSize();
        size = desc.getSize();
        samplesPerFrame = desc.getSamplesPerFrame();
        ba = desc.getBitsAllocated();
        bs = desc.getBitsStored();
        hb = desc.getHighBit();
        spp = desc.getSamplesPerPixel();
        signed = desc.isSigned();
        byPlane = desc.isByPlane();
        pmi = desc.getPmi();
        byteOrder = desc.getByteOrder();
        pixelDataVr = desc.getPixelDataVr();
        //
        data = null;
        try {
            initialReadPos = in.getStreamPosition();
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not determine current position in stream");
        }
        bOff = 16; //cause next word to be read
        samplesPassed = 0;
        initialStreamState = new PixelDataStreamMark(bOff, cw, bMask, 0);
        this.in = in;
        //shifts for getting pixel value from an int containing a cell
        sampleMaskLS = 32 - hb - 1;
        sampleMaskRS = 32 - bs;
        //masks and shifts for getting overlay bits from an int containing a cell
        hOvlMask = (1 << ba) - (1 << hb + 1);
        hOvlMaskRS = bs;
        lOvlMask = (1 << (hb + 1) - bs) - 1;
        //set byte-order
        if (pixelDataVr == VRs.OW)
            in.setByteOrder(byteOrder);
        else
            in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }

    public int readSample()
        throws IOException
    {
        return getSampleBitsFromCell(readSampleCell());
    }

    public int readSampleCell()
        throws IOException
    {
        int cell = 0;
        int bRead = 0;
        
        while (bRead < ba) {
            if (bOff == 16) {
                cw = in.readShort();
                bOff = 0;
                bMask = -1 & 0xFFFF;
            }
            cell |= ((cw & bMask) >>> bOff) << bRead;
            bRead += 16 - bOff;
            bOff = 16;
        }
        if (bRead > ba) {
            bOff -= (bRead - ba); //bOff == 16 here
            bMask = (~((1 << bOff) - 1)) & 0xFFFF;
        }
        samplesPassed++;
        return cell;
    }

    public void readFully(int[] samples, int offset, int len)
        throws IOException
    {
        int[] arr = readFully(len);
        for (int i = 0; i < len; i++) {
            samples[i + offset] = arr[i];
        }
    }

    public int[] readFully(int len)
        throws IOException
    {
        int[] arr = new int[len];
        for (int i = 0; i < len; i++)
            arr[i] = readSample();
        return arr;
    }

    public void skipSamples(int n)
        throws IOException
    {
        skipSamples((long)n);
    }

    /*TODO try to make state of imageinput stream and this class reset to before
           skipSamples() was called! (could use mark() but in the case of no error,
           no matching call to reset() would be needed (doesn't seem correct))*/
    public void skipSamples(long n)
        throws IOException
    {
        long nWords = (ba * n) / 16;
        bOff += (ba * n) % 16;
        if (bOff > 16) {
            nWords++;
            bOff -= 16;
        }
        bMask = (~((1 << bOff) - 1)) & 0xFFFF;
        in.skipBytes((nWords - 1) * 2); //TODO should check if skipBytes() actually skips less than what it was told
        samplesPassed += n;
        cw = in.readShort();
    }

    public void skipToNextFrame()
        throws IOException
    {
        long numSamplesToSkip = samplesPerFrame - (samplesPassed % samplesPerFrame);
        skipSamples(numSamplesToSkip);
    }

    public void mark()
    {
        in.mark();
        markStack.push(new PixelDataStreamMark(bOff, cw, bMask, samplesPassed));
    }

    public void reset()
        throws IOException
    {
        PixelDataStreamMark mark;
        if (markStack.size() != 0) {
            in.reset();
            mark = (PixelDataStreamMark)markStack.pop();
            bOff = mark.offset;
            cw = mark.currentWord;
            bMask = mark.mask;
            samplesPassed = mark.samplesPassed;
        }
    }

    public void resetStream()
        throws IOException
    {
        in.seek(initialReadPos);
        bOff = initialStreamState.offset;
        cw = initialStreamState.currentWord;
        bMask = initialStreamState.mask;
        samplesPassed = initialStreamState.samplesPassed;
    }

    /**
     * Returns a sample from a specified <b>absolute pixel</b> and band.
     * @param pixel Index of pixel to retrieve
     * @param band Index of band [0..getSamplesPerPixel() - 1]
     * @return The sample value
     * @throws IOException On I/O error
     * @throws IllegalArgumentException If the required sample value has been passed
     */
    private final int getSampleInternal(int pixel, int band)
        throws IOException
    {
        int sampleIndex = (byPlane) ? (pixel / frameSize) * samplesPerFrame
                                      + (pixel % frameSize) * spp + band
                                    : pixel * spp + band;
        mark();
        int sample = 0;
        try {
            resetStream();
            skipSamples(sampleIndex);
            sample = readSample();
        }
        finally {
            reset();
        }
        return sample;
    }

    public int[] getPixel(int i, int j, int k)
        throws IOException
    {
        checkBounds(i, j, k, 0);
        int p[] = new int[spp];
        for(int s = 0; s < spp; s++)
            p[s] = getSampleInternal(i + j * cols + k * frameSize, s);
        return p;
    }

    public int getSample(int i, int j, int k, int band)
        throws IOException
    {
        checkBounds(i, j, k, band);
        return getSampleInternal(i + j * cols + k * frameSize, band);
    }

    private void checkBounds(int i, int j, int k, int band)
    {
        if(i < 0 || i >= cols || j < 0 || i >= rows || k < 0 || k >= nf || band < 0 || band >= spp)
            throw new ArrayIndexOutOfBoundsException("pixel[" + i + "," + j + ","
                + k + "], band = " + band + " is out of bounds");
    }

    private void checkBounds(int i, int band)
    {
        if(i < 0 || i >= size || band < 0 || band >= spp)
            throw new ArrayIndexOutOfBoundsException("pixel[" + i + "], band = "
                + band + "is out of bounds");
    }

    private final int getSampleBitsFromCell(int cell)
    {
        return signed ? (cell << sampleMaskLS) >> sampleMaskRS
                      : (cell << sampleMaskLS) >>> sampleMaskRS;
    }

    private final int getOverlayBitsFromCell(int cell)
    {
        return (cell & hOvlMask) >> hOvlMaskRS | cell & lOvlMask;
    }

    public PixelDataDescription getPixelDataDescription()
    {
        return pdDesc;
    }

    public DataBuffer getPixelDataBuffer(int frame)
    {
        return new DataBufferInt(getPixelDataArray(frame), size);
    }

    public int[][][] getPixelDataArray()
    {
        if (data != null)
            return data;
        else
            throw new IllegalStateException("No pixel data has been read");
    }

    public int[][] getPixelDataArray(int frame)
    {
        if (frame < 0 || frame >= nf)
            throw new IllegalArgumentException("Invalid frame: " + frame);
        if (data != null)
            return data[frame];
        else
            throw new IllegalStateException("No pixel data has been read");
    }

    public void readPixelData()
        throws IOException
    {
        readPixelData(false);
    }

    public void readPixelData(boolean grabOverlayData)
        throws IOException
    {
        if (data != null)
            return;
        
        data = new int[nf][spp][frameSize];
        
        final int numSamplesToRead = size * spp;
        int i = 0, ii, cell;
        int f, s = 0, p; //frame, sample, and pixel indicies
        
        while (i < numSamplesToRead) {
            cell = readSampleCell();
            f = i / samplesPerFrame;
            ii = i % samplesPerFrame;
            if (byPlane) {
                s = ii / frameSize;
                p = ii % frameSize;
                data[f][s][p] = (grabOverlayData) ? cell : getSampleBitsFromCell(cell);
            }
            else {
                p = ii / spp;
                data[f][s][p] = (grabOverlayData) ? cell : getSampleBitsFromCell(cell);
                s = (s + 1) % spp;
            }
            i++;
        }
        return;

        /*System.out.println("byplane = " + byPlane);
        System.out.println("buff len = " + in.length());
        System.out.println("size = " + size);
        
        int nSamples = size * spp;
        int nSamplesPerFrame = frameSize * spp;
        data = new int[nf][spp][frameSize];
        int bitsRead = 0;
        int cell = 0;
        int s = 0;
        if(16 % ba == 0)
        {
            int rounds = 16 / ba;
            int cm = (1 << ba) - 1;
            for(int n = 0; n < nSamples;)
            {
                int w = in.readShort() & 0xffff;
                int r;
                for(int mb = r = 0; r < rounds && n < nSamples; mb += ba)
                {
                    cell = (w & cm << mb) >>> mb;
                    int f = n / nSamplesPerFrame;
                    int nn = n % nSamplesPerFrame;
                    if(byPlane)
                    {
                        s = nn / frameSize;
                        int p = nn % frameSize;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                    } else
                    {
                        int p = nn / spp;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                        s = (s + 1) % spp;
                    }
                    n++;
                    r++;
                }

            }

        } else
        if(ba < 16)
        {
            int mb = ba;
            int cm = (1 << mb) - 1;
            for(int n = 0; n < nSamples;)
            {
                int w = in.readShort() & 0xffff;
                cell |= (w & cm) << bitsRead;
                int f = n / nSamplesPerFrame;
                int nn = n % nSamplesPerFrame;
                if(byPlane)
                {
                    s = nn / frameSize;
                    int p = nn % frameSize;
                    data[f][s][p] = getSampleBitsFromCell(cell);
                    if(grabOverlayData)
                        ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                } else
                {
                    int p = nn / spp;
                    data[f][s][p] = getSampleBitsFromCell(cell);
                    if(grabOverlayData)
                        ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                    s = (s + 1) % spp;
                }
                while(ba <= 16 - mb) 
                {
                    n++;
                    mb += ba;
                    cm = (1 << mb) - 1;
                    cell = (w & cm) >>> mb - ba;
                    f = n / nSamplesPerFrame;
                    nn = n % nSamplesPerFrame;
                    if(byPlane)
                    {
                        s = nn / frameSize;
                        int p = nn % frameSize;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                    } else
                    {
                        int p = nn / spp;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                        s = (s + 1) % spp;
                    }
                }
                n++;
                cm = ~cm;
                cell = (w & cm) >>> mb;
                bitsRead = 16 - mb;
                mb = ba - bitsRead;
                cm = (1 << mb) - 1;
            }

        } else
        {
            int mb = 16;
            int cm = 65535;
            for(int n = 0; n < nSamples;)
            {
                int w = in.readShort() & 0xffff;
                if(ba - bitsRead <= 16)
                {
                    cell |= (w & cm) << bitsRead;
                    int f = n / nSamplesPerFrame;
                    int nn = n % nSamplesPerFrame;
                    if(byPlane)
                    {
                        s = nn / frameSize;
                        int p = nn % frameSize;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                    } else
                    {
                        int p = nn / spp;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                        s = (s + 1) % spp;
                    }
                    n++;
                    cm = ~cm;
                    cell = (w & cm) >>> mb;
                    bitsRead = 16 - mb;
                    mb = (ba - bitsRead) % 16;
                    cm = (1 << mb) - 1;
                } else
                {
                    cell |= w << bitsRead;
                    bitsRead += 16;
                }
            }

        }
        return new DataBufferInt(data[frame], spp);*/
    }
}
