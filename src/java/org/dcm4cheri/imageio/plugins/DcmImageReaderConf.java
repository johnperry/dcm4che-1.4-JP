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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.util.SystemUtils;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmImageReaderConf {
    
    public static DcmImageReaderConf getInstance() {
        return instance;
    }
    
    private static Logger log = Logger.getLogger(DcmImageReaderConf.class);
    
    private static DcmImageReaderConf instance = new DcmImageReaderConf();
    private final ClassLoader classloader;
    private String[] extraStreamMetadataFormatNames = null;
    private List formatNameList = Collections.EMPTY_LIST;
    private String[] extraStreamMetadataFormatClassNames = null;
    private String[] extraStreamMetadataFormatFilterResource = null;
    private String[] extraStreamMetadataFormatStyleResource = null;
    private Dataset[] datasetFilter = null;
    private Templates[] transformerTemplates = null;

    /** Creates a new instance of DcmImageReaderConf */
    private DcmImageReaderConf() {        
        classloader = Thread.currentThread().getContextClassLoader();
        String conf = SystemUtils.getSystemProperty(
                "dcm4cheri.imageio.plugins.DcmImageReader.config",
                "DcmImageReader.properties");

        InputStream in = classloader.getResourceAsStream(conf);
        if (in == null) {
            return;
        }
        Properties p = new Properties();
        try {
            p.load(in);
        } catch (IOException ioe) {
            log.error(ioe);
            return;
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
        StringTokenizer tk = new StringTokenizer(
                p.getProperty("extraStreamMetadataFormatNames",""),
                " ,\t");
        int n = tk.countTokens();
        if (n == 0) {
            return;
        }
        extraStreamMetadataFormatNames = new String[n];
        extraStreamMetadataFormatClassNames = new String[n];
        extraStreamMetadataFormatFilterResource = new String[n];
        extraStreamMetadataFormatStyleResource = new String[n];
        for (int i = 0; i < n; ++i) {
            String name = tk.nextToken();
            extraStreamMetadataFormatNames[i] = name;
            extraStreamMetadataFormatClassNames[i] =
                    p.getProperty(name + ".class");
            extraStreamMetadataFormatFilterResource[i] =
                    p.getProperty(name + ".filter");
            extraStreamMetadataFormatStyleResource[i] =
                    p.getProperty(name + ".style");
        }
        formatNameList = Arrays.asList(extraStreamMetadataFormatNames);
        datasetFilter = new Dataset[n];
        transformerTemplates = new Templates[n];
    }

    public String[] getExtraStreamMetadataFormatNames() {
        return extraStreamMetadataFormatNames;
    }

    public String[] getExtraStreamMetadataFormatClassNames() {
        return extraStreamMetadataFormatClassNames;
    }

    static class ConfigurationError extends Error {
        ConfigurationError(String msg, Exception x) {
            super(msg,x);
        }
    }
    
    public boolean contains(String formatName) {
        return formatNameList.indexOf(formatName) != -1;
    }

    public Dataset getFilterDataset(String formatName) {
        int index = formatNameList.indexOf(formatName);
        if (index == -1
                || extraStreamMetadataFormatFilterResource[index] == null) {
            return null;
        }
        if (datasetFilter[index] != null) {
            return datasetFilter[index];
        }
        InputStream in = classloader.getResourceAsStream(
                extraStreamMetadataFormatFilterResource[index]);
        if (in == null) {
            throw new ConfigurationError("Could not open resource "
                    + extraStreamMetadataFormatFilterResource[index], null);
        }
        try {
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            SAXParser p = SAXParserFactory.newInstance().newSAXParser();
            p.parse(in, ds.getSAXHandler());
            return (datasetFilter[index] = ds);
        } catch (Exception ex) {
            throw new ConfigurationError("Could not parse resource "
                    + extraStreamMetadataFormatFilterResource[index], ex);
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
    }

    public TransformerHandler getTransformerHandler(String formatName) {
        int index = formatNameList.indexOf(formatName);
        if (index == -1
                || extraStreamMetadataFormatStyleResource[index] == null) {
            return null;
        }
        try {
            SAXTransformerFactory tf =
                    (SAXTransformerFactory)TransformerFactory.newInstance();        
            if (transformerTemplates[index] == null) {
                InputStream in = classloader.getResourceAsStream(
                        extraStreamMetadataFormatStyleResource[index]);
                if (in == null) {
                    throw new ConfigurationError("Could not open resource "
                            + extraStreamMetadataFormatStyleResource[index], null);
                }
                try {
                    transformerTemplates[index] =
                        tf.newTemplates( new StreamSource(in));
                } finally {
                    try { in.close(); } catch (IOException ignore) {}
                }
            }
            return tf.newTransformerHandler(transformerTemplates[index]);
        } catch (Exception ex) {
            throw new ConfigurationError("Could not parse resource "
                    + extraStreamMetadataFormatStyleResource[index], ex);
        }
    }
}
