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
 * The <code>Series</code> interface represents the 
 * <i>DICOM SR Document Series Module</i>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.1 SR Document Series Module"
 */
public interface Series {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the DICOM modality name (<code>SR</code>).
     * <br>DICOM Tag: <code>(0008,0060)</code>.
     *
     * @return  <code>SR</code> as DICOM modality name for DICOM SR.
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.7.3.1.1.1 Modality"
     */
    public String getModality();
    
    /**
     * Returns the DICOM series instance UID.
     * <br>DICOM Tag: <code>(0020,000E)</code>.
     *
     * @return  The DICOM series instance UID.
     */
    public String getSeriesInstanceUID();
    
    /**
     * Returns a number that identifies the Series.
     * <br>DICOM Tag: <code>(0020,0011)</code>.
     *
     * @return  A number that identifies the Series.
     */
    public int getSeriesNumber();
    
    /**
     * Returns the single item of the 
     * <i>Referenced Study Component Sequence</i>.
     * <br>
     * <i>Referenced Study Component Sequence</i> 
     * (Tag: <code>(0008,1111)</code>)
     * is a sequence that permits only a single item.
     * Uniquely identifies the <i>Performed Procedure Step SOP Instance</i>
     * for which the Series is created. <br>
     *
     * This sequence containes two entries for the single item:
     * <ul>
     *   <li><i>Referenced SOP Class UID</i> <code>(0008,1150)</code></li>
     *   <li><i>Referenced SOP Instance UID</i> <code>(0008,1155)</code></li>
     * </ul>
     *
     * @return  Single item of the <i>Referenced Study Component Sequence</i>.
     */
    public RefSOP getRefStudyComponent();

    public void toDataset(Dataset ds);
}//end interface Series
