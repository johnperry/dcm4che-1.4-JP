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
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class SeriesImpl implements org.dcm4che.srom.Series {
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private final String modality;
    private final String seriesInstanceUID;
    private final int seriesNumber;
    private final RefSOP refStudyComponent;

    // Constructors --------------------------------------------------
    public SeriesImpl(String modality, String seriesInstanceUID,
            int seriesNumber, RefSOP refStudyComponent) {
        if (modality.length() == 0)
            throw new IllegalArgumentException(modality);
        if (seriesInstanceUID.length() == 0)
            throw new IllegalArgumentException(seriesInstanceUID);
    
        this.modality = modality;
        this.seriesInstanceUID = seriesInstanceUID;
        this.seriesNumber = seriesNumber;
        this.refStudyComponent = refStudyComponent;
    }
    
    public SeriesImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.Modality),
            ds.getString(Tags.SeriesInstanceUID),
            ds.getInt(Tags.SeriesNumber, -1),
            RefSOPImpl.newRefSOP(
                ds.getItem(Tags.RefPPSSeq)));
    }

    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    public String getModality() {
        return modality;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }
    
    public int getSeriesNumber() {
        return seriesNumber;
    }
    
    public RefSOP getRefStudyComponent() {
        return refStudyComponent;
    }
    
    public int hashCode() {
        return seriesInstanceUID.hashCode();
    }
    
    public boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof Series))
            return false;
    
        Series ser = (Series)o;
        return seriesInstanceUID.equals(ser.getSeriesInstanceUID());
    }
    
    public String toString() {
        return "Series[" + seriesInstanceUID
            + ",#" + seriesNumber
            + ",PPS=" + refStudyComponent
            + "]";
    }

    public void toDataset(Dataset ds) {
        ds.putCS(Tags.Modality, modality);
        ds.putUI(Tags.SeriesInstanceUID, seriesInstanceUID);
        ds.putIS(Tags.SeriesNumber, seriesNumber);
        DcmElement sq = ds.putSQ(Tags.RefPPSSeq);
        if (refStudyComponent != null) {
            refStudyComponent.toDataset(sq.addNewItem());
        }
    }
}
