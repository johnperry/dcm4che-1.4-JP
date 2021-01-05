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

import org.dcm4che.net.ExtNegotiation;
import org.dcm4cheri.util.StringUtils;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class ExtNegotiationImpl implements ExtNegotiation {

    private final String asuid;
    private final byte[] info;

    /** Creates a new instance of ExtNegotiationImpl */
    ExtNegotiationImpl(String asuid, byte[] info) {
        this.asuid = asuid;
        this.info = (byte[])info.clone();
    }

    ExtNegotiationImpl(DataInputStream din, int len) throws IOException {
        int uidLen = din.readUnsignedShort();
        this.asuid = AAssociateRQACImpl.readASCII(din, uidLen);
        this.info = new byte[len - uidLen - 2];
        din.readFully(info);
    }    

    public final String getSOPClassUID() {
        return asuid;
    }    

    public final byte[] info() {
        return (byte[])info.clone();
    }    

    final int length() {
        return 2 + asuid.length() + info.length;
    }

    void writeTo(DataOutputStream dout) throws IOException {
        dout.write(0x56);
        dout.write(0);
        dout.writeShort(length());
        dout.writeShort(asuid.length());
        dout.writeBytes(asuid);
        dout.write(info);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append("ExtNegotiation[sop=")
                .append(AAssociateRQACImpl.DICT.lookup(asuid))
                .append(", info=");
        StringUtils.promptBytes(sb, info, 0, info.length, Integer.MAX_VALUE);
        sb.append("]");
        return sb.toString();
    }
  }
