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

import org.dcm4che.net.AAbort;
import org.dcm4che.net.PDUException;
import org.dcm4che.net.UserIdentityAC;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 4078 $ $Date: 2007-01-25 15:52:19 +0100 (Do, 25 Jän 2007) $
 * @since Jan 25, 2007
 */
final class UserIdentityACImpl implements UserIdentityAC {

    private static final byte[] EMPTY = {};

    private final byte[] serverResponse;

    UserIdentityACImpl(byte[] serverResponse) {
        this.serverResponse = serverResponse == null ? EMPTY
                    : (byte[]) serverResponse.clone();
    }

    public UserIdentityACImpl() {
        this.serverResponse = EMPTY;
    }
    
    UserIdentityACImpl(DataInputStream din, int len) throws IOException {
        int rsplen = din.readUnsignedShort();
        if (rsplen + 2 != len) {
            throw new PDUException( "User Identity selection sub-item length: "
                    + len + " mismatch Server Response length:" + rsplen,
                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                               AAbort.INVALID_PDU_PARAMETER_VALUE));
        } 
        this.serverResponse = new byte[rsplen];
        din.readFully(serverResponse);
    }

    void writeTo(DataOutputStream dout) throws IOException {
        dout.write(0x59);
        dout.write(0);
        dout.writeShort(length());
        dout.writeShort(serverResponse.length);
        dout.write(serverResponse);
    }
    
    public final byte[] getServerResponse() {
        return (byte[]) serverResponse.clone();
    }

    public int length() {
        return 2 + serverResponse.length;
    }

    public String toString() {
        return "UserIdentity[serverResponse(" + serverResponse.length + ")]";
    }
}
