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

import org.dcm4che.Implementation;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class DcmObjectFactoryImpl extends DcmObjectFactory {

    /** Creates a new instance of DcmParserFactoryImpl */
    public DcmObjectFactoryImpl() {
    }

    public Command newCommand() {
        return new CommandImpl();
    }    

    public Dataset newDataset() {
        return new DatasetImpl();
    }    

    public FileMetaInfo newFileMetaInfo() {
        return new FileMetaInfoImpl();
    }    

    public FileMetaInfo newFileMetaInfo(String sopClassUID,
            String sopInstanceUID, String transferSyntaxUID,
            String implClassUID, String implVersName) {
        return new FileMetaInfoImpl().init(sopClassUID, sopInstanceUID,
                transferSyntaxUID, implClassUID, implVersName);
    }

    public FileMetaInfo newFileMetaInfo(String sopClassUID,
            String sopInstanceUID, String transferSyntaxUID) {
        return new FileMetaInfoImpl().init(sopClassUID, sopInstanceUID,
                transferSyntaxUID, 
                Implementation.getClassUID(),
                Implementation.getVersionName());
    }

    public PersonName newPersonName(String s) {
        return new PersonNameImpl(s, false);
    }
    
    public PersonName newPersonName() {
        return new PersonNameImpl();
    }
    
    public FileMetaInfo newFileMetaInfo(Dataset ds, String transferSyntaxUID) {
            return new FileMetaInfoImpl().init(
                    ds.getString(Tags.SOPClassUID, null),
                    ds.getString(Tags.SOPInstanceUID, null),
                    transferSyntaxUID,
                    Implementation.getClassUID(),
                    Implementation.getVersionName());
    }     
}
