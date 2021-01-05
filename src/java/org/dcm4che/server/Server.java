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

package org.dcm4che.server;

import java.io.IOException;
import java.net.Socket;
import javax.net.ServerSocketFactory;
import javax.net.ssl.HandshakeCompletedListener;

import org.dcm4che.util.HandshakeFailedListener;

/**
 * <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @since    May, 2002
 * @version    $Revision: 15981 $ $Date: 2011-09-21 01:22:48 +0200 (Mi, 21 Sep 2011) $
 */
public interface Server 
{
    int getSSLHandshakeSoTimeout();
    
    void setSSLHandshakeSoTimeout(int timeout);
    
    int getReceiveBufferSize();
    
    void setReceiveBufferSize(int size);

    int getSendBufferSize();
    
    void setSendBufferSize(int size);
        
    boolean isTcpNoDelay();

    void setTcpNoDelay(boolean on);
    
    interface Handler
    {
        void handle(Socket s)
            throws IOException;


        boolean isSockedClosedByHandler();
    }
    
    void addHandshakeCompletedListener(HandshakeCompletedListener listener);


    void addHandshakeFailedListener(HandshakeFailedListener listener);


    void removeHandshakeCompletedListener(HandshakeCompletedListener listener);

    void removeHandshakeFailedListener(HandshakeFailedListener listener);

    void setMaxClients(int max);

    int getMaxClients();

    int getNumClients();

    void setMaxIdleThreads(int max);
    
    int getMaxIdleThreads();
    
    int getNumIdleThreads();
    
    /**
     * @param  port             Description of the Parameter
     * @exception  IOException  Description of the Exception
     * @deprecated              use {@link #setPort}, {@link #start()}
     */
    void start(int port)
        throws IOException;


    /**
     * @param  port             Description of the Parameter
     * @param  ssf              Description of the Parameter
     * @exception  IOException  Description of the Exception
     * @deprecated              use {@link #setPort}, {@link #setServerSocketFactory},
     *                 {@link #start()}
     */
    void start(int port, ServerSocketFactory ssf)
        throws IOException;


    void start()
        throws IOException;


    void stop();


    /**
     * Getter for property port.
     *
     * @return    Value of property port.
     */
    public int getPort();


    /**
     * Setter for property port.
     *
     * @param  port  New value of property port.
     */
    public void setPort(int port);

    /**
     * Getter for property localAddress.
     *
     * @return    Value of property localAddress 
     */
    public String getLocalAddress();


    /**
     * Setter for property port.
     *
     * @param  port  New value of property localAddress.
     */
    public void setLocalAddress(String localAddress);

    /**
     * Getter for property serverSocketFactory.
     *
     * @return    Value of property serverSocketFactory.
     */
    public ServerSocketFactory getServerSocketFactory();


    /**
     * Setter for property serverSocketFactory.
     *
     * @param  serverSocketFactory  New value of property serverSocketFactory.
     */
    public void setServerSocketFactory(ServerSocketFactory serverSocketFactory);

}

