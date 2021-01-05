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
public class AAssociateACTest extends AAssociateRQACTest {

    public AAssociateACTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AAssociateACTest.class);
        return suite;
    }

    protected static final String A_ASSOCIATE_AC = "data/AAssociateAC.pdu";    
    
    protected void check(AAssociateAC ac) {
        super.check(ac);
        Collection c = ac.listPresContext();
        assertEquals(3, c.size());
        Iterator pcit = c.iterator();
        PresContext pc1 = (PresContext)pcit.next();
        assertEquals(PresContext.ACCEPTANCE, pc1.result());
        assertEquals(TS_UID1, pc1.getTransferSyntaxUID());
        assertEquals(pc1, ac.getPresContext(1));
        PresContext pc2 = (PresContext)pcit.next();
        assertEquals(PresContext.ACCEPTANCE, pc2.result());
        assertEquals(TS_UID2, pc2.getTransferSyntaxUID());
        assertEquals(pc2, ac.getPresContext(3));
        PresContext pc3 = (PresContext)pcit.next();
        assertEquals(PresContext.TRANSFER_SYNTAXES_NOT_SUPPORTED, pc3.result());
        assertEquals(TS_UID3, pc3.getTransferSyntaxUID());
        assertEquals(pc3, ac.getPresContext(5));
        assertTrue(!pcit.hasNext());
    }
        
    protected void set(AAssociateAC ac) {
        super.set(ac);
        ac.addPresContext(fact.newPresContext(1,
                PresContext.ACCEPTANCE, TS_UID1));
        ac.addPresContext(fact.newPresContext(3,
                PresContext.ACCEPTANCE, TS_UID2));
        ac.addPresContext(fact.newPresContext(5,
                PresContext.TRANSFER_SYNTAXES_NOT_SUPPORTED, TS_UID3));
    }
    
    public void testWrite() throws Exception {
        AAssociateAC ac = fact.newAAssociateAC();
        set(ac);
        check(ac);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        OutputStream out = new FileOutputStream(A_ASSOCIATE_AC);        
        ac.writeTo(out);
        out.close();
        assertEquals(load(A_ASSOCIATE_AC), out.toByteArray());
    }

    public void testRead() throws Exception {
        InputStream in = new FileInputStream(A_ASSOCIATE_AC);
        AAssociateAC pdu = null;
        try {
            pdu = (AAssociateAC)fact.readFrom(in, null);            
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
        check(pdu);        
     }
}
