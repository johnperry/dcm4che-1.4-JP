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

package org.dcm4che.data;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

/**
 *@author     gunter.zeilinger@tiani.com
 *@created    March 3, 2003
 */
public class DatasetTest extends TestCase {

    private final static File OUT_FILE = new File("data/TMP_TEST");
    private final static String EVR_LE = "data/examplef9.dcm";
    private final static String EVR_LE_XML = "data/examplef9.xml";
    private final static String DICOMDIR = "data/DICOMDIR";
    private final static String DICOMDIR_XML = "data/DICOMDIR.xml";
    private final static String PART10_EVR_LE = "data/6AF8_10";
    private Dataset ds;
    private TagDictionary dict;


    /**
     *  Constructor for the DatasetTest object
     *
     *@param  testName  Description of the Parameter
     */
    public DatasetTest(java.lang.String testName) {
        super(testName);
    }


    /**
     *  The main program for the DatasetTest class
     *
     *@param  args  The command line arguments
     */
    public static void main(java.lang.String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        junit.textui.TestRunner.run(suite());
    }


    /**
     *  A unit test suite for JUnit
     *
     *@return    The test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(DatasetTest.class);
        return suite;
    }


    /**
     *  The JUnit setup method
     *
     *@exception  Exception  Description of the Exception
     */
    protected void setUp() throws Exception {
        dict = DictionaryFactory.getInstance().getDefaultTagDictionary();
        ds = DcmObjectFactory.getInstance().newDataset();
    }


    /**
     *  The teardown method for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    protected void tearDown() throws Exception {
        if (OUT_FILE.exists()) {
            OUT_FILE.delete();
        }
    }


    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testReadEVR_LE() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(EVR_LE)));
        try {
            ds.readFile(in, null, -1);
        } finally {
            try {
                in.close();
            } catch (Exception ignore) {}
        }
    }

    public void testWriteDeflated() throws Exception {
    	testWriteDeflated(false);
    }
    
    public void testWriteDeflatedWithoutPreamble() throws Exception {
    	testWriteDeflated(true);
    }
    
    private void testWriteDeflated(boolean skipPreamble) throws Exception {
		testReadEVR_LE();
		DcmObjectFactory dof = DcmObjectFactory.getInstance();
		FileMetaInfo fmi = dof.newFileMetaInfo(ds, UIDs.DeflatedExplicitVRLittleEndian);
		if (skipPreamble) {
			fmi.setPreamble(null);
		}
		ds.setFileMetaInfo(fmi);
		ds.writeFile(OUT_FILE, DcmEncodeParam.DEFL_EVR_LE);
		ds.readFile(OUT_FILE, null, -1);
	};
		

    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testReadDICOMDIR() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(DICOMDIR)));
        try {
            ds.readFile(in, null, -1);
        } finally {
            try {
                in.close();
            } catch (Exception ignore) {}
        }
    }


    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testReadPART10_EVR_LE() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(PART10_EVR_LE)));
        try {
            ds.readFile(in, null, -1);
        } finally {
            try {
                in.close();
            } catch (Exception ignore) {}
        }
    }


    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testWriteEVR_LEasXML() throws Exception {
        testReadEVR_LE();
        SAXTransformerFactory tf =
                (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult(new StreamResult(OUT_FILE));
        ds.writeFile(th, dict);
    }


    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testWriteDICOMDIRasXML() throws Exception {
        testReadDICOMDIR();
        SAXTransformerFactory tf =
                (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult(new StreamResult(OUT_FILE));
        ds.writeFile(th, dict);
    }

    public void testInternalize() throws Exception {
        testReadDICOMDIR();
        ds.shareElements();
    }

    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testSAXHandlerEVR_LE() throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        p.parse(new File(EVR_LE_XML), ds.getSAXHandler());
    }


    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testSAXHandlerDICOMDIR() throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        p.parse(new File(DICOMDIR_XML), ds.getSAXHandler());
    }


    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testDumpDataset() throws Exception {
        testReadEVR_LE();
        ds.dumpDataset(new FileWriter(OUT_FILE), null);
    }


    private final static String[] SCHEDULED_STATION_AET = {"AET1", "AET2"};
    private final static String[] PATIENT_AGE = {"040Y"};
    private final static String[] IMAGE_TYPE = {"ORIGINAL", "PRIMARY", "", "OTHER"};
    private final static String[] STUDY_DATE = {"19700101"};
    private final static String[] STUDY_TIME = {"010140"};
    private final static String[] ACQUISITION_DATETIME = {"19700101010140"};
    private final static String[] IMAGE_POSITION = {"1.23E+02", "-456", "78.9"};
    private final static String[] ANCHOR_POINT = {
            String.valueOf(123.4f), String.valueOf(-56.78f)
            };
    private final static String[] TABLE_OF_Y_BREAK_POINTS = {
            String.valueOf(1.2345), String.valueOf(-6.78901)
            };
    private final static String[] REF_FRAME_NUMBER = {"3", "7", "13"};
    private final static String[] OTHER_PATIENT_IDS = {"PAT_ID1", "PAT_ID2"};
    private final static String[] ADDITIONAL_PATIENT_HISTORY = {
            "ADDITIONAL PATIENT HISTORY"
            };
    private final static String[] OTHER_PATIENT_NAMES = {
            "PAT1^NAME", "PAT2^NAME"
            };
    private final static String[] ACCESSION_NUMBER = {
            "A-23456"
            };
    private final static String[] DISPLAYED_AREA_BRHC = {
            String.valueOf(123000), String.valueOf(-456000)
            };
    private final static String[] OVERLAY_ORIGIN = {
            String.valueOf(123), String.valueOf(-456)
            };
    private final static String[] DERIVATION_DESCRIPTION = {
            "Derivation Description"
            };
    private final static String[] SOP_CLASSES_SUPPORTED = {
            "1.2.840.10008.5.1.1.14", "1.2.840.10008.5.1.1.16"
            };
    private final static String[] REF_SAMPLE_POSITIONS = {
            String.valueOf(123000), String.valueOf(456000)
            };
    private final static String[] TEXT_VALUE = {
            "Text Value"
            };
    private static final String UMLAUT = "\u00dcml\u00e4ut";
    private static final String[] UMLAUTS = { UMLAUT, UMLAUT, UMLAUT };


    /**
     *  A unit test for JUnit
     */
    public void testCalcLength8Groups() {
        ds.putLO(0x00090010, "TIANI");
        ds.putLO(0x00110010, "TIANI");
        ds.putLO(0x00130010, "TIANI");
        ds.putLO(0x00150010, "TIANI");
        ds.putLO(0x00170010, "TIANI");
        ds.putLO(0x00190010, "TIANI");
        ds.putLO(0x00210010, "TIANI");
        ds.putLO(0x00230010, "TIANI");
        assertEquals(208, ds.calcLength(DcmDecodeParam.IVR_LE));
    }


    /**
     *  Description of the Method
     */
    private void putStrings() {
        ds.putAE(Tags.ScheduledStationAET, SCHEDULED_STATION_AET);
        ds.putAS(Tags.PatientAge, PATIENT_AGE);
        ds.putCS(Tags.ImageType, IMAGE_TYPE);
        ds.putDA(Tags.StudyDate, STUDY_DATE);
        ds.putDS(Tags.ImagePosition, IMAGE_POSITION);
        ds.putDT(Tags.AcquisitionDatetime, ACQUISITION_DATETIME);
        ds.putFL(Tags.AnchorPoint, ANCHOR_POINT);
        ds.putFD(Tags.TableOfYBreakPoints, TABLE_OF_Y_BREAK_POINTS);
        ds.putIS(Tags.RefFrameNumber, REF_FRAME_NUMBER);
        ds.putLO(Tags.OtherPatientIDs, OTHER_PATIENT_IDS);
        ds.putLT(Tags.AdditionalPatientHistory, ADDITIONAL_PATIENT_HISTORY);
        ds.putPN(Tags.OtherPatientNames, OTHER_PATIENT_NAMES);
        ds.putSH(Tags.AccessionNumber, ACCESSION_NUMBER);
        ds.putSL(Tags.DisplayedAreaBottomRightHandCorner, DISPLAYED_AREA_BRHC);
        ds.putSS(Tags.OverlayOrigin, OVERLAY_ORIGIN);
        ds.putST(Tags.DerivationDescription, DERIVATION_DESCRIPTION);
        ds.putTM(Tags.StudyTime, STUDY_TIME);
        ds.putUI(Tags.SOPClassesSupported, SOP_CLASSES_SUPPORTED);
        ds.putUL(Tags.RefSamplePositions, REF_SAMPLE_POSITIONS);
        ds.putUS(Tags.RefFrameNumbers, REF_FRAME_NUMBER);
        ds.putUT(Tags.TextValue, TEXT_VALUE);
    }


    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testGetString() throws Exception {
        putStrings();
        Dataset ds2 = DcmObjectFactory.getInstance().newDataset();
        ds2.putAll(ds);
        ds = ds2;
        assertEquals(SCHEDULED_STATION_AET, ds, Tags.ScheduledStationAET);
        assertEquals(PATIENT_AGE, ds, Tags.PatientAge);
        assertEquals(IMAGE_TYPE, ds, Tags.ImageType);
        assertEquals(STUDY_DATE, ds, Tags.StudyDate);
        assertEquals(STUDY_TIME[0], ds.getString(Tags.StudyTime).substring(0, 6));
        assertEquals(ACQUISITION_DATETIME,
                ds, Tags.AcquisitionDatetime);
        assertEquals(IMAGE_POSITION, ds, Tags.ImagePosition);
        assertEquals(ANCHOR_POINT, ds, Tags.AnchorPoint);
        assertEquals(TABLE_OF_Y_BREAK_POINTS,
                ds, Tags.TableOfYBreakPoints);
        assertEquals(REF_FRAME_NUMBER, ds, Tags.RefFrameNumber);
        assertEquals(OTHER_PATIENT_IDS, ds, Tags.OtherPatientIDs);
        assertEquals(ADDITIONAL_PATIENT_HISTORY,
                ds, Tags.AdditionalPatientHistory);
        assertEquals(OTHER_PATIENT_NAMES,
                ds, Tags.OtherPatientNames);
        assertEquals(ACCESSION_NUMBER, ds, Tags.AccessionNumber);
        assertEquals(DISPLAYED_AREA_BRHC,
                ds, Tags.DisplayedAreaBottomRightHandCorner);
        assertEquals(OVERLAY_ORIGIN, ds, Tags.OverlayOrigin);
        assertEquals(DERIVATION_DESCRIPTION,
                ds, Tags.DerivationDescription);
        assertEquals(SOP_CLASSES_SUPPORTED,
                ds, Tags.SOPClassesSupported);
        assertEquals(REF_SAMPLE_POSITIONS,
                ds, Tags.RefSamplePositions);
        assertEquals(REF_FRAME_NUMBER, ds, Tags.RefFrameNumbers);
        assertEquals(TEXT_VALUE, ds, Tags.TextValue);
    }


    /**
     *  Description of the Method
     *
     *@param  expected               Description of the Parameter
     *@param  ds                     Description of the Parameter
     *@param  tag                    Description of the Parameter
     *@exception  DcmValueException  Description of the Exception
     */
    private void assertEquals(String[] expected, Dataset ds, int tag)
             throws DcmValueException {
        assertEquals(expected.length, ds.vm(tag));
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], ds.getString(tag, i));
        }
    }


    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testGetStrings() throws Exception {
        putStrings();
        assertEquals(SCHEDULED_STATION_AET,
                ds.getStrings(Tags.ScheduledStationAET));
        assertEquals(PATIENT_AGE, ds.getStrings(Tags.PatientAge));
        assertEquals(IMAGE_TYPE, ds.getStrings(Tags.ImageType));
        assertEquals(STUDY_DATE, ds.getStrings(Tags.StudyDate));
        assertEquals(STUDY_TIME.length, ds.getStrings(Tags.StudyTime).length);
        assertEquals(ACQUISITION_DATETIME,
                ds.getStrings(Tags.AcquisitionDatetime));
        assertEquals(IMAGE_POSITION, ds.getStrings(Tags.ImagePosition));
        assertEquals(ANCHOR_POINT, ds.getStrings(Tags.AnchorPoint));
        assertEquals(TABLE_OF_Y_BREAK_POINTS,
                ds.getStrings(Tags.TableOfYBreakPoints));
        assertEquals(REF_FRAME_NUMBER, ds.getStrings(Tags.RefFrameNumber));
        assertEquals(OTHER_PATIENT_IDS, ds.getStrings(Tags.OtherPatientIDs));
        assertEquals(ADDITIONAL_PATIENT_HISTORY,
                ds.getStrings(Tags.AdditionalPatientHistory));
        assertEquals(OTHER_PATIENT_NAMES,
                ds.getStrings(Tags.OtherPatientNames));
        assertEquals(ACCESSION_NUMBER, ds.getStrings(Tags.AccessionNumber));
        assertEquals(DISPLAYED_AREA_BRHC,
                ds.getStrings(Tags.DisplayedAreaBottomRightHandCorner));
        assertEquals(OVERLAY_ORIGIN, ds.getStrings(Tags.OverlayOrigin));
        assertEquals(DERIVATION_DESCRIPTION,
                ds.getStrings(Tags.DerivationDescription));
        assertEquals(SOP_CLASSES_SUPPORTED,
                ds.getStrings(Tags.SOPClassesSupported));
        assertEquals(REF_SAMPLE_POSITIONS,
                ds.getStrings(Tags.RefSamplePositions));
        assertEquals(REF_FRAME_NUMBER, ds.getStrings(Tags.RefFrameNumbers));
        assertEquals(TEXT_VALUE, ds.getStrings(Tags.TextValue));
    }


    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testSubSet() throws Exception {
        putStrings();
        Dataset sub0008 = ds.subSet(0x00080000, 0x00090000);
        assertEquals(8, sub0008.size());
        assertEquals(21, ds.size());
        sub0008.clear();
        assertTrue(sub0008.isEmpty());
        assertEquals(0, sub0008.size());
        assertEquals(13, ds.size());
    }

    private static final int[] SUBSET12 = {
            Tags.ScheduledStationAET,
            Tags.ImageType,
            Tags.ImagePosition, 
            Tags.AnchorPoint, 
            Tags.TableOfYBreakPoints,
            Tags.OtherPatientIDs,
            Tags.OtherPatientNames, 
            Tags.AccessionNumber, 
            Tags.OverlayOrigin,
            Tags.StudyTime,
            Tags.RefSamplePositions,
            Tags.TextValue
    };
    
    public void testSubSet2() throws Exception {
        putStrings();
        Dataset sub12 = ds.subSet(SUBSET12);
        assertEquals(12, sub12.size());
        assertEquals(21, ds.size());
        sub12.clear();
        assertTrue(sub12.isEmpty());
        assertEquals(0, sub12.size());
        assertEquals(9, ds.size());
    }
 
    
    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void testSetPrivateCreatorID() throws Exception {
        ds.putSH(0x00090666, "DIRECT666");
        ds.putSH(0x00090777, "DIRECT777");
        ds.putSH(0x00090999, "DIRECT999");
        assertEquals("DIRECT666", ds.getString(0x00090666));
        assertEquals("DIRECT777", ds.getString(0x00090777));
        assertEquals("DIRECT999", ds.getString(0x00090999));
        ds.setPrivateCreatorID("CREATOR_ID1");
        assertEquals("CREATOR_ID1", ds.getPrivateCreatorID());
        ds.putSH(0x00090666, "ADJUSTED666");
        assertEquals("ADJUSTED666", ds.getString(0x00090666));
        assertEquals("ADJUSTED666", ds.getString(0x00090066));
        ds.putSH(0x00090777, "ADJUSTED777");
        assertEquals("ADJUSTED777", ds.getString(0x00090777));
        assertEquals("ADJUSTED777", ds.getString(0x00090077));
        ds.setPrivateCreatorID("CREATOR_ID2");
        assertEquals("CREATOR_ID2", ds.getPrivateCreatorID());
        assertNull(ds.getString(0x00090066));
        assertNull(ds.getString(0x00090077));
        ds.putSH(0x00090999, "ADJUSTED999");
        assertEquals("ADJUSTED999", ds.getString(0x00090999));
        assertEquals("ADJUSTED999", ds.getString(0x00090099));
        ds.setPrivateCreatorID(null);
        assertNull(ds.getPrivateCreatorID());
        assertEquals("CREATOR_ID1", ds.getString(0x00090010));
        assertEquals("ADJUSTED666", ds.getString(0x00091066));
        assertEquals("ADJUSTED777", ds.getString(0x00091077));
        assertEquals("CREATOR_ID2", ds.getString(0x00090011));
        assertEquals("ADJUSTED999", ds.getString(0x00091199));
    }


    /**
     *  Description of the Method
     *
     *@param  expected  Description of the Parameter
     *@param  value     Description of the Parameter
     */
    private void assertEquals(String[] expected, String[] value) {
        assertNotNull(value);
        assertEquals(expected.length, value.length);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], value[i]);
        }
    }

    public void testTranscodeStringValues() throws Exception {
        ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
        ds.putPN(Tags.PatientName, UMLAUT);
        ds.putPN(Tags.OtherPatientNames, UMLAUTS);
        ds.putSQ(Tags.VerifyingObserverSeq).addNewItem()
                .putPN(Tags.VerifyingObserverName, UMLAUT);
        Dataset ds2 = DcmObjectFactory.getInstance().newDataset();
        ds2.putCS(Tags.SpecificCharacterSet, "ISO_IR 192");
        ds2.putAll(ds);
        assertEquals(UMLAUT, ds2.getString(Tags.PatientName));
        assertEquals(UMLAUTS, ds2.getStrings(Tags.OtherPatientNames));
        assertEquals(UMLAUT, ds2.get(Tags.VerifyingObserverSeq).getItem()
                .getString(Tags.VerifyingObserverName));
        ds.putAll(ds2);
        assertEquals(UMLAUT, ds.getString(Tags.PatientName));
        assertEquals(UMLAUTS, ds.getStrings(Tags.OtherPatientNames));
        assertEquals(UMLAUT, ds.get(Tags.VerifyingObserverSeq).getItem()
                .getString(Tags.VerifyingObserverName));
    }

    private void testPutAllPrivate(String creator1, int tag1, String value1,
            String creator2, int tag2, String value2) {
        ds.setPrivateCreatorID(creator1);
        ds.putSH(tag1, value1);
        Dataset ds2 = DcmObjectFactory.getInstance().newDataset();
        ds2.setPrivateCreatorID(creator2);
        ds2.putSH(tag2, value2);
        ds.putAll(ds2);
        ds.setPrivateCreatorID(creator2);
        assertEquals(value2, ds.getString(tag2));
    }

    private static final String CREATOR1 = "CREATOR1";
    private static final String CREATOR2 = "CREATOR2";
    private static final String VALUE1 = "VALUE1";
    private static final String VALUE2 = "VALUE2";
    private static final int TAG_00090010 = 0x00090010;
    private static final int TAG_00090020 = 0x00090020;
    private static final int TAG_00110010 = 0x00110010;
    private static final int TAG_00110020 = 0x00110020;

    public void testPutAllPrivateDiffCreatorDiffGroup() {
        testPutAllPrivate(CREATOR1, TAG_00090010, VALUE1,
                CREATOR2, TAG_00110020, VALUE2);
    }

    public void testPutAllPrivateDiffCreatorEqualGroup() {
        testPutAllPrivate(CREATOR1, TAG_00090010, VALUE1,
                CREATOR2, TAG_00090020, VALUE2);
    }

    public void testPutAllPrivateDiffCreatorDiffGroup2() {
        Dataset ds2 = DcmObjectFactory.getInstance().newDataset();

        ds2.setPrivateCreatorID(CREATOR1);
        ds2.putSH(TAG_00090010, VALUE1);
        ds2.putSH(TAG_00090020, VALUE2);

        ds2.setPrivateCreatorID(CREATOR2);
        ds2.putSH(TAG_00110010, VALUE2);
        ds2.putSH(TAG_00110020, VALUE1);

        ds.putAll(ds2);

        assertEquals(CREATOR1, ds.getString(0x00090010));
        assertEquals(VALUE1, ds.getString(0x00091010));
        assertEquals(VALUE2, ds.getString(0x00091020));

        assertEquals(CREATOR2, ds.getString(0x00110010));
        assertEquals(VALUE2, ds.getString(0x00111010));
        assertEquals(VALUE1, ds.getString(0x00111020));
    }

    public void testPutAllPrivateDiffCreatorEqualGroup2() {
        Dataset ds2 = DcmObjectFactory.getInstance().newDataset();

        ds2.setPrivateCreatorID(CREATOR1);
        ds2.putSH(TAG_00090010, VALUE1);
        ds2.putSH(TAG_00090020, VALUE2);

        ds2.setPrivateCreatorID(CREATOR2);
        ds2.putSH(TAG_00090010, VALUE2);
        ds2.putSH(TAG_00090020, VALUE1);

        ds.putAll(ds2);

        assertEquals(CREATOR1, ds.getString(0x00090010));
        assertEquals(VALUE1, ds.getString(0x00091010));
        assertEquals(VALUE2, ds.getString(0x00091020));

        assertEquals(CREATOR2, ds.getString(0x00090011));
        assertEquals(VALUE2, ds.getString(0x00091110));
        assertEquals(VALUE1, ds.getString(0x00091120));
    }

    public void testPutAllPrivateEqualCreatorDiffGroup() {
        testPutAllPrivate(CREATOR1, TAG_00090010, VALUE1,
                CREATOR1, TAG_00110020, VALUE2);
    }

    public void testPutAllPrivateEqualCreatorEqualGroup() {
        testPutAllPrivate(CREATOR1, TAG_00090010, VALUE1,
                CREATOR1, TAG_00090020, VALUE2);
    }
}

