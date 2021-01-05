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
 * @since    March 4, 2003
 * @version    $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
public class DcmProtocol
{
    /**  Description of the Field */
    public final static DcmProtocol DICOM =
            new DcmProtocol("dicom", null);
    /**  Description of the Field */
    public final static DcmProtocol DICOM_TLS =
            new DcmProtocol("dicom-tls",
            new String[]{
            "SSL_RSA_WITH_NULL_SHA",
			"TLS_RSA_WITH_AES_128_CBC_SHA",
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
            });
    /**  Description of the Field */
    public final static DcmProtocol DICOM_TLS_3DES =
            new DcmProtocol("dicom-tls.3des",
            new String[]{
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
            });
    /**  Description of the Field */
    public final static DcmProtocol DICOM_TLS_AES =
            new DcmProtocol("dicom-tls.aes",
            new String[]{
            "TLS_RSA_WITH_AES_128_CBC_SHA",
		    "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
            });
    /**  Description of the Field */
    public final static DcmProtocol DICOM_TLS_NODES =
            new DcmProtocol("dicom-tls.nodes",
            new String[]{
            "SSL_RSA_WITH_NULL_SHA",
            });


    /**
     *  Description of the Method
     *
     * @param  protocol  Description of the Parameter
     * @return           Description of the Return Value
     */
    public static DcmProtocol valueOf(String protocol)
    {
        String lower = protocol.toLowerCase();
        if (lower.equals("dicom")) {
            return DICOM;
        }
        if (lower.equals("dicom-tls")) {
            return DICOM_TLS;
        }
        if (lower.equals("dicom-tls.3des")) {
            return DICOM_TLS_3DES;
        }
        if (lower.equals("dicom-tls.aes")) {
            return DICOM_TLS_AES;
        }
        if (lower.equals("dicom-tls.nodes")) {
            return DICOM_TLS_NODES;
        }
        throw new IllegalArgumentException("protocol:" + protocol);
    }


    private final String name;
    private final String[] cipherSuites;


    private DcmProtocol(String name, String[] cipherSuites)
    {
        this.name = name;
        this.cipherSuites = cipherSuites;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString()
    {
        return name;
    }


    /**
     *  Gets the cipherSuites attribute of the DcmProtocol object
     *
     * @return    The cipherSuites value
     */
    public String[] getCipherSuites()
    {
        return cipherSuites == null ? null
                 : (String[]) cipherSuites.clone();
    }


    /**
     *  Gets the tLS attribute of the DcmProtocol object
     *
     * @return    The tLS value
     */
    public boolean isTLS()
    {
        return cipherSuites != null;
    }
}

