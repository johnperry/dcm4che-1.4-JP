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

import java.util.Iterator;

import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceRegistry;

/**
 * @author     <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @since      June, 2002
 * @version    $Revision: 7898 $ $Date: 2008-11-03 12:15:52 +0100 (Mo, 03 Nov 2008) $
 */
public interface DcmHandler extends Server.Handler
{
    /**
     *  Adds a feature to the AssociationListener attribute of the DcmHandler
     *  object
     *
     * @param  l  The feature to be added to the AssociationListener attribute
     */
    void addAssociationListener(AssociationListener l);


    /**
     *  Description of the Method
     *
     * @param  l  Description of the Parameter
     */
    void removeAssociationListener(AssociationListener l);


    Iterator associationListenerIterator();

    /**
     *  Sets the acceptorPolicy attribute of the DcmHandler object
     *
     * @param  policy  The new acceptorPolicy value
     */
    void setAcceptorPolicy(AcceptorPolicy policy);


    /**
     *  Gets the acceptorPolicy attribute of the DcmHandler object
     *
     * @return    The acceptorPolicy value
     */
    AcceptorPolicy getAcceptorPolicy();


    /**
     *  Sets the dcmServiceRegistry attribute of the DcmHandler object
     *
     * @param  services  The new dcmServiceRegistry value
     */
    void setDcmServiceRegistry(DcmServiceRegistry services);


    /**
     *  Gets the dcmServiceRegistry attribute of the DcmHandler object
     *
     * @return    The dcmServiceRegistry value
     */
    DcmServiceRegistry getDcmServiceRegistry();


    /**
     *  Getter for property rqTimeout.
     *
     * @return    Value of property rqTimeout.
     */
    int getRqTimeout();


    /**
     *  Setter for property rqTimeout.
     *
     * @param  timeout  The new rqTimeout value
     */
    void setRqTimeout(int timeout);


    /**
     *  Getter for property dimseTimeout.
     *
     * @return    Value of property dimseTimeout.
     */
    int getDimseTimeout();


    /**
     *  Setter for property dimseTimeout.
     *
     * @param  dimseTimeout  New value of property dimseTimeout.
     */
    void setDimseTimeout(int dimseTimeout);


    /**
     *  Getter for property soCloseDelay.
     *
     * @return    Value of property soCloseDelay.
     */
    int getSoCloseDelay();


    /**
     *  Setter for property soCloseDelay.
     *
     * @param  soCloseDelay  New value of property soCloseDelay.
     */
    void setSoCloseDelay(int soCloseDelay);


    /**
     *  Getter for property packPDVs.
     *
     * @return    Value of property packPDVs.
     */
    boolean isPackPDVs();


    /**
     *  Setter for property packPDVs.
     *
     * @param  packPDVs  New value of property packPDVs.
     */
    void setPackPDVs(boolean packPDVs);
}

