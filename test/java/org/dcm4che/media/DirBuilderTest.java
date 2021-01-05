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

package org.dcm4che.media;

import org.dcm4che.dict.*;
import org.dcm4che.data.*;

import junit.framework.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author  gunter zeilinger
 * @version 1.0.0
 */
public class DirBuilderTest extends TestCase {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    private static final String INST_UID = "1.2.40.0.13.1.1.99";
    private static final String FILE_SET_ID = "FILE_SET_ID";
    private static final File OUT_FILE = new File("data/TMP_TEST");
    private static final String[] FILE_IDs = {
        "data/6AF8_10",
        "data/MRABDO",
        "data/6AF8_30",
        "data/MRABDOR",
    };
    
    public static Test suite() {
        return new TestSuite(DirBuilderTest.class);
    }
    
    public DirBuilderTest(String name) {
        super(name);
    }
    
    private DcmObjectFactory objFact = DcmObjectFactory.getInstance();
    private DirBuilderFactory fact;
    private DirBuilderPref pref;
    
    protected void setUp() throws Exception {
        fact = DirBuilderFactory.getInstance();
        pref = fact.newDirBuilderPref();
        pref.setFilterForRecordType("PATIENT", getPatientFilter());
        pref.setFilterForRecordType("STUDY", getStudyFilter());
        pref.setFilterForRecordType("SERIES", getSeriesFilter());
        pref.setFilterForRecordType("IMAGE", getImageFilter());
    }
    
    protected void tearDown() throws Exception {
        if (OUT_FILE.exists()) {
            OUT_FILE.delete();
        }
    }
    
    private Dataset getPatientFilter() {
        Dataset retval = objFact.newDataset();
        retval.putCS(Tags.SpecificCharacterSet);
        retval.putPN(Tags.PatientName);
        retval.putLO(Tags.PatientID);
        retval.putDA(Tags.PatientBirthDate);
        retval.putCS(Tags.PatientSex);
        return retval;
    }
    
    private Dataset getStudyFilter() {
        Dataset retval = objFact.newDataset();
        retval.putCS(Tags.SpecificCharacterSet);
        retval.putDA(Tags.StudyDate);
        retval.putTM(Tags.StudyTime);
        retval.putSH(Tags.AccessionNumber);
        retval.putPN(Tags.ReferringPhysicianName);
        retval.putLO(Tags.StudyDescription);
        retval.putSQ(Tags.ProcedureCodeSeq);
        retval.putUI(Tags.StudyInstanceUID);
        retval.putSH(Tags.StudyID);
        return retval;
    }
    
    private Dataset getSeriesFilter() {
        Dataset retval = objFact.newDataset();
        retval.putCS(Tags.SpecificCharacterSet);
        retval.putDA(Tags.SeriesDate);
        retval.putTM(Tags.SeriesTime);
        retval.putCS(Tags.Modality);
        retval.putLO(Tags.Manufacturer);
        retval.putLO(Tags.SeriesDescription);
        retval.putCS(Tags.BodyPartExamined);
        retval.putUI(Tags.SeriesInstanceUID);
        retval.putIS(Tags.SeriesNumber);
        retval.putCS(Tags.Laterality);
        return retval;
    }
    
    private Dataset getImageFilter() {
        Dataset retval = objFact.newDataset();
        retval.putCS(Tags.SpecificCharacterSet);
        retval.putDA(Tags.ContentDate);
        retval.putTM(Tags.ContentTime);
        retval.putSQ(Tags.RefImageSeq);
        retval.putLO(Tags.ContrastBolusAgent);
        retval.putIS(Tags.InstanceNumber);
        retval.putIS(Tags.NumberOfFrames);
        return retval;
    }
    
    public void testAddFileRef() throws Exception {
        DirWriter w1 = fact.newDirWriter(OUT_FILE, INST_UID, FILE_SET_ID,
        null, null, null);
        DirBuilder b1 = fact.newDirBuilder(w1, pref);
        try {
            int c = 0;
            c += b1.addFileRef(new File(FILE_IDs[0]));
            c += b1.addFileRef(new File(FILE_IDs[1]));
            c += b1.addFileRef(new File(FILE_IDs[2]));
            c += b1.addFileRef(new File(FILE_IDs[3]));
            assertEquals(15, c);
        } finally {
            b1.close();
        }
        
    }
 }
