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

import org.dcm4che.Implementation;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.FileMetaInfo;

import java.io.File;
import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

/**
 * A DirBuilderFactory instance can be used to create
 * {@link org.dcm4che.media.DirReader},
 * {@link org.dcm4che.media.DirWriter},
 * {@link org.dcm4che.media.DirBuilder} and
 * {@link org.dcm4che.media.DirBuilderPref} objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public abstract class DirBuilderFactory {


    public static String getRecordType(String classUID) {
        return DirRecordTypes.getRecordType(classUID);
    }
    
   /**
    * Obtain a new instance of a <code>DirBuilderFactory</code>.
    * This static method creates a new factory instance.
    *
    * @return new DirBuilderFactory instance, never null.
    */
    public static DirBuilderFactory getInstance() {
        return (DirBuilderFactory) Implementation.findFactory(
            "dcm4che.media.DirBuilderFactory");
    }
    
   /**
    * Construct a DirReader from a File. Normally a File reference should be
    * used rather than an ImageInputStream reference, so
    * {@link org.dcm4che.media.DirReader#getRefFile(String[])} can resolve
    * fileIDs to an absolute File reference.
    *
    * @param file A file reference to an existing DICOM dictionary (DICOMDIR).  
    * @throws IOException if file does not exist or is not a valid DICOM
    *                        dictionary
    * @return DirReader to read content of specified DICOM dictionary.
    */   
    public abstract DirReader newDirReader(File file) throws IOException;

   /**
    * Construct a DirReader from an ImageInputStream. Normally a File reference
    * should be used rather than an ImageInputStream reference, so
    * {@link org.dcm4che.media.DirReader#getRefFile(String[])} can resolve
    * a fileID to a absolute File reference.
    *
    * @param in A ImageInputStream reference to a DICOM dictionary (DICOMDIR).  
    * @throws IOException if the ImageInputStream does not reference a valid
    *                        DICOM dictionary
    * @return DirReader to read content of specified DICOM dictionary.
    */   
    public abstract DirReader newDirReader(ImageInputStream in)
            throws IOException;

   /**
    * Construct a DirWriter to update an existing DICOM dictionary.
    *  
    * @param file A file reference to an existing DICOM dictionary (DICOMDIR).
    * @param encParam Specifies encoding options, for new added directory
    *                 records. May be <code>null</code>, in which case
    *                 default encoding options will be used.
    *
    * @throws IOException if file does not reference a valid DICOM dictionary.
    * @return  DirWriter to update content of specified DICOM dictionary.
    */    
    public abstract DirWriter newDirWriter(File file, DcmEncodeParam encParam)
            throws IOException;

   /**
    * Create new DICOM dictionary specified by the File argument and the
    * Media Storage SOP Instance UID. Other attributes of its File Meta
    * Information will be initalized with default values. 
    *
    * @param file Specifies the new created DICOM dictionary (DICOMDIR).
    * @param uid Media Storage SOP Instance UID
    * @param filesetID File-set ID. May be <code>null</code>.
    * @param descriptorFile File-set Descriptor. May be <code>null</code>.
    * @param specCharSet Specific Character Set of File-set Descriptor File.
    *                    May be <code>null</code>.
    * @param encParam Specifies encoding options. May be <code>null</code>,
    *                 in which case default encoding options will be used.
    * 
    * @throws IOException if the creation of the new DICOM dictionary failed
    *                     caused by an i/o releated error.
    * @return  DirWriter to insert content into the new created
    *          DICOM dictionary.
    */    
    public abstract DirWriter newDirWriter(File file, String uid,
            String filesetID, File descriptorFile, String specCharSet,
            DcmEncodeParam encParam) throws IOException;

   /**
    * Create new DICOM dictionary specified by the File argument and explicit
    * specified File Meta Information. 
    *
    * @param file Specifies the new created DICOM dictionary (DICOMDIR).
    * @param fmi explicit specified File Meta Information
    * @param filesetID File-set ID. May be <code>null</code>.
    * @param descriptorFile File-set Descriptor. May be <code>null</code>.
    * @param specCharSet Specific Character Set of File-set Descriptor File.
    *                    May be <code>null</code>.
    * @param encParam Specifies encoding options. May be <code>null</code>,
    *                 in which case default encoding options will be used.
    * 
    * @throws IOException if the creation of the new DICOM dictionary failed
    *                     caused by an i/o releated error.
    * @return  DirWriter to insert content into the new created
    *          DICOM dictionary.
    */    
    public abstract DirWriter newDirWriter(File file, FileMetaInfo fmi,
            String filesetID, File descriptorFile, String specCharSet,
            DcmEncodeParam encParam) throws IOException;

   /**
    * Create empty preferences for DirBuilder. 
    * @return empty preferences for DirBuilder.*/    
    public abstract DirBuilderPref newDirBuilderPref();

    /**
     * Creates new DirBuilder associated with specified DirWriter and with
     * specified preferences.
     *
     * @param writer associated DirWriter.
     * @param pref Preferences for generated directory records.
     * @return  new DirBuilder associated with specified DirWriter.
     */    
    public abstract DirBuilder newDirBuilder(DirWriter writer,
            DirBuilderPref pref);
    
}
