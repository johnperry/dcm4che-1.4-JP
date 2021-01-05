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

package org.dcm4che.srom;

import org.dcm4che.data.Dataset;

import java.util.Date;


/**
 * The <code>Template</code> interface represents a
 * <i>DICOM SR Template</i>.
 * <br>
 * <br>
 * <cite>
 * A Template for SR Documents defines a set of constraints on the 
 * relationships and content (Value Types, Codes, etc.) of Content Items 
 * that reference such a Template. Specific Templates for SR Documents are 
 * defined either by the DICOM Standard or by users of the Standard for 
 * particular purposes.
 * </cite>
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * 9 TEMPLATE IDENTIFICATION MACRO"
 */
public interface Template {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
  
    /**
     * Returns the value of field <i>Template Identifier</i>.
     * <br>DICOM Tag: <code>(0040,DB00)</code>
     * <br>Tag Name: <code>Template Identifier</code>
     * <br>
     * Template identifier.
     * 
     * @return  the value of field <i>Template Identifier</i>.
     */
    public String getTemplateIdentifier();
    
    /**
     * Returns the value of field <i>Mapping Resource</i>.
     * <br>DICOM Tag: <code>(0008,0105)</code>
     * <br>Tag Name: <code>Mapping Resource</code>
     * <br>
     * Mapping Resource that defines the template. 
     *
     * @return  the value of field <i>Mapping Resource</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * 8.4 MAPPING RESOURCE"
     */
    public String getMappingResource();
    
    /**
     * Returns the value of field <i>Template Version</i>.
     * <br>DICOM Tag: <code>(0040,DB06)</code>
     * <br>Tag Name: <code>Template Version</code>
     * <br>
     * Version of the Template. 
     * Required if the <i>Template Identifier</i> <code>(0040,DB00)</code> 
     * and <i>Mapping Resource</i> <code>(0008,0105)</code> are not sufficient 
     * to identify the template unambiguously.
     *
     * @return  the value of field <i>Template Version</i>.
     */
    public Date getTemplateVersion();
    
    /**
     * Returns the value of field <i>Template Local Version</i>.
     * <br>DICOM Tag: <code>(0040,DB07)</code>
     * <br>Tag Name: <code>Template Local Version</code>
     * <br>
     * Local version number assigned to a template that contains
     * private extensions. Required if the value of 
     * <i>Template Extension Flag</i> <code>(0040,DB0B)</code> 
     * is "<code>Y</code>".
     *
     * @return  the value of field <i>Template Local Version</i>.
     */
    public Date getTemplateLocalVersion();
    
    /**
     * Compares two <code>Template</code>s for equality.
     *
     * @param obj  the <code>Template</code> object to compare this
     *             object for equality with.
     *
     * @return <code>true</code> if <code>obj</code> has the same values
     *         as this <code>Template</code> object <code>false</code> 
     *         otherwise.
     */
    public boolean equals(Object obj);
    
    public void toDataset(Dataset ds);
    
}//end interface Template
