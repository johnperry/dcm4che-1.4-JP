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

package org.dcm4cheri.server;

import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Factory;
import org.dcm4che.hl7.HL7Service;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.hl7.MSHSegment;
import org.dcm4che.server.HL7Handler;
import org.dcm4che.util.MLLPInputStream;
import org.dcm4che.util.MLLPOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 * @since August 11, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class HL7HandlerImpl implements HL7Handler {
    
    // Constants -----------------------------------------------------
    static final Logger log = Logger.getLogger(HL7HandlerImpl.class);
    
    // Variables -----------------------------------------------------
    private static final HL7Factory hl7Fact = HL7Factory.getInstance();
    
    private int soTimeout = 0;
    
    private HashSet receivingApps = null;
    
    private HashSet sendingApps = null;
    
    private HashMap hl7Services = new HashMap();
    
    // Constructors --------------------------------------------------
    
    // Methods -------------------------------------------------------
    
    public int getSoTimeout() {
        return soTimeout;
    }
        
    public void setSoTimeout(int timeout) {
        this.soTimeout = timeout;
    }
    
    public boolean addReceivingApp(String app) {
        if (receivingApps == null)
            receivingApps = new HashSet();
        
        return receivingApps.add(app);
    }
    
    public boolean addSendingApp(String app) {
        if (sendingApps == null)
            sendingApps = new HashSet();
        
        return sendingApps.add(app);
    }
    
    public String[] getReceivingApps() {
        return receivingApps != null
            ? (String[])receivingApps.toArray(new String[receivingApps.size()])
            : null;
    }
    
    public String[] getSendingApps() {
        return sendingApps != null
            ? (String[])sendingApps.toArray(new String[sendingApps.size()])
            : null;
    }
    
    public boolean removeReceivingApp(String app) {
        return receivingApps != null && receivingApps.remove(app);
    }
    
    public boolean removeSendingApp(String app) {
        return sendingApps != null && sendingApps.remove(app);
    }
    
    public void setReceivingApps(String[] apps) {
        receivingApps = apps != null
            ? new HashSet(Arrays.asList(apps))
            : null;
    }
    
    public void setSendingApps(String[] apps) {
        sendingApps = apps != null
            ? new HashSet(Arrays.asList(apps))
            : null;
    }
    
    public HL7Service putService(String msgTypeEvent,
            HL7Service service) {
        if (service != null) {
            return (HL7Service) hl7Services.put(msgTypeEvent, service);
        } else {
            return (HL7Service) hl7Services.remove(msgTypeEvent);
        }
    }

    private String toKey(String msgType, String trEvent) {
        StringBuffer sb =
            new StringBuffer(msgType.length() + trEvent.length() + 1);
        sb.append(msgType).append('^').append(trEvent);
        return sb.toString();
    }
    
    // Server.Handler -------------------------------------------
    public void handle(Socket s) throws IOException {
        s.setSoTimeout(soTimeout);
        MLLPInputStream in = new MLLPInputStream(
            new BufferedInputStream(s.getInputStream()));
        MLLPOutputStream out = new MLLPOutputStream(
            new BufferedOutputStream(s.getOutputStream()));
        try {
            byte[] data;
            while ((data = in.readMessage()) != null) {
                HL7Message msg = hl7Fact.parse(data);
                log.info("RCV: " + msg);
                byte[] res = execute(msg.header(), data);
                log.info("SND: " + hl7Fact.parse(res));
                out.writeMessage(res);
                out.flush();
            }
        } catch (HL7Exception e) {
            log.error("Could not understand: ", e);
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            try { out.close(); } catch (IOException ignore) {}
            try { s.close(); } catch (IOException ignore) {}
        }
    }
    
    public byte[] execute(MSHSegment msh, byte[] data) {
        try {
            if (receivingApps != null && !receivingApps.contains(
                    msh.getReceivingApplication())) {
                throw new HL7Exception.AR(
                    "Unrecognized Receiving Application: "
                    + msh.getReceivingApplication());   
            }
            if (sendingApps != null && !sendingApps.contains(
                    msh.getSendingApplication())) {
                throw new HL7Exception.AR(
                    "Unrecognized Sending Application: "
                    + msh.getSendingApplication());   
            }
            HL7Service service = (HL7Service) hl7Services.get(
                toKey(msh.getMessageType(), msh.getTriggerEvent()));
            if (service == null) {
                service = (HL7Service) hl7Services.get(msh.getMessageType());
            }
            if (service == null) {
                throw new HL7Exception.AR(
                    "Unrecognized Message Type^TriggerEvent "
                    + toKey(msh.getMessageType(), msh.getTriggerEvent()));   
            }
            return service.execute(data);
        } catch (HL7Exception e) {
            log.warn(e.getMessage(), e);
            return e.makeACK(msh);
        }
    }
    
    public boolean isSockedClosedByHandler() {
        return true;
    }
}
