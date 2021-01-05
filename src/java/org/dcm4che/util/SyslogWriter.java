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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3923 $ $Date: 2005-10-25 13:25:45 +0200 (Di, 25 Okt 2005) $
 *
 */
public class SyslogWriter extends OutputStreamWriter {
    // Constants -----------------------------------------------------
    private static final String[] MONTH = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    
    private static final String[] DAY = {
        null, " 1", " 2", " 3", " 4", " 5", " 6", " 7", " 8", " 9",
        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
        "30", "31"
    };
    
    private static final String[] MINUTE = {
        "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
        "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"
    };
    
    /** system is unusable */
    public static final int LOG_EMERG   = 0;
    
    /** action must be taken immediately */
    public static final int LOG_ALERT   = 1;
    
    /** critical conditions */
    public static final int LOG_CRIT    = 2;
    
    /** error conditions */
    public static final int LOG_ERR     = 3;
    
    /** warning conditions */
    public static final int LOG_WARNING = 4;
    
    /** normal but significant condition  */
    public static final int LOG_NOTICE  = 5;
    
    /** informational */
    public static final int LOG_INFO    = 6;
    
    /** debug-level messages */
    public static final int LOG_DEBUG   = 7;
    
    
    /** kernel messages */
    public static final int LOG_KERN = 0<<3;
    
    /** random user-level messages */
    public static final int LOG_USER = 1<<3;
    
    /** mail system */
    public static final int LOG_MAIL = 2<<3;
    
    /** system daemons */
    public static final int LOG_DAEMON = 3<<3;
    
    /** security/authorization messages */
    public static final int LOG_AUTH = 4<<3;
    
    /** messages generated internally by syslogd */
    public static final int LOG_SYSLOG = 5<<3;
    
    /** line printer subsystem */
    public static final int LOG_LPR = 6<<3;
    
    /** network news subsystem */
    public static final int LOG_NEWS = 7<<3;
    
    /** UUCP subsystem */
    public static final int LOG_UUCP = 8<<3;
    
    /** clock daemon */
    public static final int LOG_CRON = 9<<3;
    
    /** security/authorization messages (private) */
    public static final int LOG_AUTHPRIV = 10<<3;
    
    /** ftp daemon */
    public static final int LOG_FTP = 11<<3;
    
    /** reserved for local use */
    public static final int LOG_LOCAL0 = 16<<3;
    
    /** reserved for local use */
    public static final int LOG_LOCAL1 = 17<<3;
    
    /** reserved for local use */
    public static final int LOG_LOCAL2 = 18<<3;
    
    /** reserved for local use */
    public static final int LOG_LOCAL3 = 19<<3;
    
    /** reserved for local use */
    public static final int LOG_LOCAL4 = 20<<3;
    
    /** reserved for local use */
    public static final int LOG_LOCAL5 = 21<<3;
    
    /** reserved for local use */
    public static final int LOG_LOCAL6 = 22<<3;
    
    /** reserved for local use */
    public static final int LOG_LOCAL7 = 23<<3;
    
    private static final String[] LEVEL = {
        "emerg",
        "alert",
        "crit",
        "err",
        "warning",
        "notice",
        "info",
        "debug"
    };
    
    public static String levelAsString(int level) {
        try {
            return LEVEL[level];
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("level: " + level);
        }
    }
    
    public static final int forName(String name) {
        try {
            return SyslogWriter.class.getField("LOG_" + name.toUpperCase()).getInt(null);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(name);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }
    
    // Attributes ----------------------------------------------------
    public String syslogHost;
    public int syslogPort = 514;
    private String tag;
    private int facility = LOG_USER;
    private InetAddress address;
    private DatagramSocket ds;
    private boolean printHostName = true;
    private String contentPrefix = "";
    private final MyByteArrayOutputStream bout;
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public SyslogWriter() {
        this(new MyByteArrayOutputStream());
    }
    
    private SyslogWriter(MyByteArrayOutputStream bout) {
        super(bout);
        this.bout = bout;
    }
    
    // Public --------------------------------------------------------
    public void setSyslogHost(String syslogHost) throws UnknownHostException {
        this.address = InetAddress.getByName(syslogHost);
        this.syslogHost = syslogHost;
    }
    
    public String getSyslogHost() {
        return syslogHost;
    }
    
    public void setSyslogPort(int syslogPort) {
        this.syslogPort = syslogPort;
    }
    
    public int getSyslogPort() {
        return syslogPort;
    }
    
    public void setPrintHostName(boolean printHostName) {
        this.printHostName = printHostName;
    }
    
    public void setContentPrefix(String contentPrefix) {
        this.contentPrefix = contentPrefix;
    }
    
    public void setFacility(String facility) {
        this.facility = forName(facility);
    }
    
    public String getFacilityAsString() {
        switch(facility) {
            case LOG_KERN:
                return "kern";
            case LOG_USER:
                return "user";
            case LOG_MAIL:
                return "mail";
            case LOG_DAEMON:
                return "daemon";
            case LOG_AUTH:
                return "auth";
            case LOG_SYSLOG:
                return "syslog";
            case LOG_LPR:
                return "lpr";
            case LOG_NEWS:
                return "news";
            case LOG_UUCP:
                return "uucp";
            case LOG_CRON:
                return "cron";
            case LOG_AUTHPRIV:
                return "authpriv";
            case LOG_FTP:
                return "ftp";
            case LOG_LOCAL0:
                return "local0";
            case LOG_LOCAL1:
                return "local1";
            case LOG_LOCAL2:
                return "local2";
            case LOG_LOCAL3:
                return "local3";
            case LOG_LOCAL4:
                return "local4";
            case LOG_LOCAL5:
                return "local5";
            case LOG_LOCAL6:
                return "local6";
            case LOG_LOCAL7:
                return "local7";
        }
        throw new RuntimeException();
    }
    
    public void setFacility(int facility) {
        switch(facility) {
            case LOG_KERN:
            case LOG_USER:
            case LOG_MAIL:
            case LOG_DAEMON:
            case LOG_AUTH:
            case LOG_SYSLOG:
            case LOG_LPR:
            case LOG_NEWS:
            case LOG_UUCP:
            case LOG_CRON:
            case LOG_AUTHPRIV:
            case LOG_FTP:
            case LOG_LOCAL0:
            case LOG_LOCAL1:
            case LOG_LOCAL2:
            case LOG_LOCAL3:
            case LOG_LOCAL4:
            case LOG_LOCAL5:
            case LOG_LOCAL6:
            case LOG_LOCAL7:
                break;
            default:
                throw new IllegalArgumentException("facility: " + facility);
        }
        this.facility = facility;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public synchronized void writeHeader(int level) throws IOException {
        writeHeader(level, System.currentTimeMillis());
    }
    
    public synchronized void writeHeader(int level, long millis) throws IOException {
        if ((level & ~7) != 0) {
            throw new IllegalArgumentException("level: " + level);
        }
        
        reset();
        this.write('<');
        this.write(String.valueOf(facility|level));
        this.write('>');
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        this.write(MONTH[cal.get(Calendar.MONTH)]);
        this.write(' ');
        this.write(DAY[cal.get(Calendar.DAY_OF_MONTH)]);
        this.write(' ');
        this.write(MINUTE[cal.get(Calendar.HOUR_OF_DAY)]);
        this.write(':');
        this.write(MINUTE[cal.get(Calendar.MINUTE)]);
        this.write(':');
        this.write(MINUTE[cal.get(Calendar.SECOND)]);
        this.write(' ');
        if (printHostName) {
            this.write(HostNameUtils.getLocalHostName());
            this.write(' ');
        }
        
        if (tag != null) {
            this.write(tag);
        }
        
        this.write(contentPrefix);
    }
    
    public synchronized void writeTo(OutputStream out) throws IOException {
        super.flush();
        bout.writeTo(out);
    }
    
    public synchronized void write(int level, String msg)
    throws IOException {
        write(level, msg, System.currentTimeMillis());
    }
    
    public synchronized void write(int level, String msg, long millis)
    throws IOException {
        writeHeader(level, millis);
        write(msg);
        flush();
    }
    
    public synchronized void writeTo(int level, String msg, OutputStream out)
    throws IOException {
        writeTo(level, msg, out, System.currentTimeMillis());
    }

    public synchronized void writeTo(int level, String msg, OutputStream out, long millis)
    throws IOException {
        writeHeader(level, millis);
        write(msg);
        writeTo(out);
    }
    
    public synchronized void reset() throws IOException {
        super.flush();
        bout.reset();
    }
    
    public synchronized void flush() throws IOException {
        if (address == null)
            address = InetAddress.getLocalHost();
        
        if (ds == null)
            ds = new DatagramSocket();
        
        super.flush();
        if (bout.size() > 0) {
            ds.send(new DatagramPacket(bout.getBuffer(), bout.size(),
                address, syslogPort));
            bout.reset();
        }
    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    private static class MyByteArrayOutputStream extends ByteArrayOutputStream {
        byte[] getBuffer() {
            return buf;
        }
    }
}
