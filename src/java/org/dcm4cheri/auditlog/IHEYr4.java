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

package org.dcm4cheri.auditlog;

import java.util.Date;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.MediaDescription;
import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.ISO8601DateFormat;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      August 27, 2002
 * @version    $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
class IHEYr4 {

    interface Message {

        void writeTo(StringBuffer sb);
    }

    // Constants -----------------------------------------------------
    // Variables -----------------------------------------------------
    private static String localHostName;

    private Message msg;

    private String host;

    private long millis;

    // Constructors --------------------------------------------------
    private IHEYr4(Message msg, String host, long millis) {
        this.msg = msg;
        this.host = host;
        this.millis = millis;
    }

    public static IHEYr4 newInstancesStored(RemoteNode rnode,
            InstancesAction action, String host, long millis) {
        return new IHEYr4(new RnodeWithInstanceActionDescription(
                "InstancesStored", rnode, action, "RemoteNode"), host, millis);
    }

    public static IHEYr4 newBeginStoringInstances(RemoteNode rnode,
            InstancesAction action, String host, long millis) {
        return new IHEYr4(new RnodeWithInstanceActionDescription(
                "BeginStoringInstances", rnode, action, "Rnode"), host, millis);
    }

    public static IHEYr4 newInstancesSent(RemoteNode rnode,
            InstancesAction action, String host, long millis) {
        return new IHEYr4(new RnodeWithInstanceActionDescription(
                "InstancesSent", rnode, action, "RNode"), host, millis);
    }

    public static IHEYr4 newActorStartStop(String actorName,
            String applicationAction, User user, String host, long millis) {
        return new IHEYr4(
                new ActorStartStop(actorName, applicationAction, user), host,
                millis);
    }

    public static IHEYr4 newActorConfig(String description, User user,
            String configType, String host, long millis) {
        return new IHEYr4(new ActorConfig(description, user, configType), host,
                millis);
    }

    public static IHEYr4 newStudyDeleted(InstancesAction action, String desc,
            String host, long millis) {
        return new IHEYr4(new StudyDeleted(action, desc), host, millis);
    }

    public static IHEYr4 newPatientRecord(String action, Patient patient,
            User user, String desc, String host, long millis) {
        return new IHEYr4(new PatientRecord(action, patient, user, desc),
                host, millis);

    }

    public static IHEYr4 newProcedureRecord(String action,
            String placerOrderNumber, String fillerOrderNumber, String suid,
            String accessionNumber, Patient patient, User user, String desc,
            String host, long millis) {
        return new IHEYr4(new ProcedureRecord(action, placerOrderNumber,
                fillerOrderNumber, suid, accessionNumber, patient, user, desc),
                host, millis);

    }

    public static IHEYr4 newDicomQuery(Dataset keys, RemoteNode requestor,
            String cuid, String host, long millis) {
        return new IHEYr4(new DicomQuery(keys, requestor, cuid), host, millis);
    }

    public static IHEYr4 newSecurityAlert(String alertType, User user,
            String description, String host, long millis) {
        return new IHEYr4(new SecurityAlert(alertType, user, description),
                host, millis);
    }

    public static IHEYr4 newUserAuthenticated(String localUserName,
            String action, String host, long millis) {
        return new IHEYr4(new UserAuthenticated(localUserName, action), host,
                millis);
    }

    public static IHEYr4 newExport(MediaDescription media, User user,
            String host, long millis) {
        return new IHEYr4(new Export(media, user), host, millis);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(512);
        sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><IHEYr4>");
        msg.writeTo(sb);
        sb.append("<Host>").append(host).append("</Host><TimeStamp>");
        sb.append(new ISO8601DateFormat().format(new Date(millis)));
        sb.append("</TimeStamp></IHEYr4>");
        return sb.toString();
    }
}

