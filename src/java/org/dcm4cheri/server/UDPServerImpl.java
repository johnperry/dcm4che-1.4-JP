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

package org.dcm4cheri.server;

import org.dcm4che.server.UDPServer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 4036 $ $Date: 2006-08-14 15:42:49 +0200 (Mo, 14 Aug 2006) $
 */
public class UDPServerImpl implements UDPServer {
    
    private static Logger log = Logger.getLogger(UDPServerImpl.class);
    
    private static final int MAX_PACKAGE_SIZE = 65507;
    private static final int DEFAULT_PORT = 4000;

    private InetAddress laddr;
    private int port = DEFAULT_PORT;
    private int maxPacketSize = MAX_PACKAGE_SIZE;
    private int rcvBuf = 0;

    private DatagramSocket socket;
    private Thread thread;
    private long lastStartedAt;
    private long lastStoppedAt;
    private Handler handler;
    
    public UDPServerImpl(Handler handler) {
    if (handler==null)
        throw new NullPointerException();
    this.handler = handler;
    }
    
    public final boolean isRunning() {
        return socket != null;
    }

    public Date getLastStoppedAt() {
        return toDate(lastStoppedAt);
    }

    public Date getLastStartedAt() {
        return toDate(lastStartedAt);
    }

    private static Date toDate(long ms) {
        return ms > 0 ? new Date(ms) : null;
    }
    
    public final String getLocalAddress() {
        return laddr == null ? "0.0.0.0" : laddr.getHostAddress();
    }
    
    public void setLocalAddress(String laddrStr) {
        try {
            laddr = InetAddress.getByName(laddrStr);           
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown Host: " + laddrStr);
        }
    }
    
    public final int getPort() {
        return port;        
    }

    public void setPort(int port) {
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port: " + port);
        }
        this.port = port;        
    }

    public final int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize)  {
        if (maxPacketSize < 512 || maxPacketSize > MAX_PACKAGE_SIZE) {
            throw new IllegalArgumentException("maxPacketSize: " 
                    + maxPacketSize + " not in range 512..65507");
        }
        this.maxPacketSize = maxPacketSize;
    }

    public final int getReceiveBufferSize() {
        return rcvBuf;
    }

    public void setReceiveBufferSize(int rcvBuf)  {
        if (rcvBuf < 0) {
            throw new IllegalArgumentException("rcvBuf: " + rcvBuf);
        }
        this.rcvBuf = rcvBuf;
    }

    public synchronized void start() throws Exception {
        startServer();
    }

    public synchronized void stop() {
        stopServer();
    }
    
    private synchronized void startServer() throws SocketException {
        if (socket != null) {
            stopServer();
        }
        socket = new DatagramSocket(port, laddr);
        int prevRcvBuf = socket.getReceiveBufferSize();
        if (rcvBuf == 0) {
            rcvBuf = prevRcvBuf;
        } else if (rcvBuf != prevRcvBuf) {
            socket.setReceiveBufferSize(rcvBuf);
            rcvBuf = socket.getReceiveBufferSize();
        }
        thread = new Thread(new Runnable() {
            public void run() {
                lastStartedAt = System.currentTimeMillis();
                SocketAddress lsa = socket.getLocalSocketAddress();
                log.info("Started UDP Server listening on " + lsa);
                byte[] data = new byte[maxPacketSize];
                DatagramPacket p = new DatagramPacket(data, data.length);
                boolean restart = false;
                while (socket != null && !socket.isClosed()) {
                    try {
                        socket.receive(p);
                        handler.handle(p);
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            log.warn("UDP Server throws i/o exception - restart", e);
                            restart = true;
                        }
                        break;
                    }
                    p.setLength(data.length);
                }
                socket = null;
                thread = null;
                lastStoppedAt = System.currentTimeMillis();
                log.info("Stopped UDP Server listening on " + lsa);
                if (restart) {
                    try {
                        startServer();
                    } catch (SocketException e) {
                        log.error("Failed to restart UDP Server", e);
                    }
                }
            }
        });
        thread.start();
    }    

    private synchronized void stopServer() {
        if (socket != null) {
            socket.close();
            try { thread.join(); } catch (Exception ignore) {}
            socket = null;
        }
    }
}
