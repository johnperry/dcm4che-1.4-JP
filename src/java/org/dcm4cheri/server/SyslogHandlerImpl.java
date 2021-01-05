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

import java.net.DatagramPacket;

import org.apache.log4j.Logger;
import org.dcm4che.server.SyslogService;
import org.dcm4che.server.UDPServer;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 5677 $ $Date: 2008-01-14 20:04:17 +0100 (Mo, 14 Jän 2008) $
 *   
 */
public class SyslogHandlerImpl implements UDPServer.Handler
{
    private static Logger log = Logger.getLogger(SyslogHandlerImpl.class);
    
    private final SyslogService service;
    
    public SyslogHandlerImpl(SyslogService service) {
        this.service = service;
    }
    
    public final void handle(DatagramPacket datagram)
    {
        byte[] buff = datagram.getData();
        try {
            SyslogMsg msg = new SyslogMsg(buff,datagram.getLength());
            service.process(msg.getTimestamp(), msg.getHost(), msg.getContent());
        }
        catch (SyslogMsg.InvalidSyslogMsgException e) {
            log.error(e);
        }
    }

}
