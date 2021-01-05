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
public class AAssociateRQTest extends AAssociateRQACTest {

    public AAssociateRQTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AAssociateRQTest.class);
        return suite;
    }

    protected static final String A_ASSOCIATE_RQ = "data/AAssociateRQ.pdu";
    
    
    protected void check(AAssociateRQ rq) {
        super.check(rq);
        Collection c = rq.listPresContext();
        assertEquals(3, c.size());
        Iterator pcit = c.iterator();
        PresContext pc1 = (PresContext)pcit.next();
        assertEquals(AS_UID1, pc1.getAbstractSyntaxUID());
        List ts1 = pc1.getTransferSyntaxUIDs();
        assertEquals(1, ts1.size());
        assertEquals(TS_UID1, ts1.get(0));
        assertEquals(pc1, rq.getPresContext(1));
        PresContext pc2 = (PresContext)pcit.next();
        assertEquals(AS_UID2, pc2.getAbstractSyntaxUID());
        List ts2 = pc2.getTransferSyntaxUIDs();
        assertEquals(2, ts2.size());
        assertEquals(TS_UID1, ts2.get(0));
        assertEquals(TS_UID2, ts2.get(1));
        assertEquals(pc2, rq.getPresContext(3));
        PresContext pc3 = (PresContext)pcit.next();
        assertEquals(AS_UID2, pc3.getAbstractSyntaxUID());
        List ts3 = pc3.getTransferSyntaxUIDs();
        assertEquals(1, ts3.size());
        assertEquals(TS_UID3, ts3.get(0));
        assertEquals(pc3, rq.getPresContext(5));
        assertTrue(!pcit.hasNext());
    }
        
    protected void set(AAssociateRQ rq) {
        super.set(rq);
        rq.addPresContext(fact.newPresContext(rq.nextPCID(), AS_UID1,
                new String[] { TS_UID1 }));
        rq.addPresContext(fact.newPresContext(rq.nextPCID(), AS_UID2,
                new String[] { TS_UID1, TS_UID2 }));
        rq.addPresContext(fact.newPresContext(rq.nextPCID(), AS_UID2,
                new String[] { TS_UID3 }));
    }
    
    public void testWrite() throws Exception {
        AAssociateRQ rq = fact.newAAssociateRQ();
        set(rq);
        check(rq);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        OutputStream out = new FileOutputStream(A_ASSOCIATE_RQ);        
        rq.writeTo(out);
        out.close();
        assertEquals(load(A_ASSOCIATE_RQ), out.toByteArray());
    }

    public void testRead() throws Exception {
        InputStream in = new FileInputStream(A_ASSOCIATE_RQ);
        AAssociateRQ pdu = null;
        try {
            pdu = (AAssociateRQ)fact.readFrom(in, null);            
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
        check(pdu);        
     }
}
