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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4cheri.imageio.plugins;

import org.apache.log4j.Logger;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date:: xxxx-xx-xx $
 * @since Mar 31, 2011
 */
class PatchJpegLS {

    private static final Logger log = Logger.getLogger(PatchJpegLS.class);

    private static final int SOI = 0xffd8;
    private static final int SOF55 = 0xfff7;
    private static final int LSE = 0xfff8;
    private static final int SOS = 0xffda;
    private static final byte[] LSE_13 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0x1f, (byte) 0xff,
        (byte) 0x00, (byte) 0x22,  // T1 = 34
        (byte) 0x00, (byte) 0x83,  // T2 = 131
        (byte) 0x02, (byte) 0x24,  // T3 = 548
        (byte) 0x00, (byte) 0x40,
    };
    private static final byte[] LSE_14 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0x3f, (byte) 0xff,
        (byte) 0x00, (byte) 0x42, // T1 = 66
        (byte) 0x01, (byte) 0x03, // T2 = 259
        (byte) 0x04, (byte) 0x44, // T3 = 1092
        (byte) 0x00, (byte) 0x40,
    };
    private static final byte[] LSE_15 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0x7f, (byte) 0xff,
        (byte) 0x00, (byte) 0x82, // T1 = 130
        (byte) 0x02, (byte) 0x03, // T2 = 515
        (byte) 0x08, (byte) 0x84, // T3 = 2180
        (byte) 0x00, (byte) 0x40,
    };
    private static final byte[] LSE_16 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0xff, (byte) 0xff,
        (byte) 0x01, (byte) 0x02, // T1 = 258
        (byte) 0x04, (byte) 0x03, // T2 = 1027
        (byte) 0x11, (byte) 0x04, // T3 = 4356
        (byte) 0x00, (byte) 0x40,
    };

    public static byte[] selectPatch(byte[] jpegheader) {
        if (toInt(jpegheader, 0) != SOI) {
            log.warn("SOI marker is missing - do not patch JPEG LS");
            return null;
        }
        int marker = toInt(jpegheader, 2);
        if (marker != SOF55) {
            log.warn(marker == LSE
                    ? "contains already LSE marker segment "
                            + "- do not patch JPEG LS"
                    : "SOI marker is not followed by JPEG-LS SOF marker "
                            + "- do not patch JPEG LS");
            return null;
        }
        if (toInt(jpegheader, 4) != 11) {
            log.warn("unexpected length of JPEG-LS SOF marker segment "
                    + "- do not patch JPEG LS");
            return null;
        }
        marker = toInt(jpegheader, 15);
        if (marker != SOS) {
            log.warn(marker == LSE
                ? "contains already LSE marker segment "
                    + "- do not patch JPEG LS"
                : "JPEG-LS SOF marker segment is not followed by SOS marker "
                    + "- do not patch JPEG LS");
            return null;
        }
        switch (jpegheader[6]) {
        case 13:
            log.info("Patch JPEG LS 13-bit with "
                    + "LSE segment(T1=34, T2=131, T3=548)");
            return LSE_13;
        case 14:
            log.info("Patch JPEG LS 14-bit with "
                    + "LSE segment(T1=66, T2=259, T3=1092)");
            return LSE_14;
        case 15:
            log.info("Patch JPEG LS 15-bit with "
                    + "LSE segment(T1=130, T2=515, T3=2180)");
            return LSE_15;
        case 16:
            log.info("Patch JPEG LS 16-bit with "
                    + "LSE segment(T1=258, T2=1027, T3=4356)");
            return LSE_16;
        }
        return null;
    }

    private static int toInt(byte[] b, int off) {
        return (b[off] & 0xff) << 8 | (b[off+1] & 0xff);
    }
}
