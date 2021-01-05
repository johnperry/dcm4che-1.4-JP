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

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

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
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4che.util.DcmURL;
import org.dcm4che.util.SSLContextAdapter;
import org.dcm4cheri.util.StringUtils;

/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 12031 $ $Date: 2009-08-18 17:27:22 +0200 (Di, 18 Aug 2009) $
 * @since Sep 13, 2005
 */
public class HPQRScu implements DimseListener, AssociationListener{

	private static final Logger log = Logger.getLogger(HPQRScu.class);
	
    private static final int PCID_FIND = 1;
    private static final int PCID_MOVE = 3;
	
    private final static String[] TS = {
        UIDs.ExplicitVRLittleEndian,
        UIDs.ImplicitVRLittleEndian
        };

	private static ResourceBundle messages = 
			ResourceBundle.getBundle("HPQRScu", Locale.getDefault());
	
    private static final LongOpt[] LONG_OPTS = new LongOpt[] {
        new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("ac-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("dimse-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("so-close-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("prior-high", LongOpt.NO_ARGUMENT, null, 'P'),
        new LongOpt("prior-low", LongOpt.NO_ARGUMENT, null, 'p'),
        new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("max-op-invoked", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("pack-pdvs", LongOpt.NO_ARGUMENT, null, 'k'),
        new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null,
                2), new LongOpt("ts", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
        new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),};
	
	
	public static void main(String args[]) throws Exception {
		Getopt g = new Getopt("hpqrscu.jar", args, "D:", LONG_OPTS);
        Configuration cfg = new Configuration(
				HPQRScu.class.getResource("hpqrscu.cfg"));

		int c;
		while ((c = g.getopt()) != -1) {
			switch (c) {
            case 2:
                cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
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
			case 'D':
                addKey(cfg, "D" + g.getOptarg());				
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
    try {
        HPQRScu inst = new HPQRScu(cfg, new DcmURL(args[optind]));
        if (inst.open()) {
            inst.find();			
	        inst.move();			
            inst.close();
        }
    } catch (IllegalArgumentException e) {
        exit(e.getMessage(), true);
    }
	}

	private static void addKey(Configuration cfg, String s) {
        int pos = s.indexOf('=');
        if (pos == -1) {
            cfg.put(s, "");
        } else {
            cfg.put(s.substring(0, pos), s.substring(pos + 1));
        }
	}

    private static void exit(String prompt, boolean error) {
        if (prompt != null) System.err.println(prompt);
        if (error) System.err.println(messages.getString("try"));
        System.exit(1);
    }

	private DcmURL url = null;
	private int priority = Command.MEDIUM;
	private int acTimeout = 5000;
	private int dimseTimeout = 0;
	private int soCloseDelay = 500;
	private AAssociateRQ assocRQ = AssociationFactory.getInstance().newAAssociateRQ();
	private boolean packPDVs = false;
	private SSLContextAdapter tls = null;
	private String[] cipherSuites = null;
	private Dataset keys = DcmObjectFactory.getInstance().newDataset();
	private ActiveAssociation aassoc = null;
	private String dest;

	private List pendingFindRspData = new ArrayList();
	private Dimse finalFindRsp;
	private IOException findException;

    public HPQRScu(Configuration cfg, DcmURL url)
    {
        this.url = url;
        this.priority = Integer.parseInt(cfg.getProperty("prior", "0"));
        this.packPDVs = "true".equalsIgnoreCase(
                cfg.getProperty("pack-pdvs", "false"));
        initAssocParam(cfg, url);
        initTLS(cfg);
        initKeys(cfg);
        this.dest = cfg.getProperty("dest");
    }
	
	private void initKeys(Configuration cfg) {
		for (Iterator it = cfg.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			final String key = (String) e.getKey();
			if (key.startsWith("D")) {
				addKey(toTags(StringUtils.split(key.substring(1), '/')),
						(String) e.getValue());
			}
		}
	}

	private void addKey(int[] tagPath, String val) {
		Dataset item = keys;
		int lastIndex = tagPath.length - 1;
		for (int i = 0; i < lastIndex; ++i) {
			Dataset tmp = item.getItem(tagPath[i]);
			item = tmp != null ? tmp : item.putSQ(tagPath[i]).addNewItem();
		}
		if (val.length() != 0)
			item.putXX(tagPath[lastIndex], val);
		else if (!item.contains(tagPath[lastIndex]))
			item.putXX(tagPath[lastIndex]);
	}

	private static int[] toTags(String[] tagStr) {
		int[] tags = new int[tagStr.length];
		for (int i = 0; i < tags.length; i++) {
			tags[i] = toTag(tagStr[i]);
		}
		return tags;
	}

	private static int toTag(String tagStr) {
		try {
			return (int) Long.parseLong(tagStr, 16);
		} catch (NumberFormatException e) {
			return Tags.forName(tagStr);
		}
	}
	
	private void initAssocParam(Configuration cfg, DcmURL url)
    {
		AssociationFactory af = AssociationFactory.getInstance();
        acTimeout = Integer.parseInt(cfg.getProperty("ac-timeout", "5000"));
        dimseTimeout = Integer.parseInt(cfg.getProperty("dimse-timeout", "0"));
        soCloseDelay = Integer.parseInt(cfg.getProperty("so-close-delay", "500"));
        assocRQ.setCalledAET(url.getCalledAET());
        assocRQ.setCallingAET(maskNull(url.getCallingAET()));
        assocRQ.setMaxPDULength(
                Integer.parseInt(cfg.getProperty("max-pdu-len", "16352")));
        assocRQ.addPresContext(af.newPresContext(PCID_FIND,
                UIDs.HangingProtocolInformationModelFIND, TS));
        assocRQ.addPresContext(af.newPresContext(PCID_MOVE,
                UIDs.HangingProtocolInformationModelMOVE, TS));
    }


 	private String maskNull(String callingAET) {
		return callingAET != null ? callingAET : "HPQRSCU";
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

	public boolean open()
	throws IOException, GeneralSecurityException
	{
		AssociationFactory af = AssociationFactory.getInstance();
		if (aassoc != null) {
			throw new IllegalStateException("Association already established");
		}		
		Association assoc = af.newRequestor(
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
	
	
	public void close()
	throws InterruptedException, IOException
	{
		if (aassoc != null) {
			try {
				aassoc.release(false);
			} finally {
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

	public void find()
	throws Exception
	{
		if (aassoc == null) {
			throw new IllegalStateException("No Association established");
		}
		Command rqCmd = DcmObjectFactory.getInstance().newCommand();
		rqCmd.initCFindRQ(aassoc.getAssociation().nextMsgID(),
				UIDs.HangingProtocolInformationModelFIND, priority);
		Dimse findRq = AssociationFactory.getInstance().newDimse(PCID_FIND, rqCmd, keys);
		log.info("C-FIND RQ Identifier:");
		log.info(keys);
		pendingFindRspData.clear();
		finalFindRsp = null;
		aassoc.invoke(findRq, this);
		synchronized (this) {
			while (finalFindRsp == null && findException == null && aassoc != null)
				wait();
		}
		log.info("Found " + pendingFindRspData.size() 
				+ " matching hanging protocols");
	}

	public void dimseReceived(Association assoc, Dimse dimse) {
		if (dimse.getCommand().isPending()) {
			try {
				Dataset ds = dimse.getDataset();
				pendingFindRspData.add(ds);
				log.info("C-FIND RSP Identifier:");
				log.info(ds);
			} catch (IOException e) {
				synchronized (this) {
					findException = e;
					notifyAll();
				}
			}
		} else {
			synchronized (this) {
				finalFindRsp = dimse;
				notifyAll();
			}
		}
	}
	
	public void write(Association src, PDU pdu) {}

	public void received(Association src, PDU pdu) {}

	public void write(Association src, Dimse dimse) {}

	public void received(Association src, Dimse dimse) {}

	public void error(Association src, IOException ioe) {
		synchronized (this) {
			findException = ioe;		
			notifyAll();
		}
	}

	public void closing(Association src) {}

	public void closed(Association src) {
		synchronized (this) {
			aassoc = null;		
			notifyAll();
		}
	}

	private void move()
	throws Exception
	{
		if (dest == null || pendingFindRspData.isEmpty())
			return;
		if (aassoc == null) {
			throw new IllegalStateException("No Association established");
		}
		FutureRSP future = aassoc.invoke(makeMoveRQ());
		Dimse moveRsp = future.get();
		Command rspCmd = moveRsp.getCommand();
		log.info("Retrieved" 
				+ rspCmd.getInt(Tags.NumberOfCompletedSubOperations, 0) 
				+ " hanging protocols to " + dest);
	}

	private Dimse makeMoveRQ() {
		final DcmObjectFactory dof = DcmObjectFactory.getInstance();
		Command rqCmd = dof.newCommand();
		rqCmd.initCMoveRQ(aassoc.getAssociation().nextMsgID(),
				UIDs.HangingProtocolInformationModelMOVE,
				priority,
				dest);
		Dataset rqDs = dof.newDataset();
		String[] iuids = new String[pendingFindRspData.size()];
		for (int i = 0; i < iuids.length; i++) {
			Dataset findRsp = (Dataset) pendingFindRspData.get(i);
			iuids[i] = findRsp.getString(Tags.SOPInstanceUID);
		}
		rqDs.putUI(Tags.SOPInstanceUID, iuids);
		return AssociationFactory.getInstance().newDimse(PCID_MOVE, rqCmd, rqDs);
	}

}

