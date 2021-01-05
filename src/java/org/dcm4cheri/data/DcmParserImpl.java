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
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.DcmParseException;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRMap;
import org.dcm4che.dict.VRs;
import org.xml.sax.ContentHandler;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.3.22
 */
final class DcmParserImpl implements org.dcm4che.data.DcmParser {

    private static final Logger log = Logger.getLogger(DcmParserImpl.class);
    
    private static final int MGLIB_MAGIC = 0x4D414749; // MAGI
    private static final int TS_ID_TAG = 0x00020010;
    private static final int ITEM_TAG = 0xFFFEE000;
    private static final int ITEM_DELIMITATION_ITEM_TAG = 0xFFFEE00D;
    private static final int SEQ_DELIMITATION_ITEM_TAG = 0xFFFEE0DD;
    private static final int MIN_MAX_VALLEN = 0x10000; // 64K
    private static final int ZLIB_HEADER = 0x789c;
    
    private final byte[] b0 = new byte[0];
    private final byte[] b12 = new byte[12];
    private final ByteBuffer bb12 = 
            ByteBuffer.wrap(b12).order(ByteOrder.LITTLE_ENDIAN);
//    private boolean explicitVR = false;
    private DcmDecodeParam decodeParam = DcmDecodeParam.IVR_LE;
    private int maxValLen = Integer.MAX_VALUE;
        
    private DataInput in = null;
    private DcmHandler handler = null;
    private VRMap vrMap = VRMap.DEFAULT;
   
    private int rTag = -1;
    private int rVR = -1;
    private int rLen = -1;
    private long rPos = 0L;
    private int hLen = -1;
    private boolean readHeader = true;
    private boolean eof = false;
    private String tsUID = null;
    
    private ByteArrayOutputStream unBuf = null;

    private DcmDecodeParam fixInvalidSequenceEncoding;
    
    public DcmParserImpl(InputStream in) {
        this.in = in instanceof DataInput ? (DataInput)in
                                          : new DataInputStream(in);
    }

    public DcmParserImpl(ImageInputStream in) {        
        if (in == null) {
            throw new NullPointerException();
        }
        this.in = in;
    }

    public InputStream getInputStream() {
        return (InputStream)in;
    }

    public ImageInputStream getImageInputStream() {
        return (ImageInputStream)in;
    }

    public int getMaxValueLength() {
        return maxValLen;
    }

    public void setMaxValueLength(int maxValLen) {
        if (maxValLen < MIN_MAX_VALLEN)
            throw new IllegalArgumentException(
                    "maxValLen: " + maxValLen + " < " + MIN_MAX_VALLEN);
        this.maxValLen = maxValLen;
    }

    public final int getReadTag() {
        return rTag;
    }
        
    public final int getReadVR() {
        return rVR;
    }
        
    public final int getReadLength() {
        return rLen;
    }
        
    public final long getStreamPosition() {
        return rPos;
    }

    public final void setStreamPosition(long rPos) {
        this.rPos = rPos;
    }
    public final void seek(long pos) throws IOException {
        if (!(in instanceof ImageInputStream)) {
            throw new UnsupportedOperationException();
        }
        ((ImageInputStream)in).seek(pos);
        this.rPos = pos;
    }
    
    public final boolean hasSeenEOF() {
        return eof;
    }
        
    public final void setDcmHandler(DcmHandler handler) {
        this.handler = handler;
    }
    
    public final void setSAXHandler(ContentHandler hc, TagDictionary dict) {
        this.handler = new DcmHandlerAdapter(hc, dict);
    }

    public final void setSAXHandler2(ContentHandler ch, TagDictionary dict,
            int[] excludeTags, int excludeValueLengthLimit, File basedir) {
        this.handler = new DcmHandlerAdapter2(ch, dict, excludeTags,
                excludeValueLengthLimit, basedir);
    }
    /*    
    public final void setInput(InputStream in) {
        if (in != null) {
            this.in = in instanceof DataInput ? (DataInput)in
                                              : new DataInputStream(in);
        } else {
            this.in = null;
        }
    }

    public final void setInput(ImageInputStream in) {        
        this.in = in;
    }
*/    
    public final void setVRMap(VRMap vrMap) {
        if (vrMap == null)
            throw new NullPointerException();
        this.vrMap = vrMap;
    }    

    public final void setDcmDecodeParam(DcmDecodeParam param)
            throws IOException {
        if (log.isDebugEnabled())
            log.debug(param.toString());
        if (param.deflated != decodeParam.deflated) {
            if (!param.deflated)
                throw new UnsupportedOperationException(
                        "Cannot remove Inflater");
            else {
                InputStream is = (in instanceof ImageInputStream)
                        ? new InputStreamAdapter((ImageInputStream) in)
                        : (InputStream) in;;
                if (hasZLIBHeader()) {
                    log.warn("Deflated DICOM Stream with ZLIB Header");
                    in = new DataInputStream(new InflaterInputStream(is));
                } else {
                    in = new DataInputStream(new InflaterInputStream(is,
                                  new Inflater(true)));
                }
            }
        }
        bb12.order(param.byteOrder);
        decodeParam = param;
    }
    
    private boolean hasZLIBHeader() throws IOException {
        byte[] buf = b12;
        if (in instanceof ImageInputStream) {
            ImageInputStream iis = (ImageInputStream) in;
            iis.mark();
            iis.read(buf, 0, 2);
            iis.reset();
        } else {
            InputStream is = (InputStream) in;
            if (!is.markSupported())
                return false;
            is.mark(2);
            is.read(buf, 0, 2);
            is.reset();
        }
        return (((buf[0] & 0xff) << 8) 
                | (buf[1] & 0xff)) == ZLIB_HEADER;
    }

    public final DcmDecodeParam getDcmDecodeParam() {
        return decodeParam;
    }

    private String logMsg() {
        return "rPos:" + rPos + " " + Tags.toString(rTag) 
                + " " + VRs.toString(rVR)
                + " #" + rLen;
    }
    
    public FileFormat detectFileFormat() throws IOException { 
        FileFormat retval = null;
        if (in instanceof DataInputStream)
            retval = detectFileFormat((DataInputStream)in);        
        else if (in instanceof ImageInputStream)
            retval = detectFileFormat((ImageInputStream)in);
        else
            throw new UnsupportedOperationException("" + in);
        if (log.isDebugEnabled())
            log.debug("detect " + retval);
        return retval;
    }
    

    private FileFormat detectFileFormat(DataInputStream in) throws IOException {
        in.mark(132);
        byte[] b = new byte[132];
        try
        {
            in.readFully(b, 0, 132);
            if (b[128] == 'D' 
                && b[129] == 'I' 
                && b[130] == 'C'
                && b[131] == 'M')
            {
                return FileFormat.DICOM_FILE;
            }
        } catch (IOException ignore) {
        } finally {
            in.reset();
        }
        return detectFileFormat(b);
    }

    private FileFormat detectFileFormat(ImageInputStream in) throws IOException {
        in.mark();
        byte[] b = new byte[132];
        try
        {
            in.readFully(b, 0, 132);
            if (b[128] == 'D' 
                && b[129] == 'I' 
                && b[130] == 'C'
                && b[131] == 'M')
            {
                return FileFormat.DICOM_FILE;
            }
        } catch (IOException ignore) {
        } finally {
            in.reset();
        }
        return detectFileFormat(b);
    }

    private FileFormat detectFileFormat(byte[] b) throws DcmParseException, IOException
    {
        boolean bigEndian = b[1] != 0;
        int tag = bigEndian
            ? (b[0] & 0xff) << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff)
            : (b[1] & 0xff) << 24 | (b[0] & 0xff) << 16 | (b[3] & 0xff) << 8 | (b[2] & 0xff);
        if (tag == MGLIB_MAGIC) {
            return detectMglibFileFormat(b);
        }
        boolean fmi = (tag & 0xffff0000) == 0x00020000;
        int vr = ((b[4] & 0xff) << 8) | (b[5] & 0xff);
        if (VRMap.DEFAULT.lookup(tag) == vr) {
            return fmi
                    ? (bigEndian 
                            ? FileFormat.EVR_BE_FILE_WO_PREAMBLE
                            : FileFormat.DICOM_FILE_WO_PREAMBLE) 
                    : (bigEndian 
                            ? FileFormat.EVR_BE_STREAM 
                            : FileFormat.EVR_LE_STREAM);
        }
        int len = bigEndian
            ? (b[4] & 0xff) << 24 | (b[5] & 0xff) << 16 | (b[6] & 0xff) << 8 | (b[7] & 0xff)
            : (b[7] & 0xff) << 24 | (b[6] & 0xff) << 16 | (b[5] & 0xff) << 8 | (b[4] & 0xff);
        if (len > 0 && len < 116) {
            int nexttag = bigEndian
                ? (b[len + 8] & 0xff) << 24 | (b[len + 9] & 0xff) << 16 
                        | (b[len + 10] & 0xff) << 8 | (b[len + 11] & 0xff)
                : (b[len + 9] & 0xff) << 24 | (b[len + 8] & 0xff) << 16 
                        | (b[len + 11] & 0xff) << 8 | (b[len + 10] & 0xff);
            if ((nexttag & 0xffff0000) == (tag & 0xffff0000) && (nexttag & 0xffff) > (tag & 0xffff)) {
                return fmi
                ? (bigEndian 
                        ? FileFormat.IVR_BE_FILE_WO_PREAMBLE
                        : FileFormat.IVR_LE_FILE_WO_PREAMBLE)
                : (bigEndian 
                        ? FileFormat.IVR_BE_STREAM 
                        : FileFormat.ACRNEMA_STREAM);
            }
        }
        throw new DcmParseException("Unknown Format");
    }
    
    private FileFormat detectMglibFileFormat(byte[] b) throws IOException {
        String s;
        boolean compressed = false;
        while ((s = in.readLine()) != null) {
            rPos += s.length() + 1;
            if ("ENDINFO".equals(s)) {
                break;
            }
            if ("COMPRESSION MG1.1".equals(s)) {
                compressed = true;
            }
        }
        return (compressed ? FileFormat.MGLIB_COMPRESSED : FileFormat.MGLIB);
    }

    public int parseHeader() throws IOException {
        eof = false;
        try {
            b12[0] = in.readByte();
        } catch (EOFException ex) {
            eof = true;
            log.debug("Detect EOF");
            return -1;
        }
        in.readFully(b12, 1, 7);
        rPos += 8;
        rTag = (bb12.getShort(0) << 16) | (bb12.getShort(2) & 0xffff);
        int retval = 8;
        switch (rTag) {
            case ITEM_TAG:
            case ITEM_DELIMITATION_ITEM_TAG:
            case SEQ_DELIMITATION_ITEM_TAG:
                rVR = VRs.NONE;
                rLen = bb12.getInt(4);
                break;
            default:
                if (!decodeParam.explicitVR) {
                    rVR = vrMap.lookup(rTag);
                    rLen = bb12.getInt(4);
                } else {
                    rVR = (bb12.get(4) << 8) | (bb12.get(5) & 0xff);
                    if (VRs.isLengthField16Bit(rVR)) {
                        rLen = bb12.getShort(6) & 0xffff;
                        if (rVR == VRs.UN_SIEMENS) {
                            if (log.isDebugEnabled()) {
                                log.debug("Replace invalid VR '??' of "
                                        + Tags.toString(rTag) + " by 'UN'");
                            }
                            rVR = VRs.UN;
                        }
                    } else {
                        in.readFully(b12, 8, 4);
                        rPos += 4;
                        rLen = bb12.getInt(8);
                        retval = 12;
                        if (rVR == VRs.OB)
                            switch (rTag) {
                            case Tags.CTDIPhantomTypeCodeSeq:
                            case Tags.OtherPatientIDSeq:                            	
                                if (log.isDebugEnabled())
                                    log.debug("Detect invalid VR 'OB' of " + Tags.toString(rTag)
                                            + " - switch Transfer Syntax to IVR_LE");
                                fixInvalidSequenceEncoding = decodeParam;
                                setDcmDecodeParam(DcmDecodeParam.IVR_LE);
                            case Tags.AcquisitionType:
                            case Tags.XRayTubeCurrentInuA:
                            case Tags.SingleCollimationWidth:
                            case Tags.TotalCollimationWidth:
                                rVR = vrMap.lookup(rTag);
                                if (log.isDebugEnabled())
                                    log.debug("Replace invalid VR 'OB' of "
                                            + Tags.toString(rTag) + " by " + VRs.toString(rVR));
                         }
                    }
                }
        }
        if (unBuf != null)
            unBuf.write(b12, 0, retval);
        if (log.isDebugEnabled())
            log.debug(logMsg());
        return retval;
    }

    private byte[] parsePreamble() throws IOException {
        log.debug("rPos:" + rPos);

        byte[] b128 = new byte[128];        
        in.readFully(b128,0,128);
        rPos += 128;
        in.readFully(b12, 0, 4);
        rPos += 4;
        if (b12[0] != (byte)'D' || b12[1] != (byte)'I'
                || b12[2] != (byte)'C' || b12[3] != (byte)'M')
            throw new DcmParseException("Missing DICM Prefix");

        return b128;
    }
    
    public long parseFileMetaInfo(boolean preamble, DcmDecodeParam param)
            throws IOException {
        rPos = 0L;
        byte[] data = preamble ? parsePreamble() : null;
        if (handler != null)
            handler.startFileMetaInfo(data);
        
        setDcmDecodeParam(param);
        parseGroup(2);
        if (handler != null)
            handler.endFileMetaInfo();
        return rPos;
    }

    public long parseFileMetaInfo() throws IOException {
        return parseFileMetaInfo(true, DcmDecodeParam.EVR_LE);
    }
    
    public long parseCommand() throws IOException {
        if (handler != null)
            handler.startCommand();
                
        setDcmDecodeParam(DcmDecodeParam.IVR_LE);
        long read = parseGroup(0);
        if (handler != null)
            handler.endCommand();
        return read;
    }

    private long parseGroup(int groupTag) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("parse group " + groupTag);
        }
        if (handler != null)
            handler.setDcmDecodeParam(decodeParam);

        final long rPos0 = rPos;
        final int hlen = parseHeader();
        if (hlen != 8 || (rTag >>> 16) != groupTag || rVR != VRs.UL
                || rLen != 4)
            throw new DcmParseException("hlen=" + hlen + ", " + logMsg());

        in.readFully(b12, 0, 4);
        rPos += 4;
        if (handler != null) {
            handler.startElement(rTag, rVR, rPos0);
            byte[] b4 = new byte[4];
            System.arraycopy(b12, 0, b4, 0, 4);
            try {
                handler.value(b4, 0, 4);
            } catch (IllegalArgumentException x) {
                throw new DcmParseException(x.getMessage(), x);
            }
            handler.endElement();
        }
        return doParse(-1, bb12.getInt(0)) + 12;
    }
/*    
    public long parse(int stopTag, int length) throws IOException {
        if (handler != null)
            handler.setDcmDecodeParam(decodeParam);
        return doParse(stopTag, length);
    }
*/
    public long parseDataset(String tuid, int stopTag)
      throws IOException
    {
        return parseDataset(DcmDecodeParam.valueOf(tuid), stopTag);
    }
               
    public long parseDataset(DcmDecodeParam param, int stopTag)
            throws IOException {
         return parseDataset(param, stopTag, -1);
    }
	
    public long parseDataset(DcmDecodeParam param, int stopTag, int length)
			throws IOException {
		if (param != null)
			setDcmDecodeParam(param);
		if (handler != null) {
			handler.startDataset();
			handler.setDcmDecodeParam(decodeParam);
		}
		long read = doParse(stopTag, length);
		if (handler != null)
			handler.endDataset();
		return read;
	}    


    public long parseDcmFile(FileFormat format, int stopTag)
            throws IOException {
        return parseDcmFile(format, stopTag, -1);
    }

    public long parseDcmFile(FileFormat format, int stopTag, int length)
			throws IOException {
		if (format == null) {
			format = detectFileFormat();
		}
		if (handler != null)
			handler.startDcmFile();
		DcmDecodeParam param = format.decodeParam;
		rPos = 0L;
		if (format.hasFileMetaInfo) {
			tsUID = null;
			parseFileMetaInfo(format.hasPreamble, format.decodeParam);
			if (tsUID == null)
				log.warn("Missing Transfer Syntax UID in FMI");
			else
				param = DcmDecodeParam.valueOf(tsUID);
		}
		parseDataset(param, stopTag, length);
		if (handler != null)
			handler.endDcmFile();
		return rPos;
	}    
	
    public long parseItemDataset() throws IOException {
        in.readFully(b12, 0, 8);
        rPos += 8;
        int itemtag = (bb12.getShort(0) << 16)
                    | (bb12.getShort(2) & 0xffff);
        int itemlen = bb12.getInt(4);
        if (itemtag == SEQ_DELIMITATION_ITEM_TAG) {
            if (itemlen != 0) {
                throw new DcmParseException(
                                    "(fffe,e0dd), Length:" + itemlen);
            }
            return -1L;
        }
        if (itemtag != ITEM_TAG) {
            throw new DcmParseException(Tags.toString(itemtag));
        }
        if (log.isDebugEnabled()) {
            log.debug("rpos:" + (rPos-8) + ",(fffe,e0dd)");
        }
        if (handler != null) {
            handler.startDataset();
        }
        long lread;
        if (itemlen == -1) {
            lread = doParse(ITEM_DELIMITATION_ITEM_TAG, itemlen);
            if (rTag != ITEM_DELIMITATION_ITEM_TAG || rLen != 0)
                throw new DcmParseException(logMsg());
        } else {
            lread = doParse(-1, itemlen);
        }
        if (handler != null)
            handler.endDataset();
        return 8 + lread;
    }
    
    public void unreadHeader() {
        if (!readHeader || hLen == -1) {
            throw new IllegalArgumentException();
        }
        readHeader = false;
    }
    
    private long doParse(int stopTag, int length) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("rpos:" + rPos
                    + ",stopTag:" + Tags.toString(stopTag)
                    + ",length:" + length);
        }
        long lread = 0;        
        if (length != 0) {
            long llen = length & 0xffffffffL;
            loop: do {
                long rPos0 = rPos;
                if (readHeader) {
                    hLen = parseHeader();
                    if (hLen == -1) {
                        if (length != -1)
                            throw new EOFException();
                        break loop;
                    }
                } else {
                    rPos0 -= hLen;
                    readHeader = true;
                }
                lread += hLen;
                if ((stopTag & 0xffffffffL) <= (rTag & 0xffffffffL)) 
                    break loop;
                if (handler != null && unBuf == null
                        && rTag != ITEM_DELIMITATION_ITEM_TAG)
                    handler.startElement(rTag, rVR, rPos0);

                if (rLen == -1 || rVR == VRs.SQ) {
                    switch (rVR) {
                        case VRs.SQ:
                        case VRs.OB: case VRs.OF: case VRs.OW:
                        case VRs.UN:
                            break;
                        default:
                            throw new DcmParseException(logMsg());
                    }                    
                    lread += parseSequence(rTag, rVR, rLen);
                } else {
                    if (rLen < 0 || rLen > maxValLen)
                        throw new DcmParseException(logMsg()
                                + ", value length [" + (rLen&0xffffffffL)
                                + "] exceeds maximal supported length ["
                                + maxValLen + "]");
                    readValue();
                    lread += rLen;
                }
                if (handler != null && unBuf == null)
                    handler.endElement();
            } while (length == -1 || lread < llen);
            if (length != -1 && lread > llen)
                log.info(logMsg()
                        + ", value length extend specified read length " + llen
                        + " for " + (lread-llen) + " Bytes!");
        }
        return lread;
    }

    private long parseSequence(int tag, int vr, int sqLen) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("rPos:" + rPos + "," + VRs.toString(vr)
                    + " #" + sqLen);
        }
        if (handler != null && unBuf == null)
            handler.startSequence(sqLen);
        long lread = 0;        
        if (sqLen != 0) {
            long llen = sqLen & 0xffffffffL;
            int id = 0;
            loop: do {
                in.readFully(b12, 0, 8);
                rPos += 8;
                if (unBuf != null)
                    unBuf.write(b12, 0, 8);
                lread += 8;
                int itemtag = (bb12.getShort(0) << 16)
                            | (bb12.getShort(2) & 0xffff);
                int itemlen = bb12.getInt(4);
                switch (itemtag) {
                    case SEQ_DELIMITATION_ITEM_TAG:
                        if (sqLen != -1) {
                            log.warn("Unexpected Sequence Delimination Item"
                                + " (fffe,e0dd) for Sequence with explicit length: "
                                + sqLen);
                        }
                        if (itemlen != 0)
                            throw new DcmParseException(
                                    "(fffe,e0dd), Length:" + itemlen);
                        break loop;
                    case ITEM_TAG:
                        lread += parseItem(++id, vr, itemlen);
                        break;
                    default:
                        throw new DcmParseException(
                                Tags.toString(itemtag));
                }
            } while (sqLen == -1 || lread < llen);
            if (sqLen != -1 && lread > llen)
                throw new DcmParseException(logMsg() + ", Read: " + lread
                        + ", Length: " + llen);
        }
//        rLen = sqLen; // restore rLen value
        if (handler != null && unBuf == null)
            handler.endSequence(sqLen);
		if (fixInvalidSequenceEncoding != null
				&& (tag == Tags.CTDIPhantomTypeCodeSeq || tag == Tags.OtherPatientIDSeq)) {
            if (log.isDebugEnabled())
                log.debug("Switch Transfer Syntax back to " + fixInvalidSequenceEncoding);
            setDcmDecodeParam(fixInvalidSequenceEncoding);
            fixInvalidSequenceEncoding = null;
        }
        return lread;
    }
            
    private long parseItem(int id, int vr, int itemlen) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("rPos:" + rPos + "," + VRs.toString(vr)
                    + " #" + itemlen);
        }
        switch (vr) {
            case VRs.SQ:
                return parseSQItem(id, itemlen);
            case VRs.UN:
                if (itemlen == -1)
                    return parseUNItem(id, itemlen);
                // fall through
            case VRs.OB: case VRs.OF: case VRs.OW:
                return readFragment(id, itemlen);
            default:
                throw new RuntimeException(logMsg());
        }
   }

    private long parseUNItem(int id, int itemlen) throws IOException {
        long retval;
        if (unBuf != null)
            retval = parseSQItem(id, itemlen);
        else {
            long rPos0 = rPos;
            unBuf = new ByteArrayOutputStream();
            final DcmDecodeParam tmpDecodeParam = decodeParam;
            try {
                setDcmDecodeParam(DcmDecodeParam.IVR_LE);
                bb12.order(ByteOrder.LITTLE_ENDIAN);
                retval = parseSQItem(id, itemlen);
                if (handler != null) {
                    handler.fragment(id, rPos0-8, unBuf.toByteArray(), 0,
                            unBuf.size()-8);
                }
            } finally {
                setDcmDecodeParam(tmpDecodeParam);
                unBuf = null;
            }
        }
        return retval;
    }

    private long parseSQItem(int id, int itemlen) throws IOException {
        if (handler != null && unBuf == null)
            handler.startItem(id, rPos-8, itemlen);

        long lread;
        if (itemlen == -1) {
            lread = doParse(ITEM_DELIMITATION_ITEM_TAG, itemlen);
            if (rTag != ITEM_DELIMITATION_ITEM_TAG || rLen != 0)
                throw new DcmParseException(logMsg());
        } else
            lread = doParse(-1, itemlen);

        if (handler != null && unBuf == null)
            handler.endItem(itemlen);

        return lread;
    }
    
    private int readValue() throws IOException {
        byte[] data = readBytes(rLen);
        if (handler != null && unBuf == null) {
            try {
                handler.value(data, 0, rLen);
            } catch (IllegalArgumentException x) {
                throw new DcmParseException(x.getMessage(), x);
            }
        }
        if (rTag == TS_ID_TAG)
            tsUID = decodeUID(data, rLen-1);
        return rLen;
    }
    
    private String decodeUID(byte[] data, int rlen1) {
        if (data.length == 0) {
            log.warn("Empty Transfer Syntax UID");
            return "";
        }
        
        while (rlen1 >= 0 && data[rlen1] == 0 || data[rlen1] == ' ')
            rlen1--;
        if (rlen1 < 0) {
            log.warn("Empty Transfer Syntax UID in FMI");
            return "";
        }
        try {
            return new String(data, 0, rlen1+1, "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            log.warn("Decoding Transfer Syntax UID in FMI failed!", ex);
            return null;
        }
    }
    
    private int readFragment(int id, int itemlen) throws IOException {
        long rPos0 = rPos;
        byte[] data = readBytes(itemlen);
        if (handler != null && unBuf == null)
            handler.fragment(id, rPos0-8, data, 0, itemlen);
        return itemlen;
    }
    
    private byte[] readBytes(int len) throws IOException {
        if (len == 0)
            return b0;
        byte[] retval;
        try {
            retval = new byte[len];
        } catch (OutOfMemoryError e) {
            throw new DcmParseException(logMsg()
                    + ", out of memory allocating byte[]");
        }
        in.readFully(retval, 0, len);
        rPos += len;
        if (unBuf != null)
            unBuf.write(retval, 0, len);
        return retval;
    }

}
