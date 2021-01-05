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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.log4j.Logger;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 3988 $ $Date: 2006-05-08 16:21:15 +0200 (Mo, 08 Mai 2006) $
 * @since 28.06.2004
 */
public class MD5Utils {

    public static final long MEGA = 1000000L;

    public static final long GIGA = 1000000000L;

    private static final int BUF_SIZE = 512;

    private static final Logger log = Logger.getLogger(MD5Utils.class);
    
    private MD5Utils() {
    }

    public static File makeMD5File(File f) {
        return new File(f.getParent(), f.getName() + ".MD5");
    }

    public static char[] toHexChars(byte[] bs) {
        char[] cbuf = new char[bs.length * 2];
        toHexChars(bs, cbuf);
        return cbuf;
    }

    private static final char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    
    private static int hex2digit(char ch) {
        return ch < '0' ? 0 : ch <= '9' ? (ch - '0') 
                : ch < 'A' ? 0 : ch <= 'F' ? (ch - ('A' - 10)) 
                : ch < 'a' ? 0 : ch <= 'f' ? (ch - ('a' - 10)) : 0;
    }

    public static void toHexChars(byte[] bs, char[] cbuf) {
        for (int i = 0, j = 0; i < bs.length; i++, j++, j++) {
            cbuf[j] = HEX_DIGIT[(bs[i] >>> 4) & 0xf];
            cbuf[j + 1] = HEX_DIGIT[bs[i] & 0xf];
        }
    }

    public static void toHexChars(byte[] bs, byte[] cbuf, int off) {
        for (int i = 0, j = off; i < bs.length; i++, j++, j++) {
            cbuf[j] = (byte) HEX_DIGIT[(bs[i] >>> 4) & 0xf];
            cbuf[j + 1] = (byte) HEX_DIGIT[bs[i] & 0xf];
        }
    }

    public static void toBytes(char[] cbuf, byte[] bs) {
        for (int i = 0, j = 0; i < bs.length; i++, j++, j++) {
            bs[i] = (byte) ((hex2digit(cbuf[j]) << 4) | hex2digit(cbuf[j+1]));
        }
    }

    public static byte[] toBytes(char[] cbuf) {
        byte[] bs = new byte[cbuf.length/2];
        toBytes(cbuf, bs);
        return bs;        
    }

    public static byte[] toBytes(String s) {
        return toBytes(s.toCharArray());
    }
    
    public static void md5sum(File f, char[] cbuf,
            MessageDigest digest, byte[] bbuf) throws IOException {
        toHexChars(md5sum(f, digest, bbuf), cbuf);
    }
    
	public static byte[] md5sum(File dst, MessageDigest digest, byte[] buf) 
	throws IOException {
        digest.reset();
        InputStream in = new DigestInputStream(new FileInputStream(dst), digest);
        try {
            while (in.read(buf) != -1)
                ;
        } finally {
            in.close();
        }
		return digest.digest();
	}   

    public static boolean verify(File driveDir, File fsDir)
            throws IOException {
        File md5sums = new File(driveDir, "MD5_SUMS");
        return md5sums.exists() ? verifyMd5Sums(md5sums,
                new byte[BUF_SIZE]) : equals(driveDir,
                fsDir,
                 new byte[BUF_SIZE],
                new byte[BUF_SIZE]);
    }

    private static boolean equals(File dst, File src,
            byte[] srcBuf, byte[] dstBuf) throws IOException {
        if (src.isDirectory()) {
            String[] ss = src.list();
            for (int i = 0; i < ss.length; i++) {
                String s = ss[i];
                if (!(equals(new File(dst, s),
                        new File(src, s),
                        srcBuf,
                        dstBuf))) return false;
            }
        } else {
            if (!dst.isFile()) {
                log.warn("File " + dst + " missing");
                return false;
            }
            log.debug("check " + dst + " = " + src);
            final long srcLen = src.length();
            final long dstLen = dst.length();
            if (dstLen != srcLen) {
                log.warn("File " + dst + " has wrong length");
                return false;
            }
            DataInputStream dstIn = new DataInputStream(
                    new FileInputStream(dst));
            try {
                InputStream srcIn = new FileInputStream(src);
                try {
                    int len;
                    while ((len = srcIn.read(srcBuf)) != -1) {
                        dstIn.readFully(dstBuf, 0, len);
                        if (!equals(dstBuf, srcBuf, len)) {
                            log.warn("File " + dst + " corrupted");
                            return false;
                        }
                    }
                } finally {
                    srcIn.close();
                }
            } finally {
                dstIn.close();
            }
        }
        return true;
    }

    private static boolean equals(byte[] dstBuf, byte[] srcBuf, int len) {
        for (int i = 0; i < len; i++)
            if (dstBuf[i] != srcBuf[i]) return false;
        return true;
    }

    private static boolean verifyMd5Sums(File md5sums, byte[] bbuf)
            throws IOException {
        String base = md5sums.getParentFile().toURI().toString();
        BufferedReader md5sumsIn = new BufferedReader(new FileReader(md5sums));
        try {
            final char[] cbuf = new char[32];
            String line;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            while ((line = md5sumsIn.readLine()) != null) {
                if (line.length() < 33) continue;
                File f = new File(new URI(base + line.substring(32).trim()));
                log.debug("md5sum " + f);
                md5sum(f, cbuf, digest, bbuf);
                if (!Arrays.equals(cbuf, line.substring(0, 32).toCharArray())) {
                    log.warn("File " + f + " corrupted");
                    return false;
                }
            }
        } catch (URISyntaxException e) {
            log.warn("File " + md5sums + " corrupted");
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } finally {
            md5sumsIn.close();
        }
        return true;
    }

    public static String formatSize(long size) {
        if (size < GIGA)
            return ((float) size / MEGA) + "MB";
        else
            return ((float) size / GIGA) + "GB";
    }

    public static long parseSize(String s, long minSize) {
        long u;
        if (s.endsWith("GB"))
            u = GIGA;
        else if (s.endsWith("MB"))
            u = MEGA;
        else
            throw new IllegalArgumentException(s);
        try {
            long size = (long) (Float.parseFloat(s.substring(0, s.length() - 2)) * u);
            if (size >= minSize)
                return size;
        } catch (IllegalArgumentException e) {
        }
        throw new IllegalArgumentException(s);
    }

}