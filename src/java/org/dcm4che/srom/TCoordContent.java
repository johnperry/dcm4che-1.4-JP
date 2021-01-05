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
 * The <code>TCoordContent</code> interface represents a
 * <i>DICOM SR Temporal Coordinate</i> of value type <code>TCOORD</code>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.18.7 Temporal Coordinates Macro"
 */
public interface TCoordContent extends Content {

    
    /**
     * The <code>Point</code> interface represents
     * a single temporal point.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface Point extends TCoordContent {}
    
    /**
     * The <code>MultiPoint</code> interface represents
     * multiple temporal points.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface MultiPoint extends TCoordContent {}
    
    /**
     * The <code>Segment</code> interface represents
     * a range between two temporal points.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface Segment extends TCoordContent {}
    
    /**
     * The <code>MultiSegment</code> interface represents
     * multiple segments, each denoted by two temporal points.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface MultiSegment extends TCoordContent {}
    
    /**
     * The <code>Begin</code> interface represents
     * a range beginning at one temporal point, and extending beyond 
     * the end of the acquired data.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface Begin extends TCoordContent {}
    
    /**
     * The <code>End</code> interface represents
     * a range beginning before the start of the acquired data, and 
     * extending to (and including) the identified temporal point.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface End extends TCoordContent {}
    
    /**
     * Generic <code>Positions</code> interface
     * for <i>DICOM SR Temporal Coordinate</i> positions.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface Positions {
        
        /**
         * Returns the number of position values.
         *
         * @return number of position values.
         */
        public int size();
        
        public void toDataset(Dataset ds);
    
        /**
         * The <code>Sample</code> interface
         * represents <i>DICOM SR Referenced Sample Positions</i>.
         * <br>
         * DICOM Tag: <code>(0040,A132)</code>
         * <br>
         * List of samples within a multiplex group specifying 
         * temporal points of the referenced data. Position of 
         * first sample is 1.
         *
         * @author  gunter.zeilinger@tiani.com
         * @version 1.0
         *
         * @see "DICOM Part 3: Information Object Definitions,
         * Annex C.18.7 Temporal Coordinates Macro"
         */
        interface Sample extends Positions {
            public int[] getIndexes();
        }
        
        /**
         * The <code>Relative</code> interface
         * represents <i>DICOM SR Referenced Time Offsets</i>.
         * <br>
         * DICOM Tag: <code>(0040,A138)</code>
         * <br>
         * Specifies temporal points for reference by number 
         * of seconds after start of data.
         *
         * @author  gunter.zeilinger@tiani.com
         * @version 1.0
         *
         * @see "DICOM Part 3: Information Object Definitions,
         * Annex C.18.7 Temporal Coordinates Macro"
         */
        interface Relative extends Positions {
            public float[] getOffsets();
        }
        
        /**
         * The <code>Absolute</code> interface
         * represents <i>DICOM SR Referenced Datetime</i>.
         * <br>
         * DICOM Tag: <code>(0040,A13A)</code>
         * <br>
         * Specifies temporal points for reference by absolute time.
         *
         * @author  gunter.zeilinger@tiani.com
         * @version 1.0
         *
         * @see "DICOM Part 3: Information Object Definitions,
         * Annex C.18.7 Temporal Coordinates Macro"
         */
        interface Absolute extends Positions {
            public Date[] getDateTimes();
        }
    }//end inner interface Positions        

    // Public --------------------------------------------------------
    
    /**
     * Returns the <i>Temporal Range Type</i>.
     * <br>DICOM Tag: <code>(0040,A130)</code>
     * <br>Tag Name: <code>Temporal Range Type</code>
     * <br>
     * This Attribute defines the type of temporal extent of the 
     * region of interest. A temporal point (or instant of time) 
     * may be defined by a waveform sample offset (for a single 
     * waveform multiplex group only), time offset, or absolute time.
     * The following return Values are specified for temporal coordinates:<br>
     * <br>
     * <dl>
     *   <dt>"POINT"</dt>
     *   <dd> a single temporal point.</dd>
     *
     *   <dt>"MULTIPOINT"</dt>
     *   <dd> multiple temporal points.</dd>
     *
     *   <dt>"SEGMENT"</dt>
     *   <dd> a range between two temporal points.</dd>
     *  
     *   <dt>"MULTISEGMENT"</dt>
     *   <dd> multiple segments, each denoted by two temporal points.</dd> 
     *
     *   <dt>"BEGIN"</dt>
     *   <dd> a range beginning at one temporal point, and extending beyond 
     *        the end of the acquired data.</dd> 
     *
     *   <dt>"END"</dt>
     *   <dd> a range beginning before the start of the acquired data, and 
     *        extending to (and including) the identified temporal point.</dd> 
     * </dl>
     *
     * @return the <i>Temporal Range Type</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7.1 Temporal Range Type"
     */
    public String getRangeType();
    
    /**
     * Returns the positions.
     *
     * @return  the positions.
     */
    public Positions getPositions();
    
}//end interface TCoordContent
