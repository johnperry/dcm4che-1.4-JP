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
 * The <code>ContainerContent</code> interface represents a
 * <i>DICOM SR Container Content</i> of value type <code>CONTAINER</code>.
 * <br>
 * A <i>Container Content</i> is used as Document Title or 
 * document section heading. Concept Name conveys 
 * the Document Title (if the <code>CONTAINER</code> is the 
 * Document Root Content Item) or the category of observation.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 */
public interface ContainerContent extends Content {

    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------

    /**
     * Spefifies whether <i>Content Items</i> are logically linked.
     * <br>DICOM Tag: <code>(0040,A050)</code>
     * <br>Tag Name: <code>Continuity of Content</code>
     * <br>
     * This flag specifies whether or not its contained
     * Content Items are logically linked in a continuous textual flow, 
     * or are separate items.
     *
     * @return  <code>true</code> if <code>Continuity of Content</code>
     * has value <code>SEPARATE</code> or <code>false</code> if
     * it has value <code>CONTINUOUS</code>
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.17.3.2 Continuity of Content"
     */
    public boolean isSeparate();

    public void setSeparate(boolean separate);
    
    /**
     * Convenient method to insert a new <i>Composite Content Item</i> into
     * this container and add the SOP instance reference into the <i>Current
     * Requested Procedure Evidence Sequence</i>. Particularly useful for
     * construction of Key Object Selection Documents.
     *
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param refSOP  the SOP instance reference.
     */
    public void insertCompositeContent(Code name, SOPInstanceRef refSOP);

    /**
     * Convenient method to insert a new <i>Image Content Item</i> into
     * this container and add the SOP instance reference of the image and the 
     * presentation state SOP instance reference into the <i>Current
     * Requested Procedure Evidence Sequence</i>. Particularly useful for
     * construction of Key Object Selection Documents.
     *
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param refSOP  the SOP instance reference of the referenced image.
     * @param frameNumbers  the references frame numbers of a multiframe image.
     * @param refPresentationSOP  the SOP instance reference of the
     *                            referenced presentation state.
     * @param iconImage <i>Icon Image</i> or <code>null</code>.
     */
    public void insertImageContent(Code name, SOPInstanceRef refSOP,
        int[] frameNumbers, SOPInstanceRef refPresentationSOP,
        IconImage iconImage);

    /**
     * Convenient method to insert a new <i>Waveform Conten Item</i> into
     * this container and add the SOP instance reference into the <i>Current
     * Requested Procedure Evidence Sequence</i>. Particularly useful for
     * construction of Key Object Selection Documents.
     *
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param refSOP  the SOP instance reference of the referenced Wavoform.
     * @param channelNumbers  the referenced channel numbers.
     */
    public void insertWaveformContent(Code name, SOPInstanceRef refSOP,
                                         int[] channelNumbers);
}
