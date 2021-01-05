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

package org.dcm4cheri.media;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirBuilder;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirBuilderPref;
import org.dcm4che.media.DirRecord;
import org.dcm4che.media.DirWriter;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DirBuilderImpl implements DirBuilder {
   
   private final DirWriter writer;
   private final DirBuilderPref pref;
   
   private String curPatID;
   private DirRecord curPatRec;
   private String curStudyUID;
   private DirRecord curStudyRec;
   private String curSeriesUID;
   private DirRecord curSeriesRec;
   
   /** Creates a new instance of DirBuilderImpl */
   public DirBuilderImpl(DirWriter writer, DirBuilderPref pref) {
      this.writer = writer;
      this.pref = pref;
   }
   
   
   public DirWriter getDirWriter() {
       return writer;
   }
   
   public int addFileRef(File file) throws IOException {
      InputStream in = new BufferedInputStream(new FileInputStream(file));
      Dataset ds = DirReaderImpl.factory.newDataset();
      try {
         ds.readFile(in, FileFormat.DICOM_FILE, Tags.PixelData);
      } finally {
         in.close();
      }
      return addFileRef(writer.toFileIDs(file), ds);
   }
   
   public int addFileRef(String[] fileIDs, Dataset ds) throws IOException {
      FileMetaInfo fmi = ds.getFileMetaInfo();
      if (fmi == null) {
         throw new IllegalArgumentException("Missing File Meta Information");
      }
      String tsUID = fmi.getTransferSyntaxUID();
      if (tsUID == null) {
         throw new IllegalArgumentException("Missing Transfer Syntax UID");
      }
      String classUID = fmi.getMediaStorageSOPClassUID();
      if (classUID == null) {
         throw new IllegalArgumentException("Missing SOP Class UID");
      }
      if (!classUID.equals(ds.getString(Tags.SOPClassUID))) {
         throw new IllegalArgumentException("Mismatch SOP Class UID");
      }
      String type = DirBuilderFactory.getRecordType(classUID);
      Dataset filter = pref.getFilterForRecordType(type);
      if (filter == null) {
         return 0;
      }
      String instUID = fmi.getMediaStorageSOPInstanceUID();
      if (instUID == null) {
         throw new IllegalArgumentException("Missing SOP Instance UID");
      }
      if (!instUID.equals(ds.getString(Tags.SOPInstanceUID))) {
         throw new IllegalArgumentException("Mismatch SOP Instance UID");
      }
      String seriesUID = ds.getString(Tags.SeriesInstanceUID);
      if (seriesUID == null) {
         throw new IllegalArgumentException("Missing Series Instance UID");
      }
      String studyUID = ds.getString(Tags.StudyInstanceUID);
      if (studyUID == null) {
         throw new IllegalArgumentException("Missing Study Instance UID");
      }
      String patID = ds.getString(Tags.PatientID, "");
      int count = 0;
      if (!patID.equals(curPatID)) {
         count += addPatRec(ds, patID);
      }
      if (!studyUID.equals(curStudyUID)) {
         count += addStudyRec(ds, studyUID);
      }
      if (!seriesUID.equals(curSeriesUID)) {
         count += addSeriesRec(ds, seriesUID);
      }
      writer.add(curSeriesRec, type,
         ds.subSet(filter), fileIDs, classUID, instUID, tsUID);
      ++count;
      return count;
   }
   
   private int addPatRec(Dataset ds, String patID) throws IOException {
      writer.commit();
      this.curSeriesUID = null;
      this.curSeriesRec = null;
      this.curStudyUID = null;
      this.curStudyRec = null;
      this.curPatID = patID;
      for (DirRecord dr = writer.getFirstRecord(true); dr != null;
      dr = dr.getNextSibling(true)) {
         if (DirRecord.PATIENT.equals(dr.getType()) && patID.equals(
         dr.getDataset().getString(Tags.PatientID))) {
            curPatRec = dr;
            return 0;
         }
      }
      curPatRec = writer.add(null, DirRecord.PATIENT,
         ds.subSet(pref.getFilterForRecordType(DirRecord.PATIENT)));
      return 1;
   }
   
   private int addStudyRec(Dataset ds, String studyUID) throws IOException {
      writer.commit();
      this.curSeriesUID = null;
      this.curSeriesRec = null;
      this.curStudyUID = studyUID;
      curPatRec.reload();
      for (DirRecord dr = curPatRec.getFirstChild(true); dr != null;
      dr = dr.getNextSibling(true)) {
         if (DirRecord.STUDY.equals(dr.getType()) && studyUID.equals(
         dr.getDataset().getString(Tags.StudyInstanceUID))) {
            curStudyRec = dr;
            return 0;
         }
      }
      curStudyRec = writer.add(curPatRec, DirRecord.STUDY,
         ds.subSet(pref.getFilterForRecordType(DirRecord.STUDY)));
      return 1;
   }
   
   private int addSeriesRec(Dataset ds, String seriesUID) throws IOException {
      writer.commit();
      this.curSeriesUID = seriesUID;
      curStudyRec.reload();
      for (DirRecord dr = curStudyRec.getFirstChild(true); dr != null;
      dr = dr.getNextSibling(true)) {
         if (DirRecord.SERIES.equals(dr.getType()) && seriesUID.equals(
         dr.getDataset().getString(Tags.SeriesInstanceUID))) {
            curSeriesRec = dr;
            return 0;
         }
      }
      curSeriesRec = writer.add(curStudyRec, DirRecord.SERIES,
         ds.subSet(pref.getFilterForRecordType(DirRecord.SERIES)));
      return 1;
   }
   
   public void close() throws IOException {
      writer.close();
   }
   
}
