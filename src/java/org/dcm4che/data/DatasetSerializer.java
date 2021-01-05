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

package org.dcm4che.data;

import java.io.IOException;

import org.dcm4che.dict.UIDs;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class DatasetSerializer implements java.io.Serializable {
    
    static final long serialVersionUID =  -4404056689087154718L;

    private static final int ITEM_DELIMITATION_ITEM_TAG = 0xFFFEE00D;
    private static final byte[] ITEM_DELIMITATION_ITEM_ELEM_LE = { 
            (byte) 0xFE, (byte) 0xFF, (byte) 0x0D, (byte) 0xE0, 0, 0, 0, 0};
    private static final byte[] ITEM_DELIMITATION_ITEM_ELEM_BE = { 
            (byte) 0xFF, (byte) 0xFE, (byte) 0xE0, (byte) 0x0D, 0, 0, 0, 0};

    private transient Dataset ds;
    
    public DatasetSerializer() {}

    public DatasetSerializer(Dataset ds) {
        this.ds = ds; 
    }
    
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        FileMetaInfo fmi = ds.getFileMetaInfo();
        if (fmi == null) {
            out.writeBoolean(false);
            ds.writeFile(out, DcmEncodeParam.EVR_LE);
            out.write(ITEM_DELIMITATION_ITEM_ELEM_LE);
        } else {
            out.writeBoolean(true);
            ds.writeFile(out, null);
            out.write(fmi.getTransferSyntaxUID() == UIDs.ExplicitVRBigEndian
                    ? ITEM_DELIMITATION_ITEM_ELEM_BE
                    : ITEM_DELIMITATION_ITEM_ELEM_LE);
            
        }
    }
    
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        ds = DcmObjectFactory.getInstance().newDataset();
        ds.readFile(in,in.readBoolean() ? FileFormat.DICOM_FILE
                                         : FileFormat.EVR_LE_STREAM,
                ITEM_DELIMITATION_ITEM_TAG);
    }
    
    private Object readResolve() throws java.io.ObjectStreamException {
        return ds;
    }        
}
