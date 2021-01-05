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

package org.dcm4che.imageio.plugins;

import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmMetadataFormat extends IIOMetadataFormatImpl {

    private static IIOMetadataFormat instance = null;
    private static final String[] VRs = {
        "AE", "AS", "AT", "CS", "DA", "DS", "DT", "FL", "FD", "IS", "LO", "LT",
        "OB", "OW", "PN", "SH", "SL", "SQ", "SS", "ST", "TM", "UI", "UL", "UN",
        "US", "UT"
    };
    private static final List VR_LIST = Arrays.asList(VRs);

    private DcmMetadataFormat() {
        super(DcmMetadata.nativeMetadataFormatName,
              CHILD_POLICY_SOME);

        addElement("filemetainfo", DcmMetadata.nativeMetadataFormatName,
                CHILD_POLICY_REPEAT);
        addAttribute("filemetainfo", "preamble", DATATYPE_STRING, false, null); 

        addElement("dataset", DcmMetadata.nativeMetadataFormatName,
                CHILD_POLICY_REPEAT);

        addElement("elm", "dataset", CHILD_POLICY_CHOICE);
        addAttribute("elm", "pos", DATATYPE_INTEGER, true, null);
        addAttribute("elm", "tag", DATATYPE_STRING, true, null); 
        addAttribute("elm", "vr", DATATYPE_STRING, true, null, VR_LIST);
        addAttribute("elm", "name", DATATYPE_STRING, false, null);

        addChildElement("elm", "filemetainfo");
        
        addElement("val", "elm", CHILD_POLICY_EMPTY);
        addAttribute("val", "vm", DATATYPE_INTEGER, true, null); 
        addAttribute("val", "len", DATATYPE_INTEGER, true, null); 
        addAttribute("val", "data", DATATYPE_STRING, true, null); 

        addElement("seq", "elm", CHILD_POLICY_SEQUENCE);
        addAttribute("seq", "len", DATATYPE_INTEGER, true, null); 
        
        addElement("item", "seq", CHILD_POLICY_REPEAT);
        addAttribute("item", "pos", DATATYPE_INTEGER, true, null);
        addAttribute("item", "id", DATATYPE_INTEGER, true, null); 
        addAttribute("item", "len", DATATYPE_INTEGER, true, null); 

        addChildElement("elm", "item");
        
        addElement("frag", "seq", CHILD_POLICY_EMPTY);
        addAttribute("frag", "pos", DATATYPE_INTEGER, true, null);
        addAttribute("frag", "id", DATATYPE_INTEGER, true, null); 
        addAttribute("frag", "len", DATATYPE_INTEGER, true, null); 
        addAttribute("frag", "data", DATATYPE_STRING, true, null); 
    }

    public boolean canNodeAppear(String elementName,
                                 ImageTypeSpecifier imageType) {
        return true;
    }

    public static synchronized IIOMetadataFormat getInstance() {
        if (instance == null) {
            instance = new DcmMetadataFormat();
        }
        return instance;
    }
}
