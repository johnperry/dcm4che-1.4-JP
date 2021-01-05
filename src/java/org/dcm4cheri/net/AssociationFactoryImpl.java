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

import org.dcm4che.net.AAbort;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AAssociateRJ;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.Association;
import org.dcm4che.net.AReleaseRQ;
import org.dcm4che.net.AReleaseRP;
import org.dcm4che.net.AsyncOpsWindow;
import org.dcm4che.net.CommonExtNegotiation;
import org.dcm4che.net.DataSource;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.ExtNegotiation;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.PDataTF;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PDUException;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.RoleSelection;
import org.dcm4che.net.UserIdentityAC;
import org.dcm4che.net.UserIdentityRQ;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;

import org.dcm4cheri.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class AssociationFactoryImpl extends AssociationFactory {

    public AssociationFactoryImpl() {
    }

    public AAssociateRQ newAAssociateRQ() {
        return new AAssociateRQImpl();
    }

    public AAssociateAC newAAssociateAC() {
        return new AAssociateACImpl();
    }

    public AAssociateRJ newAAssociateRJ(int result, int source, int reason) {
        return new AAssociateRJImpl(result, source, reason);
    }

    public PDataTF newPDataTF(int maxLength) {
        return new PDataTFImpl(maxLength);
    }

    public AReleaseRQ newAReleaseRQ() {
        return AReleaseRQImpl.getInstance();
    }

    public AReleaseRP newAReleaseRP() {
        return AReleaseRPImpl.getInstance();
    }

    public AAbort newAAbort(int source, int reason) {
        return new AAbortImpl(source, reason);
    }

    public PresContext newPresContext(int pcid, String asuid, String[] tsuids) {
        return new PresContextImpl(0x020, pcid, 0, StringUtils.checkUID(asuid),
                StringUtils.checkUIDs(tsuids));
    }

    public PresContext newPresContext(int pcid, String asuid) {
        return new PresContextImpl(0x020, pcid, 0, StringUtils.checkUID(asuid),
                new String[] { UIDs.ImplicitVRLittleEndian });
    }

    public PresContext newPresContext(int pcid, String asuid, String tsuid) {
        return new PresContextImpl(0x020, pcid, 0, StringUtils.checkUID(asuid),
                new String[] { StringUtils.checkUID(tsuid) });
    }

    public PresContext newPresContext(int pcid, int result, String tsuid) {
        return new PresContextImpl(0x021, pcid, result, null,
                new String[] { StringUtils.checkUID(tsuid) });
    }

    public PresContext newPresContext(int pcid, int result, String asuid,
            String[] tsuids) {
        return new PresContextImpl(0x021, pcid, result, StringUtils
                .checkUID(asuid), StringUtils.checkUIDs(tsuids));
    }

    public AsyncOpsWindow newAsyncOpsWindow(int maxOpsInvoked,
            int maxOpsPerfomed) {
        return new AsyncOpsWindowImpl(maxOpsInvoked, maxOpsPerfomed);
    }

    public RoleSelection newRoleSelection(String uid, boolean scu, boolean scp) {
        return new RoleSelectionImpl(uid, scu, scp);
    }

    public ExtNegotiation newExtNegotiation(String uid, byte[] info) {
        return new ExtNegotiationImpl(uid, info);
    }

    public CommonExtNegotiation newCommonExtNegotiation(String sopCUID,
            String serviceCUID, String[] relGenSopCUIDs) {
        return new CommonExtNegotiationImpl(sopCUID, serviceCUID,
                relGenSopCUIDs);
    }

    public UserIdentityRQ newUserIdentity(boolean positiveResponseRequested,
            String username, String passcode) {
        return new UserIdentityRQImpl(positiveResponseRequested, username,
                passcode);
    }

    public UserIdentityRQ newUserIdentity(int userIdentityType,
            boolean positiveResponseRequested, byte[] primaryField) {
        return new UserIdentityRQImpl(userIdentityType,
                positiveResponseRequested, primaryField);
    }

    public UserIdentityAC newUserIdentity() {
        return new UserIdentityACImpl();
    }

    public UserIdentityAC newUserIdentity(byte[] serverResponse) {
        return new UserIdentityACImpl(serverResponse);
    }

    public PDU readFrom(InputStream in, byte[] buf) throws IOException {
        UnparsedPDUImpl raw = new UnparsedPDUImpl(in, buf);
        switch (raw.type()) {
        case 1:
            return AAssociateRQImpl.parse(raw);
        case 2:
            return AAssociateACImpl.parse(raw);
        case 3:
            return AAssociateRJImpl.parse(raw);
        case 4:
            return PDataTFImpl.parse(raw);
        case 5:
            return AReleaseRQImpl.parse(raw);
        case 6:
            return AReleaseRPImpl.parse(raw);
        case 7:
            return AAbortImpl.parse(raw);
        default:
            throw new PDUException("Unrecognized " + raw, new AAbortImpl(
                    AAbort.SERVICE_PROVIDER, AAbort.UNRECOGNIZED_PDU));
        }
    }

    public Association newRequestor(Socket s) throws IOException {
        return new AssociationImpl(s, true);
    }

    public Association newAcceptor(Socket s) throws IOException {
        return new AssociationImpl(s, false);
    }

    public ActiveAssociation newActiveAssociation(Association assoc,
            DcmServiceRegistry services) {
        return new ActiveAssociationImpl(assoc, services);
    }

    public Dimse newDimse(int pcid, Command cmd) {
        return new DimseImpl(pcid, cmd, null, null);
    }

    public Dimse newDimse(int pcid, Command cmd, Dataset ds) {
        return new DimseImpl(pcid, cmd, ds, null);
    }

    public Dimse newDimse(int pcid, Command cmd, DataSource src) {
        return new DimseImpl(pcid, cmd, null, src);
    }

    public AcceptorPolicy newAcceptorPolicy() {
        return new AcceptorPolicyImpl();
    }

    public DcmServiceRegistry newDcmServiceRegistry() {
        return new DcmServiceRegistryImpl();
    }

}
