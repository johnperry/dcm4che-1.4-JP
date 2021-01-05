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

package org.dcm4che.srom;

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.Dataset;

import java.io.*;

import junit.framework.*;

/**
 *
 * @author gunter
 */
public class HL7SRExportTest extends TestCase {
    
    public HL7SRExportTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(HL7SRExportTest.class);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    private static final String SR_DCM = "data/sr_603mr.dcm";
    private static final String SR_HL7 = "data/sr_603mr.hl7";
    
    private DcmObjectFactory dsf = null;
    private SRDocumentFactory srf = null;
    private byte[] expected;
        
    protected void setUp() throws Exception {    
        dsf = DcmObjectFactory.getInstance();
        srf = SRDocumentFactory.getInstance();
        File f = new File(SR_HL7);
        InputStream in = new FileInputStream(f);        
        try {
            expected = new byte[(int)f.length()];
            in.read(expected);
        } finally {
            try { in.close(); } catch (IOException ignore) {};
        }
    }
    
    private void assertEquals(byte[] expected, byte[] value) {
        TestCase.assertNotNull(value);
        TestCase.assertEquals(expected.length, value.length);
        for (int i = 0; i < expected.length; ++i) {
            TestCase.assertEquals("byte[" + i + "]", expected[i], value[i]);
        }
    }
    
    public void testToHL7() throws Exception {
        Dataset ds = dsf.newDataset();
        InputStream in = new BufferedInputStream(
                new FileInputStream(new File(SR_DCM)));
        try {
            ds.readFile(in, null, -1);
        } finally {
            in.close();
        }
        SRDocument sr = srf.newSRDocument(ds);
        HL7SRExport export = srf.newHL7SRExport(
            "SendingApplication","SendingFacility",
            "ReceivingApplication","ReceivingFacility");
        byte[] msg = export.toHL7(sr, "MessageControlID",
            "IssuerOfPatientID", "PatientAccountNumber",
            "PlacerOrderNumber", "FillerOrderNumber",
            "UniversalServiceID");
//        new FileOutputStream(SR_HL7).write(msg);
        assertEquals(expected, msg);
     }

}
