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

import org.dcm4che.dict.UIDs;

import java.io.*;
import java.util.*;

import junit.framework.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class AAssociateRQACTest extends ExtTestCase {
    
    public AAssociateRQACTest(java.lang.String testName) {
        super(testName);
    }
    
    protected static final String IMPL_CLASS_UID = "1.2.40.0.13.1.1";
    protected static final String IMPL_VERSION_NAME = "VERSIONNAME";
    protected static final String CALLING_AET = "CALLING_AET";
    protected static final String CALLED_AET = "CALLED_AET";
    protected static final String AS_UID1 = UIDs.Verification;
    protected static final String AS_UID2 = UIDs.ComputedRadiographyImageStorage;
    protected static final String TS_UID1 = UIDs.ImplicitVRLittleEndian;
    protected static final String TS_UID2 = UIDs.ExplicitVRLittleEndian;
    protected static final String TS_UID3 = UIDs.JPEGExtended;
    protected static final int MAX_LENGTH = 12345;
    protected static final int MAX_OPS_INVOKED = 0;
    protected static final int MAX_OPS_PERFORMED = 2;
    protected static final byte[] EXT_NEG_INFO = new byte[0xe];
    
    protected AssociationFactory fact;
    
    protected void setUp() throws Exception {
        fact = AssociationFactory.getInstance();
        Arrays.fill(EXT_NEG_INFO, (byte)0xee);
    }
    
    protected void check(AAssociateRQAC rqac) {
        assertEquals(1, rqac.getProtocolVersion());
        assertEquals(CALLING_AET, rqac.getCallingAET());
        assertEquals(CALLED_AET, rqac.getCalledAET());
        assertEquals(UIDs.DICOMApplicationContextName,
        rqac.getApplicationContext());
        assertEquals(IMPL_CLASS_UID,rqac.getImplClassUID());
        assertEquals(IMPL_VERSION_NAME,rqac.getImplVersionName());
        assertEquals(MAX_LENGTH, rqac.getMaxPDULength());
        AsyncOpsWindow aow = rqac.getAsyncOpsWindow();
        assertNotNull(aow);
        assertEquals(MAX_OPS_INVOKED, aow.getMaxOpsInvoked());
        assertEquals(MAX_OPS_PERFORMED, aow.getMaxOpsPerformed());
        Collection rsList = rqac.listRoleSelections();
        assertEquals(2, rsList.size());
        Iterator rsit = rsList.iterator();
        RoleSelection rs1 = (RoleSelection)rsit.next();
        assertEquals(AS_UID1, rs1.getSOPClassUID());
        assertTrue(rs1.scu());
        assertTrue(rs1.scp());
        assertEquals(rs1, rqac.getRoleSelection(AS_UID1));
        RoleSelection rs2 = (RoleSelection)rsit.next();
        assertEquals(AS_UID2, rs2.getSOPClassUID());
        assertTrue(rs2.scu());
        assertTrue(!rs2.scp());
        assertEquals(rs2, rqac.getRoleSelection(AS_UID2));
        assertTrue(!rsit.hasNext());
        Collection enList = rqac.listExtNegotiations();
        assertEquals(1, enList.size());
        Iterator enit = enList.iterator();
        ExtNegotiation en = (ExtNegotiation)enit.next();
        assertEquals(AS_UID2, en.getSOPClassUID());
        assertEquals(EXT_NEG_INFO, en.info());
        assertEquals(en, rqac.getExtNegotiation(AS_UID2));
        assertTrue(!enit.hasNext());
    }
    
    protected void set(AAssociateRQAC rqac) {
        rqac.setImplClassUID(IMPL_CLASS_UID);
        rqac.setImplVersionName(IMPL_VERSION_NAME);
        rqac.setCallingAET(CALLING_AET);
        rqac.setCalledAET(CALLED_AET);
        rqac.setMaxPDULength(MAX_LENGTH);
        rqac.setAsyncOpsWindow(fact.newAsyncOpsWindow(
        MAX_OPS_INVOKED, MAX_OPS_PERFORMED));
        rqac.addRoleSelection(fact.newRoleSelection(AS_UID1, true, true));
        rqac.addRoleSelection(fact.newRoleSelection(AS_UID2, true, false));
        rqac.addExtNegotiation(fact.newExtNegotiation(AS_UID2, EXT_NEG_INFO));
    }
}
