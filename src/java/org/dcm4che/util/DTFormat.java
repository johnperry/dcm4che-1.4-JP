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

package org.dcm4che.util;

import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Calendar;

/**
 * <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 14962 $ $Date: 2011-02-25 14:23:59 +0100 (Fr, 25 Feb 2011) $
 * @since September 21, 2002
 */
public class DTFormat extends SimpleDateFormat {
    
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    
    // Constructors --------------------------------------------------
    public DTFormat() {
        super("yyyyMMddHHmmss.SSS");
    }
    
    // Methods -------------------------------------------------------
    public Date parse(String source, ParsePosition pos) {        
        calendar.clear();
        int p = 0;
        try {
            String s = parseTZ(source);
            int l = s.length();
            calendar.set(Calendar.YEAR,
                Integer.parseInt(s.substring(p,p+4)));
            p += 4;
            if (l > p) {
                if (!Character.isDigit(s.charAt(p))) {
                    ++p;
                }
                calendar.set(Calendar.MONTH,
                    Integer.parseInt(s.substring(p,p+2)) - 1);
                p += 2;
                if (l > p) {
                    if (!Character.isDigit(s.charAt(p))) {
                        ++p;
                    }
                    calendar.set(Calendar.DAY_OF_MONTH,
                        Integer.parseInt(s.substring(p,p+2)));
                    p += 2;
                    if (l > p) {
                        calendar.set(Calendar.HOUR_OF_DAY,
                            Integer.parseInt(s.substring(p,p+2)));
                        p += 2;
                        if (l > p) {
                            if (s.charAt(p) == ':') {
                                ++p;
                            }
                            calendar.set(Calendar.MINUTE,
                                Integer.parseInt(s.substring(p,p+2)));
                            p+=2;
                            if (l > p) {
                                if (s.charAt(p) == ':') {
                                    ++p;
                                }
                                float f = Float.parseFloat(s.substring(p));
                                int i = (int) f;
                                calendar.set(Calendar.SECOND, i);
                                calendar.set(Calendar.MILLISECOND,
                                    (int) (1000 * (f - i)));
                            }
                        }
                    }
                }
            }            
            pos.setIndex(source.length());
            return calendar.getTime();
        } catch (Exception e) {
            pos.setErrorIndex(p);
            return null;
        }
    }
    
    private String parseTZ(String source) {
        int zpos = source.length() - 5;
        if (zpos >= 0) {
            char ch = source.charAt(zpos);
            if (ch == '+' || ch == '-') {
                int off = Integer.parseInt(source.substring(zpos+1));                
                calendar.set(Calendar.ZONE_OFFSET, ch == '-' ? -off : off);
                calendar.set(Calendar.DST_OFFSET, 0);
                return source.substring(0, zpos);
            }
        }
        return source;
    }    
}
