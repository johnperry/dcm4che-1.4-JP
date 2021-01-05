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

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 14395 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020728 gunter:</b>
 * <ul>
 * <li> add {@link #listAcceptedPresContext(String)}
 * <li> add {@link #countAcceptedPresContext()}
 * </ul>
 * <p><b>20020802 gunter:</b>
 * <ul>
 * <li> add {@link #getProperty}
 * <li> add {@link #putProperty}
 * </ul>
 * <p><b>20020810 gunter:</b>
 * <ul>
 * <li> add properties rqTimeout, acTimeout, dimseTimeout
 * <li> rename property TCPCloseTimeout to soCloseDelay
 * <li> remove timeout param from connect(), accept(), read()
 * </ul>
 */
public interface Association {
    
    public static int IDLE = 1;
    
    public static int AWAITING_READ_ASS_RQ = 2;
    public static int AWAITING_WRITE_ASS_RP = 3;
    public static int AWAITING_WRITE_ASS_RQ = 4;
    public static int AWAITING_READ_ASS_RP = 5;
    
    public static int ASSOCIATION_ESTABLISHED = 6;
    
    public static int AWAITING_READ_REL_RP = 7;
    public static int AWAITING_WRITE_REL_RP = 8;
    public static int RCRS_AWAITING_WRITE_REL_RP = 9;
    public static int RCAS_AWAITING_READ_REL_RP = 10;
    public static int RCRS_AWAITING_READ_REL_RP = 11;
    public static int RCAS_AWAITING_WRITE_REL_RP = 12;
    
    public static int ASSOCIATION_TERMINATING = 13;
    
    boolean isRequestor();
    
    int getState();
    
    String getStateAsString();
    
    void addAssociationListener(AssociationListener l);
    
    void removeAssociationListener(AssociationListener l);
    
    int nextMsgID();
    
    PDU connect(AAssociateRQ rq) throws IOException;
    
    PDU accept(AcceptorPolicy policy) throws IOException;
    
    Dimse read() throws IOException;
    
    void write(Dimse dimse) throws IOException;
    
    PDU release(int timeout) throws IOException;
    
    void abort(AAbort aa) throws IOException;
    
    int getMaxOpsInvoked();
    
    int getMaxOpsPerformed();
    
    String getAcceptedTransferSyntaxUID(int pcid);
    
    PresContext getAcceptedPresContext(String asuid, String tsuid);
    
    PresContext getProposedPresContext(int pcid);
    
    List listAcceptedPresContext(String asuid);
    
    ExtNegotiation getRequestedExtNegotiation(String cuid);
    
    ExtNegotiation getAcceptedExtNegotiation(String cuid);
    
    int countAcceptedPresContext();
    
    AAssociateRQ getAAssociateRQ();
    
    AAssociateAC getAAssociateAC();
    
    AAssociateRJ getAAssociateRJ();
    
    AAbort getAAbort();
    
    Socket getSocket();
    
    String getCallingAET();

    String getCalledAET();
    
    Object getProperty(Object key);
    
    void putProperty(Object key, Object value);
    
    /** Getter for property rqTimeout.
     * @return Value of property rqTimeout.
     */
    int getRqTimeout();
    
    /** Setter for property rqTimeout.
     * @param rqTimeout New value of property rqTimeout.
     */
    void setRqTimeout(int rqTimeout);
    
    /** Getter for property dimseTimeout.
     * @return Value of property dimseTimeout.
     */
    int getDimseTimeout();
    
    /** Setter for property dimseTimeout.
     * @param dimseTimeout New value of property dimseTimeout.
     */
    void setDimseTimeout(int dimseTimeout);
    
    /** Getter for property soCloseDelay.
     * @return Value of property soCloseDelay.
     */
    int getSoCloseDelay();
    
    /** Setter for property soCloseDelay.
     * @param soCloseDelay New value of property soCloseDelay.
     */
    void setSoCloseDelay(int soCloseDelay);
    
    /** Getter for property acTimeout.
     * @return Value of property acTimeout.
     */
    int getAcTimeout();
    
    /** Setter for property acTimeout.
     * @param acTimeout New value of property acTimeout.
     */
    void setAcTimeout(int acTimeout);
    
    /** Getter for property packPDVs.
     * @return Value of property packPDVs.
     */
    boolean isPackPDVs();
    
    /** Setter for property packPDVs.
     * @param packPDVs New value of property packPDVs.
     */
    void setPackPDVs(boolean packPDVs);
    
}