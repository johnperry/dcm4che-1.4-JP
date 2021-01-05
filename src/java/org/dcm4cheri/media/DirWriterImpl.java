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

package org.dcm4cheri.media;

import org.dcm4cheri.util.IntHashtable2;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirWriter;
import org.dcm4che.media.DirRecord;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.*;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DirWriterImpl extends DirReaderImpl implements DirWriter {
    
    private static final Short INACTIVE = new Short((short)0);
    private static final Integer INTEGER0 = new Integer(0);
    private static final String[] TYPE_CODE = {
        "PATIENT",
        "STUDY",
        "SERIES",
        "IMAGE",
        "OVERLAY",
        "MODALITY LUT",
        "VOI LUT",
        "CURVE",
        "TOPIC",
        "VISIT",
        "RESULTS",
        "INTERPRETATION",
        "STUDY COMPONENT",
        "STORED PRINT",
        "RT DOSE",
        "RT STRUCTURE SET",
        "RT PLAN",
        "RT TREAT RECORD",
        "PRESENTATION",
        "WAVEFORM",
        "SR DOCUMENT",
        "KEY OBJECT DOC",
        "SPECTROSCOPY",
        "RAW DATA",
        "REGISTRATION",
        "FIDUCIAL",
        "ENCAP DOC",
        "PRIVATE",
        "MRDR",
    };
    private static final List TYPE_CODE_LIST = Arrays.asList(TYPE_CODE);
    
    private static final byte[] ITEM = {
        (byte)0xFE,(byte)0xFF,(byte)0x00,(byte)0xE0,
        (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
    };
    
    private static final byte[] ITEM_DELIMITER = {
        (byte)0xFE,(byte)0xFF,(byte)0x0D,(byte)0xE0,
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
    };

    private static final byte[] SEQ_DELIMITER = {
        (byte)0xFE,(byte)0xFF,(byte)0xDD,(byte)0xE0,
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
    };

    private static final byte[] PADDING_TAG_OB = {
        (byte)0xFC,(byte)0xFF,(byte)0xFC,(byte)0xFF,(byte)'O',(byte)'B',0,0
    };
    
    private String dirPath;
    private final ImageOutputStream out;
    private final DcmEncodeParam encParam;
    private boolean autoCommit = false;
    private long newRecPos;
    private long rollbackPos;
    private int rollbackOffLastRootRec;
    private TreeMap dirtyOffsets = new TreeMap();

    /** Creates a new instance of DirWriterImpl */
    DirWriterImpl(File file, ImageOutputStream out, DcmEncodeParam encParam) {        
        super(file, out);
        this.out = out;
        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        this.encParam = encParam != null ? encParam : DcmDecodeParam.EVR_LE;
    }
    
    public String[] toFileIDs(File refFile) throws IOException {
        if (dirPath == null) {
           this.dirPath = file.getParentFile().getAbsolutePath();
        }            
        String path = refFile.getAbsolutePath();
        if (!path.startsWith(dirPath)) {
            throw new IllegalArgumentException(path);
        }
        StringTokenizer strTk = new StringTokenizer(
                path.substring(dirPath.length()), File.separator);
        String retVal[] = new String[strTk.countTokens()];
        for (int i = 0; i < retVal.length; ++i) {
            retVal[i] = strTk.nextToken();
        }
        return retVal;
    }

    DirWriterImpl initWriter(FileMetaInfo fmi, String filesetID,
            File descriptorFile, String specCharset) throws IOException {
        parser.setDcmDecodeParam(DcmDecodeParam.EVR_LE);
        fsi = factory.newDataset();
        fsi.setFileMetaInfo(fmi);
        fsi.putCS(Tags.FileSetID, filesetID);
        if (descriptorFile != null) {
            fsi.putCS(Tags.FileSetDescriptorFileID,
                    toFileIDs(descriptorFile.getAbsoluteFile()));
            if (specCharset != null) {
               fsi.putCS(Tags.SpecificCharacterSetOfFileSetDescriptorFile,
                    specCharset);
            }
        }
        fsi.putUL(Tags.OffsetOfFirstRootDirectoryRecord,
                this.offFirstRootRec = 0);
        fsi.putUL(Tags.OffsetOfLastRootDirectoryRecord, this.offLastRootRec = 0);
        fsi.putUS(Tags.FileSetConsistencyFlag, 0);
        fsi.putSQ(Tags.DirectoryRecordSeq);
        fsi.writeFile(out, encParam);
        fsi.remove(Tags.DirectoryRecordSeq);
        if (encParam.undefSeqLen) {
            this.seqLength = -1;
            this.seqValuePos = out.getStreamPosition() - 8;
        } else {
            this.seqLength = 0;
            this.seqValuePos = out.getStreamPosition();
        }
        this.offFirstRootRecValPos = seqValuePos - 38L;
        this.offLastRootRecValPos = seqValuePos-26L;
        this.newRecPos = this.rollbackPos = seqValuePos;
        this.rollbackOffLastRootRec = offLastRootRec;
        return this;
    }
    
    DirWriterImpl initWriter() throws IOException {
        initReader();
        this.newRecPos = this.rollbackPos = seqLength != -1
                ? seqValuePos + (seqLength & 0xffffffffL)
                : offLastRootRec == 0 ? seqValuePos : parseItems();
        this.rollbackOffLastRootRec = offLastRootRec;
        return this;
    }

    private long parseItems() throws IOException {
        parser.seek(offLastRootRec & 0xffffffffL);
        while (parser.parseItemDataset() != -1L)
            ; // noop
        return parser.getStreamPosition() - 8;
    }
    
    public void close() throws IOException {
        commit();
        super.close();
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }
    
    public synchronized void setAutoCommit(boolean autoCommit)
    throws IOException {
        if (this.autoCommit == autoCommit) {
            return;
        }
        if (this.autoCommit = autoCommit) {
            commit();
        }
    }
    
    public synchronized void commit()
    throws IOException {
        if (dirtyOffsets.isEmpty()) { // nothing to commit
            if (newRecPos != rollbackPos) {
                throw new RuntimeException("newRecPos:" + newRecPos
                        + ", rollbackPos:" + rollbackPos);
            }
            return;
        }
        if (newRecPos != rollbackPos) {
            writeTrailer();
            if (seqLength != -1) {
                dirtyOffsets.put(new Long(seqValuePos - 4),
                        new Integer(seqLength += (int)(newRecPos - rollbackPos)));
            }
        }
        for (Iterator iter = dirtyOffsets.entrySet().iterator();
                iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            out.seek(((Long)entry.getKey()).longValue());
            Number num = (Number)entry.getValue();
            if (num instanceof Integer) {
                out.writeInt(num.intValue());
            } else {
                out.writeShort(num.intValue());
            }
        }
        
        dirtyOffsets.clear();
        rollbackOffLastRootRec = offLastRootRec;
        rollbackPos = newRecPos;
    }
    
    public synchronized void rollback()
    throws IOException {
        if (newRecPos == rollbackPos) { // nothing to rollback
            return;
        }
        dirtyOffsets.clear();
        lastRecNextValPosCache.clear();
        lastRootRecNextValPos = null;
        if ((this.offLastRootRec = rollbackOffLastRootRec) == 0) {
            this.offFirstRootRec = 0;
        }
        newRecPos = rollbackPos;
        writeTrailer();
    }
    
    private void writeTrailer() throws IOException {
        out.seek(newRecPos);
        if (seqLength == -1) {
            out.write(SEQ_DELIMITER);
        }
        int padlen = (int)(out.length() - out.getStreamPosition());
        if (padlen > 0) {
            out.write(PADDING_TAG_OB);
            padlen = Math.max(0,padlen - 12);
            out.writeInt((padlen + 1) & ~1);
            if ((padlen & 1) != 0) {
                out.seek(out.length());
                out.write(0);
            }
        }
    }
        
    public synchronized DirRecord add(DirRecord parent, String type, Dataset ds)
    throws IOException {
        return add(parent, type, ds, null, null, null, null);
    }
    
    public synchronized DirRecord add(DirRecord parent, String type, Dataset ds,
            String[] fileIDs, String classUID, String instUID, String tsUID)
    throws IOException {
        return add(parent, type, ds, fileIDs, classUID, instUID, tsUID,
                false);
    }
    
    private DirRecord add(DirRecord parentOrOld, String type, Dataset ds,
            String[] fileIDs, String classUID, String instUID, String tsUID,
            boolean replace) throws IOException {
        if (TYPE_CODE_LIST.indexOf(type) == -1) {
            throw new IllegalArgumentException("type:" + type);
        }
        Dataset ds0004 = factory.newDataset();
        ds0004.putUL(Tags.OffsetOfNextDirectoryRecord, 0);
        ds0004.putUS(Tags.RecordInUseFlag, DirRecord.IN_USE);
        ds0004.putUL(Tags.OffsetOfLowerLevelDirectoryEntity,
                replace ? ((DirRecordImpl)parentOrOld).lower : 0);
        ds0004.putCS(Tags.DirectoryRecordType, type);
        if (fileIDs != null) {
            if (classUID == null) {
                throw new NullPointerException();
            }
            if (instUID == null) {
                throw new NullPointerException();
            }
            if (tsUID == null) {
                throw new NullPointerException();
            }
            ds0004.putCS(Tags.RefFileID, fileIDs);
            ds0004.putUI(Tags.RefSOPClassUIDInFile, classUID);
            ds0004.putUI(Tags.RefSOPInstanceUIDInFile, instUID);
            ds0004.putUI(Tags.RefSOPTransferSyntaxUIDInFile, tsUID);
        }
        out.seek(newRecPos);
        out.write(ITEM, 0, 8);
        ds0004.writeDataset(out, encParam);
        ds.subSet(0x00080000,-1).writeDataset(out, encParam);
        long nextNewRecPos = out.getStreamPosition();
        if (encParam.undefItemLen) {
            out.write(ITEM_DELIMITER, 0, 8);
            nextNewRecPos += 8;
        } else {
            out.seek(newRecPos + 4);
            out.writeInt((int)(nextNewRecPos - newRecPos - 8));
        }
        DirRecordImpl retval = new DirRecordImpl(parser, (int)newRecPos);
        Integer newOff = new Integer((int)newRecPos);
        if (parentOrOld == null) {
            dirtyOffsets.put(
                    setLastRootRecNextValPos(retval.nextValPos), newOff);
            dirtyOffsets.put(new Long(offLastRootRecValPos), newOff);
            offLastRootRec = (int)newRecPos;
            if (offFirstRootRec == 0) {
                offFirstRootRec = (int)newRecPos;
            }
        } else if (replace) {
            DirRecordImpl rec = (DirRecordImpl)parentOrOld;
            dirtyOffsets.put(new Long(((DirRecordImpl)rec).inUsePos), INACTIVE);        
            dirtyOffsets.put(new Long(((DirRecordImpl)rec).lowerValPos), INTEGER0);        
            while (rec.next != 0) {
                rec = new DirRecordImpl(parser, rec.next);
            }
            dirtyOffsets.put(new Long(rec.nextValPos), newOff);
        } else {
            dirtyOffsets.put(
                    setLastRecNextValPos(parentOrOld, retval.nextValPos),
                    newOff);
        }
        newRecPos = nextNewRecPos;
        if (autoCommit) {
            commit();
        }
        return retval;
    }

    private Long lastRootRecNextValPos = null;
    private Long setLastRootRecNextValPos(long newval) throws IOException {
        Long retval = lastRootRecNextValPos;
        lastRootRecNextValPos = new Long(newval);
        if (offLastRootRec == 0) {
            return new Long(offFirstRootRecValPos);
        }
        if (retval != null) {
            return retval;
        }
        return new Long(new DirRecordImpl(parser, offLastRootRec).nextValPos);
    }
    
    private IntHashtable2 lastRecNextValPosCache = new IntHashtable2();
    private Long setLastRecNextValPos(DirRecord parent, long newval)
            throws IOException {
        int key = parent.hashCode();
        Long retval = (Long)lastRecNextValPosCache.get(key);
        lastRecNextValPosCache.put(key, new Long(newval));
        if (retval != null) {
            return retval;
        }
        DirRecordImpl child = (DirRecordImpl)parent.getFirstChild(true);
        if (child == null) {
            return new Long(((DirRecordImpl)parent).lowerValPos);
        }
        while (child.next != 0) {
            child = new DirRecordImpl(parser, child.next);
        }
        return new Long(child.nextValPos);
    }    

    public int remove(DirRecord rec) throws IOException {
        if (rec.getInUseFlag() == DirRecord.INACTIVE) {
            return 0;
        }
        int retval = doRemove(rec);
        if (autoCommit) {
            commit();
        }
        return retval;
    }

    private int doRemove(DirRecord rec) throws IOException {
        dirtyOffsets.put(new Long(((DirRecordImpl)rec).inUsePos), INACTIVE);        
        int retval = 1;
        for (DirRecord child = rec.getFirstChild(true); child != null;
                child = child.getNextSibling(true)) {
            retval += doRemove(child);
        } 
        return retval;
    }

    public DirRecord replace(DirRecord old, String type, Dataset ds)
            throws IOException {
        return replace(old, type, ds, null, null, null, null);
    }    

    public DirRecord replace(DirRecord old, String type, Dataset ds,
            String[] fileIDs, String classUID, String instUID, String tsUID)
            throws IOException {
        if (old.getInUseFlag() == DirRecord.INACTIVE) {
            throw new IllegalArgumentException("" + old);
        }
        return add(old, type, ds, fileIDs, classUID, instUID, tsUID, true);
    }
        
    private File backup() throws IOException {
        close();
        File dir = file.getParentFile();
        String fname = file.getName();
        File bakFile = null;
        do {
           bakFile = new File(dir, fname += '~');
        } while (bakFile.exists());
        file.renameTo(bakFile);
        return bakFile;
    }

    public DirWriter compact() throws IOException {
        File bakFile = backup();
        DirWriterImpl writer = null;
        try {
            DirReaderImpl reader = new DirReaderImpl(bakFile,
                        new FileImageInputStream(bakFile)).initReader();
            try {
                Dataset fsi = reader.getFileSetInfo();
                writer = new DirWriterImpl(file,
                        new FileImageOutputStream(file), encParam);
                writer.initWriter(
                        fsi.getFileMetaInfo(), 
                        fsi.getString(Tags.FileSetID),
                        reader.getDescriptorFile(),
                        fsi.getString(Tags.SpecificCharacterSetOfFileSetDescriptorFile));
                copy(reader, writer);
                writer.commit();
                writer.setAutoCommit(autoCommit);
            } finally {
                try { reader.close(); } catch (IOException ignore) {}
            }
        } catch (IOException e) {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignore) {}
            }
            file.delete();
            bakFile.renameTo(file);
            throw e;
        }
        bakFile.delete();
        return writer;
    }
    
    private void copy(DirReaderImpl src, DirWriterImpl dst) throws IOException {
        for (DirRecord srcRec = src.getFirstRecord(true); srcRec != null;
                srcRec = srcRec.getNextSibling(true)) {
            copyInto(srcRec, dst, null);
        }   
    }

    private void copyInto(DirRecord srcRec, DirWriterImpl dst, DirRecord parent)
            throws IOException {
        DirRecord dstRec = dst.add(parent,
                srcRec.getType(),
                srcRec.getDataset(),
                srcRec.getRefFileIDs(),
                srcRec.getRefSOPClassUID(),
                srcRec.getRefSOPInstanceUID(),
                srcRec.getRefSOPTransferSyntaxUID());
        for (DirRecord childRec = srcRec.getFirstChild(true); childRec != null;
                childRec = childRec.getNextSibling(true)) {
            copyInto(childRec, dst, dstRec);
        }   
    }
}
