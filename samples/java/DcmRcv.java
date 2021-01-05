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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.AAbort;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.BufferedOutputStream;
import org.dcm4che.util.DcmProtocol;
import org.dcm4che.util.SSLContextAdapter;

/**
 * <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created    May, 2002
 * @version    $Revision: 12031 $ $Date: 2009-08-18 17:27:22 +0200 (Di, 18 Aug 2009) $
 */
public class DcmRcv extends DcmServiceBase
{
    // Constants -----------------------------------------------------
    final static Logger log = Logger.getLogger(DcmRcv.class);
    final static boolean DEBUG = log.isDebugEnabled();

    // Attributes ----------------------------------------------------
    private static ResourceBundle messages = ResourceBundle.getBundle(
            "DcmRcv", Locale.getDefault());

    private final static ServerFactory srvFact =
            ServerFactory.getInstance();
    private final static AssociationFactory fact =
            AssociationFactory.getInstance();
    private final static DcmParserFactory pFact =
            DcmParserFactory.getInstance();
    private final static DcmObjectFactory oFact =
            DcmObjectFactory.getInstance();

    private SSLContextAdapter tls = null;
    private DcmProtocol protocol = DcmProtocol.DICOM;

    private Dataset overwrite = oFact.newDataset();
    private AcceptorPolicy policy = fact.newAcceptorPolicy();
    private DcmServiceRegistry services = fact.newDcmServiceRegistry();
    private DcmHandler handler = srvFact.newDcmHandler(policy, services);
    private Server server = srvFact.newServer(handler);
    private int bufferSize = 8192;
    private File dir = null;
    private boolean devnull = false;
    private DcmRcvFSU fsu = null;
    private long rspDelay = 0L;
    private int rspStatus = 0;

    private ThreadLocal buffer = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new byte[bufferSize];
        }
    };
    private boolean abort;
    
    // Static --------------------------------------------------------
    private final static LongOpt[] LONG_OPTS = new LongOpt[]{
            new LongOpt("max-clients", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("rq-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("dimse-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("so-close-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("called-aets", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("calling-aets", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("max-op-invoked", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("rsp-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("rsp-status", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("set", LongOpt.REQUIRED_ARGUMENT, null, 's'),
            new LongOpt("fs-id", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("fs-uid", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("fs-file-id", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("fs-lazy-update", LongOpt.NO_ARGUMENT, null, 3),
            new LongOpt("pack-pdvs", LongOpt.NO_ARGUMENT, null, 3),
            new LongOpt("buf-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("dicom-tls", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("dicom-tls.nodes", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("dicom-tls.3des", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("abort", LongOpt.NO_ARGUMENT, null, 3),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
            };
    

    private static void set(Configuration cfg, String s)
    {
        int pos = s.indexOf(':');
        if (pos == -1) {
            cfg.put("set." + s, "");
        } else {
            cfg.put("set." + s.substring(0, pos), s.substring(pos + 1));
        }
    }


    /**
     *  Description of the Method
     *
     * @param  args           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void main(String args[])
        throws Exception
    {
        Getopt g = new Getopt("dcmrcv", args, "", LONG_OPTS);

        Configuration cfg = new Configuration(
                DcmRcv.class.getResource("dcmrcv.cfg"));
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 2:
                    cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
                    break;
                case 3:
                    cfg.put(LONG_OPTS[g.getLongind()].getName(), "true");
                    break;
                case 4:
                    cfg.put("protocol", LONG_OPTS[g.getLongind()].getName());
                    break;
                case 's':
                    set(cfg, g.getOptarg());
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
        switch (args.length - optind) {
            case 0:
                exit(messages.getString("missing"), true);
            case 1:
                cfg.put("port", args[optind]);
                break;
            default:
                exit(messages.getString("many"), true);
        }
        listConfig(cfg);
        try {
            new DcmRcv(cfg).start();
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
        }
    }

    // Constructors --------------------------------------------------
    DcmRcv(Configuration cfg)
    {
        rspDelay = Integer.parseInt(cfg.getProperty("rsp-delay", "0")) * 1000L;
        String decOrHex = cfg.getProperty("rsp-status", "0");        
        rspStatus = decOrHex.endsWith("H") 
        	? Integer.parseInt(decOrHex.substring(0, decOrHex.length()-1), 16)
        	: Integer.parseInt(decOrHex);
        bufferSize = Integer.parseInt(
                cfg.getProperty("buf-len", "2048")) & 0xfffffffe;
        abort = "true".equalsIgnoreCase(cfg.getProperty("abort"));
        initServer(cfg);
        initDest(cfg);
        initTLS(cfg);
        initPolicy(cfg);
        initOverwrite(cfg);
    }

    // Public --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @exception  IOException               Description of the Exception
     */
    public void start() throws IOException
    {
        if (fsu != null) {
            new Thread(fsu).start();
        }
        server.start();
        /*//test code for stopping and starting server again 
        System.out.println("STOPPING");
        server.stop();
        System.out.println("STARTING");
        try{Thread.sleep(2500);}catch(Exception e){}
        server.start();
        new Socket(InetAddress.getLocalHost(), 4000);*/
    }

    // DcmServiceBase overrides --------------------------------------
    /**
     *  Description of the Method
     *
     * @param  assoc            Description of the Parameter
     * @param  rq               Description of the Parameter
     * @param  rspCmd           Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd)
        throws IOException
    {
        InputStream in = rq.getDataAsStream();
        try {
            if (dir != null) {
                Command rqCmd = rq.getCommand();
                FileMetaInfo fmi = objFact.newFileMetaInfo(
                        rqCmd.getAffectedSOPClassUID(),
                        rqCmd.getAffectedSOPInstanceUID(),
                        rq.getTransferSyntaxUID());
                if (fsu == null) {
                    storeToDir(in, fmi);
                } else {
                    storeToFileset(in, fmi);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            in.close();
        }
        if (rspDelay > 0L) {
            try {
                Thread.sleep(rspDelay);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        rspCmd.putUS(Tags.Status, rspStatus);
        if (abort) {
            AssociationFactory f = AssociationFactory.getInstance();
            assoc.getAssociation().abort(f.newAAbort(AAbort.SERVICE_USER, AAbort.REASON_NOT_SPECIFIED));
            throw new IOException("Abort Association");
        }
    }


    private void storeToDir(InputStream in, FileMetaInfo fmi)
        throws IOException
    {
        File file = devnull ? dir 
                : new File(dir, fmi.getMediaStorageSOPInstanceUID());
        log.info("M-WRITE " + file);
        BufferedOutputStream out = new BufferedOutputStream(
        		new FileOutputStream(file), (byte[]) buffer.get()); 
        try {
            fmi.write(out);
            out.copyFrom(in);
        } finally {
            try {
                out.close();
            } catch (IOException ignore) {}
        }
    }


    private void storeToFileset(InputStream in, FileMetaInfo fmi)
        throws IOException
    {
        Dataset ds = oFact.newDataset();
        DcmParser parser = pFact.newDcmParser(in);
        parser.setDcmHandler(ds.getDcmHandler());
        DcmDecodeParam decParam =
                DcmDecodeParam.valueOf(fmi.getTransferSyntaxUID());
        DcmEncodeParam encParam = (DcmEncodeParam) decParam;
        parser.parseDataset(decParam, Tags.PixelData);
        doOverwrite(ds);
        File file = fsu.toFile(ds);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Could not create " + parent);
            }
            log.info("M-WRITE " + parent);
        }
        BufferedOutputStream out = new BufferedOutputStream(
        		new FileOutputStream(file), (byte[]) buffer.get());
        try {
            ds.setFileMetaInfo(fmi);
            ds.writeFile(out, (DcmEncodeParam) decParam);
            if (parser.getReadTag() != Tags.PixelData) {
                return;
            }
            int len = parser.getReadLength();
            ds.writeHeader(out, encParam,
                    parser.getReadTag(),
                    parser.getReadVR(),
                    len);
            if (len == -1) {
                parser.parseHeader();
                while (parser.getReadTag() == Tags.Item) {
                    len = parser.getReadLength();
                    ds.writeHeader(out, encParam, Tags.Item, VRs.NONE, len);
                    out.copyFrom(in, len);
                    parser.parseHeader();
                }
                ds.writeHeader(
                        out,
                        encParam,
                        Tags.SeqDelimitationItem,
                        VRs.NONE,
                        0);                
            } else {
                out.copyFrom(in, len);
            }
            parser.parseDataset(decParam, -1);
            ds.subSet(Tags.PixelData, -1).writeDataset(out, encParam);
        } finally {
            try {
                out.close();
            } catch (IOException ignore) {}
        }
        fsu.schedule(file, ds);
    }

    private void doOverwrite(Dataset ds)
    {
        for (Iterator it = overwrite.iterator(); it.hasNext(); ) {
            DcmElement el = (DcmElement) it.next();
            ds.putXX(el.tag(), el.vr(), el.getByteBuffer());
        }
    }

    private static void listConfig(Configuration cfg)
    {
        StringBuffer msg = new StringBuffer();
        msg.append(messages.getString("cfg"));
        msg.append("\n\tprotocol=").append(cfg.getProperty("protocol"));
        for (int i = 0, n = LONG_OPTS.length - 2; i < n; ++i) {
            String opt = LONG_OPTS[i].getName();
            String val = cfg.getProperty(opt);
            if (val != null) {
                msg.append("\n\t").append(opt).append("=").append(val);
            }
        }
        log.info(msg.toString());
    }


    private static void exit(String prompt, boolean error)
    {
        if (prompt != null) {
            System.err.println(prompt);
        }
        if (error) {
            System.err.println(messages.getString("try"));
        }
        System.exit(1);
    }


    private final void initDest(Configuration cfg)
    {
        String dest = cfg.getProperty("dest", "", "<none>", "");
        if (dest.length() == 0) {
            return;
        }

        this.dir = new File(dest);
        if ("/dev/null".equals(dest)) {
            this.devnull = true;
            return;
        }
        if ("DICOMDIR".equals(dir.getName())) {
            this.fsu = new DcmRcvFSU(dir, cfg);
            handler.addAssociationListener(fsu);
            dir = dir.getParentFile();
        }
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info(MessageFormat.format(messages.getString("mkdir"),
                        new Object[]{dir}));
            } else {
                exit(MessageFormat.format(messages.getString("failmkdir"),
                        new Object[]{dest}), true);
            }
        } else {
            if (!dir.isDirectory()) {
                exit(MessageFormat.format(messages.getString("errdir"),
                        new Object[]{dest}), true);
            }
        }
    }


    private void initServer(Configuration cfg)
    {
        server.setPort(
                Integer.parseInt(cfg.getProperty("port")));
        server.setMaxClients(
                Integer.parseInt(cfg.getProperty("max-clients", "10")));
        handler.setRqTimeout(
                Integer.parseInt(cfg.getProperty("rq-timeout", "5000")));
        handler.setDimseTimeout(
                Integer.parseInt(cfg.getProperty("dimse-timeout", "0")));
        handler.setSoCloseDelay(
                Integer.parseInt(cfg.getProperty("so-close-delay", "500")));
        handler.setPackPDVs(
                "true".equalsIgnoreCase(cfg.getProperty("pack-pdvs", "false")));
    }


    private void initPolicy(Configuration cfg)
    {
        policy.setCalledAETs(cfg.tokenize(
                cfg.getProperty("called-aets", null, "<any>", null)));
        policy.setCallingAETs(cfg.tokenize(
                cfg.getProperty("calling-aets", null, "<any>", null)));
        policy.setMaxPDULength(
                Integer.parseInt(cfg.getProperty("max-pdu-len", "16352")));
        policy.setAsyncOpsWindow(
                Integer.parseInt(cfg.getProperty("max-op-invoked", "0")), 1);
        for (Enumeration it = cfg.keys(); it.hasMoreElements(); ) {
            String key = (String) it.nextElement();
            if (key.startsWith("pc.")) {
                initPresContext(key.substring(3),
                        cfg.tokenize(cfg.getProperty(key)));
            }
            if (key.startsWith("role.")) {
                initRole(key.substring(5),
                        cfg.tokenize(cfg.getProperty(key)));
            }
        }
    }


 	private void initRole(String asName, String[] roles) {
        String as = UIDs.forName(asName);
		policy.putRoleSelection(as, contains(roles, "scu"), contains(roles, "scp"));
	}


	private boolean contains(String[] roles, String key) {
		for (int i = 0; i < roles.length; i++) {
			if (key.equalsIgnoreCase(roles[i]))
				return true;
		}
		return false;
	}

	private void initPresContext(String asName, String[] tsNames)
    {
        String as = UIDs.forName(asName);
        String[] tsUIDs = new String[tsNames.length];
        for (int i = 0; i < tsUIDs.length; ++i) {
            tsUIDs[i] = UIDs.forName(tsNames[i]);
        }
        policy.putPresContext(as, tsUIDs);
        services.bind(as, this);
    }


    private void initOverwrite(Configuration cfg)
    {
        for (Enumeration it = cfg.keys(); it.hasMoreElements(); ) {
            String key = (String) it.nextElement();
            if (key.startsWith("set.")) {
                try {
                    overwrite.putXX(Tags.forName(key.substring(4)),
                            cfg.getProperty(key));
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Illegal entry in dcmsnd.cfg - "
                             + key + "=" + cfg.getProperty(key));
                }
            }
        }
    }


    private void initTLS(Configuration cfg)
    {
        try {
            this.protocol = DcmProtocol.valueOf(
                    cfg.getProperty("protocol", "dicom"));
            if (!protocol.isTLS()) {
                return;
            }

            tls = SSLContextAdapter.getInstance();
            char[] keypasswd = cfg.getProperty("tls-key-passwd", "secret").toCharArray();
            tls.setKey(
                    tls.loadKeyStore(
                    DcmRcv.class.getResource(cfg.getProperty("tls-key", "certificates/test_sys_1.p12")),
                    keypasswd),
                    keypasswd);
            tls.setTrust(tls.loadKeyStore(
                    DcmRcv.class.getResource(cfg.getProperty("tls-cacerts", "certificates/mesa_certs.jks")),
                    cfg.getProperty("tls-cacerts-passwd", "secret").toCharArray()));
            this.server.setServerSocketFactory(
                    tls.getServerSocketFactory(protocol.getCipherSuites()));
        } catch (Exception ex) {
            throw new RuntimeException("Could not initalize TLS configuration: ", ex);
        }
    }

    protected Dataset doNCreate(
        ActiveAssociation assoc,
        Dimse rq,
        Command rspCmd)
        throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        String cuid = rqCmd.getAffectedSOPClassUID();
        String iuid = rspCmd.getAffectedSOPInstanceUID();
        Dataset ds = rq.getDataset(); // read out dataset
        if (DEBUG)
            log.debug("Dataset:\n" + ds);
        if (dir != null && dir.isDirectory()) {            
            File f = new File(dir, iuid);
            if (f.exists()) {
                throw new DcmServiceException(Status.DuplicateSOPInstance);
            }
            ds.putUI(Tags.SOPInstanceUID, iuid);
            ds.putUI(Tags.SOPClassUID, cuid);
            ds.setFileMetaInfo(oFact.newFileMetaInfo(cuid, iuid, UIDs.ExplicitVRLittleEndian));
            log.info("M-WRITE " + f);
            writeFile(f, ds);
        }
        return ds;
    }

    private void writeFile(File f, Dataset ds) throws DcmServiceException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(f));
            ds.writeFile(out, null);
        } catch (IOException e) {
            throw new DcmServiceException(Status.ProcessingFailure);
        } finally {
            try { if (out != null) out.close(); } catch (IOException ignore) {};
        }        
    }


    protected Dataset doNDelete(
        ActiveAssociation assoc,
        Dimse rq,
        Command rspCmd)
        throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset (should be NULL!)
        Command rqCmd = rq.getCommand();
        String iuid = rqCmd.getRequestedSOPInstanceUID();
        if (dir != null && dir.isDirectory()) {            
            File f = new File(dir, iuid);
            if (!f.exists())
                throw new DcmServiceException(Status.NoSuchObjectInstance);

            log.info("M-DELETE " + f);
            if (!f.delete()) 
                throw new DcmServiceException(Status.ProcessingFailure);
        }
        return null;
    }

    protected Dataset doNGet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset (should be NULL!)
        Dataset ds = oFact.newDataset();
        Command rqCmd = rq.getCommand();
        String iuid = rqCmd.getRequestedSOPInstanceUID();
        if (dir != null && dir.isDirectory()) {            
            File f = new File(dir, iuid);
            if (!f.exists())
                throw new DcmServiceException(Status.NoSuchObjectInstance);

            log.info("M-READ " + f);
            readFile(f, ds);
        }
        int[] tags = rqCmd.getTags(Tags.AttributeIdentifierList);
        if (tags != null)
            ds = ds.subSet(tags);
        if (DEBUG)
            log.debug("Dataset in N-GET RSP:\n" + ds);
        return ds;
    }

    private void readFile(File f, Dataset ds) throws DcmServiceException {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            ds.readFile(in, FileFormat.DICOM_FILE, -1);
        } catch (IOException e) {
            throw new DcmServiceException(Status.ProcessingFailure);
        } finally {
            try { if (in != null) in.close(); } catch (IOException ignore) {};
        }        
    }
    
    protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
    throws IOException, DcmServiceException {
        Dataset modify = rq.getDataset(); // read out dataset
        if (DEBUG)
            log.debug("Dataset:\n" + modify);
        Command rqCmd = rq.getCommand();
        String iuid = rqCmd.getRequestedSOPInstanceUID();
        if (dir != null && dir.isDirectory()) {            
            File f = new File(dir, iuid);
            if (!f.exists())
                throw new DcmServiceException(Status.NoSuchObjectInstance);

            Dataset ds = oFact.newDataset();
            log.info("M-UPDATE " + f);
            readFile(f, ds);
            ds.putAll(modify);
            writeFile(f, ds);
        }
        return modify;
    }

    protected Dataset doNAction(
            ActiveAssociation assoc,
            Dimse rq,
            Command rspCmd)
    throws IOException, DcmServiceException {
        Dataset ds = rq.getDataset(); // read out dataset
        if (DEBUG)
            log.debug("Dataset:\n" + ds);
        Command rqCmd = rq.getCommand();
        String iuid = rqCmd.getRequestedSOPInstanceUID();
        if (dir != null && dir.isDirectory()) {            
            File f = new File(dir, iuid);
            if (!f.exists())
                throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        return null;
    }

    protected Dataset doNEventReport(
        ActiveAssociation assoc,
        Dimse rq,
        Command rspCmd)
        throws IOException, DcmServiceException {
        Dataset ds = rq.getDataset(); // read out dataset
        if (DEBUG)
            log.debug("Dataset:\n" + ds);
        Command rqCmd = rq.getCommand();
        String iuid = rqCmd.getRequestedSOPInstanceUID();
        if (dir != null && dir.isDirectory()) {            
            File f = new File(dir, iuid);
            if (!f.exists())
                throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        return ds;
    }
}

