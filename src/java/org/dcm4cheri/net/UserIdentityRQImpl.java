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
import java.io.UnsupportedEncodingException;

import org.dcm4che.net.AAbort;
import org.dcm4che.net.PDUException;
import org.dcm4che.net.UserIdentityRQ;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 4894 $ $Date: 2007-08-21 10:13:26 +0200 (Di, 21 Aug 2007) $
 * @since Jan 25, 2007
 */
final class UserIdentityRQImpl implements UserIdentityRQ {

    private static final byte[] EMPTY = {};

    private final int userIdentityType;

    private final boolean positiveResponseRequested;

    private final byte[] primaryField;

    private final byte[] secondaryField;

    UserIdentityRQImpl(int userIdentityType, boolean positiveResponseRequested, 
            byte[] primaryField) {
        this.userIdentityType = userIdentityType;
        this.positiveResponseRequested = positiveResponseRequested;
        this.primaryField = (byte[]) primaryField.clone();
        this.secondaryField = EMPTY;
    }
    
    UserIdentityRQImpl(boolean positiveResponseRequested, String username,
            String passcode) {
        this.userIdentityType = passcode == null ? USERNAME : USERNAME_PASSCODE;
        this.positiveResponseRequested = positiveResponseRequested;
        this.primaryField = toBytes(username);
        this.secondaryField = passcode == null ? EMPTY : toBytes(passcode);
    }
    
    UserIdentityRQImpl(DataInputStream din, int len) throws IOException {
        this.userIdentityType = din.readUnsignedByte();
        this.positiveResponseRequested = din.readBoolean();
        int primaryFieldLen = din.readUnsignedShort();
        if (primaryFieldLen + 6 > len) {
            throw new PDUException( "User Identity sub-item length: "
                    + len + " mismatch primary-field-length:" + primaryFieldLen,
                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                               AAbort.INVALID_PDU_PARAMETER_VALUE));
        }         
        this.primaryField = new byte[primaryFieldLen];
        din.read(primaryField);
        int secondaryFieldLen = din.readUnsignedShort();
        if (secondaryFieldLen + primaryFieldLen + 6 != len) {
            throw new PDUException( "User Identity sub-item length: "
                    + len + " mismatch secondary-field-length:" + secondaryFieldLen,
                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                               AAbort.INVALID_PDU_PARAMETER_VALUE));
        }         
        this.secondaryField = new byte[secondaryFieldLen];
        din.read(secondaryField);
    }

    void writeTo(DataOutputStream dout) throws IOException {
        dout.write(0x58);
        dout.write(0);
        dout.writeShort(length());
        dout.write(userIdentityType);
        dout.writeBoolean(positiveResponseRequested);
        dout.writeShort(primaryField.length);
        dout.write(primaryField);
        dout.writeShort(secondaryField.length);
        dout.write(secondaryField);
    }
    
    public final int getUserIdentityType() {
        return userIdentityType;
    }

    public final boolean isPositiveResponseRequested() {
        return positiveResponseRequested;
    }

    public final byte[] getPrimaryField() {
        return (byte[]) primaryField.clone();
    }

    public final byte[] getSecondaryField() {
        return (byte[]) secondaryField.clone();
    }

    public String getUsername() {
        return toString(primaryField);
    }

    public String getPasscode() {
        return toString(secondaryField);
    }

    private static String toString(byte[] b) {
        try {
            return new String(b, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] toBytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public int length() {
        return 6 + primaryField.length + secondaryField.length;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append("UserIdentity[type = ").append(userIdentityType);
        if (userIdentityType == USERNAME
                || userIdentityType == USERNAME_PASSCODE) {
            sb.append(", username = ").append(getUsername());
            if (userIdentityType == USERNAME_PASSCODE) {
                sb.append(", passcode = ");
                for (int i = secondaryField.length; --i >= 0;)
                    sb.append('*');
            }
        } else {
            sb.append(", primaryField(").append(primaryField.length);
            sb.append("), secondaryField(").append(secondaryField.length);
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }
}
