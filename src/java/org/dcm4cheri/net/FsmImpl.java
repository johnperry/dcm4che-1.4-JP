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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.dcm4che.net.AAbort;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRJ;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AReleaseRP;
import org.dcm4che.net.AReleaseRQ;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.AsyncOpsWindow;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.ExtNegotiation;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PDUException;
import org.dcm4che.net.PDataTF;
import org.dcm4che.net.PresContext;
import org.dcm4cheri.util.LF_ThreadPool;

/**
 *  <description>
 *
 *@author     <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *@since      March, 2002
 *@version    $Revision: 15464 $ <p>
 *
 */
final class FsmImpl
{
    final static Logger log = Logger.getLogger(FsmImpl.class);
    private final AssociationImpl assoc;
    private final boolean requestor;
    private final Socket s;
    private final InputStream in;
    private final OutputStream out;
    private int soCloseDelay = 500;
    private AAssociateRQ rq = null;
    private AAssociateAC ac = null;
    private AAssociateRJ rj = null;
    private AAbort aa = null;
    private AssociationListener assocListener = null;
    private LF_ThreadPool pool = null;


    /**
     *  Creates a new instance of DcmULServiceImpl
     *
     *@param  assoc            Description of the Parameter
     *@param  s                Description of the Parameter
     *@param  requestor        Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public FsmImpl(AssociationImpl assoc, Socket s, boolean requestor)
        throws IOException
    {
        this.assoc = assoc;
        this.requestor = requestor;
        this.s = s;
        initMDC();
        try {
            this.in = s.getInputStream();
            this.out = s.getOutputStream();
            log.info(s.toString());
            changeState(requestor ? STA4 : STA2);
        } finally {
            clearMDC();
        }
    }


    /**
     *  Adds a feature to the AssociationListener attribute of the FsmImpl
     *  object
     *
     *@param  l  The feature to be added to the AssociationListener attribute
     */
    public synchronized void addAssociationListener(AssociationListener l)
    {
        assocListener = Multicaster.add(assocListener, l);
    }


    /**
     *  Description of the Method
     *
     *@param  l  Description of the Parameter
     */
    public synchronized void removeAssociationListener(AssociationListener l)
    {
        assocListener = Multicaster.remove(assocListener, l);
    }


    /**
     *  Sets the threadPool attribute of the FsmImpl object
     *
     *@param  pool  The new threadPool value
     */
    public void setThreadPool(LF_ThreadPool pool)
    {
        this.pool = pool;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    final Socket socket()
    {
        return s;
    }


    /**
     *  Gets the requestor attribute of the FsmImpl object
     *
     *@return    The requestor value
     */
    final boolean isRequestor()
    {
        return requestor;
    }


    /**
     *  Sets the soCloseDelay attribute of the FsmImpl object
     *
     *@param  soCloseDelay  The new soCloseDelay value
     */
    final void setSoCloseDelay(int soCloseDelay)
    {
        if (soCloseDelay < 0) {
            throw new IllegalArgumentException(
                    "soCloseDelay:" + soCloseDelay);
        }
        this.soCloseDelay = soCloseDelay;
    }


    /**
     *  Gets the soCloseDelay attribute of the FsmImpl object
     *
     *@return    The soCloseDelay value
     */
    final int getSoCloseDelay()
    {
        return soCloseDelay;
    }


    /**
     *  Gets the state attribute of the FsmImpl object
     *
     *@return    The state value
     */
    public int getState()
    {
        return state.getType();
    }


    /**
     *  Gets the stateAsString attribute of the FsmImpl object
     *
     *@return    The stateAsString value
     */
    public String getStateAsString()
    {
        return state.toString();
    }


    /**
     *  Gets the aAssociateRQ attribute of the FsmImpl object
     *
     *@return    The aAssociateRQ value
     */
    final AAssociateRQ getAAssociateRQ()
    {
        return rq;
    }


    /**
     *  Gets the callingAET attribute of the FsmImpl object
     *
     *@return    The callingAET value
     */
    final String getCallingAET()
    {
        if (rq == null) {
            throw new IllegalStateException(state.toString());
        }
        return rq.getCallingAET();
    }


    /**
     *  Gets the calledAET attribute of the FsmImpl object
     *
     *@return    The calledAET value
     */
    final String getCalledAET()
    {
        if (rq == null) {
            throw new IllegalStateException(state.toString());
        }
        return rq.getCalledAET();
    }


    /**
     *  Gets the aAssociateAC attribute of the FsmImpl object
     *
     *@return    The aAssociateAC value
     */
    final AAssociateAC getAAssociateAC()
    {
        return ac;
    }


    /**
     *  Gets the aAssociateRJ attribute of the FsmImpl object
     *
     *@return    The aAssociateRJ value
     */
    final AAssociateRJ getAAssociateRJ()
    {
        return rj;
    }


    /**
     *  Gets the aAbort attribute of the FsmImpl object
     *
     *@return    The aAbort value
     */
    final AAbort getAAbort()
    {
        return aa;
    }


    /**
     *  Gets the writeMaxLength attribute of the FsmImpl object
     *
     *@return    The writeMaxLength value
     */
    final int getWriteMaxLength()
    {
        if (ac == null || rq == null) {
            throw new IllegalStateException(state.toString());
        }
        int maxLen = requestor ? ac.getMaxPDULength() : rq.getMaxPDULength();
        return maxLen == 0 || maxLen > UnparsedPDUImpl.MAX_LENGTH 
                ? UnparsedPDUImpl.MAX_LENGTH : maxLen;
    }


    /**
     *  Gets the readMaxLength attribute of the FsmImpl object
     *
     *@return    The readMaxLength value
     */
    final int getReadMaxLength()
    {
        if (ac == null || rq == null) {
            throw new IllegalStateException(state.toString());
        }
        return requestor ? rq.getMaxPDULength() : ac.getMaxPDULength();
    }


    /**
     *  Gets the acceptedTransferSyntaxUID attribute of the FsmImpl object
     *
     *@param  pcid  Description of the Parameter
     *@return       The acceptedTransferSyntaxUID value
     */
    final String getAcceptedTransferSyntaxUID(int pcid)
    {
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        PresContext pc = ac.getPresContext(pcid);
        if (pc == null || pc.result() != PresContext.ACCEPTANCE) {
            return null;
        }
        return pc.getTransferSyntaxUID();
    }

    final PresContext getProposedPresContext(int pcid)
    {
        if (rq == null) {
            throw new IllegalStateException(state.toString());
        }
        return rq.getPresContext(pcid);
    }


    /**
     *  Gets the acceptedPresContext attribute of the FsmImpl object
     *
     *@param  asuid  Description of the Parameter
     *@param  tsuid  Description of the Parameter
     *@return        The acceptedPresContext value
     */
    final PresContext getAcceptedPresContext(String asuid, String tsuid)
    {
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        for (Iterator it = rq.listPresContext().iterator(); it.hasNext(); ) {
            PresContext rqpc = (PresContext) it.next();
            if (asuid.equals(rqpc.getAbstractSyntaxUID())) {
                PresContext acpc = ac.getPresContext(rqpc.pcid());
                if (acpc != null && acpc.result() == PresContext.ACCEPTANCE
                         && tsuid.equals(acpc.getTransferSyntaxUID())) {
                    return acpc;
                }
            }
        }
        return null;
    }


    /**
     *  Description of the Method
     *
     *@param  asuid  Description of the Parameter
     *@return        Description of the Return Value
     */
    public final List listAcceptedPresContext(String asuid)
    {
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        LinkedList list = new LinkedList();
        for (Iterator it = rq.listPresContext().iterator(); it.hasNext(); ) {
            PresContext rqpc = (PresContext) it.next();
            if (asuid.equals(rqpc.getAbstractSyntaxUID())) {
                PresContext acpc = ac.getPresContext(rqpc.pcid());
                if (acpc != null && acpc.result() == PresContext.ACCEPTANCE) {
                    list.add(acpc);
                }
            }
        }
        return Collections.unmodifiableList(list);
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public final int countAcceptedPresContext()
    {
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        return ac.countAcceptedPresContext();
    }


    public ExtNegotiation getRequestedExtNegotiation(String sopClass) {
        if (rq == null) {
            throw new IllegalStateException(state.toString());
        }
        return rq.getExtNegotiation(sopClass);
    }

    public ExtNegotiation getAcceptedExtNegotiation(String sopClass) {
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        return ac.getExtNegotiation(sopClass);
    }

    /**
     *  Gets the maxOpsInvoked attribute of the FsmImpl object
     *
     *@return    The maxOpsInvoked value
     */
    int getMaxOpsInvoked()
    {
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        AsyncOpsWindow aow = ac.getAsyncOpsWindow();
        if (aow == null) {
            return 1;
        }
        return requestor ? aow.getMaxOpsInvoked() : aow.getMaxOpsPerformed();
    }


    /**
     *  Gets the maxOpsPerformed attribute of the FsmImpl object
     *
     *@return    The maxOpsPerformed value
     */
    int getMaxOpsPerformed()
    {
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        AsyncOpsWindow aow = ac.getAsyncOpsWindow();
        if (aow == null) {
            return 1;
        }
        return requestor ? aow.getMaxOpsPerformed() : aow.getMaxOpsInvoked();
    }


    /**
     *  Description of the Method
     *
     *@param  state  Description of the Parameter
     */
    private void changeState(State state)
    {
        if (this.state != state) {
            State prev = this.state;
            this.state = state;
            if (log.isDebugEnabled()) {
                log.debug(state.toString());
            }
            state.entry();
        }
    }


    /**
     *  Description of the Method
     *
     *@param  timeout          Description of the Parameter
     *@param  buf              Description of the Parameter
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public PDU read(int timeout, byte[] buf)
        throws IOException
    {
        try {
            UnparsedPDUImpl raw = null;
            synchronized (in) {
                s.setSoTimeout(timeout);
                try {
                    raw = new UnparsedPDUImpl(in, buf);
                } catch (IOException e) {
                    changeState(STA1);
                    throw e;
                }
            }
            return state.parse(raw);
        } catch (IOException ioe) {
            if (assocListener != null) {
                assocListener.error(assoc, ioe);
            }
            throw ioe;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  rq               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void write(AAssociateRQ rq)
        throws IOException
    {
        fireWrite(rq);
        try {
            synchronized (out) {
                state.write(rq);
            }
        } catch (IOException ioe) {
            if (assocListener != null) {
                assocListener.error(assoc, ioe);
            }
            throw ioe;
        }
        this.rq = rq;
    }


    /**
     *  Description of the Method
     *
     *@param  ac               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void write(AAssociateAC ac)
        throws IOException
    {
        fireWrite(ac);
        try {
            synchronized (out) {
                state.write(ac);
            }
        } catch (IOException ioe) {
            if (assocListener != null) {
                assocListener.error(assoc, ioe);
            }
            throw ioe;
        }
        this.ac = ac;
    }


    /**
     *  Description of the Method
     *
     *@param  rj               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void write(AAssociateRJ rj)
        throws IOException
    {
        fireWrite(rj);
        try {
            synchronized (out) {
                state.write(rj);
            }
        } catch (IOException ioe) {
            if (assocListener != null) {
                assocListener.error(assoc, ioe);
            }
            throw ioe;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  data             Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void write(PDataTF data)
        throws IOException
    {
        fireWrite(data);
        try {
            synchronized (out) {
                state.write(data);
            }
        } catch (IOException ioe) {
            if (assocListener != null) {
                assocListener.error(assoc, ioe);
            }
            throw ioe;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  rq               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void write(AReleaseRQ rq)
        throws IOException
    {
        fireWrite(rq);
        try {
            synchronized (out) {
                state.write(rq);
            }
        } catch (IOException ioe) {
            if (assocListener != null) {
                assocListener.error(assoc, ioe);
            }
            throw ioe;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  rp               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void write(AReleaseRP rp)
        throws IOException
    {
        fireWrite(rp);
        try {
            synchronized (out) {
                state.write(rp);
            }
        } catch (IOException ioe) {
            if (assocListener != null) {
                assocListener.error(assoc, ioe);
            }
            throw ioe;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  abort            Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void write(AAbort abort)
        throws IOException
    {
        fireWrite(abort);
        try {
            synchronized (out) {
                state.write(abort);
            }
        } catch (IOException ioe) {
            if (assocListener != null) {
                assocListener.error(assoc, ioe);
            }
            throw ioe;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  dimse  Description of the Parameter
     */
    void fireReceived(Dimse dimse)
    {
        if (log.isInfoEnabled()) {
            log.info("received " + dimse);
        }
        if (assocListener != null) {
            assocListener.received(assoc, dimse);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  dimse  Description of the Parameter
     */
    void fireWrite(Dimse dimse)
    {
        if (log.isInfoEnabled()) {
            log.info("sending " + dimse);
        }
        if (assocListener != null) {
            assocListener.write(assoc, dimse);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  pdu  Description of the Parameter
     */
    private void fireWrite(PDU pdu)
    {
        if (pdu instanceof PDataTF) {
            if (log.isDebugEnabled()) {
                log.debug("sending " + pdu);
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("sending " + pdu);
            }
        }
        if (assocListener != null) {
            assocListener.write(assoc, pdu);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  pdu  Description of the Parameter
     *@return      Description of the Return Value
     */
    private PDU fireReceived(PDU pdu)
    {
        if (pdu instanceof PDataTF) {
            if (log.isDebugEnabled()) {
                log.debug("received " + pdu);
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("received " + pdu);
            }
        }
        if (assocListener != null) {
            assocListener.received(assoc, pdu);
        }
        return pdu;
    }


    /**
     *  Description of the Class
     *
     *@author     gunter
     *@created    March 22, 2003
     */
    private abstract class State
    {

        private final int type;


        /**
         *  Constructor for the State object
         *
         *@param  type  Description of the Parameter
         */
        State(int type)
        {
            this.type = type;
        }


        /**
         *  Gets the type attribute of the State object
         *
         *@return    The type value
         */
        public final int getType()
        {
            return type;
        }


        /**
         *  Gets the open attribute of the State object
         *
         *@return    The open value
         */
        public boolean isOpen()
        {
            return false;
        }


        /**
         *  Description of the Method
         *
         *@return    Description of the Return Value
         */
        public boolean canWritePDataTF()
        {
            return false;
        }


        /**
         *  Description of the Method
         *
         *@return    Description of the Return Value
         */
        public boolean canReadPDataTF()
        {
            return false;
        }


        /**  Description of the Method */
        void entry()
        {
        }


        /**
         *  Description of the Method
         *
         *@param  raw               Description of the Parameter
         *@return                   Description of the Return Value
         *@exception  PDUException  Description of the Exception
         */
        PDU parse(UnparsedPDUImpl raw)
            throws PDUException
        {
            try {
                switch (raw.type()) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        throw new PDUException("Unexpected " + raw,
                                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                AAbort.UNEXPECTED_PDU));
                    case 7:
                        fireReceived(aa = AAbortImpl.parse(raw));
                        changeState(STA1);
                        return aa;
                    default:
                        throw new PDUException("Unrecognized " + raw,
                                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                AAbort.UNRECOGNIZED_PDU));
                }
            } catch (PDUException ule) {
                try {
                    FsmImpl.this.write(ule.getAAbort());
                } catch (Exception ignore) {}
                ;
                throw ule;
            }
        }


        /**
         *  Description of the Method
         *
         *@param  rq               Description of the Parameter
         *@exception  IOException  Description of the Exception
         */
        void write(AAssociateRQ rq)
            throws IOException
        {
            throw new IllegalStateException("Error writing. Current state is: " + state.toString());
        }


        /**
         *  Description of the Method
         *
         *@param  ac               Description of the Parameter
         *@exception  IOException  Description of the Exception
         */
        void write(AAssociateAC ac)
            throws IOException
        {
            throw new IllegalStateException("Error writing. Current state is: " + state.toString());
        }


        /**
         *  Description of the Method
         *
         *@param  rj               Description of the Parameter
         *@exception  IOException  Description of the Exception
         */
        void write(AAssociateRJ rj)
            throws IOException
        {
            throw new IllegalStateException("Error writing. Current state is: " + state.toString());
        }


        /**
         *  Description of the Method
         *
         *@param  data             Description of the Parameter
         *@exception  IOException  Description of the Exception
         */
        void write(PDataTF data)
            throws IOException
        {
        	
            throw new IllegalStateException("Error writing. Current state is: " + state.toString());
        }


        /**
         *  Description of the Method
         *
         *@param  rq               Description of the Parameter
         *@exception  IOException  Description of the Exception
         */
        void write(AReleaseRQ rq)
            throws IOException
        {
            throw new IllegalStateException("Error writing. Current state is: " + state.toString());
        }


        /**
         *  Description of the Method
         *
         *@param  rp               Description of the Parameter
         *@exception  IOException  Description of the Exception
         */
        void write(AReleaseRP rp)
            throws IOException
        {
            throw new IllegalStateException("Error writing. Current state is: " + state.toString());
        }


        /**
         *  Description of the Method
         *
         *@param  abort            Description of the Parameter
         *@exception  IOException  Description of the Exception
         */
        void write(AAbort abort)
            throws IOException
        {
            try {
                abort.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
            changeState(STA13);
        }

    }


    private final State STA1 =
        new State(Association.IDLE)
        {
            public String toString()
            {
                return "Sta 1 - Idle";
            }


            void entry()
            {
                if (pool != null) {
                    pool.shutdown();
                }// stop reading
                if (assocListener != null) {
                    assocListener.closing(assoc);
                }
                if (log.isInfoEnabled()) {
                    log.info("closing connection - " + s);
                }
                try {
                    in.close();
                } catch (IOException ignore) {}
                try {
                    out.close();
                } catch (IOException ignore) {}
                try {
                    s.close();
                } catch (IOException ignore) {}
                if (assocListener != null) {
                    assocListener.closed(assoc);
                }
            }


            void write(AAbort abort)
                throws IOException
            {
            }
        };
    private State state = STA1;

    private final State STA2 =
        new State(Association.AWAITING_READ_ASS_RQ)
        {
            public String toString()
            {
                return "Sta 2 - Transport connection open (Awaiting A-ASSOCIATE-RQ PDU)";
            }


            PDU parse(UnparsedPDUImpl raw)
                throws PDUException
            {
                try {
                    switch (raw.type()) {
                        case 1:
                            rq = AAssociateRQImpl.parse(raw);
                            initMDC();
                            fireReceived(rq);
                            changeState(STA3);
                            return rq;
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            throw new PDUException("Unexpected " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNEXPECTED_PDU));
                        case 7:
                            fireReceived(aa = AAbortImpl.parse(raw));
                            changeState(STA1);
                            return aa;
                        default:
                            throw new PDUException("Unrecognized " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNRECOGNIZED_PDU));
                    }
                } catch (PDUException ule) {
                    try {
                        FsmImpl.this.write(ule.getAAbort());
                    } catch (Exception ignore) {}
                    ;
                    throw ule;
                }
            }
        };

    private final State STA3 =
        new State(Association.AWAITING_WRITE_ASS_RP)
        {
            public String toString()
            {
                return "Sta 3 - Awaiting local A-ASSOCIATE response primitive";
            }


            void write(AAssociateAC ac)
                throws IOException
            {
                try {
                    ac.writeTo(out);
                } catch (IOException e) {
                    changeState(STA1);
                    throw e;
                }
                changeState(STA6);
            }


            void write(AAssociateRJ rj)
                throws IOException
            {
                try {
                    rj.writeTo(out);
                } catch (IOException e) {
                    changeState(STA1);
                    throw e;
                }
                changeState(STA13);
            }
        };

    private final State STA4 =
        new State(Association.AWAITING_WRITE_ASS_RQ)
        {
            public String toString()
            {
                return "Sta 4 - Awaiting transport connection opening to complete";
            }


            void write(AAssociateRQ rq)
                throws IOException
            {
                try {
                    rq.writeTo(out);
                } catch (IOException e) {
                    changeState(STA1);
                    throw e;
                }
                changeState(STA5);
            }


            void write(AAbort abort)
                throws IOException
            {
                changeState(STA1);
            }
        };

    private final State STA5 =
        new State(Association.AWAITING_READ_ASS_RP)
        {
            public String toString()
            {
                return "Sta 5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU";
            }


            PDU parse(UnparsedPDUImpl raw)
                throws PDUException
            {
                try {
                    switch (raw.type()) {
                        case 1:
                            throw new PDUException("Unexpected " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNEXPECTED_PDU));
                        case 2:
                            fireReceived(ac = AAssociateACImpl.parse(raw));
                            changeState(STA6);
                            return ac;
                        case 3:
                            fireReceived(rj = AAssociateRJImpl.parse(raw));
                            changeState(STA13);
                            return rj;
                        case 4:
                        case 5:
                        case 6:
                            throw new PDUException("Unexpected " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNEXPECTED_PDU));
                        case 7:
                            fireReceived(aa = AAbortImpl.parse(raw));
                            changeState(STA1);
                            return aa;
                        default:
                            throw new PDUException("Unrecognized " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNRECOGNIZED_PDU));
                    }
                } catch (PDUException ule) {
                    try {
                        FsmImpl.this.write(ule.getAAbort());
                    } catch (Exception ignore) {}
                    ;
                    throw ule;
                }
            }
        };

    private final State STA6 =
        new State(Association.ASSOCIATION_ESTABLISHED)
        {
            public String toString()
            {
                return "Sta 6 - Association established and ready for data transfer";
            }


            public boolean isOpen()
            {
                return true;
            }


            public boolean canWritePDataTF()
            {
                return true;
            }


            public boolean canReadPDataTF()
            {
                return true;
            }


            PDU parse(UnparsedPDUImpl raw)
                throws PDUException
            {
                try {
                    switch (raw.type()) {
                        case 1:
                        case 2:
                        case 3:
                            throw new PDUException("Unexpected " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNEXPECTED_PDU));
                        case 4:
                            return fireReceived(PDataTFImpl.parse(raw));
                        case 5:
                            PDU pdu = fireReceived(AReleaseRQImpl.parse(raw));
                            changeState(STA8);
                            return pdu;
                        case 6:
                            throw new PDUException("Unexpected " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNEXPECTED_PDU));
                        case 7:
                            fireReceived(aa = AAbortImpl.parse(raw));
                            changeState(STA1);
                            return aa;
                        default:
                            throw new PDUException("Unrecognized " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNRECOGNIZED_PDU));
                    }
                } catch (PDUException ule) {
                    try {
                        FsmImpl.this.write(ule.getAAbort());
                    } catch (Exception ignore) {}
                    ;
                    throw ule;
                }
            }


            void write(PDataTF tf)
                throws IOException
            {
                try {
                    tf.writeTo(out);
                } catch (IOException e) {
                    changeState(STA1);
                    throw e;
                }
            }


            void write(AReleaseRQ rq)
                throws IOException
            {
                try {
                    changeState(STA7);
                    rq.writeTo(out);
                } catch (IOException e) {
                    changeState(STA1);
                    throw e;
                }
            }
        };

    private final State STA7 =
        new State(Association.AWAITING_READ_REL_RP)
        {
            public String toString()
            {
                return "Sta 7 - Awaiting A-RELEASE-RP PDU";
            }


            public boolean canReadPDataTF()
            {
                return true;
            }


            PDU parse(UnparsedPDUImpl raw)
                throws PDUException
            {
                try {
                    switch (raw.type()) {
                        case 1:
                        case 2:
                        case 3:
                            throw new PDUException("Unexpected " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNEXPECTED_PDU));
                        case 4:
                            return fireReceived(PDataTFImpl.parse(raw));
                        case 5:
                            PDU pdu = fireReceived(AReleaseRQImpl.parse(raw));
                            changeState(requestor ? STA9 : STA10);
                            return pdu;
                        case 6:
                            fireReceived(pdu = AReleaseRPImpl.parse(raw));
                            changeState(STA1);
                            return pdu;
                        case 7:
                            fireReceived(aa = AAbortImpl.parse(raw));
                            changeState(STA1);
                            return aa;
                        default:
                            throw new PDUException("Unrecognized " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNRECOGNIZED_PDU));
                    }
                } catch (PDUException ule) {
                    try {
                        FsmImpl.this.write(ule.getAAbort());
                    } catch (Exception ignore) {}
                    ;
                    throw ule;
                }
            }
        };

    private final State STA8 =
        new State(Association.AWAITING_WRITE_REL_RP)
        {
            public String toString()
            {
                return "Sta 8 - Awaiting local A-RELEASE response primitive";
            }


            public boolean canWritePDataTF()
            {
                return true;
            }


            void write(PDataTF tf)
                throws IOException
            {
                try {
                    tf.writeTo(out);
                } catch (IOException e) {
                    changeState(STA1);
                    throw e;
                }
            }


            void write(AReleaseRP rp)
                throws IOException
            {
                try {
                    rp.writeTo(out);
                } catch (IOException e) {
                    changeState(STA1);
                    throw e;
                }
                changeState(STA13);
            }
        };

    private final State STA9 =
        new State(Association.RCRS_AWAITING_WRITE_REL_RP)
        {
            public String toString()
            {
                return "Sta 9 - Release collision requestor side;"
                         + " awaiting A-RELEASE response";
            }


            void write(AReleaseRP rp)
                throws IOException
            {
                try {
                    rp.writeTo(out);
                } catch (IOException e) {
                    changeState(STA1);
                    throw e;
                }
                changeState(STA11);
            }
        };

    private final State STA10 =
        new State(Association.RCAS_AWAITING_READ_REL_RP)
        {
            public String toString()
            {
                return "Sta 10 - Release collision acceptor side;"
                         + " awaiting A-RELEASE response";
            }


            PDU parse(UnparsedPDUImpl raw)
                throws PDUException
            {
                try {
                    switch (raw.type()) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                            throw new PDUException("Unexpected " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNEXPECTED_PDU));
                        case 6:
                            PDU pdu = fireReceived(AReleaseRPImpl.parse(raw));
                            changeState(STA12);
                            return pdu;
                        case 7:
                            fireReceived(aa = AAbortImpl.parse(raw));
                            changeState(STA1);
                            return aa;
                        default:
                            throw new PDUException("Unrecognized " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNRECOGNIZED_PDU));
                    }
                } catch (PDUException ule) {
                    try {
                        FsmImpl.this.write(ule.getAAbort());
                    } catch (Exception ignore) {}
                    ;
                    throw ule;
                }
            }
        };

    private final State STA11 =
        new State(Association.RCRS_AWAITING_READ_REL_RP)
        {
            public String toString()
            {
                return "Sta 11 - Release collision requestor side;"
                         + " awaiting A-RELEASE-RP PDU";
            }


            PDU parse(UnparsedPDUImpl raw)
                throws PDUException
            {
                try {
                    switch (raw.type()) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                            throw new PDUException("Unexpected " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNEXPECTED_PDU));
                        case 6:
                            PDU pdu = fireReceived(AReleaseRPImpl.parse(raw));
                            changeState(STA1);
                            return pdu;
                        case 7:
                            fireReceived(aa = AAbortImpl.parse(raw));
                            changeState(STA1);
                            return aa;
                        default:
                            throw new PDUException("Unrecognized " + raw,
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNRECOGNIZED_PDU));
                    }
                } catch (PDUException ule) {
                    try {
                        FsmImpl.this.write(ule.getAAbort());
                    } catch (Exception ignore) {}
                    ;
                    throw ule;
                }
            }
        };

    private final State STA12 =
        new State(Association.RCAS_AWAITING_WRITE_REL_RP)
        {
            public String toString()
            {
                return "Sta 12 - Release collision acceptor side;"
                         + " awaiting A-RELEASE-RP PDU";
            }


            void write(AReleaseRP rp)
                throws IOException
            {
                try {
                    rp.writeTo(out);
                } catch (IOException e) {
                    changeState(STA1);
                    throw e;
                }
                changeState(STA13);
            }
        };

    private final State STA13 =
        new State(Association.ASSOCIATION_TERMINATING)
        {
            public String toString()
            {
                return "Sta 13 - Awaiting Transport Connection Close Indication";
            }


            void entry()
            {
                if (pool != null) {
                    pool.shutdown();
                }
                try {
                    Thread.sleep(soCloseDelay);
                } catch (InterruptedException e) {
                    log.warn("Socket close Delay was interrupted: ", e);
                }
                changeState(STA1);
            }
        };

	void initMDC() {
	    initMDC(rq);
	}
	
    void initMDC(AAssociateRQ rq) {
        MDC.put("ip", s.getInetAddress().getHostAddress());
        if (rq != null) {
            MDC.put("calling", rq.getCallingAET());
            MDC.put("called", rq.getCalledAET());
        }                            
    }

    void clearMDC() {
        MDC.remove("ip");
        MDC.remove("calling");
        MDC.remove("called");
    }

}

