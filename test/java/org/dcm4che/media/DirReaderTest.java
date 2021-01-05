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

import java.io.*;
import junit.framework.*;
import org.dcm4che.data.*;

import org.dcm4che.dict.*;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      May, 2002
 * @version    $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
public class DirReaderTest extends TestCase
{

    /**
     *  The main program for the DirReaderTest class
     *
     * @param  args  The command line arguments
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }


    private final static DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private final static String DICOMDIR_ID = "data/DICOMDIR";
    private final static String CLASS_UID = "1.2.840.10008.1.3.10";


    /**
     *  A unit test suite for JUnit
     *
     * @return    The test suite
     */
    public static Test suite()
    {
        return new TestSuite(DirReaderTest.class);
    }


    /**
     * Creates new DictionaryTest
     *
     * @param  name  Description of the Parameter
     */
    public DirReaderTest(String name)
    {
        super(name);
    }


    /**
     *  The JUnit setup method
     *
     * @exception  Exception  Description of the Exception
     */
    protected void setUp()
        throws Exception
    {
    }


    /**
     *  A unit test for JUnit
     *
     * @exception  Exception  Description of the Exception
     */
    public void testIterateRoot()
        throws Exception
    {
        DirReader r =
                DirBuilderFactory.getInstance().newDirReader(new File(DICOMDIR_ID));
        try {
            Dataset fsi = r.getFileSetInfo();
            assertEquals(0, fsi.getInt(Tags.FileSetConsistencyFlag, -1));
            FileMetaInfo fmi = fsi.getFileMetaInfo();
            assertEquals(CLASS_UID,
                    fmi.getString(Tags.MediaStorageSOPClassUID));
            assertEquals("NEMA97CD", fsi.getString(Tags.FileSetID));
            DirRecord rec = r.getFirstRecord(false);
            assertNotNull(rec);
            assertEquals("PATIENT", rec.getType());
            assertEquals(DirRecord.IN_USE, rec.getInUseFlag());
            assertEquals("TXSP-H-035",
                    rec.getDataset().getString(Tags.PatientID, null));
            int count = 1;
            while ((rec = rec.getNextSibling(false)) != null) {
                assertEquals("PATIENT", rec.getType());
                assertEquals(DirRecord.IN_USE, rec.getInUseFlag());
                ++count;
            }
            assertEquals(81, count);
        } finally {
            r.close();
        }
    }


    /**
     *  A unit test for JUnit
     *
     * @exception  Exception  Description of the Exception
     */
    public void testIterateChilds()
        throws Exception
    {
        DirReader r = DirBuilderFactory.getInstance().
                newDirReader(new File(DICOMDIR_ID));
        try {
            int count = 0;
            for (DirRecord pat = r.getFirstRecord(false); pat != null;
                    pat = pat.getNextSibling(false)) {
                ++count;
                assertEquals("PATIENT", pat.getType());
                assertEquals(DirRecord.IN_USE, pat.getInUseFlag());
                for (DirRecord study = pat.getFirstChild(false); study != null;
                        study = study.getNextSibling(false)) {
                    ++count;
                    assertEquals("STUDY", study.getType());
                    assertEquals(DirRecord.IN_USE, study.getInUseFlag());
                    for (DirRecord series = study.getFirstChild(false);
                            series != null; series = series.getNextSibling(false)) {
                        ++count;
                        assertEquals("SERIES", series.getType());
                        assertEquals(DirRecord.IN_USE, series.getInUseFlag());
                        for (DirRecord image = series.getFirstChild(false);
                                image != null; image = image.getNextSibling(false)) {
                            ++count;
                            assertEquals("IMAGE", image.getType());
                            assertEquals(DirRecord.IN_USE, image.getInUseFlag());
                        }
                    }
                }
            }
            assertEquals(1203, count);
        } finally {
            r.close();
        }
    }


    /**
     *  A unit test for JUnit
     *
     * @exception  Exception  Description of the Exception
     */
    public void testQueryRecord()
        throws Exception
    {
        DirReader r = DirBuilderFactory.getInstance().
                newDirReader(new File(DICOMDIR_ID));
        try {
            Dataset patKeys = dof.newDataset();
            patKeys.putPN(Tags.PatientName, "*^volunteer*");
            Dataset studyKeys = dof.newDataset();
            studyKeys.putDA(Tags.StudyDate, "19970801-");
            DirRecord patRec = r.getFirstRecordBy("PATIENT", patKeys, false);
            assertNull(patRec);
            patRec = r.getFirstRecordBy("PATIENT", patKeys, true);
            assertNotNull(patRec);
            DirRecord studyRec = patRec.getFirstChildBy("STUDY", studyKeys, false);
            assertNotNull(studyRec);
            patRec = patRec.getNextSiblingBy("PATIENT", patKeys, true);
            assertNotNull(patRec);
            studyRec = patRec.getFirstChildBy("STUDY", studyKeys, false);
            assertNull(studyRec);
            patRec = patRec.getNextSiblingBy("PATIENT", patKeys, true);
            assertNotNull(patRec);
            studyRec = patRec.getFirstChildBy("STUDY", studyKeys, false);
            assertNull(studyRec);
            patRec = patRec.getNextSiblingBy("PATIENT", patKeys, true);
            assertNotNull(patRec);
            studyRec = patRec.getFirstChildBy("STUDY", studyKeys, false);
            assertNull(studyRec);
            patRec = patRec.getNextSiblingBy("PATIENT", patKeys, true);
            assertNull(patRec);
        } finally {
            r.close();
        }
    }

}

