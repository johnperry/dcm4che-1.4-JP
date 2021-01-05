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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Medical Insight.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4cheri.imageio.plugins;

import java.io.IOException;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;

/**
 * @author Jesper Bojesen <jbb@medical-insight.com>
 * @version $Revision$ $Date:: xxxx-xx-xx $
 * @since Mar 24, 2011
 */
public class PatchJpegLSImageOutputStream extends ImageOutputStreamImpl {

    private final ImageOutputStream ios;
    private final byte[] jpegheader;

    public PatchJpegLSImageOutputStream(ImageOutputStream ios)
            throws IOException {
        if (ios == null)
            throw new NullPointerException("ios");
        this.ios = ios;
        jpegheader = new byte[17];
    }

    public void write(byte[] b, int off, int len) throws IOException {
	int pos = (int) getStreamPosition();

        streamPos = pos + len;

        if (pos >= 17) {
            ios.write(b, off, len);
        } else {
            int b_header = Math.min(17 - pos, len);
            System.arraycopy(b, off, jpegheader, pos, b_header);

            if (pos + len >= 17) {
                ios.write(jpegheader, 0, 15);
                byte[] patch = PatchJpegLS.selectPatch(jpegheader);
                if (patch != null)
                    ios.write(patch);
                ios.write(jpegheader, 15, 2);
                ios.write(b, off + b_header, len - b_header);
            }
        }
    }

    public void write(byte[] b) throws IOException {
	write (b, 0, b.length);
    }

    public void write(int b) throws IOException {
	ios.write(b);
    }

    public int read() throws IOException {
	return ios.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
	return ios.read (b, off, len);
    }
}
