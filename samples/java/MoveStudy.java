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

import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
/*
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
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

/**
 *  <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      February 19, 2003
 * @version    $Revision: 12031 $ <p>
 */
public class MoveStudy
{

    // Constants -----------------------------------------------------
    private static ResourceBundle messages =
            ResourceBundle.getBundle("MoveStudy", Locale.getDefault());
    private final static int PCID_FIND = 1;
    private final static int PCID_MOVE = 3;
    private final static String STUDY_LABEL = "STUDY";
    private final static String[] TS = {
            UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian
            };

    // Attributes ----------------------------------------------------
    private final static Logger log = Logger.getLogger(MoveStudy.class);
    private final static AssociationFactory af =
            AssociationFactory.getInstance();
    private final static DcmObjectFactory dof =
            DcmObjectFactory.getInstance();

    private DcmURL url = null;
    private int priority = Command.MEDIUM;
    private int acTimeout = 5000;
    private int dimseTimeout = 0;
    private int soCloseDelay = 500;
    private AAssociateRQ assocRQ = af.newAAssociateRQ();
    private boolean packPDVs = false;
    private SSLContextAdapter tls = null;
    private String[] cipherSuites = null;
    private Dataset keys = dof.newDataset();
    private Association assoc = null;
    private ActiveAssociation aassoc = null;
    private String dest;
    private boolean dump = false;
    private final Map dumpParam = new HashMap(5);


    // Constructors --------------------------------------------------
    /**
     *  Constructor for the MoveStudy object
     *
     * @param  cfg  Description of the Parameter
     * @param  url  Description of the Parameter
     */
    public MoveStudy(Configuration cfg, DcmURL url)
    {
        this.url = url;
        this.priority = Integer.parseInt(cfg.getProperty("prior", "0"));
        this.packPDVs = "true".equalsIgnoreCase(
                cfg.getProperty("pack-pdvs", "false"));
        initAssocParam(cfg, url);
        initTLS(cfg);
        initKeys(cfg);
        this.dest = cfg.getProperty("dest");
        this.dump =  "true".equalsIgnoreCase(
                cfg.getProperty("dump", "false"));
        dumpParam.put("maxlen", cfg.getProperty("maxlen", "80"));
        dumpParam.put("vallen", cfg.getProperty("vallen", "64"));
        dumpParam.put("prefix", cfg.getProperty("prefix", ""));

    }


    private static String maskNull(String aet)
    {
        return aet != null ? aet : "MOVESTUDY";
    }


    private final void initAssocParam(Configuration cfg, DcmURL url)
    {
        acTimeout = Integer.parseInt(cfg.getProperty("ac-timeout", "5000"));
        dimseTimeout = Integer.parseInt(cfg.getProperty("dimse-timeout", "0"));
        soCloseDelay = Integer.parseInt(cfg.getProperty("so-close-delay", "500"));
        assocRQ.setCalledAET(url.getCalledAET());
        assocRQ.setCallingAET(maskNull(url.getCallingAET()));
        assocRQ.setMaxPDULength(
                Integer.parseInt(cfg.getProperty("max-pdu-len", "16352")));
        assocRQ.addPresContext(af.newPresContext(PCID_FIND,
                UIDs.StudyRootQueryRetrieveInformationModelFIND, TS));
        assocRQ.addPresContext(af.newPresContext(PCID_MOVE,
                UIDs.StudyRootQueryRetrieveInformationModelMOVE, TS));
    }


    private void initTLS(Configuration cfg)
    {
        try {
            cipherSuites = url.getCipherSuites();
            if (cipherSuites == null) {
                return;
            }
            tls = SSLContextAdapter.getInstance();
            char[] keypasswd = cfg.getProperty("tls-key-passwd", "secret").toCharArray();
            tls.setKey(tls.loadKeyStore(
                    MoveStudy.class.getResource(cfg.getProperty("tls-key", "certificates/test_sys_1.p12")),
                    keypasswd),
                    keypasswd);
            tls.setTrust(tls.loadKeyStore(
                    MoveStudy.class.getResource(cfg.getProperty("tls-cacerts", "certificates/mesa_certs.jks")),
                    cfg.getProperty("tls-cacerts-passwd", "secret").toCharArray()));
            tls.init();
        } catch (Exception ex) {
            throw new RuntimeException("Could not initalize TLS configuration: ", ex);
        }
    }


    private void initKeys(Configuration cfg)
    {
        keys.putCS(Tags.QueryRetrieveLevel, STUDY_LABEL);
        keys.putUS(Tags.NumberOfStudyRelatedSeries);
        keys.putUS(Tags.NumberOfStudyRelatedInstances);
        keys.putUI(Tags.StudyInstanceUID);
        keys.putLO(Tags.PatientID);
        keys.putPN(Tags.PatientName);
        keys.putDA(Tags.StudyDate);
        for (Enumeration it = cfg.keys(); it.hasMoreElements(); ) {
            String key = (String) it.nextElement();
            if (key.startsWith("key.")) {
                try {
                    keys.putXX(Tags.forName(key.substring(4)),
                            cfg.getProperty(key));
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Illegal entry in mvstudy.cfg - "
                             + key + "=" + cfg.getProperty(key));
                }
            }
        }
    }


    // Methods -------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @return                Description of the Return Value
     * @exception  Exception  Description of the Exception
     */
    public List query()
        throws Exception
    {
        if (aassoc == null) {
            throw new IllegalStateException("No Association established");
        }
        Command rqCmd = dof.newCommand();
        rqCmd.initCFindRQ(assoc.nextMsgID(),
                UIDs.StudyRootQueryRetrieveInformationModelFIND, priority);
        Dimse findRq = af.newDimse(PCID_FIND, rqCmd, keys);
        if (dump) {
            StringWriter w = new StringWriter();
            w.write("C-FIND RQ Identifier:\n");
            keys.dumpDataset(w, dumpParam);
            log.info(w.toString());
        }
        FutureRSP future = aassoc.invoke(findRq);
        Dimse findRsp = future.get();
        return future.listPending();
    }


    /**
     *  Description of the Method
     *
     * @param  findRspList    Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public void move(List findRspList)
        throws Exception
    {
        if (aassoc == null) {
            throw new IllegalStateException("No Association established");
        }
        final int numStudies = findRspList.size();
        int numSeries = 0;
        int numInst = 0;
        int failed = 0;
        int warning = 0;
        int success = 0;
        for (int i = 0; i < numStudies; ++i) {
            Dimse findRsp = (Dimse) findRspList.get(i);
            Dataset findRspDs = findRsp.getDataset();
            if (dump) {
                StringWriter w = new StringWriter();
                w.write("C-FIND RSP Identifier:\n");
                findRspDs.dumpDataset(w, dumpParam);
                log.info(w.toString());
            }
            if (numSeries >= 0) {
                numSeries += findRspDs.getInt(
                        Tags.NumberOfStudyRelatedSeries, Integer.MIN_VALUE);
            }
            if (numInst >= 0) {
                numInst += findRspDs.getInt(
                        Tags.NumberOfStudyRelatedInstances, Integer.MIN_VALUE);
            }
            if (dest != null) {
                switch (doMove(findRspDs)) {
                    case 0x0000:
                        ++success;
                        break;
                    case 0xB000:
                        ++warning;
                        break;
                    default:
                        ++failed;
                        break;
                }
            }
        }
        log.info("Found " + numStudies + " Studies with "
                 + (numSeries >= 0 ? String.valueOf(numSeries) : "?")
                 + " Series with "
                 + (numInst >= 0 ? String.valueOf(numInst) : "?")
                 + " Instances");
        if (dest != null) {
            log.info("Successfully moved " + success + " Studies");
            if (warning > 0) {
                log.error("One or more Failures during move of "
                         + warning + " Studies");
            }
            if (failed > 0) {
                log.error("Failed to move " + failed + " Studies");
            }
        }
    }


    private int doMove(Dataset findRspDs)
        throws Exception
    {
        String suid = findRspDs.getString(Tags.StudyInstanceUID);
        String patName = findRspDs.getString(Tags.PatientName);
        String patID = findRspDs.getString(Tags.PatientID);
        String studyDate = findRspDs.getString(Tags.StudyDate);
        String prompt = "Study[" + suid + "] from " + studyDate
                 + " for Patient[" + patID + "]: " + patName;
        log.info("Moving " + prompt);
        Command rqCmd = dof.newCommand();
        rqCmd.initCMoveRQ(assoc.nextMsgID(),
                UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                priority,
                dest);
        Dataset rqDs = dof.newDataset();
        rqDs.putCS(Tags.QueryRetrieveLevel, STUDY_LABEL);
        rqDs.putUI(Tags.StudyInstanceUID, suid);
        Dimse moveRq = af.newDimse(PCID_MOVE, rqCmd, rqDs);
        FutureRSP future = aassoc.invoke(moveRq);
        Dimse moveRsp = future.get();
        Command rspCmd = moveRsp.getCommand();
        int status = rspCmd.getStatus();
        switch (status) {
            case 0x0000:
                log.info("Moved " + prompt);
                break;
            case 0xB000:
                log.error("One or more failures during move of " + prompt);
                break;
            default:
                log.error("Failed to move " + prompt
                         + "\n\terror tstatus: " + Integer.toHexString(status));
                break;
        }
        return status;
    }


    /**
     *  Description of the Method
     *
     * @return                               Description of the Return Value
     * @exception  IOException               Description of the Exception
     * @exception  GeneralSecurityException  Description of the Exception
     */
    public boolean open()
        throws IOException, GeneralSecurityException
    {
        if (aassoc != null) {
            throw new IllegalStateException("Association already established");
        }
        assoc = af.newRequestor(
                newSocket(url.getHost(), url.getPort()));
        assoc.setAcTimeout(acTimeout);
        assoc.setDimseTimeout(dimseTimeout);
        assoc.setSoCloseDelay(soCloseDelay);
        assoc.setPackPDVs(packPDVs);

        PDU assocAC = assoc.connect(assocRQ);
        if (!(assocAC instanceof AAssociateAC)) {
            assoc = null;
            return false;
        }
        aassoc = af.newActiveAssociation(assoc, null);
        aassoc.start();
        return true;
    }


    /**
     *  Description of the Method
     *
     * @exception  InterruptedException  Description of the Exception
     * @exception  IOException           Description of the Exception
     */
    public void close()
        throws InterruptedException, IOException
    {
        if (assoc != null) {
            try {
                aassoc.release(false);
            } finally {
                assoc = null;
                aassoc = null;
            }
        }
    }


    private Socket newSocket(String host, int port)
        throws IOException, GeneralSecurityException
    {
        if (cipherSuites != null) {
            return tls.getSocketFactory(cipherSuites).createSocket(host, port);
        } else {
            return new Socket(host, port);
        }
    }


    // Main ----------------------------------------------------------
    private final static LongOpt[] LONG_OPTS = new LongOpt[]{
            new LongOpt("ac-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("dimse-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("so-close-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("prior-high", LongOpt.NO_ARGUMENT, null, 'P'),
            new LongOpt("prior-low", LongOpt.NO_ARGUMENT, null, 'p'),
            new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("pack-pdvs", LongOpt.NO_ARGUMENT, null, 'k'),
            new LongOpt("key", LongOpt.REQUIRED_ARGUMENT, null, 'y'),
            new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("dump", LongOpt.NO_ARGUMENT, null, 'd'),
            new LongOpt("maxlen", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("vallen", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("prefix", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
            };


    /**
     *  Description of the Method
     *
     * @param  args           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void main(String args[])
        throws Exception
    {
        Getopt g = new Getopt("mvstudy", args, "", LONG_OPTS);

        Configuration cfg = new Configuration(
                MoveStudy.class.getResource("mvstudy.cfg"));

        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 2:
                    cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
                    break;
                case 'd':
                    cfg.put("dump", "true");
                    break;
                case 'P':
                    cfg.put("prior", "1");
                    break;
                case 'p':
                    cfg.put("prior", "2");
                    break;
                case 'k':
                    cfg.put("pack-pdvs", "true");
                    break;
                case 'y':
                    putKey(cfg, g.getOptarg());
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
        if (argc > 1) {
            exit(messages.getString("tomany"), true);
        }
        //      listConfig(cfg);
        try {
            MoveStudy inst = new MoveStudy(cfg, new DcmURL(args[optind]));
            if (inst.open()) {
                inst.move(inst.query());
                inst.close();
            }
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
        }
    }


    private static void putKey(Configuration cfg, String s)
    {
        int pos = s.indexOf(':');
        if (pos == -1) {
            cfg.put("key." + s, "");
        } else {
            cfg.put("key." + s.substring(0, pos), s.substring(pos + 1));
        }
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

}

