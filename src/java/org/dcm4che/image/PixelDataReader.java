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
 * Joe Foraci <jforaci@users.sourceforge.net>
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

package org.dcm4che.image;

import java.io.IOException;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since July 2003
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 * @see "DICOM Part 5: Data Structures and Encoding, Section 8. 'Encoding of Pixel,
 *      Overlay and Waveform Data', Annex D"
 */
public interface PixelDataReader
{
    /**
     * Retrieves the <code>PixelDataDescription</code>.
     * @return PixelDataDescription
     */
    public PixelDataDescription getPixelDataDescription();

    /**
     * Marks the read position of the backing <code>ImageInputStream</code>
     */
    public void mark();

    /**
     * Resets the read position of the backing <code>ImageInputStream</code> to
     * the matching mark (where mark() was last called on the stream). If no
     * mark was set using @see org.dcm4che.image.PixelData#mark(), nothing
     * happens.
     * @throws IOException If the matching mark is in a portion of the backing
     *      <code>ImageInputStream</code> that has already been discarded.
     */
    public void reset()
        throws IOException;

    /**
     * Reset the read position of the backing <code>ImageInputStream</code> and
     * state of this instance to the initial read position
     * @throws IOException On I/O error -- a seek to beginning of stream was not
     *      was not possible
     */
    public void resetStream()
        throws IOException;

    /**
     * If a sample can't be read, the state of the contained <code>ImageInputStream</code> is
     * not disturbed (beyond any effect of trying to read and getting an IOException)
     * @return The next sample value from the underlying <code>ImageInputStream</code>
     * @throws IOException If underlying <code>ImageInputStream</code> has an I/O problem
     */
    public int readSample()
        throws IOException;

    /**
     * Skip the specified number of samples from the current position in the stream.
     * @param n Number of samples to skip. The behaviour of this method is undefined
     *          for negative values of <code>n</code> or if an IOException occurs.
     * @throws IOException On I/O error
     */
    public void skipSamples(int n)
        throws IOException;
    /**
     * Skip the specified number of samples from the current position in the stream.
     * @param n Number of samples to skip. The behavior of this class is undefined
     *          for negative values of <code>n</code> or if an IOException occurs.
     * @throws IOException On I/O error
     */
    public void skipSamples(long n)
        throws IOException;

    public void skipToNextFrame()
        throws IOException;

    public void readFully(int[] samples, int offset, int len)
        throws IOException;

    public int[] readFully(int len)
        throws IOException;

    public int[] getPixel(int i, int j, int k)
        throws IOException;

    public int getSample(int i, int j, int k, int band)
        throws IOException;

    public int[][][] getPixelDataArray();

    public int[][] getPixelDataArray(int frame);

    public void readPixelData()
        throws IOException;

    public void readPixelData(boolean grabOverlayData)
        throws IOException;
}
