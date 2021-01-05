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
 * The <code>SOPInstanceRef</code> interface represents a
 * <i>SOP Instance Reference</i>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 0.9.9
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.2.1 SOP Instance Reference Macro"
 */
public interface SOPInstanceRef extends RefSOP {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
   
    /**
     * Returns the DICOM <i>Study Instance UID</i>.
     * <br>DICOM Tag: <code>(0020,000D)</code>
     * <br>Tag Name: <code>Study Instance UID</code>
     * <br>
     * Unique identifier for the Study.
     *
     * @return  the <i>Study Instance UID</i>.
     */
    public String getStudyInstanceUID();
    
    /**
     * Returns the DICOM <i>Series Instance UID</i>.
     * <br>DICOM Tag: <code>(0020,000E)</code>
     * <br>Tag Name: <code>Series Instance UID</code>
     * <br>
     * Unique identifier of a Series that is part of this Study 
     * and contains the referenced Composite Object(s).
     *
     * @return  the <i>Series Instance UID</i>.
     */
    public String getSeriesInstanceUID();
    
}//end interface SOPInstanceRef
