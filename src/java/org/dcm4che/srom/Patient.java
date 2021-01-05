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
 * The <code>Patient</code> interface represents some of the fields of the
 * <i>DICOM Patient Module</i>.
 * 
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.7.1.1 Patient Module"
 */
public interface Patient {        
    // Constants -----------------------------------------------------
    
    /**
     * Inner static class that represents a enumeration of 
     * the patient sex.
     */
    public static final class Sex {
        private String type;
        
        /** Female patient sex. */
        public final static Sex FEMALE = new Sex("F");
        
        /** Male patient sex. */
        public final static Sex MALE = new Sex("M");
        
        /** 
         * Other patient sex. 
         * Used if sex of patient is unknown or something else that
         * male or female.
         */ 
        public final static Sex OTHER = new Sex("O");
        
        /**
         * Constructor of patient sex.
         * 
         * @param type  a String character code specifying the patient sex.
         *              Allowed values are "M" for male "F" for female
         *              "O" for other.
         */
        private Sex(String type) {
            this.type = type;
        }//end constructor
        
        /**
         * Returns the text representation of patient sex.
         */
        public String toString() { return type; }
        
        /**
         * Returns the type save patient sex value of a specified String.
         * 
         * @param s  the patient sex as string.
         * @throws IllegalArgumentException  if parameter <code>s</code>
         * is not "M", "F" or "O".
         */
        public static Sex valueOf(String s) {
            if (s == null || s.length() == 0)
                return null;
                
            if (s.length() == 1)
                switch (s.charAt(0)) {
                    case 'F':
                        return FEMALE;
                    case 'M':
                        return MALE;
                    case 'O':
                        return OTHER;
                }
             throw new IllegalArgumentException(s);
        }//end valueOf()
        
    }//end inner class Sex
        
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the DICOM <i>Patient ID</i>.
     * <br>DICOM Tag: <code>(0010,0020)</code>
     *
     * @return  the Patient ID.
     */
    public String getPatientID();
    
    /**
     * Returns the DICOM <i>Patient Name</i>.
     * <br>DICOM Tag: <code>(0010,0010)</code>
     *
     * @return  the Patient Name.
     */
    public String getPatientName();
    
    /**
     * Returns the DICOM <i>Patient's Sex</i>.
     * <br>DICOM Tag: <code>(0010,0040)</code>
     *
     * @return  the Patient's Sex.
     */
    public Sex getPatientSex();
    
    /**
     * Returns the DICOM <i>Patient's Birth Date</i>.
     * <br>DICOM Tag: <code>(0010,0030)</code>
     * 
     * @return  the Patient's Birth Date.
     */
    public Date getPatientBirthDate();
    
    public void toDataset(Dataset ds);
}//end interface Patient
