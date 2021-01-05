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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.Equipment;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
final class EquipmentImpl implements Equipment {
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private String manufacturer;
    private String institutionName;
    private String institutionAddress;
    private String stationName;
    private String departmentName;
    private String modelName;

    // Constructor --------------------------------------------------------
    public EquipmentImpl(String manufacturer, String modelName,
            String stationName) {
        setManufacturer(manufacturer);
		setModelName(modelName);
		setStationName(stationName);
    }

    public EquipmentImpl(Equipment other) {
        this(other.getManufacturer(), other.getModelName(), 
                other.getStationName());
        this.institutionName = other.getInstitutionName();
        this.institutionAddress = other.getInstitutionAddress();
        this.departmentName = other.getDepartmentName();
    }
    
    public EquipmentImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.Manufacturer),
            ds.getString(Tags.ManufacturerModelName),
            ds.getString(Tags.StationName));
        this.institutionName = ds.getString(Tags.InstitutionName);
        this.institutionAddress = ds.getString(Tags.InstitutionAddress);
        this.departmentName = ds.getString(Tags.InstitutionalDepartmentName);
    }
    
    // Public --------------------------------------------------------
    
    public final String getManufacturer() {
        return manufacturer;
    }
    
    public final void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer == null ? "" : manufacturer;
    }
    
	public final String getInstitutionName() {
        return institutionName;
    }
    
    public final void setInstitutionName(String institutionName){
        this.institutionName = institutionName;
    }
    
    public final String getInstitutionAddress() {
        return institutionAddress;
    }
    
    public final void setInstitutionAddress(String institutionAddress){
        this.institutionAddress = institutionAddress;
    }
    
    public final String getStationName() {
        return stationName;
    }
    
    public final void setStationName(String stationName){
        this.stationName = stationName;
    }
    
    public final String getDepartmentName() {
        return departmentName;
    }
    
    public final void setDepartmentName(String departmentName){
        this.departmentName = departmentName;
    }
    
    public final String getModelName() {
        return modelName;
    }
    
    public final void setModelName(String modelName){
        this.modelName = modelName;
    }
    
    public String toString() {
        return "Equipment[manufacturer=" + manufacturer
                      + ",station=" + stationName
                      + ",model=" + modelName
                      + "]";
    }

    public void toDataset(Dataset ds) {
        ds.putLO(Tags.Manufacturer, manufacturer);
        if (institutionName != null)
            ds.putLO(Tags.InstitutionName, institutionName);
        if (institutionAddress != null)
            ds.putST(Tags.InstitutionAddress, institutionAddress);
        if (stationName != null)
            ds.putSH(Tags.StationName, stationName);
        if (departmentName != null)
            ds.putLO(Tags.InstitutionalDepartmentName, departmentName);
        if (modelName != null)
            ds.putLO(Tags.ManufacturerModelName, modelName);
    }
}
