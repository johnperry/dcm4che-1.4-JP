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
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.*;
import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class NumContentImpl extends NamedContentImpl implements NumContent {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private Float value;
    private Code unit;
    private Code qualifier;

    // Constructors --------------------------------------------------
    NumContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, Float value, Code unit, Code qualifier) {
        super(owner, obsDateTime, template, checkNotNull(name));
    	this.value = value;
    	this.unit = unit;
    	this.qualifier = qualifier;
    }
    
    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
    	return new NumContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime),
                template, name, value, unit, qualifier);
    }

    // Methodes --------------------------------------------------------
    public final void setName(Code newName) {
        this.name = checkNotNull(newName);
    }
    
    public String toString() {
        return prompt().append(value).append(unit).toString();
    }

    public final ValueType getValueType() {
        return ValueType.NUM;
    }    
    
    public final Float getValue() {
        return value;
    }

    public final Code getUnit() {
        return unit;
    }

    public final Code getQualifier() {
        return qualifier;
    }

    public final void setValue(Float value) {
        this.value = value;
    }

    public final void setUnit(Code unit) {
        this.unit = unit;
    }

    public final void setQualifier(Code qualifier) {
        this.qualifier = qualifier;
    }

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        DcmElement mvsq = ds.putSQ(Tags.MeasuredValueSeq);
        if (value != null && unit != null) {
            Dataset mv = mvsq.addNewItem();
	        mv.putDS(Tags.NumericValue, value.floatValue());
	        unit.toDataset(mv.putSQ(Tags.MeasurementUnitsCodeSeq).addNewItem());
        }
        if (qualifier != null) {
        	qualifier.toDataset(ds.putSQ(Tags.NumericValueQualifierCodeSeq).addNewItem());
        }
    }
}
