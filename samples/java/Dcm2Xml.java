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

import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.TagDictionary;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import gnu.getopt.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version $Revision: 3963 $ $Date: 2006-02-21 14:42:24 +0100 (Di, 21 Feb 2006) $
 */
public class Dcm2Xml {

    private static final DcmParserFactory pfact = DcmParserFactory
            .getInstance();

    private OutputStream out = System.out;

    private URL xslt = null;

    private LinkedList xsltParams = new LinkedList();

    private boolean xsltInc = false;

    private int[] excludeTags = {};
    
    private int excludeValueLengthLimit = Integer.MAX_VALUE;

    private TagDictionary dict = DictionaryFactory.getInstance()
            .getDefaultTagDictionary();

    private File baseDir;

    /** Creates a new instance of Dcm2xml */
    public Dcm2Xml() {
    }

    public final void setExcludeValueLengthLimit(int excludeValueLengthLimit) {
        this.excludeValueLengthLimit = excludeValueLengthLimit;
    }
    
    public final void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setTagDictionary(TagDictionary dict) {
        this.dict = dict;
    }

    public void setXslt(URL xslt) {
        this.xslt = xslt;
    }

    public void setXsltInc(boolean xsltInc) {
        this.xsltInc = xsltInc;
    }

    public void addXsltParam(String expr) {
        if (expr.indexOf('=') <= 0) { throw new IllegalArgumentException(expr); }
        this.xsltParams.add(expr);
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    private TransformerHandler getTransformerHandler()
            throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        TransformerHandler th = null;
        if (xslt != null) {
            if (xsltInc) {
                tf.setAttribute("http://xml.apache.org/xalan/features/incremental",
                                Boolean.TRUE);
            }
            th = tf.newTransformerHandler(new StreamSource(xslt.openStream(),
                    xslt.toExternalForm()));
            Transformer t = th.getTransformer();
            for (Iterator it = xsltParams.iterator(); it.hasNext();) {
                String s = (String) it.next();
                int eqPos = s.indexOf('=');
                t.setParameter(s.substring(0, eqPos), s.substring(eqPos + 1));
            }
        } else {
            th = tf.newTransformerHandler();
            th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        }
        th.setResult(new StreamResult(out));
        return th;
    }

    public void process(String file) throws IOException,
            TransformerConfigurationException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(file)));
        try {
            DcmParser parser = pfact.newDcmParser(in);
            parser.setSAXHandler2(getTransformerHandler(),
                    dict,
                    excludeTags,
                    excludeValueLengthLimit,
                    baseDir);
            parser.parseDcmFile(null, -1);
        } finally {
            try {
                out.close();
            } catch (IOException ignore) {
            }
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        LongOpt[] longopts = new LongOpt[2];
        longopts[0] = new LongOpt("TXT", LongOpt.NO_ARGUMENT, null, 'T');
        longopts[1] = new LongOpt("XSL", LongOpt.REQUIRED_ARGUMENT, null, 'S');

        Getopt g = new Getopt("dcm2xml.jar", args, "S:bo:ID:Xx:L:d:", longopts,
                true);

        Dcm2Xml dcm2xml = new Dcm2Xml();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'b':
                dcm2xml.setTagDictionary(null);
                break;
            case 'o':
                dcm2xml.setOut(new BufferedOutputStream(new FileOutputStream(g
                        .getOptarg())));
                break;
            case 'T':
                dcm2xml.setXslt(Dcm2Xml.class.getResource("/Dcm2Xml2.xsl"));
                break;
            case 'S':
                dcm2xml.setXslt(new File(g.getOptarg()).toURL());
                break;
            case 'I':
                dcm2xml.setXsltInc(true);
                break;
            case 'D':
                dcm2xml.addXsltParam(g.getOptarg());
                break;
            case 'x':
                dcm2xml.addExcludeTag(toTag(g.getOptarg()));
                break;
            case 'X':
                dcm2xml.addExcludeTag(Tags.PixelData);
                break;
            case 'L':
                dcm2xml.setExcludeValueLengthLimit(Integer.parseInt(g.getOptarg()));
                break;
            case 'd':
                dcm2xml.setBaseDir(new File(g.getOptarg()));
                break;
            case '?':
                exit("");
                break;
            }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc == 0) {
            exit("dcm2xml.jar: Missing argument\n");
        }

        if (argc > 1) {
            exit("dcm2xml.jar: To many arguments\n");
        }
        dcm2xml.process(args[optind]);
    }

    private void addExcludeTag(int tag) {
        int[] tmp = new int[excludeTags.length + 1];
        System.arraycopy(excludeTags, 0, tmp, 0, excludeTags.length);
        tmp[excludeTags.length] = tag;
        excludeTags = tmp;
    }

    private static int toTag(String s) {
        try {
            return (int) Long.parseLong(s, 16);
        } catch (NumberFormatException e) {
            return Tags.forName(s);
        }
    }

    private static void exit(String prompt) {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

    private static final String USAGE = 
              "Usage: java -jar dcm2xml.jar <dcm_file> [-o <xml_file>]\n"
            + "  [-bX] [-x <tag> [,...]] [-L <maxValLen>] [-d <basedir>]\n"
            + "  [[--TXT | --XSL <xsl_file>] [-I][-D<param>=<value> [,...]]]\n\n"
            + "Transform the specified DICOM file <dcm_file> into XML and optionally apply\n"
            + "XSLT with the specified XSL stylesheet <xsl_file> to the XML presentation.\n\n"
            + "Options:\n"
            + " -o <xml_file>      Place output in <xml_file> instead in standard output.\n"
            + " -b                 Brief format: exclude attribute names from XML output.\n"
            + " -X                 Exclude pixel data from XML output. Same as -xPixelData\n"
            + " -x <tag>           Exclude value of specified tag from XML output.\n"
            + "                    Format: ggggeeee or attribute name\n"
            + " -L <maxValLen>     Exclude values which length exceeds the specified limit\n"
            + "                    from XML output.\n"
            + " -d <basedir>       file excluded values into directory <basedir>.\n"
            + " -T, --TXT          Apply default XSLT to produce text output:\n"
            + "                     -Dmaxlen=<maximal line length> [79]\n"
            + "                     -Dvallen=<displayed value length> [64]\n"
            + "                     -Dvaltail=<truncation position from value tail>. [8]\n"
            + "                     -Dellipsis=<truncation mark>. ['...']\n"
            + " -S, --XSL <file>   Apply XSLT with specified XSL stylesheet <file>.\n"
            + " -I                 Enable incremental XSLT (only usable with XALAN)\n"
            + " -D<param>=<value>  Set XSL parameter to specified value.\n";
}