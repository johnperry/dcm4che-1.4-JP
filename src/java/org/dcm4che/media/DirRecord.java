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

import java.io.IOException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      May, 2002
 * @version    $Revision: 5254 $ $Date: 2007-10-05 10:51:47 +0200 (Fr, 05 Okt 2007) $
 */
public interface DirRecord
{
    // Constants -----------------------------------------------------
    /**  Description of the Field */
    public final static int INACTIVE = 0x0000;
    /**  Description of the Field */
    public final static int IN_USE = 0xFFFF;
    public static final String PATIENT = "PATIENT";
    public static final String RT_PLAN = "RT PLAN";
    public static final String RT_TREAT_RECORD = "RT TREAT RECORD";
    public static final String RT_STRUCTURE_SET = "RT STRUCTURE SET";
    public static final String RT_DOSE = "RT DOSE";
    public static final String KEY_OBJECT_DOC = "KEY OBJECT DOC";
    public static final String SR_DOCUMENT = "SR DOCUMENT";
    public static final String PRESENTATION = "PRESENTATION";
    public static final String VOI_LUT = "VOI LUT";
    public static final String MODALITY_LUT = "MODALITY LUT";
    public static final String WAVEFORM = "WAVEFORM";
    public static final String CURVE = "CURVE";
    public static final String OVERLAY = "OVERLAY";
    public static final String IMAGE = "IMAGE";
    public static final String STORED_PRINT = "STORED PRINT";
    public static final String SERIES = "SERIES";
    public static final String STUDY = "STUDY";
    public static final String SPECTROSCOPY = "SPECTROSCOPY";
    public static final String RAW_DATA = "RAW DATA";
    public static final String REGISTRATION = "REGISTRATION";
    public static final String FIDUCIAL = "FIDUCIAL";
    public static final String ENCAP_DOC = "ENCAP DOC";
    public static final String VALUE_MAP = "VALUE MAP";
    public static final String HL7_STRUC_DOC = "HL7 STRUC DOC";
    public static final String STEREOMETRIC = "STEREOMETRIC";
    public static final String PRIVATE = "PRIVATE";

    public void reload() throws IOException, DcmValueException;
    
    /**
     *  Gets the type attribute of the DirRecord object
     *
     * @return    The type value
     */
    public String getType();


    /**
     *  Gets the inUseFlag attribute of the DirRecord object
     *
     * @return    The inUseFlag value
     */
    public int getInUseFlag();


    /**
     *  Gets the refFileIDs attribute of the DirRecord object
     *
     * @return    The refFileIDs value
     */
    public String[] getRefFileIDs();


    /**
     *  Gets the refSOPClassUID attribute of the DirRecord object
     *
     * @return    The refSOPClassUID value
     */
    public String getRefSOPClassUID();


    /**
     *  Gets the refSOPInstanceUID attribute of the DirRecord object
     *
     * @return    The refSOPInstanceUID value
     */
    public String getRefSOPInstanceUID();


    /**
     *  Gets the refSOPTransferSyntaxUID attribute of the DirRecord object
     *
     * @return    The refSOPTransferSyntaxUID value
     */
    public String getRefSOPTransferSyntaxUID();


    /**
     *  Gets the dataset attribute of the DirRecord object
     *
     * @return    The dataset value
     */
    public Dataset getDataset();


    /**
     *  Gets the firstChild attribute of the DirRecord object
     *
     * @return                  The firstChild value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstChild()
        throws IOException;


    /**
     *  Gets the firstChild attribute of the DirRecord object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The firstChild value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstChild(boolean onlyInUse)
        throws IOException;


    /**
     *  Gets the firstChildBy attribute of the DirRecord object
     *
     * @param  type             Description of the Parameter
     * @param  keys             Description of the Parameter
     * @param  ignorePNCase     Description of the Parameter
     * @return                  The firstChildBy value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstChildBy(String type, Dataset keys, boolean ignorePNCase)
        throws IOException;


    /**
     *  Gets the nextSibling attribute of the DirRecord object
     *
     * @return                  The nextSibling value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getNextSibling()
        throws IOException;


    /**
     *  Gets the nextSibling attribute of the DirRecord object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The nextSibling value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getNextSibling(boolean onlyInUse)
        throws IOException;


    /**
     *  Gets the nextSiblingBy attribute of the DirRecord object
     *
     * @param  type             Description of the Parameter
     * @param  keys             Description of the Parameter
     * @param  ignorePNCase     Description of the Parameter
     * @return                  The nextSiblingBy value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getNextSiblingBy(String type, Dataset keys, boolean ignorePNCase)
        throws IOException;


    /**
     *  Description of the Method
     *
     * @param  type          Description of the Parameter
     * @param  keys          Description of the Parameter
     * @param  ignorePNCase  Description of the Parameter
     * @return               Description of the Return Value
     */
    public boolean match(String type, Dataset keys, boolean ignorePNCase);
}

