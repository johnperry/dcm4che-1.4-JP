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

package org.dcm4cheri.data;

import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStream;
import org.xml.sax.ContentHandler;

/** Defines behavior of <code>DataSet</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 */
final class FileMetaInfoImpl extends DcmObjectImpl implements FileMetaInfo {

    static final byte[] DICM_PREFIX = { (byte) 'D', (byte) 'I', (byte) 'C',
            (byte) 'M'};

    static final byte[] VERSION = { 0, 1};

    private byte[] preamble = new byte[128];

    private String sopClassUID = null;

    private String sopInstanceUID = null;

    private String tsUID = null;

    private String implClassUID = null;

    private String implVersionName = null;

    public String toString() {
        return "FileMetaInfo[uid=" + sopInstanceUID + "\n\tclass="
                + DICT.lookup(sopClassUID) + "\n\tts=" + DICT.lookup(tsUID)
                + "\n\timpl=" + implClassUID + "-" + implVersionName + "]";
    }

    public final byte[] getPreamble() {
        return preamble != null ? (byte[]) preamble.clone() : null;
    }

    public final void setPreamble(byte[] preamble) {
        if (preamble == null) {
            this.preamble = null;
        } else {
        	if (preamble.length != 128) {
        		throw new IllegalArgumentException("preamble length: " + preamble.length);
        	}
            this.preamble = (byte[]) preamble.clone();
        }
    }
    
    FileMetaInfoImpl init(String sopClassUID, String sopInstUID, String tsUID,
            String implClassUID, String implVersName) {
        preamble = new byte[128];
        putOB(Tags.FileMetaInformationVersion, (byte[]) VERSION.clone());
        putUI(Tags.MediaStorageSOPClassUID, sopClassUID);
        putUI(Tags.MediaStorageSOPInstanceUID, sopInstUID);
        putUI(Tags.TransferSyntaxUID, tsUID);
        putUI(Tags.ImplementationClassUID, implClassUID);
        if (implVersName != null) {
            putSH(Tags.ImplementationVersionName, implVersName);
        }
        return this;
    }

    public String getMediaStorageSOPClassUID() {
        return sopClassUID;
    }

    public String getMediaStorageSOPInstanceUID() {
        return sopInstanceUID;
    }

    public String getTransferSyntaxUID() {
        return tsUID;
    }

    public String getImplementationClassUID() {
        return implClassUID;
    }

    public String getImplementationVersionName() {
        return implVersionName;
    }

    protected DcmElement put(DcmElement newElem) {
        final int tag = newElem.tag();
        if ((tag & 0xFFFF0000) != 0x00020000) { throw new IllegalArgumentException(
                newElem.toString()); }

        try {
            switch (tag) {
            case Tags.MediaStorageSOPClassUID:
                sopClassUID = newElem.getString(null);
                break;
            case Tags.MediaStorageSOPInstanceUID:
                sopInstanceUID = newElem.getString(null);
                break;
            case Tags.TransferSyntaxUID:
                tsUID = newElem.getString(null);
                break;
            case Tags.ImplementationClassUID:
                implClassUID = newElem.getString(null);
                break;
            case Tags.ImplementationVersionName:
                implVersionName = newElem.getString(null);
                break;
            }
        } catch (DcmValueException ex) {
            throw new IllegalArgumentException(newElem.toString());
        }
        return super.put(newElem);
    }

    public int length() {
        return grLen() + 12;
    }

    private int grLen() {
        int len = 0;
        for (int i = 0, n = list.size(); i < n; ++i) {
            DcmElement e = (DcmElement) list.get(i);
            len += e.length() + (VRs.isLengthField16Bit(e.vr()) ? 8 : 12);
        }
        return len;
    }

    public void write(DcmHandler handler) throws IOException {
        handler.startFileMetaInfo(preamble);
        handler.setDcmDecodeParam(DcmDecodeParam.EVR_LE);
        write(0x00020000, grLen(), handler);
        handler.endFileMetaInfo();
    }

    public void write(OutputStream out) throws IOException {
        write(new DcmStreamHandlerImpl(out));
    }

    public void write(ImageOutputStream out) throws IOException {
        write(new DcmStreamHandlerImpl(out));
    }

    public void write(ContentHandler ch, TagDictionary dict) throws IOException {
        write(new DcmHandlerAdapter(ch, dict));
    }

    public void write2(ContentHandler ch, TagDictionary dict,
            int[] excludeTags, int excludeValueLengthLimit, File basedir)
            throws IOException {
        write(new DcmHandlerAdapter2(ch, dict, excludeTags,
                excludeValueLengthLimit, basedir));
    }

    public void read(InputStream in) throws IOException {
        DcmParserImpl parser = new DcmParserImpl(in);
        parser.setDcmHandler(getDcmHandler());
        parser.parseFileMetaInfo();
    }
}

