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

package org.dcm4cheri.server;

import java.text.*;
import java.util.*;

final class SyslogMsg
{
    private static final char PriStartDelim = '<';
    private static final char PriEndDelim = '>';
    private static final char FieldDelim = ' ';  //space
    private static final int PriMaxLen = 3;
    private static final int DefaultPriValue = 13;
    private static final int DefaultFacility = (DefaultPriValue >> 3);
    private static final int DefaultSeverity = (DefaultPriValue & 0x7);
    private static final String[] Facility = new String[]
        {"kernel","user","mail","system daemons","sec/auth","syslogd",
         "lp subsystem","network subsystem","uucp subsystem","clock daemon",
         "security/authority","ftp daemon","ntp subsystem","log audit",
         "log alert","clock daemon","local use 0","local use 1","local use 2",
         "local use 3","local use 4","local use 5","local use 6",
         "local use 7"};
    private static final String[] Severity = new String[]
        {"emergency","alert","critical","error","warning","notice",
         "informational","debug"};
    private static final String[] Month = new String[]
        {"Jan","Feb","Mar","Apr","May","Jun",
         "Jul","Aug","Sep","Oct","Nov","Dec"};

    //both of the following comprise the priority portion of a syslog message
    private int severity = -1;
    private int facility = -1;
    //both of the following comprise the header portion of a syslog message
    private Calendar hdrTimeStamp = null;
    private String hdrHost = null;
    //both of the following comprise the message portion of a syslog message
    private String tag = null;
    private String content = null;
    //whether this instance was instantiated with a valid syslof msg
    private boolean isValid;

    public Date getTimestamp() {
        return hdrTimeStamp.getTime();
    }

    public String getHost() {
        return hdrHost;
    }

    public String getContent() {
        return content;
    }

    public String getMessage() {
    	if (tag==null)
            return "";
        return tag + content;
    }

    public class InvalidSyslogMsgException extends RuntimeException
    {
        InvalidSyslogMsgException(byte[] bMsg)
        {
            super(content = new String(bMsg));
            isValid = false;
            //if (bMsg.length>1024)
            //content = new String(bMsg,0,1024);
            //else
        }
    }

    /*
     * TODO: validate IP/host name part of HEADER
     */
    public SyslogMsg(byte[] bMsg, int len)
    {
        byte[] newMsg = new byte[len];
            System.arraycopy(bMsg, 0, newMsg, 0, len);
        parse(newMsg);
    }
    public SyslogMsg(byte[] bMsg)
	throws InvalidSyslogMsgException
    {
        parse(bMsg);
    }

    private void parse(byte[] bMsg)
	throws InvalidSyslogMsgException
    {
        //valid
        isValid = true;
        int iStart, iEnd = -1;
        for (int i=1; i<PriMaxLen+2 && i<bMsg.length; i++)
            if (bMsg[i]==PriEndDelim) {
                iEnd = i;
                break;
            }
        if (bMsg[0]!=PriStartDelim ||
            iEnd==-1)  //(invalid pri || no pri at all)
            throw new InvalidSyslogMsgException(bMsg);
        //fill facility/severity from PRI part
        int priority = Integer.parseInt(new String(bMsg,1,iEnd-1));
        int[] priFields = getPriorityFields(priority);
        facility = priFields[0];
        severity = priFields[1];
        //fill vars with info from HEADER part
        // time stamp
        iStart = iEnd + 1;
        final String MyDateFormat = "MMM dd HH:mm:ss";
        Date date;
        SimpleDateFormat dateFmt = new SimpleDateFormat(MyDateFormat, Locale.US);
        String sDate = new String(bMsg,iStart,15);
        if (bMsg[iStart+3]!=FieldDelim || bMsg[iStart+6]!=FieldDelim ||
            bMsg[iStart+15]!=FieldDelim ||
            (date = dateFmt.parse(sDate,new ParsePosition(0)))==null) {
            //add own timestamp and add hostname
            hdrTimeStamp = new GregorianCalendar();
            hdrHost = "unknown";
            //treat all of the datagram as content
            throw new InvalidSyslogMsgException(bMsg);
        }
        else {
            //set to current year, since syslog timestamp will not include
            // a year
            hdrTimeStamp = new GregorianCalendar();
            hdrTimeStamp.setTime(date);
            hdrTimeStamp.set(Calendar.YEAR, new
                     GregorianCalendar().get(Calendar.YEAR));
        }
        // host name/ip
        iStart += 16;
        iEnd = nextOccurence(bMsg,iStart,(byte)FieldDelim);
        if (iEnd==-1)
            throw new InvalidSyslogMsgException(bMsg);
        hdrHost = new String(bMsg,iStart,iEnd-iStart);
        iStart = iEnd + 1;
        //get MSG part
        // tag
        for (int i=iStart; i<iStart+33 && i<bMsg.length; i++)
            if (!Character.isLetterOrDigit((char)bMsg[i])) {
                iEnd = i;
                break;
            }
        if (iEnd<iStart)
            throw new InvalidSyslogMsgException(bMsg);
        tag = new String(bMsg, iStart, iEnd-iStart);
        // content
        content = new String(bMsg, iEnd, bMsg.length-iEnd);
    }
    
    private static boolean isAlphaNumeric(String str)
    {
        char[] chrs = new char[str.length()];
        str.getChars(0,str.length(),chrs,0);
        for (int i=0; i<str.length(); i++) {
            if (!Character.isLetterOrDigit(chrs[i]))
            return false;
        }
        return true;
    }

    private int nextOccurence(byte[] arr, int startIndex, byte value)
    {
	if (startIndex>=0)
	    for (int i=startIndex; i<arr.length; i++) {
		if (arr[i]==value)
		    return i;
	    }
	return -1;
    }

    private int[] getPriorityFields(int pri)
    {
	return new int[] {(pri >> 3),(pri & 0x7)};
    }

    private String severityToString(int sev)
    {
	return Severity[sev];
    }

    private String facilityToString(int fac)
    {
	return Facility[fac];
    }

    private String priorityToString(int pri)
	throws IllegalArgumentException
    {
	int[] field = getPriorityFields(pri);
	if (field[0]>=Facility.length)  //check for invalid facility
	    return "[invalid PRI]";
	return "[" + Facility[field[0]] + "," + Severity[field[1]] + "]";
    }

    public String toString()
    {
	return priorityToString((facility << 3) | severity) +
	    " [time=" + getDateString(hdrTimeStamp)
	    + ", host=" + hdrHost + "] "
	    + "TAG=" + tag + ", CONTENT=" + content;
    }

    private String getDateString(Calendar date)
    {
	DateFormat dtfmt = new SimpleDateFormat("MMM dd, yyyy HH.mm.ss Z");
	return dtfmt.format(date.getTime());
    }
}
