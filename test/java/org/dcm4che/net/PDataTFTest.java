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

import java.io.*;
import java.util.*;

import junit.framework.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class PDataTFTest extends ExtTestCase {

    public PDataTFTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PDataTFTest.class);
        return suite;
    }
    
    private static final String P_DATA_TF1 = "data/PDataTF1.pdu";
    private static final String P_DATA_TF2 = "data/PDataTF2.pdu";
    private final byte[] CMD = new byte[100];
    private final byte[] DATA = new byte[200];
    private final int PCID = 127;
    private final int MAX_LEN = 250;


    private AssociationFactory fact;
    
    protected void setUp() throws Exception {
        Arrays.fill(CMD, (byte)0xcc);
        Arrays.fill(DATA, (byte)0xdd);
        fact = AssociationFactory.getInstance();
    }
        
    public void testWrite() throws Exception {
        PDataTF pdu1 = fact.newPDataTF(MAX_LEN);
        assertEquals(MAX_LEN-6, pdu1.free());
        pdu1.openPDV(PCID, true);
        assertEquals(MAX_LEN-6, pdu1.free());
        assertEquals(CMD.length, pdu1.write(CMD, 0, CMD.length));
        assertEquals(MAX_LEN-6-CMD.length, pdu1.free());
        pdu1.closePDV(true);
        assertEquals(MAX_LEN-6-CMD.length-6, pdu1.free());
        pdu1.openPDV(PCID, false);
        int off = pdu1.write(DATA, 0, DATA.length);
        assertEquals(MAX_LEN-6-CMD.length-6, off);
        assertEquals(0, pdu1.free());
        assertTrue(!pdu1.write(0));
        pdu1.closePDV(false);
        assertEquals(-6, pdu1.free());
        ByteArrayOutputStream out1 = new ByteArrayOutputStream(MAX_LEN + 4);
//        OutputStream out1 = new FileOutputStream(P_DATA_TF1);        
        pdu1.writeTo(out1);
        out1.close();
        assertEquals(load(P_DATA_TF1), out1.toByteArray());
        PDataTF pdu2 = fact.newPDataTF(MAX_LEN);
        pdu2.openPDV(PCID, false);
        assertEquals(MAX_LEN-6, pdu2.free());
        assertTrue(pdu2.write(DATA[off++]));
        assertEquals(MAX_LEN-7, pdu2.free());
        assertEquals(DATA.length-off, pdu2.write(DATA, off, DATA.length-off));
        assertEquals(MAX_LEN-7-DATA.length+off, pdu2.free());
        pdu2.closePDV(true);
        assertEquals(MAX_LEN-7-DATA.length+off-6, pdu2.free());
        ByteArrayOutputStream out2 = new ByteArrayOutputStream(MAX_LEN + 4);
//        OutputStream out2 = new FileOutputStream(P_DATA_TF2);        
        pdu2.writeTo(out2);
        assertEquals(load(P_DATA_TF2), out2.toByteArray());
    }

    public void testRead() throws Exception {
        InputStream in1 = new FileInputStream(P_DATA_TF1);
        PDataTF pdu1 = null;
        try {
            pdu1 = (PDataTF)fact.readFrom(in1, null);            
        } finally {
            try { in1.close(); } catch (IOException ignore) {}
        }
        PDataTF.PDV pdv11 = pdu1.readPDV();
        assertNotNull(pdv11);
        assertEquals(CMD.length+2, pdv11.length());
        assertEquals(PCID, pdv11.pcid());
        assertTrue(pdv11.cmd());
        assertTrue(pdv11.last());
        byte[] cmd = new byte[CMD.length];
        InputStream pdv11in = pdv11.getInputStream();
        pdv11in.read(cmd);
        assertEquals(-1, pdv11in.read());
        assertEquals(CMD, cmd);
        PDataTF.PDV pdv12 = pdu1.readPDV();
        assertNotNull(pdv12);
        int off = pdv12.length()-2;
        assertEquals(MAX_LEN-6-CMD.length-6, off);
        assertEquals(PCID, pdv12.pcid());
        assertTrue(!pdv12.cmd());
        assertTrue(!pdv12.last());
        byte[] data = new byte[DATA.length];
        InputStream pdv12in = pdv12.getInputStream();
        pdv12in.read(data, 0, off);
        assertEquals(-1, pdv12in.read());
        assertNull(pdu1.readPDV());
        InputStream in2 = new FileInputStream(P_DATA_TF2);
        PDataTF pdu2 = null;
        try {
            pdu2 = (PDataTF)fact.readFrom(in2, null);            
        } finally {
            try { in2.close(); } catch (IOException ignore) {}
        }
        PDataTF.PDV pdv21 = pdu2.readPDV();
        assertNotNull(pdv21);
        assertEquals(DATA.length-off+2, pdv21.length());
        assertEquals(PCID, pdv21.pcid());
        assertTrue(!pdv21.cmd());
        assertTrue(pdv21.last());
        InputStream pdv21in = pdv21.getInputStream();
        pdv21in.read(data, off, DATA.length-off);
        assertEquals(-1, pdv21in.read());
        assertNull(pdu2.readPDV());
        assertEquals(DATA, data);
    }    
}

