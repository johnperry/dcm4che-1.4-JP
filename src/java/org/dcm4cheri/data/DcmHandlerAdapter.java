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

import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.dict.TagDictionary;

import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.dcm4cheri.util.StringUtils;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class DcmHandlerAdapter implements org.dcm4che.data.DcmHandler {

    private static final Attributes EMPTY_ATTR = new AttributesImpl();
    
    private final ContentHandler handler;
    private final TagDictionary dict;
    private boolean explicitVR;
    private ByteOrder byteOrder;
    private static final int UNDEF = 0;
    private static final int COMMAND = 1;
    private static final int FILEMETAINFO = 2;
    private static final int DATASET = 3;
    private static final int DCMFILE = 4;
    private int docType = UNDEF;
    private int vr;
    
    /** Creates a new instance of ContentHandlerAdapter */
    public DcmHandlerAdapter(ContentHandler handler, TagDictionary dict)
    {
        this.handler = handler;
        this.dict = dict;
    }

    public void startCommand() throws IOException {
        start(COMMAND, "command", EMPTY_ATTR);
    }
    
    public void endCommand() throws IOException {
        end(COMMAND, "command");
    }
    
    public void startFileMetaInfo(byte[] preamble) throws IOException {
        AttributesImpl attrs = new AttributesImpl();
        if (preamble != null)
            attrs.addAttribute("","preamble","preamble","",
                    StringUtils.promptBytes(preamble, 0, preamble.length));
        start(FILEMETAINFO, "filemetainfo", attrs);
    }
    
    public void endFileMetaInfo() throws IOException {
        end(FILEMETAINFO, "filemetainfo");
    }

    public void startDataset() throws IOException {
        start(DATASET, "dataset", EMPTY_ATTR);
    }
    
    public void endDataset() throws IOException {
        end(DATASET, "dataset");
    }

    public void startDcmFile() throws IOException {
        start(DCMFILE, "dicomfile", EMPTY_ATTR);
    }

    public void endDcmFile() throws IOException {
        end(DCMFILE, "dicomfile");
    }

    private void start(int docType, String xmltag, Attributes attrs)
            throws IOException {
        try {
            if (this.docType == UNDEF) {
                this.docType = docType;
                handler.startDocument();
            }
            handler.startElement("", xmltag, xmltag, attrs);
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in start " + xmltag)
                    .initCause(se);
        }
    }        
    
    private void end(int docType, String xmltag) throws IOException  {
        try {
            handler.endElement("",xmltag,xmltag);
            if (this.docType == docType) {
                handler.endDocument();
                docType = UNDEF;
            }
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in end " + xmltag)
                    .initCause(se);
        }
    }
        
    public void setDcmDecodeParam(DcmDecodeParam param) {
        this.byteOrder = param.byteOrder;
        this.explicitVR = param.explicitVR;
    }

    public void startElement(int tag, int vr, long pos) throws IOException {
        this.vr = vr;
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("","tag","tag","",Tags.toHexString(tag, 8));
            attrs.addAttribute("","vr","vr","",VRs.toString(vr));
            attrs.addAttribute("","pos","pos","", "" + pos);
            if (dict != null) {
                TagDictionary.Entry entry = dict.lookup(tag);
                if (entry != null)
                    attrs.addAttribute("","name","name","",entry.name);
            }
            handler.startElement("","elm","elm",attrs);
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in startElement")
                    .initCause(se);
        }
    }

    public void startSequence(int length) throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("","len","len","", "" + length);
            handler.startElement("","seq","seq",attrs);
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in startElement")
                    .initCause(se);
        }
    }
    
    public void fragment(int id, long pos, byte[] data, int start, int length)
            throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("","id","id","", "" + id);
            attrs.addAttribute("","pos","pos","", "" + pos);
            attrs.addAttribute("","len","len","", "" + length);
            attrs.addAttribute("","data","data","",
                    StringUtils.promptValue(vr,
                        ByteBuffer.wrap(data, start, length).order(byteOrder)));
            handler.startElement("","frag","frag",attrs);
            handler.endElement("","frag","frag");        
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in fragment")
                    .initCause(se);
        }
    }
    
    public int vm(byte[] data, int start, int length) throws IOException {
        if (length == 0)
            return 0;
        
        switch (vr) {
            case VRs.LT: case VRs.ST: case VRs.OB: case VRs.OW: case VRs.UN:
            case VRs.UT:
                return 1;
            case VRs.SS: case VRs.US:
                return length >> 1;
            case VRs.AT: case VRs.FL: case VRs.SL: case VRs.UL:                
                return length >> 2;
            case VRs.FD:
                return length >> 3;
        }
        int count = 1;
        for (int i = 0, j = start; i < length; ++i,++j)
            if (data[j] == '\\')
                ++count;
        return count;
    }
  
    public void value(byte[] data, int start, int length) throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("","vm","vm","","" + vm(data, start, length));
            attrs.addAttribute("","len","len","","" + length);
            attrs.addAttribute("","data","data","",
                    StringUtils.promptValue(vr, ByteBuffer.wrap(data, start,
                            length).order(byteOrder)));
            handler.startElement("","val","val",attrs);
            handler.endElement("","val","val");        
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in value")
                    .initCause(se);
        }
    }
    
    public void endElement() throws IOException {
        try {
            handler.endElement("","elm","elm");        
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in endElement")
                    .initCause(se);
        }
    }

    public void endSequence(int length) throws IOException {
        try {
            handler.endElement("","seq","seq");
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in endSequence")
                    .initCause(se);
        }
    }
    
    public void startItem(int id, long pos, int length) throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("","id","id","", "" + id);
            attrs.addAttribute("","pos","pos","", "" + pos);
            attrs.addAttribute("","len","len","", "" + length);
            handler.startElement("","item","item",attrs);
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in startItem")
                    .initCause(se);
        }
    }
    
    public void endItem(int len) throws IOException {
        try {
            handler.endElement("","item","item");
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in endItem")
                    .initCause(se);
        }
    }    
}
