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

package org.dcm4cheri.net;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class UnparsedPDUImpl {
    
    static final int MAX_LENGTH = 1048576; // 1 MB
    private final byte[] buf;
    private final int type;
    private final int len;
    
    /** Creates a new instance of RawPDU */
    public UnparsedPDUImpl(InputStream in, byte[] buf) throws IOException {
        if (buf == null || buf.length < 6) {
            buf = new byte[10];
        }
        readFully(in, buf, 0, 6);
        this.type = buf[0] & 0xFF;
        this.len = ((buf[2] & 0xff) << 24)
                | ((buf[3] & 0xff) << 16)
                | ((buf[4] & 0xff) << 8)
                | ((buf[5] & 0xff) << 0);
        if ((len & 0xFFFFFFFFL) > Math.max(buf.length, MAX_LENGTH)) {
//            skipFully(in, len & 0xFFFFFFFFL);
            this.buf = null;
            return;
        }
        if (buf.length < 6 + len) {
            this.buf = new byte[6 + len];
            System.arraycopy(buf, 0, this.buf, 0, 6);
        } else {
            this.buf = buf;
        }
        readFully(in, this.buf, 6, len);
    }

    public final int type() {
        return type;
    }

    public final int length() {
        return len;
    }

    public final byte[] buffer() {
        return buf;
    }
    
    public String toString() {
        return "PDU[type=" + type
                + ", length=" + (len & 0xFFFFFFFFL)
                + "]";
    }
/*
    static void skipFully(InputStream in, long len)
            throws IOException {
	long n = 0;
	while (n < len) {
	    long count = in.skip(len - n);
	    if (count <= 0)
	    	throw new EOFException();
	    n += count;
	}
    }
*/
    static void readFully(InputStream in, byte b[], int off, int len)
            throws IOException {
	int n = 0;
	while (n < len) {
	    int count = in.read(b, n + off, len - n);
	    if (count < 0)
		throw new EOFException();
	    n += count;
	}
    }
}
