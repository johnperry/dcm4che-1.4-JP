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

import org.dcm4che.Implementation;
import org.dcm4che.util.UIDGenerator;

import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class UIDGeneratorImpl extends UIDGenerator {
    
    private static final String IP;
    static {
       String tmp;
       try {
          tmp = InetAddress.getLocalHost().getHostAddress();
       } catch (UnknownHostException e) {
          tmp = "127.0.0.1";
       }
       IP = tmp;
    }

    /** Creates a new instance of UIDGeneratorImpl */
    public UIDGeneratorImpl() {
    }

    public String createUID() {
        return createUID(Implementation.getClassUID());
    }
    
    public String createUID(String root) {
        final StringBuffer sb = new StringBuffer(64).append(root).append('.');
        try {
            new UID().write(new DataOutput() {
                public void write(int b) {}
                public void write(byte b[]) {}
                public void write(byte b[], int off, int len) {}
                public void writeBoolean(boolean v) {}
                public void writeByte(int v) {}
                public void writeShort(int v) {
                    sb.append('.').append(v & 0xffff);
                }
                public void writeChar(int v) {}
                public void writeInt(int v)  {
                    if ("127.0.0.1".equals(IP)) {
                        sb.append(v & 0xffffffffL);
                    } else {
                        sb.append(IP);
                    }
                }
                public void writeLong(long v) {
                    sb.append('.').append(
                            new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(v)));
                }
                public void writeFloat(float v) {}
                public void writeDouble(double v) {}
                public void writeBytes(String s) {}
                public void writeChars(String s) {}
                public void writeUTF(String str) {}             
            });
        }
        catch (IOException ex) {
            throw new RuntimeException(ex.toString());
        }
        if (sb.length() > 64) {
            throw new IllegalArgumentException("Too long root prefix");
        }
        return sb.toString();
    }
}
