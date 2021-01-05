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

package org.dcm4che.net;

import org.dcm4che.data.Command;
import org.dcm4che.dict.Tags;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 * @since July 28, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class DcmServiceException extends Exception {
    
    private final int status;
    private int errorID = -1;
    private int actionTypeID = -1;
    private int eventTypeID = -1;
    
    public DcmServiceException(int status) {
        this.status = status;
    }    
    
    public DcmServiceException(int status, String msg) {
        super(msg);
        this.status = status;
    }

    public DcmServiceException(int status, String msg, Throwable cause) {
        super(msg, cause);
        this.status = status;
    }

    public DcmServiceException(int status, Throwable cause) {
        super(cause);
        this.status = status;
    }
    
    public int getStatus() {
        return status;
    }

    public DcmServiceException setErrorID(int errorID) {
        this.errorID = errorID;
        return this;
    }

    public int getErrorID() {
        return errorID;
    }

    public DcmServiceException setEventTypeID(int eventTypeID) {
        this.eventTypeID = eventTypeID;
        return this;
    }

    public int getEventTypeID() {
        return eventTypeID;
    }

    public DcmServiceException setActionTypeID(int actionTypeID) {
        this.actionTypeID = actionTypeID;
        return this;
    }

    public int getActionTypeID() {
        return actionTypeID;
    }

    public void writeTo(Command cmd) {
        cmd.putUS(Tags.Status, status);
        String msg = getMessage();
        if (msg != null && msg.length() > 0) {
            cmd.putLO(Tags.ErrorComment,
                toErrorComment(msg));
        }
        if (errorID >= 0) {
            cmd.putUS(Tags.ErrorID, errorID);
        }
        if (actionTypeID >= 0) {
            cmd.putUS(Tags.ActionTypeID, actionTypeID);
        }
        if (eventTypeID >= 0) {
            cmd.putUS(Tags.EventTypeID, eventTypeID);
        }
    }

	private String toErrorComment(String msg) {
		char[] a = msg.toCharArray();
		int len = Math.min(64, a.length);
		for (int i = 0; i < len; i++) {
			if (a[i] < 0x20 || a[i] > 0x126)
				a[i] = '?';
		}
		return new String(a, 0, len);
	}
}
