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
 * The <code>NumContent</code> interface represents a
 * <i>DICOM SR Numeric Content</i> of value type <code>NUM</code>.
 * <br>
 * 
 * Numeric value fully qualified by coded representation of the 
 * measurement name and unit of measurement.
 * 
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.18.1 Numeric Measurement Macro"
 */
public interface NumContent extends Content {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the numeric value.
     * <br>DICOM Tag: <code>(0040,A30A)</code>
     * <br>Tag Name: <code>Numeric Value</code>
     * <br>
     * Numeric measurement value.
     * 
     * @return  the the numeric value.
     */
    public Float getValue();
    
    /**
     * Returns the measurement unit.
     * <br>DICOM Tag: <code>(0040,08EA)</code>
     * <br>Tag Name: <code>Measurement Units Code Sequence</code>
     * <br>
     * The single item of <i>Measurement Units Code Sequence</i>
     *
     * @return the measurement unit.
     */
    public Code getUnit();

    public Code getQualifier();
    
    public void setValue(Float value);

    public void setUnit(Code code);

    public void setQualifier(Code code);
    
}//end interface NumContent
