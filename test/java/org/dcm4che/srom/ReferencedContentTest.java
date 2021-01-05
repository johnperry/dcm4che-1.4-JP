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

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;

import junit.framework.*;

/**
 *
 * @author gunter
 */
public class ReferencedContentTest extends TestCase {
    
    public ReferencedContentTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    static SRDocumentFactory srf = SRDocumentFactory.getInstance();
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ReferencedContentTest.class);
        return suite;
    }
    
    private Patient pat;
    private Study study;
    private Series series;
    private Equipment equip;
    private Code title;
    private RefSOP refSOP;
    private ImageContent imgRef;
    private NumContent num;
    private TextContent text;
    private SCoordContent scoord;
    private SRDocument doc;
    
    protected void setUp() throws Exception {
        pat = srf.newPatient("P1234", "DOE^JOHN", Patient.Sex.MALE, null);
        study = srf.newStudy("1.2.3.4.5", "S12345", null, null, "A12345",
            null, null);
        series = srf.newSRSeries("1.2.3.4.5.6", 6, null);
        equip = srf.newEquipment("TIANI", "ReferencedContentTest", null);
        title = srf.newCode("11528-7", "LN", "Radiology Report");
        doc = srf.newSRDocument(pat, study, series, equip, UIDs.ComprehensiveSR,
            "1.2.3.4.5.6.7", 7, null, null, title, true);
        refSOP = srf.newRefSOP(UIDs.CTImageStorage, "1.2.3.4.5.1.1");
        imgRef = doc.createImageContent(null, null,
            null, refSOP, null, null, null);
        text = doc.createTextContent(null, null, 
            srf.newCode("121073", "DCM", "Impression"),
            "May be an artifact");
        num = doc.createNumContent(null, null,
            srf.newCode("121211", "DCM","Path Length"), 5.f,
            srf.newCode("cm", "UCUM", "1.4", "centimeter"));
        scoord = doc.createPolylineSCoordContent(null, null,
            srf.newCode("121055", "DCM", "Path"),
            new float[] { 543.f, 221.f, 503.f, 251.f });
    }
    
    public void testBackwardRef() throws Exception {
        doc.appendChild(Content.RelationType.CONTAINS, imgRef);
        doc.appendChild(Content.RelationType.CONTAINS, text);
        text.appendChild(Content.RelationType.INFERRED_FROM,
            doc.createReferencedContent(imgRef));
        doc.appendChild(Content.RelationType.CONTAINS, num);
        num.appendChild(Content.RelationType.INFERRED_FROM, scoord);
        scoord.appendChild(Content.RelationType.SELECTED_FROM,
            doc.createReferencedContent(imgRef));
        Dataset ds = doc.toDataset();

//        ds.writeFile(new java.io.FileOutputStream("testSR1.dcm"), null);
        
        SRDocument doc2 = srf.newSRDocument(ds);
        Content imgRef2 = doc2.getFirstChild();
        assertTrue(imgRef2 instanceof ImageContent);

        Content text2 = imgRef2.getNextSibling();
        assertTrue(text2 instanceof TextContent);
        Content ref1 = text2.getFirstChild();
        assertTrue(ref1 instanceof ReferencedContent);
        Content imgRef3 = ((ReferencedContent) ref1).getRefContent();
        assertSame(imgRef2, imgRef3);

        Content num2 = text2.getNextSibling();
        assertTrue(num2 instanceof NumContent);
        Content scoord2 = num2.getFirstChild();
        assertTrue(scoord2 instanceof SCoordContent);
        Content ref2 = scoord2.getFirstChild();
        assertTrue(ref2 instanceof ReferencedContent);
        Content imgRef4 = ((ReferencedContent) ref2).getRefContent();
        assertSame(imgRef2, imgRef4);
    }
    
    
    public void testForwardRef() throws Exception {
        doc.appendChild(Content.RelationType.CONTAINS, text);
        doc.appendChild(Content.RelationType.CONTAINS, num);
        doc.appendChild(Content.RelationType.CONTAINS, imgRef);
        text.appendChild(Content.RelationType.INFERRED_FROM,
            doc.createReferencedContent(imgRef));
        num.appendChild(Content.RelationType.INFERRED_FROM, scoord);
        scoord.appendChild(Content.RelationType.SELECTED_FROM,
            doc.createReferencedContent(imgRef));
        Dataset ds = doc.toDataset();
        
//        ds.writeFile(new java.io.FileOutputStream("testSR2.dcm"), null);
        
        SRDocument doc2 = srf.newSRDocument(ds);
        Content text2 = doc2.getFirstChild();
        assertTrue(text2 instanceof TextContent);        
        Content num2 = text2.getNextSibling();
        assertTrue(num2 instanceof NumContent);        
        Content imgRef2 = num2.getNextSibling();
        assertTrue(imgRef2 instanceof ImageContent);
        
        Content ref1 = text2.getFirstChild();
        assertTrue(ref1 instanceof ReferencedContent);
        Content imgRef3 = ((ReferencedContent) ref1).getRefContent();
        assertSame(imgRef2, imgRef3);

        Content scoord2 = num2.getFirstChild();
        assertTrue(scoord2 instanceof SCoordContent);
        Content ref2 = scoord2.getFirstChild();
        assertTrue(ref2 instanceof ReferencedContent);
        Content imgRef4 = ((ReferencedContent) ref2).getRefContent();
        assertSame(imgRef2, imgRef4);
    }
    
}
