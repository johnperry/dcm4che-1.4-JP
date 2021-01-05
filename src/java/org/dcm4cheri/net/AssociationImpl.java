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

package org.dcm4cheri.net;

import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AAssociateRJ;
import org.dcm4che.net.AAbort;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.ExtNegotiation;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;

import org.dcm4cheri.util.LF_ThreadPool;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;

/**
 * <description> 
 *
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 14395 $ $Date: 2010-11-25 13:54:12 +0100 (Do, 25 Nov 2010) $
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
 */
final class AssociationImpl implements Association {

    private final FsmImpl fsm;
    private final DimseReaderImpl reader;
    private final DimseWriterImpl writer;
    private int msgID = 0;
    private final byte[] b10 = new byte[10];
    private Hashtable properties = null;
    private int rqTimeout = 5000;
    private int acTimeout = 5000;
    private int dimseTimeout = 0;

    /** Creates a new instance of AssociationImpl */
    public AssociationImpl(Socket s, boolean requestor) throws IOException {
        this.fsm = new FsmImpl(this, s, requestor);
        this.reader = new DimseReaderImpl(fsm);
        this.writer = new DimseWriterImpl(fsm);
    }
    
    public final boolean isRequestor() {
        return fsm.isRequestor();
    }

    public Socket getSocket() {
        return fsm.socket();
    }

    public final String toString() {
        return "Assoc[sock="
            + fsm.socket()
            + ", state="
            + getStateAsString()
            + "]";
    }

    public void addAssociationListener(AssociationListener l) {
        fsm.addAssociationListener(l);
    }

    public void removeAssociationListener(AssociationListener l) {
        fsm.removeAssociationListener(l);
    }

    public final int getState() {
        return fsm.getState();
    }

    public final String getStateAsString() {
        return fsm.getStateAsString();
    }

    public synchronized final int nextMsgID() {
        return ++msgID;
    }

    public int getMaxOpsInvoked() {
        return fsm.getMaxOpsInvoked();
    }

    public int getMaxOpsPerformed() {
        return fsm.getMaxOpsPerformed();
    }

    public AAssociateRQ getAAssociateRQ() {
        return fsm.getAAssociateRQ();
    }

    public AAssociateAC getAAssociateAC() {
        return fsm.getAAssociateAC();
    }

    public AAssociateRJ getAAssociateRJ() {
        return fsm.getAAssociateRJ();
    }

    public AAbort getAAbort() {
        return fsm.getAAbort();
    }

    public String getCallingAET() {
        return fsm.getCallingAET();
    }

    public String getCalledAET() {
        return fsm.getCalledAET();
    }

    public void setThreadPool(LF_ThreadPool pool) {
        fsm.setThreadPool(pool);
        reader.setThreadPool(pool);
    }

    /** Setter for property soCloseDelay.
     * @param soCloseDelay New value of property soCloseDelay.
     */
    public final void setSoCloseDelay(int soCloseDelay) {
        fsm.setSoCloseDelay(soCloseDelay);
    }

    /** Getter for property soCloseDelay.
     * @return Value of property soCloseDelay.
     */
    public final int getSoCloseDelay() {
        return fsm.getSoCloseDelay();
    }

    /** Getter for property rqTimeout.
     * @return Value of property rqTimeout.
     */
    public int getRqTimeout() {
        return rqTimeout;
    }

    /** Setter for property rqTimeout.
     * @param rqTimeout New value of property rqTimeout.
     */
    public void setRqTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout: " + timeout);
        }
        this.rqTimeout = timeout;
    }

    /** Getter for property dimseTimeout.
     * @return Value of property dimseTimeout.
     */
    public int getDimseTimeout() {
        return dimseTimeout;
    }

    /** Setter for property dimseTimeout.
     * @param dimseTimeout New value of property dimseTimeout.
     */
    public void setDimseTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout: " + timeout);
        }
        this.dimseTimeout = timeout;
    }

    /** Getter for property acTimeout.
     * @return Value of property acTimeout.
     */
    public int getAcTimeout() {
        return acTimeout;
    }

    /** Setter for property acTimeout.
     * @param acTimeout New value of property acTimeout.
     */
    public void setAcTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout: " + timeout);
        }
        this.acTimeout = timeout;
    }

    public final PDU connect(AAssociateRQ rq) throws IOException {
		fsm.initMDC(rq);
        try {
            fsm.write(rq);
            return fsm.read(acTimeout, b10);
        } finally {
            clearMDC();
        }
    }

    public final PDU accept(AcceptorPolicy policy) throws IOException {
		initMDC();
        try {
            PDU rq = fsm.read(rqTimeout, b10);
            if (!(rq instanceof AAssociateRQ))
                return (AAbort) rq;

            PDU rp = policy.negotiate(this);
            if (rp instanceof AAssociateAC)
                fsm.write((AAssociateAC) rp);
            else
                fsm.write((AAssociateRJ) rp);
            return rp;
        } finally {
            clearMDC();
        }
    }

    final void initMDC() {
        fsm.initMDC();
    }

	final void clearMDC() {
		fsm.clearMDC();
	}

    public final Dimse read() throws IOException {
        initMDC();
        try {
            Dimse dimse = reader.read(dimseTimeout);
            if (dimse != null) {
                msgID = Math.max(dimse.getCommand().getMessageID(), msgID);
            }
            return dimse;
        } finally {
            clearMDC();
        }
    }

    public final void write(Dimse dimse) throws IOException {
        initMDC();
        try {
            msgID = Math.max(dimse.getCommand().getMessageID(), msgID);
            writer.write(dimse);
        } finally {
            clearMDC();
        }
    }

    public final PDU release(int timeout) throws IOException {
        initMDC();
        try {
            fsm.write(AReleaseRQImpl.getInstance());
            return fsm.read(timeout, b10);
        } finally {
            clearMDC();
        }
    }

    final void writeReleaseRQ() throws IOException {
        initMDC();
        try {
            fsm.write(AReleaseRQImpl.getInstance());
        } finally {
            clearMDC();
        }
    }

    public final void abort(AAbort aa) throws IOException {
        initMDC();
        try {
            fsm.write(aa);
        } finally {
            clearMDC();
        }
    }

    public final String getAcceptedTransferSyntaxUID(int pcid) {
        return fsm.getAcceptedTransferSyntaxUID(pcid);
    }

    public final PresContext getProposedPresContext(int pcid) {
        return fsm.getProposedPresContext(pcid);
    }

    public final PresContext getAcceptedPresContext(
        String asuid,
        String tsuid) {
        return fsm.getAcceptedPresContext(asuid, tsuid);
    }

    public final List listAcceptedPresContext(String asuid) {
        return fsm.listAcceptedPresContext(asuid);
    }

    public final int countAcceptedPresContext() {
        return fsm.countAcceptedPresContext();
    }

    public ExtNegotiation getRequestedExtNegotiation(String cuid) {
        return fsm.getRequestedExtNegotiation(cuid);
    }

    public ExtNegotiation getAcceptedExtNegotiation(String cuid) {
        return fsm.getAcceptedExtNegotiation(cuid);
    }

    public Object getProperty(Object key) {
        return properties != null ? properties.get(key) : null;
    }

    public void putProperty(Object key, Object value) {
        if (properties == null) {
            properties = new Hashtable(2);
        }
        if (value != null) {
            properties.put(key, value);
        } else {
            properties.remove(key);
        }
    }

    /** Getter for property packPDVs.
     * @return Value of property packPDVs.
     */
    public boolean isPackPDVs() {
        return writer.isPackPDVs();
    }

    /** Setter for property packPDVs.
     * @param packPDVs New value of property packPDVs.
     */
    public void setPackPDVs(boolean packPDVs) {
        writer.setPackPDVs(packPDVs);
    }

}
