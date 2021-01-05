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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParseException;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DataSource;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4che.server.PollDirSrv;
import org.dcm4che.server.PollDirSrvFactory;
import org.dcm4che.util.DcmURL;
import org.dcm4che.util.SSLContextAdapter;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class DcmSnd implements PollDirSrv.Handler {

    // Constants -----------------------------------------------------
    private static final String[] DEF_TS = { UIDs.ImplicitVRLittleEndian };
    private static final int PCID_ECHO = 1;

    // Attributes ----------------------------------------------------
    static final Logger log = Logger.getLogger("DcmSnd");
    private static ResourceBundle messages =
        ResourceBundle.getBundle("DcmSnd", Locale.getDefault());

    private static final UIDDictionary uidDict =
        DictionaryFactory.getInstance().getDefaultUIDDictionary();
    private static final AssociationFactory aFact =
        AssociationFactory.getInstance();
    private static final DcmObjectFactory oFact =
        DcmObjectFactory.getInstance();
    private static final DcmParserFactory pFact =
        DcmParserFactory.getInstance();

    private static final int ECHO = 0;
    private static final int SEND = 1;
    private static final int POLL = 2;

    private final int mode;
    private DcmURL url = null;
    private int repeatSingle = 1;
    private int repeatWhole = 1;
    private int priority = Command.MEDIUM;
    private int acTimeout = 5000;
    private int dimseTimeout = 0;
    private int soCloseDelay = 500;
    private String studyUIDSuffix = null;
    private String seriesUIDSuffix = null;
    private String instUIDSuffix = null;
    private AAssociateRQ assocRQ = aFact.newAAssociateRQ();
    private boolean packPDVs = false;
    private boolean truncPostPixelData = false;
    private int bufferSize = 2048;
    private byte[] buffer = null;
    private SSLContextAdapter tls = null;
    private String[] cipherSuites = null;
    private Dataset overwrite = oFact.newDataset();
    private PollDirSrv pollDirSrv = null;
    private File pollDir = null;
    private long pollPeriod = 5000L;
    private ActiveAssociation activeAssociation = null;
    private int sentCount = 0;
    private long sentBytes = 0L;
    private boolean excludePrivate = false;

    // Static --------------------------------------------------------
    private static final LongOpt[] LONG_OPTS =
        new LongOpt[] {
            new LongOpt("ac-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("dimse-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("so-close-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("prior-high", LongOpt.NO_ARGUMENT, null, 'P'),
            new LongOpt("prior-low", LongOpt.NO_ARGUMENT, null, 'p'),
            new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("max-op-invoked", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("pack-pdvs", LongOpt.NO_ARGUMENT, null, 'k'),
            new LongOpt("trunc-post-pixeldata", LongOpt.NO_ARGUMENT, null, 't'),
            new LongOpt("exclude-private", LongOpt.NO_ARGUMENT, null, 'x'),
            new LongOpt("buf-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("set", LongOpt.REQUIRED_ARGUMENT, null, 's'),
            new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt(
                "tls-cacerts-passwd",
                LongOpt.REQUIRED_ARGUMENT,
                null,
                2),
            new LongOpt("poll-dir", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("poll-period", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("poll-retry-open", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt(
                "poll-delta-last-modified",
                LongOpt.REQUIRED_ARGUMENT,
                null,
                2),
            new LongOpt("poll-done-dir", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("repeat-dimse", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("repeat-assoc", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("study-uid-suffix", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("series-uid-suffix", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("inst-uid-suffix", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("merge-config", LongOpt.REQUIRED_ARGUMENT, null, 'm'),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
            };

    private static void set(Configuration cfg, String s) {
        int pos = s.indexOf(':');
        if (pos == -1) {
            cfg.put("set." + s, "");
        } else {
            cfg.put("set." + s.substring(0, pos), s.substring(pos + 1));
        }
    }

    public static void main(String args[]) throws Exception {
        Getopt g = new Getopt("dcmsnd", args, "", LONG_OPTS);

        Configuration cfg =
            new Configuration(DcmSnd.class.getResource("dcmsnd.cfg"));
        String mergeCfg = null;
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 2 :
                    cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
                    break;
                case 'P' :
                    cfg.put("prior", "1");
                    break;
                case 'p' :
                    cfg.put("prior", "2");
                    break;
                case 'k' :
                    cfg.put("pack-pdvs", "true");
                    break;
                case 't' :
                    cfg.put("trunc-post-pixeldata", "true");
                    break;
                case 'x' :
                    cfg.put("exclude-private", "true");
                    break;
                case 's' :
                    set(cfg, g.getOptarg());
                    break;
                case 'm' :
                    mergeCfg = g.getOptarg();
                    break;
                case 'v' :
                    exit(messages.getString("version"), false);
                case 'h' :
                    exit(messages.getString("usage"), false);
                case '?' :
                    exit(null, true);
                    break;
            }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc == 0) {
            exit(messages.getString("missing"), true);
        }
        //      listConfig(cfg);
        try {
            if (mergeCfg != null) {
                mergeCfg(cfg, mergeCfg);
            }
            DcmSnd dcmsnd = new DcmSnd(cfg, new DcmURL(args[optind]), argc);
            dcmsnd.execute(args, optind + 1);
        } catch (Exception e) {
            exit(e.getMessage(), true);
        }
    }

    
    private static void mergeCfg(Configuration cfg, String mergeCfg)
            throws IOException {
        InputStream is = new BufferedInputStream(
                new FileInputStream(mergeCfg));
        try {
            cfg.load(is);
        } finally {
            is.close();
        }
    }
    // Constructors --------------------------------------------------

    DcmSnd(Configuration cfg, DcmURL url, int argc) {
        this.url = url;
        this.priority = Integer.parseInt(cfg.getProperty("prior", "0"));
        this.packPDVs =
            "true".equalsIgnoreCase(cfg.getProperty("pack-pdvs", "false"));
        this.truncPostPixelData =
            "true".equalsIgnoreCase(
                cfg.getProperty("trunc-post-pixeldata", "false"));
        this.excludePrivate =
            "true".equalsIgnoreCase(
                cfg.getProperty("exclude-private", "false"));
        this.bufferSize =
            Integer.parseInt(cfg.getProperty("buf-len", "2048")) & 0xfffffffe;
        this.repeatWhole =
            Integer.parseInt(cfg.getProperty("repeat-assoc", "1"));
        this.repeatSingle =
            Integer.parseInt(cfg.getProperty("repeat-dimse", "1"));
        this.studyUIDSuffix = cfg.getProperty("study-uid-suffix");
        this.seriesUIDSuffix = cfg.getProperty("series-uid-suffix");
        this.instUIDSuffix = cfg.getProperty("inst-uid-suffix");
        this.mode = argc > 1 ? SEND : initPollDirSrv(cfg) ? POLL : ECHO;
        initAssocParam(cfg, url, mode == ECHO);
        initTLS(cfg);
        initOverwrite(cfg);
    }

    // Public --------------------------------------------------------
    public void execute(String[] args, int offset)
        throws InterruptedException, IOException, GeneralSecurityException {
        switch (mode) {
            case ECHO :
                echo();
                break;
            case SEND :
                send(args, offset);
                break;
            case POLL :
                poll();
                break;
            default :
                throw new RuntimeException("Illegal mode: " + mode);
        }
    }
    private ActiveAssociation openAssoc()
        throws IOException, GeneralSecurityException {
        Association assoc =
            aFact.newRequestor(newSocket(url.getHost(), url.getPort()));
        assoc.setAcTimeout(acTimeout);
        assoc.setDimseTimeout(dimseTimeout);
        assoc.setSoCloseDelay(soCloseDelay);
        assoc.setPackPDVs(packPDVs);

        PDU assocAC = assoc.connect(assocRQ);
        if (!(assocAC instanceof AAssociateAC)) {
            return null;
        }
        ActiveAssociation retval = aFact.newActiveAssociation(assoc, null);
        retval.start();
        return retval;
    }

    public void echo()
        throws InterruptedException, IOException, GeneralSecurityException {
        long t1 = System.currentTimeMillis();
        int count = 0;
        for (int i = 0; i < repeatWhole; ++i) {
            ActiveAssociation active = openAssoc();
            if (active != null) {
                if (active
                    .getAssociation()
                    .getAcceptedTransferSyntaxUID(PCID_ECHO)
                    == null) {
                    log.error(messages.getString("noPCEcho"));
                } else
                    for (int j = 0; j < repeatSingle; ++j, ++count) {
                        active.invoke(
                            aFact.newDimse(
                                PCID_ECHO,
                                oFact.newCommand().initCEchoRQ(j)),
                            null);
                    }
                active.release(true);
            }
        }
        long dt = System.currentTimeMillis() - t1;
        log.info(
            MessageFormat.format(
                messages.getString("echoDone"),
                new Object[] { new Integer(count), new Long(dt)}));
    }

    public void send(String[] args, int offset)
        throws InterruptedException, IOException, GeneralSecurityException {
        if (bufferSize > 0) {
            buffer = new byte[bufferSize];
        }
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < repeatWhole; ++i) {
            ActiveAssociation active = openAssoc();
            if (active != null) {
                for (int k = offset; k < args.length; ++k) {
                    send(active, new File(args[k]));
                }
                active.release(true);
            }
        }
        long dt = System.currentTimeMillis() - t1;
        log.info(
            MessageFormat.format(
                messages.getString("sendDone"),
                new Object[] {
                    new Integer(sentCount),
                    new Long(sentBytes),
                    new Long(dt),
                    new Float(sentBytes / (1.024f * dt)),
                    }));
    }

    public void poll() {
        pollDirSrv.start(pollDir, pollPeriod);
    }

    // PollDirSrv.Handler implementation --------------------------------
    public void openSession() throws Exception {
        activeAssociation = openAssoc();
        if (activeAssociation == null) {
            throw new IOException("Could not open association");
        }
    }

    public boolean process(File file) throws Exception {
        return sendFile(activeAssociation, file);
    }

    public void closeSession() {
        if (activeAssociation != null) {
            try {
                activeAssociation.release(true);
            } catch (Exception e) {
                log.warn("release association throws:", e);
            }
            activeAssociation = null;
        }
    }

    // Private -------------------------------------------------------
    private void send(ActiveAssociation active, File file)
        throws InterruptedException, IOException {
        if (!file.isDirectory()) {
            for (int i = 0; i < repeatSingle; ++i) {
                sendFile(active, file);
            }
            return;
        }
        File[] list = file.listFiles();
        for (int i = 0; i < list.length; ++i) {
            send(active, list[i]);
        }
    }

    private boolean sendFile(ActiveAssociation active, File file)
        throws InterruptedException, IOException {
        InputStream in = null;
        DcmParser parser = null;
        Dataset ds = null;
        try {
            try {
                in = new BufferedInputStream(new FileInputStream(file));
                parser = pFact.newDcmParser(in);
                try {
                    FileFormat format = parser.detectFileFormat();
                    ds = oFact.newDataset();
                    parser.setDcmHandler(ds.getDcmHandler());
                    parser.parseDcmFile(format, Tags.PixelData);
                    if (parser.getReadTag() == Tags.PixelData) {
                        if (parser.getStreamPosition() + parser.getReadLength()
                            > file.length()) {
                            throw new EOFException(
                                "Pixel Data Length: "
                                    + parser.getReadLength()
                                    + " exceeds file length: "
                                    + file.length());
                        }
                    }
                    log.info(
                        MessageFormat.format(
                            messages.getString("readDone"),
                            new Object[] { file }));
                } catch (DcmParseException e) {
                    log.error(
                        MessageFormat.format(
                            messages.getString("failformat"),
                            new Object[] { file }));
                    return false;
                }
            } catch (IOException e) {
                log.error(
                    MessageFormat.format(
                        messages.getString("failread"),
                        new Object[] { file, e }));
                return false;
            }
            sendDataset(active, file, parser, ds);
            return true;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                };
            }
        }
    }

    private boolean sendDataset(ActiveAssociation active, File file,
            DcmParser parser, Dataset ds)
            throws InterruptedException, IOException {
        applyUIDSuffix(ds);
        doOverwrite(ds);
        String sopInstUID = ds.getString(Tags.SOPInstanceUID);
        if (sopInstUID == null) {
            log.error(
                MessageFormat.format(
                    messages.getString("noSOPinst"),
                    new Object[] { file }));
            return false;
        }
        String sopClassUID = ds.getString(Tags.SOPClassUID);
        if (sopClassUID == null) {
            log.error(
                MessageFormat.format(
                    messages.getString("noSOPclass"),
                    new Object[] { file }));
            return false;
        }
        PresContext pc = null;
        Association assoc = active.getAssociation();
        if (parser.getDcmDecodeParam().encapsulated) {
            String tsuid = ds.getFileMetaInfo().getTransferSyntaxUID();
            if ((pc = assoc.getAcceptedPresContext(sopClassUID, tsuid))
                == null) {
                log.error(
                    MessageFormat.format(
                        messages.getString("noPCStore3"),
                        new Object[] {
                            uidDict.lookup(sopClassUID),
                            uidDict.lookup(tsuid),
                            file }));
                return false;
            }
        } else if (
            (pc =
                assoc.getAcceptedPresContext(
                    sopClassUID,
                    UIDs.ImplicitVRLittleEndian))
                == null
                && (pc =
                    assoc.getAcceptedPresContext(
                        sopClassUID,
                        UIDs.ExplicitVRLittleEndian))
                    == null
                && (pc =
                    assoc.getAcceptedPresContext(
                        sopClassUID,
                        UIDs.ExplicitVRBigEndian))
                    == null) {
            log.error(
                MessageFormat.format(
                    messages.getString("noPCStore2"),
                    new Object[] { uidDict.lookup(sopClassUID), file }));
            return false;

        }
        active.invoke(
            aFact.newDimse(
                pc.pcid(),
                oFact.newCommand().initCStoreRQ(
                    assoc.nextMsgID(),
                    sopClassUID,
                    sopInstUID,
                    priority),
                new MyDataSource(parser, ds, buffer)),
            null);
        sentBytes += parser.getStreamPosition();
        ++sentCount;
        return true;
    }

    private void applyUIDSuffix(Dataset ds) {
        if (studyUIDSuffix != null)
            ds.putUI(
                Tags.StudyInstanceUID,
                ds.getString(Tags.StudyInstanceUID, "") + studyUIDSuffix);
        if (seriesUIDSuffix != null)
            ds.putUI(
                Tags.SeriesInstanceUID,
                ds.getString(Tags.SeriesInstanceUID, "") + seriesUIDSuffix);
        if (instUIDSuffix != null)
            ds.putUI(
                Tags.SOPInstanceUID,
                ds.getString(Tags.SOPInstanceUID, "") + instUIDSuffix);
    }

    private void doOverwrite(Dataset ds) {
        for (Iterator it = overwrite.iterator(); it.hasNext();) {
            DcmElement el = (DcmElement) it.next();
            ds.putXX(el.tag(), el.vr(), el.getByteBuffer());
        }
    }

    private final class MyDataSource implements DataSource {
        final DcmParser parser;
        final Dataset ds;
        final byte[] buffer;
        MyDataSource(DcmParser parser, Dataset ds, byte[] buffer) {
            this.parser = parser;
            this.ds = ds;
            this.buffer = buffer;
        }
        public void writeTo(OutputStream out, String tsUID)
            throws IOException {
            DcmEncodeParam netParam =
                (DcmEncodeParam) DcmDecodeParam.valueOf(tsUID);
            if (excludePrivate)
                ds.excludePrivate().writeDataset(out, netParam);
            else
                ds.writeDataset(out, netParam);
            if (parser.getReadTag() == Tags.PixelData) {
                DcmDecodeParam fileParam = parser.getDcmDecodeParam();
                ds.writeHeader(
                    out,
                    netParam,
                    parser.getReadTag(),
                    parser.getReadVR(),
                    parser.getReadLength());
                if (netParam.encapsulated) {
                    parser.parseHeader();
                    while (parser.getReadTag() == Tags.Item) {
                        ds.writeHeader(
                            out,
                            netParam,
                            parser.getReadTag(),
                            parser.getReadVR(),
                            parser.getReadLength());
                        writeValueTo(out, false);
                        parser.parseHeader();
                    }
                    if (parser.getReadTag() != Tags.SeqDelimitationItem) {
                        throw new DcmParseException(
                            "Unexpected Tag:"
                                + Tags.toString(parser.getReadTag()));
                    }
                    if (parser.getReadLength() != 0) {
                        throw new DcmParseException(
                            "(fffe,e0dd), Length:" + parser.getReadLength());
                    }
                    ds.writeHeader(
                        out,
                        netParam,
                        Tags.SeqDelimitationItem,
                        VRs.NONE,
                        0);
                } else {
                    boolean swap =
                        fileParam.byteOrder != netParam.byteOrder
                            && parser.getReadVR() == VRs.OW;
                    writeValueTo(out, swap);
                }
                if (truncPostPixelData) {
                    return;
                }

                ds.clear();
                try {
                    parser.parseDataset(fileParam, -1);
                } catch (IOException e) {
                    log.warn("Error reading post-pixeldata attributes:", e);
                }
                if (excludePrivate)
                    ds.excludePrivate().writeDataset(out, netParam);
                else
                    ds.writeDataset(out, netParam);
            }
        }

        private void writeValueTo(OutputStream out, boolean swap)
            throws IOException {
            InputStream in = parser.getInputStream();
            int len = parser.getReadLength();
            if (swap && (len & 1) != 0) {
                throw new DcmParseException(
                    "Illegal length of OW Pixel Data: " + len);
            }
            if (buffer == null) {
                if (swap) {
                    int tmp;
                    for (int i = 0; i < len; ++i, ++i) {
                        tmp = in.read();
                        out.write(in.read());
                        out.write(tmp);
                    }
                } else {
                    for (int i = 0; i < len; ++i) {
                        out.write(in.read());
                    }
                }
            } else {
                byte tmp;
                int c, remain = len;
                while (remain > 0) {
                    c = in.read(buffer, 0, Math.min(buffer.length, remain));
                    if (c == -1) {
                        throw new EOFException("EOF during read of pixel data");
                    }
                    if (swap) {
                        if ((c & 1) != 0) {
                            buffer[c++] = (byte) in.read();
                        }
                        for (int i = 0; i < c; ++i, ++i) {
                            tmp = buffer[i];
                            buffer[i] = buffer[i + 1];
                            buffer[i + 1] = tmp;
                        }
                    }
                    out.write(buffer, 0, c);
                    remain -= c;
                }
            }
            parser.setStreamPosition(parser.getStreamPosition() + len);
        }
    }

    private Socket newSocket(String host, int port)
        throws IOException, GeneralSecurityException {
        if (cipherSuites != null) {
            return tls.getSocketFactory(cipherSuites).createSocket(host, port);
        } else {
            return new Socket(host, port);
        }
    }

    private static void exit(String prompt, boolean error) {
        if (prompt != null)
            System.err.println(prompt);
        if (error)
            System.err.println(messages.getString("try"));
        System.exit(1);
    }

    private static String maskNull(String aet) {
        return aet != null ? aet : "DCMSND";
    }

    private final void initAssocParam(
        Configuration cfg,
        DcmURL url,
        boolean echo) {
        acTimeout = Integer.parseInt(cfg.getProperty("ac-timeout", "5000"));
        dimseTimeout = Integer.parseInt(cfg.getProperty("dimse-timeout", "0"));
        soCloseDelay =
            Integer.parseInt(cfg.getProperty("so-close-delay", "500"));
        assocRQ.setCalledAET(url.getCalledAET());
        assocRQ.setCallingAET(maskNull(url.getCallingAET()));
        assocRQ.setMaxPDULength(
            Integer.parseInt(cfg.getProperty("max-pdu-len", "16352")));
        assocRQ.setAsyncOpsWindow(
            aFact.newAsyncOpsWindow(
                Integer.parseInt(cfg.getProperty("max-op-invoked", "0")),
                1));
        if (echo) {
            assocRQ.addPresContext(
                aFact.newPresContext(PCID_ECHO, UIDs.Verification, DEF_TS));
            return;
        }
        for (Enumeration it = cfg.keys(); it.hasMoreElements();) {
            String key = (String) it.nextElement();
            if (key.startsWith("pc.")) {
                initPresContext(
                    Integer.parseInt(key.substring(3)),
                    cfg.tokenize(cfg.getProperty(key), new LinkedList()));
            }
        }
    }

    private final void initPresContext(int pcid, List val) {
        Iterator it = val.iterator();
        String as = UIDs.forName((String) it.next());
        String[] tsUIDs = new String[val.size() - 1];
        for (int i = 0; i < tsUIDs.length; ++i) {
            tsUIDs[i] = UIDs.forName((String) it.next());
        }
        assocRQ.addPresContext(aFact.newPresContext(pcid, as, tsUIDs));
    }

    private void initOverwrite(Configuration cfg) {
        for (Enumeration it = cfg.keys(); it.hasMoreElements();) {
            String key = (String) it.nextElement();
            if (key.startsWith("set.")) {
                try {
                    overwrite.putXX(
                        Tags.forName(key.substring(4)),
                        cfg.getProperty(key));
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Illegal entry in dcmsnd.cfg - "
                            + key
                            + "="
                            + cfg.getProperty(key));
                }
            }
        }
    }

    private boolean initPollDirSrv(Configuration cfg) {
        String pollDirName = cfg.getProperty("poll-dir", "", "<none>", "");
        if (pollDirName.length() == 0) {
            return false;
        }

        pollDir = new File(pollDirName);
        if (!pollDir.isDirectory()) {
            throw new IllegalArgumentException(
                "Not a directory - " + pollDirName);
        }
        pollPeriod =
            1000L * Integer.parseInt(cfg.getProperty("poll-period", "5"));
        pollDirSrv = PollDirSrvFactory.getInstance().newPollDirSrv(this);
        pollDirSrv.setOpenRetryPeriod(Integer.parseInt(cfg.getProperty(
                "poll-retry-open", "60")) * 1000L);
        pollDirSrv.setDeltaLastModified(
            1000L
                * Integer.parseInt(
                    cfg.getProperty("poll-delta-last-modified", "3")));
        String doneDirName = cfg.getProperty("poll-done-dir", "", "<none>", "");
        if (doneDirName.length() != 0) {
            File doneDir = new File(doneDirName);
            if (!doneDir.isDirectory()) {
                throw new IllegalArgumentException(
                    "Not a directory - " + doneDirName);
            }
            pollDirSrv.setDoneDir(doneDir);
        }
        return true;
    }

    private void initTLS(Configuration cfg) {
        try {
            cipherSuites = url.getCipherSuites();
            if (cipherSuites == null) {
                return;
            }
            tls = SSLContextAdapter.getInstance();
            char[] keypasswd =
                cfg.getProperty("tls-key-passwd", "secret").toCharArray();
            tls.setKey(
                tls.loadKeyStore(
                    DcmSnd.class.getResource(
                        cfg.getProperty("tls-key", "certificates/test_sys_1.p12")),
                    keypasswd),
                keypasswd);
            tls.setTrust(
                tls.loadKeyStore(
                    DcmSnd.class.getResource(
                        cfg.getProperty("tls-cacerts", "certificates/mesa_certs.jks")),
                    cfg
                        .getProperty("tls-cacerts-passwd", "secret")
                        .toCharArray()));
            tls.init();
        } catch (Exception ex) {
            throw new RuntimeException(
                "Could not initalize TLS configuration: ",
                ex);
        }
    }

}
