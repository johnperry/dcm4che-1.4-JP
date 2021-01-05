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

import org.dcm4che.net.*;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class AAbortImpl implements AAbort {
    
    private final byte[] buf;
    
    static AAbortImpl parse(UnparsedPDUImpl raw) throws PDUException {
        if (raw.length() != 4) {
            throw new PDUException("Illegal A-ABORT " + raw,
                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                   AAbort.INVALID_PDU_PARAMETER_VALUE));
        }
        return new AAbortImpl(raw.buffer());
    }

    private AAbortImpl(byte[] buf) {
        this.buf = buf;
    }
    
    AAbortImpl(int source, int reason) {
        this.buf = new byte[]{ 7, 0, 0, 0, 0, 4, 0, 0,
                (byte)source,
                (byte)reason
        };
    }

    public final int source() {
        return buf[8] & 0xff;
    }
    
    public final int reason() {
        return buf[9] & 0xff;
    }
    
    public void writeTo(OutputStream out) throws IOException {
        out.write(buf);
//        out.flush();
    }

    public String toString(boolean verbose) {
       return toString();
    }

    public String toString() {
        return toStringBuffer(new StringBuffer()).toString();
    }
    
    final StringBuffer toStringBuffer(StringBuffer sb) {
        return sb.append("A-ABORT\n\tsource=").append(sourceAsString())
                .append("\n\treason=").append(reasonAsString());
    }    
    
    private String sourceAsString() {
        switch (source()) {
            case SERVICE_USER:
                return "0 - service-user";
            case SERVICE_PROVIDER:
                return "2 - service-provider";
            default:
                return String.valueOf(source());
        }
    }
    
    private String reasonAsString() {
        switch (reason()) {
            case REASON_NOT_SPECIFIED:
                return "0 - reason-not-specified";
            case UNRECOGNIZED_PDU:
                return "1 - unrecognized-PDU";
            case UNEXPECTED_PDU:
                return "2 - unexpected-PDU";
            case UNRECOGNIZED_PDU_PARAMETER:
                return "4 - unrecognized-PDU parameter";
            case UNEXPECTED_PDU_PARAMETER:
                return "5 - unexpected-PDU parameter";
            case INVALID_PDU_PARAMETER_VALUE:
                return "6 - invalid-PDU-parameter value";
            default:
                return String.valueOf(reason());
        }
    }        
}
