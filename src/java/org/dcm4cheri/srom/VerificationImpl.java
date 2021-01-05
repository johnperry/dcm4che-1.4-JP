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

import org.dcm4che.srom.Code;
import org.dcm4che.srom.Verification;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;

import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class VerificationImpl implements Verification {
    // Constants -----------------------------------------------------
    static final Verification[] EMPTY_ARRAY = {};

    // Attributes ----------------------------------------------------
    private final long time;
    private final String observerName;
    private final String observerOrg;
    private final Code observerCode;

    // Constructors --------------------------------------------------
    public VerificationImpl(Date time, String observerName, String observerOrg,
        Code observerCode)
    {
        this.time = time.getTime();
        if ((this.observerName = observerName).length() == 0)
            throw new IllegalArgumentException();
        this.observerOrg = observerOrg;
        this.observerCode = observerCode;
    }

    public VerificationImpl(Dataset ds) throws DcmValueException {
        this(ds.getDate(Tags.VerificationDateTime),
                ds.getString(Tags.VerifyingObserverName),
                ds.getString(Tags.VerifyingOrganization),
                CodeImpl.newCode(ds.getItem(
                        Tags.VerifyingObserverIdentificationCodeSeq)));
    }
    
    // Methodes ------------------------------------------------------
    public String getVerifyingObserverName() { return observerName; }
    public Code getVerifyingObserverCode() { return observerCode; }
    public String getVerifyingOrganization() { return observerOrg; }
    public Date getVerificationDateTime() { return new Date(time); }
    
    //compares code value,coding scheme designator only    
    public String toString() {
        return "Verification[" + getVerificationDateTime() 
                         + "/" + observerName + "]";
    }
        
    public int compareTo(java.lang.Object obj) {
        VerificationImpl v = (VerificationImpl)obj;
        return (int)(v.time - time);
    }
        
    public void toDataset(Dataset ds) {
        ds.putDT(Tags.VerificationDateTime, new Date(time));
        ds.putPN(Tags.VerifyingObserverName, observerName);
        ds.putLO(Tags.VerifyingOrganization, observerOrg);
        if (observerCode != null) {
            observerCode.toDataset(
                ds.putSQ(Tags.VerifyingObserverIdentificationCodeSeq)
                        .addNewItem());
        }
    }
    
}
