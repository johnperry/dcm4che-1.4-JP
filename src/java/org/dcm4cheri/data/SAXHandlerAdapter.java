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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.dict.VRs;
import org.dcm4cheri.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class SAXHandlerAdapter extends org.xml.sax.helpers.DefaultHandler {
    private static Logger log = Logger.getLogger(SAXHandlerAdapter.class);

    private final DcmObjectHandlerImpl handler;
    
    private int vr;
    
    /** Creates a new instance of DatasetXMLAdapter */
    public SAXHandlerAdapter(DcmHandler handler) {
        this.handler = (DcmObjectHandlerImpl) handler;
    }
    
    public void startDocument() throws SAXException
    {
	handler.setDcmDecodeParam(DcmDecodeParam.EVR_LE);
    }
    
    public void startElement (String uri, String localName,
			      String qName, Attributes attr)
	throws SAXException
    {
        try {
            if ("elm".equals(qName)) {
                element(attr.getValue("tag"), 
                        attr.getValue("vr"),
                        attr.getValue("pos"));
            } else if ("val".equals(qName)) {
                value(attr.getValue("len"),
                      attr.getValue("data"));
            } else if ("seq".equals(qName)) {
                handler.startSequence(-1);
            } else if ("item".equals(qName)) {
                item(attr.getValue("id"),
                        attr.getValue("pos"));
            } else if ("frag".equals(qName)) {
                fragment(attr.getValue("id"),
                        attr.getValue("pos"),
                        attr.getValue("len"),
                        attr.getValue("data"));
            } else if ("filemetainfo".equals(qName)) {
                handler.startFileMetaInfo(
                        preamble(attr.getValue("preamble")));
            } else if ("dataset".equals(qName)) {
                handler.startDataset();
            }
        } catch (Exception ex) {
            log.error(ex);
            throw new SAXException(qName, ex);
        }
    }
    
    private byte[] preamble(String data) {
        return data == null ? null : StringUtils.parseBytes(data);
    }
    
    public void endElement (String uri, String localName, String qName)
	throws SAXException
    {
        try {
            if ("elm".equals(qName))
                handler.endElement();
            else if ("seq".equals(qName))
                handler.endSequence(-1);
            else if ("item".equals(qName))
                handler.endItem(-1);
            else if ("filemetainfo".equals(qName)) {
                handler.endFileMetaInfo();
            } else if ("dataset".equals(qName)) {
                handler.endDataset();
            }
        } catch (Exception ex) {
            log.error(ex);
            throw new SAXException(qName, ex);
        }
    }

//    static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");         
    private void element(String tag, String vrStr, String pos)
             throws IOException {
        handler.startElement(
                        Integer.parseInt(tag,16),
                        vr = VRs.valueOf(vrStr),
                        pos != null ? Integer.parseInt(pos) : -1);
    }
    
    private void value(String len, String val)
            throws IOException {
        int l = Integer.parseInt(len);
        if (VRs.isStringValue(vr)) {
            handler.value(val);
        } else {
            handler.value(StringUtils.parseValue(vr, val));
        }
    }

    private void sequence(String tag) throws IOException {
        handler.startSequence(-1);
    }
    
    private void item(String id, String pos) throws IOException {
        handler.startItem(Integer.parseInt(id),
                pos != null ? Integer.parseInt(pos) : -1, -1);
    }
    
    private void fragment(String id, String pos, String len, String val)
            throws IOException {
        int l = Integer.parseInt(len);
        byte[] b = StringUtils.parseValue(vr, val);
        handler.fragment(Integer.parseInt(id),
                pos != null ? Integer.parseInt(pos) : -1, b, 0, l);
    }
}
