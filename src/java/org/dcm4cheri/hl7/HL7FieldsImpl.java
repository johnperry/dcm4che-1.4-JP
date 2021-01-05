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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 *   
 */
class HL7FieldsImpl implements HL7Fields
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   protected final byte[] data;
   protected final int off;
   protected final int len;
   protected final byte[] delim;
   protected HL7Fields[] subFields = null;
   
   // Static --------------------------------------------------------
   static final HL7Fields NULL = new HL7Fields() {
      public String toString() {
         return "";
      }

      public String get(int index) {
         return "";
      }
         
      public int size() {
         return 0;
      }
      
      public String get(int[] index) {
         return "";
      }
      
      public int size(int index) {
         return 0;
      }
         
      public int size(int[] index) {
         return 0;
      }
      
      public void writeTo(ByteArrayOutputStream out) {
      }

      public void writeTo(int index, ByteArrayOutputStream out) {
      }
   };
   
   static HL7Fields newInstance(byte[] data, int off, int len, byte[] delim) {
      return len == 0 ? NULL : new HL7FieldsImpl(data, off, len, delim);
   }
   
   // Constructors --------------------------------------------------
   public HL7FieldsImpl(byte[] data, int off, int len, byte[] delim) {
      if (data.length < off + len) {
         throw new IllegalArgumentException("data.length[" + data.length
               + "] < off[" + off + "] + len [" + len + "]");
      }
      if (delim.length == 0) {
         throw new IllegalArgumentException("delim.length == 0");
      }
      this.data = data;
      this.off = off;
      this.len = len;
      this.delim = delim;
   }
   
   // Public --------------------------------------------------------
   
   public int size() {
      initSubFields();
      return subFields.length;
   }

   public int size(int index) {
     if (delim.length == 0) {
         throw new IllegalArgumentException("delim.length == 0");
     }
     initSubFields();
     return index < subFields.length ? subFields[index].size() : 0;
   }

   public int size(int[] index) {
      if (index.length > delim.length) {
         throw new IllegalArgumentException("index.length[" + index.length
               + "] > delim.length[" + delim.length + "]");
      }
      switch (index.length) {
         case 0:
            return size();
         case 1:
            return size(index[0]);
         default:
            initSubFields();
            if (index[0] >= subFields.length) {
               return 0;
            }
            int[] index_1 = new int[index.length-1];
            System.arraycopy(index, 1, index_1, 0, index_1.length);
            return subFields[index[0]].size(index_1);
      }
   }

   public void writeTo(ByteArrayOutputStream out) {
      out.write(data, off, len);
   }

   public void writeTo(int index, ByteArrayOutputStream out) {
      initSubFields();
      if (index < subFields.length) {
         subFields[index].writeTo(out);
      }
   }

   public String toString() {
       try {
           return new String(data, off, len, "ISO-8859-1");
       } catch (UnsupportedEncodingException e) {
           return new String(data, off, len);
       }
   }
   
   public String get(int index) {
     if (delim.length == 0) {
         throw new IllegalArgumentException("delim.length == 0");
     }
     initSubFields();
     return index < subFields.length ? subFields[index].toString() : "";
   }      
   
   public String get(int[] index) {
      if (index.length > delim.length) {
         throw new IllegalArgumentException("index.length[" + index.length
               + "] > delim.length[" + delim.length + "]");
      }
      switch (index.length) {
         case 0:
            return toString();
         case 1:
            return get(index[0]);
         default:
            initSubFields();
            if (index[0] >= subFields.length) {
               return "";
            }
            int[] index_1 = new int[index.length-1];
            System.arraycopy(index, 1, index_1, 0, index_1.length);
            return subFields[index[0]].get(index_1);
      }
   }

   private void initSubFields() {
      if (subFields != null) {
         return;
      }
      
      int index = 0;
      for (int i = off, n = off + len; i < n; ++i) {
         if (data[i] == delim[0]) {
            ++index;
         }
      }
      subFields = new HL7Fields[index+1];
      byte[] delim_1 = new byte[delim.length-1];
      System.arraycopy(delim, 1, delim_1, 0, delim_1.length);
      index = 0;
      int left = off;
      for (int i = off, n = off + len; i < n; ++i) {
         if (data[i] == delim[0]) {
            subFields[index++] = newInstance(data, left, i - left, delim_1);
            left = i+1;
         }
      }
      subFields[index] = newInstance(data, left, off + len - left, delim_1);      
   }   
}
