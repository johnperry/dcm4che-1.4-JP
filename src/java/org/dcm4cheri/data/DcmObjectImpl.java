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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.DcmObject;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.PersonName;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.VRMap;
import org.dcm4che.dict.VRs;
import org.dcm4che.util.DTFormat;
import org.dcm4cheri.util.StringUtils;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since March 2002
 * @version $Revision: 16575 $ $Date: 2012-02-02 10:12:28 +0100 (Do, 02 Feb 2012) $
 */
abstract class DcmObjectImpl implements DcmObject {

    static UIDDictionary DICT = DictionaryFactory.getInstance()
            .getDefaultUIDDictionary();

    protected final static Logger log = Logger.getLogger(DcmObjectImpl.class);

    protected ArrayList list = new ArrayList();

    public DcmHandler getDcmHandler() {
        return new DcmObjectHandlerImpl(this);
    }

    public DefaultHandler getSAXHandler() {
        return new SAXHandlerAdapter(getDcmHandler());
    }

    public DefaultHandler getSAXHandler2(File basedir) {
        return new SAXHandlerAdapter2(getDcmHandler(), basedir);
    }

    public String getPrivateCreatorID() {
        return null;
    }

    public void setPrivateCreatorID(String privateCreatorID) {
        throw new UnsupportedOperationException();
    }

    public SpecificCharacterSet getSpecificCharacterSet() {
        return null;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void clear() {
        list.clear();
    }

    public void shareElements() {
        synchronized (list) {
            final int size = list.size();
            for (int i = 0; i < size; ++i)
                list.set(i, ((DcmElement) list.get(i)).share());
        }
    }

    private int indexOf(int tag) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) >> 1;
            DcmElementImpl midVal = (DcmElementImpl) list.get(mid);
            long cmp = (midVal.tag & 0xffffffffL) - (tag & 0xffffffffL);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1); // key not found
    }

    public boolean contains(int tag) {
        if (Tags.isPrivate(tag)) {
            try {
                tag = adjustPrivateTag(tag, false);
            } catch (DcmValueException e) {
                log.warn("Could not access Creator ID", e);
                return false;
            }
            if (tag == 0) {
                return false;
            }
        }
        return indexOf(tag) >= 0;
    }

    public boolean containsValue(int tag) {
        DcmElement e = get(tag);
        return e != null && !e.isEmpty();
    }

    public int vm(int tag) {
        if (Tags.isPrivate(tag)) {
            try {
                tag = adjustPrivateTag(tag, false);
            } catch (DcmValueException e) {
                log.warn("Could not access Creator ID", e);
                return -1;
            }
            if (tag == 0) {
                return -1;
            }
        }
        int index = indexOf(tag);
        return index >= 0 ? ((DcmElement) list.get(index))
                .vm(getSpecificCharacterSet()) : -1;
    }

    private int tagOfCreatorID(String creatorID, int tag, boolean create)
            throws DcmValueException {
        int gr = tag & 0xffff0000;
        int tagOfCreatorID = gr | 0x10;
        int index = indexOf(tagOfCreatorID);
        if (index >= 0) {
            DcmElement elm = (DcmElement) list.get(index);
            final int size = list.size();
            while (++index < size) {
                if (creatorID.equals(elm.getString(getSpecificCharacterSet()))) {
                    return elm.tag();
                }
                tagOfCreatorID = elm.tag() + 1;
                elm = (DcmElement) list.get(index);
                if ((elm.tag() & 0xffffff00) != gr) {
                    break;
                }
            }
        }
        if (!create) {
            return 0;
        }
        doPut(StringElement.createLO(tagOfCreatorID, creatorID,
                getSpecificCharacterSet()));
        return tagOfCreatorID;
    }

    private int adjustPrivateTag(int tag, boolean create)
            throws DcmValueException {
        String creatorID = getPrivateCreatorID();
        // no adjustments, if creatorID not set
        if (creatorID == null) {
            return tag;
        }
        int tagOfCreatorID = tagOfCreatorID(creatorID, tag, create);
        return  (tagOfCreatorID == 0)
                ? 0
                : (tag & 0xffff00ff) | ((tagOfCreatorID & 0xff) << 8);
    }

    public DcmElement get(int tag) {
        if (Tags.isPrivate(tag)) {
            try {
                tag = adjustPrivateTag(tag, false);
            } catch (DcmValueException e) {
                log.warn("Could not access Creator ID", e);
                return null;
            }
            if (tag == 0) {
                return null;
            }
        }
        int index = indexOf(tag);
        return index >= 0 ? (DcmElement) list.get(index) : null;
    }

    public DcmElement remove(int tag) {
        if (Tags.isPrivate(tag)) {
            try {
                tag = adjustPrivateTag(tag, false);
            } catch (DcmValueException e) {
                log.warn("Could not access Creator ID", e);
                return null;
            }
            if (tag == 0) {
                return null;
            }
        }
        synchronized (list) {
            int index = indexOf(tag);
            return index >= 0 ? (DcmElement) list.remove(index) : null;
        }
    }

    public ByteBuffer getByteBuffer(int tag) {
        DcmElement e = get(tag);
        return e != null ? e.getByteBuffer() : null;
    }

    public String getString(int tag, String defVal) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? el
                    .getString(getSpecificCharacterSet()) : defVal;
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    public String getString(int tag) {
        return getString(tag, null);
    }

    public String getString(int tag, int index) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getString(index, getSpecificCharacterSet())
                    : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public String getString(int tag, int index, String defVal) {
        String s = getString(tag, index);
        return s != null ? s : defVal;
    }

    public String[] getStrings(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getStrings(getSpecificCharacterSet()) : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public String getBoundedString(int maxLen, int tag, String defVal) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? el.getBoundedString(maxLen,
                    getSpecificCharacterSet()) : defVal;
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    public String getBoundedString(int maxLen, int tag) {
        return getBoundedString(maxLen, tag, null);
    }

    public String getBoundedString(int maxLen, int tag, int index) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getBoundedString(index,
                    getSpecificCharacterSet()) : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public String getBoundedString(int maxLen, int tag, int index, String defVal) {
        String s = getBoundedString(tag, index);
        return s != null ? s : defVal;
    }

    public String[] getBoundedStrings(int maxLen, int tag) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getBoundedStrings(maxLen,
                    getSpecificCharacterSet()) : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Integer getInteger(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? new Integer(el.getInt())
                    : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Integer getInteger(int tag, int index) {
        DcmElement el = get(tag);
        try {
            return el != null && index < el.vm(null) ? new Integer(el
                    .getInt(index)) : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public PersonName getPersonName(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? el
                    .getPersonName(getSpecificCharacterSet()) : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public PersonName[] getPersonNames(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getPersonNames(getSpecificCharacterSet())
                    : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public PersonName getPersonName(int tag, int index) {
        PersonName[] pns = getPersonNames(tag);
        return pns != null && index < pns.length ? pns[index] : null;
    }

    public int getInt(int tag, int defVal) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? el.getInt() : defVal;
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    public int getInt(int tag, int index, int defVal) {
        DcmElement el = get(tag);
        try {
            return el != null && index < el.vm(null) ? el.getInt(index)
                    : defVal;
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    public int[] getInts(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getInts() : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public int getTag(int tag, int defVal) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? el.getTag() : defVal;
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    public int getTag(int tag, int index, int defVal) {
        DcmElement el = get(tag);
        try {
            return el != null && index < el.vm(null) ? el.getTag(index)
                    : defVal;
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    public int[] getTags(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getTags() : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Float getFloat(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? new Float(el.getFloat())
                    : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Float getFloat(int tag, int index) {
        DcmElement el = get(tag);
        try {
            return el != null && index < el.vm(null) ? new Float(el
                    .getFloat(index)) : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public float getFloat(int tag, float defVal) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? el.getFloat() : defVal;
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    public float getFloat(int tag, int index, float defVal) {
        DcmElement el = get(tag);
        try {
            return el != null && index < el.vm(null) ? el.getFloat(index)
                    : defVal;
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    public float[] getFloats(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getFloats() : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Double getDouble(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? new Double(el.getDouble())
                    : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Double getDouble(int tag, int index) {
        DcmElement el = get(tag);
        try {
            return el != null && index < el.vm(null) ? new Double(el
                    .getDouble(index)) : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public double getDouble(int tag, double defVal) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? el.getDouble() : defVal;
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    public double getDouble(int tag, int index, double defVal) {
        DcmElement el = get(tag);
        try {
            return el != null && index < el.vm(null) ? el.getDouble(index)
                    : defVal;
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    public double[] getDoubles(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getDoubles() : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Date getDate(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getDate() : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Date getDate(int tag, int index) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getDate(index) : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Date[] getDateRange(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null && !el.isEmpty() ? el.getDateRange() : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Date[] getDates(int tag) {
        DcmElement el = get(tag);
        try {
            return el != null ? el.getDates() : null;
        } catch (DcmValueException e) {
            return null;
        }
    }

    public Date getDateTime(int dateTag, int timeTag) {
        DcmElement date = get(dateTag);
        if (date == null || date.isEmpty()) {
            return null;
        }

        try {
            DcmElement time = get(timeTag);
            if (time == null || time.isEmpty()) {
                return date.getDate();
            }
            String dt = date.getString(null) + time.getString(null);
            return new DTFormat().parse(dt);
        } catch (Exception e) {
            return null;
        }
    }

    public Date[] getDateTimeRange(int dateTag, int timeTag) {
        String tm = getString(timeTag);
        if (tm == null || tm.equals("*") || tm.equals("-")) {
            return getDateRange(dateTag);
        }
        String da = getString(dateTag);
        if (da == null || da.equals("*") || da.equals("-")) {
            return null;
        }
        try {
            String[] dateRange = splitRange(da);
            String[] timeRange = splitRange(tm);
            Date[] result = new Date[2];
            DTFormat f = new DTFormat();
            if (dateRange[0] != null) {
                result[0] = f.parse(dateRange[0]
                        + (timeRange[0] == null ? "" : timeRange[0]));
            }
            if (dateRange[1] != null) {
                result[1] = f.parse(dateRange[1]
                        + (timeRange[1] == null ? "235959.999" : timeRange[1]));
            }
            return result;
        } catch (ParseException e) {
            return null;
        }
    }

    private static String[] splitRange(String range) {
        int hypen = range.indexOf('-');
        String[] result = new String[2];
        if (hypen != 0) {
            result[0] = hypen == -1 ? range : range.substring(0, hypen);
        }
        if (hypen != range.length() - 1) {
            result[1] = hypen == -1 ? range : range.substring(hypen + 1);
        }
        return result;
    }

    public Dataset getItem(int tag) {
        return getItem(tag, 0);
    }

    public Dataset getItem(int tag, int index) {
        DcmElement e = get(tag);
        return e != null && index < e.countItems() ? e.getItem(index) : null;
    }

    /**
     * Description of the Method
     * 
     * @param newElem
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    protected DcmElement put(DcmElement newElem) {
        if (log.isDebugEnabled()) {
            log.debug("put " + newElem);
        }
        if ((newElem.tag() & 0xffff) == 0) {
            return newElem;
        }

        if (Tags.isPrivate(newElem.tag())) {
            try {
                ((DcmElementImpl) newElem).tag = adjustPrivateTag(
                        newElem.tag(), true);
            } catch (DcmValueException e) {
                log.warn("Could not access creator ID - ignore " + newElem, e);
                return newElem;
            }
        }
        return doPut(newElem);
    }

    private DcmElement doPut(DcmElement newElem) {
        synchronized (list) {
            final int size = list.size();
            final int newTag = newElem.tag();
            if (size == 0
                    || (((DcmElementImpl) list.get(size - 1)).tag & 0xffffffffL) < (newTag & 0xffffffffL)) {
                list.add(newElem);
            } else {
                int index = indexOf(newTag);
                if (index >= 0) {
                    list.set(index, newElem);
                } else {
                    list.add(-(index + 1), newElem);
                }
            }
            return newElem;
        }
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAE(int tag) {
        return put(StringElement.createAE(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAE(int tag, String value) {
        return put(value != null ? StringElement.createAE(tag, value)
                : StringElement.createAE(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAE(int tag, String[] values) {
        return put(values != null ? StringElement.createAE(tag, values)
                : StringElement.createAE(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAS(int tag) {
        return put(StringElement.createAS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAS(int tag, String value) {
        return put(value != null && value.length() != 0 ? StringElement
                .createAS(tag, value) : StringElement.createAS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAS(int tag, String[] values) {
        return put(values != null ? StringElement.createAS(tag, values)
                : StringElement.createAS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAT(int tag) {
        return put(ValueElement.createAT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAT(int tag, int value) {
        return put(ValueElement.createAT(tag, value));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAT(int tag, int[] values) {
        return put(values != null ? ValueElement.createAT(tag, values)
                : StringElement.createAT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAT(int tag, String value) {
        return value != null && value.length() != 0 ? putAT(tag, Integer
                .parseInt(value, 16)) : putAT(tag);
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putAT(int tag, String[] values) {
        if (values == null) {
            return putAT(tag);
        }
        int[] a = new int[values.length];
        for (int i = 0; i < values.length; ++i) {
            a[i] = Integer.parseInt(values[i], 16);
        }
        return putAT(tag, a);
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putCS(int tag) {
        return put(StringElement.createCS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putCS(int tag, String value) {
        return put(value != null ? StringElement.createCS(tag, value)
                : StringElement.createCS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putCS(int tag, String[] values) {
        return put(values != null ? StringElement.createCS(tag, values)
                : StringElement.createCS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDA(int tag) {
        return put(StringElement.createDA(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDA(int tag, Date value) {
        return put(value != null ? StringElement.createDA(tag, value)
                : StringElement.createDA(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDA(int tag, Date[] values) {
        return put(values != null ? StringElement.createDA(tag, values)
                : StringElement.createDA(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param from
     *            Description of the Parameter
     * @param to
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDA(int tag, Date from, Date to) {
        return put(from != null || to != null ? StringElement.createDA(tag,
                from, to) : StringElement.createDA(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDA(int tag, String value) {
        return put(value != null && value.length() != 0 ? StringElement
                .createDA(tag, value) : StringElement.createDA(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDA(int tag, String[] values) {
        return put(values != null ? StringElement.createDA(tag, values)
                : StringElement.createDA(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDS(int tag) {
        return put(StringElement.createDS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDS(int tag, float value) {
        return put(StringElement.createDS(tag, value));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDS(int tag, float[] values) {
        return put(values != null ? StringElement.createDS(tag, values)
                : StringElement.createDS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDS(int tag, String value) {
        return put(value != null && value.length() != 0 ? StringElement
                .createDS(tag, value) : StringElement.createDS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDS(int tag, String[] values) {
        return put(values != null ? StringElement.createDS(tag, values)
                : StringElement.createDS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDT(int tag) {
        return put(StringElement.createDT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDT(int tag, Date value) {
        return put(value != null ? StringElement.createDT(tag, value)
                : StringElement.createDT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDT(int tag, Date[] values) {
        return put(values != null ? StringElement.createDT(tag, values)
                : StringElement.createDT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param from
     *            Description of the Parameter
     * @param to
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDT(int tag, Date from, Date to) {
        return put(from != null || to != null ? StringElement.createDT(tag,
                from, to) : StringElement.createDT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDT(int tag, String value) {
        return put(value != null && value.length() != 0 ? StringElement
                .createDT(tag, value) : StringElement.createDT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putDT(int tag, String[] values) {
        return put(values != null ? StringElement.createDT(tag, values)
                : StringElement.createDT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putFL(int tag) {
        return put(ValueElement.createFL(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putFL(int tag, float value) {
        return put(ValueElement.createFL(tag, value));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putFL(int tag, float[] values) {
        return put(values != null ? ValueElement.createFL(tag, values)
                : ValueElement.createFL(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putFL(int tag, String value) {
        return put(value != null && value.length() != 0 ? ValueElement
                .createFL(tag, Float.parseFloat(value)) : ValueElement
                .createFL(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putFL(int tag, String[] values) {
        return put(values != null ? ValueElement.createFL(tag, StringUtils
                .parseFloats(values)) : ValueElement.createFL(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putFD(int tag) {
        return put(ValueElement.createFD(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putFD(int tag, double value) {
        return put(ValueElement.createFD(tag, value));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putFD(int tag, double[] values) {
        return put(values != null ? ValueElement.createFD(tag, values)
                : ValueElement.createFD(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putFD(int tag, String value) {
        return put(value != null && value.length() != 0 ? ValueElement
                .createFD(tag, Double.parseDouble(value)) : ValueElement
                .createFD(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putFD(int tag, String[] values) {
        return put(values != null ? ValueElement.createFD(tag, StringUtils
                .parseDoubles(values)) : ValueElement.createFD(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putIS(int tag) {
        return put(StringElement.createIS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putIS(int tag, int value) {
        return put(StringElement.createIS(tag, value));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putIS(int tag, int[] values) {
        return put(values != null ? StringElement.createIS(tag, values)
                : StringElement.createIS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putIS(int tag, String value) {
        return put(value != null && value.length() != 0 ? StringElement
                .createIS(tag, value) : StringElement.createIS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putIS(int tag, String[] values) {
        return put(values != null ? StringElement.createIS(tag, values)
                : StringElement.createIS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putLO(int tag) {
        return put(StringElement.createLO(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putLO(int tag, String value) {
        return put(value != null ? StringElement.createLO(tag, value,
                getSpecificCharacterSet()) : StringElement.createLO(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putLO(int tag, String[] values) {
        return put(values != null ? StringElement.createLO(tag, values,
                getSpecificCharacterSet()) : StringElement.createLO(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putLT(int tag) {
        return put(StringElement.createLT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putLT(int tag, String value) {
        return put(value != null ? StringElement.createLT(tag, value,
                getSpecificCharacterSet()) : StringElement.createLT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putLT(int tag, String[] values) {
        return put(values != null ? StringElement.createLT(tag, values,
                getSpecificCharacterSet()) : StringElement.createLT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOB(int tag) {
        return put(ValueElement.createOB(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOB(int tag, byte[] value) {
        return put(value != null ? ValueElement.createOB(tag, value)
                : ValueElement.createOB(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOB(int tag, ByteBuffer value) {
        return put(value != null ? ValueElement.createOB(tag, value)
                : ValueElement.createOB(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOBsq(int tag) {
        return put(FragmentElement.createOB(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOF(int tag) {
        return put(ValueElement.createOF(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOF(int tag, float[] value) {
        return put(value != null ? ValueElement.createOF(tag, value)
                : ValueElement.createOF(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOF(int tag, ByteBuffer value) {
        return put(value != null ? ValueElement.createOF(tag, value)
                : ValueElement.createOF(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOFsq(int tag) {
        return put(FragmentElement.createOF(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOW(int tag) {
        return put(ValueElement.createOW(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOW(int tag, short[] value) {
        return put(value != null ? ValueElement.createOW(tag, value)
                : ValueElement.createOW(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOW(int tag, ByteBuffer value) {
        return put(value != null ? ValueElement.createOW(tag, value)
                : ValueElement.createOW(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putOWsq(int tag) {
        return put(FragmentElement.createOW(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putPN(int tag) {
        return put(StringElement.createPN(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putPN(int tag, PersonName value) {
        return put(value != null ? StringElement.createPN(tag, value,
                getSpecificCharacterSet()) : StringElement.createPN(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putPN(int tag, PersonName[] values) {
        return put(values != null ? StringElement.createPN(tag, values,
                getSpecificCharacterSet()) : StringElement.createPN(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putPN(int tag, String value) {
        return put(value != null && value.length() != 0 ? StringElement
                .createPN(tag, new PersonNameImpl(value, false),
                        getSpecificCharacterSet()) : StringElement
                .createPN(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putPN(int tag, String[] values) {
        if (values == null) {
            return StringElement.createPN(tag);
        }
        PersonName[] a = new PersonName[values.length];
        for (int i = 0; i < a.length; ++i) {
            a[i] = new PersonNameImpl(values[i], false);
        }
        return put(StringElement.createPN(tag, a, getSpecificCharacterSet()));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSH(int tag) {
        return put(StringElement.createSH(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSH(int tag, String value) {
        return put(value != null ? StringElement.createSH(tag, value,
                getSpecificCharacterSet()) : StringElement.createSH(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSH(int tag, String[] values) {
        return put(values != null ? StringElement.createSH(tag, values,
                getSpecificCharacterSet()) : StringElement.createSH(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSL(int tag) {
        return put(ValueElement.createSL(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSL(int tag, int value) {
        return put(ValueElement.createSL(tag, value));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSL(int tag, int[] values) {
        return put(values != null ? ValueElement.createSL(tag, values)
                : ValueElement.createSL(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSL(int tag, String value) {
        return put(value != null && value.length() != 0 ? ValueElement
                .createSL(tag, StringUtils.parseInt(value, Integer.MIN_VALUE,
                        Integer.MAX_VALUE)) : ValueElement.createSL(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSL(int tag, String[] values) {
        return put(ValueElement.createSL(tag, StringUtils.parseInts(values,
                Integer.MIN_VALUE, Integer.MAX_VALUE)));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSQ(int tag) {
        throw new UnsupportedOperationException();
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSS(int tag) {
        return put(ValueElement.createSS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSS(int tag, int value) {
        return put(ValueElement.createSS(tag, value));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSS(int tag, int[] values) {
        return put(values != null ? ValueElement.createSS(tag, values)
                : ValueElement.createSS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSS(int tag, String value) {
        return put(value != null && value.length() != 0 ? ValueElement
                .createSS(tag, StringUtils.parseInt(value, Short.MIN_VALUE,
                        Short.MAX_VALUE)) : ValueElement.createSS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putSS(int tag, String[] values) {
        return put(values != null ? ValueElement.createSS(tag, StringUtils
                .parseInts(values, Short.MIN_VALUE, Short.MAX_VALUE))
                : ValueElement.createSS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putST(int tag) {
        return put(StringElement.createST(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putST(int tag, String value) {
        return put(value != null ? StringElement.createST(tag, value,
                getSpecificCharacterSet()) : StringElement.createST(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putST(int tag, String[] values) {
        return put(values != null ? StringElement.createST(tag, values,
                getSpecificCharacterSet()) : StringElement.createST(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putTM(int tag) {
        return put(StringElement.createTM(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putTM(int tag, Date value) {
        return put(value != null ? StringElement.createTM(tag, value)
                : StringElement.createTM(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putTM(int tag, Date[] values) {
        return put(values != null ? StringElement.createTM(tag, values)
                : StringElement.createTM(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param from
     *            Description of the Parameter
     * @param to
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putTM(int tag, Date from, Date to) {
        return put(from != null || to != null ? StringElement.createTM(tag,
                from, to) : StringElement.createTM(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putTM(int tag, String value) {
        return put(value != null && value.length() != 0 ? StringElement
                .createTM(tag, value) : StringElement.createTM(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putTM(int tag, String[] values) {
        return put(values != null ? StringElement.createTM(tag, values)
                : StringElement.createTM(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUC(int tag) {
        return put(StringElement.createUC(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUC(int tag, String value) {
        return put(value != null ? StringElement.createUC(tag, value,
                getSpecificCharacterSet()) : StringElement.createUC(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUC(int tag, String[] values) {
        return put(values != null ? StringElement.createUC(tag, values,
                getSpecificCharacterSet()) : StringElement.createUC(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUI(int tag) {
        return put(StringElement.createUI(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUI(int tag, String value) {
        return put(value != null ? StringElement.createUI(tag, value)
                : StringElement.createUI(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUI(int tag, String[] values) {
        return put(values != null ? StringElement.createUI(tag, values)
                : StringElement.createUI(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUL(int tag) {
        return put(ValueElement.createUL(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUL(int tag, int value) {
        return put(ValueElement.createUL(tag, value));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUL(int tag, int[] values) {
        return put(values != null ? ValueElement.createUL(tag, values)
                : StringElement.createUI(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUL(int tag, String value) {
        return put(value != null && value.length() != 0 ? ValueElement
                .createUL(tag, StringUtils.parseInt(value, 0L, 0xFFFFFFFFL))
                : ValueElement.createUL(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUL(int tag, String[] values) {
        return put(values != null ? ValueElement.createUL(tag, StringUtils
                .parseInts(values, 0L, 0xFFFFFFFFL)) : ValueElement
                .createUL(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUN(int tag) {
        return put(ValueElement.createUN(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUN(int tag, byte[] value) {
        return put(value != null ? ValueElement.createUN(tag, value)
                : ValueElement.createUN(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUN(int tag, ByteBuffer value) {
        return put(value != null ? ValueElement.createUN(tag, value)
                : ValueElement.createUN(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUNsq(int tag) {
        return put(FragmentElement.createUN(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUS(int tag) {
        return put(ValueElement.createUS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUS(int tag, int value) {
        return put(ValueElement.createUS(tag, value));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUS(int tag, int[] values) {
        return put(values != null ? ValueElement.createUS(tag, values)
                : ValueElement.createUS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUS(int tag, String value) {
        return put(value != null && value.length() != 0 ? ValueElement
                .createUS(tag, StringUtils.parseInt(value, 0L, 0xFFFFL))
                : ValueElement.createUS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUS(int tag, String[] values) {
        return put(values != null ? ValueElement.createUS(tag, StringUtils
                .parseInts(values, 0L, 0xFFFFL)) : ValueElement.createUS(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUT(int tag) {
        return put(StringElement.createUT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUT(int tag, String value) {
        return put(value != null ? StringElement.createUT(tag, value,
                getSpecificCharacterSet()) : StringElement.createUT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putUT(int tag, String[] values) {
        return put(values != null ? StringElement.createUT(tag, values,
                getSpecificCharacterSet()) : StringElement.createUT(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putXX(int tag) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param bytes
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putXX(int tag, ByteBuffer bytes) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag), bytes);
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putXX(int tag, String value) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag), value);
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putXX(int tag, String[] values) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag), values);
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putXXsq(int tag) {
        return putXXsq(tag, VRMap.DEFAULT.lookup(tag));
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param vr
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putXXsq(int tag, int vr) {
        switch (vr) {
        case VRs.OB:
            return putOBsq(tag);
        case VRs.OF:
            return putOFsq(tag);
        case VRs.OW:
            return putOWsq(tag);
        case VRs.UN:
            return putUNsq(tag);
        default:
            throw new IllegalArgumentException(Tags.toString(tag) + " "
                    + VRs.toString(vr));
        }
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param vr
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putXX(int tag, int vr) {
        switch (vr) {
        case VRs.AE:
            return putAE(tag);
        case VRs.AS:
            return putAS(tag);
        case VRs.AT:
            return putAT(tag);
        case VRs.CS:
            return putCS(tag);
        case VRs.DA:
            return putDA(tag);
        case VRs.DS:
            return putDS(tag);
        case VRs.DT:
            return putDT(tag);
        case VRs.FL:
            return putFL(tag);
        case VRs.FD:
            return putFD(tag);
        case VRs.IS:
            return putIS(tag);
        case VRs.LO:
            return putLO(tag);
        case VRs.LT:
            return putLT(tag);
        case VRs.OB:
            return putOB(tag);
        case VRs.OF:
            return putOF(tag);
        case VRs.OW:
            return putOW(tag);
        case VRs.PN:
            return putPN(tag);
        case VRs.SH:
            return putSH(tag);
        case VRs.SL:
            return putSL(tag);
        case VRs.SQ:
            return ((Dataset) this).putSQ(tag);
        case VRs.SS:
            return putSS(tag);
        case VRs.ST:
            return putST(tag);
        case VRs.TM:
            return putTM(tag);
        case VRs.UC:
            return putUC(tag);
        case VRs.UI:
            return putUI(tag);
        case VRs.UN:
            return putUN(tag);
        case VRs.UL:
            return putUL(tag);
        case VRs.US:
            return putUS(tag);
        case VRs.UT:
            return putUT(tag);
        default:
            return putXX(tag, checkIllegalVR(tag, vr));
        }
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param vr
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putXX(int tag, int vr, ByteBuffer value) {
        if (value == null) {
            return putXX(tag, vr);
        }
        switch (vr) {
        case VRs.AE:
            return put(StringElement.createAE(tag, value));
        case VRs.AS:
            return put(StringElement.createAS(tag, value));
        case VRs.AT:
            return put(ValueElement.createAT(tag, value));
        case VRs.CS:
            return put(StringElement.createCS(tag, value));
        case VRs.DA:
            return put(StringElement.createDA(tag, value));
        case VRs.DS:
            return put(StringElement.createDS(tag, value));
        case VRs.DT:
            return put(StringElement.createDT(tag, value));
        case VRs.FL:
            return put(ValueElement.createFL(tag, value));
        case VRs.FD:
            return put(ValueElement.createFD(tag, value));
        case VRs.IS:
            return put(StringElement.createIS(tag, value));
        case VRs.LO:
            return put(StringElement.createLO(tag, value));
        case VRs.LT:
            return put(StringElement.createLT(tag, value));
        case VRs.OB:
            return put(ValueElement.createOB(tag, value));
        case VRs.OF:
            return put(ValueElement.createOF(tag, value));
        case VRs.OW:
            return put(ValueElement.createOW(tag, value));
        case VRs.PN:
            return put(StringElement.createPN(tag, value));
        case VRs.SH:
            return put(StringElement.createSH(tag, value));
        case VRs.SL:
            return put(ValueElement.createSL(tag, value));
        case VRs.SS:
            return put(ValueElement.createSS(tag, value));
        case VRs.ST:
            return put(StringElement.createST(tag, value));
        case VRs.TM:
            return put(StringElement.createTM(tag, value));
        case VRs.UC:
            return put(StringElement.createUC(tag, value));
        case VRs.UI:
            return put(StringElement.createUI(tag, value));
        case VRs.UN:
            return put(ValueElement.createUN(tag, value));
        case VRs.UL:
            return put(ValueElement.createUL(tag, value));
        case VRs.US:
            return put(ValueElement.createUS(tag, value));
        case VRs.UT:
            return put(StringElement.createUT(tag, value));
        default:
            return putXX(tag, checkIllegalVR(tag, vr), value);
        }
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param vr
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putXX(int tag, int vr, String value) {
        if (value == null) {
            return putXX(tag, vr);
        }
        switch (vr) {
        case VRs.AE:
            return putAE(tag, value);
        case VRs.AS:
            return putAS(tag, value);
        case VRs.AT:
            return putAT(tag, value);
        case VRs.CS:
            return putCS(tag, value);
        case VRs.DA:
            return putDA(tag, value);
        case VRs.DS:
            return putDS(tag, value);
        case VRs.DT:
            return putDT(tag, value);
        case VRs.FL:
            return putFL(tag, value);
        case VRs.FD:
            return putFD(tag, value);
        case VRs.IS:
            return putIS(tag, value);
        case VRs.LO:
            return putLO(tag, value);
        case VRs.LT:
            return putLT(tag, value);
        case VRs.OB:
        case VRs.OF:
        case VRs.OW:
            throw new IllegalArgumentException(Tags.toString(tag) + " "
                    + VRs.toString(vr));
        case VRs.PN:
            return putPN(tag, value);
        case VRs.SH:
            return putSH(tag, value);
        case VRs.SL:
            return putSL(tag, value);
        case VRs.SS:
            return putSS(tag, value);
        case VRs.ST:
            return putST(tag, value);
        case VRs.TM:
            return putTM(tag, value);
        case VRs.UC:
            return putUC(tag, value);
        case VRs.UI:
            return putUI(tag, value);
        case VRs.UN:
            throw new IllegalArgumentException(Tags.toString(tag) + " "
                    + VRs.toString(vr));
        case VRs.UL:
            return putUL(tag, value);
        case VRs.US:
            return putUS(tag, value);
        case VRs.UT:
            return putUT(tag, value);
        default:
            return putXX(tag, checkIllegalVR(tag, vr), value);
        }
    }

    /**
     * Description of the Method
     * 
     * @param tag
     *            Description of the Parameter
     * @param vr
     *            Description of the Parameter
     * @param values
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public DcmElement putXX(int tag, int vr, String[] values) {
        if (values == null) {
            return putXX(tag, vr);
        }
        switch (vr) {
        case VRs.AE:
            return putAE(tag, values);
        case VRs.AS:
            return putAS(tag, values);
        case VRs.AT:
            return putAT(tag, values);
        case VRs.CS:
            return putCS(tag, values);
        case VRs.DA:
            return putDA(tag, values);
        case VRs.DS:
            return putDS(tag, values);
        case VRs.DT:
            return putDT(tag, values);
        case VRs.FL:
            return putFL(tag, values);
        case VRs.FD:
            return putFD(tag, values);
        case VRs.IS:
            return putIS(tag, values);
        case VRs.LO:
            return putLO(tag, values);
        case VRs.LT:
            return putLT(tag, values);
        case VRs.OB:
        case VRs.OF:
        case VRs.OW:
            throw new IllegalArgumentException(Tags.toString(tag) + " "
                    + VRs.toString(vr));
        case VRs.PN:
            return putPN(tag, values);
        case VRs.SH:
            return putSH(tag, values);
        case VRs.SL:
            return putSL(tag, values);
        case VRs.SS:
            return putSS(tag, values);
        case VRs.ST:
            return putST(tag, values);
        case VRs.TM:
            return putTM(tag, values);
        case VRs.UC:
            return putUC(tag, values);
        case VRs.UI:
            return putUI(tag, values);
        case VRs.UN:
            throw new IllegalArgumentException(Tags.toString(tag) + " "
                    + VRs.toString(vr));
        case VRs.UL:
            return putUL(tag, values);
        case VRs.US:
            return putUS(tag, values);
        case VRs.UT:
            return putUT(tag, values);
        default:
            return putXX(tag, checkIllegalVR(tag, vr), values);
        }
    }

    /**
     * Description of the Method
     * 
     * @return Description of the Return Value
     */
    public Iterator iterator() {
        return list.iterator();
    }

    public int hashCode() {
        int h = 0;
        for (Iterator iter = iterator(); iter.hasNext();) {
            h = 31 * h + iter.next().hashCode();
        }
        return h;
    }

    public boolean equals(Object o) {
        if (!(o instanceof DcmObject)) {
            return false;
        }
        DcmObject dcmobj2 = (DcmObject) o;
        Iterator it1 = iterator();
        Iterator it2 = dcmobj2.iterator();
        while (it1.hasNext()) {
            if (!it2.hasNext()) {
                if (log.isDebugEnabled()) {
                    log.debug("Size of " + dcmobj2
                            + " differs from size of this " + this);
                }
                return false;
            }
            Object el1 = it1.next();
            Object el2 = it2.next();
            if (!el1.equals(el2)) {
                if (log.isDebugEnabled()) {
                    log.debug("" + el2 + " of " + dcmobj2 + " differs from "
                            + el1 + " of this " + this);
                }
                return false;
            }
        }
        return !it2.hasNext();
    }

    public void putAll(DcmObject dcmObj) {
        putAll(dcmObj, REPLACE_ITEMS);
    }

    public void putAll(DcmObject dcmObj, int itemTreatment) {
        SpecificCharacterSet dstCharSet = getSpecificCharacterSet();
        SpecificCharacterSet srcCharSet = dcmObj.getSpecificCharacterSet();
        boolean skipSpecificCharacterSet = (this instanceof Dataset) &&
                (!isEmpty() || ((Dataset) this).getParent() != null);
        boolean transcodeStringValues = skipSpecificCharacterSet
                && (dstCharSet != null 
                        ? !dstCharSet.contains(srcCharSet)
                        : srcCharSet != null && !srcCharSet.isAscii());
        int tagOfCreatorID = 0;
        dcmObj.setPrivateCreatorID(null);
        setPrivateCreatorID(null);
        for (Iterator it = dcmObj.iterator(); it.hasNext();) {
            DcmElement el = (DcmElement) it.next();
            int tag = el.tag();
            if (skipSpecificCharacterSet
                    && tag == Tags.SpecificCharacterSet) {
                continue;
            }
            if (Tags.isPrivateCreatorDataElement(tag)) {
                try {
                    // prevent duplicate and overwrite of existing private creator id
                    if (contains(tag) || (!el.isEmpty() && tagOfCreatorID(
                            el.getString(srcCharSet), tag, false) != 0))
                        continue;
                } catch (DcmValueException e) {
                    // copy illegal Private Creator Data Element verbatim
                }
                setPrivateCreatorID(null);
            } else if (Tags.isPrivate(tag)) {
                int tagOfCreatorID2 = (tag & 0xffff0000) | ((tag & 0xff00) >> 8);
                if (tagOfCreatorID != tagOfCreatorID2) {
                    setPrivateCreatorID(dcmObj.getString(tagOfCreatorID2));
                    tagOfCreatorID = tagOfCreatorID2;
                }
            } else {
                setPrivateCreatorID(null);
            }
            if (el.isEmpty()) {
                putXX(tag, el.vr());
            } else {
                DcmElement sq;
                Dataset item;
                ByteBuffer value;
                switch (el.vr()) {
                case VRs.SQ:
                    sq = itemTreatment != REPLACE_ITEMS ? get(tag) : null;
                    if (sq == null || sq.vr() != VRs.SQ)
                        sq = putSQ(tag);
                    for (int i = 0, n = el.countItems(); i < n; ++i) {
                        item = itemTreatment == MERGE_ITEMS ? sq.getItem(i)
                                : null;
                        if (item == null)
                            item = sq.addNewItem();
                        item.putAll(el.getItem(i), itemTreatment);
                    }
                    break;
                case VRs.OB:
                case VRs.OF:
                case VRs.OW:
                case VRs.UN:
                    if (el.hasDataFragments()) {
                        sq = putXXsq(tag, el.vr());
                        for (int i = 0, n = el.countItems(); i < n; ++i) {
                            sq.addDataFragment(el.getDataFragment(i));
                        }
                        break;
                    }
                default:
                    value = el.getByteBuffer();
                    if (transcodeStringValues) {
                        switch (el.vr()) {
                        case VRs.LO:
                        case VRs.LT:
                        case VRs.PN:
                        case VRs.SH:
                        case VRs.ST:
                        case VRs.UT:
                            value = transcodeString(value, srcCharSet, dstCharSet);
                        }
                    }
                    putXX(tag, el.vr(), value);
                    break;
                }
            }
        }
        setPrivateCreatorID(null);
    }

    private ByteBuffer transcodeString(ByteBuffer value,
            SpecificCharacterSet srcCharSet, SpecificCharacterSet dstCharSet) {
        byte[] b = value.array();
        String s = srcCharSet == null ? new String(b) : srcCharSet.decode(b);
        return ByteBuffer.wrap(dstCharSet == null ? s.getBytes()
                : dstCharSet.encode(s));
    }

    /**
     * Description of the Method
     * 
     * @param grTag
     *            Description of the Parameter
     * @param grLen
     *            Description of the Parameter
     * @param handler
     *            Description of the Parameter
     * @exception IOException
     *                Description of the Exception
     */
    protected void write(int grTag, int grLen, DcmHandler handler)
            throws IOException {
        byte[] b4 = { (byte) grLen, (byte) (grLen >>> 8),
                (byte) (grLen >>> 16), (byte) (grLen >>> 24) };
        long el1Pos = ((DcmElement) list.get(0)).getStreamPosition();
        handler.startElement(grTag, VRs.UL, el1Pos == -1L ? -1L : el1Pos - 12);
        handler.value(b4, 0, 4);
        handler.endElement();
        for (int i = 0, n = list.size(); i < n; ++i) {
            DcmElement el = (DcmElement) list.get(i);
            int len = el.length();
            handler.startElement(el.tag(), el.vr(), el.getStreamPosition());
            ByteBuffer bb = el.getByteBuffer(ByteOrder.LITTLE_ENDIAN);
            handler.value(bb.array(), bb.arrayOffset(), bb.limit());
            handler.endElement();
        }
    }

    /**
     * Description of the Method
     * 
     * @param out
     *            Description of the Parameter
     * @param encParam
     *            Description of the Parameter
     * @param tag
     *            Description of the Parameter
     * @param vr
     *            Description of the Parameter
     * @param len
     *            Description of the Parameter
     * @exception IOException
     *                Description of the Exception
     */
    public void writeHeader(ImageOutputStream out, DcmEncodeParam encParam,
            int tag, int vr, int len) throws IOException {
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(tag >> 16);
            out.write(tag >> 24);
            out.write(tag >> 0);
            out.write(tag >> 8);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(tag >> 24);
            out.write(tag >> 16);
            out.write(tag >> 8);
            out.write(tag >> 0);
        }
        if (vr != VRs.NONE && encParam.explicitVR) {
            out.write(vr >> 8);
            out.write(vr >> 0);
            if (VRs.isLengthField16Bit(vr)) {
                if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    out.write(len >> 0);
                    out.write(len >> 8);
                } else {
                    out.write(len >> 8);
                    out.write(len >> 0);
                }
                return;
            } else {
                out.write(0);
                out.write(0);
            }
        }
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(len >> 0);
            out.write(len >> 8);
            out.write(len >> 16);
            out.write(len >> 24);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(len >> 24);
            out.write(len >> 16);
            out.write(len >> 8);
            out.write(len >> 0);
        }
    }

    /**
     * Description of the Method
     * 
     * @param out
     *            Description of the Parameter
     * @param encParam
     *            Description of the Parameter
     * @param tag
     *            Description of the Parameter
     * @param vr
     *            Description of the Parameter
     * @param len
     *            Description of the Parameter
     * @exception IOException
     *                Description of the Exception
     */
    public void writeHeader(OutputStream out, DcmEncodeParam encParam, int tag,
            int vr, int len) throws IOException {
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(tag >> 16);
            out.write(tag >> 24);
            out.write(tag >> 0);
            out.write(tag >> 8);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(tag >> 24);
            out.write(tag >> 16);
            out.write(tag >> 8);
            out.write(tag >> 0);
        }
        if (vr != VRs.NONE && encParam.explicitVR) {
            out.write(vr >> 8);
            out.write(vr >> 0);
            if (VRs.isLengthField16Bit(vr)) {
                if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    out.write(len >> 0);
                    out.write(len >> 8);
                } else {
                    out.write(len >> 8);
                    out.write(len >> 0);
                }
                return;
            } else {
                out.write(0);
                out.write(0);
            }
        }
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(len >> 0);
            out.write(len >> 8);
            out.write(len >> 16);
            out.write(len >> 24);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(len >> 24);
            out.write(len >> 16);
            out.write(len >> 8);
            out.write(len >> 0);
        }
    }

    /**
     * Log warning about illegal VR code and check if default VR of given tag is different.
     * Throws an IllegalArgumentException if given VR is already the default VR!
     * 
     * @param tag
     * @param vr
     * @return Default VR code of given tag
     */
    private int checkIllegalVR(int tag, int vr) {
        log.warn(Tags.toString(tag) + " with illegal VR Code: "
                + Integer.toHexString(vr) + "H");
        int defaultVR = VRMap.DEFAULT.lookup(tag);
        if (vr == defaultVR)
            throw new IllegalArgumentException("Illegal VR code "+ Integer.toHexString(vr) + "H! Tag:"+Tags.toString(tag));
        return defaultVR;
    }
    
}
