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

package org.dcm4che.net;

import java.util.Collection;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface AAssociateRQAC extends PDU {
    
    /** Returns Protocol-Version.
     * @return Protocol-Version. */    
    public int getProtocolVersion();

    /** Sets Protocol-Version.
     * @param version Protocol-Version. */    
    public void setProtocolVersion(int version);

    /** Returns Application-context-name.
     * Default = { @link org.dcm4che.dict.UIDs#DICOMApplicationContextName }.
     * @see #setApplicationContext
     * @return Application-context-name. */    
    public String getApplicationContext();

    /** Returns Source DICOM Application Name.
     * Default = "ANONYMOUS".
     * @see #getCalledAET
     * @see #setCallingAET
     * @return Source DICOM Application Name. */    
    public String getCallingAET();

    /** Returns Destination DICOM Application Name.
     * Default = "ANONYMOUS".
     * @see #getCallingAET
     * @see #setCalledAET
     * @return Destination DICOM Application Name. */    
    public String getCalledAET();

    /** Sets Application-context-name.
     * @see #getApplicationContext
     * @param uid Application-context-name */    
    public void setApplicationContext(String uid);

    /** Sets Source DICOM Application Name.
     * @see #setCalledAET
     * @see #getCallingAET
     * @param callingAET Source DICOM Application Name. */    
    public void setCallingAET(String callingAET);

    /** Sets Destination DICOM Application Name.
     * @see #setCallingAET
     * @see #getCalledAET
     * @param calledAET  Destination DICOM Application Name. */    
    public void setCalledAET(String calledAET);
    
    /** Returns next free valid Presentation-context-ID. Starting with 1,3,5,...
     * @return  next free Presentation-context-ID or -1, if maximum number
     * of Presentation Context (128) is reached. */    
    public int nextPCID();
    
    /** Adds specified Presentation Context to this Associate RQ/AC.
     * If this AssociateRQ previously contained a Presentation Context with an
     * equal Presentation-context-ID, the old will be replaced.
     * @param pc Presentation Context to add.
     * @return previous Presentation Context which was replaced or
     * <CODE>null</CODE> if there was no previous Presentation Context with an
     * equal Presentation-context-ID */    
    public PresContext addPresContext(PresContext pc);

    /** Removes the Presentation Context which the specified id (if present).
     * @param id Presentation-context-ID
     * @return previous Presentation Context which was removed or
     * <CODE>null</CODE> if there was no previous Presentation Context with this id */    
    public PresContext removePresContext(int id);    

    /** Removes all Presentation Context Items from this Associate RQ/AC. */    
    public void clearPresContext();

    /** Returns the Presentation Context which the specified id.
     * @param id Presentation-context-ID
     * @return  Presentation Context which the specified id or
     * <CODE>null</CODE> if this AssociateRQ contains no
     * Presentation Context with this id. */    
    public PresContext getPresContext(int id);

    /** Returns an list of contained Presentation Context Items.
     * The sequence shall be equal to the order, in which the Presentation Context Items
     * where added by {@link #addPresContext}.
     * @return list of contained Presentation Context Items. */    
    public Collection listPresContext();
    
    /** Returns Implementation Class UID in this AssociateRQ.
     * @see #setImplClassUID
     * @return Implementation Class UID */  
    public String getImplClassUID();

    /** Returns Implementation Version Name in this AssociateRQ or
     * <CODE>null</CODE> if no Implementation Version Name shall be negotiated.
     * @see #setImplVersionName
     * @return Implementation Version Name or 
     * <CODE>null</CODE> if no Implementation Version Name shall be negotiated */  
    public String getImplVersionName();

    /** Sets Implementation Class UID in this AssociateRQ.
     * @see #getImplClassUID
     * @param uid Implementation Class UID */    
    public void setImplClassUID(String uid);

    /** Sets Implementation Version Name in this AssociateRQ.
     * @see #getImplVersionName Implementation Version Name or
     * <CODE>null</CODE> if no Implementation Version Name shall be negotiated.
     * @param name Implementation Version Name  */    
    public void setImplVersionName(String name);

    /** Returns maximum size for received PDUs.
     * Default = 16352.
     * @see #setMaxPDULength
     * @return maximum size for received PDUs */    
    public int getMaxPDULength();

    /** Sets maximum size for received PDUs.
     * @see #getMaxPDULength
     * @param maxLength maximum size for received PDUs */    
    public void setMaxPDULength(int maxLength);
    
    /** Returns Asynchronous Operations Window or
     * <CODE>null</CODE> if no Asynchronous Operations Windo shall be negotiated.
     * Default = <CODE>null</CODE>.
     * @see #setAsyncOpsWindow
     * @return Asynchronous Operations Window
     * <CODE>null</CODE> if no Asynchronous Operations Windo shall be negotiated.*/    
    public AsyncOpsWindow getAsyncOpsWindow();
    
    /** Sets Asynchronous Operations Window.
     * @see #getAsyncOpsWindow
     * @param aow Asynchronous Operations Window or
     * <CODE>null</CODE> if no Asynchronous Operations Windo shall be negotiated. */    
    public void setAsyncOpsWindow(AsyncOpsWindow aow);

    /** Adds specified SCP/SCU Role Selection Sub-Item to this AssociateRQ.
     * If this AssociateRQ previously contained a SCP/SCU Role Selection Sub-Item associated
     * with the same SOP Class, the old will be replaced.
     * @param roleSelection SCP/SCU Role Selection Sub-Item to add.
     * @return previous SCP/SCU Role Selection Sub-Item which was replaced or
     * <CODE>null</CODE> if there was no previous SCP/SCU Role Selection Sub-Item associated
     * with the same SOP Class */    
    public RoleSelection addRoleSelection(RoleSelection roleSelection);

    /** Removes the SCP/SCU Role Selection Sub-Item, associated which the specified SOP Class (if present).
     * @param sopClass SOP Class UID
     * @return previous SCP/SCU Role Selection Sub-Item  which was removed or
     * <CODE>null</CODE> if there was no previous SCP/SCU Role Selection Sub-Item  associated
     * with this SOP Class */    
    public RoleSelection removeRoleSelection(String sopClass);
       
    /** Removes all SCP/SCU Role Selection Sub-Items from this AssociateRQ. */    
    public void clearRoleSelections();

    /** Returns the SCP/SCU Role Selection Sub-Item, associated which the specified SOP Class.
     * @param sopClass SOP Class UID
     * @return  SCP/SCU Role Selection Sub-Item, or
     * <CODE>null</CODE> if no SCP/SCU Role Selection Sub-Item is 
     * associated which the specified SOP Class UID. */    
    public RoleSelection getRoleSelection(String sopClass);

    /** Returns list of the contained SCP/SCU Role Selection Sub-Items.
     * The sequence shall be equal to the order, in which the SCP/SCU Role Selection Sub-Items
     * where added by {@link #addRoleSelection}.
     * @return list of contained SCP/SCU Role Selection Sub-Items */    
    public Collection listRoleSelections();

    /** Adds specified Extended Negotiation Sub-item to this AssociateRQ.
     * If this AssociateRQ previously contained a Extended Negotiation Sub-Item associated
     * with the same SOP Class, the old will be replaced.
     * @param extNeg Extended Negotiation Sub-Item to add.
     * @return previous Extended Negotiation Sub-Item which was replaced or
     * <CODE>null</CODE> if there was no previous Extended Negotiation Sub-Item associated
     * with the same SOP Class */    
    public ExtNegotiation addExtNegotiation(ExtNegotiation extNeg);

    /** Removes the Extended Negotiation Sub-Item, associated which the specified SOP Class (if present).
     * @param sopClass SOP Class UID
     * @return previous Extended Negotiation Sub-Item  which was removed or
     * <CODE>null</CODE> if there was no previous Extended Negotiation Sub-Item  associated
     * with this SOP Class */    
    public ExtNegotiation removeExtNegotiation(String sopClass);

    /** Removes all Extended Negotiation Sub-Items from this AssociateRQ. */    
    public void clearExtNegotiations();

    /** Returns the Extended Negotiation Sub-Item, associated which the specified SOP Class.
     * @param sopClass SOP Class UID
     * @return  Extended Negotiation Sub-Item, or
     * <CODE>null</CODE> if no Extended Negotiation Sub-Item is 
     * associated which the specified SOP Class UID. */    
    public ExtNegotiation getExtNegotiation(String sopClass);

    /** Returns list of contained Extended Negotiation Sub-Items.
     * The sequence shall be equal to the order, in which the Extended Negotiation Sub-Items
     * where added by {@link #addRoleSelection}.
     * @return list the contained Extended Negotiation Sub-Items */    
    public Collection listExtNegotiations();

}

