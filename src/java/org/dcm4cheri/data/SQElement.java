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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.VRs;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      May, 2002
 * @version    $Revision: 17485 $ $Date: 2012-12-07 14:15:32 +0100 (Fr, 07 Dez 2012) $
 */
class SQElement extends DcmElementImpl
{

    private final ArrayList list = new ArrayList();
    private final Dataset parent;
    private int totlen = -1;


    /**
     * Creates a new instance of ElementImpl
     *
     * @param  tag     Description of the Parameter
     * @param  parent  Description of the Parameter
     */
    public SQElement(int tag, Dataset parent)
    {
        super(tag);
        this.parent = parent;
    }
    
    public DcmElement share() {
        synchronized(list) {
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                ((Dataset) list.get(i)).shareElements();
            }
        }
        return this;
    }

    public int hashCode() {
        return tag ^ list.hashCode();
    }
    
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SQElement))
            return false;
        SQElement ve = (SQElement) o;
        if (tag != ve.tag)
            return false;
        return list.equals(ve.list);
    }
    
    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public final int vr()
    {
        return VRs.SQ;
    }

    public final int vm(SpecificCharacterSet cs)
    {
        return countItems();
    }

    public final int countItems()
    {
        return list.size();
    }

    public final boolean isEmpty()
    {
        return list.isEmpty();
    }    

    public final int getLength()
    {
        return list.isEmpty() ? 0 : -1;
    }    
    
    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public final boolean hasItems()
    {
        return true;
    }


    /**
     *  Gets the item attribute of the SQElement object
     *
     * @param  index  Description of the Parameter
     * @return        The item value
     */
    public Dataset getItem(int index)
    {
        if (index >= list.size()) {
            return null;
        }
        return (Dataset) list.get(index);
    }


    /**
     *  Adds a feature to the Item attribute of the SQElement object
     *
     * @param  item  The feature to be added to the Item attribute
     */
    public void addItem(Dataset item)
    {
        list.add(item);
    }


    /**
     *  Adds a feature to the NewItem attribute of the SQElement object
     *
     * @return    Description of the Return Value
     */
    public Dataset addNewItem()
    {
        Dataset item = new DatasetImpl(parent);
        list.add(item);
        return item;
    }


    /**
     *  Description of the Method
     *
     * @param  param  Description of the Parameter
     * @return        Description of the Return Value
     */
    public int calcLength(DcmEncodeParam param)
    {
        totlen = param.undefSeqLen ? 8 : 0;
        for (int i = 0, n = list.size(); i < n; ++i) {
            totlen += getItem(i).calcLength(param) +
                    (param.undefItemLen ? 16 : 8);
        }
        return totlen;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public int length()
    {
        return totlen;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(DICT.toString(tag));
        sb.append(",SQ[size=").append(list.size()).append("]");
        return sb.toString();
    }


    /**
     *  Description of the Method
     *
     * @param  key           Description of the Parameter
     * @param  ignorePNCase  Description of the Parameter
     * @param  keyCS         Description of the Parameter
     * @param  dsCS          Description of the Parameter
     * @return               Description of the Return Value
     */
    protected boolean matchValue(DcmElement key, boolean ignorePNCase, boolean ignoreEmpty,
    		SpecificCharacterSet keyCS, SpecificCharacterSet dsCS)
    {
        for (int i = 0, m = key.countItems(); i < m; ++i) {
            Dataset keys = key.getItem(i);
            for (int j = 0, n = list.size(); j < n; ++j) {
                if (getItem(j).match(keys, ignorePNCase, ignoreEmpty)) {
                    return true;
                }
            }
        }
        return false;
    }
}

