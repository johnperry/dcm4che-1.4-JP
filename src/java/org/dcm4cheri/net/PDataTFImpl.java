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

package org.dcm4cheri.net;

import org.dcm4che.net.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class PDataTFImpl implements PDataTF {
   
   private static final int DEF_MAX_LENGTH = 0xFFFF;
   private static final int MIN_MAX_LENGTH = 128;
   private final byte[] buf;
   private int pdulen;
   private int wpos;
   private final LinkedList pdvs = new LinkedList();
   private final Iterator it;
   private PDVImpl curPDV = null;
   
   static PDataTFImpl parse(UnparsedPDUImpl raw) throws PDUException {
      if (raw.buffer() == null) {
         throw new PDUException(
         "PDU length exceeds supported maximum " + raw,
         new AAbortImpl(AAbort.SERVICE_PROVIDER,
         AAbort.REASON_NOT_SPECIFIED));
      }
      return new PDataTFImpl(raw.length(), raw.buffer());
   }
   
   private PDataTFImpl(int pdulen, byte[] buf) throws PDUException {
      this.pdulen = pdulen;
      this.wpos = pdulen + 12;
      this.buf = buf;
      int off = 6;
      while (off <= pdulen) {
         PDVImpl pdv = new PDVImpl(off);
         pdvs.add(pdv);
         off += 4 + pdv.length();
      }
      if (off != pdulen + 6) {
         throw new PDUException("Illegal " + toString(),
         new AAbortImpl(AAbort.SERVICE_PROVIDER,
         AAbort.INVALID_PDU_PARAMETER_VALUE));
      }
      this.it = pdvs.iterator();
   }
   
   PDataTFImpl(int maxLength) {
      if (maxLength == 0) {
         maxLength = DEF_MAX_LENGTH;
      }
      if (maxLength < MIN_MAX_LENGTH
      || maxLength > UnparsedPDUImpl.MAX_LENGTH) {
         throw new IllegalArgumentException("maxLength:" + maxLength);
      }
      this.pdulen = 0;
      this.wpos = 12;
      this.buf = new byte[6 + maxLength];
      this.it = null;
   }
   
   public void clear() {
      if (it != null) {
         throw new IllegalStateException("P-DATA-TF read only");
      }
      pdulen = 0;
      wpos = 12;
      pdvs.clear();
   }
   
   public PDV readPDV() {
      if (it == null) {
         throw new IllegalStateException("P-DATA-TF write only");
      }
      return it.hasNext() ? (PDV)it.next() : null;
   }
   
   public String toString(boolean verbose) {
      return toString();
   }
   
   public String toString() {
      return toStringBuffer(new StringBuffer()).toString();
   }
   
   StringBuffer toStringBuffer(StringBuffer sb) {
      sb.append("P-DATA-TF[pdulen=").append(pdulen).append("]");
      Iterator it = pdvs.iterator();
      while (it.hasNext()) {
         sb.append("\n\t").append(it.next());
      }
      return sb;
   }
   
   public final int free() {
      return buf.length - wpos;
   }
   
   public void openPDV(int pcid, boolean cmd) {
      if (it != null) {
         throw new IllegalStateException("P-DATA-TF read only");
      }
      if (curPDV != null) {
         throw new IllegalStateException("Open PDV " + curPDV);
      }
      if (free() < 0) {
         throw new IllegalStateException("Maximal length of PDU reached");
      }
      curPDV = new PDVImpl(6 + pdulen);
      curPDV.pcid(pcid);
      curPDV.cmd(cmd);
      pdulen += 6;
   }
   
   boolean isOpenPDV() {
      return curPDV != null;
   }
   
   boolean isEmpty() {
      return pdvs.isEmpty();
   }
   
   public void closePDV(boolean last) {
      if (curPDV == null) {
         throw new IllegalStateException("No open PDV");
      }
      curPDV.last(last);
      curPDV.close();
      pdvs.add(curPDV);
      curPDV = null;
      wpos += 6;
   }
   
   public final boolean write(int b) {
      if (curPDV == null) {
         throw new IllegalStateException("No open PDV");
      }
      if (wpos >= buf.length) return false;
      buf[wpos++] = (byte)b;
      ++pdulen;
      return true;
   }
   
   public final int write(byte[] b, int off, int len) {
      if (curPDV == null) {
         throw new IllegalStateException("No open PDV");
      }
      int wlen = Math.min(len, buf.length - wpos);
      System.arraycopy(b, off, buf, wpos, wlen);
      wpos += wlen;
      pdulen += wlen;
      return wlen;
   }
   
   public void writeTo(OutputStream out) throws IOException {
      if (curPDV != null) {
         throw new IllegalStateException("Open PDV " + curPDV);
      }
      buf[0] = (byte)4;
      buf[1] = (byte)0;
      buf[2] = (byte)(pdulen >> 24);
      buf[3] = (byte)(pdulen >> 16);
      buf[4] = (byte)(pdulen >> 8);
      buf[5] = (byte)(pdulen >> 0);
      out.write(buf, 0, pdulen + 6);
   }
   
   final class PDVImpl implements PDataTF.PDV {
      final int off;
      PDVImpl(int off) {
         this.off = off;
         buf[off+5] &= 3;
      }
      
      final void pcid(int pcid) {
         buf[off+4] = (byte)pcid;
      }
      
      final void length(int pdvLen) {
         buf[off] = (byte)(pdvLen >> 24);
         buf[off+1] = (byte)(pdvLen >> 16);
         buf[off+2] = (byte)(pdvLen >> 8);
         buf[off+3] = (byte)(pdvLen >> 0);
      }
      
      final void cmd(boolean cmd) {
         if (cmd) {
            buf[off+5] |= 1;
         } else {
            buf[off+5] &= ~1;
         }
      }
      
      final void last(boolean last) {
         if (last) {
            buf[off+5] |= 2;
         } else {
            buf[off+5] &= ~2;
         }
      }
      
      final void close() {
         length(wpos - off - 4);
      }
      
      public final int length() {
         return ((buf[off] & 0xff) << 24)
         | ((buf[off+1] & 0xff) << 16)
         | ((buf[off+2] & 0xff) << 8)
         | ((buf[off+3] & 0xff) << 0);
      }
      
      public final int pcid() {
         return buf[off+4] & 0xFF;
      }
      
      public final boolean cmd() {
         return (buf[off+5] & 1) != 0;
      }
      
      public final boolean last() {
         return (buf[off+5] & 2) != 0;
      }
      
      public final InputStream getInputStream() {
         return new ByteArrayInputStream(buf, off + 6, length() - 2);
      }
           
      public String toString() {
         return toStringBuffer(new StringBuffer()).toString();
      }
      
      StringBuffer toStringBuffer(StringBuffer sb) {
         return sb.append("PDV[pc-").append(pcid())
         .append(cmd() ? ",cmd" : ",data")
         .append(last() ? "(last),off=" : ",off=").append(off)
         .append(",pdvlen=").append(length())
         .append("]");
      }
   }
}
