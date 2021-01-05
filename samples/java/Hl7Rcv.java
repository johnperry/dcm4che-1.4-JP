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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.apache.log4j.Logger;
import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Factory;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.hl7.HL7Service;
import org.dcm4che.server.HL7Handler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.MLLP_Protocol;
import org.dcm4che.util.SSLContextAdapter;

/**
 * <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created    September 7, 2002
 * @version    $Revision: 14732 $ $Date: 2011-01-19 16:27:12 +0100 (Mi, 19 Jän 2011) $
 */
public class Hl7Rcv implements HL7Service
{

    // Constants -----------------------------------------------------
    
    
    private final static LongOpt[] LONG_OPTS = new LongOpt[]{
            new LongOpt("max-clients", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("so-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("ack-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("receiving-apps", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("sending-apps", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("mllp-tls", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("mllp-tls.nodes", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("mllp-tls.3des", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
            new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("msg-types", LongOpt.REQUIRED_ARGUMENT, null, 2)
            };

    private static ResourceBundle messages = ResourceBundle.getBundle(
            "Hl7Rcv", Locale.getDefault());

    // Variables -----------------------------------------------------
    final static Logger log = Logger.getLogger(Hl7Rcv.class);
    final static ServerFactory sf = ServerFactory.getInstance();
    final static HL7Factory hl7f = HL7Factory.getInstance();

    private final HL7Handler handler = sf.newHL7Handler();
    private final Server server = sf.newServer(handler);
    private int ackDelay = 0;

    private SSLContextAdapter tls = null;
    private MLLP_Protocol protocol = MLLP_Protocol.MLLP;
    private int fileNumber = 1;
    private File dir = null;
    // Constructors --------------------------------------------------
    Hl7Rcv(Configuration cfg)
    {
        initServer(cfg);
        initDest(cfg);
        initTLS(cfg);
    }
    
    private final void initDest(Configuration cfg)
    {
        String dest = cfg.getProperty("dest", "", "<none>", "");
        if (dest.length() == 0 || "/dev/null".equals(dest)) {
            return;
        }

        this.dir = new File(dest);
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
       log.info("Dest=" + dest);
    }
    
    private void initServer(Configuration cfg)
    {
        server.setPort(
                Integer.parseInt(cfg.getProperty("port")));
        server.setMaxClients(
                Integer.parseInt(cfg.getProperty("max-clients", "10")));
        handler.setSoTimeout(
                Integer.parseInt(cfg.getProperty("so-timeout", "0")));
        handler.setReceivingApps(cfg.tokenize(
                cfg.getProperty("receiving-apps", null, "<any>", null)));
        handler.setSendingApps(cfg.tokenize(
                cfg.getProperty("sending-apps", null, "<any>", null)));
        ackDelay = Integer.parseInt(cfg.getProperty("ack-delay", "0"));
        handler.putService("ADT", this);
        handler.putService("ORM", this);
        handler.putService("ORU", this);       
        String[] msgTypes = cfg.tokenize(cfg.getProperty("msg-types", "", "<none>", ""));
        if (msgTypes != null) {
            log.info("Additional msg-types="+msgTypes);
            for (int i = 0 ; i < msgTypes.length ; i++) {
                handler.putService(msgTypes[i], this);
            }
        }
    }


    private void initTLS(Configuration cfg)
    {
        try {
            this.protocol = MLLP_Protocol.valueOf(
                    cfg.getProperty("protocol", "mllp"));
            if (!protocol.isTLS()) {
                return;
            }

            tls = SSLContextAdapter.getInstance();
            char[] keypasswd = cfg.getProperty("tls-key-passwd", "secret").toCharArray();
            tls.setKey(
                    tls.loadKeyStore(
                    Hl7Rcv.class.getResource(cfg.getProperty("tls-key", "certificates/test_sys_1.p12")),
                    keypasswd),
                    keypasswd);
            tls.setTrust(tls.loadKeyStore(
                    Hl7Rcv.class.getResource(cfg.getProperty("tls-cacerts", "certificates/mesa_certs.jks")),
                    cfg.getProperty("tls-cacerts-passwd", "secret").toCharArray()));
            this.server.setServerSocketFactory(
                    tls.getServerSocketFactory(protocol.getCipherSuites()));
        } catch (Exception ex) {
            throw new RuntimeException("Could not initalize TLS configuration: ", ex);
        }
    }

    // Methods -------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @exception  IOException  Description of the Exception
     */
    public void start()
        throws IOException
    {
        server.start();
    }


    /**
     *  Description of the Method
     *
     * @param  msg               Description of the Parameter
     * @return                   Description of the Return Value
     * @exception  HL7Exception  Description of the Exception
     */
    public byte[] execute(byte[] msg)
        throws HL7Exception
    {
        HL7Message hl7 = hl7f.parse(msg);
        if (log.isDebugEnabled()) {
            log.debug("Received:\n" + hl7.toVerboseString());
        }
       
        if(dir != null)
        {
            File fileName = new File(dir,hl7.header().getMessageType() + (fileNumber++) + ".hl7");
            try {
                FileOutputStream f = new FileOutputStream(fileName);
                f.write(msg);
                f.close();
            } catch (FileNotFoundException e1) {
                log.error("Couldn't open file: " + fileName, e1);
                e1.printStackTrace();
            } catch (IOException e) {
                log.error("IO/Exception writing to file: " + fileName);
                e.printStackTrace();
            }
        }
       
        if (ackDelay > 0) {
            try {
                Thread.sleep(ackDelay);
            } catch (InterruptedException e) {}
        }
        byte[] ack = hl7.header().makeACK_AA();
        if (log.isDebugEnabled()) {
            log.debug("Send:\n" + hl7f.parse(ack).toVerboseString());
        }
        
        return ack;
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
        Getopt g = new Getopt("hl7rcv", args, "", LONG_OPTS);

        Configuration cfg = new Configuration(
                Hl7Rcv.class.getResource("hl7rcv.cfg"));
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 2:
                    cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
                    break;
                case 4:
                    cfg.put("protocol", LONG_OPTS[g.getLongind()].getName());
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
            new Hl7Rcv(cfg).start();
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
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

}

