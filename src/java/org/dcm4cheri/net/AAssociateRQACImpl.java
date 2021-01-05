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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.dcm4che.Implementation;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAbort;
import org.dcm4che.net.AAssociateRQAC;
import org.dcm4che.net.AsyncOpsWindow;
import org.dcm4che.net.CommonExtNegotiation;
import org.dcm4che.net.ExtNegotiation;
import org.dcm4che.net.PDUException;
import org.dcm4che.net.PDataTF;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.RoleSelection;
import org.dcm4che.net.UserIdentityAC;
import org.dcm4che.net.UserIdentityRQ;
import org.dcm4cheri.util.StringUtils;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
abstract class AAssociateRQACImpl implements AAssociateRQAC {
   static final Logger log = Logger.getLogger(AAssociateRQACImpl.class);
   static UIDDictionary DICT =
         DictionaryFactory.getInstance().getDefaultUIDDictionary();
   
   private String appCtxUID = UIDs.DICOMApplicationContextName;
   private int version = 1;
   private int maxLength = PDataTF.DEF_MAX_PDU_LENGTH;
   private String callingAET = "ANONYMOUS";
   private String calledAET = "ANONYMOUS";
   private String implClassUID = Implementation.getClassUID();
   private String implVers = Implementation.getVersionName();
   private AsyncOpsWindow asyncOpsWindow = null;
   protected UserIdentityRQ userIdentityRQ = null;
   protected UserIdentityAC userIdentityAC = null;
   protected final LinkedHashMap presCtxs = new LinkedHashMap();
   protected final LinkedHashMap roleSels = new LinkedHashMap();
   protected final LinkedHashMap extNegs = new LinkedHashMap();
   protected final LinkedHashMap commonExtNegs = new LinkedHashMap();

   
   protected AAssociateRQACImpl init(UnparsedPDUImpl raw) throws PDUException {
      if (raw.buffer() == null) {
         throw new PDUException("PDU length exceeds supported maximum " + raw,
            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                           AAbort.REASON_NOT_SPECIFIED));
      }
      ByteArrayInputStream bin = new ByteArrayInputStream(
      raw.buffer(), 6, raw.length());
      DataInputStream din = new DataInputStream(bin);
      try {
         version = din.readShort();
         din.readUnsignedByte();
         din.readUnsignedByte();
         calledAET = readASCII(din, 16).trim();
         callingAET = readASCII(din, 16).trim();
         if (din.skip(32) != 32) {
            throw new EOFException();
         }
         while (din.available() > 0) {
            int itemType = din.readUnsignedByte();
            din.readUnsignedByte();
            int itemLen = din.readUnsignedShort();
            switch (itemType) {
               case 0x10:
                  appCtxUID = readASCII(din, itemLen);
                  break;
               case 0x20:
               case 0x21:
                  if (itemType != pctype()) {
                       log.warn("Treat unexpected item[type="
                              + Integer.toHexString(itemType)
                              + "H, length=" + itemLen + "] in received "
                              + typeAsString() + " as item[type=" 
                              + Integer.toHexString(pctype()) + "H]");
                  }
                  addPresContext(
                          new PresContextImpl(pctype(), din, itemLen));
                  break;
               case 0x50:
                  readUserInfo(din, itemLen);
                  break;
               default:
                   log.warn("Skip unrecognized item[type="
                           + Integer.toHexString(itemType)
                           + "H, length=" + itemLen 
                           + "] in received " + typeAsString());
                   din.skipBytes(itemLen);
            }
         }
      } catch (PDUException e) {
         throw e;
      } catch (Exception e) {
         throw new PDUException("Failed to parse " + raw, e,
         new AAbortImpl(AAbort.SERVICE_PROVIDER,
         AAbort.REASON_NOT_SPECIFIED));
      }
      return this;
   }
   
   public final int getProtocolVersion() {
      return version;
   }
   
   public final void setProtocolVersion(int version) {
      this.version = version;
   }
   
   public String getCalledAET() {
      return calledAET;
   }
   
   public String getCallingAET() {
      return callingAET;
   }
   
   public void setCalledAET(String aet) {
      this.calledAET = StringUtils.checkAET(aet);
   }
   
   public void setCallingAET(String aet) {
      this.callingAET = StringUtils.checkAET(aet);
   }
   
   public final String getApplicationContext() {
      return appCtxUID;
   }
   
   public final void setApplicationContext(String appCtxUID) {
      appCtxUID = StringUtils.checkUID(appCtxUID);
   }
   
   public final int nextPCID() {
      int c = presCtxs.size();
      if (c == 128) {
         return -1;
      }
      int retval = ((c << 1) | 1);
      while (presCtxs.containsKey(new Integer(retval))) {
         retval = (retval + 2) % 256;
      }
      return retval;
   }
   
   public final PresContext addPresContext(PresContext presCtx) {
      if (((PresContextImpl)presCtx).type() != pctype()) {
         throw new IllegalArgumentException("wrong type of " + presCtx);
      }
      return (PresContext)presCtxs.put(
      new Integer(presCtx.pcid()), presCtx);
   }
   
   public final PresContext removePresContext(int pcid) {
      return (PresContext)presCtxs.remove(new Integer(pcid));
   }
   
   public final PresContext getPresContext(int pcid) {
      return (PresContext)presCtxs.get(new Integer(pcid));
   }
   
   public final Collection listPresContext() {
      return presCtxs.values();
   }
   
   public final void clearPresContext() {
      presCtxs.clear();
   }
   
   public final String getImplClassUID() {
      return implClassUID;
   }
   
   public final void setImplClassUID(String uid) {
      this.implClassUID = StringUtils.checkUID(uid);
   }
   
   public final String getImplVersionName() {
      return implVers;
   }
   
   public final void setImplVersionName(String name) {
      this.implVers = name != null ? StringUtils.checkAET(name) : null;
   }
   
   public final int getMaxPDULength() {
      return maxLength;
   }
   
   public final void setMaxPDULength(int maxLength) {
      if (maxLength < 0) {
         throw new IllegalArgumentException("maxLength:" + maxLength);
      }
      this.maxLength = maxLength;
   }
   
   public final AsyncOpsWindow getAsyncOpsWindow() {
      return asyncOpsWindow;
   }
   
   public final void setAsyncOpsWindow(AsyncOpsWindow aow) {
      this.asyncOpsWindow = aow;
   }
   
   public final RoleSelection removeRoleSelection(String uid) {
      return (RoleSelection)roleSels.remove(uid);
   }
   
   public final RoleSelection getRoleSelection(String uid) {
      return (RoleSelection)roleSels.get(uid);
   }
   
   public Collection listRoleSelections() {
      return roleSels.values();
   }
   
   public void clearRoleSelections() {
      roleSels.clear();
   }
   
   public final ExtNegotiation removeExtNegotiation(String uid) {
      return (ExtNegotiation)extNegs.remove(uid);
   }
   
   public final ExtNegotiation getExtNegotiation(String uid) {
      return (ExtNegotiation)extNegs.get(uid);
   }
   
   public Collection listExtNegotiations() {
      return extNegs.values();
   }
   
   public void clearExtNegotiations() {
      extNegs.clear();
   }

   static String readASCII(DataInputStream in, int len)
   throws IOException {
      byte[] b = new byte[len];
      in.readFully(b);
      while (len > 0 && b[len-1] == 0) --len;
      return new String(b, 0, len, "US-ASCII");
   }
   
   public RoleSelection addRoleSelection(RoleSelection roleSel) {
      return (RoleSelection)roleSels.put(
      roleSel.getSOPClassUID(), roleSel);
   }
   
   public ExtNegotiation addExtNegotiation(ExtNegotiation extNeg) {
      return (ExtNegotiation)extNegs.put(extNeg.getSOPClassUID(), extNeg);
   }
   
   public CommonExtNegotiation addCommonExtNegotiation(
           CommonExtNegotiation extNeg) {
       return (CommonExtNegotiation) 
               commonExtNegs.put(extNeg.getSOPClassUID(), extNeg);
    }
    
   private void readUserInfo(DataInputStream din, int len)
   throws IOException, PDUException {
      int diff = len - din.available();
      if (diff != 0) {
         throw new PDUException("User info item length=" + len
         + " mismatch PDU length (diff=" + diff + ")",
         new AAbortImpl(AAbort.SERVICE_PROVIDER,
         AAbort.INVALID_PDU_PARAMETER_VALUE));
      }
      while (din.available() > 0) {
         int subItemType = din.readUnsignedByte();
         din.readUnsignedByte();
         int itemLen = din.readUnsignedShort();
         switch (subItemType) {
            case 0x51:
               if (itemLen != 4) {
                  throw new PDUException(
                  "Illegal length of Maximum length sub-item: "
                  + itemLen,
                  new AAbortImpl(AAbort.SERVICE_PROVIDER,
                  AAbort.INVALID_PDU_PARAMETER_VALUE));
               }
               maxLength = din.readInt();
               break;
            case 0x52:
               implClassUID = readASCII(din, itemLen);
               break;
            case 0x53:
               asyncOpsWindow = new AsyncOpsWindowImpl(din, itemLen);
               break;
            case 0x54:
               addRoleSelection(new RoleSelectionImpl(din, itemLen));
               break;
            case 0x55:
               implVers = readASCII(din, itemLen);
               break;
            case 0x56:
               addExtNegotiation(new ExtNegotiationImpl(din, itemLen));
               break;
            case 0x57:
               addCommonExtNegotiation(new CommonExtNegotiationImpl(din, itemLen));
               break;
            case 0x58:
               userIdentityRQ = new UserIdentityRQImpl(din, itemLen);
               break;
            case 0x59:
               userIdentityAC = new UserIdentityACImpl(din, itemLen);
               break;
          default:
              log.warn("Skip unrecognized user sub-item [type="
                      + Integer.toHexString(subItemType)
                      + "H, length=" + itemLen 
                      + "] in received " + typeAsString());
              din.skipBytes(itemLen);
         }
      }
   }
   
   protected abstract int type();
   protected abstract int pctype();
   
   private static final byte[] ZERO32 = {
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
   };
   
   private void writeAE(DataOutputStream dout, String aet)
   throws IOException {
      dout.writeBytes(aet);
      for (int n = aet.length(); n < 16; ++n) {
         dout.write(' ');
      }
   }
   
   private static final class MyByteArrayOutputStream
   extends ByteArrayOutputStream {
      MyByteArrayOutputStream() {
         super(4096);
         write(0);
         write(0);
         write(0);
         write(0);
         write(0);
         write(0);
      }
      void writeTo(int type, OutputStream out) throws IOException {
         int len = count - 6;
         buf[0] = (byte)type;
         buf[1] = (byte)0;
         buf[2] = (byte)(len >> 24);
         buf[3] = (byte)(len >> 16);
         buf[4] = (byte)(len >> 8);
         buf[5] = (byte)(len >> 0);
         out.write(buf, 0, count);
      }
   }
   
   public final void writeTo(OutputStream out) throws IOException {
      MyByteArrayOutputStream bout = new MyByteArrayOutputStream();
      DataOutputStream dout = new DataOutputStream(bout);
      dout.writeShort(version);
      dout.write(0);
      dout.write(0);
      writeAE(dout, calledAET);
      writeAE(dout, callingAET);
      dout.write(ZERO32);
      dout.write(0x10);
      dout.write(0);
      dout.writeShort(appCtxUID.length());
      dout.writeBytes(appCtxUID);
      for (Iterator it = presCtxs.values().iterator(); it.hasNext();) {
         ((PresContextImpl)it.next()).writeTo(dout);
      }
      writeUserInfo(dout);
      bout.writeTo(type(), out);
   }
   
   private void writeUserInfo(DataOutputStream dout) throws IOException {
      dout.write(0x50);
      dout.write(0);
      dout.writeShort(getUserInfoLength());
      dout.write(0x51);
      dout.write(0);
      dout.writeShort(4);
      dout.writeInt(maxLength);
      dout.write(0x52);
      dout.write(0);
      dout.writeShort(implClassUID.length());
      dout.writeBytes(implClassUID);
      if (asyncOpsWindow != null) {
         ((AsyncOpsWindowImpl)asyncOpsWindow).writeTo(dout);
      }
      for (Iterator it = roleSels.values().iterator(); it.hasNext();) {
         ((RoleSelectionImpl)it.next()).writeTo(dout);
      }
      if (implVers != null) {
         dout.write(0x55);
         dout.write(0);
         dout.writeShort(implVers.length());
         dout.writeBytes(implVers);
      }
      for (Iterator it = extNegs.values().iterator(); it.hasNext();) {
         ((ExtNegotiationImpl)it.next()).writeTo(dout);
      }
      for (Iterator it = commonExtNegs.values().iterator(); it.hasNext();) {
          ((CommonExtNegotiationImpl)it.next()).writeTo(dout);
      }
      if (userIdentityRQ != null) {
          ((UserIdentityRQImpl)userIdentityRQ).writeTo(dout);
      }
      if (userIdentityAC != null) {
          ((UserIdentityACImpl)userIdentityAC).writeTo(dout);
      }
   }
   
   private int getUserInfoLength() {
      int retval = 12 + implClassUID.length();
      if (asyncOpsWindow != null) {
         retval += 8;
      }
      for (Iterator it = roleSels.values().iterator(); it.hasNext();) {
         RoleSelectionImpl rs = (RoleSelectionImpl)it.next();
         retval += 4 + rs.length();
      }
      if (implVers != null) {
         retval += 4 + implVers.length();
      }
      for (Iterator it = extNegs.values().iterator(); it.hasNext();) {
         ExtNegotiationImpl en = (ExtNegotiationImpl)it.next();
         retval += 4 + en.length();
      }
      for (Iterator it = commonExtNegs.values().iterator(); it.hasNext();) {
          CommonExtNegotiationImpl en = (CommonExtNegotiationImpl)it.next();
          retval += 4 + en.length();
      }
      if (userIdentityRQ != null) {
          retval += 4 + ((UserIdentityRQImpl)userIdentityRQ).length();
      }
      if (userIdentityAC != null) {
          retval += 4 + ((UserIdentityACImpl)userIdentityAC).length();
      }
      return retval;
   }
   
   protected abstract String typeAsString();
   
   public String toString() {
      return toString(true);
   }
   
   public String toString(boolean verbose) {
      return toStringBuffer(new StringBuffer(), verbose).toString();
   }

   final StringBuffer toStringBuffer(StringBuffer sb, boolean verbose) {
      sb.append(typeAsString())
        .append("\n\tappCtxName:\t").append(DICT.lookup(appCtxUID))
        .append("\n\timplClass:\t").append(implClassUID)
        .append("\n\timplVersion:\t").append(implVers)
        .append("\n\tcalledAET:\t").append(calledAET)
        .append("\n\tcallingAET:\t").append(callingAET)
        .append("\n\tmaxPDULen:\t").append(maxLength)
        .append("\n\tasyncOpsWindow:\t");
      if (asyncOpsWindow != null) {
          sb.append("maxOpsInvoked=")
            .append(asyncOpsWindow.getMaxOpsInvoked())
            .append(", maxOpsPerformed=")
            .append(asyncOpsWindow.getMaxOpsPerformed());
      }
      if (verbose) {
         for (Iterator it = presCtxs.values().iterator(); it.hasNext();) {
            append((PresContext)it.next(), sb);
         }
         for (Iterator it = roleSels.values().iterator(); it.hasNext();) {
            sb.append("\n\t").append(it.next());
         }
         for (Iterator it = extNegs.values().iterator(); it.hasNext();) {
            sb.append("\n\t").append(it.next());
         }
         for (Iterator it = commonExtNegs.values().iterator(); it.hasNext();) {
             sb.append("\n\t").append(it.next());
          }
      } else {
        appendPresCtxSummary(sb);
        sb.append("\n\troleSel:\t#").append(roleSels.size())
          .append("\n\textNego:\t#").append(extNegs.size())
          .append("\n\tcommonExtNego:\t#").append(commonExtNegs.size());
      }
      return sb;
   }
   
   protected abstract void append(PresContext pc, StringBuffer sb);
   protected abstract void appendPresCtxSummary(StringBuffer sb);

}
