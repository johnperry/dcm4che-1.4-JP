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
 * The <code>Verification</code> interface represents an
 * item of the <i>Verifying Observer Sequence</i> <code>(0040,A073)</code>.
 * <br>
 * The person or persons authorized to verify documents of this 
 * type and accept responsibility for the content of this document. 
 * One or more Items may be included in this sequence. Required if 
 * <i>{@link SRDocument#isVerified Verification Flag}</i> 
 * <code>(0040,A493)</code> is <code>VERIFIED</code>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.2 SR DOCUMENT GENERAL MODULE"
 */
public interface Verification extends Comparable {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------

    /**
     * Returns the verifying observer name.
     * <br>DICOM Tag: <code>(0040,A075)</code>
     * <br>Tag Name: <code>Verifying Observer Name</code>
     * <br>
     * The person authorized by the <i>Verifying Organization</i> 
     * <code>(0040,A027)</code>
     * to verify documents of this type and who accepts responsibility 
     * for the content of this document. 
     *
     * @return  the verifying observer name.
     */
    public String getVerifyingObserverName();
    
    /**
     * Returns the single item of the 
     * <i>Verifying Observer Identification Code Sequence</i>.
     * <br>
     * DICOM Tag: <code>(0040,A088)</code><br>
     * Tag Name: <code>Verifying Observer Identification Code Sequence</code>
     * <br>
     * Coded identifier of the Verifying Observer. Zero or one Items 
     * shall be permitted in this sequence. 
     *
     * @return  the single item of the 
     * <i>Verifying Observer Identification Code Sequence</i>.
     */
    public Code getVerifyingObserverCode();
    
    /**
     * Returns the verifying organization.
     * <br>DICOM Tag: <code>(0040,A027)</code>
     * <br>Tag Name: <code>Verifying Organization</code>
     * <br>
     * Organization to which the <i>Verifying Observer Name</i>
     * <code>(0040,A075)</code> is accountable for this document in the 
     * current interpretation procedure. 
     *
     * @return  the verifying organization.
     */
    public String getVerifyingOrganization();
    
    /**
     * Returns the verification date time.
     * <br>DICOM Tag: <code>(0040,A030)</code>
     * <br>Tag Name: <code>Verification DateTime</code>
     * <br>
     * Date and Time of verification by the 
     * <i>Verifying Observer Name</i> <code>(0040,A075)</code>.
     *
     * @return  the verification date time.
     */
    public Date getVerificationDateTime();   

    public void toDataset(Dataset ds);

}//end interface Verification
