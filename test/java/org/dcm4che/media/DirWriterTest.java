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
import java.nio.ByteOrder;
import java.util.Date;

/**
 *
 * @author  gunter zeilinger
 * @version 1.0.0
 */
public class DirWriterTest extends TestCase {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    private static final String CLASS_UID = "1.2.840.10008.1.3.10";
    private static final String INST_UID = "1.2.40.0.13.1.1.99";
    private static final String TS_UID = "1.2.840.10008.1.2.1";
    private static final String FILE_SET_ID = "FILE_SET_ID";
    
    public static Test suite() {
        return new TestSuite(DirWriterTest.class);
    }
    
    /**
     * Creates new DictionaryTest
     */
    public DirWriterTest(String name) {
        super(name);
    }
    
    private File theFile;
    private File IMG1_FILE;
    private File IMG2_FILE;
    
    protected void setUp() throws Exception {
        theFile = File.createTempFile("dcm4che",".dcm");
        File dir = theFile.getParentFile();
        IMG1_FILE = new File(new File(dir, "DIR1"),"IMG1_FILE");
        IMG2_FILE = new File(new File(dir, "DIR2"),"IMG2_FILE");
    }
    
    protected void tearDown() throws Exception {
        theFile.delete();
    }
    
    private void checkFilesetIDs(DirReader r) throws Exception {
        Dataset fsi = r.getFileSetInfo();
        FileMetaInfo fmi = fsi.getFileMetaInfo();
        assertEquals(INST_UID, fmi.getMediaStorageSOPInstanceUID());
        assertEquals(CLASS_UID, fmi.getMediaStorageSOPClassUID());
        assertEquals(TS_UID, fmi.getTransferSyntaxUID());
        assertEquals(FILE_SET_ID, fsi.getString(Tags.FileSetID));
        assertEquals(0,fsi.getInt(Tags.FileSetConsistencyFlag, -1));
    }
    
    private static final String CHARSET = "ISO_IR 100";
    private static final String PAT1_ID = "P1234";
    private static final String PAT2_ID = "P5678";
    private static final String PAT3_ID = "P9999";
    private static final String PAT1_NAME = "PAT1^NAME";
    private static final String PAT2_NAME = "PAT2^NAME";
    private static final String PAT3_NAME = "PAT3^NAME";
    private static final String STUDY1_ID = "S1111";
    private static final String STUDY2_ID = "S2222";
    private static final String STUDY1_UID = "1.2.40.0.13.1.1.99.1111";
    private static final String STUDY2_UID = "1.2.40.0.13.1.1.99.2222";
    private static final String STUDY1_DESC = "STUDY1_DESC";
    private static final String STUDY2_DESC = "STUDY2_DESC";
    private static final String ACC_NO = "A7777";
    private static final int SERIES1_NO = 1;
    private static final int SERIES2_NO = 2;
    private static final int SERIES3_NO = 3;
    private static final String SERIES1_UID = "1.2.40.0.13.1.1.99.1111.1";
    private static final String SERIES2_UID = "1.2.40.0.13.1.1.99.1111.2";
    private static final String SERIES3_UID = "1.2.40.0.13.1.1.99.1111.3";
    private static final int IMG1_NO = 1;
    private static final int IMG2_NO = 2;
    private static final String IMG1_UID = "1.2.40.0.13.1.1.99.1111.1.1";
    private static final String IMG2_UID = "1.2.40.0.13.1.1.99.1111.1.2";
    private static final String CT_UID = "1.2.840.10008.5.1.4.1.1.2";

    private final DirBuilderFactory wfact = DirBuilderFactory.getInstance();
    private final DcmObjectFactory dsfact = DcmObjectFactory.getInstance();
    private Dataset newPatient(String id, String name) throws Exception {
        Dataset pat = dsfact.newDataset();
        pat.putCS(Tags.SpecificCharacterSet, CHARSET);
        pat.putLO(Tags.PatientID, id);
        pat.putPN(Tags.PatientName, name);
        return pat;
    }        
    
    private void checkPatient(Dataset pat, String id, String name)
            throws Exception {
        assertEquals(CHARSET, pat.getString(Tags.SpecificCharacterSet, null));
        assertEquals(id, pat.getString(Tags.PatientID, null));
        assertEquals(name, pat.getString(Tags.PatientName, null));
    }        

    private Dataset newStudy(String id, String uid, Date date, String desc,
            String accNo) throws Exception {
        Dataset study = dsfact.newDataset();
        study.putCS(Tags.SpecificCharacterSet, CHARSET);
        study.putSH(Tags.StudyID, id);
        study.putUI(Tags.StudyInstanceUID, uid);
        study.putLO(Tags.StudyDescription, desc);
        study.putSH(Tags.AccessionNumber, accNo);
        study.putDA(Tags.StudyDate, date);
        study.putTM(Tags.StudyTime, date);
        return study;
    }        

    private void checkStudy(Dataset study, String id, String uid, String desc,
            String accNo) throws Exception {
        assertEquals(CHARSET, study.getString(Tags.SpecificCharacterSet, null));
        assertEquals(id, study.getString(Tags.StudyID, null));
        assertEquals(uid, study.getString(Tags.StudyInstanceUID, null));
        assertEquals(desc, study.getString(Tags.StudyDescription, null));
        assertEquals(accNo, study.getString(Tags.AccessionNumber, null));
    }        

    private Dataset newSeries(String md, int no, String uid)
            throws Exception {
        Dataset series = dsfact.newDataset();
        series.putCS(Tags.Modality, md);
        series.putIS(Tags.SeriesNumber, no);
        series.putUI(Tags.SeriesInstanceUID, uid);
        return series;
    }
    
    private void checkSeries(Dataset series, String md, int no, String uid)
            throws Exception {
        assertEquals(md, series.getString(Tags.Modality, null));
        assertEquals(no, series.getInt(Tags.SeriesNumber, -1));
        assertEquals(uid, series.getString(Tags.SeriesInstanceUID, null));
    }
    
    private Dataset newImage(int no)
            throws Exception {
        Dataset img = dsfact.newDataset();
        img.putIS(Tags.InstanceNumber, no);
        return img;
    }
    
    private void checkImage(Dataset img, int no)
            throws Exception {
        assertEquals(no, img.getInt(Tags.InstanceNumber, -1));
    }

    public void testAdd_WithGroupLen_DefSeqLen_DefItemLen() throws Exception {
        doTestAdd(false, false, false);
    }

    public void testAdd_WithGroupLen_DefSeqLen_UndefItemLen() throws Exception {
        doTestAdd(false, false, true);
    }

    public void testAdd_WithGroupLen_UndefSeqLen_DefItemLen() throws Exception {
        doTestAdd(false, true, false);
    }

    public void testAdd_WithGroupLen_UndefSeqLen_UndefItemLen() throws Exception {
        doTestAdd(false, true, true);
    }

    public void testAdd_SkipGroupLen_DefSeqLen_DefItemLen() throws Exception {
        doTestAdd(true, false, false);
    }

    public void testAdd_SkipGroupLen_DefSeqLen_UndefItemLen() throws Exception {
        doTestAdd(true, false, true);
    }

    public void testAdd_SkipGroupLen_UndefSeqLen_DefItemLen() throws Exception {
        doTestAdd(true, true, false);
    }

    public void testAdd_SkipGroupLen_UndefSeqLen_UndefItemLen() throws Exception {
        doTestAdd(true, true, true);
    }


    private void doTestAdd(boolean skipGroupLen, boolean undefSeqLen,
            boolean undefItemLen) throws Exception {
        DcmEncodeParam encParam = new DcmEncodeParam(ByteOrder.LITTLE_ENDIAN,
                true, false, false, skipGroupLen, undefSeqLen, undefItemLen);
        DirWriter w1 = wfact.newDirWriter(theFile, INST_UID,  FILE_SET_ID,
                null, null, encParam);
        try {
            DirRecord patRec1 = w1.add(null, "PATIENT",
                    newPatient(PAT1_ID, PAT1_NAME));
            DirRecord studyRec1 = w1.add(patRec1, "STUDY",
                    newStudy(STUDY1_ID, STUDY1_UID, new Date(), STUDY1_DESC,
                            ACC_NO));
            DirRecord seriesRec1 = w1.add(studyRec1, "SERIES",
                    newSeries("CT", SERIES1_NO, SERIES1_UID));
            DirRecord imgRec1 = w1.add(seriesRec1, "IMAGE",
                    newImage(IMG1_NO),  w1.toFileIDs(IMG1_FILE), CT_UID,
                    IMG1_UID, TS_UID);
            DirRecord seriesRec2 = w1.add(studyRec1, "SERIES",
                    newSeries("CT", SERIES2_NO, SERIES2_UID));
            DirRecord imgRec2 = w1.add(seriesRec2, "IMAGE",
                    newImage(IMG2_NO), w1.toFileIDs(IMG2_FILE), CT_UID,
                    IMG2_UID,  TS_UID);
            DirRecord patRec2 = w1.add(null, "PATIENT",
                    newPatient(PAT2_ID, PAT2_NAME));
        } finally {
            w1.close();
        }
        DirWriter w2 = wfact.newDirWriter(theFile, encParam);
        try {
            DirRecord patRec1 = w2.getFirstRecord(false);
            DirRecord patRec2 = patRec1.getNextSibling(false);
            DirRecord studyRec1 = patRec1.getFirstChild(false);
            Dataset study2 =
                    newStudy(STUDY2_ID, STUDY2_UID, new Date(), STUDY2_DESC,
                            ACC_NO);
            Dataset series3 = newSeries("MR", SERIES3_NO, SERIES3_UID);
            w2.add(null, "PATIENT", newPatient(PAT3_ID, PAT3_NAME));
            w2.add(patRec2, "STUDY", study2);
            w2.add(studyRec1, "SERIES", series3);
            w2.rollback();
            DirRecord studyRec2 = w2.add(patRec1, "STUDY", study2);
            w2.add(studyRec2, "SERIES", series3);
       } finally {
            w2.close();
        }
        DirWriter w3 = wfact.newDirWriter(theFile, null);
        try {
            checkFilesetIDs(w3);
            DirRecord patRec1 = w3.getFirstRecord(false);
            assertNotNull(patRec1);
            assertEquals("PATIENT",patRec1.getType());
            assertEquals(DirRecord.IN_USE,patRec1.getInUseFlag());
            checkPatient(patRec1.getDataset(), PAT1_ID, PAT1_NAME);
            DirRecord studyRec1 = patRec1.getFirstChild(false);
            assertNotNull(studyRec1);
            assertEquals("STUDY",studyRec1.getType());
            assertEquals(DirRecord.IN_USE,studyRec1.getInUseFlag());
            checkStudy(studyRec1.getDataset(), STUDY1_ID, STUDY1_UID,
                    STUDY1_DESC, ACC_NO);
            DirRecord seriesRec1 = studyRec1.getFirstChild(false);
            assertNotNull(seriesRec1);
            assertEquals("SERIES",seriesRec1.getType());
            assertEquals(DirRecord.IN_USE,seriesRec1.getInUseFlag());
            checkSeries(seriesRec1.getDataset(), "CT", SERIES1_NO, SERIES1_UID);
            DirRecord imageRec1 = seriesRec1.getFirstChild(false);
            assertNotNull(imageRec1);
            assertEquals("IMAGE",imageRec1.getType());
            assertEquals(DirRecord.IN_USE,imageRec1.getInUseFlag());
            assertEquals(IMG1_FILE,w3.getRefFile(imageRec1.getRefFileIDs()));
            assertEquals(CT_UID,imageRec1.getRefSOPClassUID());
            assertEquals(IMG1_UID,imageRec1.getRefSOPInstanceUID());
            assertEquals(TS_UID,imageRec1.getRefSOPTransferSyntaxUID());
            checkImage(imageRec1.getDataset(), IMG1_NO);
            assertNull(imageRec1.getNextSibling(false));
            DirRecord seriesRec2 = seriesRec1.getNextSibling(false);
            assertNotNull(seriesRec2);
            assertEquals("SERIES",seriesRec2.getType());
            assertEquals(DirRecord.IN_USE,seriesRec2.getInUseFlag());
            checkSeries(seriesRec2.getDataset(), "CT", SERIES2_NO, SERIES2_UID);
            DirRecord imageRec2 = seriesRec2.getFirstChild(false);
            assertNotNull(imageRec2);
            assertEquals("IMAGE",imageRec2.getType());
            assertEquals(DirRecord.IN_USE,imageRec2.getInUseFlag());
            assertEquals(IMG2_FILE,w3.getRefFile(imageRec2.getRefFileIDs()));
            assertEquals(CT_UID,imageRec2.getRefSOPClassUID());
            assertEquals(IMG2_UID,imageRec2.getRefSOPInstanceUID());
            assertEquals(TS_UID,imageRec2.getRefSOPTransferSyntaxUID());
            checkImage(imageRec2.getDataset(), IMG2_NO);
            assertNull(imageRec2.getNextSibling(false));
            assertNull(seriesRec2.getNextSibling(false));
            DirRecord studyRec2 = studyRec1.getNextSibling(false);
            assertNotNull(studyRec2);
            assertEquals("STUDY",studyRec2.getType());
            assertEquals(DirRecord.IN_USE,studyRec2.getInUseFlag());
            checkStudy(studyRec2.getDataset(), STUDY2_ID, STUDY2_UID,
                    STUDY2_DESC, ACC_NO);
            DirRecord seriesRec3 = studyRec2.getFirstChild(false);
            assertNotNull(seriesRec3);
            assertEquals("SERIES",seriesRec3.getType());
            assertEquals(DirRecord.IN_USE,seriesRec3.getInUseFlag());
            checkSeries(seriesRec3.getDataset(), "MR", SERIES3_NO, SERIES3_UID);
            assertNull(seriesRec3.getFirstChild(false));
            assertNull(seriesRec3.getNextSibling(false));
            assertNull(studyRec2.getNextSibling(false));
            DirRecord patRec2 = patRec1.getNextSibling(false);
            assertNotNull(patRec2);
            assertEquals("PATIENT",patRec2.getType());
            assertEquals(DirRecord.IN_USE,patRec2.getInUseFlag());
            checkPatient(patRec2.getDataset(), PAT2_ID, PAT2_NAME);
            assertNull(patRec2.getFirstChild(false));
            assertNull(patRec2.getNextSibling(false));
        } finally {
            w3.close();
        }
    }
    
    public void testRemove_WithGroupLen_DefSeqLen_DefItemLen() throws Exception {
        doTestRemove(false, false, false);
    }

    public void testRemove_WithGroupLen_DefSeqLen_UndefItemLen() throws Exception {
        doTestRemove(false, false, true);
    }

    public void testRemove_WithGroupLen_UndefSeqLen_DefItemLen() throws Exception {
        doTestRemove(false, true, false);
    }

    public void testRemove_WithGroupLen_UndefSeqLen_UndefItemLen() throws Exception {
        doTestRemove(false, true, true);
    }

    public void testRemove_SkipGroupLen_DefSeqLen_DefItemLen() throws Exception {
        doTestRemove(true, false, false);
    }

    public void testRemove_SkipGroupLen_DefSeqLen_UndefItemLen() throws Exception {
        doTestRemove(true, false, true);
    }

    public void testRemove_SkipGroupLen_UndefSeqLen_DefItemLen() throws Exception {
        doTestRemove(true, true, false);
    }

    public void testRemove_SkipGroupLen_UndefSeqLen_UndefItemLen() throws Exception {
        doTestRemove(true, true, true);
    }

    private void doTestRemove(boolean skipGroupLen, boolean undefSeqLen,
            boolean undefItemLen) throws Exception {
        DcmEncodeParam encParam = new DcmEncodeParam(ByteOrder.LITTLE_ENDIAN,
                true, false, false, skipGroupLen, undefSeqLen, undefItemLen);
        DirWriter w1 = wfact.newDirWriter(theFile, INST_UID,  FILE_SET_ID,
                null, null, encParam);
        try {
            DirRecord patRec1 = w1.add(null, "PATIENT",
                    newPatient(PAT1_ID, PAT1_NAME));
            DirRecord studyRec1 = w1.add(patRec1, "STUDY",
                    newStudy(STUDY1_ID, STUDY1_UID, new Date(), STUDY1_DESC,
                            ACC_NO));
            DirRecord seriesRec1 = w1.add(studyRec1, "SERIES",
                    newSeries("CT", SERIES1_NO, SERIES1_UID));
            DirRecord imgRec1 = w1.add(seriesRec1, "IMAGE",
                    newImage(IMG1_NO),  w1.toFileIDs(IMG1_FILE), CT_UID,
                    IMG1_UID, TS_UID);
            DirRecord seriesRec2 = w1.add(studyRec1, "SERIES",
                    newSeries("CT", SERIES2_NO, SERIES2_UID));
            DirRecord imgRec2 = w1.add(seriesRec2, "IMAGE",
                    newImage(IMG2_NO), w1.toFileIDs(IMG2_FILE), CT_UID,
                    IMG2_UID,  TS_UID);
            DirRecord patRec2 = w1.add(null, "PATIENT",
                    newPatient(PAT2_ID, PAT2_NAME));
        } finally {
            w1.close();
        }
        DirWriter w2 = wfact.newDirWriter(theFile, encParam);
        try {
            DirRecord patRec1 = w2.getFirstRecord(false);
            DirRecord studyRec1 = patRec1.getFirstChild(false);
            DirRecord seriesRec1 = studyRec1.getFirstChild(false);
            assertEquals(2, w2.remove(seriesRec1));
       } finally {
            w2.close();
        }
        DirWriter w4, w3 = wfact.newDirWriter(theFile, encParam);
        try {
            checkFilesetIDs(w3);
            DirRecord patRec1 = w3.getFirstRecord(false);
            assertNotNull(patRec1);
            assertEquals("PATIENT",patRec1.getType());
            assertEquals(DirRecord.IN_USE,patRec1.getInUseFlag());
            DirRecord studyRec1 = patRec1.getFirstChild(false);
            assertNotNull(studyRec1);
            assertEquals("STUDY",studyRec1.getType());
            assertEquals(DirRecord.IN_USE,studyRec1.getInUseFlag());
            DirRecord seriesRec1 = studyRec1.getFirstChild(false);
            assertNotNull(seriesRec1);
            assertEquals("SERIES",seriesRec1.getType());
            assertEquals(DirRecord.INACTIVE,seriesRec1.getInUseFlag());
            DirRecord imageRec1 = seriesRec1.getFirstChild(false);
            assertNotNull(imageRec1);
            assertEquals("IMAGE",imageRec1.getType());
            assertEquals(DirRecord.INACTIVE,imageRec1.getInUseFlag());
            DirRecord seriesRec2 = studyRec1.getFirstChild(true);
            assertNotNull(seriesRec2);
            assertEquals("SERIES",seriesRec2.getType());
            assertEquals(DirRecord.IN_USE,seriesRec2.getInUseFlag());
            checkSeries(seriesRec2.getDataset(), "CT", SERIES2_NO, SERIES2_UID);
            DirRecord patRec2 = patRec1.getNextSibling(false);
            assertNotNull(patRec2);
            assertEquals("PATIENT",patRec2.getType());
            assertEquals(DirRecord.IN_USE,patRec2.getInUseFlag());
            checkPatient(patRec2.getDataset(), PAT2_ID, PAT2_NAME);
            assertNull(patRec2.getFirstChild(false));
            assertNull(patRec2.getNextSibling(false));
            w4 = w3.compact();
            w3 = null;
        } finally {
            if (w3 != null) {
                try { w3.close(); } catch (IOException ignore) {}
            }
        }
        try {
            checkFilesetIDs(w4);
            DirRecord patRec1 = w4.getFirstRecord(false);
            assertNotNull(patRec1);
            assertEquals("PATIENT",patRec1.getType());
            assertEquals(DirRecord.IN_USE,patRec1.getInUseFlag());
            DirRecord studyRec1 = patRec1.getFirstChild(false);
            assertNotNull(studyRec1);
            assertEquals("STUDY",studyRec1.getType());
            assertEquals(DirRecord.IN_USE,studyRec1.getInUseFlag());
            DirRecord seriesRec2 = studyRec1.getFirstChild(false);
            assertNotNull(seriesRec2);
            assertEquals("SERIES",seriesRec2.getType());
            assertEquals(DirRecord.IN_USE,seriesRec2.getInUseFlag());
            checkSeries(seriesRec2.getDataset(), "CT", SERIES2_NO, SERIES2_UID);
            DirRecord imageRec2 = seriesRec2.getFirstChild(false);
            assertNotNull(imageRec2);
            assertEquals("IMAGE",imageRec2.getType());
            assertEquals(DirRecord.IN_USE,imageRec2.getInUseFlag());
            assertEquals(IMG2_UID,imageRec2.getRefSOPInstanceUID());
            assertNull(imageRec2.getNextSibling(false));
            assertNull(seriesRec2.getNextSibling(false));
            DirRecord patRec2 = patRec1.getNextSibling(false);
            assertNotNull(patRec2);
            assertEquals("PATIENT",patRec2.getType());
            assertEquals(DirRecord.IN_USE,patRec2.getInUseFlag());
            checkPatient(patRec2.getDataset(), PAT2_ID, PAT2_NAME);
            assertNull(patRec2.getFirstChild(false));
            assertNull(patRec2.getNextSibling(false));
        } finally {
            w4.close();
        }
    }
    
    public void testReplace_WithGroupLen_DefSeqLen_DefItemLen() throws Exception {
        doTestReplace(false, false, false);
    }

    public void testReplace_WithGroupLen_DefSeqLen_UndefItemLen() throws Exception {
        doTestReplace(false, false, true);
    }

    public void testReplace_WithGroupLen_UndefSeqLen_DefItemLen() throws Exception {
        doTestReplace(false, true, false);
    }

    public void testReplace_WithGroupLen_UndefSeqLen_UndefItemLen() throws Exception {
        doTestReplace(false, true, true);
    }

    public void testReplace_SkipGroupLen_DefSeqLen_DefItemLen() throws Exception {
        doTestReplace(true, false, false);
    }

    public void testReplace_SkipGroupLen_DefSeqLen_UndefItemLen() throws Exception {
        doTestReplace(true, false, true);
    }

    public void testReplace_SkipGroupLen_UndefSeqLen_DefItemLen() throws Exception {
        doTestReplace(true, true, false);
    }

    public void testReplace_SkipGroupLen_UndefSeqLen_UndefItemLen() throws Exception {
        doTestReplace(true, true, true);
    }

    private void doTestReplace(boolean skipGroupLen, boolean undefSeqLen,
            boolean undefItemLen) throws Exception {
        DcmEncodeParam encParam = new DcmEncodeParam(ByteOrder.LITTLE_ENDIAN,
                true, false, false, skipGroupLen, undefSeqLen, undefItemLen);
        DirWriter w1 = wfact.newDirWriter(theFile, INST_UID,  FILE_SET_ID,
                null, null, encParam);
        try {
            DirRecord patRec1 = w1.add(null, "PATIENT",
                    newPatient(PAT1_ID, PAT1_NAME));
            DirRecord studyRec1 = w1.add(patRec1, "STUDY",
                    newStudy(STUDY1_ID, STUDY1_UID, new Date(), STUDY1_DESC,
                            ACC_NO));
            DirRecord seriesRec1 = w1.add(studyRec1, "SERIES",
                    newSeries("CT", SERIES1_NO, SERIES1_UID));
            DirRecord imgRec1 = w1.add(seriesRec1, "IMAGE",
                    newImage(IMG1_NO),  w1.toFileIDs(IMG1_FILE), CT_UID,
                    IMG1_UID, TS_UID);
            DirRecord seriesRec2 = w1.add(studyRec1, "SERIES",
                    newSeries("CT", SERIES2_NO, SERIES2_UID));
            DirRecord imgRec2 = w1.add(seriesRec2, "IMAGE",
                    newImage(IMG2_NO), w1.toFileIDs(IMG2_FILE), CT_UID,
                    IMG2_UID,  TS_UID);
            DirRecord patRec2 = w1.add(null, "PATIENT",
                    newPatient(PAT2_ID, PAT2_NAME));
        } finally {
            w1.close();
        }
        DirWriter w2 = wfact.newDirWriter(theFile, encParam);
        try {
            DirRecord patRec1 = w2.getFirstRecord(false);
            DirRecord studyRec1 = patRec1.getFirstChild(false);
            DirRecord seriesRec1 = studyRec1.getFirstChild(false);
            DirRecord seriesRec3 = w2.replace(seriesRec1, "SERIES",
                    newSeries("MR", SERIES3_NO, SERIES3_UID));
            DirRecord imageRec1 = seriesRec3.getFirstChild(false);
            assertNotNull(imageRec1);
            assertEquals("IMAGE",imageRec1.getType());
            assertEquals(DirRecord.IN_USE,imageRec1.getInUseFlag());
            assertEquals(IMG1_FILE,w2.getRefFile(imageRec1.getRefFileIDs()));
            assertEquals(CT_UID,imageRec1.getRefSOPClassUID());
            assertEquals(IMG1_UID,imageRec1.getRefSOPInstanceUID());
            assertEquals(TS_UID,imageRec1.getRefSOPTransferSyntaxUID());
            checkImage(imageRec1.getDataset(), IMG1_NO);
            assertNull(imageRec1.getNextSibling(false));
            DirRecord patRec2 = patRec1.getNextSibling(false);
            assertNotNull(patRec2);
            assertEquals("PATIENT",patRec2.getType());
            assertEquals(DirRecord.IN_USE,patRec2.getInUseFlag());
            checkPatient(patRec2.getDataset(), PAT2_ID, PAT2_NAME);
            assertNull(patRec2.getFirstChild(false));
            assertNull(patRec2.getNextSibling(false));
       } finally {
            w2.close();
        }
        DirWriter w4, w3 = wfact.newDirWriter(theFile, encParam);
        try {
            checkFilesetIDs(w3);
            DirRecord patRec1 = w3.getFirstRecord(false);
            assertNotNull(patRec1);
            assertEquals("PATIENT",patRec1.getType());
            assertEquals(DirRecord.IN_USE,patRec1.getInUseFlag());
            DirRecord studyRec1 = patRec1.getFirstChild(false);
            assertNotNull(studyRec1);
            assertEquals("STUDY",studyRec1.getType());
            assertEquals(DirRecord.IN_USE,studyRec1.getInUseFlag());
            DirRecord seriesRec1 = studyRec1.getFirstChild(false);
            assertNotNull(seriesRec1);
            assertEquals("SERIES",seriesRec1.getType());
            assertEquals(DirRecord.INACTIVE,seriesRec1.getInUseFlag());
            DirRecord seriesRec2 = seriesRec1.getNextSibling(false);
            assertNotNull(seriesRec2);
            assertEquals("SERIES",seriesRec2.getType());
            assertEquals(DirRecord.IN_USE,seriesRec2.getInUseFlag());
            checkSeries(seriesRec2.getDataset(), "CT", SERIES2_NO, SERIES2_UID);
            DirRecord seriesRec3 = seriesRec2.getNextSibling(false);
            assertNotNull(seriesRec3);
            assertEquals("SERIES",seriesRec3.getType());
            assertEquals(DirRecord.IN_USE,seriesRec3.getInUseFlag());
            checkSeries(seriesRec3.getDataset(), "MR", SERIES3_NO, SERIES3_UID);
            assertNull(seriesRec3.getNextSibling(false));
            DirRecord patRec2 = patRec1.getNextSibling(false);
            assertNotNull(patRec2);
            assertEquals("PATIENT",patRec2.getType());
            assertEquals(DirRecord.IN_USE,patRec2.getInUseFlag());
            checkPatient(patRec2.getDataset(), PAT2_ID, PAT2_NAME);
            assertNull(patRec2.getFirstChild(false));
            assertNull(patRec2.getNextSibling(false));
            w4 = w3.compact();
            w3 = null;
        } finally {
            if (w3 != null) {
                try { w3.close(); } catch (IOException ignore) {}
            }
        }
        try {
            checkFilesetIDs(w4);
            DirRecord patRec1 = w4.getFirstRecord(false);
            assertNotNull(patRec1);
            assertEquals("PATIENT",patRec1.getType());
            assertEquals(DirRecord.IN_USE,patRec1.getInUseFlag());
            DirRecord studyRec1 = patRec1.getFirstChild(false);
            assertNotNull(studyRec1);
            assertEquals("STUDY",studyRec1.getType());
            assertEquals(DirRecord.IN_USE,studyRec1.getInUseFlag());
            DirRecord seriesRec2 = studyRec1.getFirstChild(false);
            assertNotNull(seriesRec2);
            assertEquals("SERIES",seriesRec2.getType());
            assertEquals(DirRecord.IN_USE,seriesRec2.getInUseFlag());
            checkSeries(seriesRec2.getDataset(), "CT", SERIES2_NO, SERIES2_UID);
            DirRecord imageRec2 = seriesRec2.getFirstChild(false);
            assertNotNull(imageRec2);
            assertEquals("IMAGE",imageRec2.getType());
            assertEquals(DirRecord.IN_USE,imageRec2.getInUseFlag());
            assertEquals(IMG2_UID,imageRec2.getRefSOPInstanceUID());
            assertNull(imageRec2.getNextSibling(false));
            DirRecord seriesRec3 = seriesRec2.getNextSibling(false);
            assertNotNull(seriesRec3);
            assertEquals("SERIES",seriesRec3.getType());
            assertEquals(DirRecord.IN_USE,seriesRec3.getInUseFlag());
            checkSeries(seriesRec3.getDataset(), "MR", SERIES3_NO, SERIES3_UID);
            assertNull(seriesRec3.getNextSibling(false));
            DirRecord imageRec1 = seriesRec3.getFirstChild(false);
            assertNotNull(imageRec1);
            assertEquals("IMAGE",imageRec1.getType());
            assertEquals(DirRecord.IN_USE,imageRec1.getInUseFlag());
            assertEquals(IMG1_UID,imageRec1.getRefSOPInstanceUID());
            assertNull(imageRec1.getNextSibling(false));
            DirRecord patRec2 = patRec1.getNextSibling(false);
            assertNotNull(patRec2);
            assertEquals("PATIENT",patRec2.getType());
            assertEquals(DirRecord.IN_USE,patRec2.getInUseFlag());
            checkPatient(patRec2.getDataset(), PAT2_ID, PAT2_NAME);
            assertNull(patRec2.getFirstChild(false));
            assertNull(patRec2.getNextSibling(false));
        } finally {
            w4.close();
        }
    }
}//end class DirReaderTest
