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
 * See authors listed below.
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

package org.dcm4cheri.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;
import org.dcm4che.server.Server;
import org.dcm4che.util.HandshakeFailedEvent;
import org.dcm4che.util.HandshakeFailedListener;
import org.dcm4cheri.util.LF_ThreadPool;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Thomas Hacklaender <thomas@hacklaender-online.de>
 * @version $Revision: 15981 $ $Date: 2011-09-21 01:22:48 +0200 (Mi, 21 Sep 2011) $
 */
class ServerImpl implements LF_ThreadPool.Handler, Server {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    static final Logger log = Logger.getLogger(ServerImpl.class);
    private static int instCount = 0;
    private final String name = "TCPServer-" + ++instCount;
    
    private final Handler handler;
    private LF_ThreadPool threadPool = new LF_ThreadPool(this, name);
    private ServerSocket ss;
    private int port = 104;
    private List hcl = null;
    private List hfl = null;
    private ServerSocketFactory ssf = ServerSocketFactory.getDefault();    
    private int sslHandshakeSoTimeout = 0;
    private int soRcvBuf;
    private int soSndBuf;
    private boolean tcpNoDelay = true;
    
    private InetAddress laddr;
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public ServerImpl(Handler handler) {
        if (handler == null)
            throw new NullPointerException();
        
        this.handler = handler;
    }
    
    // Public --------------------------------------------------------
    public void setSSLHandshakeSoTimeout(int timeout){
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout: " + timeout);
        }
        this.sslHandshakeSoTimeout = timeout;
    }

    public int getSSLHandshakeSoTimeout(){
        return sslHandshakeSoTimeout;
    }
    
    public final int getReceiveBufferSize() {
        return soRcvBuf;
    }
    
    public final void setReceiveBufferSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size: " + size);
        }
        this.soRcvBuf = size;
    }

    public final int getSendBufferSize() {
        return soSndBuf;        
    }
    
    public final void setSendBufferSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size: " + size);
        }
        this.soSndBuf = size;
    }
        
    public final boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public final void setTcpNoDelay(boolean on) {
        this.tcpNoDelay = on;
    }

    // Server implementation -----------------------------------------
    public void addHandshakeCompletedListener(
            HandshakeCompletedListener listener) {
        hcl = addToList(hcl, listener);
    }
    
    public void addHandshakeFailedListener(
            HandshakeFailedListener listener) {
        hfl = addToList(hfl, listener);
    }
    
    public void removeHandshakeCompletedListener(
            HandshakeCompletedListener listener) {
        hcl = removeFromList(hcl, listener);
    }
    
    public void removeHandshakeFailedListener(
            HandshakeFailedListener listener) {
        hfl = removeFromList(hfl, listener);
    }
    
    public String getLocalAddress() {
        return laddr == null ? "0.0.0.0" : laddr.getHostAddress();
    }
    
    public void setLocalAddress(String laddrStr) {
        try {
            laddr = InetAddress.getByName(laddrStr);           
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown Host: " + laddrStr);
        }
    }

    // Listeners
    
    // Add an element to a list, creating a new list if the
    // existing list is null, and return the list.
    static List addToList(List l, Object elt) {
        if (l == null) {
            l = new ArrayList();
        }
        l.add(elt);
        return l;
    }
    
    
    // Remove an element from a list, discarding the list if the
    // resulting list is empty, and return the list or null.
    static List removeFromList(List l, Object elt) {
        if (l == null) {
            return l;
        }
        l.remove(elt);
        if (l.size() == 0) {
            l = null;
        }
        return l;
    }
    
    public void setMaxClients(int max) {
        threadPool.setMaxRunning(max);
    }
    
    public int getMaxClients() {
        return threadPool.getMaxRunning();
    }
    
    public int getNumClients() {
        return threadPool.running()-1;
    }
    
    public void setMaxIdleThreads(int max) {
        threadPool.setMaxWaiting(max);
    }

    public int getMaxIdleThreads() {
        return threadPool.getMaxWaiting();
    }

    public int getNumIdleThreads() {
        return threadPool.waiting();
    }
    
    public void start(int port) throws IOException {
        start(port, ServerSocketFactory.getDefault());
    }
    
    public void start(int port, ServerSocketFactory ssf) throws IOException {
        setPort(port);
        setServerSocketFactory(ssf);
        start();
    }
    
    public void start() throws IOException {
        checkNotRunning();
        if (log.isInfoEnabled())
            log.info("Start Server listening on " + getLocalAddress() + ":" + port);
        ss = ssf.createServerSocket(port,0,laddr);
        new Thread(new Runnable() {
            public void run() { threadPool.join(); }
        }, name).start();
    }
    
    public void stop() {
        if (ss == null)
            return;
        
        int port = ss.getLocalPort();
        if (log.isInfoEnabled()) {
            log.info("Stop Server listening at port " + port);
        }
        threadPool.shutdown();
        try {
            ss.close();
        } catch (IOException ignore) {}
        
        ss = null;
    	LF_ThreadPool tp = new LF_ThreadPool(this, name);
    	tp.setMaxRunning( threadPool.getMaxRunning());
    	tp.setMaxWaiting( threadPool.getMaxWaiting());
    	threadPool = tp;
    }
    
    // LF_ThreadPool.Handler implementation --------------------------
    public void run(LF_ThreadPool pool) {
        if (ss == null)
            return;
        
        Socket s = null;
        try {
            s = ss.accept();
            if (log.isInfoEnabled()) {
                log.info("handle - " + s);
            }
            if (s instanceof SSLSocket) {
                init((SSLSocket) s);
            }
            
            initSendBufferSize(s);
            initReceiveBufferSize(s);
            if (s.getTcpNoDelay() != tcpNoDelay) {
                s.setTcpNoDelay(tcpNoDelay );
            }
            
            pool.promoteNewLeader();
            handler.handle(s);
            if (!handler.isSockedClosedByHandler() && s != null) {
                try { s.close(); } catch (IOException ignore) {}
            }
        } catch (Exception e) {
            if ((e instanceof SocketException) && (s == null)) {
                log.info("Blocking ServerSocket.accept method canceled");
            } else {
                log.error(e, e);
            }
            if (s != null) {
                try { s.close(); } catch (IOException ignore) {};
            }
        }
        if (log.isInfoEnabled() && s != null) {
            log.info("finished - " + s);
        }
    }
    
    // Y overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    private void initSendBufferSize(Socket s) throws SocketException {
        int tmp = s.getSendBufferSize();
        if (soSndBuf == 0) {
            soSndBuf = tmp;
        }
        if (soSndBuf != tmp) {
            s.setSendBufferSize(soSndBuf);
            soSndBuf = s.getSendBufferSize();
        }
    }

    private void initReceiveBufferSize(Socket s) throws SocketException {
        int tmp = s.getReceiveBufferSize();
        if (soRcvBuf == 0) {
            soRcvBuf = tmp;
        }
        if (soRcvBuf != tmp) {
            s.setReceiveBufferSize(soRcvBuf);
            soRcvBuf = s.getReceiveBufferSize();
        }
    }

    private void checkNotRunning() {
        if (ss != null) {
            throw new IllegalStateException("Already Running - " + threadPool);
        }
    }
    
    private void init(SSLSocket s) throws IOException {
        if (hcl != null) {
            for (int i = 0, n = hcl.size(); i < n; ++i) {
                s.addHandshakeCompletedListener(
                    (HandshakeCompletedListener) hcl.get(i));
            }
        }
        InetAddress remoteAddr = s.getInetAddress();
        try {
        	s.setSoTimeout(sslHandshakeSoTimeout);
            s.startHandshake();
            if (log.isInfoEnabled()) {
                SSLSession se = s.getSession();
                try {
                    X509Certificate cert = (X509Certificate)
                        se.getPeerCertificates()[0];
                    cert.checkValidity();
                    log.info(s.getInetAddress().toString() + 
                        ": accept " + se.getCipherSuite() + " with "
                        + cert.getSubjectDN()+" valid from "+cert.getNotBefore()+" to "+cert.getNotAfter());
                } catch (SSLPeerUnverifiedException e) {
                    log.error("SSL peer not verified:",e);
                } catch ( CertificateException ce ) {
                	throw new IOException(ce.getMessage());
                }
            }
        } catch (IOException e) {
            if (hfl != null) {
                HandshakeFailedEvent event = new HandshakeFailedEvent(s,remoteAddr,e);
                for (int i = 0, n = hfl.size(); i < n; ++i) {
                    ((HandshakeFailedListener) hfl.get(i)).handshakeFailed(event);
                }
                throw e;
            }
        } finally {
            // Delegate timeout management to the handler
            s.setSoTimeout(0);
        }
    }
        
    /** Getter for property port.
     * @return Value of property port.
     *
     */
    public int getPort() {
        return port;
    }
    
    /** Setter for property port.
     * @param port New value of property port.
     *
     */
    public void setPort(int port) {
        if (port <= 0) {
            throw new IllegalArgumentException("port: " + port);
        }
        this.port = port;
    }
    
    /** Getter for property serverSocketFactory.
     * @return Value of property serverSocketFactory.
     *
     */
    public ServerSocketFactory getServerSocketFactory() {
        return ssf;
    }
    
    /** Setter for property serverSocketFactory.
     * @param serverSocketFactory New value of property serverSocketFactory.
     *
     */
    public void setServerSocketFactory(ServerSocketFactory ssf) {
        if (ssf == null) {
            throw new NullPointerException();
        }
        this.ssf = ssf;
    }
    
}
