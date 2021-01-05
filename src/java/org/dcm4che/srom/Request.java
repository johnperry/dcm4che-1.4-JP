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

/**
 * The <code>Request</code> interface represents an item of
 * the <i>Referenced Request Sequence</i> (DICOM Tag: <code>(0040,A370)</code>)
 * in <i>DICOM SR Document General Module</i>.
 *
 * <p>
 *  The <i>Referenced Request Sequence</i> itself is defined as:<br> 
 *  <pre>
 *      Identifies Requested Procedures which are being 
 *      fulfilled (completely or partially) by creation 
 *      of this Document. One or more Items may be 
 *      included in this sequence. Required if this 
 *      Document fulfills at least one Requested Procedure.
 *  </pre>
 * </p>
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.2 SR DOCUMENT GENERAL MODULE"
 */
public interface Request {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the DICOM <i>Study Instance UID</i>.
     * <br>DICOM Tag: <code>(0020,000D)</code>
     * <br>
     * Unique identifier for the Study.
     *
     * @return  the Study Instance UID.
     */
    public String getStudyInstanceUID();
    
    /**
     * Returns the DICOM <i>Accession Number</i>.
     * <br>DICOM Tag: <code>(0008,0050)</code>
     * <br>
     * A departmental IS generated number which identifies 
     * the order for the Study.
     *
     * @return  the Accession Number.
     */
    public String getAccessionNumber();
    
    /**
     * Returns the DICOM <i>Placer Order Number</i>.
     * <br>DICOM Tag: <code>(0040,2016)</code>
     * <br>
     * The order number assigned to the Imaging Service Request 
     * by the party placing the order.
     *
     * @return  the Placer Order Number.
     */
    public String getPlacerOrderNumber();
    
    /**
     * Returns the DICOM <i>Filler Order Number</i>.
     * <br>DICOM Tag: <code>(0040,2017)</code>
     * <br>
     * The order number assigned to the Imaging Service Request 
     * by the party filling the order.
     *
     * @return  the Filler Order Number.
     */
    public String getFillerOrderNumber();
    
    /**
     * Returns the DICOM <i>Requested Procedure ID</i>.
     * <br>DICOM Tag: <code>(0040,1001)</code>
     * <br>
     * Identifier of the related Requested Procedure.
     *
     * @return  the Requested Procedure ID.
     */
    public String getProcedureID();
    
    /**
     * Returns the DICOM <i>Requested Procedure Description</i>.
     * <br>DICOM Tag: <code>(0032,1060)</code>
     * <br>
     * Institution-generated administrative description or 
     * classification of Requested Procedure.
     *
     * @return  the Requested Procedure Description.
     */
    public String getProcedureDescription();
    
    /**
     * Returns the single item of a 
     * DICOM <i>Requested Procedure Code Sequence</i>.
     * <br>DICOM Tag: <code>(0032,1064)</code>
     * <br>
     * A sequence that conveys the requested procedure. 
     * Zero or one Item may be included in this sequence.
     *
     * @return  the Requested Procedure Code or <code>null</code>
     *          if the <i>Requested Procedure Code Sequence</i>
     *          had no entry.
     */
    public Code getProcedureCode();
    
    /**
     * Compares two <code>Request</code> objects for equality.
     * <br>
     * <b>Note:</b> Only the <i>study instance UID</i> of the
     *             <code>Request</code> objects will the compared.
     *
     * @param obj  the <code>Request</code> object to be compared
     *             with this instance.
     * @return <code>true</code> if this instance and <code>obj</code>
     *         are equal <code>false</code> otherwise.
     */
    public boolean equals(Object obj);
    
    public void toDataset(Dataset ds);
    
}//end interface Request
