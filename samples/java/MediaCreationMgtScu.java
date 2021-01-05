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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4che.util.DcmURL;
import org.dcm4che.util.SSLContextAdapter;

/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 12031 $ $Date: 2009-08-18 17:27:22 +0200 (Di, 18 Aug 2009) $
 * @since 18.06.2004
 *
 */
public class MediaCreationMgtScu {

    private static final String FALSE = "false";

    private static final String TRUE = "true";

    private static final String[] DEF_TS = { UIDs.ImplicitVRLittleEndian};

    private static final int PCID_ECHO = 1;

    private static final int PCID_MCM = 3;

    private static final int ECHO = 0;

    private static final int CREATE = 1;

    private static final int SCHEDULE = 2;

    private static final int CANCEL = 4;

    private static final int GET = 8;

    private static final int INITIATE_MEDIA_CREATION = 1;

    private static final int CANCEL_MEDIA_CREATION = 2;

    private static final Logger log = Logger.getLogger("MediaCreationMgtScu");

    private static final ResourceBundle messages = ResourceBundle.getBundle(
            "MediaCreationMgtScu", Locale.getDefault());

    private static final LongOpt[] LONG_OPTS = new LongOpt[] {
            new LongOpt("profile", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("create", LongOpt.REQUIRED_ARGUMENT, null, 'C'),
            new LongOpt("action", LongOpt.REQUIRED_ARGUMENT, null, 'A'),
            new LongOpt("get", LongOpt.REQUIRED_ARGUMENT, null, 'G'),
            new LongOpt("ac-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("dimse-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("so-close-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("pack-pdvs", LongOpt.NO_ARGUMENT, null, 'k'),
            new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null,
                    2), new LongOpt("ts", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),};

    static final AssociationFactory aFact = AssociationFactory.getInstance();

    static final DcmObjectFactory oFact = DcmObjectFactory.getInstance();

    private int cmd = ECHO;

    private DcmURL url = null;

    private Dataset createAttrs = oFact.newDataset();

    private Dataset actionAttrs = oFact.newDataset();

    private int[] getAttrs;

    private int acTimeout = 5000;

    private int dimseTimeout = 0;

    private int soCloseDelay = 500;

    private AAssociateRQ assocRQ = aFact.newAAssociateRQ();

    private ActiveAssociation assoc = null;

    private boolean packPDVs = false;

    private SSLContextAdapter tls = null;

    private String[] cipherSuites = null;

    private ActiveAssociation activeAssociation = null;

    private String profile;

    public static void main(String args[]) throws Exception {
        Getopt g = new Getopt("mcmscu", args, "caxgu:hv", LONG_OPTS);

        Configuration cfg = new Configuration(MediaCreationMgtScu.class
                .getResource("mcmscu.cfg"));

        int cmd = ECHO;
        String iuid = null;
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'c':
            case 'a':
            case 'x':
            case 'g':
                cmd |= c == 'c' ? CREATE : c == 'a' ? SCHEDULE
                        : c == 'x' ? CANCEL : GET;
                break;
            case 'u':
                iuid = g.getOptarg();
                break;
            case 2:
                cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
                break;
            case 'k':
                cfg.put("pack-pdvs", TRUE);
                break;
            case 'C':
                set(cfg, g.getOptarg(), "create.");
                break;
            case 'A':
                set(cfg, g.getOptarg(), "action.");
                break;
            case 'G':
                set(cfg, g.getOptarg(), "get.");
                break;
            case 'v':
                exit(messages.getString("version"), false);
                break;
            case 'h':
                exit(messages.getString("usage"), false);
                break;
            case '?':
                exit(null, true);
                break;
            }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc == 0)
            exit(messages.getString("missing-url"), true);        
        if (iuid == null && cmd != ECHO && (cmd & CREATE) == 0)
            exit(messages.getString("missing-iuid"), true);
        
        //      listConfig(cfg);
        try {
            MediaCreationMgtScu scu = new MediaCreationMgtScu(cfg, new DcmURL(
                    args[optind]));
            Dataset createAttrs = null;
            if ((cmd & CREATE) != 0)
                    createAttrs = scu.makeCreateAttrs(args, optind + 1);
            scu.openAssoc();
            try {
                if (cmd == ECHO)
                    scu.echo();
                else {
                    if (createAttrs != null)
                        iuid = scu.create(iuid, createAttrs);
                    if (iuid != null) {
                        if ((cmd & SCHEDULE) != 0) scu.initiate(iuid);
                        if ((cmd & CANCEL) != 0) scu.cancel(iuid);
                        if ((cmd & GET) != 0) scu.get(iuid);
                    }
                }
            } finally {
                scu.releaseAssoc();
            }
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
        }
    }

    public Dataset makeCreateAttrs(String[] args, int off) {
        Dataset ds = oFact.newDataset();
        ds.putAll(createAttrs);
        DcmElement refSOPSeq = ds.putSQ(Tags.RefSOPSeq);
        for (int i = off; i < args.length; ++i) {
            addRefSOPItem(refSOPSeq, new File(args[i]));
        }
        return ds;
    }

    private void addRefSOPItem(DcmElement refSOPSeq, File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++)
                addRefSOPItem(refSOPSeq, files[i]);
            return;
        }
        InputStream in = null;
        try {
            log.info("M-READ " + file);
            in = new BufferedInputStream(new FileInputStream(file));
            Dataset ds = oFact.newDataset();
            ds.readFile(in, null, Tags.PixelData);
            final String iuid = ds.getString(Tags.SOPInstanceUID);
            final String cuid = ds.getString(Tags.SOPClassUID);
            if (iuid != null && cuid != null) {
                Dataset item = refSOPSeq.addNewItem();
                item.putUI(Tags.RefSOPInstanceUID, iuid);
                item.putUI(Tags.RefSOPClassUID, cuid);
                if (profile != null)
                        item.putLO(Tags.RequestedMediaApplicationProfile,
                                profile);
            } else {
                log.warn("Missing CUID and/or IUID in DICOM object read from "
                        + file);
            }
        } catch (IOException e) {
            log.warn("M-READ " + file + " failed:", e);
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    private static void set(Configuration cfg, String s, String prefix) {
        int pos = s.indexOf(':');
        if (pos == -1) {
            cfg.put(prefix + s, "");
        } else {
            cfg.put(prefix + s.substring(0, pos), s.substring(pos + 1));
        }
    }

    private static void exit(String prompt, boolean error) {
        if (prompt != null) System.err.println(prompt);
        if (error) System.err.println(messages.getString("try"));
        System.exit(1);
    }

    public MediaCreationMgtScu(Configuration cfg, DcmURL url) {
        this.url = url;
        packPDVs = TRUE.equalsIgnoreCase(cfg.getProperty("pack-pdvs", FALSE));
        acTimeout = Integer.parseInt(cfg.getProperty("ac-timeout", "5000"));
        dimseTimeout = Integer.parseInt(cfg.getProperty("dimse-timeout", "0"));
        soCloseDelay = Integer.parseInt(cfg
                .getProperty("so-close-delay", "500"));
        initAssocRQ(cfg);
        initTLS(cfg);
        initAttrs(cfg);
        profile = cfg.getProperty("profile");
    }

    private void initAssocRQ(Configuration cfg) {
        assocRQ.setCalledAET(url.getCalledAET());
        String calling = url.getCallingAET();
        if (calling == null) calling = "MCMSCU";
        assocRQ.setCallingAET(calling);
        assocRQ.setMaxPDULength(Integer.parseInt(cfg.getProperty("max-pdu-len",
                "16352")));
        packPDVs = TRUE.equalsIgnoreCase(cfg.getProperty("pack-pdvs", FALSE));
        assocRQ.addPresContext(aFact.newPresContext(PCID_ECHO,
                UIDs.Verification, DEF_TS));
        assocRQ.addPresContext(aFact.newPresContext(PCID_MCM,
                UIDs.MediaCreationManagementSOPClass,
                getTransferSyntaxUIDs(cfg)));
    }

    private String[] getTransferSyntaxUIDs(Configuration cfg) {
        List tsNames = cfg.tokenize(cfg.getProperty("ts"), new LinkedList());
        String[] tsUIDs = new String[tsNames.size()];
        for (int i = 0; i < tsUIDs.length; ++i)
            tsUIDs[i] = UIDs.forName((String) tsNames.get(i));
        return tsUIDs;
    }

    private void initTLS(Configuration cfg) {
        try {
            cipherSuites = url.getCipherSuites();
            if (cipherSuites == null) { return; }
            tls = SSLContextAdapter.getInstance();
            char[] keypasswd = cfg.getProperty("tls-key-passwd", "secret")
                    .toCharArray();
            tls.setKey(tls.loadKeyStore(DcmSnd.class.getResource(cfg
                    .getProperty("tls-key", "certificates/test_sys_1.p12")), keypasswd),
                    keypasswd);
            tls
                    .setTrust(tls.loadKeyStore(DcmSnd.class.getResource(cfg
                            .getProperty("tls-cacerts", "certificates/mesa_certs.jks")), cfg
                            .getProperty("tls-cacerts-passwd", "secret")
                            .toCharArray()));
            tls.init();
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Could not initalize TLS configuration: ", ex);
        }
    }

    private void initAttrs(Configuration cfg) {
        List list = new ArrayList();
        for (Iterator it = cfg.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            try {
                if (key.startsWith("create.")) {
                    createAttrs.putXX(Tags.forName(key.substring("create."
                            .length())), value);
                } else if (key.startsWith("action.")) {
                    actionAttrs.putXX(Tags.forName(key.substring("action."
                            .length())), value);
                } else if (key.startsWith("get.")) {
                    list.add(new Integer(Tags.forName(key.substring("get."
                            .length()))));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Illegal entry in mcmscu.cfg - " + key + "=" + value);
            }
        }
        if (!list.isEmpty()) {
            getAttrs = new int[list.size()];
            for (int i = 0; i < getAttrs.length; i++)
                getAttrs[i] = ((Integer) list.get(i)).intValue();
        }
    }

    private Socket newSocket(String host, int port) throws IOException,
            GeneralSecurityException {
        if (cipherSuites != null) {
            return tls.getSocketFactory(cipherSuites).createSocket(host, port);
        } else {
            return new Socket(host, port);
        }
    }

    public void openAssoc() throws IOException, GeneralSecurityException {
        Association a = aFact.newRequestor(newSocket(url.getHost(), url
                .getPort()));
        a.setAcTimeout(acTimeout);
        a.setDimseTimeout(dimseTimeout);
        a.setSoCloseDelay(soCloseDelay);
        a.setPackPDVs(packPDVs);
        PDU assocAC = a.connect(assocRQ);
        if (!(assocAC instanceof AAssociateAC))
                throw new IOException("Association rejected");
        assoc = aFact.newActiveAssociation(a, null);
        assoc.start();
    }

    public void releaseAssoc() throws InterruptedException, IOException {
        checkAssoc();

        try {
            assoc.release(true);
        } finally {
            assoc = null;
        }
    }

    private void checkAssoc() {
        if (assoc == null)
                throw new IllegalStateException("No open association");
    }

    private boolean checkPC(int pcid, String msgid) {
        if (assoc.getAssociation().getAcceptedTransferSyntaxUID(pcid) != null)
                return true;

        log.error(messages.getString(msgid));
        return false;
    }

    public void echo() throws InterruptedException, IOException,
            GeneralSecurityException {
        checkAssoc();
        if (checkPC(PCID_ECHO, "noPCEcho"))
                assoc.invoke(aFact.newDimse(PCID_ECHO, oFact.newCommand()
                        .initCEchoRQ(1)), null);
    }

    public String create(String iuid, Dataset ds) throws InterruptedException,
            IOException {
        checkAssoc();
        if (!checkPC(PCID_MCM, "noPCMcm")) return null;

        FutureRSP futureRsp = assoc.invoke(aFact.newDimse(PCID_MCM, oFact
                .newCommand().initNCreateRQ(1,
                        UIDs.MediaCreationManagementSOPClass, iuid), ds));
        Dimse rsp = futureRsp.get();
        Command cmdRsp = rsp.getCommand();
        Dataset dataRsp = rsp.getDataset();
        int status = cmdRsp.getStatus();
        switch (status) {
        case Status.AttributeValueOutOfRange:
            log.warn("Warning: Attribute Value Out Of Range: "
                    + cmdRsp.getString(Tags.ErrorComment, "") + dataRsp);
        case Status.Success:
            return iuid != null ? iuid : cmdRsp.getAffectedSOPInstanceUID();
        }
        log.error("Failure Status " + Integer.toHexString(status) + ": "
                + cmdRsp.getString(Tags.ErrorComment, "") + dataRsp);
        return null;
    }

    private void action(int msgid, String iuid, int actionid, Dataset attrs)
            throws InterruptedException, IOException {
        if (!checkPC(PCID_MCM, "noPCMcm")) return;

        FutureRSP futureRsp = assoc.invoke(aFact.newDimse(PCID_MCM, oFact
                .newCommand().initNActionRQ(msgid,
                        UIDs.MediaCreationManagementSOPClass, iuid, actionid),
                attrs));
        Dimse rsp = futureRsp.get();
        Command cmdRsp = rsp.getCommand();
        Dataset dataRsp = rsp.getDataset();
        int status = cmdRsp.getStatus();
        if (status != 0)
                log.error("Failure Status " + Integer.toHexString(status)
                        + ": " + cmdRsp.getString(Tags.ErrorComment, "")
                        + dataRsp);
    }

    public void initiate(String iuid) throws InterruptedException, IOException {
        log.info("Initiate Media Creation Request[iuid:" + iuid + "]\n:"
                + actionAttrs);
        action(3, iuid, INITIATE_MEDIA_CREATION, actionAttrs);
    }

    public void cancel(String iuid) throws InterruptedException, IOException {
        log.info("Canceling Media Creation Request[iuid:" + iuid + "]");
        action(5, iuid, CANCEL_MEDIA_CREATION, null);
    }

    public void get(String iuid) throws InterruptedException, IOException {
        checkAssoc();
        if (!checkPC(PCID_MCM, "noPCMcm")) return;

        FutureRSP futureRsp = assoc.invoke(aFact.newDimse(PCID_MCM, oFact
                .newCommand().initNGetRQ(7,
                        UIDs.MediaCreationManagementSOPClass, iuid, getAttrs)));
        Dimse rsp = futureRsp.get();
        Command cmdRsp = rsp.getCommand();
        Dataset dataRsp = rsp.getDataset();
        int status = cmdRsp.getStatus();
        if (status != 0)
            log.error("Failure Status " + Integer.toHexString(status) + ": "
                    + cmdRsp.getString(Tags.ErrorComment, "")
                    + (dataRsp == null ? "" : ("\n" + dataRsp)));
        else
            log.info("Received Attributes:\n" + dataRsp);
    }
}
