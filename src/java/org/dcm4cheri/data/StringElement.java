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

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.PersonName;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.VRs;
import org.dcm4che.util.DAFormat;
import org.dcm4che.util.DTFormat;
import org.dcm4che.util.TMFormat;
import org.dcm4cheri.util.StringUtils;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author     <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since      May, 2002
 * @version    $Revision: 14714 $ $Date: 2011-01-17 11:04:17 +0100 (Mo, 17 Jän 2011) $
 */
abstract class StringElement extends ValueElement {
    private static final String[] STRING0 = {};
    private static final long MS_PER_DAY = 24 * 3600000L;

    static Logger log = Logger.getLogger(StringElement.class);

    private interface Trim {
        public String trim(String s);

        public int begin(ByteBuffer data);

        public int end(ByteBuffer data);

    }

    private final static Trim NO_TRIM = new Trim() {
        public String trim(String s) {
            return s;
        }

        public int begin(ByteBuffer data) {
            return 0;
        }

        public int end(ByteBuffer data) {
            return data.limit();
        }

    };

    private final static Trim TRAIL_TRIM = new Trim() {
        public String trim(String s) {
            char ch;
            int r = s.length();
            while (r > 0) {
                if ((ch = s.charAt(--r)) != '\0' && ch != ' ') {
                    return s.substring(0, r+1);
                }
            }
            return "";
        }

        public int begin(ByteBuffer data) {
            return 0;
        }

        public int end(ByteBuffer data) {
            int ch;
            int r = data.limit();
            while (r > 0) {
                if ((ch = data.get(--r)) !='\0' && ch != ' ') {
                    return r + 1;
                }
            }
            return 0;
        }

    };

    private final static Trim PN_TRIM = new Trim() {
        public String trim(String s) {
            char ch;
            int r = s.length();
            while (r > 0) {
                if ((ch = s.charAt(--r)) != '\0' && ch != ' ' && ch != '^') {
                    return s.substring(0, r+1);
                }
            }
            return "";
        }

        public int begin(ByteBuffer data) {
            return 0;
        }

        public int end(ByteBuffer data) {
            int ch;
            int r = data.limit();
            while (r > 0) {
                if ((ch = data.get(--r)) !='\0' && ch != ' ' && ch != '^') {
                    return r + 1;
                }
            }
            return 0;
        }

    };

    private final static Trim TOT_TRIM = new Trim() {
        public String trim(String s) {
            char ch;
            int r = s.length();
            while (r > 0) {
                if ((ch = s.charAt(--r)) != '\0' && ch != ' ') {
                    int l = 0;
                    while (l <= r) {
                        if (s.charAt(l++) != ' ') {
                            return s.substring(l-1, r+1);
                        }
                    }
                }
            }
            return "";
        }


        public int begin(ByteBuffer data) {
            int r = data.limit();
            int l = 0;
            while (l < r) {
                if (data.get(l++) != ' ') {
                    return l-1;
                }
            }
            return 0;
        }

        public int end(ByteBuffer data) {
            int ch;
            int r = data.limit();
            while (r > 0) {
                if ((ch = data.get(--r)) !='\0' && ch != ' ') {
                    return r + 1;
                }
            }
            return 0;
        }

    };

    private interface Check {
        public String check(String s);
    }

    private final static Check NO_CHECK = new Check() {
        public String check(String s) {
            return s;
        }
    };

    private static class CheckImpl implements Check {
        protected final int maxLen;
        protected final boolean text;

        CheckImpl(int maxLen, boolean text) {
            this.maxLen = maxLen;
            this.text = text;
        }

        public String check(String s) {
            char[] a = s.toCharArray();
            if (a.length > maxLen) {
                log.warn("Value: " + s + " exeeds VR length limit: " + maxLen);
            }
            for (int i = 0; i < a.length; ++i) {
                if (!check(a[i])) {
                    log.warn("Illegal character '" + a[i] + "' in value: " + s);
                }
            }
            return s;
        }

        protected boolean check(char c) {
            return !Character.isISOControl(c) || text && isDICOMControl(c);
        }
    }

    private static boolean isDICOMControl(char c) {
        switch (c) {
            case '\n' :
            case '\f' :
            case '\r' :
            case '\033' :
                return true;
        }
        return false;
    }

    private static ByteBuffer toByteBuffer(String value, Trim trim, Check check,
        SpecificCharacterSet cs) {
        if (value == null || (value = trim.trim(value)).length() == 0) {
            return EMPTY_VALUE;
        }
        check.check(value);
        return ByteBuffer.wrap(cs == null ? value.getBytes() : cs.encode(value));
    }

    private static ByteBuffer toByteBuffer(
        String[] values,
        Trim trim,
        Check check,
        SpecificCharacterSet cs) {
        if (values.length == 0) {
            return EMPTY_VALUE;
        }
        if (values.length == 1) {
            return toByteBuffer(values[0], trim, check, cs);
        }
        String[] ss = new String[values.length];
        for (int i = 0; i < ss.length; i++) {
            ss[i] = check.check(trim.trim(values[i]));
        }
        String s = StringUtils.toString(ss, '\\');
        return ByteBuffer.wrap(cs == null ? s.getBytes() : cs.encode(s));
    }

    protected final Trim trim;

    StringElement(int tag, ByteBuffer data, Trim trim) {
        super(tag, data);
        this.trim = trim;
    }

    public int hashCode() {
        if (data == null)
            return tag;
        return tag ^ hashCode(data, trim.begin(data), trim.end(data));
    }

    private int hashCode(ByteBuffer data, int begin, int end) {
        if (begin == end) {
            return 0;
        }
        int h = 1;
        int p = begin;
        for (int i = end - 1; i >= p; i--)
            h = 31 * h + data.get(i);
        return h;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StringElement))
            return false;
        StringElement se = (StringElement) o;
        if (tag != se.tag)
            return false;
        if (data == null)
            return (se.data == null 
                    || trim.begin(se.data) == trim.end(se.data));
        if (se.data == null)
            return trim.begin(data) == trim.end(data);
        return equals(data, trim.begin(data), trim.end(data),
                se.data, trim.begin(se.data), trim.end(se.data));
    }

    private boolean equals(ByteBuffer data1, int begin1, int end1,
            ByteBuffer data2, int begin2, int end2) {
        if (end1 - begin1 != end2 - begin2)
            return false;
        for (int i = end1 - 1, j = end2 - 1; i >= begin1; i--, j-- ) {
            if (data1.get(i) != data2.get(j))
                return false;
        }
        return true;
    }

    public String getString(int index, SpecificCharacterSet cs)
        throws DcmValueException {
        ByteBuffer bb = getByteBuffer();
        if (bb == null) {
            return null;
        }
        byte[] b = bb.array();
        return trim.trim(cs == null ? new String(b) : cs.decode(b));
    }

    public String[] getStrings(SpecificCharacterSet cs)
        throws DcmValueException {
        String s = getString(0, cs);
        return s == null ? STRING0 : new String[] { s };
    }

    private static boolean isUniversalMatch(String p) {
        if (p == null) {
            return true;
        }
        for (int i = 0, n = p.length(); i < n; ++i) {
            if (p.charAt(i) != '*') {
                return false;
            }
        }
        return true;
    }

    protected boolean matchValue(
        DcmElement key,
        boolean ignorePNCase,
        boolean ignoreEmpty,
        SpecificCharacterSet keyCS,
        SpecificCharacterSet dsCS) {
        String[] patterns, values;
        if (isEmpty()) {
            return true;
        }
        try {
            values = getStrings(dsCS);
        } catch (DcmValueException e) {
            // Illegal Value match always (like null value)
            return true;
        }
        try {
            patterns = key.getStrings(keyCS);
        } catch (DcmValueException e) {
            throw new IllegalArgumentException("key: " + key);
        }
        for (int i = 0; i < patterns.length; ++i) {
            if (isUniversalMatch(patterns[i])) {
                return true;
            }
            for (int j = 0; j < values.length; ++j) {
                if (ignorePNCase
                    && vr() == VRs.PN
                        ? match(patterns[i].toUpperCase(), values[j].toUpperCase())
                        : match(patterns[i], values[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean match(String pattern, String input) {
        if (pattern.indexOf('*') == -1 && pattern.indexOf('?') == -1) {
            return pattern.equals(input);
        }
        return Pattern.matches(toRegEx(pattern), input);
    }

    private static String toRegEx(String pattern) {
        char[] a = pattern.toCharArray();
        StringBuffer sb = new StringBuffer(a.length + 10);
        boolean inQuote = false;
        for (int i = 0; i < a.length; ++i) {
            if (a[i] == '*' || a[i] == '?') {
                if (inQuote) {
                    sb.append('\\').append('E');
                    inQuote = false;
                }
                sb.append('.');
                if (a[i] == '*') {
                    sb.append('*');
                }
            } else {
                if (!inQuote) {
                    sb.append('\\').append('Q');
                    inQuote = true;
                }
                sb.append(a[i]);
            }
        }
        if (inQuote) {
            sb.append('\\').append('E');
        }
        return sb.toString();
    }

    // LT -------------------------------------------------------------
    private final static class LT extends StringElement {
        LT(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x4C54;
        }
    }

    private final static Check LT_CHECK = new CheckImpl(10240, true);

    static DcmElement createLT(int tag, ByteBuffer data) {
        return new LT(tag, data);
    }

    static DcmElement createLT(int tag) {
        return new LT(tag, EMPTY_VALUE);
    }

    static DcmElement createLT(int tag, String value, SpecificCharacterSet cs) {
        return new LT(tag, toByteBuffer(value, TRAIL_TRIM, LT_CHECK, cs));
    }

    static DcmElement createLT(int tag, String[] values, SpecificCharacterSet cs) {
        return new LT(tag, toByteBuffer(values, TRAIL_TRIM, LT_CHECK, cs));
    }

    // ST -------------------------------------------------------------
    private final static class ST extends StringElement {
        ST(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x5354;
        }
    }

    private final static Check ST_CHECK = new CheckImpl(1024, true);

    static DcmElement createST(int tag, ByteBuffer data) {
        return new ST(tag, data);
    }

    static DcmElement createST(int tag) {
        return new ST(tag, EMPTY_VALUE);
    }

    static DcmElement createST(int tag, String value, SpecificCharacterSet cs) {
        return new ST(tag, toByteBuffer(value, TRAIL_TRIM, ST_CHECK, cs));
    }

    static DcmElement createST(int tag, String[] values, SpecificCharacterSet cs) {
        return new ST(tag, toByteBuffer(values, TRAIL_TRIM, ST_CHECK, cs));
    }

    // UC -------------------------------------------------------------
    private final static class UC extends StringElement {
        UC(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x5543;
        }
    }

    final static Check UC_CHECK = new CheckImpl(Integer.MAX_VALUE, true);

    static DcmElement createUC(int tag, ByteBuffer data) {
        return new UC(tag, data);
    }

    static DcmElement createUC(int tag) {
        return new UC(tag, EMPTY_VALUE);
    }

    static DcmElement createUC(int tag, String value, SpecificCharacterSet cs) {
        return new UC(tag, toByteBuffer(value, TRAIL_TRIM, UC_CHECK, cs));
    }

    static DcmElement createUC(int tag, String[] values, SpecificCharacterSet cs) {
        return new UC(tag, toByteBuffer(values, TRAIL_TRIM, UC_CHECK, cs));
    }

    // UT -------------------------------------------------------------
    private final static class UT extends StringElement {
        UT(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x5554;
        }
    }

    final static Check UT_CHECK = new CheckImpl(Integer.MAX_VALUE, true);

    static DcmElement createUT(int tag, ByteBuffer data) {
        return new UT(tag, data);
    }

    static DcmElement createUT(int tag) {
        return new UT(tag, EMPTY_VALUE);
    }

    static DcmElement createUT(int tag, String value, SpecificCharacterSet cs) {
        return new UT(tag, toByteBuffer(value, TRAIL_TRIM, UT_CHECK, cs));
    }

    static DcmElement createUT(int tag, String[] values, SpecificCharacterSet cs) {
        return new UT(tag, toByteBuffer(values, TRAIL_TRIM, UT_CHECK, cs));
    }

    // MultiStringElements ---------------------------------------------------
    private final static byte DELIM = 0x5c;

    private abstract static class MultiStringElement extends StringElement {
 
        MultiStringElement(int tag, ByteBuffer data, Trim trim) {
            super(tag, data, trim);
        }

         public final int vm(SpecificCharacterSet cs) {
            if (data.limit() == 0) {
                return 0;
            }
            if (cs == null) {
                int vm = 1;
                byte[] a = data.array();
                for (int i = 0; i < a.length; i++) {
                    if (a[i] == DELIM) {
                        ++vm;
                    }
                }
                return vm;
            }
            return StringUtils.count(cs.decode(data.array()), '\\') + 1;
         }

         public String getString(SpecificCharacterSet cs)
             throws DcmValueException {
             if (isEmpty()) {
                 return null;
             }
             byte[] b = data.array();
             String s = trim.trim(cs == null ? new String(b) : cs.decode(b));
             int end = s.indexOf('\\');
             return end != -1 ? s.substring(0, end) : s;             
         }

        public String getString(int index, SpecificCharacterSet cs)
            throws DcmValueException {
            if (index == 0) {
                return getString(cs);
            }
            String[] ss = getStrings(cs);
            return index < ss.length ? ss[index] : null;
        }

        public String[] getStrings(SpecificCharacterSet cs)
            throws DcmValueException {
            if (isEmpty()) {
                return STRING0;
            }
            byte[] b = data.array();
            String s = trim.trim(cs == null ? new String(b) : cs.decode(b));
            return StringUtils.split(s, '\\');
        }
    }

    // LO ------------------------------------------------------------------
    private final static class LO extends MultiStringElement {
        LO(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4C4F;
        }
    }

    private final static Check LO_CHECK = new CheckImpl(64, false);

    static DcmElement createLO(int tag, ByteBuffer data) {
        return new LO(tag, data);
    }

    static DcmElement createLO(int tag) {
        return new LO(tag, EMPTY_VALUE);
    }

    static DcmElement createLO(int tag, String value, SpecificCharacterSet cs) {
        return new LO(tag, toByteBuffer(value, TOT_TRIM, LO_CHECK, cs));
    }

    static DcmElement createLO(int tag, String[] values, SpecificCharacterSet cs) {
        return new LO(tag, toByteBuffer(values, TOT_TRIM, LO_CHECK, cs));
    }

    // PN ------------------------------------------------------------------
    private final static class PN extends MultiStringElement {
        PN(int tag, ByteBuffer data) {
            super(tag, data, PN_TRIM);
        }

        public final int vr() {
            return 0x504E;
        }

        public final PersonName getPersonName(SpecificCharacterSet cs)
            throws DcmValueException {
            return new PersonNameImpl(getString(cs), true);
        }

        public final PersonName getPersonName(int index, SpecificCharacterSet cs)
            throws DcmValueException {
            return new PersonNameImpl(getString(index, cs), true);
        }

        public final PersonName[] getPersonNames(SpecificCharacterSet cs)
            throws DcmValueException {
            String[] ss = getStrings(cs);
            PersonName[] pns = new PersonName[ss.length];
            for (int i = 0; i < ss.length; i++) {
                pns[i] = new PersonNameImpl(ss[i], true);
            }
            return pns;
        }
    }

    static DcmElement createPN(int tag, ByteBuffer data) {
        return new PN(tag, data);
    }

    static DcmElement createPN(int tag) {
        return new PN(tag, EMPTY_VALUE);
    }

    static DcmElement createPN(int tag, PersonName value, SpecificCharacterSet cs) {
        return new PN(
            tag,
            toByteBuffer(value.toString(), NO_TRIM, NO_CHECK, cs));
    }

    static DcmElement createPN(int tag, PersonName[] values, SpecificCharacterSet cs) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = values[i].toString();
        }
        return new PN(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, cs));
    }

    // SH ------------------------------------------------------------------
    private final static class SH extends MultiStringElement {
        SH(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x5348;
        }
    }

    private final static Check SH_CHECK = new CheckImpl(16, false);

    static DcmElement createSH(int tag, ByteBuffer data) {
        return new SH(tag, data);
    }

    static DcmElement createSH(int tag) {
        return new SH(tag, EMPTY_VALUE);
    }

    static DcmElement createSH(int tag, String value, SpecificCharacterSet cs) {
        return new SH(tag, toByteBuffer(value, TOT_TRIM, SH_CHECK, cs));
    }

    static DcmElement createSH(int tag, String[] values, SpecificCharacterSet cs) {
        return new SH(tag, toByteBuffer(values, TOT_TRIM, SH_CHECK, cs));
    }

    // AsciiMultiStringElements ----------------------------------------------
    private abstract static class AsciiMultiStringElement
        extends MultiStringElement {
        AsciiMultiStringElement(int tag, ByteBuffer data, Trim trim) {
            super(tag, data, trim);
        }

        public final String getString(int index, SpecificCharacterSet cs)
            throws DcmValueException {
            return super.getString(index, null);
        }
    }

    // AE ------------------------------------------------------------------
    private final static class AE extends AsciiMultiStringElement {
        AE(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4145;
        }
    }

    private final static Check AE_CHECK = new CheckImpl(16, false);

    static DcmElement createAE(int tag, ByteBuffer data) {
        return new AE(tag, data);
    }

    static DcmElement createAE(int tag) {
        return new AE(tag, EMPTY_VALUE);
    }

    static DcmElement createAE(int tag, String value) {
        return new AE(tag, toByteBuffer(value, TOT_TRIM, AE_CHECK, null));
    }

    static DcmElement createAE(int tag, String[] values) {
        return new AE(tag, toByteBuffer(values, TOT_TRIM, AE_CHECK, null));
    }

    // AS ------------------------------------------------------------------
    private final static class AS extends AsciiMultiStringElement {
        AS(int tag, ByteBuffer data) {
            super(tag, data, NO_TRIM);
        }

        public final int vr() {
            return 0x4153;
        }
    }

    private final static Check AS_CHECK = new Check() {
        public String check(String s) {
            if (s.length() == 4
                && Character.isDigit(s.charAt(0))
                && Character.isDigit(s.charAt(1))
                && Character.isDigit(s.charAt(2))) {
                switch (s.charAt(3)) {
                    case 'D' :
                    case 'W' :
                    case 'M' :
                    case 'Y' :
                        return s;
                }
            }
            log.warn("Illegal Age String: " + s);
            return s;
        }
    };

    static DcmElement createAS(int tag, ByteBuffer data) {
        return new AS(tag, data);
    }

    static DcmElement createAS(int tag) {
        return new AS(tag, EMPTY_VALUE);
    }

    static DcmElement createAS(int tag, String value) {
        return new AS(tag, toByteBuffer(value, NO_TRIM, AS_CHECK, null));
    }

    static DcmElement createAS(int tag, String[] values) {
        return new AS(tag, toByteBuffer(values, NO_TRIM, AS_CHECK, null));
    }

    // CS ------------------------------------------------------------------
    private final static class CS extends AsciiMultiStringElement {
        CS(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4353;
        }
    }

    final static Check CS_CHECK = new CheckImpl(16, false) {
        protected boolean check(char c) {
            return (
                (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == ' '
                    || c == '_');
        }
    };

    static DcmElement createCS(int tag, ByteBuffer data) {
        return new CS(tag, data);
    }

    static DcmElement createCS(int tag) {
        return new CS(tag, EMPTY_VALUE);
    }

    static DcmElement createCS(int tag, String v) {
        return new CS(tag, toByteBuffer(v, TOT_TRIM, CS_CHECK, null));
    }

    static DcmElement createCS(int tag, String[] a) {
        return new CS(tag, toByteBuffer(a, TOT_TRIM, CS_CHECK, null));
    }

    // DS ------------------------------------------------------------------
    final static Check DS_CHECK = new Check() {
        public String check(String s) {
            try {
                Float.parseFloat(s);
                if (s.length() > 16) {
                    log.warn("DS Value: " + s + " exeeds DS length limit: 16");
                }
            } catch (NumberFormatException e) {
                log.warn("Illegal DS Value: " + s);
            }
            return s;
        }
    };

    private final static class DS extends AsciiMultiStringElement {
        DS(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4453;
        }

        public final float getFloat(int index) throws DcmValueException {
            return Float.parseFloat(commaToPeriod(super.getString(index, null)));
        }

        public final float[] getFloats() throws DcmValueException {
            String[] ss = super.getStrings(null);
            float[] retval = new float[ss.length];
            for (int i = 0; i < retval.length; ++i) {
                retval[i] = Float.parseFloat(commaToPeriod(ss[i]));
            }
            return retval;
        }

        private static String commaToPeriod(String ds) {
            String s = ds.replace(',', '.');
            if (s != ds) {
                log.warn("Illegal DS value: " + ds);
            }
            return s;
        }
    }

    static DcmElement createDS(int tag, ByteBuffer data) {
        return new DS(tag, data);
    }

    static DcmElement createDS(int tag) {
        return new DS(tag, EMPTY_VALUE);
    }

    static DcmElement createDS(int tag, float value) {
        return new DS(
            tag,
            toByteBuffer(String.valueOf(value), NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDS(int tag, float[] values) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = String.valueOf(values[i]);
        }
        return new DS(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDS(int tag, String v) {
        return new DS(tag, toByteBuffer(v, TOT_TRIM, DS_CHECK, null));
    }

    static DcmElement createDS(int tag, String[] a) {
        return new DS(tag, toByteBuffer(a, TOT_TRIM, DS_CHECK, null));
    }

    // IS ------------------------------------------------------------------
    final static Check IS_CHECK = new Check() {
        public String check(String s) {
            try {
                parseIS(s);
                if (s.length() > 12) {
                    log.warn("IS Value: " + s + " exeeds IS length limit: 12");
                }
            } catch (NumberFormatException e) {
                log.warn("Illegal IS Value: " + s);
            }
            return s;
        }
    };

    private final static class IS extends AsciiMultiStringElement {
        IS(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4953;
        }

        public final int getInt(int index) throws DcmValueException {
            String s = super.getString(index, null);
            try {
                return parseIS(s);
            } catch (NumberFormatException ex) {
                throw new DcmValueException(s, ex);
            }
        }

        public final int[] getInts() throws DcmValueException {
            String[] ss = super.getStrings(null);
            int[] retval = new int[ss.length];
            for (int i = 0; i < retval.length; ++i) {
                retval[i] = parseIS(ss[i]);
            }
            return retval;
        }
    }

    private static int parseIS(String s) {
        return (int) Long.parseLong(s.startsWith("+") ? s.substring(1) : s);
    }

    static DcmElement createIS(int tag, ByteBuffer data) {
        return new IS(tag, data);
    }

    static DcmElement createIS(int tag) {
        return new IS(tag, EMPTY_VALUE);
    }

    static DcmElement createIS(int tag, int value) {
        return new IS(
            tag,
            toByteBuffer(String.valueOf(value), NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createIS(int tag, int[] values) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = String.valueOf(values[i]);
        }
        return new IS(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createIS(int tag, String v) {
        return new IS(tag, toByteBuffer(v, TOT_TRIM, IS_CHECK, null));
    }

    static DcmElement createIS(int tag, String[] a) {
        return new IS(tag, toByteBuffer(a, TOT_TRIM, IS_CHECK, null));
    }

    // UI ------------------------------------------------------------------
    private final static class UI extends AsciiMultiStringElement {
        UI(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x5549;
        }
    }

    private final static int UID_DIGIT1 = 0;
    private final static int UID_DIGIT = 1;
    private final static int UID_DOT = 2;
    private final static int UID_ERROR = -1;

    private static int nextState(int state, char c) {
        switch (state) {
            case UID_DIGIT1 :
                if (c > '0' && c <= '9') {
                    return UID_DIGIT;
                }
                if (c == '0') {
                    return UID_DOT;
                }
                return UID_ERROR;
            case UID_DIGIT :
                if (c >= '0' && c <= '9') {
                    return UID_DIGIT;
                }
                // fall through
            case UID_DOT :
                if (c == '.') {
                    return UID_DIGIT1;
                }
        }
        return UID_ERROR;
    }

    private final static Check UI_CHECK = new CheckImpl(64, false) {
        public String check(String s) {
            char[] a = s.toCharArray();
            if (a.length > maxLen) {
                log.warn("Value: " + s + " exeeds VR length limit: " + maxLen);
            }
            int state = UID_DIGIT1;
            for (int i = 0; i < a.length; ++i) {
                if ((state = nextState(state, a[i])) == UID_ERROR) {
                    log.warn("Illegal UID value: " + s);
                    return s;
                }
            }
            if (state == UID_DIGIT1) {
                log.warn("Illegal UID value: " + s);
            }
            return s;
        }
    };

    static DcmElement createUI(int tag, ByteBuffer data) {
        return new UI(tag, data);
    }

    static DcmElement createUI(int tag) {
        return new UI(tag, EMPTY_VALUE);
    }

    static DcmElement createUI(int tag, String value) {
        return new UI(tag, toByteBuffer(value, NO_TRIM, UI_CHECK, null));
    }

    static DcmElement createUI(int tag, String[] values) {
        return new UI(tag, toByteBuffer(values, NO_TRIM, UI_CHECK, null));
    }

    // DA ----------------------------------------------
    private final static byte HYPHEN = 0x2d;

    private abstract static class DateString extends AsciiMultiStringElement {
        DateString(int tag, ByteBuffer data, Trim trim) {
            super(tag, data, trim);
        }

        public final boolean isDataRange() {
            for (int i = 0, n = data.limit(); i < n; ++i) {
                if (data.get(i) == HYPHEN) {
                    return true;
                }
            }
            return false;
        }

        private Date toDate(String s) throws DcmValueException {
            try {
                return parseDate(getFormat(), s);
            } catch (ParseException e) {
                throw new DcmValueException(s);
            }
        }

        public final Date getDate() throws DcmValueException {
            return toDate(super.getString(null));
        }

        public final Date getDate(int index) throws DcmValueException {
            return toDate(super.getString(index, null));
        }

        public final Date[] getDateRange() throws DcmValueException {
            String s = super.getString(null);
            try {
                return parseDateRange(getFormat(), s, getTimeResolution());
            } catch (ParseException e) {
                throw new DcmValueException(s);
            }
        }

        public final Date[] getDates() throws DcmValueException {
            String[] ss = super.getStrings(null);
            Date[] a = new Date[ss.length];
            for (int i = 0; i < a.length; ++i) {
                a[i] = toDate(ss[i]);
            }
            return a;
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
            boolean ignoreEmpty,
            SpecificCharacterSet keyCS,
            SpecificCharacterSet dsCS) {
            String[] keys;
            try {
                keys = key.getStrings(null);
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0; i < keys.length; ++i) {
                Date[] range;
                try {
                    range = parseDateRange(getFormat(), keys[i],
                            getTimeResolution());
                } catch (ParseException e1) {
                    throw new IllegalArgumentException("key: " + key);
                }
                long from =
                    range[0] != null ? range[0].getTime() : Long.MIN_VALUE;
                long to =
                    range[1] != null ? range[1].getTime() : Long.MAX_VALUE;
                try {
                    Date[] values = getDates();
                    for (int j = 0; j < values.length; ++j) {
                        if (values[i] == null) {
                            return true;
                        }
                        final long time = values[i].getTime();
                        if (time >= from && time <= to) {
                            return true;
                        }
                    }
                } catch (DcmValueException e) {
                    return true;
                }
            }
            return false;
        }

        protected abstract DateFormat getFormat();
        
        protected long getTimeResolution() { return 1L; }

    }

    private static Date parseDate(DateFormat f, String s)
            throws ParseException {
        return s != null ? f.parse(s) : null;
    }

    private static Date[] parseDateRange(DateFormat f, String s,
            long resolution) throws ParseException {
        if (s == null || s.equals("*") ||  s.equals("-")) {
            return null;
        }
        Date[] range = new Date[2];
        int delim = s.indexOf('-');
        if (delim == -1) {
            range[0] = f.parse(s);
            range[1] = new Date(range[0].getTime() + resolution - 1);
        } else {
            if (delim > 0) {
                range[0] = f.parse(s.substring(0, delim));
            }
            if (delim + 1 < s.length()) {
                range[1] = new Date(f.parse(s.substring(delim + 1).trim())
                            .getTime() + resolution - 1);
            }
        }
        return range;
    }

    private final static class DA extends DateString {
        DA(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x4441;
        }

        protected DateFormat getFormat() {
            return new DAFormat();
        }
        
        protected long getTimeResolution() { 
        	return MS_PER_DAY;
        }

    }

    static DcmElement createDA(int tag, ByteBuffer data) {
        return new DA(tag, data);
    }

    static DcmElement createDA(int tag) {
        return new DA(tag, EMPTY_VALUE);
    }

    private static String toString(DateFormat f, Date d) {
        return d == null ? null : f.format(d);
    }

    private static String toString(DateFormat f, Date from, Date to) {
        StringBuffer sb = new StringBuffer(64);
        if (from != null) {
            sb.append(f.format(from));
        }
        sb.append('-');
        if (to != null) {
            sb.append(f.format(to));
        }
        return sb.toString();
    }

    static DcmElement createDA(int tag, Date value) {
        return new DA(
            tag,
            toByteBuffer(
                toString(new DAFormat(), value),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createDA(int tag, Date[] values) {
        DAFormat f = new DAFormat();
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = toString(f, values[i]);
        }
        return new DA(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDA(int tag, Date from, Date to) {
        return new DA(
            tag,
            toByteBuffer(
                toString(new DAFormat(), from, to),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createDA(int tag, String value) {
        checkDate(new DAFormat(), value);
        return new DA(tag, toByteBuffer(value, NO_TRIM, NO_CHECK, null));
    }

    static void checkDate(DateFormat f, String value) {
        try {
            if (value.indexOf('-') != -1) {
                parseDateRange(f, value, MS_PER_DAY);
            } else {
                parseDate(f, value);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(value);
        }
    }

    static DcmElement createDA(int tag, String[] values) {
        checkDates(new DAFormat(), values);
        return new DA(tag, toByteBuffer(values, NO_TRIM, NO_CHECK, null));
    }

    static void checkDates(DateFormat f, String[] values) {
        for (int i = 0; i < values.length; ++i) {
            try {
                parseDate(f, values[i]);
            } catch (ParseException e) {
                throw new IllegalArgumentException(values[i]);
            }
        }
    }

    private final static class DT extends DateString {
        DT(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x4454;
        }

        protected DateFormat getFormat() {
            return new DTFormat();
        }
    }

    static DcmElement createDT(int tag, ByteBuffer data) {
        return new DT(tag, data);
    }

    static DcmElement createDT(int tag) {
        return new DT(tag, EMPTY_VALUE);
    }

    static DcmElement createDT(int tag, Date value) {
        return new DT(
            tag,
            toByteBuffer(
                toString(new DTFormat(), value),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createDT(int tag, Date[] values) {
        DTFormat f = new DTFormat();
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = toString(f, values[i]);
        }
        return new DT(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDT(int tag, Date from, Date to) {
        return new DT(
            tag,
            toByteBuffer(
                toString(new DTFormat(), from, to),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createDT(int tag, String value) {
        checkDate(new DTFormat(), value);
        return new DT(tag, toByteBuffer(value, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDT(int tag, String[] values) {
        checkDates(new DTFormat(), values);
        return new DT(tag, toByteBuffer(values, NO_TRIM, NO_CHECK, null));
    }

    private final static class TM extends DateString {
        TM(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x544D;
        }

        protected DateFormat getFormat() {
            return new TMFormat();
        }
    }

    static DcmElement createTM(int tag, ByteBuffer data) {
        return new TM(tag, data);
    }

    static DcmElement createTM(int tag) {
        return new TM(tag, EMPTY_VALUE);
    }

    static DcmElement createTM(int tag, Date value) {
        return new TM(
            tag,
            toByteBuffer(
                toString(new TMFormat(), value),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createTM(int tag, Date[] values) {
        TMFormat f = new TMFormat();
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = toString(f, values[i]);
        }
        return new TM(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createTM(int tag, Date from, Date to) {
        return new TM(
            tag,
            toByteBuffer(
                toString(new TMFormat(), from, to),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createTM(int tag, String value) {
        checkDate(new TMFormat(), value);
        return new TM(tag, toByteBuffer(value, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createTM(int tag, String[] values) {
        checkDates(new TMFormat(), values);
        return new TM(tag, toByteBuffer(values, NO_TRIM, NO_CHECK, null));
    }

}
