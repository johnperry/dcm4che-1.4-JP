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

import org.dcm4che.srom.Code;
import org.dcm4che.srom.Request;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class RequestImpl implements Request {
    // Constants -----------------------------------------------------
    static final Request[] EMPTY_ARRAY = {};

    // Attributes ----------------------------------------------------
    private final String studyInstanceUID;
    private final String accessionNumber;
    private final String fillerOrderNumber;
    private final String placerOrderNumber;
    private final String procedureID;
    private final String procedureDescription;
    private final Code procedureCode;

    // Constructors --------------------------------------------------
    public RequestImpl(String studyInstanceUID, String accessionNumber,
        String fillerOrderNumber, String placerOrderNumber,
        String procedureID, String procedureDescription,
        Code procedureCode)
    {
        if ((this.studyInstanceUID = studyInstanceUID).length() == 0)
            throw new IllegalArgumentException();
       this.accessionNumber = accessionNumber;
       this.fillerOrderNumber = fillerOrderNumber;
       this.placerOrderNumber = placerOrderNumber;
       this.procedureID = procedureID;
       this.procedureDescription = procedureDescription;
       this.procedureCode = procedureCode;
    }

    public RequestImpl(Dataset ds) throws DcmValueException
    {
        this(ds.getString(Tags.StudyInstanceUID),
                ds.getString(Tags.AccessionNumber),
                ds.getString(Tags.FillerOrderNumber),
                ds.getString(Tags.PlacerOrderNumber),
                ds.getString(Tags.RequestedProcedureID),
                ds.getString(Tags.RequestedProcedureDescription),
                CodeImpl.newCode(
                        ds.getItem(Tags.RequestedProcedureCodeSeq)));
    }
    // Methodes ------------------------------------------------------
    
    //compares code value,coding scheme designator only
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof RequestImpl))
            return false;
        RequestImpl o = (RequestImpl)obj;
        return studyInstanceUID.equals(o.studyInstanceUID);
    }        

    public int hashCode() { return studyInstanceUID.hashCode(); }
    
    public String toString() {
        return "Request[uid=" + studyInstanceUID
             + ",accession=" + accessionNumber
             + ",fillerOrd=" + fillerOrderNumber
             + ",placerOrd=" + placerOrderNumber
             + ",procedure(" + procedureID
             + "," + procedureDescription
             + "," + procedureCode
             + ")]";
    }
    
    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }
    
    public String getFillerOrderNumber() {
        return fillerOrderNumber;
    }
    
    public String getPlacerOrderNumber() {
        return placerOrderNumber;
    }
    
    public String getAccessionNumber() {
        return accessionNumber;
    }
    
    public String getProcedureID() {
        return procedureID;
    }
    
    public String getProcedureDescription() {
        return procedureDescription;
    }
    
    public Code getProcedureCode() {
        return procedureCode;
    }    

    public void toDataset(Dataset ds) {
        ds.putUI(Tags.StudyInstanceUID, studyInstanceUID);
        ds.putLO(Tags.AccessionNumber, accessionNumber);
        ds.putLO(Tags.FillerOrderNumber, fillerOrderNumber);
        ds.putLO(Tags.PlacerOrderNumber, placerOrderNumber);
        ds.putSH(Tags.RequestedProcedureID, procedureID);
        ds.putLO(Tags.RequestedProcedureDescription, procedureDescription);
        if (procedureCode != null) {
            procedureCode.toDataset(
                ds.putSQ(Tags.RequestedProcedureCodeSeq).addNewItem());
        }
    }
}
