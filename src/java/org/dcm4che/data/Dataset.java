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

package org.dcm4che.data;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.dcm4che.dict.TagDictionary;

import org.xml.sax.ContentHandler;

/**
 *  Defines behavior of <code>Dataset</code> container objects.
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author     <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since      March, 2002
 * @see        "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 * @version    $Revision: 4028 $ $Date: 2006-07-10 18:39:15 +0200 (Mo, 10 Jul 2006) $ <p>
 */
public interface Dataset extends DcmObject, Serializable {

    void shareElements();

    Dataset setFileMetaInfo(FileMetaInfo fmi);

    FileMetaInfo getFileMetaInfo();

    Dataset getParent();

    Dataset setItemOffset(long itemOffset);

    long getItemOffset();

    int calcLength(DcmEncodeParam param);

    void writeDataset(DcmHandler handler, DcmEncodeParam param)
            throws IOException;

    void writeDataset(OutputStream out, DcmEncodeParam param)
            throws IOException;

    void writeDataset(ImageOutputStream out, DcmEncodeParam param)
            throws IOException;

    void readDataset(InputStream in, DcmDecodeParam param, int stopTag)
            throws IOException;

    void readFile(InputStream in, FileFormat format, int stopTag)
            throws IOException;

    void readFile(ImageInputStream iin, FileFormat format, int stopTag)
            throws IOException;

    void readFile(File file, FileFormat format, int stopTag) throws IOException;

    void writeFile(OutputStream out, DcmEncodeParam param) throws IOException;

    void writeFile(ImageOutputStream iout, DcmEncodeParam param)
            throws IOException;

    void writeFile(File file, DcmEncodeParam param) throws IOException;

    void writeFile(ContentHandler handler, TagDictionary dict)
            throws IOException;

    void writeFile2(ContentHandler ch, TagDictionary dict, int[] excludeTags,
            int excludeValueLengthLimit, File basedir) throws IOException;

    void writeDataset(ContentHandler handler, TagDictionary dict)
            throws IOException;

    void writeDataset2(ContentHandler ch, TagDictionary dict,
            int[] excludeTags, int excludeValueLengthLimit, File basedir)
            throws IOException;

    Dataset subSet(int fromTag, int toTag);

    Dataset subSet(Dataset filter);

    Dataset subSet(int[] tags);

    Dataset subSet(int[] tags, boolean exclude, boolean excludePrivate);
    
    Dataset exclude(int[] tags);

    Dataset subSet(int[] tags, int[] vrs);

    Dataset subSet(int[] tags, int[] vrs, boolean exclude, boolean excludePrivate);
    
    Dataset exclude(int[] tags, int[] vrs);

    Dataset excludePrivate();
    
    boolean match(Dataset keys, boolean ignorePNCase, boolean ignoreEmpty);

    public void dumpDataset(OutputStream out, Map map, int excludeValueLengthLimit)
            throws IOException;

    public void dumpDataset(OutputStream out, Map map) throws IOException;

    public void dumpDataset(Writer w, Map map, int excludeValueLengthLimit)
            throws IOException;

    public void dumpDataset(Writer w, Map map) throws IOException;

    /**
     * Constructs a <code>BufferedImage</code> representing this datasets image. The
     * first image, if multiframe.
     *
     * @return                         A <code>BufferedImage</code> representing the encoded DICOM image.
     * @throws  IllegalStateException  If this dataset contains invalid or unsupported
     * Image Pixel Module attributes describing the encoding of DICOM image pixel data
     */
    BufferedImage toBufferedImage();

    /**
     * Constructs a <code>BufferedImage</code> of the given <code>frame</code>
     * number of this dataset
     *
     * @param  frame                      The frame number to convert to a <code>BufferedImage</code>
     * @return                            A <code>BufferedImage</code> representing the encoded DICOM image.
     * @throws  IllegalStateException     If this dataset contains invalid or unsupported
     * Image Pixel Module attributes describing the encoding of DICOM image pixel data
     * @throws  IllegalArgumentException  If <code>frame</code> does not appear to exist
     */
    BufferedImage toBufferedImage(int frame);

    /**
     * Place the attributes to represent the given <code>BufferedImage</code>
     * into this dataset. The most appropriate Image Pixel Module attributes to
     * represent the BufferedImage are placed in the dataset as well as the actual
     * image data and any other related attributes (palette LUTs, etc). No other
     * attributes are set.
     *
     * @param  bi  A <code>BufferedImage</code>
     */
    void putBufferedImage(BufferedImage bi);

    void putBufferedImage(BufferedImage bi, Rectangle sourceRegion);

    void putBufferedImage(BufferedImage bi, Rectangle sourceRegion,
            boolean writeIndexedAsPaletteColor);

    void putBufferedImageAsRgb(BufferedImage bi, Rectangle sourceRegion);

    void putBufferedImageAsMonochrome(BufferedImage bi, Rectangle sourceRegion,
            boolean writeAsMonochrome2);

    void putBufferedImageAsPaletteColor(BufferedImage bi, Rectangle sourceRegion);
}

