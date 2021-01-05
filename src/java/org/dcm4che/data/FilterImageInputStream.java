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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
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

package org.dcm4che.data;

import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 4088 $ $Date: 2007-02-26 11:38:27 +0100 (Mo, 26 Feb 2007) $
 * @since Feb 26, 2007
 */
public class FilterImageInputStream implements ImageInputStream {

    /**
     * The input stream to be filtered. 
     */
    protected volatile ImageInputStream in;

    /**
     * Creates a <code>FilterImageInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param   in   the underlying input stream, or <code>null</code> if 
     *          this instance is to be created without an underlying stream.
     */
    protected FilterImageInputStream(ImageInputStream in) {
        this.in = in;
    }

    public void close() throws IOException {
        in.close();
    }

    public void flush() throws IOException {
        in.flush();
    }

    public void flushBefore(long pos) throws IOException {
        in.flushBefore(pos);
    }

    public int getBitOffset() throws IOException {
        return in.getBitOffset();
    }

    public ByteOrder getByteOrder() {
        return in.getByteOrder();
    }

    public long getFlushedPosition() {
        return in.getFlushedPosition();
    }

    public long getStreamPosition() throws IOException {
        return in.getStreamPosition();
    }

    public boolean isCached() {
        return in.isCached();
    }

    public boolean isCachedFile() {
        return in.isCachedFile();
    }

    public boolean isCachedMemory() {
        return in.isCachedMemory();
    }

    public long length() throws IOException {
        return in.length();
    }

    public void mark() {
        in.mark();
    }

    public int read() throws IOException {
        return in.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    public int readBit() throws IOException {
        return in.readBit();
    }

    public long readBits(int numBits) throws IOException {
        return in.readBits(numBits);
    }

    public boolean readBoolean() throws IOException {
        return in.readBoolean();
    }

    public byte readByte() throws IOException {
        return in.readByte();
    }

    public void readBytes(IIOByteBuffer buf, int len) throws IOException {
        in.readBytes(buf, len);
    }

    public char readChar() throws IOException {
        return in.readChar();
    }

    public double readDouble() throws IOException {
        return in.readDouble();
    }

    public float readFloat() throws IOException {
        return in.readFloat();
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        in.readFully(b, off, len);
    }

    public void readFully(byte[] b) throws IOException {
        in.readFully(b);
    }

    public void readFully(char[] c, int off, int len) throws IOException {
        in.readFully(c, off, len);
    }

    public void readFully(double[] d, int off, int len) throws IOException {
        in.readFully(d, off, len);
    }

    public void readFully(float[] f, int off, int len) throws IOException {
        in.readFully(f, off, len);
    }

    public void readFully(int[] i, int off, int len) throws IOException {
        in.readFully(i, off, len);
    }

    public void readFully(long[] l, int off, int len) throws IOException {
        in.readFully(l, off, len);
    }

    public void readFully(short[] s, int off, int len) throws IOException {
        in.readFully(s, off, len);
    }

    public int readInt() throws IOException {
        return in.readInt();
    }

    public String readLine() throws IOException {
        return in.readLine();
    }

    public long readLong() throws IOException {
        return in.readLong();
    }

    public short readShort() throws IOException {
        return in.readShort();
    }

    public int readUnsignedByte() throws IOException {
        return in.readUnsignedByte();
    }

    public long readUnsignedInt() throws IOException {
        return in.readUnsignedInt();
    }

    public int readUnsignedShort() throws IOException {
        return in.readUnsignedShort();
    }

    public String readUTF() throws IOException {
        return in.readUTF();
    }

    public void reset() throws IOException {
        in.reset();
    }

    public void seek(long pos) throws IOException {
        in.seek(pos);
    }

    public void setBitOffset(int bitOffset) throws IOException {
        in.setBitOffset(bitOffset);
    }

    public void setByteOrder(ByteOrder byteOrder) {
        in.setByteOrder(byteOrder);
    }

    public int skipBytes(int n) throws IOException {
        return in.skipBytes(n);
    }

    public long skipBytes(long n) throws IOException {
        return in.skipBytes(n);
    }
}
