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

package org.dcm4che.data;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.stream.ImageOutputStream;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Defines common behavior of <code>Command</code>, <code>Dataset</code>, and
 * <code>FileMetaInfo</code> container objects.
 * 
 * @see "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 8442 $ $Date: 2008-11-28 12:31:02 +0100 (Fr, 28 Nov 2008) $
 * 
 *          <p>
 *          <b>Revisions:</b>
 * 
 *          <p>
 *          <b>20020722 gunter:</b>
 *          <ul>
 *          <li>add Private Data Elements access functions:<br>
 *          consider new Private Creator ID property
 *          </ul>
 */
public interface DcmObject {

    /**
     * Constant for itemTreatment param of @link {#putAll(dcmObj, int
     * itemTreatment)}
     */
    int REPLACE_ITEMS = 0;

    /**
     * Constant for itemTreatment param of @link {#putAll(dcmObj, int
     * itemTreatment)}
     */

    int ADD_ITEMS = 1;

    /**
     * Constant for itemTreatment param of @link {#putAll(dcmObj, int
     * itemTreatment)}
     */
    int MERGE_ITEMS = 2;

    void setPrivateCreatorID(String privateCreatorID);

    String getPrivateCreatorID();

    SpecificCharacterSet getSpecificCharacterSet();

    boolean isEmpty();

    int size();

    int length();

    void clear();

    DcmHandler getDcmHandler();

    DefaultHandler getSAXHandler();

    DefaultHandler getSAXHandler2(File basedir);

    Iterator iterator();

    boolean contains(int tag);

    boolean containsValue(int tag);

    int vm(int tag);

    DcmElement get(int tag);

    DcmElement remove(int tag);

    ByteBuffer getByteBuffer(int tag);

    String getString(int tag);

    String getString(int tag, String defVal);

    String getString(int tag, int index);

    String getString(int tag, int index, String defVal);

    String[] getStrings(int tag);

    String getBoundedString(int maxLen, int tag);

    String getBoundedString(int maxLen, int tag, String defVal);

    String getBoundedString(int maxLen, int tag, int index);

    String getBoundedString(int maxLen, int tag, int index, String defVal);

    String[] getBoundedStrings(int maxLen, int tag);

    PersonName getPersonName(int tag);

    PersonName[] getPersonNames(int tag);

    PersonName getPersonName(int tag, int index);

    Integer getInteger(int tag);

    Integer getInteger(int tag, int index);

    int getInt(int tag, int defVal);

    int getInt(int tag, int index, int defVal);

    int[] getInts(int tag);

    int getTag(int tag, int defVal);

    int getTag(int tag, int index, int defVal);

    int[] getTags(int tag);

    Float getFloat(int tag);

    Float getFloat(int tag, int index);

    float getFloat(int tag, float defVal);

    float getFloat(int tag, int index, float defVal);

    float[] getFloats(int tag);

    Double getDouble(int tag);

    Double getDouble(int tag, int index);

    double getDouble(int tag, double defVal);

    double getDouble(int tag, int index, double defVal);

    double[] getDoubles(int tag);

    Date getDate(int tag);

    Date getDate(int tag, int index);

    Date[] getDates(int tag);

    Date getDateTime(int dateTag, int timeTag);

    Date[] getDateRange(int tag);

    Date[] getDateTimeRange(int dateTag, int timeTag);

    Dataset getItem(int tag);

    Dataset getItem(int tag, int index);

    DcmElement putAE(int tag);

    DcmElement putAE(int tag, String value);

    DcmElement putAE(int tag, String[] values);

    DcmElement putAS(int tag);

    DcmElement putAS(int tag, String value);

    DcmElement putAS(int tag, String[] values);

    DcmElement putAT(int tag);

    DcmElement putAT(int tag, int value);

    DcmElement putAT(int tag, int[] values);

    DcmElement putAT(int tag, String value);

    DcmElement putAT(int tag, String[] values);

    DcmElement putCS(int tag);

    DcmElement putCS(int tag, String value);

    DcmElement putCS(int tag, String[] values);

    DcmElement putDA(int tag);

    DcmElement putDA(int tag, Date value);

    DcmElement putDA(int tag, Date[] values);

    DcmElement putDA(int tag, Date from, Date to);

    DcmElement putDA(int tag, String value);

    DcmElement putDA(int tag, String[] values);

    DcmElement putDS(int tag);

    DcmElement putDS(int tag, float value);

    DcmElement putDS(int tag, float[] values);

    DcmElement putDS(int tag, String value);

    DcmElement putDS(int tag, String[] values);

    DcmElement putDT(int tag);

    DcmElement putDT(int tag, Date value);

    DcmElement putDT(int tag, Date[] values);

    DcmElement putDT(int tag, Date from, Date to);

    DcmElement putDT(int tag, String value);

    DcmElement putDT(int tag, String[] values);

    DcmElement putFL(int tag);

    DcmElement putFL(int tag, float value);

    DcmElement putFL(int tag, float[] values);

    DcmElement putFL(int tag, String value);

    DcmElement putFL(int tag, String[] values);

    DcmElement putFD(int tag);

    DcmElement putFD(int tag, double value);

    DcmElement putFD(int tag, double[] values);

    DcmElement putFD(int tag, String value);

    DcmElement putFD(int tag, String[] values);

    DcmElement putIS(int tag);

    DcmElement putIS(int tag, int value);

    DcmElement putIS(int tag, int[] values);

    DcmElement putIS(int tag, String value);

    DcmElement putIS(int tag, String[] values);

    DcmElement putLO(int tag);

    DcmElement putLO(int tag, String value);

    DcmElement putLO(int tag, String[] values);

    DcmElement putLT(int tag);

    DcmElement putLT(int tag, String value);

    DcmElement putLT(int tag, String[] values);

    DcmElement putOB(int tag);

    DcmElement putOB(int tag, byte[] value);

    DcmElement putOB(int tag, ByteBuffer value);

    DcmElement putOBsq(int tag);

    DcmElement putOF(int tag);

    DcmElement putOF(int tag, float[] value);

    DcmElement putOF(int tag, ByteBuffer value);

    DcmElement putOFsq(int tag);

    DcmElement putOW(int tag);

    DcmElement putOW(int tag, short[] value);

    DcmElement putOW(int tag, ByteBuffer value);

    DcmElement putOWsq(int tag);

    DcmElement putPN(int tag);

    DcmElement putPN(int tag, PersonName value);

    DcmElement putPN(int tag, PersonName[] values);

    DcmElement putPN(int tag, String value);

    DcmElement putPN(int tag, String[] values);

    DcmElement putSH(int tag);

    DcmElement putSH(int tag, String value);

    DcmElement putSH(int tag, String[] values);

    DcmElement putSL(int tag);

    DcmElement putSL(int tag, int value);

    DcmElement putSL(int tag, int[] values);

    DcmElement putSL(int tag, String value);

    DcmElement putSL(int tag, String[] values);

    DcmElement putSQ(int tag);

    DcmElement putSS(int tag);

    DcmElement putSS(int tag, int value);

    DcmElement putSS(int tag, int[] values);

    DcmElement putSS(int tag, String value);

    DcmElement putSS(int tag, String[] values);

    DcmElement putST(int tag);

    DcmElement putST(int tag, String value);

    DcmElement putST(int tag, String[] values);

    DcmElement putTM(int tag);

    DcmElement putTM(int tag, Date value);

    DcmElement putTM(int tag, Date[] values);

    DcmElement putTM(int tag, Date from, Date to);

    DcmElement putTM(int tag, String value);

    DcmElement putTM(int tag, String[] values);

    DcmElement putUC(int tag);

    DcmElement putUC(int tag, String value);

    DcmElement putUC(int tag, String[] values);

    DcmElement putUI(int tag);

    DcmElement putUI(int tag, String value);

    DcmElement putUI(int tag, String[] values);

    DcmElement putUL(int tag);

    DcmElement putUL(int tag, int value);

    DcmElement putUL(int tag, int[] values);

    DcmElement putUL(int tag, String value);

    DcmElement putUL(int tag, String[] values);

    DcmElement putUN(int tag);

    DcmElement putUN(int tag, byte[] value);

    DcmElement putUNsq(int tag);

    DcmElement putUS(int tag);

    DcmElement putUS(int tag, int value);

    DcmElement putUS(int tag, int[] values);

    DcmElement putUS(int tag, String value);

    DcmElement putUS(int tag, String[] values);

    DcmElement putUT(int tag);

    DcmElement putUT(int tag, String value);

    DcmElement putUT(int tag, String[] values);

    DcmElement putXX(int tag, int vr);

    DcmElement putXX(int tag, int vr, ByteBuffer value);

    DcmElement putXX(int tag, int vr, String value);

    DcmElement putXX(int tag, int vr, String[] values);

    DcmElement putXXsq(int tag, int vr);

    DcmElement putXX(int tag);

    DcmElement putXX(int tag, ByteBuffer value);

    DcmElement putXX(int tag, String value);

    DcmElement putXX(int tag, String[] values);

    DcmElement putXXsq(int tag);

    void putAll(DcmObject dcmObj);

    void putAll(DcmObject dcmObj, int itemTreatment);

    void writeHeader(OutputStream out, DcmEncodeParam encParam, int tag,
            int vr, int len) throws IOException;

    void writeHeader(ImageOutputStream iout, DcmEncodeParam encParam, int tag,
            int vr, int len) throws IOException;
}
