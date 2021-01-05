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

package org.dcm4cheri.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.VRs;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 3994 $ $Date: 2006-05-18 00:10:23 +0200 (Do, 18 Mai 2006) $
 * @since 20.09.2004
 *
 */
class ExcludePrivateSQElement extends DcmElementImpl implements DcmElement {

    private final SQElement sqElem;
    private int totlen = -1;

    public ExcludePrivateSQElement(SQElement sqElem) {
        super(sqElem.tag());
        this.sqElem = sqElem;
    }

    public final int vr() {
        return VRs.SQ;
    }

    public final int vm(SpecificCharacterSet cs) {
        return sqElem.vm(cs);
    }
    
    public final int countItems() {
        return sqElem.countItems();
    }
    
    public Dataset getItem(int index) {
        return new FilterDataset.ExcludePrivate(sqElem.getItem(index));
    }
    
    public int calcLength(DcmEncodeParam param) {
        totlen = param.undefSeqLen ? 8 : 0;
        for (int i = 0, n = countItems(); i < n; ++i)
            totlen += getItem(i).calcLength(param) +
                    (param.undefItemLen ? 16 : 8);
        return totlen;
    }
    
    public int length() {
        return totlen;
    }        
}
