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

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelParam;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class PaletteColorParam extends BasicColorModelParam {
	private static final Logger log = Logger.getLogger(PaletteColorParam.class);
    private final byte[] r,g,b;

    /** Creates a new instance of PaletteColorParam */
    public PaletteColorParam(Dataset ds) {
        super(ds);
        if (super.min < 0) {
            throw new UnsupportedOperationException(
                    "Signed PALETTE COLOR not supported!");
        }
        this.r = generate(size, ds, Tags.RedPaletteColorLUTDescriptor,
                Tags.RedPaletteColorLUTData,
                Tags.SegmentedRedPaletteColorLUTData);
        this.g = generate(size, ds, Tags.GreenPaletteColorLUTDescriptor,
                Tags.GreenPaletteColorLUTData,
                Tags.SegmentedGreenPaletteColorLUTData);
        this.b = generate(size, ds, Tags.BluePaletteColorLUTDescriptor,
                Tags.BluePaletteColorLUTData,
                Tags.SegmentedBluePaletteColorLUTData);
    }

    public ColorModel newColorModel() {
    	log.info("Creatinga  plaett color model.");
        return new IndexColorModel(bits, size, r, g, b);
    }
    
    private static void throwLengthMismatch(int lutLen, int descLen) {
        throw new IllegalArgumentException("LUT Data length: " + lutLen
                +  " mismatch entry value: " + descLen + " in LUT Descriptor");
    }
        
    private static byte[] generate(int size, Dataset ds, int descTag,
                int dataTag, int segmTag) {
        int[] desc = ds.getInts(descTag);
        if (desc == null) {
            throw new IllegalArgumentException("Missing LUT Descriptor!");
        }
        if (desc.length != 3) {
            throw new IllegalArgumentException("Illegal LUT Descriptor: " + desc);
        }
        int len = desc[0] == 0 ? 0x10000 : desc[0];
        int off = desc[1];
        if (len < 0)
            throw new IllegalArgumentException("Illegal LUT Descriptor: len=" + len);
        if (off < 0)
            throw new IllegalArgumentException("off: " + off);
        ByteBuffer data = ds.getByteBuffer(dataTag);
        ByteBuffer segm = ds.getByteBuffer(segmTag);

        if (data == null && segm == null)
            throw new IllegalArgumentException("Missing LUT Data!");

//        if (data != null && segm != null)
//            throw new IllegalArgumentException("Native & Segmented LUT Data!");

        byte[] out = new byte[size];
        switch (desc[2]) {
            case 16:
                if (data != null) {
                    if (data.limit() != len * 2) {
                        throwLengthMismatch(data.limit(), len);
                    }
                    data.rewind();
                    for (int i = off; data.hasRemaining(); ++i) {
                        out[i] = (byte)(data.getShort() >> 8);
                    }
                } else {
                    inflate(segm, out, off, len);
                }
                break;
            case 8:
                if (data != null) {
                    if (data.limit() != len) {
                        throwLengthMismatch(data.limit(), len);
                    }
                    data.rewind();
                    short tmp;
                    for (int i = off; data.hasRemaining(); ) {
                        tmp = data.getShort();
                        out[i++] = (byte)(tmp & 0xff);
                        out[i++] = (byte)((tmp >> 8) & 0xff);
                    }
                    break;
                }
            default:
                throw new IllegalArgumentException (
                    "Illegal LUT Descriptor: bits=" + desc[2]);
        }
        Arrays.fill(out, 0, off, out[off]);
        Arrays.fill(out, off + len, size, out[off + len - 1]);
        return out;
    }

    private static void inflate(ByteBuffer segm, byte[] out, int off, int len) {
        int x0 = off;
        int y0 = 0;
        int y1,dy;
        segm.rewind();
        while (segm.hasRemaining()) {
            int op = segm.getShort();
            int n = segm.getShort() & 0xffff;
            switch (op) {
                case 0:
                    for (int j = 0; j < n; ++j) {
                        out[x0++] = (byte)((y0 = segm.getShort() & 0xffff) >> 8);
                    }
                    break;
                case 1:
                    y1 = segm.getShort() & 0xffff;
                    dy = y1 - y0;
                    for (int j = 0; j < n;) {
                        out[x0++] = (byte)((y0 + dy * ++j / n)>>8);
                    }
                    y0 = y1;
                    break;
                case 2:
                    int pos = (segm.getShort() & 0xffff)
                            | (segm.getShort() << 16);
                    segm.mark();
                    segm.position(pos);
                    for (int j = 0; j < n; ++j) {
                        int op2 = segm.getShort();
                        int n2 = segm.getShort() & 0xffff;
                        switch (op2) {
                            case 0:
                                for (int j2 = 0; j2 < n2; ++j2) {
                                    out[x0++] = (byte)((y0 = segm.getShort()
                                            & 0xffff) >> 8);
                                }
                                break;
                            case 1:
                                y1 = segm.getShort() & 0xffff;
                                dy = y1 - y0;
                                for (int j2 = 0; j2 < n2;) {
                                    out[x0++] = (byte)((y0 + dy*++j2 / n2)>>8);
                                }
                                y0 = y1;
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "illegal op code:" + op2
                                        + ", index:" + (segm.position()-4));
                        }
                    }
                    segm.reset();
                    break;
                default:
                    throw new IllegalArgumentException("illegal op code:" + op
                            + ", index:" + (segm.position()-4));
            }
        }
        if (x0 - off != len) {
            throwLengthMismatch(x0 - off, len);
        }
    }
    
    public ColorModelParam update(float center, float width, boolean inverse) {
        throw new UnsupportedOperationException();
    }
    
    public float getRescaleSlope() {
        throw new UnsupportedOperationException();
    }
    
    public float getRescaleIntercept() {
        throw new UnsupportedOperationException();
    }

    public float getWindowCenter(int index) {
        throw new UnsupportedOperationException();
    }
    
    public float getWindowWidth(int index) {
        throw new UnsupportedOperationException();
    }
    
    public int getNumberOfWindows() {
        throw new UnsupportedOperationException();
    }
    
    public Dataset getVOILUT() {
        throw new UnsupportedOperationException();
    }

    public boolean isMonochrome() {
        return false;
    }
    
    public boolean isCacheable() {
        return false;
    }
    
    public boolean isInverse() {
        return false;
    }
    
    public float toMeasureValue(int pxValue) {
        throw new UnsupportedOperationException();
    }
    
    public int toPixelValue(float measureValue) {
        throw new UnsupportedOperationException();
    }
}
