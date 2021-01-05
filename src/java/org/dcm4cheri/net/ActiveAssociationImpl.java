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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4cheri.util.IntHashtable2;
import org.dcm4cheri.util.LF_ThreadPool;

/**
 * <description>
 * 
 * @see <related>
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author <a href="mailto:umberto.cappellini@chello.at">Umberto Cappellini</a>
 * @version $Revision: 16965 $ $Date: 2008-01-14 13:04:17 -0600 (Mon, 14 Jan
 *          2008) $
 * 
 */
final class ActiveAssociationImpl implements ActiveAssociation,
        LF_ThreadPool.Handler, AssociationListener {
    private static Logger log = Logger.getLogger(ActiveAssociationImpl.class);

    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private static int instCount = 0;

    private final String name = "ActiveAssoc-" + ++instCount;

    // Static --------------------------------------------------------
    private final AssociationImpl assoc;

    private final DcmServiceRegistry services;

    private final IntHashtable2 rspDispatcher = new IntHashtable2();

    private final IntHashtable2 cancelDispatcher = new IntHashtable2();

    private final LF_ThreadPool threadPool = new LF_ThreadPool(this, name);

    private boolean running = false;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    ActiveAssociationImpl(Association assoc, DcmServiceRegistry services) {
        if (assoc.getState() != Association.ASSOCIATION_ESTABLISHED)
            throw new IllegalStateException("Association not established - "
                    + assoc.getState());

        this.assoc = (AssociationImpl) assoc;
        this.services = services;
        ((AssociationImpl) assoc).setThreadPool(threadPool);
        assoc.addAssociationListener(this);
    }

    // Public --------------------------------------------------------
    public final void addCancelListener(int msgID, DimseListener l) {
        synchronized (cancelDispatcher) {
            cancelDispatcher.put(msgID, l);
        }
    }
    public final void removeCancelListener(int msgID) {
        synchronized (cancelDispatcher) {
            cancelDispatcher.remove(msgID);
        }
    }

    public String toString() {
        return "Active-" + assoc;
    }

    public void run() {
        if (running)
            throw new IllegalStateException("Already running: " + threadPool);

        this.running = true;
        threadPool.join();
    }

    public void start() {
        if (running)
            throw new IllegalStateException("Already running: " + threadPool);

        new Thread(this, name).start();
    }

    public Association getAssociation() {
        return assoc;
    }

    public void invoke(Dimse rq, DimseListener l) throws InterruptedException,
            IOException {
        // checkRunning();
        int msgID = rq.getCommand().getMessageID();
        int maxOps = assoc.getMaxOpsInvoked();
        synchronized (rspDispatcher) {
            if (maxOps > 0)
                while (rspDispatcher.size() >= maxOps)
                    rspDispatcher.wait();
            rspDispatcher.put(msgID, l);
        }
        assoc.write(rq);
    }

    public FutureRSP invoke(Dimse rq) throws InterruptedException, IOException {
        FutureRSPImpl retval = new FutureRSPImpl(assoc);
        invoke(rq, retval);
        return retval;
    }

    public void release(boolean waitOnRSP) throws InterruptedException,
            IOException {
        // checkRunning();
        if (waitOnRSP) {
            waitForPendingRSP();
        }
        ((AssociationImpl) assoc).writeReleaseRQ();
    }

    public void waitForPendingRSP() throws InterruptedException {
        synchronized (rspDispatcher) {
            while (!rspDispatcher.isEmpty()) {
                rspDispatcher.wait();
            }
        }
    }

    // LF_ThreadPool.Handler implementation --------------------------
    public void run(LF_ThreadPool pool) {
        Dimse dimse = null;
        try {
            dimse = assoc.read();

            // if Association was released
            if (dimse == null) {
                pool.shutdown();
                return;
            }
            assoc.initMDC();
            Command cmd = dimse.getCommand();
            switch (cmd.getCommandField()) {
            case Command.C_STORE_RQ:
                services.lookup(cmd.getAffectedSOPClassUID()).c_store(this,
                        dimse);
                break;
            case Command.C_GET_RQ:
                services.lookup(cmd.getAffectedSOPClassUID())
                        .c_get(this, dimse);
                break;
            case Command.C_FIND_RQ:
                services.lookup(cmd.getAffectedSOPClassUID()).c_find(this,
                        dimse);
                break;
            case Command.C_MOVE_RQ:
                services.lookup(cmd.getAffectedSOPClassUID()).c_move(this,
                        dimse);
                break;
            case Command.C_ECHO_RQ:
                services.lookup(cmd.getAffectedSOPClassUID()).c_echo(this,
                        dimse);
                break;
            case Command.N_EVENT_REPORT_RQ:
                services.lookup(cmd.getAffectedSOPClassUID()).n_event_report(
                        this, dimse);
                break;
            case Command.N_GET_RQ:
                services.lookup(cmd.getRequestedSOPClassUID()).n_get(this,
                        dimse);
                break;
            case Command.N_SET_RQ:
                services.lookup(cmd.getRequestedSOPClassUID()).n_set(this,
                        dimse);
                break;
            case Command.N_ACTION_RQ:
                services.lookup(cmd.getRequestedSOPClassUID()).n_action(this,
                        dimse);
                break;
            case Command.N_CREATE_RQ:
                services.lookup(cmd.getAffectedSOPClassUID()).n_create(this,
                        dimse);
                break;
            case Command.N_DELETE_RQ:
                services.lookup(cmd.getRequestedSOPClassUID()).n_delete(this,
                        dimse);
                break;
            case Command.C_STORE_RSP:
            case Command.C_GET_RSP:
            case Command.C_FIND_RSP:
            case Command.C_MOVE_RSP:
            case Command.C_ECHO_RSP:
            case Command.N_EVENT_REPORT_RSP:
            case Command.N_GET_RSP:
            case Command.N_SET_RSP:
            case Command.N_ACTION_RSP:
            case Command.N_CREATE_RSP:
            case Command.N_DELETE_RSP:
                handleResponse(dimse);
                break;
            case Command.C_CANCEL_RQ:
                handleCancel(dimse);
                break;
            default:
                throw new RuntimeException("Illegal Command: " + cmd);
            }
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
            pool.shutdown();
        } finally {
            // ensure readout of data PDVs
            if (dimse != null)
                dimse.closeDataStream();
            assoc.clearMDC();
        }
    }

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    private void handleResponse(Dimse dimse) throws IOException {
        DimseListener l = null;
        synchronized (rspDispatcher) {
            Command cmd = dimse.getCommand();
            Dataset ds = dimse.getDataset(); // read out dataset, if any
            int msgID = cmd.getMessageIDToBeingRespondedTo();
            if (cmd.isPending()) {
                l = (DimseListener) rspDispatcher.get(msgID);
            } else {
                l = (DimseListener) rspDispatcher.remove(msgID);
                rspDispatcher.notify();
            }
            if (l != null)
                l.dimseReceived(assoc, dimse);
        }
    }

    private void handleCancel(Dimse dimse) {
        Command cmd = dimse.getCommand();
        int msgID = cmd.getMessageIDToBeingRespondedTo();
        DimseListener l = (DimseListener) cancelDispatcher.remove(msgID);

        if (l != null)
            l.dimseReceived(assoc, dimse);
    }

    private void checkRunning() {
        if (!running)
            throw new IllegalStateException("Not running: " + threadPool);
    }

    // AssociationListener implementation ----------------------------
    public void write(Association src, PDU pdu) {
    }

    public void received(Association src, Dimse dimse) {
    }

    public void error(Association src, IOException ioe) {
    }

    public void closing(Association src) {
    }

    public void closed(Association src) {
        synchronized (rspDispatcher) {
            rspDispatcher.clear();
            rspDispatcher.notifyAll();
        }
        assoc.removeAssociationListener(this);
    }

    public void write(Association src, Dimse dimse) {
    }

    public void received(Association src, PDU pdu) {
    }

}
