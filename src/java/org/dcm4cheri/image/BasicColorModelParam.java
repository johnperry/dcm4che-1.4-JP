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

import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelParam;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @version $Revision: 4113 $ $Date: 2007-06-15 15:09:11 +0200 (Fr, 15 Jun 2007) $
 */
abstract class BasicColorModelParam
    implements ColorModelParam
{
	private static final Logger log = Logger.getLogger(BasicColorModelParam.class);   

    protected final int dataType;
    protected final int size;
    protected final int bits;
    protected final int min;
    protected final int max;
    protected final int shiftmask;
    protected final int alloc;
    protected final int hBit;

    protected BasicColorModelParam(Dataset ds)
    {
        alloc = ds.getInt(Tags.BitsAllocated, 8);
        if (alloc <= 8)
            dataType = DataBuffer.TYPE_BYTE;
        else if (alloc <= 16)
            dataType = DataBuffer.TYPE_USHORT;
        else if (alloc <= 32) //dataType = DataBuffer.TYPE_INT
            throw new IllegalArgumentException(alloc + " Bits Allocated not supported for Java BufferedImages");
        else
            throw new IllegalArgumentException("Bits allocated " + alloc + " not supported");
        bits = ds.getInt(Tags.BitsStored, 8);
        hBit = ds.getInt(Tags.HighBit, bits - 1);
        size = 1 << bits;
        if(ds.getInt(Tags.PixelRepresentation, 0) == 0) {
            min = 0;
            max = size;
        }
        else {
        	max = size>>1;//max is greatest pos value + 1 (as in unsigned!)
            min = -max;
        }
        shiftmask = 32 - bits;
        log.debug("max="+max+" min="+min+" bits="+bits+" hBit="+hBit+" size="+size);
    }

    protected BasicColorModelParam(BasicColorModelParam other)
    {
        alloc = other.alloc;
        hBit = other.hBit;
        dataType = other.dataType;
        size = other.size;
        bits = other.bits;
        min = other.min;
        max = other.max;
        shiftmask = other.shiftmask;
    }

    public final int toSampleValue(int pxValue)
    {
        return min != 0 ? (pxValue << shiftmask) >> shiftmask : (pxValue << shiftmask) >>> shiftmask;
    }

    public final int toPixelValueRaw(int sampleValue)
    {
        int bsMask = (1 << bits) - 1;
        int packedValue = (sampleValue & bsMask) << (hBit - bits) + 1;
        return packedValue;
    }

    public abstract ColorModel newColorModel();

    public abstract ColorModelParam update(float f, float f1, boolean flag);

    public abstract float getRescaleSlope();

    public abstract float getRescaleIntercept();

    public abstract float getWindowCenter(int i);

    public abstract float getWindowWidth(int i);

    public abstract int getNumberOfWindows();

    public abstract float toMeasureValue(int i);

    public abstract int toPixelValue(float f);

    public abstract boolean isInverse();

    public abstract boolean isCacheable();
}
