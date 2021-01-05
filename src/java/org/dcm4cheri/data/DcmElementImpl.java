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

package org.dcm4cheri.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.PersonName;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.VRs;
import org.dcm4cheri.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since March 2002
 * @version $Revision: 3994 $ $Date: 2006-05-18 00:10:23 +0200 (Do, 18 Mai 2006) $
 */
class DcmElementImpl implements DcmElement {
    static final Logger log = Logger.getLogger(DcmElementImpl.class);
    static final TagDictionary DICT =
        DictionaryFactory.getInstance().getDefaultTagDictionary();

    static final byte[] BYTE0 = {};
    static final ByteBuffer EMPTY_VALUE =
        ByteBuffer.wrap(BYTE0).order(ByteOrder.LITTLE_ENDIAN);

    int tag;
    long streamPos = -1L;

    /** Creates a new instance of ElementImpl */
    public DcmElementImpl(int tag) {
        this.tag = tag;
    }

    public DcmElement share() {
        return this;
    }

    public final int tag() {
        return tag;
    }

    public int vr() {
        return VRs.NONE;
    }

    /**
     * @deprecated may return wrong number in case of multi-byte char sets;
     * use {@link #vm(SpecificCharacterSet)} or for number of items/fragments
     * in sequences {@link #countItems()} instead.
     */
    public final int vm() {
        return vm(null);
    }

    public int vm(SpecificCharacterSet cs) {
        return 0;
    }

    public int countItems() {
        return 0;
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    public int length() {
        return -1;
    }

    public final DcmElement setStreamPosition(long streamPos) {
        this.streamPos = streamPos;
        return this;
    }

    public final long getStreamPosition() {
        return streamPos;
    }

    public int hashCode() {
        return tag;
    }

    public String toString() {
        return toString(
            tag,
            vr(),
            vm(null),
            length(),
            StringUtils.promptValue(vr(), getByteBuffer(), 64));
    }

    static String toString(int tag, int vr, int vm, int len, String val) {
        return DICT.toString(tag)
            + ","
            + VRs.toString(vr)
            + ",*"
            + vm
            + ",#"
            + len
            + ",["
            + val
            + "]";
    }

    boolean match(
        DcmElement key,
        boolean ignorePNCase,
        boolean ignoreEmpty,
        SpecificCharacterSet keyCS,
        SpecificCharacterSet dsCS) {
        if (key == null) {
            return true;
        }
        if (key.tag() != tag || key.vr() != vr()) {
            return false;
        }
        if (isEmpty() || key.isEmpty()) {
            return ignoreEmpty || (isEmpty() && key.isEmpty());
        }
        return matchValue(key, ignorePNCase, ignoreEmpty, keyCS, dsCS);
    }

    protected boolean matchValue(
        DcmElement key,
        boolean ignorePNCase,
        boolean ignoreEmpty,
        SpecificCharacterSet keyCS,
        SpecificCharacterSet dsCS) {
        throw new UnsupportedOperationException("" + this);
    }

    public ByteBuffer getByteBuffer() {
        throw new UnsupportedOperationException("" + this);
    }

    public ByteBuffer getByteBuffer(ByteOrder byteOrder) {
        throw new UnsupportedOperationException("" + this);
    }

    public boolean hasDataFragments() {
        return false;
    }

    public ByteBuffer getDataFragment(int index) {
        throw new UnsupportedOperationException("" + this);
    }

    public ByteBuffer getDataFragment(int index, ByteOrder byteOrder) {
        throw new UnsupportedOperationException("" + this);
    }

    public int getDataFragmentLength(int index) {
        throw new UnsupportedOperationException("" + this);
    }

    public PersonName getPersonName(SpecificCharacterSet cs)
        throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public PersonName getPersonName(int index, SpecificCharacterSet cs)
        throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public PersonName[] getPersonNames(SpecificCharacterSet cs)
    throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public String getString(SpecificCharacterSet cs) throws DcmValueException {
        return getString(0, cs);
    }

    public String getString(int index, SpecificCharacterSet cs) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public String[] getStrings(SpecificCharacterSet cs) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public String getBoundedString(int maxLen, SpecificCharacterSet cs)
        throws DcmValueException {
        return getBoundedString(maxLen, 0, cs);
   }

    public String getBoundedString(int maxLen, int index, SpecificCharacterSet cs)
        throws DcmValueException {
        return getString(index, cs);
    }

    public String[] getBoundedStrings(int maxLen, SpecificCharacterSet cs)
        throws DcmValueException {
        return getStrings(cs);
    }

    public int getInt() throws DcmValueException {
        return getInt(0);
    }

    public int getInt(int index) throws DcmValueException {
         throw new UnsupportedOperationException("" + this);
    }

    public int[] getInts() throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public int getTag() throws DcmValueException {
        return getTag(0);
    }

    public int getTag(int index) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public int[] getTags() throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public float getFloat() throws DcmValueException {
        return getFloat(0);
    }

    public float getFloat(int index) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public float[] getFloats() throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public double getDouble() throws DcmValueException {
        return getDouble(0);
    }

    public double getDouble(int index) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public double[] getDoubles() {
        throw new UnsupportedOperationException("" + this);
    }

    public Date getDate() throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public Date getDate(int index) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public Date[] getDates() throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public Date[] getDateRange() throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public void addDataFragment(ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException("" + this);
    }

    public boolean hasItems() {
        return false;
    }

    public Dataset addNewItem() {
        throw new UnsupportedOperationException("" + this);
    }

    public void addItem(Dataset item) {
        throw new UnsupportedOperationException("" + this);
    }

    public Dataset getItem() {
        return getItem(0);
    }

    public Dataset getItem(int index) {
        throw new UnsupportedOperationException("" + this);
    }

    static ByteOrder swap(ByteOrder from) {
        return from == ByteOrder.LITTLE_ENDIAN
            ? ByteOrder.BIG_ENDIAN
            : ByteOrder.LITTLE_ENDIAN;
    }

    static void swapWords(ByteBuffer bb) {
        if ((bb.limit() & 1) != 0)
            throw new IllegalArgumentException("illegal value length: " + bb);
        byte b;
        for (int i = 0, n = bb.limit(); i < n; i += 2) {
            b = bb.get(i);
            bb.put(i, bb.get(i+1));
            bb.put(i+1, b);
        }
        bb.order(swap(bb.order()));
    }

    static void swapInts(ByteBuffer bb) {
        if ((bb.limit() & 3) != 0)
            throw new IllegalArgumentException("illegal value length " + bb);

        byte b;
        for (int i = 0, n = bb.limit(); i < n; i += 4) {
            b = bb.get(i);
            bb.put(i, bb.get(i+3));
            bb.put(i+3, b);
            b = bb.get(i+1);
            bb.put(i+1, bb.get(i+2));
            bb.put(i+2, b);
        }
        bb.order(swap(bb.order()));
    }
    
    static void swapLongs(ByteBuffer bb) {
        if ((bb.limit() & 7) != 0)
            throw new IllegalArgumentException("illegal value length " + bb);

        byte b;
        for (int i = 0, n = bb.limit(); i < n; i += 8) {
            b = bb.get(i);
            bb.put(i, bb.get(i+7));
            bb.put(i+7, b);
            b = bb.get(i+1);
            bb.put(i+1, bb.get(i+6));
            bb.put(i+6, b);
            b = bb.get(i+2);
            bb.put(i+2, bb.get(i+5));
            bb.put(i+5, b);
            b = bb.get(i+3);
            bb.put(i+3, bb.get(i+4));
            bb.put(i+4, b);
        }
        bb.order(swap(bb.order()));
    }
}
