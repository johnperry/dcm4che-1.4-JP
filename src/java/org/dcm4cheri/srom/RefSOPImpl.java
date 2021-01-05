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
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.Tags;

import org.dcm4che.srom.RefSOP;

import org.apache.log4j.Logger;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class RefSOPImpl implements org.dcm4che.srom.RefSOP {
    static Logger log = Logger.getLogger(RefSOPImpl.class);
    // Constants -----------------------------------------------------
    private static UIDDictionary uidDict =
            DictionaryFactory.getInstance().getDefaultUIDDictionary( );   

    // Attributes ----------------------------------------------------
    private final String refSOPClassUID;
    private final String refSOPInstanceUID;

    // Constructors --------------------------------------------------
    public RefSOPImpl(String refSOPClassUID, String refSOPInstanceUID) {
        if (refSOPClassUID.length() == 0)
            throw new IllegalArgumentException(refSOPClassUID);
    
        if (refSOPInstanceUID.length() == 0)
            throw new IllegalArgumentException(refSOPInstanceUID);
    
        this.refSOPClassUID = refSOPClassUID;
        this.refSOPInstanceUID = refSOPInstanceUID;
    }

    public RefSOPImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.RefSOPClassUID),
            ds.getString(Tags.RefSOPInstanceUID));
    }
    
    private static boolean hasValue(String s) {
        return s != null && s.length() > 0;
    }
    
    public static RefSOP newRefSOP(Dataset ds) throws DcmValueException {
        if (ds == null) {
            return null;
        }
        String cuid;
        String iuid;
        if (!hasValue(cuid = ds.getString(Tags.RefSOPClassUID))) {
            log.warn("Missing Ref SOP Class UID - ignore reference");
            return null;
        }
        if (!hasValue(iuid = ds.getString(Tags.RefSOPInstanceUID))) {
            log.warn("Missing Ref SOP Instance UID - ignore reference");
            return null;
        }
        return new RefSOPImpl(cuid, iuid);
    }
    
    // Public --------------------------------------------------------
    public String toString() {
        return uidDict.lookup(refSOPClassUID) + "[" + refSOPInstanceUID + "]"; 
    }

    public void toDataset(Dataset ds) {
        ds.putUI(Tags.RefSOPClassUID, refSOPClassUID);
        ds.putUI(Tags.RefSOPInstanceUID, refSOPInstanceUID);
    }    
    
    public final String getRefSOPClassUID() {
        return refSOPClassUID;
    }
    
    public final String getRefSOPInstanceUID() {
        return refSOPInstanceUID;
    }
    
    public final int hashCode() {
        return refSOPInstanceUID.hashCode();
    }
    
    public final boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof RefSOP))
            return false;
    
        RefSOP refSOP = (RefSOP)o;
        return refSOPInstanceUID.equals(refSOP.getRefSOPInstanceUID());
    }
}
