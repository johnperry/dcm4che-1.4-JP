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

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObject;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.VRs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.LinkedList;
/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class DcmObjectHandlerImpl implements org.dcm4che.data.DcmHandler {

    private final DcmObject result;
    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    private DcmObject curDcmObject;
    private int tag;
    private int vr;
    private long pos;
    private final LinkedList seqStack = new LinkedList();
    
    /** Creates a new instance of DcmHandlerImpl */
    public DcmObjectHandlerImpl(DcmObject result) {
        if (result == null)
            throw new NullPointerException();
        
        this.result = result;
    }

    public void startCommand() {
        curDcmObject = (Command)result;
        seqStack.clear();
    }
    
    public void endCommand() {
        curDcmObject = null;
    }
    
    public void startDcmFile() {
        // noop
    }
    
    public void endDcmFile() {
        // noop
    }
    
    public void startFileMetaInfo(byte[] preamble) {
        if (result instanceof Dataset) {
            curDcmObject = ((Dataset)result).getFileMetaInfo();
            if (curDcmObject == null)
                ((Dataset)result).setFileMetaInfo((FileMetaInfo)
                        (curDcmObject =  new FileMetaInfoImpl()));
        } else
            curDcmObject = (FileMetaInfo)result;
        ((FileMetaInfo) curDcmObject).setPreamble(preamble);
        seqStack.clear();
    }
    
    public void endFileMetaInfo() {
        if (result instanceof Dataset) {
            curDcmObject = result;
        } else
            curDcmObject = null;
    }
    
    public void startDataset() {
        curDcmObject = (Dataset)result;
        seqStack.clear();
    }
    
    public void endDataset() {
        curDcmObject = null;
    }
    
    public void setDcmDecodeParam(DcmDecodeParam param) {
        this.byteOrder = param.byteOrder;
    }
            
    public void startElement(int tag, int vr, long pos)
            throws IOException {
        this.tag = tag;
        this.vr = vr;
        this.pos = pos;
    }
    
    public void endElement() throws IOException {
    }

    public void startSequence(int length) throws IOException {
        seqStack.add(vr == VRs.SQ ? curDcmObject.putSQ(tag)
                                      : curDcmObject.putXXsq(tag, vr));
    }

    public void endSequence(int length) throws IOException {
        seqStack.removeLast();
    }
    
    public void value(byte[] data, int start, int length) throws IOException {
        curDcmObject.putXX(tag, vr,
                ByteBuffer.wrap(data, start, length).order(byteOrder))
                .setStreamPosition(pos);
    }
    

    void value(byte[] data) throws IOException {
        value(data, 0, data.length);
    }

    void value(String data) throws IOException {
        curDcmObject.putXX(tag, vr, data).setStreamPosition(pos);
    }
    
    public void fragment(int id, long pos, byte[] data, int start, int length)
            throws IOException {
        ((DcmElement)seqStack.getLast()).addDataFragment(
                ByteBuffer.wrap(data, start, length).order(byteOrder));
    }
    
    public void startItem(int id, long pos, int length) throws IOException {
        curDcmObject = ((DcmElement)seqStack.getLast()).addNewItem()
                .setItemOffset(pos);
    }
    
    public void endItem(int len) throws IOException {
        curDcmObject = ((Dataset)curDcmObject).getParent();
    }
}
