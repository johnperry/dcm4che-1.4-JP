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

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DataSource;
import org.dcm4che.net.Dimse;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class DimseImpl implements Dimse {

    protected static final DcmObjectFactory objFact =
            DcmObjectFactory.getInstance();
    private final int pcid;
    private final Command cmd;
    private Dataset ds;
    private InputStream in;
    private final DataSource src;
    private String tsUID;
        
    public DimseImpl(int pcid, String tsUID, Command cmd, InputStream in) {
        this.pcid = pcid;
        this.cmd = cmd;
        this.ds = null;
        this.src = null;
        this.in = in;
        this.tsUID = tsUID;
    }

    public DimseImpl(int pcid, Command cmd, Dataset ds, DataSource src) {
        this.pcid = pcid;
        this.cmd = cmd;
        this.ds = ds;
        this.src = src;
        this.in = null;
        this.tsUID = null;
        this.cmd.putUS(Tags.DataSetType,
                ds == null && src == null ? Command.NO_DATASET : 0);
    }

    public final int pcid() {
        return pcid;
    }
    
    public final Command getCommand() {
        return cmd;
    }
    
    public final String getTransferSyntaxUID() {
        return tsUID;
    }
    
    final void setTransferSyntaxUID(String tsuid) {
        this.tsUID = tsuid;
    }
    
    public final Dataset getDataset() throws IOException {
        if (ds != null) {
            return ds;
        }
        if (in == null) {
            return null;
        }
        if (tsUID == null) {
            throw new IllegalStateException();
        }
        ds = objFact.newDataset();
        ds.readDataset(in, DcmDecodeParam.valueOf(tsUID), -1);
        in.close();
        in = null;
        return ds;
    }
    
    public final InputStream getDataAsStream() {
        return in;
    }
    
    public void writeTo(OutputStream out, String tsUID) throws IOException {
        if (src != null) {
            src.writeTo(out, tsUID);
            return;
        }
        if (ds == null) {
            throw new IllegalStateException("Missing Dataset");
        }
        ds.writeDataset(out, DcmDecodeParam.valueOf(tsUID));       
    }

    public String toString() {
       return "[pc-" + pcid + "] " + cmd.toString();
    }
    
    public void closeDataStream() {
       if (in != null)
           try {
               in.close();
           } catch (IOException ignore) {               
           }           
    }
}