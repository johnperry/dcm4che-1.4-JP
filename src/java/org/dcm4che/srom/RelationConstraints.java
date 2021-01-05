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


/**
 * This interface will check the <i>Relationship</i> 
 * constraints between two <i>SR Content Item</i>s. 
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 0.9.9
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 */
public interface RelationConstraints {
    // Constants -----------------------------------------------------    
   
    // Public --------------------------------------------------------
    
    /**
     * Checks if a <i>Relation</i> is valid.
     * <i>SR Content Item</i>s have to have valid <i>Relationship Type</i>s
     * in order to have a valid SR Document.
     * <br>
     * e.g. the <code>source</code> of <i>Relationship</i> 
     * <code>CONTAINS</code> has to be of 
     * <i>Content Type</i> <code>CONTAINER</code>.
     *
     * @param source  the source <i>Content Item</i>.
     * @param relation  the <i>Relationship Type</i>. 
     * @param target  the target <i>Content Item</i>.
     *
     * @throws IllegalArgumentException  if <i>Relationship</i> is not valid.
     */
    public void check(Content source, Content.RelationType relation,
            Content target);

}//end interface RelationConstraints 
