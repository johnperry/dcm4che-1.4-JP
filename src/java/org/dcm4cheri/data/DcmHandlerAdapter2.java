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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4cheri.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 16576 $ $Date: 2012-02-02 11:07:15 +0100 (Do, 02 Feb 2012) $
 * @since 07.09.2004
 *
 */
class DcmHandlerAdapter2 implements DcmHandler {

    private static final char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

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

    private final char[] cbuf = new char[512];

    private final StringBuffer sb = new StringBuffer(512);

    private int tag;

    private long pos;

    private final File outsrcDir;

    private final int[] outsrcTags;
    
    private final int outsrcValueLengthLimit;

    private final StringBuffer outsrcName = new StringBuffer();

    private String tagStr;

    private boolean outsrc;

    private SpecificCharacterSet cs;

    public DcmHandlerAdapter2(final ContentHandler handler,
            final TagDictionary dict, final int[] outsrcTags,
            final int outsrcValueLengthLimit, final File outsrcDir) {
        this.handler = handler;
        this.dict = dict;
        this.outsrcValueLengthLimit = outsrcValueLengthLimit;
        this.outsrcDir = outsrcDir;
        this.outsrcTags = outsrcTags == null ? new int[0] : (int[]) outsrcTags
                .clone();
        Arrays.sort(this.outsrcTags);
    }

    public void startCommand() throws IOException {
        start(COMMAND, "command", EMPTY_ATTR);
    }

    public void endCommand() throws IOException {
        end(COMMAND, "command");
    }

    public void startFileMetaInfo(byte[] preamble) throws IOException {
        start(FILEMETAINFO, "filemetainfo", EMPTY_ATTR);
        if (preamble == null) return;
        try {
            handler.startElement("", "preamble", "preamble", EMPTY_ATTR);
            outOB(preamble, 0, preamble.length);
            handler.endElement("", "preamble", "preamble");
        } catch (SAXException se) {
            throw (IOException) new IOException(
                    "Exception in startFileMetaInfo").initCause(se);
        }
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
            throw (IOException) new IOException("Exception in start " + xmltag)
                    .initCause(se);
        }
    }

    private void end(int docType, String xmltag) throws IOException {
        try {
            handler.endElement("", xmltag, xmltag);
            if (this.docType == docType) {
                handler.endDocument();
                docType = UNDEF;
            }
        } catch (SAXException se) {
            throw (IOException) new IOException("Exception in end " + xmltag)
                    .initCause(se);
        }
    }

    public void setDcmDecodeParam(DcmDecodeParam param) {
        this.byteOrder = param.byteOrder;
        this.explicitVR = param.explicitVR;
    }

    public void startElement(int tag, int vr, long pos) throws IOException {
        this.tag = tag;
        this.outsrc = Arrays.binarySearch(outsrcTags, tag) >= 0;
        this.tagStr = Tags.toHexString(tag, 8);
        this.vr = vr;
        this.pos = pos;
        outsrcName.append(tagStr);
    }

    public void endElement() throws IOException {
        outsrcName.setLength(outsrcName.length() - 8);
    }

    public void startSequence(int length) throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "tag", "tag", "", Tags.toHexString(tag, 8));
            attrs.addAttribute("", "vr", "vr", "", VRs.toString(vr));
            attrs.addAttribute("", "pos", "pos", "", "" + pos);
            if (dict != null) {
                TagDictionary.Entry entry = dict.lookup(tag);
                if (entry != null)
                        attrs.addAttribute("", "name", "name", "", entry.name);
            }
            attrs.addAttribute("", "len", "len", "", "" + length);
            handler.startElement("", "attr", "attr", attrs);
        } catch (SAXException se) {
            throw (IOException) new IOException("Exception in startElement")
                    .initCause(se);
        }
    }

    public void fragment(int id, long pos, byte[] data, int start, int length)
            throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "id", "id", "", "" + id);
            attrs.addAttribute("", "pos", "pos", "", "" + pos);
            attrs.addAttribute("", "len", "len", "", "" + length);
            final String fname = outsrcName.toString() + '-' + id;
            outsrc = outsrc || length > outsrcValueLengthLimit;
            if (outsrc && length > 0) { 
                if (outsrcDir != null)
                    attrs.addAttribute("", "src", "src", "", fname);
                else
                    attrs.addAttribute("", "hide", "hide", "", "true");
            }
            handler.startElement("", "item", "item", attrs);
            if (length > 0) {
                if (outsrc)
                    outsrc(fname, data, start, length);
                else
                    out(data, start, length);
            }
            handler.endElement("", "item", "item");
        } catch (SAXException se) {
            throw (IOException) new IOException("Exception in fragment")
                    .initCause(se);
        }
    }

    private void outsrc(String fname, byte[] data, int start, int length)
            throws IOException {
        if (outsrcDir == null) return;
        FileOutputStream out = new FileOutputStream(new File(outsrcDir, fname));
        try {
            out.write(data, start, length);
        } finally {
            out.close();
        }
    }

    public int vm(byte[] data, int start, int length) throws IOException {
        if (length == 0) return 0;

        switch (vr) {
        case VRs.LT:
        case VRs.ST:
        case VRs.OB:
        case VRs.OW:
        case VRs.UN:
        case VRs.UT:
            return 1;
        case VRs.SS:
        case VRs.US:
            return length >> 1;
        case VRs.AT:
        case VRs.FL:
        case VRs.SL:
        case VRs.UL:
            return length >> 2;
        case VRs.FD:
            return length >> 3;
        }
        int count = 1;
        for (int i = 0, j = start; i < length; ++i, ++j)
            if (data[j] == '\\') ++count;
        return count;
    }

    public void value(byte[] data, int start, int length) throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "tag", "tag", "", tagStr);
            attrs.addAttribute("", "vr", "vr", "", VRs.toString(vr));
            attrs.addAttribute("", "pos", "pos", "", "" + pos);
            if (dict != null) {
                TagDictionary.Entry entry = dict.lookup(tag);
                if (entry != null)
                        attrs.addAttribute("", "name", "name", "", entry.name);
            }
            attrs
                    .addAttribute("", "vm", "vm", "", ""
                            + vm(data, start, length));
            attrs.addAttribute("", "len", "len", "", "" + length);
            String fname = outsrcName.toString();
            outsrc = outsrc || length > outsrcValueLengthLimit;
            if (outsrc && length > 0) {
                if (outsrcDir != null)
                    attrs.addAttribute("", "src", "src", "", fname);
                else
                    attrs.addAttribute("", "hide", "hide", "", "true");
            }
            handler.startElement("", "attr", "attr", attrs);
            if (length > 0) {
                if (outsrc)
                    outsrc(fname, data, start, length);
                else
                    out(data, start, length);
            }
            handler.endElement("", "attr", "attr");
        } catch (SAXException se) {
            throw (IOException) new IOException("Exception in value")
                    .initCause(se);
        }
    }

    public void endSequence(int length) throws IOException {
        try {
            handler.endElement("", "attr", "attr");
        } catch (SAXException se) {
            throw (IOException) new IOException("Exception in endSequence")
                    .initCause(se);
        }
    }

    public void startItem(int id, long pos, int length) throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "id", "id", "", "" + id);
            attrs.addAttribute("", "pos", "pos", "", "" + pos);
            attrs.addAttribute("", "len", "len", "", "" + length);
            handler.startElement("", "item", "item", attrs);
            outsrcName.append('-').append(id).append('-');
        } catch (SAXException se) {
            throw (IOException) new IOException("Exception in startItem")
                    .initCause(se);
        }
    }

    public void endItem(int len) throws IOException {
        try {
            handler.endElement("", "item", "item");

            int outsrcNameLen = outsrcName.length() - 2;
            while (outsrcName.charAt(outsrcNameLen) != '-')
                --outsrcNameLen;
            outsrcName.setLength(outsrcNameLen);
        } catch (SAXException se) {
            throw (IOException) new IOException("Exception in endItem")
                    .initCause(se);
        }
    }

    private void out(byte[] data, int start, int length) throws SAXException {
        switch (vr) {
        case VRs.AE:
        case VRs.AS:
        case VRs.CS:
        case VRs.DA:
        case VRs.DS:
        case VRs.DT:
        case VRs.IS:
        case VRs.TM:
        case VRs.UI:
            outText(data, start, length, false, null); break;
        case VRs.LO:
        case VRs.PN:
        case VRs.SH:
            outText(data, start, length, false, cs); break;
        case VRs.LT:
        case VRs.ST:
        case VRs.UT:
            outText(data, start, length, true, cs); break;
        case VRs.AT:
            outAT(data, start, length);
            return;
        case VRs.FL:
        case VRs.OF:
            outFL_OF(data, start, length);
            return;
        case VRs.FD:
            outFD(data, start, length);
            return;
        case VRs.OB:
            outOB(data, start, length);
            return;
        case VRs.OW:
        case VRs.US:
            outOW_SS_US(data, start, length, 0xffff);
            return;
        case VRs.SL:
            outSL_UL(data, start, length, -1L);
            return;
        case VRs.SS:
            outOW_SS_US(data, start, length, -1);
            return;
        case VRs.UN:
            outUN(data, start, length);
            return;
        case VRs.UL:
            outSL_UL(data, start, length, 0xffffffffL);
            return;
        }
    }

    private void outText(byte[] data, int start, int length,
            boolean preserveLeadingWhitespace, SpecificCharacterSet cs) throws SAXException {
        String s = trim(cs == null ? new String(data, start, length)
                              : cs.decode(data, start, length), preserveLeadingWhitespace);
        if (tag == Tags.SpecificCharacterSet) {
            this.cs = SpecificCharacterSet.valueOf(
                    StringUtils.split(s.trim(), '\\'));
        }
        for (int pos = 0; pos < s.length();) {
            int end = Math.min(s.length(), pos + cbuf.length);
            s.getChars(pos, end, cbuf, 0);
            handler.characters(cbuf, 0, end - pos);
            pos = end;
        }
    }

    private String trim(String s, boolean preserveLeadingWhitespace) {
        if (!preserveLeadingWhitespace)
            return s.trim();
        int len = s.length();
        while ((len > 0) && (s.charAt(len - 1) <= ' '))
            len--;
        return s.substring(0, len);
    }

    private void outAT(byte[] data, int start, int length) throws SAXException {
        ByteBuffer bb = ByteBuffer.wrap(data, start, length).order(byteOrder);
        while (bb.remaining() > 0) {
            if (sb.length() + 9 > cbuf.length) flushChars();
            short grTag = bb.getShort();
            short elTag = bb.getShort();
            Tags.toHexString(sb, Tags.valueOf(grTag, elTag), 8).append('\\');
        }
        sb.setLength(sb.length() - 1);
        flushChars();
    }

    private void outFD(byte[] data, int start, int length) throws SAXException {
        ByteBuffer bb = ByteBuffer.wrap(data, start, length).order(byteOrder);
        while (bb.remaining() > 0) {
            if (sb.length() + 26 > cbuf.length) flushChars();
            sb.append(bb.getDouble()).append('\\');
        }
        sb.setLength(sb.length() - 1);
        flushChars();
    }

    private void outFL_OF(byte[] data, int start, int length)
            throws SAXException {
        ByteBuffer bb = ByteBuffer.wrap(data, start, length).order(byteOrder);
        while (bb.remaining() > 0) {
            if (sb.length() + 16 > cbuf.length) flushChars();
            sb.append(bb.getFloat()).append('\\');
        }
        sb.setLength(sb.length() - 1);
        flushChars();
    }

    private void outSL_UL(byte[] data, int start, int length, long mask)
            throws SAXException {
        ByteBuffer bb = ByteBuffer.wrap(data, start, length).order(byteOrder);
        while (bb.remaining() > 0) {
            if (sb.length() + 12 > cbuf.length) flushChars();
            sb.append(bb.getInt() & mask).append('\\');
        }
        sb.setLength(sb.length() - 1);
        flushChars();
    }

    private void outOW_SS_US(byte[] data, int start, int length, int mask)
            throws SAXException {
        ByteBuffer bb = ByteBuffer.wrap(data, start, length).order(byteOrder);
        while (bb.remaining() > 0) {
            if (sb.length() + 6 > cbuf.length) flushChars();
            sb.append(bb.getShort() & mask).append('\\');
        }
        sb.setLength(sb.length() - 1);
        flushChars();
    }

    private void outOB(byte[] data, int start, int length) throws SAXException {
        for (int i = 0, j = start; i < length; i++, j++) {
            if (sb.length() + 4 > cbuf.length) flushChars();
            sb.append(data[j] & 0xff).append('\\');
        }
        sb.setLength(sb.length() - 1);
        flushChars();
    }

    private void outUN(byte[] data, int start, int length) throws SAXException {
        for (int i = 0, j = start; i < length; i++, j++) {            
            if (sb.length() + 3 > cbuf.length) flushChars();
            int v = data[j];
            if (v >= 32 && v < 128) {
                sb.append((char)v);
                if (v == '\\')
                    sb.append('\\');
            } else {
                sb.append('\\').append(HEX_DIGIT[(v>>4)&0xf]).append(HEX_DIGIT[v&0xf]);
            }
        }
        flushChars();
    }

    private void flushChars() throws SAXException {
        final int end = sb.length();
        sb.getChars(0, end, cbuf, 0);
        handler.characters(cbuf, 0, end);
        sb.setLength(0);
    }

}