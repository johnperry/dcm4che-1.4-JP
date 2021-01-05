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

package org.dcm4cheri.auditlog;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.dcm4che.auditlog.Destination;
import org.dcm4che.auditlog.MediaDescription;
import org.dcm4che.auditlog.Patient;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March 16, 2003
 * @version    $Revision: 3978 $ $Date: 2006-04-18 01:24:24 +0200 (Di, 18 Apr 2006) $
 */
class MediaDescriptionImpl implements MediaDescription
{

    // Variables -----------------------------------------------------
    private String mediaID;
    private String mediaType;
    private LinkedHashSet pats = new LinkedHashSet(3);
    private Destination dest;

    // Constructors --------------------------------------------------
    /**
     *Constructor for the MediaDescriptionImpl object
     *
     * @param  patient  Description of the Parameter
     */
    public MediaDescriptionImpl(Patient patient)
    {
        addPatient(patient);
    }

    // Methods -------------------------------------------------------
    /**
     *  Sets the mediaID attribute of the MediaDescriptionImpl object
     *
     * @param  mediaID  The new mediaID value
     */
    public void setMediaID(String mediaID)
    {
        this.mediaID = mediaID;
    }


    /**
     *  Sets the mediaType attribute of the MediaDescriptionImpl object
     *
     * @param  mediaType  The new mediaType value
     */
    public void setMediaType(String mediaType)
    {
        this.mediaType = mediaType;
    }


    /**
     *  Sets the destination attribute of the MediaDescriptionImpl object
     *
     * @param  dest  The new destination value
     */
    public void setDestination(Destination dest)
    {
        this.dest = dest;
    }


    /**
     *  Adds a feature to the Patient attribute of the MediaDescriptionImpl object
     *
     * @param  patient  The feature to be added to the Patient attribute
     */
    public final void addPatient(Patient patient)
    {
        pats.add(patient);
    }


    /**
     *  Description of the Method
     *
     * @param  sb  Description of the Parameter
     */
    public void writeTo(StringBuffer sb)
    {
        if (mediaID != null) {
            sb.append("<MediaID><![CDATA[")
                    .append(mediaID)
                    .append("]]></MediaID>");
        }
        if (mediaType != null) {
            sb.append("<MediaType><![CDATA[")
                    .append(mediaType)
                    .append("]]></MediaType>");
        }
        for (Iterator it = pats.iterator(); it.hasNext(); ) {
            Patient pat = (Patient) it.next();
            pat.writeTo(sb);
        }
        if (dest != null) {
            dest.writeTo(sb);
        }
    }
}

