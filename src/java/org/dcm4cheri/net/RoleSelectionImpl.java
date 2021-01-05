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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.dcm4che.net.AAbort;
import org.dcm4che.net.PDUException;
import org.dcm4che.net.RoleSelection;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class RoleSelectionImpl implements RoleSelection {

    private final String asuid;
    private final boolean scu;
    private final boolean scp;
    
    /** Creates a new instance of RoleSelectionImpl */
    RoleSelectionImpl(String asuid, boolean scu, boolean scp) {
        this.asuid = asuid;
        this.scu = scu;
        this.scp = scp;
    }
    
    RoleSelectionImpl(DataInputStream din, int len)
            throws IOException, PDUException {
        int uidLen = din.readUnsignedShort();
        if (uidLen + 4 != len) {
            throw new PDUException( "SCP/SCU role selection sub-item length: "
                    + len + " mismatch UID-length:" + uidLen,
                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                               AAbort.INVALID_PDU_PARAMETER_VALUE));
        } 
        this.asuid = AAssociateRQACImpl.readASCII(din, uidLen);
        this.scu = din.readBoolean();
        this.scp = din.readBoolean();
    }

    public final String getSOPClassUID() {
        return asuid;
    }    

    public final boolean scu() {
        return scu;
    }

    public final boolean scp() {
        return scp;
    }
    
    final int length() {
        return 4 + asuid.length();
    }
    
    void writeTo(DataOutputStream dout) throws IOException {
        dout.write(0x54);
        dout.write(0);
        dout.writeShort(length());
        dout.writeShort(asuid.length());
        dout.writeBytes(asuid);
        dout.writeBoolean(scu);
        dout.writeBoolean(scp);         
    }
    
    public String toString() {
        return "RoleSelection[sop=" + AAssociateRQACImpl.DICT.lookup(asuid)
                + ", scu=" + scu + ", scp=" + scp + "]";
    }    
}
