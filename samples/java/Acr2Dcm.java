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

import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.UIDGenerator;

import gnu.getopt.*;
import java.io.*;
import java.nio.ByteOrder;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class Acr2Dcm {

    static final byte[] PXDATA_GROUPLEN = {
            (byte)0xe0, (byte)0x7f, (byte)0x00, (byte)0x00, 4, 0, 0, 0 };
    static final byte[] PXDATA_TAG = {
            (byte)0xe0, (byte)0x7f, (byte)0x10, (byte)0x00 };
            
    static final DcmParserFactory pfact = DcmParserFactory.getInstance();
    static final DcmObjectFactory fact = DcmObjectFactory.getInstance();
    static final UIDGenerator gen = UIDGenerator.getInstance();
    static String uid(String uid) {
        return uid != null ? uid : gen.createUID();
    }

    private String studyUID = null;
    private String seriesUID = null;
    private String instUID = null;
    private String classUID = UIDs.SecondaryCaptureImageStorage;
    private boolean skipGroupLen = true;
    private boolean undefSeqLen = true;
    private boolean undefItemLen = true;
    private boolean fmi = true;
        
    /** Creates a new instance of Acr2Dcm */
    public Acr2Dcm() {
    }
    
    public void setStudyUID(String uid) {
        this.studyUID = uid;
    }

    public void setSeriesUID(String uid) {
        this.seriesUID = uid;
    }

    public void setInstUID(String uid) {
        this.instUID = uid;
    }

    public void setClassUID(String uid) {
        this.classUID = uid;
    }

    public void setSkipGroupLen(boolean skipGroupLen) {
		this.skipGroupLen = skipGroupLen;
    }
    
    public void setUndefSeqLen(boolean undefSeqLen) {
		this.undefSeqLen = undefSeqLen;
    }
    
    public void setUndefItemLen(boolean undefItemLen) {
		this.undefItemLen = undefItemLen;
    }
    
    public void setFileMetaInfo(boolean fmi) {
        this.fmi = fmi;
    }

    private DcmEncodeParam encodeParam() {
        return new DcmEncodeParam(ByteOrder.LITTLE_ENDIAN,
                false, false, false, skipGroupLen, undefSeqLen, undefItemLen);
    }
    
    public void convert(File src, File dest)
            throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(src));
        DcmParser p = pfact.newDcmParser(in);
        Dataset ds = fact.newDataset();
        p.setDcmHandler(ds.getDcmHandler());
        try {
            FileFormat format = p.detectFileFormat();
            if (format != FileFormat.ACRNEMA_STREAM) {
                 System.out.println("\n" + src + ": not an ACRNEMA stream!");
                 return;
            }
            p.parseDcmFile(format, Tags.PixelData);
            if (ds.contains(Tags.StudyInstanceUID) 
                    || ds.contains(Tags.SeriesInstanceUID)
                    || ds.contains(Tags.SOPInstanceUID)
                    || ds.contains(Tags.SOPClassUID)) {
                 System.out.println("\n" + src + ": contains UIDs!"
                        + " => probable already DICOM - do not convert");
                 return;
            }
            boolean hasPixelData = p.getReadTag() == Tags.PixelData;
            boolean inflate = hasPixelData && 
                    ds.getInt(Tags.BitsAllocated, 0) == 12;
            int pxlen = p.getReadLength();
            if (hasPixelData) {
                if (inflate) {
                    ds.putUS(Tags.BitsAllocated, 16);
                    pxlen = pxlen * 4 / 3;
                }
                if (pxlen != (ds.getInt(Tags.BitsAllocated, 0) >>> 3)
                        * ds.getInt(Tags.Rows,0) * ds.getInt(Tags.Columns,0)
                        * ds.getInt(Tags.NumberOfFrames,1)
                        * ds.getInt(Tags.NumberOfSamples,1)) {
                     System.out.println("\n" + src + ": mismatch pixel data length!"
                            + " => do not convert");
                     return;
                }
            }
            ds.putUI(Tags.StudyInstanceUID, uid(studyUID));
            ds.putUI(Tags.SeriesInstanceUID, uid(seriesUID));
            ds.putUI(Tags.SOPInstanceUID, uid(instUID));
            ds.putUI(Tags.SOPClassUID, classUID);
            if (!ds.contains(Tags.NumberOfSamples)) {
                ds.putUS(Tags.NumberOfSamples,1);
            }
            if (!ds.contains(Tags.PhotometricInterpretation)) {
                ds.putCS(Tags.PhotometricInterpretation,"MONOCHROME2");
            }
            
            if (fmi) {
                ds.setFileMetaInfo(fact.newFileMetaInfo(ds,
                        UIDs.ImplicitVRLittleEndian));
            }
            OutputStream out = new BufferedOutputStream(
                            new FileOutputStream(dest));
            try {
            } finally {
                ds.writeFile(out, encodeParam());
                if (hasPixelData) {
                    if (!skipGroupLen) {
                        out.write(PXDATA_GROUPLEN);
                        int grlen = pxlen + 8;
                        out.write((byte)grlen);
                        out.write((byte)(grlen >> 8));
                        out.write((byte)(grlen >> 16));
                        out.write((byte)(grlen >> 24));
                    }
                    out.write(PXDATA_TAG);
                    out.write((byte)pxlen);
                    out.write((byte)(pxlen >> 8));
                    out.write((byte)(pxlen >> 16));
                    out.write((byte)(pxlen >> 24));
                }
                if (inflate) {
                    int b2,b3;
                    for (;pxlen > 0; pxlen -= 3) {
                        out.write(in.read());
                        b2 = in.read();
                        b3 = in.read();
                        out.write(b2 & 0x0f);
                        out.write(b2 >> 4 | ((b3 & 0x0f) << 4));
                        out.write(b3 >> 4);
                    }
                } else {
                    for (;pxlen > 0; --pxlen) {
                        out.write(in.read());
                    }
                }
                out.close();
            }
            System.out.print('.');
        } finally {
            in.close();
        }
        
    }        
    
    public int mconvert(String[] args, int optind, File destDir)
            throws IOException {
        int count = 0;
        for (int i = optind, n = args.length-1; i < n; ++i) {
            File src = new File(args[i]);
            count += mconvert(src, new File(destDir, src.getName()));
        }
        return count;
    }        

    public int mconvert(File src, File dest) throws IOException {
        if (src.isFile()) {
            convert(src, dest);
            return 1;
        }
        File[] files = src.listFiles();
        if (files.length > 0 && !dest.exists()) {
            dest.mkdirs();
        }
        int count = 0;
        for (int i = 0; i < files.length; ++i) {
            count += mconvert(files[i], new File(dest, files[i].getName()));
        }
        return count;
    }        
    
    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        LongOpt[] longopts = new LongOpt[8];
        longopts[0] = new LongOpt("study-uid", LongOpt.REQUIRED_ARGUMENT, null, 'S');
        longopts[1] = new LongOpt("series-uid", LongOpt.REQUIRED_ARGUMENT, null, 's');
        longopts[2] = new LongOpt("inst-uid", LongOpt.REQUIRED_ARGUMENT, null, 'i');
        longopts[3] = new LongOpt("class-uid", LongOpt.REQUIRED_ARGUMENT, null, 'c');
        longopts[4] = new LongOpt("grouplen", LongOpt.NO_ARGUMENT, null, 'g');
        longopts[5] = new LongOpt("seqlen", LongOpt.NO_ARGUMENT, null, 'q');
        longopts[6] = new LongOpt("itemlen", LongOpt.NO_ARGUMENT, null, 'm');
        longopts[7] = new LongOpt("no-fmi", LongOpt.NO_ARGUMENT, null, 'n');

        Getopt g = new Getopt("acr2dcm.jar", args, "S:s:i:c:g", longopts, true);
        
        Acr2Dcm acr2dcm = new Acr2Dcm();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {                
                case 'S':
                    acr2dcm.setStudyUID(g.getOptarg());
                    break;
                case 's':
                    acr2dcm.setSeriesUID(g.getOptarg());
                    break;
                case 'i':
                    acr2dcm.setInstUID(g.getOptarg());
                    break;
                case 'c':
                    acr2dcm.setClassUID(g.getOptarg());
                    break;
                case 'g':
                   acr2dcm.setSkipGroupLen(false);
                    break;
                case 'q':
                    acr2dcm.setUndefSeqLen(false);
                    break;
                case 'm':
                    acr2dcm.setUndefItemLen(false);
                    break;
                case 'n':
                    acr2dcm.setFileMetaInfo(false);
                    break;
                case '?':
                    exit("");
                    break;
                }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc < 2) {
            exit("acr2dcm.jar: missing argument\n");
        }

        File dest = new File(args[args.length-1]);
        long t1 = System.currentTimeMillis();
        int count = 1;
        if (dest.isDirectory()) {
            count = acr2dcm.mconvert(args, optind, dest);
        } else {
            File src = new File(args[optind]);
            if (argc > 2 || src.isDirectory()) {
                exit("acr2dcm.jar: when converting several files, "
                        + "last argument must be a directory\n");
            }
            acr2dcm.convert(src, dest);
        }
       long t2 = System.currentTimeMillis();
       System.out.println("\nconverted " + count + " files in "
                + (t2-t1)/1000f + " s.");
    }
    
    private static void exit(String prompt) {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

    private static final String USAGE =
"Usage: java -jar acr2dcm.jar [OPTION]... SOURCE DEST\n" +
"    or java -jar acr2dcm.jar [OPTION]... SOURCE... DIRECTORY\n\n" +
"Convert ACR/NEMA file(s) to DICOM file(s).\n\n" +
"Options:\n" +
" -S --study-uid  <uid>  set value of Study Instance UID; default: auto\n" +
" -s --series-uid <uid>  set value of Series Instance UID; default: auto\n" +
" -i --inst-uid   <uid>  set value of SOP Instance UID; default: auto\n" +
" -c --class-uid  <uid>  set value of SOP Class UID;\n" +
"                          default: 1.2.840.10008.5.1.4.1.1.7\n" +
" -g --grouplen   encode with (gggg,0000) group length attributes\n" +
" --seqlen        encode sequence attributes with explicit length\n" +
" --itemlen       encode sequence items with explicit length\n" +
" --no-fmi        skip File Meta Information\n";
}
