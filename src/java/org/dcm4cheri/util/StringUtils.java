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

package org.dcm4cheri.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class StringUtils {

    /** Prevent instances of Utility class */
    private StringUtils() {
    }
    
    public static StringBuffer promptBytes(StringBuffer sb, byte[] data,
            int start, int length, int maxlen) {
        if (length == 0)
            return sb;
        Tags.toHexString(sb, data[start] & 0xff, 2);
        for (int i = start+1, remain = Math.min(length, (maxlen-2)/3);
               --remain > 0; ++i)
            Tags.toHexString(sb.append('\\'), data[i] & 0xff, 2);
        
        // if limited by maxlen
        if (sb.length() < 3 * length - 1) {
           sb.setLength(maxlen-2);
           sb.append("..");
        }
        return sb;
    }

    public static String promptBytes(byte[] data, int start, int length,
         int maxlen)
    {
        if (length == 0)
            return "";
        return promptBytes(new StringBuffer(Math.min(maxlen, length * 3 - 1)),
               data, start, length, maxlen).toString();
    }

    public static String promptBytes(byte[] data, int start, int length)
    {
       return promptBytes(data, start, length, Integer.MAX_VALUE);
    }
    
    public static String truncate(String val, int maxlen) {
       return val.length() > maxlen ? (val.substring(0, maxlen-2) + "..") : val;
    }
    
    static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");         
    public static String promptValue(int vr, ByteBuffer bb) {
       return promptValue(vr, bb, ISO_8859_1, Integer.MAX_VALUE);
    }
    
    public static String promptValue(int vr, ByteBuffer bb, Charset cs) {
       return promptValue(vr, bb, cs, Integer.MAX_VALUE);
    }
    
    public static String promptValue(int vr, ByteBuffer bb, int maxlen) {
       return promptValue(vr, bb, ISO_8859_1, maxlen);
    }
    
    public static String promptValue(int vr, ByteBuffer bb, Charset cs,
         int maxlen)
    {
        if (bb.limit() == 0)
            return "";
        
        if (VRs.isStringValue(vr)) {
            if (bb.get(bb.limit()-1) == 0)
                bb.limit(bb.limit()-1);
            return truncate(cs.decode(bb).toString(), maxlen);
        }
        
        switch (vr) {
            case VRs.AT:
                return promptAT(bb, maxlen);
            case VRs.FD:
                return promptFD(bb, maxlen);
            case VRs.FL:
                return promptFL(bb, maxlen);
            case VRs.OB: case VRs.UN:
                return promptOB(bb, maxlen);
            case VRs.OF:
                return promptOF(bb, maxlen);
            case VRs.OW:
                return promptOW(bb, maxlen);
            case VRs.SL:
                return promptSL(bb, maxlen);
            case VRs.SS:
                return promptSS(bb, maxlen);
            case VRs.UL:
                return promptUL(bb, maxlen);
            case VRs.US:
                return promptUS(bb, maxlen);
        }
        throw new IllegalArgumentException("VR:" + VRs.toString(vr));
    }

    public static String promptAT(ByteBuffer bb, int maxlen) {
        int l = bb.limit() / 4 * 9 - 1;
        if (l < 0)
            return "";

        StringBuffer sb = new StringBuffer(l);
        bb.rewind();
        Tags.toHexString(sb, bb.getShort() & 0xffff, 4);
        Tags.toHexString(sb, bb.getShort() & 0xffff, 4);
        while (bb.remaining() >= 4 && sb.length() < maxlen) {
            Tags.toHexString(sb.append('\\'), bb.getShort() & 0xffff, 4);
            Tags.toHexString(sb, bb.getShort() & 0xffff, 4);
        }                
        return truncate(sb.toString(), maxlen);        
    }
    
    public static String promptFD(ByteBuffer bb, int maxlen) {
        if (bb.limit() < 8)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getDouble());
        while (bb.remaining() >= 8 && sb.length() < maxlen)
            sb.append('\\').append(bb.getDouble());

        return truncate(sb.toString(), maxlen);        
    }
    
    public static String promptFL(ByteBuffer bb, int maxlen) {
        if (bb.limit() < 4)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getFloat());
        while (bb.remaining() >= 4 && sb.length() < maxlen)
            sb.append('\\').append(bb.getFloat());

        return truncate(sb.toString(), maxlen);        
    }

    public static String promptOB(ByteBuffer bb, int maxlen) {
        return promptBytes(bb.array(), bb.arrayOffset(), bb.limit(), maxlen);
    }
    
    public static String promptOF(ByteBuffer bb, int maxlen) {
        return promptFL(bb, maxlen);
    }
    
    public static String promptOW(ByteBuffer bb, int maxlen) {
        int l = bb.limit() / 2 * 5 - 1;
        if (l < 0)
            return "";
        
        StringBuffer sb = new StringBuffer(l);
        bb.rewind();
        Tags.toHexString(sb, bb.getShort() & 0xffff, 4);
        while (bb.remaining() >= 2 && sb.length() < maxlen)
            Tags.toHexString(sb.append('\\'), bb.getShort() & 0xffff, 4);
                
        return truncate(sb.toString(), maxlen);        
    }

    public static String promptSL(ByteBuffer bb, int maxlen) {
        if (bb.limit() < 4)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getInt());
        while (bb.remaining() >= 4 && sb.length() < maxlen)
            sb.append('\\').append(bb.getInt());

        return truncate(sb.toString(), maxlen);        
    }
    
    public static String promptSS(ByteBuffer bb, int maxlen) {
        if (bb.limit() < 2)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getShort());
        while (bb.remaining() >= 2 && sb.length() < maxlen)
            sb.append('\\').append(bb.getShort());

        return truncate(sb.toString(), maxlen);        
    }
    
    public static String promptUL(ByteBuffer bb, int maxlen) {
        if (bb.limit() < 4)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getInt() & 0xffffffffL);
        while (bb.remaining() >= 4 && sb.length() < maxlen)
            sb.append('\\').append(bb.getInt() & 0xffffffffL);

        return truncate(sb.toString(), maxlen);        
    }
    
    public static String promptUS(ByteBuffer bb, int maxlen) {
        if (bb.limit() < 2)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getShort() & 0xffff);
        while (bb.remaining() >= 2 && sb.length() < maxlen)
            sb.append('\\').append(bb.getShort() & 0xffff);

        return truncate(sb.toString(), maxlen);        
    }

    static final byte[] b0 = {};
    public static byte[] parseValue(int vr, String str) {
        if (str.length() == 0)
            return b0;
        
        switch (vr) {
            case VRs.AT:
                return parseAT(str);
            case VRs.FD:
                return parseFD(str);
            case VRs.FL:
                return parseFL(str);
            case VRs.OB: case VRs.UN:
                return parseBytes(str);
            case VRs.OF:
                return parseOF(str);
            case VRs.OW:
                return parseOW(str);
            case VRs.SS: case VRs.US:
                return parseSS_US(str);
            case VRs.SL: case VRs.UL:
                return parseSL_UL(str);
        }
        throw new IllegalArgumentException("VR:" + VRs.toString(vr));
    }

    static final String[] EMPTY_STRING_ARRAY = {};
    public static String[] split(String s, char delim) {
        if (s == null) {
            return null;
        }
        if (s.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        final int r0 = s.indexOf(delim);
        if (r0 == -1)
            return new String[]{s};
        int i = 2;
        int l, r = r0;
        for (; (r = s.indexOf(delim, l = r + 1)) != -1; ++i);
        String[] retval = new String[i];
        i = l = 0;
        r = r0;
        do retval[i++] = s.substring(l, r);
        while ((r = s.indexOf(delim, l = r + 1)) != -1);
        retval[i] = s.substring(l);
        return retval;
    }

    public static int count(String s, char delim) {
        if (s == null) return 0;
        int c = 0;
        for (int pos = -1; (pos = s.indexOf(delim, pos+1)) != -1; c++);
        return c;
    }
    
    public static String toString(String[] a, char delim) {
        if (a == null) {
            return null;
        }
        if (a.length == 0) {
            return "";
        }
        if (a.length == 1) {
            return a[0];
        }
        StringBuffer sb = new StringBuffer(a[0]);
        for (int i = 1; i < a.length; ++i) {
            sb.append(delim).append(a[i]);
        }
        return sb.toString();
    }
    
    public static byte[] parseAT(String str) {
        String[] a = split(str,'\\');
        byte[] b = new byte[a.length * 4];
        ByteBuffer bb = ByteBuffer.wrap(b, 0, b.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            int tag = (int)Long.parseLong(a[i],16);
            bb.putShort((short)(tag >>> 16));
            bb.putShort((short)tag);
        }
        return b;
    }
    
    public static byte[] parseFD(String str) {
        String[] a = split(str,'\\');
        byte[] b = new byte[a.length * 8];
        ByteBuffer bb = ByteBuffer.wrap(b, 0, b.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putDouble(Double.parseDouble(a[i]));
        return b;
    }
    
    public static byte[] parseFL(String str) {
        String[] a = split(str,'\\');
        byte[] b = new byte[a.length * 4];
        ByteBuffer bb = ByteBuffer.wrap(b, 0, b.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putFloat(Float.parseFloat(a[i]));
        return b;
    }

    public static byte[] parseOF(String str) {
        return parseFL(str);
    }
    
    public static byte[] parseBytes(String str) {
        byte[] b = new byte[(str.length()+1)/3];
        for (int i = 0, l = 0; i < b.length; ++i, l+=3)
            b[i++] = (byte)Short.parseShort(str.substring(l, l+2), 16);
        return b;
    }
    
    public static byte[] parseOW(String str) {
        byte[] b = new byte[(str.length()+1)/5];
        for (int i = 0, l = 0; i < b.length; l+=5) {
            short s = (short)Integer.parseInt(str.substring(l, l+4), 16);
            b[i++] = (byte)(s);
            b[i++] = (byte)(s >>> 8);
        }
        return b;
    }

    public static byte[] parseSS_US(String str) {
        String[] a = split(str,'\\');
        byte[] b = new byte[a.length * 2];
        ByteBuffer bb = ByteBuffer.wrap(b, 0, b.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putShort((short)Integer.parseInt(a[i]));
        return b;
    }
    
    public static byte[] parseSL_UL(String str) {
        String[] a = split(str,'\\');
        byte[] b = new byte[a.length * 4];
        ByteBuffer bb = ByteBuffer.wrap(b, 0, b.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putInt((int)Long.parseLong(a[i]));
        return b;
    }
    
    public static float[] parseFloats(String[] values) {
        float[] retval = new float[values.length];
        for (int i = 0; i < retval.length; ++i) {
            retval[i] = Float.parseFloat(values[i]);
        }
        return retval;
    }
    
    public static double[] parseDoubles(String[] values) {
        double[] retval = new double[values.length];
        for (int i = 0; i < retval.length; ++i) {
            retval[i] = Double.parseDouble(values[i]);
        }
        return retval;
    }

    public static int parseInt(String value, long min, long max) {
        long retval = Long.parseLong(value);
        if (retval < min || retval > max) {
            throw new NumberFormatException("value: " + value
                    + ", min:" + min + ", max:" + max);
        }
        return (int)retval;
    }
    
    public static int[] parseInts(String[] values, long min, long max) {
        int[] retval = new int[values.length];
        for (int i = 0; i < retval.length; ++i) {
            retval[i] = parseInt(values[i], min, max);
        }
        return retval;
    }
    
    private static final int UID_DIGIT1 = 0;
    private static final int UID_DIGIT = 1;
    private static final int UID_DOT = 2;
    private static final int UID_ERROR = -1;
    private static int nextState(int state, char c)
    {
       switch (state)
       {
          case UID_DIGIT1:
             if (c > '0' && c <= '9')
                return UID_DIGIT;
             if (c == '0')
                return UID_DOT;
             return UID_ERROR;
          case UID_DIGIT:
             if (c >= '0' && c <= '9')
                return UID_DIGIT;
             // fall through
          case UID_DOT:
             if (c == '.')
                return UID_DIGIT1;
             // fall through
       }
       return UID_ERROR;
    }
    
    public static String checkUID(String s)
    {
       char[] a = s.toCharArray();
       if (a.length == 0 || a.length > 64)
          throw new IllegalArgumentException(s);
       
       int state = UID_DIGIT1;
       for (int i = 0; i < a.length; ++i)
       {
          if ((state = nextState(state, a[i])) == UID_ERROR)
             throw new IllegalArgumentException(s);
       }
       if (state == UID_DIGIT1)
          throw new IllegalArgumentException(s);

       return s;
    }

    public static String[] checkUIDs(String[] a)
    {
       for (int i = 0; i < a.length; ++i)
       {
          checkUID(a[i]);
       }
       return a;
    }
        
    public static String checkAET(String s)
    {
       char[] a = s.toCharArray();
       if (a.length == 0 || a.length > 16)
          throw new IllegalArgumentException("AE title does not conform to DICOM standard, it is not " +
          		"allowed to contain more than 16 characters (or 0) and " + s + 
          		" contains " + s.length() + " characters.");

       for (int i = 0; i < a.length; ++i)
       {
          if (a[i] < '\u0020' || a[i] >= '\u007f')
             throw new IllegalArgumentException("AE title does not conform to DICOM standard, " + s
            		 + " contains unsupported characters.");
       }
       return s;
    }

    public static String[] checkAETs(String[] a)
    {
       for (int i = 0; i < a.length; ++i)
       {
          checkAET(a[i]);
       }
       return a;
    }
        
}
