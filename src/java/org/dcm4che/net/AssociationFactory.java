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

package org.dcm4che.net;

import org.dcm4che.Implementation;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 6750 $ $Date: 2008-08-06 07:59:45 +0200 (Mi, 06 Aug 2008) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public abstract class AssociationFactory {
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------
    private static AssociationFactory instance = (AssociationFactory)
            Implementation.findFactory("dcm4che.net.AssociationFactory");

    public static AssociationFactory getInstance() {
        return instance;
    }

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------
    public abstract AAssociateRQ newAAssociateRQ();

    public abstract AAssociateAC newAAssociateAC();

    public abstract AAssociateRJ newAAssociateRJ(int result, int source,
            int reason);

    public abstract PDataTF newPDataTF(int maxLength);

    public abstract AReleaseRQ newAReleaseRQ();

    public abstract AReleaseRP newAReleaseRP();

    public abstract AAbort newAAbort(int source, int reason);

    public abstract PresContext newPresContext(int pcid, String asuid);

    public abstract PresContext newPresContext(int pcid, String asuid,
            String[] tsuids);

    public abstract PresContext newPresContext(int pcid, String asuid,
            String tsuid);
    
    public abstract PresContext newPresContext(int pcid, int result,
            String tsuid);
    
    public abstract PresContext newPresContext(int pcid, int result, String asuid,
            String[] tsuids);

    public abstract AsyncOpsWindow newAsyncOpsWindow(int maxOpsInvoked,
            int maxOpsPerfomed);

    public abstract RoleSelection newRoleSelection(String uid, boolean scu,
            boolean scp);

    public abstract ExtNegotiation newExtNegotiation(String uid, byte[] info);
    
    public abstract CommonExtNegotiation newCommonExtNegotiation(String sopCUID,
            String serviceCUID, String[] relGenSopCUIDs);

    public abstract UserIdentityRQ newUserIdentity(
            boolean positiveResponseRequested, String username, String passcode);

    public abstract UserIdentityRQ newUserIdentity(int userIdentityType,
            boolean positiveResponseRequested, byte[] primaryField);

    public abstract UserIdentityAC newUserIdentity();
    
    public abstract UserIdentityAC newUserIdentity(byte[] serverResponse);

    public abstract PDU readFrom(InputStream in, byte[] buf) throws IOException;

    public abstract Association newRequestor(Socket s) throws IOException;

    public abstract Association newAcceptor(Socket s) throws IOException;

    public abstract ActiveAssociation newActiveAssociation(Association assoc,
            DcmServiceRegistry services);

    public abstract Dimse newDimse(int pcid, Command cmd);

    public abstract Dimse newDimse(int pcid, Command cmd, Dataset ds);

    public abstract Dimse newDimse(int pcid, Command cmd, DataSource src);

    public abstract AcceptorPolicy newAcceptorPolicy();

    public abstract DcmServiceRegistry newDcmServiceRegistry();

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
