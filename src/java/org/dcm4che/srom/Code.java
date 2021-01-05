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
 * The <code>Code</code> interface represents a
 * <i>coded entry</i>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 0.9.9
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * 8.8 STANDARD ATTRIBUTE SETS FOR CODE SEQUENCE ATTRIBUTES"
 */
public interface Code {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the DICOM <i>Code Value</i>.
     * <br>DICOM Tag: <code>(0008,0100)</code>
     * The code value.
     *
     * @return  the <i>Code Value</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions, 8.1 CODE VALUE"
     */
    public String getCodeValue();

    /**
     * Returns the DICOM <i>Coding Scheme Designator</i>.
     * <br>DICOM Tag: <code>(0008,0102)</code>
     * The coding scheme designator.
     *
     * @return  the <i>Coding Scheme Designator</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions, 
     * 8.2 CODING SCHEME DESIGNATOR"
     */
    public String getCodingSchemeDesignator();
    
    /**
     * Returns the DICOM <i>Coding Scheme Version</i>.
     * <br>DICOM Tag: <code>(0008,0103)</code>
     * The coding scheme version.
     *
     * @return  the <i>Coding Scheme Version</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions, 
     * 8.2 CODING SCHEME VERSION"
     */
    public String getCodingSchemeVersion();

    /**
     * Returns the DICOM <i>Code Meaning</i>.
     * <br>DICOM Tag: <code>(0008,0104)</code>
     * The code meaning.
     *
     * @return  the <i>Code Meaning</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions, 
     * 8.3 CODE MEANING"
     */
    public String getCodeMeaning();
    
    /**
     * Compares two Code objects for equality.
     * Only code value and coding scheme designator will be use to
     * find out of this code and the one specified as parameter 
     * <code>obj</code> are equal.
     *
     * @param obj  the Code object to be compared for equality with this Code
     *             object.
     *
     * @return <code>true</code> if equal <code>false</code> otherwise.
     */
    public boolean equals(Object obj);
    
    public void toDataset(Dataset ds);   
}//end interface Code
