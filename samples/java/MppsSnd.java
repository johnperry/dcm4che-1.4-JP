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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4che.server.PollDirSrv;
import org.dcm4che.server.PollDirSrvFactory;
import org.dcm4che.util.DcmURL;
import org.dcm4che.util.SSLContextAdapter;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class MppsSnd implements PollDirSrv.Handler {
    
    // Constants -----------------------------------------------------
    private static final String[] DEF_TS = { UIDs.ImplicitVRLittleEndian };
    private static final int PCID_ECHO = 1;
    private static final int PCID_MPPS = 3;
    private static final int PCID_GPPPS = 5;
    
    // Attributes ----------------------------------------------------
    static final Logger log = Logger.getLogger("MppsSnd");
    private static ResourceBundle messages = ResourceBundle.getBundle(
        "MppsSnd", Locale.getDefault());
    
    private static final UIDDictionary uidDict =
        DictionaryFactory.getInstance().getDefaultUIDDictionary();
    private static final AssociationFactory aFact =
        AssociationFactory.getInstance();
    private static final DcmObjectFactory oFact =
        DcmObjectFactory.getInstance();
    
    private static final int ECHO = 0;
    private static final int SEND = 1;
    private static final int POLL = 2;
    
    private final int mode;
    private DcmURL url = null;
    private int acTimeout = 5000;
    private int dimseTimeout = 0;
    private int soCloseDelay = 500;
    private AAssociateRQ assocRQ = aFact.newAAssociateRQ();
    private boolean packPDVs = false;
    private SSLContextAdapter tls = null;
    private String[] cipherSuites = null;
    private PollDirSrv pollDirSrv = null;
    private File pollDir = null;
    private long pollPeriod = 5000L;
    private ActiveAssociation activeAssociation = null;
    
    // Static --------------------------------------------------------
    private static final LongOpt[] LONG_OPTS = new LongOpt[] {
        new LongOpt("ac-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("dimse-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("so-close-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("max-op-invoked", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("pack-pdvs", LongOpt.NO_ARGUMENT, null, 'k'),
        new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("ts", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("poll-dir", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("poll-period", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("poll-retry-open", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("poll-delta-last-modified", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("poll-done-dir", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
        new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
    };
    
    public static void main(String args[]) throws Exception {
        Getopt g = new Getopt("mppssnd", args, "", LONG_OPTS);
        
        Configuration cfg = new Configuration(
        MppsSnd.class.getResource("mppssnd.cfg"));
        
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 2:
                    cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
                    break;
                case 'k':
                    cfg.put("pack-pdvs", "true");
                    break;
                 case 'v':
                    exit(messages.getString("version"), false);
                case 'h':
                    exit(messages.getString("usage"), false);
                case '?':
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
            MppsSnd mppssnd = new MppsSnd(cfg, new DcmURL(args[optind]), argc);
            mppssnd.execute(args, optind+1);
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
        }
    }
    
    // Constructors --------------------------------------------------
    
    MppsSnd(Configuration cfg, DcmURL url, int argc) {
        this.url = url;
        this.mode = argc > 1 ? SEND : initPollDirSrv(cfg) ? POLL : ECHO;
        initAssocParam(cfg, url, mode == ECHO);
        initTLS(cfg);
    }
    
    // Public --------------------------------------------------------
    public void execute(String[] args, int offset)
    throws InterruptedException, IOException, GeneralSecurityException {
        switch (mode) {
            case ECHO:
                echo();
                break;
            case SEND:
                send(args, offset);
                break;
            case POLL:
                poll();
                break;
            default:
                throw new RuntimeException("Illegal mode: " + mode);
        }
    }
    private ActiveAssociation openAssoc()
    throws IOException, GeneralSecurityException {
        Association assoc = aFact.newRequestor(
            newSocket(url.getHost(), url.getPort()));
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
        ActiveAssociation active = openAssoc();
        if (active != null) {
            if (active.getAssociation().getAcceptedTransferSyntaxUID(PCID_ECHO)
                    == null) {
                log.error(messages.getString("noPCEcho"));
            } else {
                active.invoke( aFact.newDimse(PCID_ECHO,
                oFact.newCommand().initCEchoRQ(1)), null);
            }
            active.release(true);
        }
        long dt = System.currentTimeMillis() - t1;
        log.info(
            MessageFormat.format(messages.getString("echoDone"),
            new Object[]{ new Long(dt) }));
    }
    
    public void send(String[] args, int offset)
    throws InterruptedException, IOException, GeneralSecurityException {
        long t1 = System.currentTimeMillis();
        int count = 0;
        ActiveAssociation active = openAssoc();
        if (active != null) {
            for (int k = offset; k < args.length; ++k) {
                if (sendFile(active, new File(args[k]))) {
                    ++count;
                }
            }
            active.release(true);
        }
        long dt = System.currentTimeMillis() - t1;
        log.info(
            MessageFormat.format(messages.getString("sendDone"),
            new Object[]{
                new Integer(count),
                new Long(dt),
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
    private boolean sendFile(ActiveAssociation active, File file)
    throws InterruptedException, IOException {
        Dataset ds = loadDataset(file);
        if (ds == null) {
            return false;
        }
        boolean mpps = ds.contains(Tags.ScheduledStepAttributesSeq);
        boolean ncreate = mpps || ds.contains(Tags.PerformedWorkitemCodeSeq);
        String iuid;
        String cuid;
        int pcid;
        FileMetaInfo fmi = ds.getFileMetaInfo();
        if (fmi == null) {
            if (!ncreate) {
                log.error(
                    MessageFormat.format(messages.getString("noFMI"),
                    new Object[]{ file }));
                return false;
            }
            iuid = null;
            cuid = mpps ? UIDs.ModalityPerformedProcedureStep
                        : UIDs.GeneralPurposePerformedProcedureStepSOPClass;
            pcid = mpps ? PCID_MPPS : PCID_GPPPS;
        } else {
            iuid = fmi.getMediaStorageSOPInstanceUID();
            cuid = fmi.getMediaStorageSOPClassUID();
            if (UIDs.ModalityPerformedProcedureStep.equals(cuid)) {
                mpps = true;
                pcid = PCID_MPPS;
            } else if (UIDs.GeneralPurposePerformedProcedureStepSOPClass.equals(cuid))
            {
                mpps = false;
                pcid = PCID_GPPPS;
            } else {
                log.error(
                    MessageFormat.format(messages.getString("errSOPClass"),
                        new Object[]{ file, uidDict.toString(cuid)
                    }));
                return false;
            }
        }
        if (active.getAssociation().getAcceptedTransferSyntaxUID(pcid) == null)
        {
            log.error(messages.getString(mpps ? "noPCMPPS" : "noPCGPPPS"));
            return false;
        }
        int msgId = active.getAssociation().nextMsgID();
        Command cmd = ncreate
                ? oFact.newCommand().initNCreateRQ(msgId, cuid, iuid)
                : oFact.newCommand().initNSetRQ(msgId,cuid, iuid);
        FutureRSP futureRSP = active.invoke(aFact.newDimse(pcid, cmd, ds));
        Dimse rsp = futureRSP.get();
        int status = rsp.getCommand().getStatus();
        if (status != 0) {
            log.error(
                    MessageFormat.format(messages.getString("errRSP"),
                        new Object[]{ file, Integer.toHexString(status)
                    }));
            return false;
        }
        return true;
    }
        
    private Dataset loadDataset(File file) {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            Dataset retval = oFact.newDataset();
            retval.readFile(in, null, -1);
            log.info(
                MessageFormat.format(messages.getString("readDone"),
                new Object[]{ file }));
            return retval;
        } catch (IOException e) {
            log.error(
                MessageFormat.format(messages.getString("failread"),
                new Object[]{ file, e }));
            return null;
        } finally {
            try { in.close(); } catch (IOException ignore) {};
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
        return aet != null ? aet : "MPPSSND";
    }
    
    private final void initAssocParam(Configuration cfg, DcmURL url, boolean echo) {
        acTimeout = Integer.parseInt(cfg.getProperty("ac-timeout", "5000"));
        dimseTimeout = Integer.parseInt(cfg.getProperty("dimse-timeout", "0"));
        soCloseDelay = Integer.parseInt(cfg.getProperty("so-close-delay", "500"));
        assocRQ.setCalledAET(url.getCalledAET());
        assocRQ.setCallingAET(maskNull(url.getCallingAET()));
        assocRQ.setMaxPDULength(
            Integer.parseInt(cfg.getProperty("max-pdu-len", "16352")));
        assocRQ.setAsyncOpsWindow(aFact.newAsyncOpsWindow(
            Integer.parseInt(cfg.getProperty("max-op-invoked", "0")),1));
        packPDVs = "true".equalsIgnoreCase(
            cfg.getProperty("pack-pdvs", "false"));
        if (echo) {
            assocRQ.addPresContext(
            aFact.newPresContext(PCID_ECHO, UIDs.Verification, DEF_TS));
        } else {
            List tsNames = cfg.tokenize(cfg.getProperty("ts"), new LinkedList());
            String[] tsUIDs = new String[tsNames.size()];
            Iterator it = tsNames.iterator();
            for (int i = 0; i < tsUIDs.length; ++i) {
                tsUIDs[i] = UIDs.forName((String)it.next());
            }
            assocRQ.addPresContext(aFact.newPresContext(
                PCID_MPPS, UIDs.ModalityPerformedProcedureStep, tsUIDs));
            assocRQ.addPresContext(aFact.newPresContext(
                    PCID_GPPPS, UIDs.GeneralPurposePerformedProcedureStepSOPClass, tsUIDs));
        }
    }
    
    private boolean initPollDirSrv(Configuration cfg) {
        String pollDirName = cfg.getProperty("poll-dir", "", "<none>", "");
        if (pollDirName.length() == 0) {
            return false;
        }
        
        pollDir = new File(pollDirName);
        if (!pollDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory - " + pollDirName);
        }
        pollPeriod = 1000L * Integer.parseInt(
        cfg.getProperty("poll-period", "5"));
        pollDirSrv = PollDirSrvFactory.getInstance().newPollDirSrv(this);
        pollDirSrv.setOpenRetryPeriod(1000L * Integer.parseInt(
        cfg.getProperty("poll-retry-open", "60")) * 1000L);
        pollDirSrv.setDeltaLastModified(1000L * Integer.parseInt(
        cfg.getProperty("poll-delta-last-modified", "3")));
        String doneDirName = cfg.getProperty("poll-done-dir", "", "<none>", "");
        if (doneDirName.length() != 0) {
            File doneDir = new File(doneDirName);
            if (!doneDir.isDirectory()) {
                throw new IllegalArgumentException("Not a directory - " + doneDirName);
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
                cfg.getProperty("tls-key-passwd","secret").toCharArray();
            tls.setKey(
                tls.loadKeyStore(
                    MppsSnd.class.getResource(
                        cfg.getProperty("tls-key","certificates/test_sys_1.p12")),
                    keypasswd),
                keypasswd);
            tls.setTrust(tls.loadKeyStore(
                MppsSnd.class.getResource(
                    cfg.getProperty("tls-cacerts", "certificates/mesa_certs.jks")),
                cfg.getProperty("tls-cacerts-passwd", "secret").toCharArray()));
            tls.init();
        } catch (Exception ex) {
           throw new RuntimeException("Could not initalize TLS configuration: ", ex);
        }
    }
    
}
