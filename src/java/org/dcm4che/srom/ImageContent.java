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
 * The <code>ImageContent</code> interface represents a
 * <i>DICOM SR Image Content</i> of value type <code>IMAGE</code>.
 * <br>
 *
 * A <i>DICOM SR Image Content</i> specifies the Attributes that convey 
 * a reference to a DICOM image.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.18.4 Image Reference Macro"
 */
public interface ImageContent extends CompositeContent {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the frame numbers of a refereced DICOM image.
     * <br>DICOM Tag: <code>(0008,1160)</code>
     * <br>Tag Name: <code>Referenced Frame Number</code>
     * <br>
     * Identifies the frame numbers within the Referenced SOP Instance 
     * to which the reference applies. The first frame shall be denoted 
     * as frame number 1. 
     * 
     * <br><b>Note:</b><br> 
     * <pre>
     *   This Attribute may be multi-valued. 
     *   Required if the Referenced SOP Instance 
     *   is a multi-frame image and the reference 
     *   does not apply to all frames.
     * </pre>
     *
     * @return  Frame numbers of a refereced DICOM image.
     */
    public int[] getFrameNumbers();

    public void setFrameNumbers(int[] frameNumbers);
    
    /**
     * Returns the <b>single item</b> of <i>DICOM Referenced SOP Sequence</i>
     * for reference to a 
     * <i>Softcopy Presentation State SOP Class/SOP Instance</i> pair.
     * <br>DICOM Tag: <code>(0008,1199)</code>
     * <br>Tag Name: <code>Referenced SOP Sequence</code>
     * <br>
     * This is the reference to the presentation state of a DICOM image.
     * Only a single Item is permitted in this sequence so only one
     * {@link RefSOP} object will be returned.
     *
     * <br>
     * <br><b>NOTE:</b>  from [Clunie2000]
     * <pre>
     *   There may not be more than one item (since that would imply 
     *   more than one value),<sup>58</sup> and there may not be zero items 
     *   (since that would imply no value).
     *   
     *   <sup>58.</sup><small>
     *      This is not strictly true. As of the time of writing,
     *      there is what is probably an error in Supplement 23 
     *      that has not yet been corrected with a CP. Though the
     *      text describing the COMPOSITE value type implies a 
     *      single reference,the macro allows for one or more 
     *      items in the sequence. This also affects the IMAGE 
     *      and WAVEFORM value types which include the COMPOSITE 
     *      macro.
     *     </small>
     * </pre>
     *
     * @return  Single item of <code>Referenced SOP Sequence</code>.
     *
     * @see "[Clunie2000] - Clunie, David. <i>DICOM Structured Reporting</i>. 
     * PixelMed Publishing, Bangor, Pennsylvania, 2000. ISBN: 0970136900."
     */
    public RefSOP getRefPresentationSOP();

    public void setRefPresentationSOP(RefSOP refPresentationSOP);
    
    public IconImage getIconImage();

    public void setIconImage(IconImage iconImage);
    
}//end interface ImageContent
