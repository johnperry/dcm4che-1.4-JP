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

import java.util.List;

/**
 * Defines association acceptance/rejection behavior.
 *
 * @see Association#accept
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 4896 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020518 gunter zeilinger:</b>
 * <ul>
 * <li> Initial import
 * </ul>
 */
public interface AcceptorPolicy
{
   void setMaxPDULength(int maxLength);
   
   int getMaxPDULength();
   
   AsyncOpsWindow getAsyncOpsWindow();
    
   void setAsyncOpsWindow(int maxOpsInvoked, int maxOpsPerformed);
   
   void setImplClassUID(String implClassUID);

   String getImplClassUID();
   
   void setImplVersionName(String implVers);
   
   String getImplVersionName();
   
   String putApplicationContextName(String proposed, String returned);
   
   void setCalledAETFilter(AETFilter filter);
   
   AETFilter getCalledAETFilter();

   void setCallingAETFilter(AETFilter filter);

   AETFilter getCallingAETFilter();
   
   boolean addCalledAET(String aet);

   boolean removeCalledAET(String aet);

   void setCalledAETs(String[] aets);
   
   String[] getCalledAETs();
   
   boolean addCallingAET(String aet);

   boolean removeCallingAET(String aet);
   
   void setCallingAETs(String[] aets);
   
   String[] getCallingAETs();
   
   AcceptorPolicy putPolicyForCalledAET(String aet, AcceptorPolicy policy);
   
   AcceptorPolicy getPolicyForCalledAET(String aet);
   
   AcceptorPolicy putPolicyForCallingAET(String aet, AcceptorPolicy policy);

   AcceptorPolicy getPolicyForCallingAET(String aet);

   PresContext putPresContext(String asuid, String[] tsuids);

   PresContext getPresContext(String asuid);

   List listPresContext();
   
   RoleSelection putRoleSelection(String uid, boolean scu, boolean scp);
   
   RoleSelection getRoleSelection(String uid);

   RoleSelection removeRoleSelection(String uid);

   ExtNegotiator putExtNegPolicy(String uid, ExtNegotiator en);
   
   ExtNegotiator getExtNegPolicy(String uid);

   void setUserIdentityNegotiator(UserIdentityNegotiator identNegotiator);
   
   UserIdentityNegotiator getUserIdentityNegotiator();
      
   PDU negotiate(Association assoc);
  
}
