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

import java.util.ListResourceBundle;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmMetadataFormatResources extends ListResourceBundle {

    static final Object[][] contents = {
        // Node name, followed by description
        { "filemetainfo", "DICOM File Meta Information" },
        { "dataset", "Data Set with Data Elements, except the Pixel Data" },
        { "elm", "Data Element, contains single Value or Sequence" },
        { "val", "Value of Data Element" },
        { "seq", "Sequence of Items or Data Fragments" },
        { "item", "Sequence Item, contains nested Data Elements" },
        { "frag", "Data Fragment" },

        // Node name + "/" + AttributeName, followed by description
        { "filemetainfo/preamble", "128 byte File Preamble" },
        { "elm/pos", "offset of first byte of this attribute in stream" },
        { "elm/tag", "data element tag as 4 byte hex number" },
        { "elm/vr", "value representation" },
        { "elm/name", "attribute name" },
        { "val/vm", "number of entries in the value data attribute" },
        { "val/len", "value length" },
        { "val/data", "value data" },
        { "seq/len", "sequence length or -1 for undefined length" },
        { "item/pos", "offset of first byte of this item in stream" },
        { "item/id", "item index, starting with 1" },
        { "item/len", "item length or -1 for undefined length" },
        { "frag/pos", "offset of first byte of this fragment in stream" },
        { "frag/id", "data fragment index, starting with 1" },
        { "frag/len", "data fragment length" },
        { "frag/data", "fragment data" },
    };

    public DcmMetadataFormatResources() {}

    public Object[][] getContents() {
        return contents;
    }
}
