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
final class AAssociateRJImpl implements AAssociateRJ {

    private final byte[] buf;
    
    static AAssociateRJImpl parse(UnparsedPDUImpl raw) throws PDUException {
        if (raw.length() != 4) {
            throw new PDUException("Illegal A-ASSOCIATE-RJ " + raw,
                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                   AAbort.INVALID_PDU_PARAMETER_VALUE));
        }
        return new AAssociateRJImpl(raw.buffer());
    }

    private AAssociateRJImpl(byte[] buf) {
        this.buf = buf;
    }
    
    AAssociateRJImpl(int result, int source, int reason) {
        this.buf = new byte[]{ 3, 0, 0, 0, 0, 4, 0,
                (byte)result,
                (byte)source,
                (byte)reason
        };
    }

    public final int result() {
        return buf[7] & 0xff;
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
        return sb.append("A-ASSOCIATE-RJ\n\tresult=").append(resultAsString())
                .append("\n\tsource=").append(sourceAsString())
                .append("\n\treason=").append(reasonAsString());
    }    
    
    private String resultAsString() {
        switch (result()) {
            case REJECTED_PERMANENT:
                return "1 - rejected-permanent";
            case REJECTED_TRANSIENT:
                return "2 - rejected-transient";
            default:
                return String.valueOf(result());
        }
    }

    private String sourceAsString() {
        switch (source()) {
            case SERVICE_USER:
                return "1 - service-user";
            case SERVICE_PROVIDER_ACSE:
                return "2 - service-provider (ACSE)";
            case SERVICE_PROVIDER_PRES:
                return "3 - service-provider (Presentation)";
            default:
                return String.valueOf(source());
        }
    }
    
    private String reasonAsString() {
        switch (source()) {
            case SERVICE_USER:
                switch (reason()) {
                    case NO_REASON_GIVEN:
                        return "1 - no-reason-given";
                    case APPLICATION_CONTEXT_NAME_NOT_SUPPORTED:
                        return "2 - application-context-name-not-supported";
                    case CALLING_AE_TITLE_NOT_RECOGNIZED:
                        return "3 - calling-AE-title-not-recognized";
                    case CALLED_AE_TITLE_NOT_RECOGNIZED:
                        return "7 - called-AE-title-not-recognized";
                }
            case SERVICE_PROVIDER_ACSE:
                switch (reason()) {
                    case NO_REASON_GIVEN:
                        return "1 - no-reason-given";
                    case PROTOCOL_VERSION_NOT_SUPPORTED:
                        return "2 - protocol-version-not-supported";
                }
            case SERVICE_PROVIDER_PRES:
                switch (reason()) {
                    case TEMPORARY_CONGESTION:
                        return "1 - temporary-congestion";
                    case LOCAL_LIMIT_EXCEEDED:
                        return "2 - local-limit-exceeded";
                }
        }
        return String.valueOf(reason());
    }        
}
