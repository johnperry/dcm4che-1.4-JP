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

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class FileFormat {

    public final boolean hasPreamble;
    
    public final boolean hasFileMetaInfo;

    public final boolean mglib;
    
    public final DcmDecodeParam decodeParam;
    
    private FileFormat(boolean hasPreamble, boolean hasFileMetaInfo,
            DcmDecodeParam decodeParam, boolean mglib) {
        if (hasPreamble && !hasFileMetaInfo) {
            throw new IllegalArgumentException("Preamble without FMI");
        }
        this.hasPreamble = hasPreamble;
        this.hasFileMetaInfo = hasFileMetaInfo;
        this.decodeParam = decodeParam;
        this.mglib = mglib;
    }
    
    public String toString() {
        return "FileFormat["
            + (hasFileMetaInfo ? 
                        (hasPreamble ? "Part 10,"
                                     : "FMI without preamble,")
                               : "Stream, ")
            + decodeParam.toString() + "]";
    }
    
    public static final FileFormat DICOM_FILE =
            new FileFormat(true, true, DcmDecodeParam.EVR_LE, false);

    public static final FileFormat DICOM_FILE_WO_PREAMBLE =
            new FileFormat(false, true, DcmDecodeParam.EVR_LE, false);

    public static final FileFormat EVR_LE_STREAM =
            new FileFormat(false, false, DcmDecodeParam.EVR_LE, false);

    public static final FileFormat EVR_BE_FILE =
            new FileFormat(true, true, DcmDecodeParam.EVR_BE, false);

    public static final FileFormat EVR_BE_FILE_WO_PREAMBLE =
            new FileFormat(false, true, DcmDecodeParam.EVR_BE, false);

    public static final FileFormat EVR_BE_STREAM =
            new FileFormat(false, false, DcmDecodeParam.EVR_BE, false);

    public static final FileFormat IVR_BE_FILE =
            new FileFormat(true, true, DcmDecodeParam.IVR_BE, false);

    public static final FileFormat IVR_BE_FILE_WO_PREAMBLE =
            new FileFormat(false, true, DcmDecodeParam.IVR_BE, false);

    public static final FileFormat IVR_BE_STREAM =
            new FileFormat(false, false, DcmDecodeParam.IVR_BE, false);

    public static final FileFormat IVR_LE_FILE =
            new FileFormat(true, true, DcmDecodeParam.IVR_LE, false);

    public static final FileFormat IVR_LE_FILE_WO_PREAMBLE =
            new FileFormat(false, true, DcmDecodeParam.IVR_LE, false);

    public static final FileFormat ACRNEMA_STREAM =
            new FileFormat(false, false, DcmDecodeParam.IVR_LE, false);

    public static final FileFormat MGLIB =
            new FileFormat(false, false, DcmDecodeParam.IVR_LE, true);
    
    public static final FileFormat MGLIB_COMPRESSED =
    		new FileFormat(false, false, DcmDecodeParam.IVR_LE, true);
}
