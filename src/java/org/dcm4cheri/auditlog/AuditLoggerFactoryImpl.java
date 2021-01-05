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

import java.net.Socket;

import org.apache.log4j.Logger;
import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.Destination;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.MediaDescription;
import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      August 27, 2002
 * @version    $Revision: 3995 $ $Date: 2006-05-19 14:32:05 +0200 (Fr, 19 Mai 2006) $
 */

public class AuditLoggerFactoryImpl extends AuditLoggerFactory
{

    // Public --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public AuditLogger newAuditLogger()
    {
        return new AuditLoggerImpl(Logger.getLogger(AuditLogger.class));
    }


    /**
     *  Description of the Method
     *
     * @param  log  Description of the Parameter
     * @return      Description of the Return Value
     */
    public AuditLogger newAuditLogger(Logger log)
    {
        return new AuditLoggerImpl(log);
    }


    /**
     *  Description of the Method
     *
     * @param  id    Description of the Parameter
     * @param  name  Description of the Parameter
     * @return       Description of the Return Value
     */
    public Patient newPatient(String id, String name)
    {
        return new PatientImpl(id, name);
    }


    /**
     *  Description of the Method
     *
     * @param  ip     Description of the Parameter
     * @param  hname  Description of the Parameter
     * @param  aet    Description of the Parameter
     * @return        Description of the Return Value
     */
    public RemoteNode newRemoteNode(String ip, String hname, String aet)
    {
        return new RemoteNodeImpl(ip, hname, aet);
    }


    /**
     *  Description of the Method
     *
     * @param  socket  Description of the Parameter
     * @param  aet     Description of the Parameter
     * @return         Description of the Return Value
     */
    public RemoteNode newRemoteNode(Socket socket, String aet)
    {
        return new RemoteNodeImpl(socket, aet, disableHostLookup);
    }


    /**
     *  Description of the Method
     *
     * @param  name  Description of the Parameter
     * @return       Description of the Return Value
     */
    public User newLocalUser(String name)
    {
        return new LocalUserImpl(name);
    }


    /**
     *  Description of the Method
     *
     * @param  rnode  Description of the Parameter
     * @return        Description of the Return Value
     */
    public User newRemoteUser(RemoteNode rnode)
    {
        return new RemoteUserImpl(rnode);
    }


    /**
     *  Description of the Method
     *
     * @param  name  Description of the Parameter
     * @return       Description of the Return Value
     */
    public Destination newLocalPrinter(String name)
    {
        return new LocalPrinterImpl(name);
    }


    /**
     *  Description of the Method
     *
     * @param  rnode  Description of the Parameter
     * @return        Description of the Return Value
     */
    public Destination newRemotePrinter(RemoteNode rnode)
    {
        return new RemotePrinterImpl(rnode);
    }


    /**
     *  Description of the Method
     *
     * @param  action   Description of the Parameter
     * @param  suid     Description of the Parameter
     * @param  patient  Description of the Parameter
     * @return          Description of the Return Value
     */
    public InstancesAction newInstancesAction(String action, String suid,
            Patient patient)
    {
        return new InstancesActionImpl(action, suid, patient);
    }


    /**
     *  Description of the Method
     *
     * @param  patient  Description of the Parameter
     * @return          Description of the Return Value
     */
    public MediaDescription newMediaDescription(Patient patient)
    {
        return new MediaDescriptionImpl(patient);
    }
}
