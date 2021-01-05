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

package org.dcm4cheri.image;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.dcm4che.dict.VRs;
import org.dcm4che.image.PixelDataDescription;
import org.dcm4che.image.PixelDataWriter;

public class PixelDataWriterImpl implements PixelDataWriter
{
    private final ImageOutputStream out;
    private final int[][][] data;
    private final int sampleOffset;
    private final boolean containsOverlayData;
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

    PixelDataWriterImpl(int[][][] data, boolean containsOverlayData,
        PixelDataDescription desc, ImageOutputStream out)
    {
        if (desc == null)
            throw new IllegalArgumentException("pixel data description can not be null");
        this.containsOverlayData = containsOverlayData;
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
        if (out == null)
            throw new IllegalArgumentException("out can not be null");
        this.out = out;
        if (data == null)
            throw new IllegalArgumentException("data can not be null");
        this.data = data;
        //offset in bits from low-order bit of a sample
        sampleOffset = hb - bs + 1;
        //set byte-order
        if (pixelDataVr == VRs.OW)
            out.setByteOrder(byteOrder);
        else
            out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }

    public ByteBuffer writePixelDataToByteBuffer()
    {
        ByteArrayOutputStream buff = new ByteArrayOutputStream((size * ba) >>> 3)
            {
                //avoid ByteArrayOutputStream making a copy
                public synchronized byte[] toByteArray()
                {
                    return buf;
                }
            };
        try {
            ImageOutputStream out = ImageIO.createImageOutputStream(new BufferedOutputStream(buff));
            writePixelData(out);
            out.flush();
            out.close();
            return ByteBuffer.wrap(buff.toByteArray(), 0, buff.size());
        }
        catch (IOException ioe) {
            return null;
        }
    }

    public void writePixelData()
        throws IOException
    {
        writePixelData(out);
    }

    public void writePixelData(ImageOutputStream out)
        throws IOException
    {
        final int cellBits = ba; // (writeOverlayData && containsOverlayData) ? ba : bs;
        int w = 0, bNeeded = 16, bUsed = 0, read;
        int f, p, s;

        if (byPlane) {
            for (f = 0; f < nf; f++) {
                for (s = 0; s < spp; s++) {
                    for (p = 0; p < frameSize; ) {
                        //pre-conditions: bUsed is setup, bNeeded is [1..16]
                        w |= (data[f][s][p] >>> (bUsed)) << (16 - bNeeded);
                        read = cellBits - bUsed;
                        if (read < bNeeded) {
                            bUsed = 0;
                            bNeeded -= read;
                            p++;
                        }
                        else {
                            bUsed += bNeeded;
                            if (bUsed == cellBits) {
                                p++;
                                bUsed = 0;
                            }
                            bNeeded = 16;
                            out.writeShort(w);
                            w = 0;
                        }
                    }
                }
            }
        }
        else {
            for (f = 0; f < nf; f++) {
                for (p = 0; p < frameSize; p++) {
                    for (s = 0; s < spp; ) {
                        //pre-conditions: bUsed is setup, bNeeded is [1..16]
                        w |= (data[f][s][p] >>> (bUsed)) << (16 - bNeeded);
                        read = cellBits - bUsed;
                        if (read < bNeeded) {
                            bUsed = 0;
                            bNeeded -= read;
                            s++;
                        }
                        else {
                            bUsed += bNeeded;
                            if (bUsed == cellBits) {
                                s++;
                                bUsed = 0;
                            }
                            bNeeded = 16;
                            out.writeShort(w);
                            w = 0;
                        }
                    }
                }
            }
        }
    }

    public PixelDataDescription getPixelDataDescription()
    {
        return pdDesc;
    }
}
