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

package org.dcm4cheri.hl7;

import org.dcm4che.hl7.HL7Segment;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 */
public class HL7SegmentImpl extends HL7FieldsImpl
implements HL7Segment
{
   // Constants -----------------------------------------------------   
   private static final int MIN_LEN = 4;
   private static final byte[] DELIM = {
      (byte)'|', (byte)'~', (byte)'^', (byte)'&'
   };

   // Attributes ----------------------------------------------------
   private final String id;
   
   // Static --------------------------------------------------------
   static final ResourceBundle DICT =
         ResourceBundle.getBundle("org/dcm4cheri/hl7/HL7Dictionary");
   
   static String getName(String key, String defVal) {
      try {
         return DICT.getString(key);
      } catch (MissingResourceException e) {
         return defVal;
      }
   }
   
   // Constructors --------------------------------------------------
   HL7SegmentImpl(byte[] data, int off, int len) {
      super(data, off, len, DELIM);
      if (len < MIN_LEN || data[off+3] != (byte)'|') {
         throw new IllegalArgumentException(toString());
      }
      this.id = super.get(0);
   }
   
   // Public --------------------------------------------------------
   public String id() {
      return id;
   }
   
   public String get(int seq, int rep) {
      return super.get(new int[]{ seq, rep-1 });
   }
   
   public String get(int seq, int rep, int comp) {
      return super.get(new int[]{ seq, rep-1, comp-1 });
   }
   
   public String get(int seq, int rep, int comp, int sub) {
      return super.get(new int[]{ seq, rep-1, comp-1, sub-1 });
   }
   
   public int size(int seq, int rep) {
      return super.size(new int[]{ seq, rep-1 });
   }

   public int size(int seq, int rep, int comp) {
      return super.size(new int[]{ seq, rep-1, comp-1 });
   }
   
   StringBuffer toVerboseStringBuffer(StringBuffer sb) {
      sb.append(id).append(" - ").append(getName(id, ""));
      for (int i = 1, n = size(); i < n; ++i) {
         String key = id + '.' + i;
         sb.append("\n\t").append(key)
           .append(": ").append(get(i)).append("\t\t//")
           .append(getName(key, "????"));
      }
      return sb;
   }

   public String toVerboseString() {
      return toVerboseStringBuffer(new StringBuffer()).toString();
   }      
}
