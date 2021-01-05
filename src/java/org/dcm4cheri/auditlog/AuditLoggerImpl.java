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

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.MediaDescription;
import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.HostNameUtils;
import org.dcm4che.util.SyslogWriter;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      August 22, 2002
 * @version    $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
class AuditLoggerImpl implements AuditLogger {

    // Constants -----------------------------------------------------
    private final Logger log;

    // Variables -----------------------------------------------------
    private final SyslogWriter writer = new SyslogWriter();

    private boolean actorStartStop = true;

    private boolean instancesStored = true;

    private boolean beginStoringInstances = true;

    private boolean instancesSent = true;

    private boolean dicomQuery = true;

    private boolean securityAlert = true;

    private boolean userAuthenticated = true;

    private boolean actorConfig = true;

    private boolean logExport = true;

    private boolean logStudyDeleted = true;

    private boolean logPatientRecord = true;

    private boolean logProcedureRecord = true;
    
    private boolean strictIHEYr4 = false;

    // Constructors --------------------------------------------------
    AuditLoggerImpl(Logger log) {
        this.log = log;
    }

    // Methods -------------------------------------------------------
    public void setSyslogHost(String syslogHost) throws UnknownHostException {
        writer.setSyslogHost(syslogHost);
    }

    public String getSyslogHost() {
        return writer.getSyslogHost();
    }

    public void setSyslogPort(int syslogPort) {
        writer.setSyslogPort(syslogPort);
    }

    public int getSyslogPort() {
        return writer.getSyslogPort();
    }

    public String getFacility() {
        return writer.getFacilityAsString();
    }

    public void setFacility(String facility) {
        writer.setFacility(facility);
    }

    public boolean isLogActorStartStop() {
        return actorStartStop;
    }

    public void setLogActorStartStop(boolean actorStartStop) {
        this.actorStartStop = actorStartStop;
    }

    public boolean isLogInstancesStored() {
        return instancesStored;
    }

    public void setLogInstancesStored(boolean instancesStored) {
        this.instancesStored = instancesStored;
    }

    public boolean isLogBeginStoringInstances() {
        return beginStoringInstances;
    }

    public void setLogBeginStoringInstances(boolean beginStoringInstances) {
        this.beginStoringInstances = beginStoringInstances;
    }

    public boolean isLogInstancesSent() {
        return instancesSent;
    }

    public void setLogInstancesSent(boolean instancesSent) {
        this.instancesSent = instancesSent;
    }

    public boolean isLogDicomQuery() {
        return dicomQuery;
    }

    public void setLogDicomQuery(boolean dicomQuery) {
        this.dicomQuery = dicomQuery;
    }

    public boolean isLogSecurityAlert() {
        return securityAlert;
    }

    public void setLogSecurityAlert(boolean securityAlert) {
        this.securityAlert = securityAlert;
    }

    public boolean isLogUserAuthenticated() {
        return userAuthenticated;
    }

    public void setLogUserAuthenticated(boolean userAuthenticated) {
        this.userAuthenticated = userAuthenticated;
    }

    public boolean isLogActorConfig() {
        return actorConfig;
    }

    public void setLogActorConfig(boolean actorConfig) {
        this.actorConfig = actorConfig;
    }

    public boolean isLogExport() {
        return logExport;
    }

    public void setLogExport(boolean logExport) {
        this.logExport = logExport;
    }

    public final boolean isLogPatientRecord() {
        return logPatientRecord;
    }

    public final void setLogPatientRecord(boolean logPatientRecord) {
        this.logPatientRecord = logPatientRecord;
    }

    public final boolean isLogProcedureRecord() {
        return logProcedureRecord;
    }

    public final void setLogProcedureRecord(boolean logProcedureRecord) {
        this.logProcedureRecord = logProcedureRecord;
    }

    public final boolean isLogStudyDeleted() {
        return logStudyDeleted;
    }

    public final void setLogStudyDeleted(boolean logStudyDeleted) {
        this.logStudyDeleted = logStudyDeleted;
    }

    public final boolean isStrictIHEYr4() {
        return strictIHEYr4;
    }
    
    public final void setStrictIHEYr4(boolean strictIHEYr4) {
        this.strictIHEYr4 = strictIHEYr4;
    }
    
    public void logActorStartStop(String actorName, String action, User user) {
        if (!actorStartStop) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newActorStartStop(actorName,
                            action,
                            user,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logInstancesStored(RemoteNode rnode,
            InstancesAction instanceActionDescription) {
        if (!instancesStored) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newInstancesStored(rnode,
                            instanceActionDescription,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logBeginStoringInstances(RemoteNode rnode,
            InstancesAction instanceActionDescription) {
        if (!beginStoringInstances) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newBeginStoringInstances(rnode,
                            instanceActionDescription,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logInstancesSent(RemoteNode rnode,
            InstancesAction instanceActionDescription) {
        if (!instancesSent) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4.newInstancesSent(rnode,
                    instanceActionDescription,
                    HostNameUtils.getLocalHostName(),
                    millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logDicomQuery(Dataset keys, RemoteNode requestor, String cuid) {
        if (!dicomQuery) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4.newDicomQuery(keys,
                    requestor,
                    cuid,
                    HostNameUtils.getLocalHostName(),
                    millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logSecurityAlert(String alertType, User user, String description) {
        if (!securityAlert) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newSecurityAlert(alertType,
                            user,
                            description,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logUserAuthenticated(String localUserName, String action) {
        if (!userAuthenticated) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newUserAuthenticated(localUserName,
                            action,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logActorConfig(String description, User user, String configType) {
        if (!actorConfig) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newActorConfig(description,
                            user,
                            configType,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logExport(MediaDescription media, User user) {
        if (!logExport) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4.newExport(media,
                    user,
                    HostNameUtils.getLocalHostName(),
                    millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logPatientRecord(String action, Patient patient, User user,
            String desc) {
        if (!logPatientRecord) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4.newPatientRecord(action,
                    patient,
                    user,
                    strictIHEYr4 ? null : desc,
                    HostNameUtils.getLocalHostName(),
                    millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logProcedureRecord(String action, Patient patient,
            String placerOrderNumber, String fillerOrderNumber, String suid,
            String accessionNumber, User user, String desc) {
        if (!logProcedureRecord) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newProcedureRecord(action,
                            placerOrderNumber,
                            fillerOrderNumber,
                            suid,
                            accessionNumber,
                            patient,
                            user,
                            strictIHEYr4 ? null : desc,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logStudyDeleted(InstancesAction action, String desc) {
        if (!logStudyDeleted) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4.newStudyDeleted(action,
                    strictIHEYr4 ? null : desc,
                    HostNameUtils.getLocalHostName(),
                    millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }
}

