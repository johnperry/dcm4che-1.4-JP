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

package org.dcm4che.hl7;

import java.io.*;
import java.util.*;

import junit.framework.*;
/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 * @since August 11, 2002
 *
 */
public class HL7MessageTest  extends TestCase {

    public HL7MessageTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(HL7MessageTest.class);
        return suite;
    }

    private static final String ORM_O01 = "data/ORM_O01.hl7";
    private static final String ADT_A40 = "data/ADT_A40.hl7";

    private static final HL7Factory fact = HL7Factory.getInstance();
    
    protected void setUp() throws Exception {
    }
    
    private byte[] readDataFrom(String fname) throws Exception {
        File f = new File(fname);
        byte[] data = new byte[(int)f.length()];
        InputStream in = new FileInputStream(f);
        try {
            in.read(data);
        } finally {
            try { in.close(); } catch (IOException e) {}
        }
        return data;
    }
     
    public void testORM_O01() throws Exception {
        HL7Message msg = fact.parse(readDataFrom(ORM_O01));
//        System.out.println(msg.toVerboseString());
        MSHSegment msh = msg.header();
        assertEquals("ORM", msh.getMessageType());
        assertEquals("O01", msh.getTriggerEvent());
        Iterator it = msg.segments().iterator();
        assertTrue(it.hasNext());
        HL7Segment pid = (HL7Segment) it.next();
        assertEquals("PID", pid.id());        
        assertTrue(it.hasNext());
        HL7Segment pv1 = (HL7Segment) it.next();
        assertEquals("PV1", pv1.id());        
        assertTrue(it.hasNext());
        HL7Segment orc = (HL7Segment) it.next();
        assertEquals("ORC", orc.id());        
        assertTrue(it.hasNext());
        HL7Segment obr = (HL7Segment) it.next();
        assertEquals("OBR", obr.id());        
        assertTrue(it.hasNext());
        HL7Segment zds = (HL7Segment) it.next();
        assertEquals("ZDS", zds.id());        
        assertTrue(!it.hasNext());
    }
    
    public void testADT_A40() throws Exception {
        HL7Message msg = fact.parse(readDataFrom(ADT_A40));
//        System.out.println(msg.toVerboseString());
        MSHSegment msh = msg.header();
        assertEquals("ADT", msh.getMessageType());
        assertEquals("A40", msh.getTriggerEvent());
        Iterator it = msg.segments().iterator();
        assertTrue(it.hasNext());
        HL7Segment evn = (HL7Segment) it.next();
        assertEquals("EVN", evn.id());
        assertTrue(it.hasNext());
        HL7Segment pid = (HL7Segment) it.next();
        assertEquals("PID", pid.id());        
        assertTrue(it.hasNext());
        HL7Segment mrg = (HL7Segment) it.next();
        assertEquals("MRG", mrg.id());        
        assertTrue(!it.hasNext());
    }
}
