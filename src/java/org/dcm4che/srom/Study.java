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

import java.util.Date;


/**
 * The <code>Study</code> interface represents some of the fields of the
 * <i>DICOM General Study Module</i>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.7.2.1 General Study Module"
 */
public interface Study {
        
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the study instance UID.
     * <br>DICOM Tag: <code>(0020,000D)</code>
     *
     * @return the study instance UID.
     */
    public String getStudyInstanceUID();
    
    /**
     * Returns the DICOM <i>Study ID</i>.
     * <br>DICOM Tag: <code>(0020,0010)</code>
     *
     * @return the study ID.
     */
    public String getStudyID();
    
    /**
     * Returns the study date and time.
     * <br>DICOM Tags: <br>
     * <ul>
     *   <li> <i>Study Date</i> <code>(0008,0020)</code> </li>
     *   <li> <i>Study Time</i> <code>(0008,0030)</code> </li>
     * </ul>
     *
     * @return  the study date and time.
     */
    public Date getStudyDateTime();
    
    /**
     * Returns the DICOM <i>Referring Physician's Name</i>.
     * <br>DICOM Tag: <code>(0008,0090)</code>
     *
     * @return Referring Physician's Name.
     */
    public String getReferringPhysicianName();
    
    /**
     * Returns the DICOM <i>Accession Number</i>.
     * <br>DICOM Tag: <code>(0008,0050)</code>
     *
     * @return  Accession Number.
     */
    public String getAccessionNumber();
    
    /**
     * Returns the DICOM <i>Study Description</i>.
     * <br>DICOM Tag: <code>(0008,1030)</code>
     *
     * @return  Study Description.
     */
    public String getStudyDescription();
    
    /**
     * Returns the entries of the DICOM <i>Procedure Code Sequence</i>.
     * <br>DICOM Tag: <code>(0008,1032)</code>
     *
     * @see Code
     * @return Procedure Code Sequence.
     */
    public Code[] getProcedureCodes();
    
    public void toDataset(Dataset ds);
}//end interface Study