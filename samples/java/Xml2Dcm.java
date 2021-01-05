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

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.Dataset;

import org.xml.sax.SAXException;
import java.io.*;
import javax.xml.parsers.*;
import gnu.getopt.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
public class Xml2Dcm {

    private Dataset ds = DcmObjectFactory.getInstance().newDataset();

    private File baseDir = new File(".");

    /** Creates a new instance of Xml2Dcm */
    public Xml2Dcm() {
    }

    public final void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void read(DataInputStream in) throws IOException, DcmValueException {
        ds.clear();
        try {
            ds.readFile(in, null, -1);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    public void process(String xml_file, DataOutputStream out)
            throws IOException, DcmValueException,
            ParserConfigurationException, SAXException {
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser p = f.newSAXParser();
            p.parse(new File(xml_file), ds.getSAXHandler2(baseDir));
            ds.writeFile(out, null);
        } finally {
            try {
                out.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        Getopt g = new Getopt("xml2dcm.jar", args, "2i:d:");

        Xml2Dcm xml2dcm = new Xml2Dcm();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'd':
                xml2dcm.setBaseDir(new File(g.getOptarg()));
                break;
            case 'i':
                xml2dcm.read(new DataInputStream(new BufferedInputStream(
                        new FileInputStream(g.getOptarg()))));
                break;
            case '?':
                exit("");
                break;
            }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc < 2) {
            exit("xml2dcm.jar: Missing argument\n");
        }

        if (argc > 2) {
            exit("xml2dcm.jar: To many arguments\n");
        }
        xml2dcm.process(args[optind],
                new DataOutputStream(new BufferedOutputStream(
                        new FileOutputStream(args[optind + 1]))));
    }

    private static void exit(String prompt) {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

    private static final String USAGE = "Usage:\n\n"
            + " java -jar xml2dcm.jar [-i <dcm_file>] [-d <base_di>] <xml_file> <dcm_file>\n\n"
            + "Create or update DICOM file <dcm_file> according XML specification <xml_file>.\n\n"
            + "Options:\n"
            + " -i <dcm_file>  Update specified DICOM file but store it as new one.\n"
            + " -d <base_dir>  Specifies directory where referenced source files are located\n"
            + "                Default: current working directory.\n";
}