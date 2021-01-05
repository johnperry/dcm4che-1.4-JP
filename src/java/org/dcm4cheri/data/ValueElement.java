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

package org.dcm4cheri.data;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.WeakHashMap;

import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author     <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since      May, 2002
 * @version    $Revision: 4025 $ $Date: 2006-06-30 14:27:49 +0200 (Fr, 30 Jun 2006) $
 */
abstract class ValueElement extends DcmElementImpl {
    
    private static final WeakHashMap whm = new WeakHashMap();
    
    protected final ByteBuffer data;

    ValueElement(int tag, ByteBuffer data) {
        super(tag);
        this.data = data;
    }

    public int hashCode() {
        if (data == null || data.limit() == 0)
            return tag;
//        data.rewind();
        return tag ^ data.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ValueElement))
            return false;
        ValueElement ve = (ValueElement) o;
        if (tag != ve.tag)
            return false;
        if (data == null || data.limit() == 0)
            return (ve.data == null || ve.data.limit() == 0);
        if (ve.data == null || ve.data.limit() == 0)
            return false;
//        data.rewind();
//        ve.data.rewind();
        return data.equals(ve.data);
    }
    
    public DcmElement share() {
        WeakReference wr = (WeakReference) whm.get(this);
        if (wr != null) {
            DcmElement e = (DcmElement) wr.get();
            if (e != null)
                return e;
        }
        streamPos = -1L;
        whm.put(this, new WeakReference(this));
        return this;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public final int length() {
        return ((data.limit() + 1) & (~1));
    }

    public final boolean isEmpty() {
        return data.limit() == 0;
    }
    
    /**
     *  Gets the byteBuffer attribute of the ValueElement object
     *
     * @return    The byteBuffer value
     */
    public final ByteBuffer getByteBuffer() {
        return data.duplicate().order(data.order());
    }

    /**
     *  Gets the byteBuffer attribute of the ValueElement object
     *
     * @param  byteOrder  Description of the Parameter
     * @return            The byteBuffer value
     */
    public final ByteBuffer getByteBuffer(ByteOrder byteOrder) {
        if (data.order() != byteOrder) {
            swapOrder();
        }
        return getByteBuffer();
    }

    public int vm(SpecificCharacterSet cs) {
        return data.limit() == 0 ? 0 : 1;
    }

    public String getString(int index, SpecificCharacterSet cs) throws DcmValueException {
        return index < vm(null) ? Integer.toString(getInt(index)) : null;
    }

    public String[] getStrings(SpecificCharacterSet cs) throws DcmValueException {
        String[] ss = new String[vm(null)];
        for (int i = 0; i < ss.length; ++i) {
            ss[i] = Integer.toString(getInt(i));
        }
        return ss;
    }

    /**  Description of the Method */
    protected void swapOrder() {
        data.order(swap(data.order()));
    }

    // SS, US -------------------------------------------------------------
    private static ByteBuffer setShort(int v) {
        return ByteBuffer.wrap(new byte[2]).order(
                ByteOrder.LITTLE_ENDIAN).putShort(0, (short)v);
    }

    private static ByteBuffer setShorts(int[] a) {
        if (a.length == 0) {
            return EMPTY_VALUE;
        }

        if (a.length == 1) {
            return setShort(a[0]);
        }

        ByteBuffer bb =
            ByteBuffer.wrap(new byte[a.length << 1]).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            bb.putShort((short) a[i]);
        }
        bb.rewind();
        return bb;
    }

    private final static class SS extends ValueElement {
        SS(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x5353;
        }

        public final int vm(SpecificCharacterSet cs) {
            return data.limit() >> 1;
        }

        public final int getInt(int index) {
            return index < vm(null) ? data.getShort(index << 1) : 0;
        }

        public final int[] getInts() {
            int[] a = new int[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = data.getShort(i << 1);
            }
            return a;
        }

        protected void swapOrder() {
            swapWords(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            SpecificCharacterSet keyCS,
            SpecificCharacterSet dsCS) {
            int v;
            try {
                v = key.getInt();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = data.limit() >> 1; i < n; ++i) {
                if (data.getShort(i >> 1) == v) {
                    return true;
                }
            }
            return false;
        }

    }

    static DcmElement createSS(int tag, ByteBuffer data) {
        if ((data.limit() & 1) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " SS #"
                    + data.limit());
            return new SS(tag, EMPTY_VALUE);
        }
        return new SS(tag, data);
    }

    static DcmElement createSS(int tag) {
        return new SS(tag, EMPTY_VALUE);
    }

    static DcmElement createSS(int tag, int v) {
        return new SS(tag, setShort(v));
    }

    static DcmElement createSS(int tag, int[] a) {
        return new SS(tag, setShorts(a));
    }

    private final static class US extends ValueElement {
        US(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x5553;
        }

        public final int vm(SpecificCharacterSet cs) {
            return data.limit() >> 1;
        }

        public final int getInt(int index) {
            return index < vm(null) ? data.getShort(index << 1) & 0xffff : 0;
        }

        public final int[] getInts() {
            int[] a = new int[data.limit() >> 1];
            for (int i = 0; i < a.length; ++i) {
                a[i] = data.getShort(i << 1) & 0xffff;
            }
            return a;
        }

        protected void swapOrder() {
            swapWords(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            SpecificCharacterSet keyCS,
            SpecificCharacterSet dsCS) {
            int v;
            try {
                v = key.getInt();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = data.limit() >> 1; i < n; ++i) {
                if ((data.getShort(i >> 1) & 0xffff) == v) {
                    return true;
                }
            }
            return false;
        }
    }

    static DcmElement createUS(int tag, ByteBuffer data) {
        if ((data.limit() & 1) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " US #"
                    + data.limit());
            return new US(tag, EMPTY_VALUE);
        }
        return new US(tag, data);
    }

    static DcmElement createUS(int tag) {
        return new US(tag, EMPTY_VALUE);
    }

    static DcmElement createUS(int tag, int s) {
        return new US(tag, setShort(s));
    }

    static DcmElement createUS(int tag, int[] s) {
        return new US(tag, setShorts(s));
    }

    // SL, UL -------------------------------------------------------------
    private static ByteBuffer setInt(int v) {
        return ByteBuffer.wrap(new byte[4]).order(
            ByteOrder.LITTLE_ENDIAN).putInt(0, v);
    }

    private static ByteBuffer setInts(int[] a) {
        if (a.length == 0) {
            return EMPTY_VALUE;
        }

        if (a.length == 1) {
            return setInt(a[0]);
        }

        ByteBuffer bb =
            ByteBuffer.wrap(new byte[a.length << 2]).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            bb.putInt(a[i]);
        }
        bb.rewind();
        return bb;
    }

    private abstract static class Int extends ValueElement {
        Int(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vm(SpecificCharacterSet cs) {
            return data.limit() >> 2;
        }

        public final int getInt(int index) {
            return index < vm(null) ? data.getInt(index << 2) : 0;
        }

        public final int[] getInts() {
            int[] a = new int[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = data.getInt(i << 2);
            }
            return a;
        }
        
        protected void swapOrder() {
            swapInts(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            SpecificCharacterSet keyCS,
            SpecificCharacterSet dsCS) {
            int v;
            try {
                v = key.getInt();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = data.limit() >> 2; i < n; ++i) {
                if (data.getInt(i >> 2) == v) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class SL extends Int {
        SL(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x534C;
        }
    }

    static DcmElement createSL(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " SL #"
                    + data.limit());
            return new SL(tag, EMPTY_VALUE);
        }
        return new SL(tag, data);
    }

    static DcmElement createSL(int tag) {
        return new SL(tag, EMPTY_VALUE);
    }

    static DcmElement createSL(int tag, int v) {
        return new SL(tag, setInt(v));
    }

    static DcmElement createSL(int tag, int[] a) {
        return new SL(tag, setInts(a));
    }

    static class UL extends Int {
        UL(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x554C;
        }

        public String getString(int index, SpecificCharacterSet cs) throws DcmValueException {
            return index < vm(null) ? Long.toString(getInt(index) & 0xffffffffL) : null;
        }

        public String[] getStrings(SpecificCharacterSet cs) throws DcmValueException {
            String[] ss = new String[vm(null)];
            for (int i = 0; i < ss.length; ++i) {
                ss[i] = Long.toString(getInt(i) & 0xffffffffL);
            }
            return ss;
        }        
    }

    static DcmElement createUL(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " UL #"
                    + data.limit());
            return new UL(tag, EMPTY_VALUE);
        }
        return new UL(tag, data);
    }

    static DcmElement createUL(int tag) {
        return new UL(tag, EMPTY_VALUE);
    }

    static DcmElement createUL(int tag, int v) {
        return new UL(tag, setInt(v));
    }

    static DcmElement createUL(int tag, int[] a) {
        return new UL(tag, setInts(a));
    }

    // AT -------------------------------------------------------------
    private static ByteBuffer setTag(int v) {
        return ByteBuffer
            .wrap(new byte[4])
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(0, (short) (v >> 16))
            .putShort(2, (short) v);
    }

    private static ByteBuffer setTags(int[] a) {
        if (a.length == 0) {
            return EMPTY_VALUE;
        }

        if (a.length == 1) {
            return setTag(a[0]);
        }

        ByteBuffer bb =
            ByteBuffer.wrap(new byte[a.length << 2]).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            bb.putShort((short) (a[i] >> 16)).putShort((short) a[i]);
        }
        bb.rewind();
        return bb;
    }

    private final static class AT extends ValueElement {
        AT(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x4154;
        }

        public final int vm(SpecificCharacterSet cs) {
            return data.limit() >> 2;
        }

        public final int getTag(int index) {
            return index < vm(null) ? toTag(index) : 0;
        }

        public final int[] getTags() {
            int[] a = new int[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = toTag(i);
            }
            return a;
        }

        private final int toTag(int i) {
            return (data.getShort(i << 2) << 16)
                    | (data.getShort((i << 2) + 2) & 0xffff);
        }

        public String getString(int index, SpecificCharacterSet cs)
            throws DcmValueException {
            return index < vm(null) ? Tags.toHexString(toTag(index), 8) : null;
        }

        public String[] getStrings(SpecificCharacterSet cs) throws DcmValueException {
            String[] a = new String[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = Tags.toHexString(toTag(i), 8);
            }
            return a;
        }

        protected void swapOrder() {
            swapWords(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            SpecificCharacterSet keyCS,
            SpecificCharacterSet dsCS) {
            int v;
            try {
                v = key.getTag();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = vm(null); i < n; ++i) {
                if (getTag(i) == v) {
                    return true;
                }
            }
            return false;
        }
    }

    static DcmElement createAT(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " AT #"
                    + data.limit());
            return new AT(tag, EMPTY_VALUE);
        }
        return new AT(tag, data);
    }

    static DcmElement createAT(int tag) {
        return new AT(tag, EMPTY_VALUE);
    }

    static DcmElement createAT(int tag, int v) {
        return new AT(tag, setTag(v));
    }

    static DcmElement createAT(int tag, int[] a) {
        return new AT(tag, setTags(a));
    }

    // FL -------------------------------------------------------------
    private static ByteBuffer setFloat(float v) {
        return ByteBuffer.wrap(new byte[4]).order(
            ByteOrder.LITTLE_ENDIAN).putFloat(0, v);
    }

    private static ByteBuffer setFloats(float[] a) {
        if (a.length == 0) {
            return EMPTY_VALUE;
        }

        if (a.length == 1) {
            return setFloat(a[0]);
        }

        ByteBuffer bb =
            ByteBuffer.wrap(new byte[a.length << 2]).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            bb.putFloat(a[i]);
        }
        bb.rewind();
        return bb;
    }

    private final static class FL extends ValueElement {
        FL(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vm(SpecificCharacterSet cs) {
            return data.limit() >> 2;
        }

        public final int vr() {
            return 0x464C;
        }

        public final float getFloat(int index) {
            return index < vm(null) ? data.getFloat(index << 2) : 0.f;
        }

        public final float[] getFloats() {
            float[] a = new float[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = data.getFloat(i << 2);
            }
            return a;
        }

        public String getString(int index, SpecificCharacterSet cs) {
            return index < vm(null) ? Float.toString(data.getFloat(index << 2)) : null;
        }

        public String[] getStrings(SpecificCharacterSet cs) throws DcmValueException {
            String[] a = new String[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = Float.toString(data.getFloat(i << 2));
            }
            return a;
        }

        protected void swapOrder() {
            swapInts(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            SpecificCharacterSet keyCS,
            SpecificCharacterSet dsCS) {
            float v;
            try {
                v = key.getFloat();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = vm(null); i < n; ++i) {
                if (data.getFloat(i << 2) == v) {
                    return true;
                }
            }
            return false;
        }
    }

    static DcmElement createFL(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " FL #"
                    + data.limit());
            return new FL(tag, EMPTY_VALUE);
        }

        return new FL(tag, data);
    }

    static DcmElement createFL(int tag) {
        return new FL(tag, EMPTY_VALUE);
    }

    static DcmElement createFL(int tag, float v) {
        return new FL(tag, setFloat(v));
    }

    static DcmElement createFL(int tag, float[] a) {
        return new FL(tag, setFloats(a));
    }

    // FD -------------------------------------------------------------
    private static ByteBuffer setDouble(double v) {
        return ByteBuffer.wrap(new byte[8]).order(
            ByteOrder.LITTLE_ENDIAN).putDouble(0, v);
    }

    private static ByteBuffer setDoubles(double[] a) {
        if (a.length == 0) {
            return EMPTY_VALUE;
        }

        if (a.length == 1) {
            return setDouble(a[0]);
        }

        ByteBuffer bb =
            ByteBuffer.wrap(new byte[a.length << 3]).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            bb.putDouble(a[i]);
        }
        bb.rewind();
        return bb;
    }

    private final static class FD extends ValueElement {
        FD(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vm(SpecificCharacterSet cs) {
            return data.limit() >>> 3;
        }

        public final int vr() {
            return 0x4644;
        }

        public final double getDouble(int index) {
            return index < vm(null) ? data.getDouble(index << 3) : 0.;
        }

        public final double[] getDoubles() {
            double[] a = new double[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = data.getDouble(i << 3);
            }
            return a;
        }

        public String getString(int index, SpecificCharacterSet cs) {
            return index < vm(null) ? Double.toString(data.getDouble(index << 3)) : null;
        }

        public String[] getStrings(SpecificCharacterSet cs) throws DcmValueException {
            String[] a = new String[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = Double.toString(data.getDouble(i << 3));
            }
            return a;
        }

        protected void swapOrder() {
            swapLongs(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            SpecificCharacterSet keyCS,
            SpecificCharacterSet dsCS) {
            double v;
            try {
                v = key.getDouble();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = vm(null); i < n; ++i) {
                if (data.getDouble(i << 3) == v) {
                    return true;
                }
            }
            return false;
        }
    }

    static DcmElement createFD(int tag, ByteBuffer data) {
        if ((data.limit() & 7) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " FD #"
                    + data.limit());
            return new FD(tag, EMPTY_VALUE);
        }
        return new FD(tag, data);
    }

    static DcmElement createFD(int tag) {
        return new FD(tag, EMPTY_VALUE);
    }

    static DcmElement createFD(int tag, double v) {
        return new FD(tag, setDouble(v));
    }

    static DcmElement createFD(int tag, double[] a) {
        return new FD(tag, setDoubles(a));
    }

    // OF, OW, OB, UN -------------------------------------------------------------

    private final static class OF extends ValueElement {
        OF(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x4F46;
        }

        public final float getFloat(int index) {
            return index < (data.limit() >> 2) 
                    ? data.getFloat(index << 2) : 0.f;
        }

        public final float[] getFloats() {
            float[] a = new float[data.limit() >> 2];
            for (int i = 0; i < a.length; ++i) {
                a[i] = data.getFloat(i << 2);
            }
            return a;
        }

        public String getString(int index, SpecificCharacterSet cs) {
            return getBoundedString(Integer.MAX_VALUE, index, cs);
        }

        public String getBoundedString(int maxLen, int index, SpecificCharacterSet cs) {
            return index < vm(null) ? StringUtils.promptOF(getByteBuffer(), maxLen) : null;
        }

        public String[] getStrings(SpecificCharacterSet cs) throws DcmValueException {
            return getBoundedStrings(Integer.MAX_VALUE, cs);
        }

        public String[] getBoundedStrings(int maxLen, SpecificCharacterSet cs) {
            String[] a = new String[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = StringUtils.promptOF(getByteBuffer(), maxLen);
            }
            return a;
        }

        protected void swapOrder() {
            swapInts(data);
        }
    }

    static DcmElement createOF(int tag) {
        return new OF(tag, EMPTY_VALUE);
    }

    static DcmElement createOF(int tag, float[] v) {
        ByteBuffer buf = ByteBuffer.allocate(v.length << 2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < v.length; ++i) {
            buf.putFloat(v[i]);
        }
        buf.rewind();
        return new OF(tag, buf);
    }

    static DcmElement createOF(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " OW #"
                    + data.limit());
            return new OF(tag, EMPTY_VALUE);
        }
        return new OF(tag, data);
    }

    private final static class OW extends ValueElement {
        OW(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x4F57;
        }

        public final int getInt(int index) {
            return index < (data.limit() >> 1) ? 
                    data.getShort(index << 1) & 0xffff : 0;
        }

        public final int[] getInts() {
            int[] a = new int[data.limit() >> 1];
            for (int i = 0; i < a.length; ++i) {
                a[i] = data.getShort(i << 1) & 0xffff;
            }
            return a;
        }

        public String getString(int index, SpecificCharacterSet cs) {
            return getBoundedString(Integer.MAX_VALUE, index, cs);
        }

        public String getBoundedString(int maxLen, int index, SpecificCharacterSet cs) {
            return index < vm(null) ? StringUtils.promptOW(getByteBuffer(), maxLen) : null;
        }

        public String[] getStrings(SpecificCharacterSet cs) {
            return getBoundedStrings(Integer.MAX_VALUE, cs);
        }

        public String[] getBoundedStrings(int maxLen, SpecificCharacterSet cs) {
            String[] a = new String[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = StringUtils.promptOW(getByteBuffer(), maxLen);
            }
            return a;
        }

        protected void swapOrder() {
            swapWords(data);
        }
    }

    static DcmElement createOW(int tag) {
        return new OW(tag, EMPTY_VALUE);
    }

    static DcmElement createOW(int tag, short[] v) {
        ByteBuffer buf = ByteBuffer.allocate(v.length << 1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < v.length; ++i) {
            buf.putShort(v[i]);
        }
        buf.rewind();
        return new OW(tag, buf);
    }

    static DcmElement createOW(int tag, ByteBuffer data) {
        if ((data.limit() & 1) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " OW #"
                    + data.limit());
            return new OW(tag, EMPTY_VALUE);
        }
        return new OW(tag, data);
    }

    private final static class OB extends ValueElement {
        OB(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x4F42;
        }

        public String getString(int index, SpecificCharacterSet cs) {
            return getBoundedString(Integer.MAX_VALUE, index, cs);
        }

        public String getBoundedString(int maxLen, int index, SpecificCharacterSet cs) {
            return index < vm(null) ? StringUtils.promptOB(getByteBuffer(), maxLen) : null;
        }

        public String[] getStrings(SpecificCharacterSet cs) {
            return getBoundedStrings(Integer.MAX_VALUE, cs);
        }

        public String[] getBoundedStrings(int maxLen, SpecificCharacterSet cs) {
            String[] a = new String[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = StringUtils.promptOB(getByteBuffer(), maxLen);
            }
            return a;
        }
    }

    static DcmElement createOB(int tag) {
        return new OB(tag, EMPTY_VALUE);
    }

    static DcmElement createOB(int tag, ByteBuffer v) {
        return new OB(tag, v);
    }

    static DcmElement createOB(int tag, byte[] v) {
        return new OB(tag, ByteBuffer.wrap(v).order(ByteOrder.LITTLE_ENDIAN));
    }

    private final static class UN extends ValueElement {
        UN(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x554E;
        }

        public String getString(int index, SpecificCharacterSet cs) {
            return getBoundedString(Integer.MAX_VALUE, index, cs);
        }

        public String getBoundedString(int maxLen, int index, SpecificCharacterSet cs) {
            return index < vm(null) ? StringUtils.promptOB(getByteBuffer(), maxLen) : null;
        }

        public String[] getStrings(SpecificCharacterSet cs) throws DcmValueException {
            return getBoundedStrings(Integer.MAX_VALUE, cs);
        }

        public String[] getBoundedStrings(int maxLen, SpecificCharacterSet cs) {
            String[] a = new String[vm(null)];
            for (int i = 0; i < a.length; ++i) {
                a[i] = StringUtils.promptOB(getByteBuffer(), maxLen);
            }
            return a;
        }
    }

    static DcmElement createUN(int tag) {
        return new UN(tag, EMPTY_VALUE);
    }

    static DcmElement createUN(int tag, ByteBuffer v) {
        return new UN(tag, v);
    }

    static DcmElement createUN(int tag, byte[] v) {
        return new UN(tag, ByteBuffer.wrap(v).order(ByteOrder.LITTLE_ENDIAN));
    }
}
