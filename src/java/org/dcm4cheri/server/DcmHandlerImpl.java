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

import org.dcm4che.server.DcmHandler;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.AssociationFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *@author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 *@created    June, 2002
 *@version    $Revision: 7898 $ $Date: 2008-11-03 12:15:52 +0100 (Mo, 03 Nov 2008) $
 */
class DcmHandlerImpl implements DcmHandler
{

    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private final static AssociationFactory fact =
            AssociationFactory.getInstance();

    private final LinkedList listeners = new LinkedList();
    private AcceptorPolicy policy;
    private DcmServiceRegistry services;

    private int rqTimeout = 5000;
    private int dimseTimeout = 0;
    private int soCloseDelay = 500;
    private boolean packPDVs = false;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the DcmHandlerImpl object
     *
     *@param  policy    Description of the Parameter
     *@param  services  Description of the Parameter
     */
    public DcmHandlerImpl(AcceptorPolicy policy, DcmServiceRegistry services)
    {
        setAcceptorPolicy(policy);
        setDcmServiceRegistry(services);
    }

    // Public --------------------------------------------------------
    /**
     *  Sets the acceptorPolicy attribute of the DcmHandlerImpl object
     *
     *@param  policy  The new acceptorPolicy value
     */
    public final void setAcceptorPolicy(AcceptorPolicy policy)
    {
        if (policy == null) {
            throw new NullPointerException();
        }
        this.policy = policy;
    }


    /**
     *  Gets the acceptorPolicy attribute of the DcmHandlerImpl object
     *
     *@return    The acceptorPolicy value
     */
    public final AcceptorPolicy getAcceptorPolicy()
    {
        return policy;
    }


    /**
     *  Sets the dcmServiceRegistry attribute of the DcmHandlerImpl object
     *
     *@param  services  The new dcmServiceRegistry value
     */
    public final void setDcmServiceRegistry(DcmServiceRegistry services)
    {
        if (services == null) {
            throw new NullPointerException();
        }
        this.services = services;
    }


    /**
     *  Gets the dcmServiceRegistry attribute of the DcmHandlerImpl object
     *
     *@return    The dcmServiceRegistry value
     */
    public final DcmServiceRegistry getDcmServiceRegistry()
    {
        return services;
    }


    /**
     *  Sets the rqTimeout attribute of the DcmHandlerImpl object
     *
     *@param  timeout  The new rqTimeout value
     */
    public void setRqTimeout(int timeout)
    {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout: " + timeout);
        }
        this.rqTimeout = timeout;
    }


    /**
     *  Gets the rqTimeout attribute of the DcmHandlerImpl object
     *
     *@return    The rqTimeout value
     */
    public int getRqTimeout()
    {
        return rqTimeout;
    }


    /**
     *  Gets the dimseTimeout attribute of the DcmHandlerImpl object
     *
     *@return    The dimseTimeout value
     */
    public int getDimseTimeout()
    {
        return dimseTimeout;
    }


    /**
     *  Sets the dimseTimeout attribute of the DcmHandlerImpl object
     *
     *@param  timeout  The new dimseTimeout value
     */
    public void setDimseTimeout(int dimseTimeout)
    {
        if (dimseTimeout < 0) {
            throw new IllegalArgumentException("timeout: " + dimseTimeout);
        }
        this.dimseTimeout = dimseTimeout;
    }


    /**
     *  Gets the soCloseDelay attribute of the DcmHandlerImpl object
     *
     *@return    The soCloseDelay value
     */
    public int getSoCloseDelay()
    {
        return soCloseDelay;
    }


    /**
     *  Sets the soCloseDelay attribute of the DcmHandlerImpl object
     *
     *@param  delay  The new soCloseDelay value
     */
    public void setSoCloseDelay(int delay)
    {
        if (delay < 0) {
            throw new IllegalArgumentException("delay: " + delay);
        }
        this.soCloseDelay = delay;
    }

    /** Getter for property packPDVs.
     * @return Value of property packPDVs.
     */
    public boolean isPackPDVs() {
        return packPDVs;
    }
    
    /** Setter for property packPDVs.
     * @param packPDVs New value of property packPDVs.
     */
    public void setPackPDVs(boolean packPDVs) {
        this.packPDVs = packPDVs;
    }
    // DcmHandler implementation -------------------------------------
    /**
     *  Description of the Method
     *
     *@param  s                Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void handle(Socket s)
        throws IOException
    {
        Association assoc = fact.newAcceptor(s);
        assoc.setRqTimeout(rqTimeout);
        assoc.setDimseTimeout(dimseTimeout);
        assoc.setSoCloseDelay(soCloseDelay);
        assoc.setPackPDVs(packPDVs);
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
            assoc.addAssociationListener((AssociationListener) it.next());
        }
        if (assoc.accept(policy) instanceof AAssociateAC) {
            fact.newActiveAssociation(assoc, services).run();
        }
    }


    /**
     *  Adds a feature to the AssociationListener attribute of the
     *  DcmHandlerImpl object
     *
     *@param  l  The feature to be added to the AssociationListener attribute
     */
    public void addAssociationListener(AssociationListener l)
    {
        synchronized (listeners) {
            listeners.add(l);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  l  Description of the Parameter
     */
    public void removeAssociationListener(AssociationListener l)
    {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public Iterator associationListenerIterator() {
        return listeners.iterator();
    }

    /**
     *  Gets the sockedClosedByHandler attribute of the DcmHandlerImpl object
     *
     *@return    The sockedClosedByHandler value
     */
    public boolean isSockedClosedByHandler()
    {
        return true;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

