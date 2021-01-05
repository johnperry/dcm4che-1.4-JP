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
import org.dcm4che.data.Command;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DimseWriterImpl {

    private final FsmImpl fsm;
    private PDataTFImpl pDataTF = null;
    private int pcid;
    private boolean cmd;
    private boolean packPDVs = false;
    
//    PDataTFOutputStream pDataTFout = null;

    /** Creates a new instance of PDataTFWriteAdapter */
    public DimseWriterImpl(FsmImpl fsm) {
        this.fsm = fsm;
    }
    
    public synchronized void write(Dimse dimse) throws IOException {
        pcid = dimse.pcid();
        String tsUID = fsm.getAcceptedTransferSyntaxUID(pcid);
        if (tsUID == null) {
            throw new IllegalStateException();
        }
        ((DimseImpl)dimse).setTransferSyntaxUID(tsUID);
        fsm.fireWrite(dimse);
        if (pDataTF == null) {
            pDataTF = new PDataTFImpl(fsm.getWriteMaxLength());
        }
        pDataTF.openPDV(pcid, cmd = true);
        OutputStream out = new PDataTFOutputStream();
        Command c = dimse.getCommand();
        try {
            c.write(out);
        } finally {
            out.close();
        }
        if (c.hasDataset()) {
            if (!packPDVs) {
                flushPDataTF();
            }
            pDataTF.openPDV(pcid, cmd = false);
            out = new PDataTFOutputStream();
            try {
                dimse.writeTo(out, tsUID);
            } finally {
                out.close();
            }                
        }
        flushPDataTF();
    }
    
    public void flushPDataTF() throws IOException {
        boolean open = pDataTF.isOpenPDV();
        if (open) {
            pDataTF.closePDV(false);
        }
        if (!pDataTF.isEmpty()) {
            fsm.write(pDataTF);
        }
        pDataTF.clear();
//        pDataTF = new PDataTFImpl(fsm.getMaxLength()); 
        if (open) {
            pDataTF.openPDV(pcid, cmd);
        }
    }
        
    private void closeStream() throws IOException {
        pDataTF.closePDV(true);
        if (!cmd) {
            flushPDataTF();
        }
    }

    /** Getter for property packPDVs.
     * @return Value of property packPDVs.
     */
    public boolean isPackPDVs() {
        return packPDVs;
    }
    
    /** Setter for property packPDVs.
     * @param packPDVs New value of property packPDVs.
     */
    public void setPackPDVs(boolean packPDVs) {
        this.packPDVs = packPDVs;
    }
    
    private class PDataTFOutputStream extends OutputStream {
        public final void write(int b) throws IOException {
            if (pDataTF.free() == 0) {
                flushPDataTF();
            }
            pDataTF.write(b);
        }
        public final void write(byte b[], int off, int len)
                throws IOException {
            if (len == 0) {
                return;
            }
            int toWrite = len;
            for (;;) {
                int n = Math.min(pDataTF.free(), toWrite);
                pDataTF.write(b, off, n);
                off += n;
                toWrite -= n;
                if (toWrite == 0) {
                    return;
                }
                flushPDataTF();
            }
        }
        public void close() throws IOException {
            closeStream();
        }
    }
}
