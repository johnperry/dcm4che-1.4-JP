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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/** Defines behavior of <code>CommandSet</code> container objects.
 *
 * @see "DICOM Part 7: Message Exchange, 6.3.1 Command Set Structure"
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020726 gunter:</b>
 * <ul>
 * <li> Fix C_CANCEL_RQ value 
 * </ul>
 */
public interface Command extends DcmObject {
    
    // Constants -----------------------------------------------------
    public static final int C_STORE_RQ         = 0x0001;
    public static final int C_STORE_RSP        = 0x8001;
    public static final int C_GET_RQ           = 0x0010;
    public static final int C_GET_RSP          = 0x8010;
    public static final int C_FIND_RQ          = 0x0020;
    public static final int C_FIND_RSP         = 0x8020;
    public static final int C_MOVE_RQ          = 0x0021;
    public static final int C_MOVE_RSP         = 0x8021;
    public static final int C_ECHO_RQ          = 0x0030;
    public static final int C_ECHO_RSP         = 0x8030;
    public static final int N_EVENT_REPORT_RQ  = 0x0100;
    public static final int N_EVENT_REPORT_RSP = 0x8100;
    public static final int N_GET_RQ           = 0x0110;
    public static final int N_GET_RSP          = 0x8110;
    public static final int N_SET_RQ           = 0x0120;
    public static final int N_SET_RSP          = 0x8120;
    public static final int N_ACTION_RQ        = 0x0130;
    public static final int N_ACTION_RSP       = 0x8130;
    public static final int N_CREATE_RQ        = 0x0140;
    public static final int N_CREATE_RSP       = 0x8140;
    public static final int N_DELETE_RQ        = 0x0150;
    public static final int N_DELETE_RSP       = 0x8150;
    public static final int C_CANCEL_RQ        = 0x0FFF;

    public static final int MEDIUM             = 0x0000;
    public static final int HIGH               = 0x0001;
    public static final int LOW                = 0x0002;

    public static final int NO_DATASET         = 0x0101;
    
    public int getMessageID();

    public int getMessageIDToBeingRespondedTo();

    public String getAffectedSOPClassUID();

    public String getAffectedSOPInstanceUID();

    public String getRequestedSOPClassUID();

    public String getRequestedSOPInstanceUID();

    public int getCommandField();

    public int getStatus();

    public boolean isPending();

    public boolean isRequest();

    public boolean isResponse();

    public boolean hasDataset();

    public Command initCStoreRQ(int msgID, String sopClassUID,
            String sopInstUID, int priority);

    public Command setMoveOriginator(String aet, int msgID);
    
    public Command initCStoreRSP(int msgID, String sopClassUID,
            String sopInstUID, int status);

    public Command initCFindRQ(int msgID, String sopClassUID, int priority);
        
    public Command initCFindRSP(int msgID, String sopClassUID, int status);
    
    public Command initCCancelRQ(int msgID);

    public Command initCGetRQ(int msgID, String sopClassUID, int priority);

    public Command initCGetRSP(int msgID, String sopClassUID, int status);

    public Command initCMoveRQ(int msgID, String sopClassUID, int priority,
            String moveDest);

    public Command initCMoveRSP(int msgID, String sopClassUID, int status);

    public Command initCEchoRQ(int msgID, String sopClassUID);

    public Command initCEchoRQ(int msgID);
    
    public Command initCEchoRSP(int msgID, String sopClassUID, int status);
    
    public Command initCEchoRSP(int msgID);

    public Command initNEventReportRQ(int msgID, String sopClassUID,
            String sopInstanceUID, int eventTypeID);
    
    public Command initNEventReportRSP(int msgID, String sopClassUID,
            String sopInstUID, int status);
    
    public Command initNGetRQ(int msgID, String sopClassUID,
            String sopInstUID, int[] attrIDs);
    
    public Command initNGetRSP(int msgID, String sopClassUID,
            String sopInstUID, int status);
    
    public Command initNSetRQ(int msgID, String sopClassUID,
            String sopInstUID);
    
    public Command initNSetRSP(int msgID, String sopClassUID,
            String sopInstUID, int status);
    
    public Command initNActionRQ(int msgID, String sopClassUID,
            String sopInstUID, int actionTypeID);
    
    public Command initNActionRSP(int msgID, String sopClassUID,
            String sopInstUID, int status) ;

    public Command initNCreateRQ(int msgID, String sopClassUID,
            String sopInstanceUID);

    public Command initNCreateRSP(int msgID, String sopClassUID,
            String sopInstUID, int status) ;

    public Command initNDeleteRQ(int msgID, String sopClassUID,
            String sopInstUID);
    
    public Command initNDeleteRSP(int msgID, String sopClassUID,
            String sopInstUID, int status);
    
    public void write(DcmHandler handler) throws IOException;

    public void write(OutputStream out) throws IOException;

    public void read(InputStream in) throws IOException;
    
    public String cmdFieldAsString();
}

