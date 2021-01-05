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

import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.VRMap;

import org.xml.sax.ContentHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface DcmParser {

    InputStream getInputStream();

    ImageInputStream getImageInputStream();

    long getStreamPosition();

    void setStreamPosition(long pos);

    void seek(long pos) throws IOException;
    
    void unreadHeader();

    void setDcmHandler(DcmHandler handler);

    void setSAXHandler(ContentHandler hc, TagDictionary dict);

    void setSAXHandler2(ContentHandler hc, TagDictionary dict,
            int[] excludeTags, int excludeValueLengthLimit, File basedir);

    void setVRMap(VRMap vrMap);

    void setDcmDecodeParam(DcmDecodeParam decodeParam) throws IOException;

    DcmDecodeParam getDcmDecodeParam();

    void setMaxValueLength(int maxValLen);

    int getMaxValueLength();

    FileFormat detectFileFormat() throws IOException;

    int parseHeader() throws IOException;

    long parseFileMetaInfo(boolean preamble, DcmDecodeParam param)
            throws IOException;

    long parseFileMetaInfo() throws IOException;

    long parseCommand() throws IOException;

    long parseDataset(String tuid, int stopTag) throws IOException;

    long parseDataset(DcmDecodeParam param, int stopTag) throws IOException;

    long parseDcmFile(FileFormat format, int stopTag) throws IOException;
    
    long parseDcmFile(FileFormat format, int stopTag, int length) throws IOException;

    long parseItemDataset() throws IOException;

    int getReadTag();

    int getReadVR();

    int getReadLength();

    boolean hasSeenEOF();
}

