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

package org.dcm4che.dict;


/** 
 * Provides VR constants and VR related utility functions.
 *
 * <p>
 * Further Information regarding Value Representation (DICOM data types)
 * can be found at: <br>
 * <code>PS 3.5 - 2000 Section 6.2 Page 15</code>
 * </p>
 * 
 * @author gunter.zeilinger@tiani.com (Gunter Zeilinger)
 * @author hauer@psicode.com (Sebastian Hauer)
 * @version 1.0
 * @since version 0.1
 */
public class VRs  {
  
    /**
     * Private constructor.
     */
    private VRs() {}
  
    public static String toString(int vr) {
        return (vr == NONE
                ? "NONE"
                : new String(new byte[]{(byte)(vr>>8), (byte)(vr)}));
    }

    public static int valueOf(String str) {
        if ("NONE".equals(str))
            return VRs.NONE;
        
        if (str.length() != 2)
            throw new IllegalArgumentException(str);
        
        return ((str.charAt(0) & 0xff) << 8) | (str.charAt(1) & 0xff);
    }
    
    /**
     * NULL element for VRs. Use as VR value for Data Elements, 
     * Item (FFFE,E000), Item Delimitation Item (FFFE,E00D), and
     * Sequence Delimitation Item (FFFE,E0DD).
     */
    public static final int NONE = 0x0000;
    
    public static final int UN_SIEMENS = 0x3F3F;
    
    public static final int AE = 0x4145;
    
    public static final int AS = 0x4153;
    
    public static final int AT = 0x4154;
    
    public static final int CS = 0x4353;
    
    public static final int DA = 0x4441;
    
    public static final int DS = 0x4453;
    
    public static final int DT = 0x4454;
    
    public static final int FL = 0x464C;
    
    public static final int FD = 0x4644;
    
    public static final int IS = 0x4953;
    
    public static final int LO = 0x4C4F;
    
    public static final int LT = 0x4C54;
    
    public static final int OB = 0x4F42;
    
    public static final int OF = 0x4F46;

    public static final int OW = 0x4F57;
    
    public static final int PN = 0x504E;
    
    public static final int SH = 0x5348;
    
    public static final int SL = 0x534C;
    
    public static final int SQ = 0x5351;
    
    public static final int SS = 0x5353;
    
    public static final int ST = 0x5354;
    
    public static final int TM = 0x544D;
    
    public static final int UC = 0x5543;
    
    public static final int UI = 0x5549;
    
    public static final int UL = 0x554C;
    
    public static final int UN = 0x554E;
    
    public static final int US = 0x5553;
    
    public static final int UT = 0x5554;
        
    public static boolean isLengthField16Bit(int vr) {
        switch (vr) {
            case UN_SIEMENS:
            case AE: case AS: case AT: case CS: case DA: case DS: case DT:
            case FL: case FD: case IS: case LO: case LT: case PN: case SH:
            case SL: case SS: case ST: case TM: case UI: case UL: case US:
                return true;
            default:
                return false;
        }
    }//end isLengthField16Bit()
  
    public static int getPadding(int vr) {
        switch (vr) {
            case AE: case AS: case CS: case DA: case DS: case DT: case IS:
            case LO: case LT: case PN: case SH: case SL: case ST: case TM:
            case UC: case UT:
                return ' ';
            default:
                return 0;
        }
    }
    
    public static boolean isStringValue(int vr) {
        switch (vr) {
            case AE: case AS: case CS: case DA: case DS: case DT: case IS:
            case LO: case LT: case PN: case SH: case ST: case TM: case UI:
            case UC: case UT:
                return true;
        }
        return false;
    }
    
}//end class VR
