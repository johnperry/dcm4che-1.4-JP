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

package org.dcm4cheri.data;

import org.apache.log4j.Logger;
import org.dcm4che.data.PersonName;

import java.util.Arrays;
import java.util.StringTokenizer;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class PersonNameImpl implements org.dcm4che.data.PersonName {
    private static final int[] FORMAT_ORDER = { 
        PREFIX, GIVEN, MIDDLE, FAMILY, SUFFIX
    };
    private static final Logger log = Logger.getLogger(PersonNameImpl.class);

    private final String[] components = new String[5];
    private PersonNameImpl ideographic;
    private PersonNameImpl phonetic;  

    public PersonNameImpl() {}
    
    public PersonNameImpl(String s, boolean lenient) {
        if (s == null)
            return;
        int grLen = s.indexOf('=');
        if ((grLen == -1 ? s.length() : grLen) > 64) {
            if (lenient) {
                log.warn("To long PN: " + s);
            } else {
                throw new IllegalArgumentException(s);
            }
        }
        StringTokenizer stk = new StringTokenizer(s, "=^", true);
        int field = FAMILY;
        String tk;
        WHILE:
        while (stk.hasMoreTokens()) {
            tk = stk.nextToken();
            switch (tk.charAt(0)) {
                case '^':
                    if (++field > PersonName.SUFFIX) {
                        log.warn("Illegal PN: " + s + " - ignore '^' delimiter");
                        --field;
                    }
                    break;
                case '=':
                    break WHILE;
                default:
                    components[field] = tk;
                    break;
            }
        }
        if (!stk.hasMoreTokens())
            return;

        tk = stk.nextToken("=");
        if (tk.charAt(0) != '=' ) {
            ideographic = new PersonNameImpl(tk, lenient);
            if (stk.hasMoreTokens())
                tk = stk.nextToken("=");
        }
        if (!stk.hasMoreTokens())
            return;

        tk = stk.nextToken();
        if (tk.charAt(0) == '=' || stk.hasMoreTokens())
            throw new IllegalArgumentException(s);

        phonetic = new PersonNameImpl(tk, lenient);
    }

    public String get(int field) {
	return components[field];
    }
    
    public void set(int field, String value) {
	components[field] = value;
    }
    
    public PersonName getIdeographic() {
	return ideographic;
    }
    
    public PersonName getPhonetic() {
	return phonetic;
    }

    public void setIdeographic(PersonName ideographic) {
	this.ideographic = (PersonNameImpl)ideographic;
    }
    
    public void setPhonetic(PersonName phonetic) {
	this.phonetic = (PersonNameImpl)phonetic;
    }
    
    private StringBuffer appendComponents(StringBuffer sb, String nullmask, 
            boolean trim) {
        sb.append(maskNull(components[FAMILY], nullmask));
        sb.append('^');
        sb.append(maskNull(components[GIVEN], nullmask));
        sb.append('^');
        sb.append(maskNull(components[MIDDLE], nullmask));
        sb.append('^');
        sb.append(maskNull(components[PREFIX], nullmask));
        sb.append('^');
        sb.append(maskNull(components[SUFFIX], nullmask));
        if (trim) {
            int last = sb.length() - 1;
            while (last >= 0 && sb.charAt(last) == '^') {
                --last;
            }
            sb.setLength(last+1);
        }
        return sb;
    }
    
    private String maskNull(String val, String mask) {
        return val != null ? val : mask;
    }
    
    public String toComponentGroupString(boolean trim) {
        return toComponentGroupString("", trim);
    }

    public String toComponentGroupMatch() {
        return trimMatch(toComponentGroupString("*", false));
    }
    
    private String trimMatch(String val) {
        char[] a = val.toCharArray();
        int len = a.length;
        if (len < 3 || a[len-3] != '*' || a[len-2] != '^' || a[len-1] != '*') {
            return val;
        }
        do {
            len -= 2;
        } while (len >= 3 && a[len-3] == '*' && a[len-2] == '^' );     
        return val.substring(0, len);
    }

    private String toComponentGroupString(String nullMask, boolean trim) {
        StringBuffer sb = new StringBuffer();
        appendComponents(sb, nullMask, trim);
        return sb.toString();
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        appendComponents(sb, "", true);
        if (ideographic != null || phonetic != null) {
            sb.append('=');
            if (ideographic != null) {
                ideographic.appendComponents(sb, "", true);
            }
            if (phonetic != null) {
                sb.append('=');
                phonetic.appendComponents(sb, "", true);
            }
        }
        return sb.toString();
    }
    
    public String format() {
        return format(FORMAT_ORDER);
    }
    
    public String format(int[] fields) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fields.length; i++) {
            String s = get(fields[i]);
            if (s != null && s.length() > 0) {
                sb.append(s).append(' ');
            }
        }
        return sb.substring(0, Math.max(0, sb.length()-1));
    }

    public boolean equals(Object o) {
        if (!(o instanceof PersonNameImpl)) {
            return false;
        }
        
        PersonNameImpl other = (PersonNameImpl)o;
        return Arrays.equals(components, other.components);
    }
}
