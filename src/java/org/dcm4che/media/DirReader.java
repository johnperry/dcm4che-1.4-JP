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

package org.dcm4che.media;

import java.io.File;
import java.io.IOException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      May, 2002
 * @version    $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
public interface DirReader
{
    // Constants -----------------------------------------------------
    /**  Description of the Field */
    public final static int CONSISTENCY = 0x0000;
    /**  Description of the Field */
    public final static int INCONSISTENCY = 0xFFFF;

    // Public --------------------------------------------------------
    /**
     *  Gets the fileSetInfo attribute of the DirReader object
     *
     * @return    The fileSetInfo value
     */
    public Dataset getFileSetInfo();


    /**
     *  Gets the refFile attribute of the DirReader object
     *
     * @param  root     Description of the Parameter
     * @param  fileIDs  Description of the Parameter
     * @return          The refFile value
     */
    public File getRefFile(File root, String[] fileIDs);


    /**
     *  Gets the refFile attribute of the DirReader object
     *
     * @param  fileIDs  Description of the Parameter
     * @return          The refFile value
     */
    public File getRefFile(String[] fileIDs);


    /**
     *  Gets the descriptorFile attribute of the DirReader object
     *
     * @param  root                   Description of the Parameter
     * @return                        The descriptorFile value
     * @exception  DcmValueException  Description of the Exception
     */
    public File getDescriptorFile(File root)
        throws DcmValueException;


    /**
     *  Gets the descriptorFile attribute of the DirReader object
     *
     * @return                        The descriptorFile value
     * @exception  DcmValueException  Description of the Exception
     */
    public File getDescriptorFile()
        throws DcmValueException;


    /**
     *  Gets the empty attribute of the DirReader object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The empty value
     * @exception  IOException  Description of the Exception
     */
    public boolean isEmpty(boolean onlyInUse)
        throws IOException;


    /**
     *  Gets the firstRecord attribute of the DirReader object
     *
     * @return                  The firstRecord value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecord()
        throws IOException;


    /**
     *  Gets the firstRecord attribute of the DirReader object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The firstRecord value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecord(boolean onlyInUse)
        throws IOException;


    /**
     *  Gets the firstRecordBy attribute of the DirReader object
     *
     * @param  type             Description of the Parameter
     * @param  keys             Description of the Parameter
     * @param  ignorePNCase     Description of the Parameter
     * @return                  The firstRecordBy value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecordBy(String type, Dataset keys, boolean ignorePNCase)
        throws IOException;


    /**
     *  Description of the Method
     *
     * @exception  IOException  Description of the Exception
     */
    public void close()
        throws IOException;

}

