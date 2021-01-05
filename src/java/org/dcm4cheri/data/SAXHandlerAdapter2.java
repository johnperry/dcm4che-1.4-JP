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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.dict.VRMap;
import org.dcm4che.dict.VRs;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 7531 $ $Date: 2008-10-14 10:05:45 +0200 (Di, 14 Okt 2008) $
 * @since 07.09.2004
 *
 */
class SAXHandlerAdapter2 extends DefaultHandler {
    private static Logger log = Logger.getLogger(SAXHandlerAdapter2.class);

    private static final int EXPECT_ELM = 0;

    private static final int EXPECT_VAL_OR_FIRST_ITEM = 1;

    private static final int EXPECT_FRAG = 2;

    private static final int EXPECT_NEXT_ITEM = 3;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private final StringBuffer sb = new StringBuffer();

    private final Stack vrStack = new Stack();

    private final Stack idStack = new Stack();
    
    private final DcmObjectHandlerImpl handler;
    
    private final File baseDir;

    private int vr;

    private int state = EXPECT_ELM;
    
    private String src;

    public SAXHandlerAdapter2(final DcmHandler handler, File baseDir) {
        this.handler = (DcmObjectHandlerImpl) handler;
        this.baseDir = baseDir;
    }

    public void startDocument() throws SAXException {
        handler.setDcmDecodeParam(DcmDecodeParam.EVR_LE);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attrs) throws SAXException {
        try {
            sb.setLength(0);
            out.reset();
            if ("attr".equals(qName)) {
                onStartElement(attrs.getValue("tag"), attrs.getValue("vr"), attrs.getValue("src"));
            } else if ("item".equals(qName)) {
                onStartItem(attrs.getValue("src"));
            } else if ("filemetainfo".equals(qName)) {
                handler.startFileMetaInfo(new byte[128]);
            } else if ("dataset".equals(qName)) {
                handler.startDataset();
            }
        } catch (Exception ex) {
            log.error(ex);
            throw new SAXException(qName, ex);
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        try {
            if ("attr".equals(qName)) {
                onEndElement();
            } else if ("item".equals(qName)) {
                onEndItem();
            } else if ("filemetainfo".equals(qName)) {
                handler.endFileMetaInfo();
            } else if ("dataset".equals(qName)) {
                handler.endDataset();
            }
        } catch (Exception ex) {
            log.error(ex);
            throw new SAXException(qName, ex);
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        try {
            switch (state) {
            case EXPECT_VAL_OR_FIRST_ITEM:
                if (vr == VRs.SQ) return;
            case EXPECT_FRAG:
                sb.append(ch, start, length);
                parse(false);
                break;
            }
        } catch (Exception ex) {
            log.error(ex);
            throw new SAXException(ex);
        }
    }

    private void onStartElement(String tagStr, String vrStr, String src) throws IOException {
        int tag = (int) Long.parseLong(tagStr, 16);
        this.vr = vrStr == null ? VRMap.DEFAULT.lookup(tag) : VRs.valueOf(vrStr);
        handler.startElement(tag, vr, -1);
        state = EXPECT_VAL_OR_FIRST_ITEM;
        this.src = src;
    }

    private void onEndElement() throws IOException {
        switch (state) {
        case EXPECT_VAL_OR_FIRST_ITEM:
            if (vr == VRs.SQ) {
                handler.startSequence(0);
                handler.endSequence(0);
            } else if (src != null) {
                handler.value(readData());
            } else if (parse(true)) {
                handler.value(getByteData());
            } else {
                handler.value(getStringData());
            }
            break;
        case EXPECT_NEXT_ITEM:
            handler.endSequence(-1);
            vrStack.pop();
            idStack.pop();
            break;
        default:
            throw new IllegalArgumentException("state:" + state);
        }
        handler.endElement();
        state = EXPECT_ELM;
    }

    private void onStartItem(String src) throws IOException {
        if (state == EXPECT_VAL_OR_FIRST_ITEM) {
            handler.startSequence(-1);
            vrStack.push(new Integer(vr));
            idStack.push(new int[]{0});
        }
        this.src = src;
        int id = ++((int[])(idStack.peek()))[0];
        if (((Integer) (vrStack.peek())).intValue() == VRs.SQ) {
            handler.startItem(id, -1, -1);
            state = EXPECT_ELM;
        } else {
            state = EXPECT_FRAG;
        }
    }

    private void onEndItem() throws IOException {
        int id = ((int[])(idStack.peek()))[0]--;
        switch (state) {
        case EXPECT_ELM:
            handler.endItem(-1);
            break;
        case EXPECT_FRAG:
            byte[] data;
            if (src != null) {
                data = readData();
            } else {
                parse(true);
                data = getByteData();
            }
            handler.fragment(id, -1, data, 0, data.length);
            break;
        default:
            throw new IllegalArgumentException("state:" + state);
        }
        state = EXPECT_NEXT_ITEM;
    }

    private byte[] readData() throws IOException {
        URL url;
        try {
            url = new URL(src);
        } catch (MalformedURLException e) {
            url = new URL(baseDir.toURL(), src);
        }
        URLConnection con = url.openConnection();
        DataInputStream in = new DataInputStream(con.getInputStream());
        try {
	        int len = (int) con.getContentLength();
	        byte[] data = new byte[(len+1)&~1];
	        in.readFully(data, 0, len);
	        return data;
        } finally {
            in.close();
        }
    }

    private boolean parse(boolean last) {
        switch (vr) {
        case VRs.AT:
            parseAT(last);
            return true;
        case VRs.FL:
        case VRs.OF:
            parseFL_OF(last);
            return true;
        case VRs.FD:
            parseFD(last);
            return true;
        case VRs.OB:
            parseOB(last);
            return true;
        case VRs.OW:
        case VRs.SS:
        case VRs.US:
            parseOW_SS_US(last);
            return true;
        case VRs.SL:
        case VRs.UL:
            parseSL_UL(last);
            return true;
        case VRs.UN:
            parseUN(last);
            return true;
        }
        return false;
    }

    private byte[] getByteData() {
        if ((out.size() & 1) != 0)
            out.write(0);
        try {
            return out.toByteArray();
        } finally {
            out.reset();
        }
    }

    private String getStringData() {
        try {
            return sb.toString();
        } finally {
            sb.setLength(0);
        }
    }

    private void parseAT(boolean last) {
        if (sb.length() == 0) return;
        int begin = 0;
        int end;
        while ((end = sb.indexOf("\\", begin)) != -1) {
            writeTag((int) Long.parseLong(sb.substring(begin, end), 16));
            begin = end + 1;
        }
        String remain = sb.substring(begin);
        sb.setLength(0);
        if (last)
            writeTag((int) Long.parseLong(remain, 16));
        else
            sb.append(remain);
    }

    private void writeTag(int tag) {
        out.write((tag >> 16) & 0xff);
        out.write((tag >> 24) & 0xff);
        out.write((tag >> 0) & 0xff);
        out.write((tag >> 8) & 0xff);
    }

    private void parseFL_OF(boolean last) {
        if (sb.length() == 0) return;
        int begin = 0;
        int end;
        while ((end = sb.indexOf("\\", begin)) != -1) {
            writeInt(Float.floatToIntBits(Float.parseFloat(sb.substring(begin,
                    end))));
            begin = end + 1;
        }
        String remain = sb.substring(begin);
        sb.setLength(0);
        if (last)
            writeInt(Float.floatToIntBits(Float.parseFloat(remain)));
        else
            sb.append(remain);
    }

    private void writeShort(int s) {
        out.write((s >> 0) & 0xff);
        out.write((s >> 8) & 0xff);
    }

    private void writeInt(int i) {
        out.write((i >> 0) & 0xff);
        out.write((i >> 8) & 0xff);
        out.write((i >> 16) & 0xff);
        out.write((i >> 24) & 0xff);
    }

    private void parseFD(boolean last) {
        if (sb.length() == 0) return;
        int begin = 0;
        int end;
        while ((end = sb.indexOf("\\", begin)) != -1) {
            writeLong(Double.doubleToLongBits(Double.parseDouble(sb
                    .substring(begin, end))));
            begin = end + 1;
        }
        String remain = sb.substring(begin);
        sb.setLength(0);
        if (last)
            writeLong(Double.doubleToLongBits(Double.parseDouble(remain)));
        else
            sb.append(remain);
    }

    private void writeLong(long l) {
        out.write((int) ((l >> 0) & 0xff));
        out.write((int) ((l >> 8) & 0xff));
        out.write((int) ((l >> 16) & 0xff));
        out.write((int) ((l >> 24) & 0xff));
        out.write((int) ((l >> 32) & 0xff));
        out.write((int) ((l >> 40) & 0xff));
        out.write((int) ((l >> 48) & 0xff));
        out.write((int) ((l >> 56) & 0xff));
    }

    private void parseOB(boolean last) {
        if (sb.length() == 0) return;
        int begin = 0;
        int end;
        while ((end = sb.indexOf("\\", begin)) != -1) {
            out.write(Integer.parseInt(sb.substring(begin, end)));
            begin = end + 1;
        }
        String remain = sb.substring(begin);
        sb.setLength(0);
        if (last)
            out.write(Integer.parseInt(remain));
        else
            sb.append(remain);
    }

    private void parseUN(boolean last) {
        if (sb.length() == 0) return;
        int begin = 0;
        for (int end = sb.length() - 2; begin < end; ++begin) {
            char ch = sb.charAt(begin);
            if (ch != '\\' || sb.charAt(++begin) == '\\') {
                out.write(ch);
                continue;
            }
            out.write(Integer.parseInt(sb.substring(begin, begin+2), 16));
            ++begin;
        }
        String remain = sb.substring(begin);
        sb.setLength(0);
        if (last)
            out.write(remain.getBytes(), 0, remain.length());
        else
            sb.append(remain);            
    }

    private void parseOW_SS_US(boolean last) {
        if (sb.length() == 0) return;
        int begin = 0;
        int end;
        while ((end = sb.indexOf("\\", begin)) != -1) {
            writeShort(Integer.parseInt(sb.substring(begin, end)));
            begin = end + 1;
        }
        String remain = sb.substring(begin);
        sb.setLength(0);
        if (last)
            writeShort(Integer.parseInt(remain));
        else
            sb.append(remain);
    }

    private void parseSL_UL(boolean last) {
        if (sb.length() == 0) return;
        int begin = 0;
        int end;
        while ((end = sb.indexOf("\\", begin)) != -1) {
            writeInt((int) Long.parseLong(sb.substring(begin, end)));
            begin = end + 1;
        }
        String remain = sb.substring(begin);
        sb.setLength(0);
        if (last)
            writeInt((int) Long.parseLong(remain));
        else
            sb.append(remain);
    }
}