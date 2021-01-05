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

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface HL7SRExport {
    
    /** Getter for property sendingApplication.
     * @return Value of property sendingApplication.
     */
    String getSendingApplication();
    
    /** Setter for property sendingApplication.
     * @param sendingApplication New value of property sendingApplication.
     */
    void setSendingApplication(String sendingApplication);
    
    /** Getter for property sendingFacility.
     * @return Value of property sendingFacility.
     */
    String getSendingFacility();
    
    /** Setter for property sendingFacility.
     * @param sendingFacility New value of property sendingFacility.
     */
    void setSendingFacility(String sendingFacility);
    
    /** Getter for property receivingApplication.
     * @return Value of property receivingApplication.
     */
    String getReceivingApplication();
    
    /** Setter for property receivingApplication.
     * @param receivingApplication New value of property receivingApplication.
     */
    void setReceivingApplication(String receivingApplication);
    
    /** Getter for property receivingFacility.
     * @return Value of property receivingFacility.
     */
    String getReceivingFacility();
    
    /** Setter for property receivingFacility.
     * @param receivingFacility New value of property receivingFacility.
     */
    void setReceivingFacility(String receivingFacility);
    
    String nextMessageControlID();
    
    byte[] toHL7(SRDocument doc, String messageControlID,
            String issuerOfPatientID, String patientAccountNumber,
            String placerOrderNumber, String fillerOrderNumber,
            String universalServiceID);
}
