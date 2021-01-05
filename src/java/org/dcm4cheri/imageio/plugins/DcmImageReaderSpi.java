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

package org.dcm4cheri.imageio.plugins;

import org.dcm4che.data.DcmParseException;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmImageReaderSpi extends ImageReaderSpi {

    static final String vendorName = "TIANI MEDGRAPH AG";

    static final String version = "1.0";

    static final String[] names = { "DICOM" };

    static final String[] suffixes = { "dcm" };

    static final String[] MIMETypes = { "Application/dicom" };

    static final String readerClassName =
            "org.dcm4cheri.imageio.plugins.DcmImageReader";

    private static final String[] writerSpiNames = null;

    static final boolean supportsStandardStreamMetadataFormat = false;
    static final String nativeStreamMetadataFormatName = 
            org.dcm4che.imageio.plugins.DcmMetadata.nativeMetadataFormatName;
    static final String nativeStreamMetadataFormatClassName = 
            "org.dcm4che.imageio.plugins.DcmMetadataFormat";
    
    static final DcmImageReaderConf conf = DcmImageReaderConf.getInstance();
    static final boolean supportsStandardImageMetadataFormat = false;
    static final String nativeImageMetadataFormatName = null;
    static final String nativeImageMetadataFormatClassName = null;
    static final String[] extraImageMetadataFormatNames = null;
    static final String[] extraImageMetadataFormatClassNames = null;

    public DcmImageReaderSpi() {
            super(vendorName, version,
                  names, suffixes, MIMETypes,
                  readerClassName,
                  STANDARD_INPUT_TYPE, // Accept ImageInputStreams
                  writerSpiNames,
                  supportsStandardStreamMetadataFormat,
                  nativeStreamMetadataFormatName,
                  nativeStreamMetadataFormatClassName,
                  conf.getExtraStreamMetadataFormatNames(),
                  conf.getExtraStreamMetadataFormatClassNames(),
                  supportsStandardImageMetadataFormat,
                  nativeImageMetadataFormatName,
                  nativeImageMetadataFormatClassName,
                  extraImageMetadataFormatNames,
                  extraImageMetadataFormatClassNames);
    }

    public String getDescription(Locale locale) {
        return "DICOM image reader";
    }

    public boolean canDecodeInput(Object input) throws IOException {
        if (!(input instanceof ImageInputStream)) {
            return false;
        }
        
        DcmParser parser = DcmParserFactory.getInstance()
        		.newDcmParser((ImageInputStream)input);
        try {
            parser.detectFileFormat();
            return true;
        } catch (DcmParseException e) {
            return false;
        }
    }

    public ImageReader createReaderInstance(Object extension) {
        return new DcmImageReader(this);
    }

}
