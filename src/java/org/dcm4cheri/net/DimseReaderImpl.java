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

import org.dcm4che.net.*;
import org.dcm4che.data.*;

import org.dcm4cheri.util.LF_ThreadPool;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DimseReaderImpl {
    
    private static final DcmObjectFactory dcmObjFact =
            DcmObjectFactory.getInstance();
    private final FsmImpl fsm;
    private int timeout = 0;
    
    private PDataTF pDataTF = null;
    private PDataTF.PDV pdv = null;
    private Command cmd = null;
    private byte[] buf = null;
    private LF_ThreadPool pool = null;

    /** Creates a new instance of DimseReader */
    public DimseReaderImpl(FsmImpl fsm) {
        this.fsm = fsm;
    }
    
    public void setThreadPool(LF_ThreadPool pool) {
       this.pool = pool;
    }
        
    public synchronized Dimse read(int timeout) throws IOException {
        this.timeout = timeout;
        try {
            if (!nextPDV()) {
                return null;
            }
        } catch (EOFException e) {
            FsmImpl.log.warn("Socket closed on open association:" + fsm.socket());
            return null;
        }
        if (!pdv.cmd()) {
            abort("Command PDV expected, but received " + pdv);
        }
        int pcid = pdv.pcid();
        String tsUID = fsm.getAcceptedTransferSyntaxUID(pcid);
        if (tsUID == null) {
            abort("No Presentation Context negotiated with pcid:" + pcid);
        }
        InputStream in = new PDataTFInputStream(pdv.getInputStream());
        cmd = dcmObjFact.newCommand();
        boolean ds = false;
        try {
            cmd.read(in);
            ds = cmd.hasDataset();
        } catch (IllegalArgumentException e) { // very lousy Exception Handling 
            abort(e.getMessage());
        } catch (DcmValueException e) {
            abort(e.getMessage());
        } finally {
            in.close();
        }
        in = null;
        if (ds) {
            if (!nextPDV()) {
                throw new EOFException(
                        "Association released during receive of DIMSE");
            }
            if (pdv.cmd()) {
                abort("Data PDV expected, but received " + pdv);
            }
            if (pcid != pdv.pcid()) {
                abort("Mismatch between Command PDV pcid: " + pcid
                        + " and " + pdv);
            }
            in = new PDataTFInputStream(pdv.getInputStream());
        } else { // no Dataset
           // if no Data Fragment
            forkNextReadNext();
        }
        DimseImpl retval = new DimseImpl(pcid, tsUID, cmd, in);
        fsm.fireReceived(retval);
        return retval;
    }

    private void forkNextReadNext() {
       if (pool == null)
          return;
       if (cmd.isRequest()) {
          switch (cmd.getCommandField()) {
             case Command.C_GET_RQ:
             case Command.C_FIND_RQ:
             case Command.C_MOVE_RQ:
             case Command.C_CANCEL_RQ:
                break;
             default:
                // no need for extra thread in syncron mode
                if (fsm.getMaxOpsPerformed() == 1)
                   return;
          }
       }
       pool.promoteNewLeader();
    }

    private InputStream nextStream() throws IOException {
        if (pdv != null && pdv.last()) {
           // if last Data Fragment
           if (!pdv.cmd()) {
              forkNextReadNext();
           }
           return null;
        }
        if (!nextPDV()) {
            throw new EOFException(
                    "Association released during receive of DIMSE");
        }
        return pdv.getInputStream();
    }
    
    private boolean nextPDV() throws IOException {
        boolean hasPrev = pdv != null && !pdv.last();
        boolean prevCmd = hasPrev && pdv.cmd();
        int prevPcid = hasPrev ? pdv.pcid() : 0;
        while (pDataTF == null || (pdv = pDataTF.readPDV()) == null) {
            if (!nextPDataTF()) {
                return false;
            }
        }
        if (hasPrev && (prevCmd != pdv.cmd() || prevPcid != pdv.pcid())) {
            abort("Mismatch of following PDVs: " + pdv);
        }
        return true;
    }
    
    private void abort(String msg) throws IOException {
        AAbort aa = new AAbortImpl(AAbort.SERVICE_USER, 0);
        fsm.write(aa);
        throw new PDUException(msg, aa);
    }

    private boolean nextPDataTF() throws IOException {
        if (buf == null) {
            buf = new byte[fsm.getReadMaxLength() + 6];
        }
        PDU pdu = fsm.read(timeout, buf);
        if (pdu instanceof PDataTF) {
            pDataTF = (PDataTF)pdu;
            return true;
        }
        if (pdu instanceof AReleaseRP) {
            return false;
        }
        if (pdu instanceof AReleaseRQ) {
            fsm.write(AReleaseRPImpl.getInstance());
            return false;
        }
        throw new PDUException("Received " + pdu, (AAbort)pdu);
    }                

    private class PDataTFInputStream extends InputStream {
        private InputStream in;

        PDataTFInputStream(InputStream in) {
            this.in = in;
        }

        public int available() throws IOException {
            if(in == null) {
                return 0; // no way to signal EOF from available()
            }
            return in.available();
        }
        
        public int read() throws IOException {
            if (in == null) {
                return -1;
            }
            int c = in.read();
            if (c == -1) {
                in = nextStream();
                return read();
            }
            return c;
        }
        
        public int read(byte b[], int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if ((off < 0) || (off > b.length) || (len < 0) ||
                       ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            } 
            while (in != null) {  
                int n = in.read(b, off, len);
                if (n > 0)
                    return n;
                in = nextStream();
            }
            return -1;
        }
        
        public void close() throws IOException {
            while (in != null) {
                in = nextStream();
            }
        }
    }
}
