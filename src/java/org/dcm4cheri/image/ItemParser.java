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
 * See listed authors below.
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

package org.dcm4cheri.image;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

import com.sun.media.imageio.stream.SegmentedImageInputStream;
import com.sun.media.imageio.stream.StreamSegment;
import com.sun.media.imageio.stream.StreamSegmentMapper;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 16896 $ $Date: 2012-07-17 17:09:38 +0200 (Di, 17 Jul 2012) $
 * @since 04.08.2004
 * 
 */
public class ItemParser implements StreamSegmentMapper {

    private static final Logger log = Logger.getLogger(ItemParser.class);

    private static final HashSet JPEG_TS = new HashSet(
            Arrays.asList(new String[] { UIDs.JPEGBaseline,
                            UIDs.JPEGExtended, UIDs.JPEGExtended35Retired,
                            UIDs.JPEG68Retired, UIDs.JPEG79Retired,
                            UIDs.JPEG1012Retired, UIDs.JPEG1113Retired,
                            UIDs.JPEGLossless14, UIDs.JPEGLossless15Retired,
                            UIDs.JPEG1618Retired, UIDs.JPEG1719Retired,
                            UIDs.JPEG2022Retired, UIDs.JPEG2123Retired,
                            UIDs.JPEG2426Retired, UIDs.JPEG2527Retired,
                            UIDs.JPEGLoRetired, UIDs.JPEG29Retired,
                            UIDs.JPEGLossless, UIDs.JPEGLSLossless,
                            UIDs.JPEGLSLossy, UIDs.JPEG2000Lossless,
                            UIDs.JPEG2000Lossy }));

    public static final class Item {

        public final long offset;

        public final long startPos;

        public final int length;

        public Item(long offset, long startPos, int length) {
            this.offset = offset;
            this.startPos = startPos;
            this.length = length;
        }

        public final long nextOffset() {
            return offset + length;
        }

        public final long nextItemPos() {
            return startPos + length;
        }

        public String toString() {
            return "Item[off=" + offset + ", pos=" + startPos + ", len="
                    + length + "]";
        }

    }

    private final ArrayList items = new ArrayList();

    private final DcmParser parser;
    private final ImageInputStream iis;
    private final ArrayList firstItemOfFrame;
    private final int numberOfFrames;
    private final boolean rle;
    private final boolean jpeg;
    private final boolean jpeg2000;
    private int[] basicOffsetTable;
    private byte[] soi = new byte[2];
    private boolean lastItemSeen = false;
    private int frame;

    public ItemParser(DcmParser parser, int numberOfFrames, String tsuid)
            throws IOException {
        this.parser = parser;
        this.iis = parser.getImageInputStream();
        this.numberOfFrames = numberOfFrames;
        this.firstItemOfFrame = new ArrayList(numberOfFrames);
        this.jpeg = JPEG_TS.contains(tsuid);
        this.jpeg2000 = jpeg && (UIDs.JPEG2000Lossless.equals(tsuid) || UIDs.JPEG2000Lossless.equals(tsuid));
        this.rle = !jpeg && UIDs.RLELossless.equals(tsuid);
        parser.parseHeader();
        int offsetTableLen = parser.getReadLength();
        if (offsetTableLen != 0) {
            if (offsetTableLen != numberOfFrames * 4) {
                log.warn("Skip Basic Offset Table with illegal length: "
                        + offsetTableLen + " for image with " + numberOfFrames
                        + " frames!");
                iis.skipBytes(offsetTableLen);
            } else {
                basicOffsetTable = new int[numberOfFrames];
                iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < basicOffsetTable.length; i++) {
                    basicOffsetTable[i] = iis.readInt();
                }
            }
        }
        next();
    }

    public int getNumberOfDataFragments() {
        while (!lastItemSeen)
            next();
        return items.size();
    }

    private Item getFirstItemOfFrame(int frame) throws IOException {
        while (firstItemOfFrame.size() <= frame) {
            if (next() == null)
                throw new IOException("Could not detect first item of frame #"
                        + (frame+1));
        }
        return (Item) firstItemOfFrame.get(frame);
    }

    private Item next() {
        if (lastItemSeen)
            return null;
        try {
            if (!items.isEmpty())
                iis.seek(last().nextItemPos());
            parser.parseHeader();
            if (log.isDebugEnabled())
                log.debug("Read " + Tags.toString(parser.getReadTag()) + " #"
                        + parser.getReadLength());
            if (parser.getReadTag() == Tags.Item) {
                Item item = new Item(items.isEmpty() ? 0 : last().nextOffset(),
                        iis.getStreamPosition(), parser.getReadLength());
                if (items.isEmpty() || rle) {
                    addFirstItemOfFrame(item);
                } else if (firstItemOfFrame.size() < numberOfFrames) {
                    if (basicOffsetTable != null) {
                        Item firstItem = (Item) firstItemOfFrame.get(0);
                        int frame = firstItemOfFrame.size();
                        if (item.startPos == firstItem.startPos
                                + (basicOffsetTable[frame] & 0xFFFFFFFFL)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Start position of item #"
                                        + (items.size()+1) + " matches "
                                        + (frame+1)
                                        + ".entry of Basic Offset Table.");
                            }
                            addFirstItemOfFrame(item);
                        }
                    } else if (jpeg) {
                        iis.read(soi, 0, 2);
                        if (soi[0] == (byte) 0xFF && (soi[1] == (byte) 0xD8
                                                   || soi[1] == (byte) 0x4F)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Detect JPEG SOI/SOC at item #"
                                        + (items.size()+1));
                            }
                            addFirstItemOfFrame(item);
                        }
                        iis.seek(item.startPos);
                    }
                }
                items.add(item);
                return item;
            }
        } catch (IOException e) {
            log.warn("i/o error reading next item:", e);
        }
        if (parser.getReadTag() != Tags.SeqDelimitationItem
                || parser.getReadLength() != 0) {
            log.warn("expected (FFFE,E0DD) #0 but read "
                    + Tags.toString(parser.getReadTag()) + " #"
                    + parser.getReadLength());
        }
        lastItemSeen = true;
        return null;
    }

    private void addFirstItemOfFrame(Item item) {
        if (log.isDebugEnabled()) {
            log.debug("Detect item #" + (items.size()+1)
                    + " as first item of frame #"
                    + (firstItemOfFrame.size()+1));
        }
        firstItemOfFrame.add(item);
    }

    private Item last() {
        return (Item) items.get(items.size() - 1);
    }

    public StreamSegment getStreamSegment(long pos, int len) {
        StreamSegment retval = new StreamSegment();
        getStreamSegment(pos, len, retval);
        return retval;
    }

    public void getStreamSegment(long pos, int len, StreamSegment seg) {
        if (log.isDebugEnabled())
            log.debug("getStreamSegment(pos=" + pos + ", len=" + len + ")");
        if (isEndOfFrame(pos)) {
            setEOF(seg);
            return;
        }
        Item item = last();
        while (item.nextOffset() <= pos) {
            if ((item = next()) == null || isEndOfFrame(pos)) {
                setEOF(seg);
                return;
            }
        }
        int i = items.size() - 1;
        while (item.offset > pos)
            item = (Item) items.get(--i);
        seg.setStartPos(item.startPos + pos - item.offset);
        seg.setSegmentLength(Math.min((int) (item.offset + item.length - pos),
                len));
        if (log.isDebugEnabled())
            log.debug("return StreamSegment[start=" + seg.getStartPos()
                    + ", len=" + seg.getSegmentLength() + "]");
    }

    private boolean isEndOfFrame(long pos) {
        return frame+1 < firstItemOfFrame.size()
            && ((Item) firstItemOfFrame.get(frame+1)).offset <= pos;
    }

    private void setEOF(StreamSegment seg) {
        seg.setSegmentLength(-1);
        if (log.isDebugEnabled())
            log.debug("return StreamSegment[start=" + seg.getStartPos()
                    + ", len=-1]");
    }

    public void seekFrame(SegmentedImageInputStream siis, int frame)
            throws IOException {
        if (log.isDebugEnabled())
            log.debug("seek frame #" + (frame+1));
        Item item = getFirstItemOfFrame(frame);
        siis.seek(item.offset);
        iis.seek(item.startPos);
        this.frame = frame;
        if (log.isDebugEnabled())
            log.debug("seek " + item);
    }

    public void seekFooter() throws IOException {
        while (!lastItemSeen)
            next();
        iis.seek(last().nextItemPos());
        parser.parseHeader();
    }

}
