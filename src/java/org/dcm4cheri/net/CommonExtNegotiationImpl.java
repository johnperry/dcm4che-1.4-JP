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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dcm4che.net.AAbort;
import org.dcm4che.net.CommonExtNegotiation;
import org.dcm4che.net.PDUException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 4078 $ $Date: 2007-01-25 15:52:19 +0100 (Do, 25 Jän 2007) $
 * @since Jan 25, 2007
 */
final class CommonExtNegotiationImpl implements CommonExtNegotiation {

    private final String sopCUID;
    private final String serviceCUID;
    private final List relGenSopCUIDs;

    CommonExtNegotiationImpl(String sopCUID, String serviceCUID, 
            String[] relGenSopCUIDs) {
        this.sopCUID = sopCUID;
        this.serviceCUID = serviceCUID;
        this.relGenSopCUIDs = relGenSopCUIDs == null ? new ArrayList(0)
                    : new ArrayList(Arrays.asList(relGenSopCUIDs));
    }
    
    CommonExtNegotiationImpl(DataInputStream din, int len) throws IOException {
        int remaining = len - 6;
        int uidLen = din.readUnsignedShort();
        checkLength(remaining -= uidLen, len);
        this.sopCUID = AAssociateRQACImpl.readASCII(din, uidLen);
        uidLen = din.readUnsignedShort();
        checkLength(remaining -= uidLen, len);
        this.serviceCUID = AAssociateRQACImpl.readASCII(din, uidLen);
        int uidsLen = din.readUnsignedShort();
        this.relGenSopCUIDs = new ArrayList(1);
        checkLength(remaining -= uidsLen, len);
        int remaining2 = uidsLen;
        while (remaining2 > 0) {
            uidLen = din.readUnsignedShort();
            checkRelGenSopCUIDsLen(remaining2 -= uidLen, uidsLen);
            relGenSopCUIDs.add(AAssociateRQACImpl.readASCII(din, uidLen));
        }
    }

    private static void checkLength(int remaining, int len)
            throws PDUException {
        if (remaining < 0) {
            throwPDUException("Common extended negotiation sub-item length: "
                + len + " mismatch content length: >=" + (len - remaining));
        }
    }

    private static void checkRelGenSopCUIDsLen(int remaining, int len)
            throws PDUException {
        if (remaining < 0) {
            throwPDUException("Related-general-sop-class-identification-length: "
                + len + " mismatch content length: >=" + (len - remaining));
        }
    }

    private static void throwPDUException(String msg) throws PDUException {
        throw new PDUException(msg,
                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                           AAbort.INVALID_PDU_PARAMETER_VALUE));
    }
        
    public final String getSOPClassUID() {
        return sopCUID;
    }

    public final String getServiceClassUID() {
        return serviceCUID;
    }
    
    public final List getRelatedGeneralSOPClassUIDs() {
        return Collections.unmodifiableList(relGenSopCUIDs);
    }
    
    final int length() {
        return 6 + sopCUID.length() + serviceCUID.length() 
                + relGenSopCUIDsLength();
    }
    
    private int relGenSopCUIDsLength() {
        int len = 0;
        for (Iterator iter = relGenSopCUIDs.iterator(); iter.hasNext();) {
            len += 2 + ((String) iter.next()).length();            
        }
        return len;
    }

    void writeTo(DataOutputStream dout) throws IOException {
        dout.write(0x57);
        dout.write(0);
        dout.writeShort(length());
        dout.writeShort(sopCUID.length());
        dout.writeBytes(sopCUID);
        dout.writeShort(serviceCUID.length());
        dout.writeBytes(serviceCUID);
        dout.writeShort(relGenSopCUIDsLength());
        for (Iterator iter = relGenSopCUIDs.iterator(); iter.hasNext();) {
            String uid = (String) iter.next();
            dout.writeShort(uid.length());
            dout.writeBytes(uid);           
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append("CommonExtNegotiation[sop=")
                .append(AAssociateRQACImpl.DICT.lookup(sopCUID))
                .append(", service=")
                .append(AAssociateRQACImpl.DICT.lookup(serviceCUID));
        for (Iterator iter = relGenSopCUIDs.iterator(); iter.hasNext();) {
            sb.append(", relGenSop=")
                .append(AAssociateRQACImpl.DICT.lookup((String) iter.next()));
        }
        sb.append("]");
        return sb.toString();
    }
    
}
