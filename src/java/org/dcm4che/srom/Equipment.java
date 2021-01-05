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
 * The <code>Equipment</code> interface represents some of the fields of the
 * <i>DICOM General Equipment Module</i>.
 * 
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.7.5.1 General Equipment Module"
 */
public interface Equipment {
    
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------

    /**
     * Returns the DICOM <i>Manufacturer</i>.
     * <br>DICOM Tag: <code>(0008,0070)</code>
     * <br>
     * Manufacturer of the equipment that produced the digital images.
     *
     * @return  the Manufacturer.
     */
    public String getManufacturer();
    
    /**
     * Sets the DICOM <i>Manufacturer</i>.
     * <br>DICOM Tag: <code>(0008,0070)</code>
     * <br>
     * Manufacturer of the equipment that produced the digital images.
     *
     * @param manufacturer  the Manufacturer.
     */
    public void setManufacturer(String manufacturer);
    
    /**
     * Returns the DICOM <i>Institution Name</i>.
     * <br>DICOM Tag: <code>(0008,0080)</code>
     * <br>
     * Institution where the equipment is located that produced the 
     * digital images.
     *
     * @return  the Institution Name.
     */
    public String getInstitutionName();
    
    /**
     * Sets the DICOM <i>Institution Name</i>.
     * <br>DICOM Tag: <code>(0008,0080)</code>
     * <br>
     * Institution where the equipment is located that produced the 
     * digital images.
     *
     * @param institutionName  the Institution Name.
     */
    public void setInstitutionName(String institutionName);
    
    /**
     * Returns the DICOM <i>Institution Address</i>.
     * <br>DICOM Tag: <code>(0008,0081)</code>
     * <br>
     * Mailing address of the institution where the equipment is 
     * located that produced the digital images.
     *
     * @return  the Institution Address.
     */
    public String getInstitutionAddress();
    
    /**
     * Sets the DICOM <i>Institution Address</i>.
     * <br>DICOM Tag: <code>(0008,0081)</code>
     * <br>
     * Mailing address of the institution where the equipment is 
     * located that produced the digital images.
     *
     * @param institutionAddress  the Institution Address.
     */
    public void setInstitutionAddress(String institutionAddress);
    
    /**
     * Returns the DICOM <i>Station Name</i>.
     * <br>DICOM Tag: <code>(0008,1010)</code>
     * <br>
     * User defined name identifying the machine that produced the 
     * digital images.
     *
     * @return  the Station Name.
     */
    public String getStationName();
    
    /**
     * Sets the DICOM <i>Station Name</i>.
     * <br>DICOM Tag: <code>(0008,1010)</code>
     * <br>
     * User defined name identifying the machine that produced the 
     * digital images.
     *
     * @param stationName  the Station Name.
     */
    public void setStationName(String stationName);
    
    /**
     * Returns the DICOM <i>Institutional Department Name</i>.
     * <br>DICOM Tag: <code>(0008,1040)</code>
     * <br>
     * Department in the institution where the equipment is located 
     * that produced the digital images.
     *
     * @return  the Institutional Department Name.
     */
    public String getDepartmentName();
    
    /**
     * Sets the DICOM <i>Institutional Department Name</i>.
     * <br>DICOM Tag: <code>(0008,1040)</code>
     * <br>
     * Department in the institution where the equipment is located 
     * that produced the digital images.
     *
     * @param departmentName  the Institutional Department Name.
     */
    public void setDepartmentName(String departmentName);
    
    /**
     * Returns the DICOM <i>Manufacturer's Model Name</i>.
     * <br>DICOM Tag: <code>(0008,1090)</code>
     * <br>
     * Manufacturer's model number of the equipment that produced 
     * the digital images.
     *
     * @return  the Manufacturer's Model Name.
     */
    public String getModelName();
    
    /**
     * Sets the DICOM <i>Manufacturer's Model Name</i>.
     * <br>DICOM Tag: <code>(0008,1090)</code>
     * <br>
     * Manufacturer's model number of the equipment that produced 
     * the digital images.
     *
     * @param modelName  the Manufacturer's Model Name.
     */
    public void setModelName(String modelName);
    
    public void toDataset(Dataset ds);
}//end interface Equipment
