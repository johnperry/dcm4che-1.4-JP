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

package org.dcm4che.data;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface PersonName {
    /**  
     * Field number for get and set indicating the family name complex 
     */
    public static final int FAMILY = 0;
  
    /**  
     * Field number for get and set indicating the given name complex 
     */
    public static final int GIVEN = 1;
  
    /**  
     * Field number for get and set indicating the middle name 
     */
    public static final int MIDDLE = 2;
  
    /**  
     * Field number for get and set indicating the name prefix 
     */
    public static final int PREFIX = 3;
  
    /**  
     * Field number for get and set indicating the name suffix 
     */
    public static final int SUFFIX = 4;
  
    /** 
     * Gets the value for a given name field.
     * 
     * @param field the given name field.
     * @return the value for the given name field
     */
    public String get(int field);

    /** 
     * Sets the name field with the given value.
     * 
     * @param field the given name field.
     * @param value the value to be set for the given name field.
     */
    public void set(int field, String value);
    
    /**
     * Returns ideographic representation.
     *
     * @return ideographic representation
     * */
    public PersonName getIdeographic();

    /** 
     * Returns phonetic representation.
     * 
     * @return phonetic representation
     */
    public PersonName getPhonetic();
    
    /** 
     * Sets ideographic representation.
     *
     * @param the ideographic representation to be set
     */
    public void setIdeographic(PersonName ideographic);
    
    /** 
     * Sets phonetic representation.
     *
     * @param the phonetic representation to be set
     */
    public void setPhonetic(PersonName phonetic);
    
    public String toString();

    public String toComponentGroupString(boolean trim);
    
    public String toComponentGroupMatch();
    
    public String format();
    
    public String format(int[] fields);
    
}

