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

package org.dcm4che.util;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since    May, 2002
 * @version    $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
public final class MLLP_URL extends Object
{
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private MLLP_Protocol protocol;
    private String host;
    private int port = 0;

    // Constructors --------------------------------------------------
    /**
     *Constructor for the MLLP_URL object
     *
     * @param  spec  Description of the Parameter
     */
    public MLLP_URL(String spec)
    {
        parse(spec.trim());
    }


    /**
     *Constructor for the MLLP_URL object
     *
     * @param  protocol  Description of the Parameter
     * @param  host      Description of the Parameter
     * @param  port      Description of the Parameter
     */
    public MLLP_URL(String protocol, String host, int port)
    {
        this.protocol = MLLP_Protocol.valueOf(protocol);
        this.host = host;
        this.port = port;
    }

    // Public --------------------------------------------------------
    /**
     *  Gets the protocol attribute of the MLLP_URL object
     *
     * @return    The protocol value
     */
    public final String getProtocol()
    {
        return protocol.toString();
    }


    /**
     *  Gets the tLS attribute of the MLLP_URL object
     *
     * @return    The tLS value
     */
    public final boolean isTLS()
    {
        return protocol.isTLS();
    }


    /**
     *  Gets the cipherSuites attribute of the MLLP_URL object
     *
     * @return    The cipherSuites value
     */
    public final String[] getCipherSuites()
    {
        return protocol.getCipherSuites();
    }


    /**
     *  Gets the host attribute of the MLLP_URL object
     *
     * @return    The host value
     */
    public final String getHost()
    {
        return host;
    }


    /**
     *  Gets the port attribute of the MLLP_URL object
     *
     * @return    The port value
     */
    public final int getPort()
    {
        return port;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString()
    {
        return protocol.toString() + "://" + host + ':' + port;
    }

    // Private -------------------------------------------------------

    private void parse(String s)
    {
        int delimPos1 = s.indexOf("://");
        if (delimPos1 == -1) {
            throw new IllegalArgumentException(s);
        }
        int delimPos2 = s.indexOf(':', delimPos1 + 3);
        if (delimPos2 == -1) {
            throw new IllegalArgumentException(s);
        }
        protocol = MLLP_Protocol.valueOf(s.substring(0, delimPos1));
        host = s.substring(delimPos1 + 3, delimPos2);
        port = Integer.parseInt(s.substring(delimPos2 + 1));
    }
}

