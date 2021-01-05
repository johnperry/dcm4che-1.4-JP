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

package org.dcm4cheri.imageio.plugins;

import org.dcm4che.imageio.plugins.DcmImageReadParam;

/**
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $
 * @since November 21, 2002
 *
 */
public class DcmImageReadParamImpl extends DcmImageReadParam {

    private byte[] pvalToDDL = null;

    private boolean maskPixelData = true;
    private boolean autoWindowing = true;

    public byte[] getPValToDDL() {
        return pvalToDDL;
    }

    public void setPValToDDL(byte[] pvalToDDL) {
        if (pvalToDDL != null) {
            checkLen(pvalToDDL.length);
        }
        this.pvalToDDL = pvalToDDL;
    }

    public final boolean isMaskPixelData() {
        return maskPixelData;
    }

    public final void setMaskPixelData(boolean mask) {
        this.maskPixelData = mask;
    }

	public final boolean isAutoWindowing() {
		return autoWindowing;
	}

	public final void setAutoWindowing(boolean autoWindowing) {
		this.autoWindowing = autoWindowing;
	}
    
    private final static void checkLen(int len) {
        for (int n = 0x100; n <= 0x10000; n <<= 1) {
            if (n == len)
                return;
        }
        throw new IllegalArgumentException("pvalToDDL length: " + len);
    }
}
