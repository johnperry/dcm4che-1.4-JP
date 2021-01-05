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

package org.dcm4cheri.media;

import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirReader;
import org.dcm4che.media.DirRecord;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      May, 2002
 * @version    $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
class DirReaderImpl implements DirReader
{

    final static DcmParserFactory pfactory = DcmParserFactory.getInstance();
    final static DcmObjectFactory factory = DcmObjectFactory.getInstance();
    /**  Description of the Field */
    protected final File file;
    /**  Description of the Field */
    protected final ImageInputStream in;
    /**  Description of the Field */
    protected final DcmParser parser;
    /**  Description of the Field */
    protected Dataset fsi;
    /**  Description of the Field */
    protected int offFirstRootRec;
    /**  Description of the Field */
    protected int offLastRootRec;
    /**  Description of the Field */
    protected int seqLength;
    /**  Description of the Field */
    protected long offFirstRootRecValPos;
    /**  Description of the Field */
    protected long offLastRootRecValPos;
    /**  Description of the Field */
    protected long seqValuePos;


    /**
     * Creates a new instance of DirReaderImpl
     *
     * @param  file  Description of the Parameter
     * @param  in    Description of the Parameter
     */
    DirReaderImpl(File file, ImageInputStream in)
    {
        this.file = file != null ? file.getAbsoluteFile() : null;
        this.in = in;
        this.parser = pfactory.newDcmParser(in);
    }


    DirReaderImpl initReader()
        throws IOException
    {
        this.fsi = factory.newDataset();
        parser.setDcmHandler(fsi.getDcmHandler());
        this.seqValuePos =
                parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.DirectoryRecordSeq);
        parser.setDcmHandler(null);
        if (parser.getReadTag() != Tags.DirectoryRecordSeq) {
            throw new DcmValueException("Missing Directory Record Sequence");
        }
        this.seqLength = parser.getReadLength();

        DcmElement offFirstRootRecElem =
                fsi.get(Tags.OffsetOfFirstRootDirectoryRecord);
        if (offFirstRootRecElem == null || offFirstRootRecElem.isEmpty()) {
            throw new DcmValueException(
                    "Missing Offset of First Directory Record");
        }
        this.offFirstRootRec = offFirstRootRecElem.getInt();
        this.offFirstRootRecValPos = offFirstRootRecElem.getStreamPosition() + 8;

        DcmElement offLastRootRecElem =
                fsi.get(Tags.OffsetOfLastRootDirectoryRecord);
        if (offLastRootRecElem == null || offLastRootRecElem.isEmpty()) {
            throw new DcmValueException(
                    "Missing Offset of Last Directory Record");
        }
        this.offLastRootRec = offLastRootRecElem.getInt();
        this.offLastRootRecValPos = offLastRootRecElem.getStreamPosition() + 8;
        return this;
    }


    /**
     *  Gets the fileSetInfo attribute of the DirReaderImpl object
     *
     * @return    The fileSetInfo value
     */
    public Dataset getFileSetInfo()
    {
        return fsi;
    }


    /**
     *  Gets the refFile attribute of the DirReaderImpl object
     *
     * @param  root     Description of the Parameter
     * @param  fileIDs  Description of the Parameter
     * @return          The refFile value
     */
    public File getRefFile(File root, String[] fileIDs)
    {
        File retval = new File(root, fileIDs[0]);
        for (int i = 1; i < fileIDs.length; ++i) {
            retval = new File(retval, fileIDs[i]);
        }
        return retval;
    }


    /**
     *  Gets the refFile attribute of the DirReaderImpl object
     *
     * @param  fileIDs  Description of the Parameter
     * @return          The refFile value
     */
    public File getRefFile(String[] fileIDs)
    {
        if (file == null) {
            throw new IllegalStateException("Unkown root directory");
        }
        return getRefFile(file.getParentFile(), fileIDs);
    }


    /**
     *  Gets the descriptorFile attribute of the DirReaderImpl object
     *
     * @param  root                   Description of the Parameter
     * @return                        The descriptorFile value
     * @exception  DcmValueException  Description of the Exception
     */
    public File getDescriptorFile(File root)
        throws DcmValueException
    {
        String[] fileID = fsi.getStrings(Tags.FileSetDescriptorFileID);
        if (fileID == null || fileID.length == 0) {
            return null;
        }
        return getRefFile(root, fileID);
    }


    /**
     *  Gets the descriptorFile attribute of the DirReaderImpl object
     *
     * @return                        The descriptorFile value
     * @exception  DcmValueException  Description of the Exception
     */
    public File getDescriptorFile()
        throws DcmValueException
    {
        if (file == null) {
            throw new IllegalStateException("Unkown root directory");
        }
        return getDescriptorFile(file.getParentFile());
    }


    /**
     *  Gets the empty attribute of the DirReaderImpl object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The empty value
     * @exception  IOException  Description of the Exception
     */
    public boolean isEmpty(boolean onlyInUse)
        throws IOException
    {
        return getFirstRecord(onlyInUse) == null;
    }


    /**
     *  Gets the firstRecord attribute of the DirReaderImpl object
     *
     * @return                  The firstRecord value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecord()
        throws IOException
    {
        return getFirstRecord(true);
    }


    /**
     *  Gets the firstRecord attribute of the DirReaderImpl object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The firstRecord value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecord(boolean onlyInUse)
        throws IOException
    {
        if (offFirstRootRec == 0) {
            return null;
        }
        DirRecord retval = new DirRecordImpl(parser, offFirstRootRec);
        if (onlyInUse && retval.getInUseFlag() == DirRecord.INACTIVE) {
            return retval.getNextSibling(onlyInUse);
        }
        return retval;
    }


    /**
     *  Gets the firstRecordBy attribute of the DirReaderImpl object
     *
     * @param  type             Description of the Parameter
     * @param  keys             Description of the Parameter
     * @param  ignorePNCase     Description of the Parameter
     * @return                  The firstRecordBy value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecordBy(String type, Dataset keys, boolean ignorePNCase)
        throws IOException
    {
        DirRecord dr = getFirstRecord(true);
        return (dr == null || dr.match(type, keys, ignorePNCase))
                 ? dr
                 : dr.getNextSiblingBy(type, keys, ignorePNCase);
    }


    /**
     *  Description of the Method
     *
     * @exception  IOException  Description of the Exception
     */
    public void close()
        throws IOException
    {
        in.close();
    }

}

