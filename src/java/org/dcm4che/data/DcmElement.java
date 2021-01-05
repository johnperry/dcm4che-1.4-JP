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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

/** Element in <code>DcmObject</code>.
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since March 2002
 * @version $Revision: 3994 $ $Date: 2006-05-18 00:10:23 +0200 (Do, 18 Mai 2006) $
 * @see "DICOM Part 5: Data Structures and Encoding, 7.1 Data Elements"
 * @see "DICOM Part 7: Message Exchange, 6.3.1 Command Set Structure"
 */
public interface DcmElement {
   
   int tag();
   
   int vr();
   
   /**
    * @deprecated may return wrong number in case of multi-byte char sets;
    * use {@link #vm(SpecificCharacterSet)} or for number of items/fragments
    * in sequences {@link #countItems()} instead.
    */
   int vm();

   int vm(SpecificCharacterSet cs);
   
   int countItems();
   
   int length();
   
   boolean isEmpty();
   
   int hashCode();
   
   DcmElement share();
      
   ByteBuffer getByteBuffer();
   
   ByteBuffer getByteBuffer(ByteOrder byteOrder);
   
   boolean hasDataFragments();
   
   ByteBuffer getDataFragment(int index);
   
   ByteBuffer getDataFragment(int index, ByteOrder byteOrder);
   
   int getDataFragmentLength(int index);
   
   String getString(SpecificCharacterSet cs) throws DcmValueException;
   
   String getString(int index, SpecificCharacterSet cs) throws DcmValueException;
   
   String[] getStrings(SpecificCharacterSet cs) throws DcmValueException;
   
   String getBoundedString(int maxLen, SpecificCharacterSet cs)
   throws DcmValueException;
   
   String getBoundedString(int maxLen, int index, SpecificCharacterSet cs)
   throws DcmValueException;
   
   String[] getBoundedStrings(int maxLen, SpecificCharacterSet cs)
   throws DcmValueException;
   
   int getInt() throws DcmValueException;
   
   int getInt(int index) throws DcmValueException;
   
   int[] getInts() throws DcmValueException;
   
   int getTag() throws DcmValueException;
   
   int getTag(int index) throws DcmValueException;
   
   int[] getTags() throws DcmValueException;
   
   float getFloat() throws DcmValueException;
   
   float getFloat(int index) throws DcmValueException;
   
   float[] getFloats() throws DcmValueException;
   
   double getDouble() throws DcmValueException;
   
   double getDouble(int index) throws DcmValueException;
   
   double[] getDoubles() throws DcmValueException;
   
   Date getDate() throws DcmValueException;
   
   Date getDate(int index) throws DcmValueException;
   
   Date[] getDates() throws DcmValueException;

   Date[] getDateRange() throws DcmValueException;

   PersonName getPersonName(SpecificCharacterSet cs)  throws DcmValueException;

   PersonName getPersonName(int index, SpecificCharacterSet cs) throws DcmValueException;
   
   PersonName[] getPersonNames(SpecificCharacterSet cs)  throws DcmValueException;
   
   void addDataFragment(ByteBuffer byteBuffer);
   
   boolean hasItems();
   
   Dataset addNewItem();
   
   void addItem(Dataset item);
   
   Dataset getItem();
   
   Dataset getItem(int index);
   
   DcmElement setStreamPosition(long streamPos);
   
   long getStreamPosition();

}
