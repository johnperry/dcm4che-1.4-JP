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
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;

//java imports
import java.util.Date;


/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
abstract class TCoordContentImpl extends NamedContentImpl
        implements org.dcm4che.srom.TCoordContent {

            
    static class Point extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.Point {
                
        Point(KeyObject owner, Date obsDateTime, Template template, 
              Code name, Positions positions) {
            super(owner, obsDateTime, template, name, positions);
            if (positions.size() != 1) {
                throw new IllegalArgumentException("" + positions);
            }
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.Point(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "POINT";  }        
    }//end inner class Point

    
    static class MultiPoint extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.MultiPoint {
                
        MultiPoint(KeyObject owner, Date obsDateTime, Template template, 
                   Code name, Positions positions) {
            super(owner, obsDateTime, template, name, positions);
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.MultiPoint(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "MULTIPOINT";  }        
    }//end inner class MultiPoint

    
    static class Segment extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.Segment {
                
        Segment(KeyObject owner, Date obsDateTime, Template template, 
                Code name, Positions positions) {
            super(owner, obsDateTime, template, name, positions);
            if (positions.size() != 2) {
                throw new IllegalArgumentException("" + positions);
            }
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.Segment(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "SEGMENT";  }        
    }//end inner class Segment

    
    static class MultiSegment extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.MultiSegment {
                
        MultiSegment(KeyObject owner, Date obsDateTime, Template template, 
                     Code name, Positions positions) {
            super(owner, obsDateTime, template, name, positions);
            
            if ((positions.size() & 1) != 0) {
                throw new IllegalArgumentException("" + positions);
            }
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.MultiSegment(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "MULTISEGMENT";  }        
    }//end inner class MultiSegment

    
    static class Begin extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.Begin {
                
        Begin(KeyObject owner, Date obsDateTime, Template template, Code name,
              Positions positions) {
            super(owner, obsDateTime, template, name, positions);
            if (positions.size() != 1) {
                throw new IllegalArgumentException("" + positions);
            }
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.Begin(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "BEGIN";  }        
    }//end inner class Begin

    
    static class End extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.End {
                
        End(KeyObject owner, Date obsDateTime, Template template, Code name,
            Positions positions) {
            super(owner, obsDateTime, template, name, positions);
            if (positions.size() != 1) {
                throw new IllegalArgumentException("" + positions);
            }
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.End(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "END";  }        
    }//end inner class End

    //-- Positions ----------------------------------------------------
    static Positions newPositions(Dataset ds) throws DcmValueException {
        DcmElement e;
        if ((e = ds.get(Tags.RefSamplePositions)) != null) {
            return new SamplePositions(e.getInts());
        }
        if ((e = ds.get(Tags.RefTimeOffsets)) != null) {
            return new RelativePositions(e.getFloats());
        }
        if ((e = ds.get(Tags.RefDatetime)) != null) {
            return new AbsolutePositions(e.getDates());
        }
        throw new IllegalArgumentException("Missing Positions");
    }
    
    static class SamplePositions
            implements org.dcm4che.srom.TCoordContent.Positions.Sample {
        private final int[] indexes;
        
        SamplePositions(int[] indexes) {
            this.indexes = (int[])indexes.clone();
        }//end constructor
        
        public int size() { return indexes.length; }
        
        public int[] getIndexes() {
            return (int[])indexes.clone();
        }//end getIndexes()
        
        public String toString() {
            return "Sample[" + size() + "]";
        }
        
        public void toDataset(Dataset ds) {
            ds.putUL(Tags.RefSamplePositions, indexes);
        }
    }//end inner class SamplePositions

    
    static class RelativePositions
            implements org.dcm4che.srom.TCoordContent.Positions.Relative {
        private final float[] offsets;
        
        RelativePositions(float[] offsets) {
            this.offsets = (float[])offsets.clone();
        }//end constructor
        
        public int size() { return offsets.length; }
        
        public float[] getOffsets() {
            return (float[])offsets.clone();
        }//end getOffsets()
        
        public String toString() {
            return "Offset[" + size() + "]";
        }
        
        public void toDataset(Dataset ds) {
            ds.putDS(Tags.RefTimeOffsets, offsets);
        }
    }//end inner class RelativePositions

    
    static class AbsolutePositions
            implements org.dcm4che.srom.TCoordContent.Positions.Absolute {
        private final long[] dateTimes;
        
        AbsolutePositions(Date[] dateTimes) {
            this.dateTimes = new long[dateTimes.length];
            for (int i = 0; i < dateTimes.length; ++i) {
                this.dateTimes[i] = dateTimes[i].getTime();
            }
        }//end constructor
        
        public int size() { return dateTimes.length; }
        
        public Date[] getDateTimes() {
            Date[] dts = new Date[dateTimes.length];
            for (int i = 0; i < dateTimes.length; ++i) {
                dts[i] = new Date(dateTimes[i]);
            }
            return dts;
        }//end getDateTimes()
    
        public String toString() {
            return "DateTime[" + size() + "]";
        }
        
        public void toDataset(Dataset ds) {
            ds.putDT(Tags.RefDatetime, getDateTimes());
        }
    }//end inner class AbsolutePositions

    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    protected final Positions positions;

    // Constructors --------------------------------------------------

    // Constructors --------------------------------------------------
    TCoordContentImpl(KeyObject owner, Date obsDateTime, Template template,
                      Code name, Positions positions) {
        super(owner, obsDateTime, template, name);
        this.positions = positions;
    }//end generic TCoordContentImpl constructor

    
    // Methodes --------------------------------------------------------
    
    public final ValueType getValueType() {
        return ValueType.TCOORD;
    }//end getValueType()
    
    public final Positions getPositions() {
        return positions;
    }//end getPositions()

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        ds.putCS(Tags.TemporalRangeType, getRangeType());
        positions.toDataset(ds);
    }
}//end abstract class TCoordContentImpl
