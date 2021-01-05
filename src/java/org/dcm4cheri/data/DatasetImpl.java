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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.Tags;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

/** Implementation of <code>Dataset</code> container objects.
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author     <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since March 2002
 * @version $Revision: 3994 $ $Date: 2006-05-18 00:10:23 +0200 (Do, 18 Mai 2006) $
 * @see "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 */
final class DatasetImpl extends BaseDatasetImpl
        implements org.dcm4che.data.Dataset {

    private final Dataset parent;
    private SpecificCharacterSet charset = null;
    private String privateCreatorID = null;
    private long itemOffset = -1L;
    DatasetImpl() {
        this(null);
    }
    
    DatasetImpl(Dataset parent) {
        this.parent = parent;
    }

    public void setPrivateCreatorID(String privateCreatorID) {
        this.privateCreatorID = privateCreatorID;
    }       

    public String getPrivateCreatorID() {
        return privateCreatorID != null ? privateCreatorID
                : parent != null ? parent.getPrivateCreatorID() : null;
    }
    
    public SpecificCharacterSet getSpecificCharacterSet() {
        return charset != null ? charset
                : parent != null ? parent.getSpecificCharacterSet() : null;
    }

    public final Dataset getParent() {
        return parent;
    }

    public final Dataset setItemOffset(long itemOffset) {
        this.itemOffset = itemOffset;
        return this;
    }

    public final long getItemOffset() {
        if (itemOffset != -1L || list.isEmpty())
            return itemOffset;
        
        long elm1pos = ((DcmElement)list.get(0)).getStreamPosition();
        return elm1pos == -1L ? -1L : elm1pos - 8L;
    }

    public DcmElement putSQ(int tag) {
        return put(new SQElement(tag, this));
    }

    protected DcmElement put(DcmElement newElem) {
        if ((newElem.tag() >>> 16) < 4) {
            log.warn("Ignore illegal attribute " + newElem);
            return newElem;
        }
        if (newElem.tag() == Tags.SpecificCharacterSet) {
            try {
                this.charset = SpecificCharacterSet.valueOf(newElem.getStrings(null));
            } catch (Exception ex) {
                log.warn("Failed to consider specified Charset!");
                this.charset = null;
            }
        }

        return super.put(newElem);
    }
    
    public DcmElement remove(int tag) {
        if (tag == Tags.SpecificCharacterSet)
            charset = null;
        return super.remove(tag);
    }
    
    public void clear() {
        super.clear();
        charset = null;
        totLen = 0;
    }
        
    public void readFile(InputStream in, FileFormat format, int stopTag)
            throws IOException, DcmValueException {
        DcmParserImpl parser = new DcmParserImpl(in);
        parser.setDcmHandler(getDcmHandler());
        parser.parseDcmFile(format, stopTag);
    }
    
    public void readDataset(InputStream in, DcmDecodeParam param, int stopTag)
            throws IOException, DcmValueException {
        DcmParserImpl parser = new DcmParserImpl(in);
        parser.setDcmHandler(getDcmHandler());
        parser.parseDataset(param, stopTag);
    }

    public void readFile(ImageInputStream in, FileFormat format, int stopTag)
            throws IOException, DcmValueException {
        DcmParserImpl parser = new DcmParserImpl(in);
        parser.setDcmHandler(getDcmHandler());
        parser.parseDcmFile(format, stopTag);
    }

    public void readFile(File f, FileFormat format, int stopTag)
    	throws IOException
	{
	    InputStream in = new BufferedInputStream(new FileInputStream(f));
	    try {
	        readFile(in, format, stopTag);
	    } finally {
	        try { in.close(); } catch (IOException ignore) {}
	    }
	}
    
}
