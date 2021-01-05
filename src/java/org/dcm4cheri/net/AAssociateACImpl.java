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

import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.PDUException;
import org.dcm4che.net.UserIdentityAC;
import org.dcm4che.net.UserIdentityRQ;

import java.util.Iterator;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 4078 $ $Date: 2007-01-25 15:52:19 +0100 (Do, 25 Jän 2007) $
 * @since May, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020728 gunter:</b>
 * <ul>
 * <li> add {@link #countAcceptedPresContext}
 * </ul>
 */
final class AAssociateACImpl extends AAssociateRQACImpl
        implements AAssociateAC {

    static AAssociateACImpl parse(UnparsedPDUImpl raw) throws PDUException {
        return (AAssociateACImpl)new AAssociateACImpl().init(raw);
    }

    AAssociateACImpl() {
    }
 
    public final UserIdentityAC getUserIdentity() {
        return userIdentityAC;
    }

    public final void setUserIdentity(UserIdentityAC userIdentity) {
        this.userIdentityAC = userIdentity;
    }  
    
    public int countAcceptedPresContext() {
       int accepted = 0;
       for (Iterator it = presCtxs.values().iterator(); it.hasNext();) {
          if(((PresContext)it.next()).result() == 0)
             ++accepted;
       }
       return accepted;
    }
        

    protected int type() {
        return 2;
    }
    
    protected int pctype() {
        return 0x21;
    }
    
    protected String typeAsString() {
       return "AAssociateAC";
    }

    protected void append(PresContext pc, StringBuffer sb) {
       sb.append("\n\tpc-").append(pc.pcid())
         .append(":\t").append(pc.resultAsString())
         .append("\n\t\tts=").append(DICT.lookup(pc.getTransferSyntaxUID()));       
    }

    protected void appendPresCtxSummary(StringBuffer sb) {
       int accepted = countAcceptedPresContext();       
       sb.append("\n\tpresCtx:\taccepted=").append(accepted)
         .append(", rejected=").append(presCtxs.size() - accepted);
    }
}
