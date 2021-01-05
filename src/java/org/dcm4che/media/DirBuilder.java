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

import org.dcm4che.data.Dataset;

import java.io.File;
import java.io.IOException;

/** Builder facade for {@link DirWriter} to generate and insert Directory
 * Records refering DICOM files in the DICOM Directory (= DICOMDIR file).
 * <p>Can only be used for DICOM Directory with scheme
 * <pre>
 * PATIENT
 *  STUDY
 *    SERIES
 *      IMAGE|PRESENTATION|SR DOCUMENT|KEY OBJECT DOC|...
 * </pre>
 * <p>{@link DirRecord} objects will be generated according associated
 * {@link DirBuilderPref}, specified in factory method.
 * {@link DirBuilderFactory#newDirBuilder}
 * <p><code>DirBuilder</code> also take care, that there will be only
 * <ul>
 * <li> one <code>PATIENT</code> record with the same value of
 * <code>Patient ID (0010,0020)</code>,
 * <li> one <code>STUDY</code> record with the same value of
 * <code>Study Instance UID (0020,000D)</code> with the same parent
 * <code>PATIENT</code> record,
 * <li> one <code>SERIES</code> record with the same value of
 * <code>Series Instance UID (0020,000E)</code> with the same parent
 * <code>STUDY</code> record,
 * </ul>
 * in the associated DICOM Directory.
 *
 * @see DirBuilderFactory#newDirBuilder
 * @see DirBuilderPref
 * @see DirWriter
 * @see DirRecord
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>2002/07/16 gunter zeilinger:</b>
 * <ul>
 * <li> add javadoc comments
 * </ul>
 */
public interface DirBuilder {
   
   /** Add (up to 4) {@link DirRecord} objects refering the DICOM object
    * in the specified <code>file</code> to the associated DICOM Directory.
    * <p>The function may internally invoke {@link DirWriter#commit} of the
    * associated <code>DirWriter</codeS. Therefore, the operation cannot be
    * undo by {@link DirWriter#rollback}!
    *
    * @param file DICOM file
    * @throws IOException if an IO error occurs, writing the record into the
    *                     DICOM Directory.
    * @return number of added {@link DirRecord} objects.
    */   
   int addFileRef(File file) throws IOException;
   
   /** Add (up to 4) {@link DirRecord} objects refering the DICOM object
    * with a specified File IDs to the associated DICOM Directory.
    * <p>The function may internally invoke {@link DirWriter#commit} of the
    * associated <code>DirWriter</code>. Therefore, the operation cannot be
    * undo by {@link DirWriter#rollback}!
    *
    * @param fileIDs File ID components
    * @param ds DICOM Data Set
    * @throws IOException if an IO error occurs, writing the record into the
    *                     DICOM Directory.
    * @return number of added {@link DirRecord} objects.
    */   
   int addFileRef(String[] fileIDs, Dataset ds) throws IOException;
   
   /** Close the DICOM Dictionary (= DICOMDIR file).
    * @throws IOException  If an I/O error occurs */   
   void close() throws IOException;
   
   /** Get underlying {@link DirWriter} object
    * @return underlying {@link DirWriter} object.
    */   
   DirWriter getDirWriter();       
}

