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

package org.dcm4cheri.srom;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.*;
import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class StudyImpl implements org.dcm4che.srom.Study {
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private final String studyInstanceUID;
    private final String studyID;
    private final Long studyDateTime;
    private final String referringPhysicianName;
    private final String accessionNumber;
    private final String studyDescription;
    private final Code[] procedureCodes;

    // Constructors --------------------------------------------------
    public StudyImpl(String studyInstanceUID, String studyID,
            Date studyDateTime, String referringPhysicianName,
            String accessionNumber, String studyDescription,
            Code[] procedureCodes) {
        if (studyInstanceUID.length() == 0)
            throw new IllegalArgumentException(studyInstanceUID);
    
        this.studyInstanceUID = studyInstanceUID;
        this.studyID = studyID;
        this.studyDateTime = studyDateTime != null 
            ? new Long(studyDateTime.getTime()) : null;
        this.referringPhysicianName = referringPhysicianName;
        this.accessionNumber = accessionNumber;
        this.studyDescription = studyDescription;
        this.procedureCodes = procedureCodes != null
                ? (Code[])procedureCodes.clone()
                : CodeImpl.EMPTY_ARRAY;
    }
    
    public StudyImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.StudyInstanceUID),
            ds.getString(Tags.StudyID),
            ds.getDateTime(Tags.StudyDate, Tags.StudyTime),
            ds.getString(Tags.ReferringPhysicianName),
            ds.getString(Tags.AccessionNumber),
            ds.getString(Tags.StudyDescription), 
            CodeImpl.newCodes(ds.get(Tags.ProcedureCodeSeq)));
    }
    
    // Public --------------------------------------------------------
    public final String getStudyInstanceUID() {
        return studyInstanceUID;
    }
    
    public final String getStudyID() {
        return studyID;
    }
    
    public final Date getStudyDateTime() {
        return studyDateTime != null
            ? new Date(studyDateTime.longValue()) : null;
    }
    
    public final String getReferringPhysicianName() {
        return referringPhysicianName;
    }
    
    public final String getAccessionNumber() {
        return accessionNumber;
    }
    
    public final String getStudyDescription() {
        return studyDescription;
    }
    
    public final Code[] getProcedureCodes() {
        return (Code[])procedureCodes.clone();
    }

    public int hashCode() {
        return studyInstanceUID.hashCode();
    }
    
    public boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof Study))
            return false;
    
        Study sty = (Study)o;
        return studyInstanceUID.equals(sty.getStudyInstanceUID());
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Study[").append(studyInstanceUID);
        sb.append(",ID=").append(studyID);
        sb.append(",A#=").append(accessionNumber);
        for (int i = 0; i < procedureCodes.length; ++i)
            sb.append(",PC=").append(procedureCodes[i]);
        sb.append("]");
        return sb.toString();
    }

    public void toDataset(Dataset ds) {
        ds.putUI(Tags.StudyInstanceUID, studyInstanceUID);
        ds.putSH(Tags.StudyID, studyID);
        Date date = getStudyDateTime();
        ds.putDA(Tags.StudyDate, date);
        ds.putTM(Tags.StudyTime, date);
        ds.putPN(Tags.ReferringPhysicianName, referringPhysicianName);
        ds.putLO(Tags.AccessionNumber, accessionNumber);
                
        if (studyDescription != null)
            ds.putLO(Tags.StudyDescription, studyDescription);
        
        if (procedureCodes.length != 0) {
            DcmElement sq = ds.putSQ(Tags.ProcedureCodeSeq);
            for (int i = 0; i < procedureCodes.length; ++i) {
                procedureCodes[i].toDataset(sq.addNewItem());
            }
        }
    }
}
