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

package org.dcm4che.client;

import java.nio.ByteBuffer;

import org.dcm4che.dict.VRs;

/**
 * Wraps a <code>ByteBuffer</code> for a DICOM look-up table (LUT).
 * @author jforaci
 * @since Jul 29, 2003
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
final class LutBuffer
{
    public static final int TYPE_BYTE = 0;
    public static final int TYPE_WORD = 1;

    private final ByteBuffer buff;
    private final int dataType;
    private final int lutSize, firstValueMapped, depth;

    /**
     * Creates a LutBuffer with the specified backing buffer. Note that
     * the value of the third descriptor (lut depth) determines whether the
     * size of the entries with be grabbed in bytes or words. The depth of the
     * LUT must be from 8 to 16 inclusive.
     * @param backend The backing <code>ByteBuffer</code> of the LUT.
     * @param descriptor Assumed to be <code>int</code>s with values that would
     *  be the sign-extended or unsigned value from a dataset, depending on the
     *  implied or explicit VR when reading the dataset.
     * @param vr Overrides the actual value (only for the second descriptor) to
     *  be interpreted as <code>vr</code>, which must be either <code>VRs.US</code>
     *  or <code>VRs.SS</code>.
     */
    public LutBuffer(ByteBuffer backend, int[] descriptor, int vr)
    {
        if (descriptor[2] <= 8)
            this.dataType = TYPE_BYTE;
        else if (descriptor[2] <= 16)
            this.dataType = TYPE_WORD;
        else
            throw new IllegalArgumentException("The LUT's depth must be within 8 and 16, inclusive");
        lutSize = (descriptor[0] == 0) ? (1 << 16) : descriptor[0] & 0xFFFF; //always US
        if (vr == VRs.US)
            firstValueMapped = descriptor[1] & 0xFFFF;
        else if (vr == VRs.SS)
            firstValueMapped = (descriptor[1] << 16) >> 16;
        else throw new IllegalArgumentException("VR may only be US or SS");
        depth = descriptor[2] & 0xFFFF; //always US
        this.buff = backend;
    }

    /**
     * Creates a LutBuffer with the specified backing buffer. The depth of the
     * LUT must be from 8 to 16 inclusive.
     * @param backend The backing <code>ByteBuffer</code> of the LUT.
     * @param lutSize The number of entries in the LUT
     * @param firstValueMapped The value mapped to the first entry of this LUT
     * @param depthInBits The depth of this LUT's entries
     */
    public LutBuffer(ByteBuffer backend, int lutSize, int firstValueMapped,
        int depthInBits)
    {
        if (depthInBits <= 8)
            this.dataType = TYPE_BYTE;
        else if (depthInBits <= 16)
            this.dataType = TYPE_WORD;
        else
            throw new IllegalArgumentException("The LUT's depth must be within 8 and 16, inclusive");
        this.lutSize = lutSize;
        this.firstValueMapped = firstValueMapped;
        this.depth = depthInBits;
        this.buff = backend;
    }

    /**
     * Creates a LutBuffer with the specified backing buffer and the data type
     * allocated for the LUT entries. The depth of the LUT must be from 8 to 16
     * inclusive.
     * @param backend The backing <code>ByteBuffer</code> of the LUT.
     * @param lutSize The number of entries in the LUT
     * @param firstValueMapped The value mapped to the first entry of this LUT
     * @param depthInBits The depth of this LUT's entries
     * @param dataType The actual data type allocated for each entry
     *  (<code>TYPE_BYTE</code> or <code>TYPE_WORD</code>)
     */
    public LutBuffer(ByteBuffer backend, int lutSize, int firstValueMapped,
        int depthInBits, int dataType)
    {
        if (dataType != TYPE_BYTE && dataType != TYPE_WORD)
            throw new IllegalArgumentException("Bad dataType");
        this.dataType = dataType;
        this.lutSize = lutSize;
        this.firstValueMapped = firstValueMapped;
        this.depth = depthInBits;
        this.buff = backend;
    }

    public int[] getDescriptor()
    {
        return new int[] {lutSize, firstValueMapped, depth};
    }

    public int getEntry(int index)
    {
        return (dataType == TYPE_BYTE)
            ? (int)(buff.get(index) & 0xFF)
            : (int)(buff.getShort(index * 2) & 0xFFFF);
    }

    public int getEntryFromInput(int value)
    {
        if (value <= firstValueMapped)
            return getEntry(0);
        else if (value - firstValueMapped >= lutSize)
            return getEntry(lutSize - 1);
        else
            return getEntry(value - firstValueMapped);
    }

    public int getDepth() {
        return depth;
    }

    public int getFirstValueMapped() {
        return firstValueMapped;
    }

    public int getLutSize() {
        return lutSize;
    }
}
