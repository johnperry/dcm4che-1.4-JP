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

import org.dcm4che.auditlog.RemoteNode;

import java.net.InetAddress;
import java.net.Socket;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3995 $ $Date: 2006-05-19 14:32:05 +0200 (Fr, 19 Mai 2006) $
 * @since August 27, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class RemoteNodeImpl implements RemoteNode {
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    private String ip;
    private String hname;
    private String aet;
    
    // Constructors --------------------------------------------------
    public RemoteNodeImpl(String ip, String hname, String aet) {
        this.ip = ip;
        this.hname = hname;
        this.aet = aet;
    }

    public RemoteNodeImpl(Socket s, String aet, boolean disableHostLookup) {
        InetAddress addr = s.getInetAddress();
        this.ip = addr.getHostAddress();
        if (!disableHostLookup) {
            this.hname = toHname(addr.getHostName());
        }
        this.aet = aet;
    }
        
    public final String getAET() {
        return aet;
    }
    
    public final String getHostName() {
        return hname;
    }
    
    public final String getIP() {
        return ip;
    }

    private final String toHname(String name) {
        if (Character.isDigit(name.charAt(0))) {
            return null;
        }
        int pos = name.indexOf('.');
        return pos == -1 ? name : name.substring(0,pos);
    }
    
    // Methods -------------------------------------------------------
    public void writeTo(StringBuffer sb) {
        sb.append("<IP>").append(ip).append("</IP>");
        if (hname != null) {
            sb.append("<Hname>").append(hname).append("</Hname>");
        }
        if (aet != null) {
            sb.append("<AET><![CDATA[").append(aet).append("]]></AET>");
        }
    }
}
