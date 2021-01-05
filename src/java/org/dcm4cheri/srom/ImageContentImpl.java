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

import org.dcm4che.srom.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class ImageContentImpl extends CompositeContentImpl implements ImageContent {
    // Constants -----------------------------------------------------
    private static final int[] NULL_FRAMENUMBER = {};
    
    // Attributes ----------------------------------------------------
    protected int[] frameNumbers;
    protected RefSOP refPresentationSOP;
    protected IconImage iconImage;

    // Constructors --------------------------------------------------
    ImageContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, RefSOP refSOP, int[] frameNumbers,
            RefSOP refPresentationSOP, IconImage iconImage) {
        super(owner, obsDateTime, template, name, refSOP);
        setFrameNumbers(frameNumbers);
        this.refPresentationSOP = refPresentationSOP;
        this.iconImage = iconImage;
    }
    
    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        return new ImageContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime), template,
                name, refSOP, frameNumbers, refPresentationSOP, iconImage);
    }

    // Methodes --------------------------------------------------------
    public String toString() {
        StringBuffer sb = prompt().append(refSOP);
        for (int i = 0; i < frameNumbers.length; ++i)
            sb.append(",[").append(frameNumbers[0]).append("]");

        if (refPresentationSOP != null)
            sb.append(",").append(refPresentationSOP);

        return sb.append(')').toString();
    }

    public final ValueType getValueType() {
        return ValueType.IMAGE;
    }    
    
    public final int[] getFrameNumbers() {
        return (int[])frameNumbers.clone();
    }
    
    public final void setFrameNumbers(int[] frameNumbers) {
        this.frameNumbers = frameNumbers != null
                ? (int[])frameNumbers.clone() : NULL_FRAMENUMBER;
    }
    
    public final RefSOP getRefPresentationSOP() {
        return refPresentationSOP;
    }

    public final void setRefPresentationSOP(RefSOP refPresentationSOP) {
        this.refPresentationSOP = refPresentationSOP;
    }
    
    public final IconImage getIconImage() {
        return iconImage;
    }
    
    public final void setIconImage(IconImage iconImage) {
        this.iconImage = iconImage;
    }
    
    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        if (frameNumbers.length == 0 && refPresentationSOP == null) {
            return;
        }

        Dataset sop = ds.get(Tags.RefSOPSeq).getItem();
        if (frameNumbers.length != 0) {
            sop.putIS(Tags.RefFrameNumber, frameNumbers);
        }

        if (refPresentationSOP != null) {
            refPresentationSOP.toDataset(
                    sop.putSQ(Tags.RefSOPSeq).addNewItem());
        }

        if (iconImage != null) {
            iconImage.toDataset(
                    sop.putSQ(Tags.IconImageSeq).addNewItem());
        }
    }
    
}
