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

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Locale;

import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Factory;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.util.MLLP_URL;
import org.dcm4che.util.MLLPInputStream;
import org.dcm4che.util.MLLPOutputStream;
import org.dcm4che.util.SSLContextAdapter;

import java.util.ResourceBundle;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 12031 $ $Date: 2009-08-18 17:27:22 +0200 (Di, 18 Aug 2009) $
 * @since August 22, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class Hl7Snd {
    
    // Constants -----------------------------------------------------
    private static final LongOpt[] LONG_OPTS = new LongOpt[] {
        new LongOpt("ack-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
        new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
        new LongOpt("check", LongOpt.NO_ARGUMENT, null, 'c')
    };
    
    // Variables -----------------------------------------------------
    private static final Logger log = Logger.getLogger(Hl7Snd.class);
    private static final HL7Factory hl7Fact = HL7Factory.getInstance();
    private static ResourceBundle messages = 
        ResourceBundle.getBundle("Hl7Snd", Locale.getDefault());
    private static boolean checkInputFile = false;
    private MLLP_URL url;
    private int ackTimeout = 0;
    private SSLContextAdapter tls = null;
    private String[] cipherSuites = null;
    
    public static void main(String args[]) throws Exception {
        Getopt g = new Getopt("hl7snd", args, "", LONG_OPTS);
        
        Configuration cfg = new Configuration(
            Hl7Snd.class.getResource("hl7snd.cfg"));
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 2:
                    cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
                    break;
                case 'v':
                    exit(messages.getString("version"), false);
                case 'h':
                    exit(messages.getString("usage"), false);
                case '?':
                    exit(null, true);
                    break;
                case 'c':
                	checkInputFile = true;
            }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc < 2) {
            exit(messages.getString("missing"), true);
        }
        //      listConfig(cfg);
        try {
            Hl7Snd hl7snd = new Hl7Snd(cfg, new MLLP_URL(args[optind]), argc);
            System.exit( hl7snd.execute(args, optind+1));
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
        }
    }
    
    private static void exit(String prompt, boolean error) {
        if (prompt != null)
            System.err.println(prompt);
        if (error)
            System.err.println(messages.getString("try"));
        System.exit(1);
    }

    // Constructors --------------------------------------------------
    Hl7Snd(Configuration cfg, MLLP_URL url, int argc) {
        this.url = url;
        this.ackTimeout = Integer.parseInt(cfg.getProperty("ack-timeout", "0"));
        initTLS(cfg);
    }
    
    // Methods -------------------------------------------------------
    public int execute(String[] args, int offset) {
        long t1 = System.currentTimeMillis();
        int count = 0;
        int exitStatus = 0;
        Socket s = null;
        MLLPInputStream in = null;
        MLLPOutputStream out = null;
        try {
            s = newSocket(url.getHost(), url.getPort());
            s.setSoTimeout(ackTimeout);
            in = new MLLPInputStream(
                new BufferedInputStream(s.getInputStream()));
            out = new MLLPOutputStream(
                new BufferedOutputStream(s.getOutputStream()));
            for (int i = offset; i < args.length; ++i) {
                count += send(new File(args[i]), in, out);
            }
        } catch (Exception e) {
            log.error("Could not send all messages: ", e);
            exitStatus = 1;
        } finally {
            if (out != null) {
                try { out.close(); } catch (IOException ignore) {}
            }
            if (in != null) {
                try { in.close(); } catch (IOException ignore) {}
            }
            if (s != null) {
                try { s.close(); } catch (IOException ignore) {}
            }
        }
        long dt = System.currentTimeMillis() - t1;
        log.info(
            MessageFormat.format(messages.getString("sendDone"),
            new Object[]{
                new Integer(count),
                new Long(dt),
            }));
        return exitStatus;
    }
    
    private int send(File file, MLLPInputStream in, MLLPOutputStream out)
    throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            int count = 0;
            for (int i = 0; i < files.length; ++i) {
                count += send(files[i], in, out);
            }
            return count;
        }
        FileInputStream fin = null;
        byte[] msg;
        try {
            fin = new FileInputStream(file);
            msg = new byte[(int) file.length()];
            fin.read(msg);
        } catch (IOException e) {
            log.warn("Could not read " + file);
            return 0;
        } finally {
            if (fin != null) {
                try { fin.close(); } catch (IOException ignore) {}
            }
        }
        if (log.isInfoEnabled() || checkInputFile ) {
            try {
                HL7Message hl7 = hl7Fact.parse(msg);
                log.info("Send: " + hl7);
                if (log.isDebugEnabled()) {
                    log.debug(hl7.toVerboseString());
                }
            } catch (Exception e) {
                if ( checkInputFile ) {
                    log.warn("Could not parse HL7 message load from " + file, e);
                	System.exit(2);
                } else {
                    log.warn("Could not parse HL7 message load from " + file + "! Ignored!");
                	return 0;
                }
            }
        }
        out.writeMessage(msg);
        out.flush();
        msg = in.readMessage();
        if (log.isInfoEnabled()) {
            try {
                HL7Message hl7 = hl7Fact.parse(msg);
                log.info("Received: " + hl7);
                if (log.isDebugEnabled()) {
                    log.debug(hl7.toVerboseString());
                }
            } catch (HL7Exception e) {
                log.warn("Could not parse HL7 message received from " + url, e);
            }
        }
        return 1;
    }

    private Socket newSocket(String host, int port)
    throws IOException, GeneralSecurityException {
        if (cipherSuites != null) {
            return tls.getSocketFactory(cipherSuites).createSocket(host, port);
        } else {
            return new Socket(host, port);
        }
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
                    Hl7Snd.class.getResource(
                        cfg.getProperty("tls-key","certificates/test_sys_1.p12")),
                    keypasswd),
                keypasswd);
            tls.setTrust(tls.loadKeyStore(
                Hl7Snd.class.getResource(
                    cfg.getProperty("tls-cacerts", "certificates/mesa_certs.jks")),
                cfg.getProperty("tls-cacerts-passwd", "secret").toCharArray()));
            tls.init();
        } catch (Exception ex) {
           throw new RuntimeException("Could not initalize TLS configuration: ", ex);
        }
    }
}
