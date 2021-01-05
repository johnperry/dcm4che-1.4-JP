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

package org.dcm4che.auditlog;

import java.net.UnknownHostException;

import org.dcm4che.data.Dataset;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      September 1, 2002
 * @version    $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
public interface AuditLogger {

    public final static String START = "Start";

    public final static String STOP = "Stop";

    public final static String SECURITY = "Security";

    public final static String NETWORKING = "Networking";

    public void setSyslogHost(String syslogHost) throws UnknownHostException;

    public String getSyslogHost();

    public void setSyslogPort(int syslogPort);

    public int getSyslogPort();

    public String getFacility();

    public void setFacility(String facility);

    public boolean isLogActorStartStop();

    public void setLogActorStartStop(boolean enable);

    public boolean isLogInstancesStored();

    public void setLogInstancesStored(boolean enable);

    public boolean isLogBeginStoringInstances();

    public void setLogBeginStoringInstances(boolean enable);

    public boolean isLogInstancesSent();

    public void setLogInstancesSent(boolean enable);

    public boolean isLogDicomQuery();

    public void setLogDicomQuery(boolean enable);

    public boolean isLogSecurityAlert();

    public void setLogSecurityAlert(boolean enable);

    public boolean isLogUserAuthenticated();

    public void setLogUserAuthenticated(boolean enable);

    public boolean isLogActorConfig();

    public void setLogActorConfig(boolean enable);

    public boolean isLogExport();

    public void setLogExport(boolean enable);

    public boolean isLogPatientRecord();

    public void setLogPatientRecord(boolean enable);

    public boolean isLogProcedureRecord();

    public void setLogProcedureRecord(boolean enable);

    public boolean isLogStudyDeleted();

    public void setLogStudyDeleted(boolean enable);

    public void logActorStartStop(String actorName, String action, User user);

    public void logInstancesStored(RemoteNode rNode, InstancesAction action);

    public void logBeginStoringInstances(RemoteNode rNode,
            InstancesAction action);

    public void logInstancesSent(RemoteNode rNode, InstancesAction action);

    public void logDicomQuery(Dataset keys, RemoteNode requestor, String cuid);

    public void logSecurityAlert(String alertType, User user,
            String description);

    public void logUserAuthenticated(String localUserName, String action);

    public void logActorConfig(String description, User user, String configType);

    public void logExport(MediaDescription media, User user);

    public void logPatientRecord(String action, Patient patient, User user, 
            String description);

    public void logProcedureRecord(String action, Patient patient,
            String placerOrderNumber, String fillerOrderNumber, String suid,
            String accessionNumber, User user, String description);

    public void logStudyDeleted(InstancesAction action, String description);

    public boolean isStrictIHEYr4();
    
    public void setStrictIHEYr4(boolean strictIHEYr4);
}