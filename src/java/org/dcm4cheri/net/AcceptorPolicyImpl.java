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

import org.dcm4che.Implementation;
import org.dcm4che.net.AAssociateRJException;
import org.dcm4che.net.AETFilter;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRJ;
import org.dcm4che.net.Association;
import org.dcm4che.net.PDU;
import org.dcm4che.net.AsyncOpsWindow;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.ExtNegotiation;
import org.dcm4che.net.ExtNegotiator;
import org.dcm4che.net.PDataTF;
import org.dcm4che.net.RoleSelection;
import org.dcm4che.net.UserIdentityAC;
import org.dcm4che.net.UserIdentityNegotiator;
import org.dcm4che.net.UserIdentityRQ;
import org.dcm4che.dict.UIDs;

import org.dcm4cheri.util.StringUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Defines association acceptance/rejection behavior.
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 13973 $ $Date: 2010-09-02 11:53:12 +0200 (Do, 02 Sep 2010) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020518 gunter zeilinger:</b>
 * <ul>
 * <li> Initial import
 * </ul>
 * <p><b>20030425 gunter zeilinger:</b>
 * <ul>
 * <li> Fix Permanent-reject with reason "CallingAE-not-recognized".
 *      Thanx to Jie from INPHACT for sending me the Bug Fix
 * </ul>
 * <p><b>20031202 gunter zeilinger:</b>
 * <ul>
 * <li> Use empty AET HashSet as NULL object (=>no AET check)
 *      to avoid checks for null value.
 * </ul>
 */
class AcceptorPolicyImpl implements AcceptorPolicy {

    // Constants -----------------------------------------------------
    private static final UserIdentityNegotiator DEF_USER_IDENTITY_NEGOTIATOR = 
            new UserIdentityNegotiator(){

                public UserIdentityAC negotiate(Association assoc) {
                	UserIdentityRQ rq =
                			assoc.getAAssociateRQ().getUserIdentity();
                    return rq != null && rq.isPositiveResponseRequested() 
                        ? new UserIdentityACImpl() : null;
                }};
    
    // Attributes ----------------------------------------------------
    private int maxLength = PDataTF.DEF_MAX_PDU_LENGTH;
    
    private AsyncOpsWindow aow = null;
    
    private String implClassUID = Implementation.getClassUID();
    
    private String implVers = Implementation.getVersionName();
    
    private HashMap appCtxMap = new HashMap();
    
    private final HashSet calledAETs = new HashSet();
    
    private AETFilter calledAETFilter = new AETFilter() {

        public boolean accept(String aet) {
            return calledAETs.isEmpty() || calledAETs.contains(aet);
        }
        
    };
    
    private final HashSet callingAETs = new HashSet();

    private AETFilter callingAETFilter = new AETFilter() {

        public boolean accept(String aet) {
            return callingAETs.isEmpty() || callingAETs.contains(aet);
        }
        
    };
    
    private HashMap policyForCalledAET = new HashMap();
    
    private HashMap policyForCallingAET = new HashMap();
    
    private UserIdentityNegotiator userIdentityNegotiator = 
            DEF_USER_IDENTITY_NEGOTIATOR; 
    
    private LinkedHashMap presCtxMap = new LinkedHashMap();
    
    private HashMap roleSelectionMap = new HashMap();
    
    private HashMap extNegotiaionMap = new HashMap();        
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public AcceptorPolicyImpl() {
        putPresContext(UIDs.Verification,
            new String[] { UIDs.ImplicitVRLittleEndian,
                UIDs.ExplicitVRLittleEndian });
    }
    
    // Public --------------------------------------------------------
    
    // AcceptorPolicy implementation ---------------------------------
    public void setMaxPDULength(int maxLength) {
        if (maxLength < 0)
            throw new IllegalArgumentException("maxLength:" + maxLength);
        
        this.maxLength = maxLength;
    }
    
    public int getMaxPDULength() {
        return maxLength;
    }
    
    public AsyncOpsWindow getAsyncOpsWindow() {
        return aow;
    }
    
    public void setAsyncOpsWindow(int maxOpsInvoked, int maxOpsPerformed) {
        if (maxOpsInvoked == 1 && maxOpsPerformed == 1) {
            aow = null;
        } else if (aow == null 
                || aow.getMaxOpsInvoked() != maxOpsInvoked
                || aow.getMaxOpsPerformed() != maxOpsPerformed) {
            aow = new AsyncOpsWindowImpl(maxOpsInvoked, maxOpsPerformed);
        }
    }
    
    public void setImplClassUID(String implClassUID) {
        this.implClassUID = StringUtils.checkUID(implClassUID);
    }
    
    public String getImplClassUID() {
        return implClassUID;
    }
    
    public void setImplVersionName(String implVers) {
        this.implVers = implVers != null
        ? StringUtils.checkAET(implVers) : null;
    }
    
    public String getImplVersionName() {
        return implVers;
    }
    
    public String putApplicationContextName(String proposed, String returned) {
        return (String)appCtxMap.put(StringUtils.checkUID(proposed),
        StringUtils.checkUID(returned));
    }
    
    public boolean addCalledAET(String aet) {
        StringUtils.checkAET(aet);
        return calledAETs.add(aet);
    }
    
    public boolean removeCalledAET(String aet) {
        return calledAETs.remove(aet);
    }
    
    public void setCalledAETs(String[] aets) {
        if (aets == null) {
            calledAETs.clear();
        } else {
            StringUtils.checkAETs(aets); 
            calledAETs.clear();
            calledAETs.addAll(Arrays.asList(aets));
        }
    }
    
    public String[] getCalledAETs() {
        return (String[])calledAETs.toArray(new String[calledAETs.size()]);
    }
    
    public boolean addCallingAET(String aet) {
        StringUtils.checkAET(aet);
        return callingAETs.add(aet);
    }
    
    public boolean removeCallingAET(String aet) {
        return callingAETs.remove(aet);
    }
    
    public void setCallingAETs(String[] aets) {
        if (aets == null) {
            callingAETs.clear();
        } else {
            StringUtils.checkAETs(aets); 
            callingAETs.clear();
            callingAETs.addAll(Arrays.asList(aets));
        }
    }
    
    public String[] getCallingAETs() {
        return (String[])callingAETs.toArray(new String[callingAETs.size()]);
    }

    public void setCalledAETFilter(AETFilter filter) {
        this.calledAETFilter = filter;
    }

    public AETFilter getCalledAETFilter() {
        return calledAETFilter;
    }

    public void setCallingAETFilter(AETFilter filter) {
        this.callingAETFilter = filter;
    }

    public AETFilter getCallingAETFilter() {
        return callingAETFilter;
    }
    
    
    public AcceptorPolicy getPolicyForCallingAET(String aet) {
        return (AcceptorPolicy)policyForCallingAET.get(aet);
    }

    public AcceptorPolicy putPolicyForCallingAET (String aet,
            AcceptorPolicy policy) {
        return putPolicyForXXXAET(aet, policy, policyForCallingAET);
    }
    
    public AcceptorPolicy getPolicyForCalledAET(String aet) {
        return (AcceptorPolicy)policyForCalledAET.get(aet);
    }
    
    public AcceptorPolicy putPolicyForCalledAET (String aet,
            AcceptorPolicy policy) {
        return putPolicyForXXXAET(aet, policy, policyForCalledAET);
    }

    private AcceptorPolicy putPolicyForXXXAET(String aet, 
            AcceptorPolicy policy, HashMap map) {
        if (policy != null) {
            return (AcceptorPolicy)map.put(StringUtils.checkAET(aet), policy);
        } else {
            return (AcceptorPolicy)map.remove(aet);
        }
    }
        
    public final PresContext putPresContext(String asuid, String[] tsuids) {
        if (tsuids != null) {
            return (PresContext)presCtxMap.put(asuid,
                new PresContextImpl(0x020, 1, 0,
                    StringUtils.checkUID(asuid), 
                    StringUtils.checkUIDs(tsuids)));
        } else {
            return (PresContext)presCtxMap.remove(asuid);
        }
    }
    
    public PresContext getPresContext(String as) {
        return (PresContext)presCtxMap.get(as);
    }
    
    public RoleSelection putRoleSelection(String uid, boolean scu, boolean scp) {
        return (RoleSelection)roleSelectionMap.put(
            StringUtils.checkUID(uid), new RoleSelectionImpl(uid, scu, scp));
    }
    
    public RoleSelection getRoleSelection(String uid) {
        return (RoleSelection)roleSelectionMap.get(uid);
    }
    
    public RoleSelection removeRoleSelection(String uid) {
        return (RoleSelection)roleSelectionMap.remove(uid);
    }
    
    public ExtNegotiator putExtNegPolicy(String uid, ExtNegotiator en) {
        if (en != null) {
            return (ExtNegotiator)extNegotiaionMap.put(uid, en);
        } else {
            return (ExtNegotiator)extNegotiaionMap.remove(uid);
        }
    }
    
    public ExtNegotiator getExtNegPolicy(String uid) {
        return (ExtNegotiator)extNegotiaionMap.get(uid);
    }
    
    public final void setUserIdentityNegotiator(
            UserIdentityNegotiator userIdentityNegotiator) {
        if (userIdentityNegotiator == null) {
            throw new NullPointerException();
        }
        this.userIdentityNegotiator = userIdentityNegotiator;
    }

    public final UserIdentityNegotiator getUserIdentityNegotiator() {
        return userIdentityNegotiator;
    }

    public PDU negotiate(Association assoc) {
    	AAssociateRQ rq = assoc.getAAssociateRQ();
        if ((rq.getProtocolVersion() & 0x0001) == 0) {
            return new AAssociateRJImpl(
                AAssociateRJ.REJECTED_PERMANENT,
                AAssociateRJ.SERVICE_PROVIDER_ACSE,
                AAssociateRJ.PROTOCOL_VERSION_NOT_SUPPORTED);
        }
        String calledAET = rq.getCalledAET();
        if (calledAETFilter != null && !calledAETFilter.accept(calledAET)) {
            return new AAssociateRJImpl(
                AAssociateRJ.REJECTED_PERMANENT,
                AAssociateRJ.SERVICE_USER,
                AAssociateRJ.CALLED_AE_TITLE_NOT_RECOGNIZED);
        }
        AcceptorPolicyImpl policy1 =
        (AcceptorPolicyImpl)getPolicyForCalledAET(calledAET);
        if (policy1 == null)
            policy1 = this;
        
        String callingAET = rq.getCallingAET();
        if (policy1.callingAETFilter != null && !policy1.callingAETFilter.accept(callingAET)) {
            return new AAssociateRJImpl(
                AAssociateRJ.REJECTED_PERMANENT,
                AAssociateRJ.SERVICE_USER,
                AAssociateRJ.CALLING_AE_TITLE_NOT_RECOGNIZED);
        }
        AcceptorPolicyImpl policy2 =
        	(AcceptorPolicyImpl)policy1.getPolicyForCallingAET(callingAET);
        if (policy2 == null)
            policy2 = policy1;
        
        return policy2.doNegotiate(assoc, rq);
    }

	private PDU doNegotiate(Association assoc, AAssociateRQ rq) {
		String appCtx = negotiateAppCtx(rq.getApplicationContext());
        if (appCtx == null) {
            return new AAssociateRJImpl(
            AAssociateRJ.REJECTED_PERMANENT,
            AAssociateRJ.SERVICE_USER,
            AAssociateRJ.APPLICATION_CONTEXT_NAME_NOT_SUPPORTED);
        }
        
        AAssociateAC ac = new AAssociateACImpl();
        ac.setApplicationContext(appCtx);
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        ac.setMaxPDULength(this.maxLength);
        ac.setImplClassUID(this.implClassUID);
        ac.setImplVersionName(this.implVers);
        ac.setAsyncOpsWindow(negotiateAOW(rq.getAsyncOpsWindow()));
        try {
            ac.setUserIdentity(
            		userIdentityNegotiator.negotiate(assoc));
        } catch (AAssociateRJException e) {
            return e.getAAssociateRJ();
        }
        negotiatePresCtx(rq, ac);
        negotiateRoleSelection(rq, ac);
        negotiateExt(rq, ac);
        return ac;
	}
    
    private String negotiateAppCtx(String proposed) {
        String retval = (String)appCtxMap.get(proposed);
        if (retval != null)
            return retval;
        
        if (UIDs.DICOMApplicationContextName.equals(proposed))
            return proposed;
        
        return null;
    }
    
    private void negotiatePresCtx(AAssociateRQ rq, AAssociateAC ac) {
        for (Iterator it = rq.listPresContext().iterator(); it.hasNext();)
            ac.addPresContext(negotiatePresCtx((PresContext)it.next()));
    }
    
    private PresContext negotiatePresCtx(PresContext offered) {
        PresContext accept = getPresContext(offered.getAbstractSyntaxUID());
        if (accept == null)
            return mkACPresCtx(offered,
                    PresContext.ABSTRACT_SYNTAX_NOT_SUPPORTED,
                    offered.getTransferSyntaxUID());

        for (Iterator it = accept.getTransferSyntaxUIDs().iterator();
                it.hasNext();) {
            String tsuid = (String) it.next();
            if (offered.getTransferSyntaxUIDs().indexOf(tsuid) != -1)
                return mkACPresCtx(offered, PresContext.ACCEPTANCE, tsuid);
        }

        return mkACPresCtx(offered,
                PresContext.TRANSFER_SYNTAXES_NOT_SUPPORTED,
                offered.getTransferSyntaxUID());
    }

    private PresContextImpl mkACPresCtx(PresContext offered, int result,
            String tsuid) {
        return new PresContextImpl(0x021, offered.pcid(), result, null,
                new String[]{ tsuid } );
    }
    
    private void negotiateRoleSelection(AAssociateRQ rq, AAssociateAC ac) {
        for (Iterator it = rq.listRoleSelections().iterator(); it.hasNext();)
            ac.addRoleSelection(negotiateRoleSelection((RoleSelection)it.next()));
    }
    
    private RoleSelection negotiateRoleSelection(RoleSelection offered) {
        boolean scu = offered.scu();
        boolean scp = false;
        
        RoleSelection accept = getRoleSelection(offered.getSOPClassUID());
        if (accept != null) {
            scu = offered.scu() && accept.scu();
            scp = offered.scp() && accept.scp();
        }
        return new RoleSelectionImpl(offered.getSOPClassUID(), scu, scp);
    }
    
    private void negotiateExt(AAssociateRQ rq, AAssociateAC ac) {
        for (Iterator it = rq.listExtNegotiations().iterator(); it.hasNext();) {
            ExtNegotiation offered = (ExtNegotiation)it.next();
            String uid = offered.getSOPClassUID();
            ExtNegotiator enp = getExtNegPolicy(uid);
            if (enp != null)
                ac.addExtNegotiation(
                new ExtNegotiationImpl(uid, enp.negotiate(offered.info())));
        }
    }

    private AsyncOpsWindow negotiateAOW(AsyncOpsWindow offered) {
        if (offered == null)
            return null;
        
        if (aow == null)
            return AsyncOpsWindowImpl.DEFAULT;
        
        return new AsyncOpsWindowImpl(
            minAOW(offered.getMaxOpsInvoked(), aow.getMaxOpsInvoked()),
            minAOW(offered.getMaxOpsPerformed(), aow.getMaxOpsPerformed()));
    }
    
    static int minAOW(int a, int b) {
        return a == 0 ? b : b == 0 ? a : Math.min(a,b);
    }
    
    public List listPresContext() {
        return Collections.unmodifiableList(
            new ArrayList(presCtxMap.values()));       
    }

}
