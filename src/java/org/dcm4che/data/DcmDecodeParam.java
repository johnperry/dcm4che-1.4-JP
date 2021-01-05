/*                                                                           *
 *  Copyright (c) 2002-2005 by TIANI MEDGRAPH AG                             *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */

package org.dcm4che.data;

import java.nio.ByteOrder;

import org.dcm4che.dict.UIDs;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmDecodeParam {
    
    public final ByteOrder byteOrder;
    
    public final boolean explicitVR;
    
    public final boolean deflated;
    
    public final boolean encapsulated;

    public DcmDecodeParam(ByteOrder byteOrder, boolean explicitVR,
            boolean deflated, boolean encapsulated) {
        if (byteOrder == null)
            throw new NullPointerException();
        this.byteOrder = byteOrder;
        this.explicitVR = explicitVR;
        this.deflated = deflated;
        this.encapsulated = encapsulated;
    }

    public String toString() {
        return (explicitVR ? "explVR-" : "implVR-")
            + byteOrder.toString()
            + (deflated ? " deflated" : "")
            + (encapsulated ? " encapsulated" : "");
    }
    
    public final static DcmEncodeParam IVR_LE = new DcmEncodeParam(
            ByteOrder.LITTLE_ENDIAN, false, false, false, true, true, true);

    public final static DcmEncodeParam IVR_BE = new DcmEncodeParam(
            ByteOrder.BIG_ENDIAN, false, false, false, true, true, true);

    public final static DcmEncodeParam EVR_LE = new DcmEncodeParam(
            ByteOrder.LITTLE_ENDIAN, true, false, false, true, true, true);

    public final static DcmEncodeParam EVR_BE = new DcmEncodeParam(
            ByteOrder.BIG_ENDIAN, true, false, false, true, true, true);

    public final static DcmEncodeParam DEFL_EVR_LE = new DcmEncodeParam(
            ByteOrder.LITTLE_ENDIAN, true, true, false, true, true, true);

    public final static DcmEncodeParam ENCAPS_EVR_LE = new DcmEncodeParam(
            ByteOrder.LITTLE_ENDIAN, true, false, true, true, true, true);

    public final static DcmEncodeParam valueOf(String tsuid) {
        if (UIDs.ImplicitVRLittleEndian.equals(tsuid))
            return IVR_LE;
        if (UIDs.ExplicitVRLittleEndian.equals(tsuid)
                || UIDs.JPIPReferenced.equals(tsuid)
                || UIDs.NoPixelData.equals(tsuid))
            return EVR_LE;
        if (UIDs.DeflatedExplicitVRLittleEndian.equals(tsuid)
                || UIDs.JPIPReferencedDeflate.equals(tsuid)
                || UIDs.NoPixelDataDeflate.equals(tsuid))
            return DEFL_EVR_LE;
        if (UIDs.ExplicitVRBigEndian.equals(tsuid))
            return EVR_BE;
        return ENCAPS_EVR_LE;
    }
}
