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

import org.dcm4che.data.*;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/** Defines behavior of <code>Command</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 7: Message Exchange, 6.3.1 Command Set Structure"
 */
final class CommandImpl extends DcmObjectImpl implements Command {
   
   private int cmdField = -1;
   private int dataSetType = -1;
   private int status = -1;
   private int msgID = -1;
   private String sopClassUID = null;
   private String sopInstUID = null;

   public int getCommandField() {
      return cmdField;
   }

   public int getMessageID() {
      return msgID;
   }
   
   public int getMessageIDToBeingRespondedTo() {
      return msgID;
   }
   
   public String getAffectedSOPClassUID() {
      return sopClassUID;
   }
   
   public String getRequestedSOPClassUID() {
      return sopClassUID;
   }
   
   public String getAffectedSOPInstanceUID() {
      return sopInstUID;
   }
   
   public String getRequestedSOPInstanceUID() {
      return sopInstUID;
   }
   
   public int getStatus() {
      return status;
   }
   
   public boolean isPending() {
      switch (status) {
         case 0xff00:
         case 0xff01:
            return true;
      }
      return false;
   }
   
   public boolean isRequest() {
      switch (cmdField) {
         case C_STORE_RQ:
         case C_GET_RQ:
         case C_FIND_RQ:
         case C_MOVE_RQ:
         case C_ECHO_RQ:
         case N_EVENT_REPORT_RQ:
         case N_GET_RQ:
         case N_SET_RQ:
         case N_ACTION_RQ:
         case N_CREATE_RQ:
         case N_DELETE_RQ:
         case C_CANCEL_RQ:
            return true;
      }
      return false;
   }
    
   public boolean isResponse() {
      switch (cmdField) {
         case C_STORE_RSP:
         case C_GET_RSP:
         case C_FIND_RSP:
         case C_MOVE_RSP:
         case C_ECHO_RSP:
         case N_EVENT_REPORT_RSP:
         case N_GET_RSP:
         case N_SET_RSP:
         case N_ACTION_RSP:
         case N_CREATE_RSP:
         case N_DELETE_RSP:
            return true;
      }
      return false;
   }
   
   public boolean hasDataset() {
      if (dataSetType == -1)
         throw new IllegalStateException();
      
      return dataSetType != NO_DATASET;
   }
   
      
   private Command initCxxxxRQ(int cmd, int msgID, String sopClassUID,
   int priority) {
      if (priority != MEDIUM &&  priority != HIGH &&  priority != LOW) {
         throw new IllegalArgumentException("priority=" + priority);
      }
      if (sopClassUID.length() == 0) {
         throw new IllegalArgumentException();
      }
      putUI(Tags.AffectedSOPClassUID, sopClassUID);
      putUS(Tags.CommandField, cmd);
      putUS(Tags.MessageID, msgID);
      putUS(Tags.Priority, priority);
      return this;
   }
   
   private Command initCxxxxRSP(int cmd, int msgID, String sopClassUID,
   int status) {
      if (sopClassUID != null) {
         putUI(Tags.AffectedSOPClassUID, sopClassUID);
      }
      putUS(Tags.CommandField, cmd);
      putUS(Tags.MessageIDToBeingRespondedTo, msgID);
      putUS(Tags.Status, status);
      return this;
   }
   
   public Command initCStoreRQ(int msgID, String sopClassUID,
   String sopInstUID, int priority) {
      if (sopInstUID.length() == 0) {
         throw new IllegalArgumentException();
      }
      initCxxxxRQ(C_STORE_RQ, msgID, sopClassUID, priority);
      putUI(Tags.AffectedSOPInstanceUID, sopInstUID);
      return this;
   }
   
   public Command setMoveOriginator(String aet, int msgID) {
      if (aet.length() == 0) {
         throw new IllegalArgumentException();
      }
      putAE(Tags.MoveOriginatorAET, aet);
      putUS(Tags.MoveOriginatorMessageID, msgID);
      return this;
   }
   
   public Command initCStoreRSP(int msgID, String sopClassUID,
   String sopInstUID, int status) {
      return initNxxxxRSP(C_STORE_RSP, msgID, sopClassUID, sopInstUID,
      status);
   }
   
   public Command initCFindRQ(int msgID, String sopClassUID, int priority) {
      return initCxxxxRQ(C_FIND_RQ, msgID, sopClassUID, priority);
   }
   
   public Command initCFindRSP(int msgID, String sopClassUID, int status) {
      return initCxxxxRSP(C_FIND_RSP, msgID, sopClassUID, status);
   }
   
   public Command initCCancelRQ(int msgID) {
      putUS(Tags.CommandField, C_CANCEL_RQ);
      putUS(Tags.MessageIDToBeingRespondedTo, msgID);
      return this;
   }
   
   public Command initCGetRQ(int msgID, String sopClassUID, int priority) {
      return initCxxxxRQ(C_GET_RQ, msgID, sopClassUID, priority);
   }
   
   public Command initCGetRSP(int msgID, String sopClassUID, int status) {
      return initCxxxxRSP(C_GET_RSP, msgID, sopClassUID, status);
   }
   
   public Command initCMoveRQ(int msgID, String sopClassUID, int priority,
   String moveDest) {
      if (moveDest.length() == 0) {
         throw new IllegalArgumentException();
      }
      initCxxxxRQ(C_MOVE_RQ, msgID, sopClassUID, priority);
      putAE(Tags.MoveDestination, moveDest);
      return this;
   }
   
   public Command initCMoveRSP(int msgID, String sopClassUID, int status) {
      return initCxxxxRSP(C_MOVE_RSP, msgID, sopClassUID, status);
   }
   
   public Command initCEchoRQ(int msgID, String sopClassUID) {
      if (sopClassUID.length() == 0) {
         throw new IllegalArgumentException();
      }
      putUI(Tags.AffectedSOPClassUID, sopClassUID);
      putUS(Tags.CommandField, C_ECHO_RQ);
      putUS(Tags.MessageID, msgID);
      return this;
   }
   
   public Command initCEchoRQ(int msgID) {
      return initCEchoRQ(msgID, UIDs.Verification);
   }
   
   public Command initCEchoRSP(int msgID, String sopClassUID, int status) {
      return initCxxxxRSP(C_ECHO_RSP, msgID, sopClassUID, status);
   }
   
   public Command initCEchoRSP(int msgID) {
      return initCxxxxRSP(C_ECHO_RSP, msgID, UIDs.Verification, 0);
   }
   
   private Command initNxxxxRQ(int cmd, int msgID, String sopClassUID,
         String sopInstanceUID) {
      if (sopClassUID.length() == 0) {
         throw new IllegalArgumentException();
      }
      if (sopInstanceUID.length() == 0) {
         throw new IllegalArgumentException();
      }
      putUI(Tags.RequestedSOPClassUID, sopClassUID);
      putUS(Tags.CommandField, cmd);
      putUS(Tags.MessageID, msgID);
      putUI(Tags.RequestedSOPInstanceUID, sopInstanceUID);
      return this;
   }
   
   private Command initNxxxxRSP(int cmd, int msgID, String sopClassUID,
         String sopInstanceUID, int status) {
      if (sopClassUID != null) {
         putUI(Tags.AffectedSOPClassUID, sopClassUID);
      }
      putUS(Tags.CommandField, cmd);
      putUS(Tags.MessageIDToBeingRespondedTo, msgID);
      putUS(Tags.Status, status);
      if (sopInstanceUID != null) {
         putUI(Tags.AffectedSOPInstanceUID, sopInstanceUID);
      }
      return this;
   }
   
   public Command initNEventReportRQ(int msgID, String sopClassUID,
         String sopInstanceUID, int eventTypeID) {
      if (sopClassUID.length() == 0) {
         throw new IllegalArgumentException();
      }
      if (sopInstanceUID.length() == 0) {
         throw new IllegalArgumentException();
      }
      putUI(Tags.AffectedSOPClassUID, sopClassUID);
      putUS(Tags.CommandField, N_EVENT_REPORT_RQ);
      putUS(Tags.MessageID, msgID);
      putUI(Tags.AffectedSOPInstanceUID, sopInstanceUID);
      putUS(Tags.EventTypeID, eventTypeID);
      return this;
   }
   
   public Command initNEventReportRSP(int msgID, String sopClassUID,
         String sopInstUID, int status) {
      return initNxxxxRSP(N_EVENT_REPORT_RSP, msgID, sopClassUID, sopInstUID,
         status);
   }
   
   public Command initNGetRQ(int msgID, String sopClassUID,
         String sopInstUID, int[] attrIDs) {
      initNxxxxRQ(N_GET_RQ, msgID, sopClassUID, sopInstUID);
      if (attrIDs != null) {
         putAT(Tags.AttributeIdentifierList, attrIDs);
      }
      return this;
   }
   
   public Command initNGetRSP(int msgID, String sopClassUID,
         String sopInstUID, int status) {
      return initNxxxxRSP(N_GET_RSP, msgID, sopClassUID, sopInstUID, status);
   }
   
   public Command initNSetRQ(int msgID, String sopClassUID,
         String sopInstUID) {
      return initNxxxxRQ(N_SET_RQ, msgID, sopClassUID, sopInstUID);
   }
   
   public Command initNSetRSP(int msgID, String sopClassUID,
         String sopInstUID, int status) {
      return initNxxxxRSP(N_SET_RSP, msgID, sopClassUID, sopInstUID, status);
   }
   
   public Command initNActionRQ(int msgID, String sopClassUID,
         String sopInstUID, int actionTypeID) {
      initNxxxxRQ(N_ACTION_RQ, msgID, sopClassUID, sopInstUID);
      putUS(Tags.ActionTypeID, actionTypeID);
      return this;
   }
   
   public Command initNActionRSP(int msgID, String sopClassUID,
         String sopInstUID, int status) {
      return initNxxxxRSP(N_ACTION_RSP, msgID, sopClassUID, sopInstUID,
      status);
   }
   
   public Command initNCreateRQ(int msgID, String sopClassUID,
         String sopInstanceUID) {
      if (sopClassUID.length() == 0) {
         throw new IllegalArgumentException();
      }
      putUI(Tags.AffectedSOPClassUID, sopClassUID);
      putUS(Tags.CommandField, N_CREATE_RQ);
      putUS(Tags.MessageID, msgID);
      if (sopInstanceUID != null) {
         putUI(Tags.AffectedSOPInstanceUID, sopInstanceUID);
      }
      return this;
   }
   
   public Command initNCreateRSP(int msgID, String sopClassUID,
         String sopInstUID, int status) {
      return initNxxxxRSP(N_CREATE_RSP, msgID, sopClassUID, sopInstUID,
      status);
   }
   
   public Command initNDeleteRQ(int msgID, String sopClassUID,
   String sopInstUID) {
      return initNxxxxRQ(N_DELETE_RQ, msgID, sopClassUID, sopInstUID);
   }
   
   public Command initNDeleteRSP(int msgID, String sopClassUID,
         String sopInstUID, int status) {
      return initNxxxxRSP(N_DELETE_RSP, msgID, sopClassUID, sopInstUID,
      status);
   }
   
   protected DcmElement put(DcmElement newElem) {
      int tag = newElem.tag();
      if ((tag & 0xFFFF0000) != 0x00000000)
         throw new IllegalArgumentException(newElem.toString());
/*
        if (newElem.getByteBuffer().order() != ByteOrder.LITTLE_ENDIAN)
            throw new IllegalArgumentException(
                    newElem.getByteBuffer().toString());
 */
      try {
         switch (tag) {
            case Tags.AffectedSOPClassUID:
            case Tags.RequestedSOPClassUID:
               sopClassUID = newElem.getString(null);
               break;
            case Tags.CommandField:
               cmdField = newElem.getInt();
               break;
            case Tags.MessageID:
            case Tags.MessageIDToBeingRespondedTo:
               msgID = newElem.getInt();
               break;
            case Tags.DataSetType:
               dataSetType = newElem.getInt();
               break;
            case Tags.Status:
               status = newElem.getInt();
               break;
            case Tags.AffectedSOPInstanceUID:
            case Tags.RequestedSOPInstanceUID:
               sopInstUID = newElem.getString(null);
               break;
         }
      } catch (DcmValueException ex) {
         throw new IllegalArgumentException(newElem.toString());
      }
      return super.put(newElem);
   }
   
   public int length() {
      return grLen() + 12;
   }
   
   private int grLen() {
      int len = 0;
      for (int i = 0, n = list.size(); i < n; ++i)
         len += ((DcmElement)list.get(i)).length() + 8;
      
      return len;
   }
   
   public void write(DcmHandler handler) throws IOException {
      handler.setDcmDecodeParam(DcmDecodeParam.IVR_LE);
      write(0x00000000, grLen(), handler);
   }
   
   public void write(OutputStream out) throws IOException {
      write(new DcmStreamHandlerImpl(out));
   }
   
   public void read(InputStream in) throws IOException {
      DcmParserImpl parser = new DcmParserImpl(in);
      parser.setDcmHandler(getDcmHandler());
      parser.parseCommand();
   }
   
    public String toString() {
        return toStringBuffer(new StringBuffer()).toString();
    }
    
    private StringBuffer toStringBuffer(StringBuffer sb) {
       String s;
       Integer i;
       sb.append(msgID).append(':').append(cmdFieldAsString());
       if (dataSetType != NO_DATASET)
          sb.append(" with Dataset");
       if (sopClassUID != null)
          sb.append("\n\tclass:\t").append(DICT.lookup(sopClassUID));
       if (sopInstUID != null)
          sb.append("\n\tinst:\t").append(DICT.lookup(sopInstUID));
       if (status != -1)
          sb.append("\n\tstatus:\t").append(Integer.toHexString(status));
       if ((s = getString(Tags.ErrorComment)) != null)
           sb.append("\n\terror comment:\t").append(s);
       if ((s = getString(Tags.MoveDestination)) != null)
          sb.append("\n\tmove dest:\t").append(s);
       if ((i = getInteger(Tags.ActionTypeID)) != null)
           sb.append("\n\taction type:\t").append(i);
       if ((i = getInteger(Tags.EventTypeID)) != null)
           sb.append("\n\tevent type:\t").append(i);
       return sb;
    }

    public String cmdFieldAsString() {
      switch (cmdField) {
         case C_STORE_RQ:
            return "C_STORE_RQ";
         case C_GET_RQ:
            return "C_GET_RQ";
         case C_FIND_RQ:
            return "C_FIND_RQ";
         case C_MOVE_RQ:
            return "C_MOVE_RQ";
         case C_ECHO_RQ:
            return "C_ECHO_RQ";
         case N_EVENT_REPORT_RQ:
            return "N_EVENT_REPORT_RQ";
         case N_GET_RQ:
            return "N_GET_RQ";
         case N_SET_RQ:
            return "N_SET_RQ";
         case N_ACTION_RQ:
            return "N_ACTION_RQ";
         case N_CREATE_RQ:
            return "N_CREATE_RQ";
         case N_DELETE_RQ:
            return "N_DELETE_RQ";
         case C_CANCEL_RQ:
            return "C_CANCEL_RQ";
         case C_STORE_RSP:
            return "C_STORE_RSP";
         case C_GET_RSP:
            return "C_GET_RSP";
         case C_FIND_RSP:
            return "C_FIND_RSP";
         case C_MOVE_RSP:
            return "C_MOVE_RSP";
         case C_ECHO_RSP:
            return "C_ECHO_RSP";
         case N_EVENT_REPORT_RSP:
            return "N_EVENT_REPORT_RSP";
         case N_GET_RSP:
            return "N_GET_RSP";
         case N_SET_RSP:
            return "N_SET_RSP";
         case N_ACTION_RSP:
            return "N_ACTION_RSP";
         case N_CREATE_RSP:
            return "N_CREATE_RSP";
         case N_DELETE_RSP:
            return "N_DELETE_RSP";
      }
      return "cmd:" + Integer.toHexString(cmdField);
    }
}

