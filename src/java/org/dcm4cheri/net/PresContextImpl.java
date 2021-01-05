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
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.UIDDictionary;

import java.io.*;
import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class PresContextImpl implements PresContext {
    private final int type;
    private final int pcid;
    private final int result;
    private final String asuid;
    private final List tsuids;

    PresContextImpl(int type, int pcid, int result, String asuid,
            String[] tsuids) {
        if ((pcid | 1) == 0 || (pcid & ~0xff) != 0) {
            throw new IllegalArgumentException("pcid=" + pcid);
        }
	if (tsuids.length == 0) {
	    throw new IllegalArgumentException("Missing TransferSyntax");
	}
        this.type = type;
        this.pcid = pcid;
        this.result = result;
        this.asuid = asuid;
        this.tsuids = new ArrayList(Arrays.asList(tsuids));
    }
    
    PresContextImpl(int type, DataInputStream din, int len)
            throws IOException, PDUException {
        this.type = type;
        this.pcid = din.readUnsignedByte();
        din.readUnsignedByte();
        this.result = din.readUnsignedByte();
        din.readUnsignedByte();
        int remain = len - 4;
        String asuid = null;
        this.tsuids = new LinkedList();
        while (remain > 0) {
            int uidtype = din.readUnsignedByte();
            din.readUnsignedByte();
            int uidlen = din.readUnsignedShort();
            switch (uidtype) {
                case 0x30:
                    if (type == 0x21 || asuid != null) {
                        throw new PDUException(
                                "Unexpected Abstract Syntax sub-item in"
                                + " Presentation Context",
                                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                            AAbort.UNEXPECTED_PDU_PARAMETER));
                    }
                    asuid = AAssociateRQACImpl.readASCII(din, uidlen);
                    break;
                case 0x40:
                    if (type == 0x21 && !tsuids.isEmpty()) {
                        throw new PDUException(
                                "Unexpected Transfer Syntax sub-item in"
                                + " Presentation Context",
                                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                            AAbort.UNEXPECTED_PDU_PARAMETER));
                    }
                    tsuids.add(AAssociateRQACImpl.readASCII(din, uidlen));
                    break;
                default:
                    throw new PDUException(
                            "unrecognized item type "
                                    + Integer.toHexString(uidtype) + 'H',
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNRECOGNIZED_PDU_PARAMETER));
            }
            remain -= 4 + uidlen;
        }
        this.asuid = asuid;
        if (remain < 0) {
            throw new PDUException("Presentation item length: " + len
                + " mismatch length of sub-items",
                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                               AAbort.INVALID_PDU_PARAMETER_VALUE));
        }
    }
    
    void writeTo(DataOutputStream dout) throws IOException {
        dout.write(type);
        dout.write(0);
        dout.writeShort(length());
        dout.write(pcid);
        dout.write(0);
        dout.write(result);
        dout.write(0);
        if (asuid != null) {
            dout.write(0x30);
            dout.write(0);
            dout.writeShort(asuid.length());
            dout.writeBytes(asuid);
        }
        for (Iterator it = tsuids.iterator(); it.hasNext();) {
            String tsuid = (String)it.next();
            dout.write(0x40);
            dout.write(0);
            dout.writeShort(tsuid.length());
            dout.writeBytes(tsuid);
        }            
    }
    
    final int length() {
        int retval = 4;
        if (asuid != null) {
            retval += 4 + asuid.length();
        }
        for (Iterator it = tsuids.iterator(); it.hasNext();) {
            retval += 4 + ((String)it.next()).length();
        }
        return retval;
    }

    final int type() {
        return type;
    }    

    public final int pcid() {
        return pcid;
    }    
    
    public final int result() {
        return result;
    }    
    
    public final String getAbstractSyntaxUID() {
        return asuid;
    }    
    
    public final List getTransferSyntaxUIDs() {
        return Collections.unmodifiableList(tsuids);
    }    

    public final String getTransferSyntaxUID() {
        return tsuids.isEmpty() ? null : (String)tsuids.get(0);
    }

    public String toString() {
	return toStringBuffer(new StringBuffer()).toString();
    }

    private StringBuffer toStringBuffer(StringBuffer sb) {
	sb.append("PresContext[pcid=").append(pcid);
	if (type == 0x20) {
	    sb.append(", as=").append(AAssociateRQACImpl.DICT.lookup(asuid));
	} else {
	    sb.append(", result=").append(resultAsString());
	}
	Iterator it = tsuids.iterator();
	sb.append(", ts=").append(AAssociateRQACImpl.DICT.lookup((String)it.next()));
	while (it.hasNext()) {
	    sb.append(", ").append(AAssociateRQACImpl.DICT.lookup((String)it.next()));
	}
	return sb.append("]");
    }

    public String resultAsString() {
        switch (result()) {
            case ACCEPTANCE:
                return "0 - acceptance";
            case USER_REJECTION:
                return "1 - user-rejection";
            case NO_REASON_GIVEN:
                return "2 - no-reason-given";
            case ABSTRACT_SYNTAX_NOT_SUPPORTED:
                return "3 - abstract-syntax-not-supported";
            case TRANSFER_SYNTAXES_NOT_SUPPORTED:
                return "4 - transfer-syntaxes-not-supported";
            default:
                return String.valueOf(result());
        }
    }
}
