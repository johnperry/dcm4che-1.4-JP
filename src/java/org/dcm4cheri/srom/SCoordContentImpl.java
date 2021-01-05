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

package org.dcm4cheri.srom;

import org.dcm4che.srom.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

//java imports
import java.util.Date;


/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
abstract class SCoordContentImpl extends NamedContentImpl
        implements SCoordContent {
            
    // Inner classes ---------------------------------------------------------
            
    static class Point extends SCoordContentImpl
            implements SCoordContent.Point {
                
        Point(KeyObject owner, Date obsDateTime, Template template, Code name,
                float[] graphicData) {
            super(owner, obsDateTime, template, name, graphicData);
            if (graphicData.length != 2)
                throw new IllegalArgumentException("float[" 
                                                   + graphicData.length + "]");
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new SCoordContentImpl.Point(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, graphicData);
        }//end clone()

        public String getGraphicType() {  return "POINT";  }        
    }//end inner class Point
    
            
    static class MultiPoint extends SCoordContentImpl
            implements SCoordContent.MultiPoint {
                
        MultiPoint(KeyObject owner, Date obsDateTime, Template template, 
                   Code name, float[] graphicData) {
            super(owner, obsDateTime, template, name, graphicData);
            if ((graphicData.length & 1) != 0)
                throw new IllegalArgumentException("float[" 
                                                   + graphicData.length + "]");
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new SCoordContentImpl.MultiPoint(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, graphicData);
        }//end clone()

        public String getGraphicType() {  return "MULTIPOINT";  }        
    }//end inner class MultiPoint
            
    
    static class Polyline extends SCoordContentImpl
            implements SCoordContent.Polyline {
                
        Polyline(KeyObject owner, Date obsDateTime, Template template, 
                 Code name, float[] graphicData) {
            super(owner, obsDateTime, template, name, graphicData);
            if ((graphicData.length&1) != 0)
                throw new IllegalArgumentException("float[" 
                                                   + graphicData.length + "]");
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new SCoordContentImpl.Polyline(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, graphicData);
        }//end clone()

        public String getGraphicType() {  return "POLYLINE";  }        
    }//end inner class Polyline
            
    
    static class Circle extends SCoordContentImpl
            implements SCoordContent.Circle {
                
        Circle(KeyObject owner, Date obsDateTime, Template template, 
               Code name, float[] graphicData) {
            super(owner, obsDateTime, template, name, graphicData);
            if (graphicData.length != 4)
                throw new IllegalArgumentException("float[" 
                                                   + graphicData.length + "]");
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new SCoordContentImpl.Circle(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, graphicData);
        }//end clone()

        public String getGraphicType() {  return "CIRCLE";  }        
    }//end inner class Circle
            
    
    static class Ellipse extends SCoordContentImpl
            implements SCoordContent.Ellipse {
                
        Ellipse(KeyObject owner, Date obsDateTime, Template template, 
                Code name, float[] graphicData) {
            super(owner, obsDateTime, template, name, graphicData);
            if (graphicData.length != 8)
                throw new IllegalArgumentException("float[" 
                                                   + graphicData.length + "]");
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new SCoordContentImpl.Ellipse(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, graphicData);
        }//end clone()

        public String getGraphicType() {  return "ELLIPSE";  }        
    }//end inner class Ellipse
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    protected final float[] graphicData;

    // Constructors --------------------------------------------------
    SCoordContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, float[] graphicData) {
        super(owner, obsDateTime, template, name);
        this.graphicData = (float[])graphicData.clone();
    }//end generic SCoordContentImpl constructor
    
    // Methodes --------------------------------------------------------
    public String toString() {
        StringBuffer sb = prompt().append(getGraphicType()).append(":[");
        if (graphicData.length > 8) {
            sb.append("N=").append(graphicData.length);
        } else {
            sb.append(graphicData[0]);
            for (int i = 1; i < graphicData.length; ++i) {
                sb.append(',').append(graphicData[i]);
            }
        }
        return sb.append("]").toString();
    }//end toString()

    
    public ValueType getValueType() {
        return ValueType.SCOORD;
    }//end getValueType()
    
    
    public float[] getGraphicData() {
        return (float[])graphicData.clone();
    }//end getGraphicData()


    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        ds.putCS(Tags.GraphicType, getGraphicType());
        ds.putFL(Tags.GraphicData, graphicData);
    }
}//end class SCoordContentImpl
