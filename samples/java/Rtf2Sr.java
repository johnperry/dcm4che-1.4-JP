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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import org.dcm4che.Implementation;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.srom.Code;
import org.dcm4che.srom.Content;
import org.dcm4che.srom.Equipment;
import org.dcm4che.srom.Patient;
import org.dcm4che.srom.SRDocument;
import org.dcm4che.srom.SRDocumentFactory;
import org.dcm4che.srom.Series;
import org.dcm4che.srom.Study;
import org.dcm4che.srom.Template;
import org.dcm4che.srom.Patient.Sex;
import org.dcm4che.util.UIDGenerator;
import com.tetrasix.majix.rtf.RtfAnalyser;
import com.tetrasix.majix.rtf.RtfCompoundObject;
import com.tetrasix.majix.rtf.RtfDocument;
import com.tetrasix.majix.rtf.RtfObject;
import com.tetrasix.majix.rtf.RtfParagraph;
import com.tetrasix.majix.rtf.RtfReader;
import com.tetrasix.majix.rtf.RtfStyleSheet;

/**
 * @author gunter.zeilinger@tiani.com
 * @author Jacek.Ratzinger@tiani.com
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 * @since 07.08.2004
 *  
 */
public class Rtf2Sr {

    private static final String PERSON_OBSERVER_NAME = "121008@DCM";

    private static final String PERSON = "121006@DCM";

    private static final String OBSERVER_TYPE = "121005@DCM";

    private static final String LANG = "language";

    private static final String LANG_OF_CONTENT = "121049@DCM";

    private static final class Section {

        final String heading;

        final ArrayList paragraphs = new ArrayList();

        Section(String heading) {
            this.heading = heading;
        }

        void add(String text) {
            paragraphs.add(text);
        }

        int size() {
            return paragraphs.size();
        }

        String get(int i) {
            return (String) paragraphs.get(i);
        }
    }

    private static final Logger log = Logger.getLogger(Rtf2Sr.class);

    private static final ResourceBundle messages = ResourceBundle
            .getBundle("Rtf2Sr");

    private static final ResourceBundle codes = ResourceBundle
            .getBundle("Rtf2SrCodes");

    private static final UIDGenerator uidgen = UIDGenerator.getInstance();

    private static final SRDocumentFactory srFact = SRDocumentFactory
            .getInstance();

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final Template TID2000 = srFact.newTemplate("2000", "DCMR");

    private static final LongOpt[] LONG_OPTS = new LongOpt[] {             
            new LongOpt("datetime-format", LongOpt.REQUIRED_ARGUMENT, null, 'f'),
            new LongOpt("manufacturer", LongOpt.REQUIRED_ARGUMENT, null, 'M'),
            new LongOpt("modelname", LongOpt.REQUIRED_ARGUMENT, null, 'm'),
            new LongOpt("station", LongOpt.REQUIRED_ARGUMENT, null, 't'),
            new LongOpt("uid-root", LongOpt.REQUIRED_ARGUMENT, null, 'U'),
            new LongOpt("study-uid", LongOpt.REQUIRED_ARGUMENT, null, 'S'),
            new LongOpt("study-id", LongOpt.REQUIRED_ARGUMENT, null, 'I'),
            new LongOpt("series-uid", LongOpt.REQUIRED_ARGUMENT, null, 's'),
            new LongOpt("series-no", LongOpt.REQUIRED_ARGUMENT, null, 'n'),
            new LongOpt("inst-uid", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
            new LongOpt("inst-no", LongOpt.REQUIRED_ARGUMENT, null, 'N'),
            new LongOpt("styles", LongOpt.NO_ARGUMENT, null, 'y'),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),};

    public static void main(String[] args) {
        Getopt g = new Getopt("rtf2sr", args, "yhv", LONG_OPTS);
        Rtf2Sr rtf2sr = new Rtf2Sr();
        try {
            int c;
            while ((c = g.getopt()) != -1) {
                switch (c) {
                case 'M':
                    rtf2sr.manufacturer = g.getOptarg();
                    break;
                case 'm':
                    rtf2sr.modelname = g.getOptarg();
                    break;
                case 't':
                    rtf2sr.stationname = g.getOptarg();
                    break;
                case 'U':
                    rtf2sr.rootuid = g.getOptarg();
                    break;
                case 'I':
                    rtf2sr.studyid = g.getOptarg();
                    break;
                case 'S':
                    rtf2sr.studyuid = g.getOptarg();
                    break;
                case 's':
                    rtf2sr.seriesuid = g.getOptarg();
                    break;
                case 'u':
                    rtf2sr.iuid = g.getOptarg();
                    break;
                case 'f':
                    rtf2sr.dtf = g.getOptarg();
                    break;
                case 'n':
                    Integer.parseInt(rtf2sr.seriesno = g.getOptarg());
                    break;
                case 'N':
                    Integer.parseInt(rtf2sr.instno = g.getOptarg());
                    break;
                case 'y':
                    System.err.println(messages.getString("styles"));
                    System.exit(1);
                case 'v':
                    System.err.println(messages.getString("version"));
                    System.exit(1);
                case '?':
                case 'h':
                    System.err.println(messages.getString("usage"));
                    System.exit(1);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
            System.err.println(messages.getString("usage"));
            System.exit(1);
        }
        int optind = g.getOptind();
        if (args.length - optind < 2) {
            System.err.println(messages.getString("missing"));
            System.err.println(messages.getString("usage"));
            System.exit(1);
        }
        try {
            rtf2sr.transform(args[optind], args[optind + 1]);
        } catch (IOException e) {
            log.error("Failed to convert " + args[0] + " to " + args[1], e);
        }
        log.info("Converted " + args[0] + " to " + args[1] + " successfully");
    }

    private static Code lookupCodeFor(String meaning) {
        try {
            return makeCode(codes.getString(meaning), meaning);
        } catch (MissingResourceException e) {
            throw new IllegalArgumentException("No code configured for: "
                    + meaning);
        }
    }

    private static Code getCode(String value) {
        try {
            return makeCode(value, codes.getString(value));
        } catch (MissingResourceException e) {
            throw new IllegalArgumentException("Unkown code: " + value);
        }
    }

    private static Code makeCode(String value, String meaning) {
        final int delimPos = value.indexOf('@');
        return srFact.newCode(value.substring(0, delimPos), value
                .substring(delimPos + 1), meaning);
    }

    private String rootuid = Implementation.getClassUID();

    private String dtf = "yyyy-MM-dd HH:mm:ss";

    private String studyuid;

    private String seriesuid;

    private String cuid = UIDs.BasicTextSR;

    private String iuid;

    private String studyid = "";

    private String seriesno = "1";

    private String instno = "1";

    private HashMap srFields = new HashMap();

    private ArrayList srSections = new ArrayList();

    private Section curSection;

    private String manufacturer = "Tiani Medgraph AG";

    private String modelname = "dcm4che/rtf2sr";

    private String stationname;

    public void transform(String rtfFileName, String srFileName)
            throws IOException {
        reset();
        load(rtfFileName);
        store(srFileName, buildSRDocument());
    }

    private void reset() {
        curSection = null;
        srSections.clear();
        srFields.clear();
    }

    private void load(String rtfFileName) throws IOException {
        RtfReader rtfReader = new RtfReader(rtfFileName);
        RtfAnalyser rtfAnalyser = new RtfAnalyser(rtfReader, null);
        RtfDocument rtfDocument = rtfAnalyser.parse();
        init(rtfDocument.getStyleSheet(), rtfDocument);
    }

    private void init(RtfStyleSheet sheet, RtfCompoundObject compound) {
        if (compound instanceof RtfParagraph) {
            RtfParagraph rtfParagraph = (RtfParagraph) compound;
            String text = rtfParagraph.getData().trim();
            if (text.length() == 0) return;
            String style = sheet.getStyleName(rtfParagraph.getProperties()
                    .getStyle());
            if ("srHeading".equals(style))
                srSections.add(curSection = new Section(text));
            else if (curSection != null)
                curSection.add(text);
            else
                srFields.put(style, text);
        } else {
            for (int i = 0, n = compound.size(); i < n; i++) {
                RtfObject rtfObject = compound.getObject(i);
                if (rtfObject instanceof RtfCompoundObject) {
                    init(sheet, (RtfCompoundObject) rtfObject);
                }
            }
        }
    }

    private void store(String srFileName, SRDocument doc) throws IOException {
        Dataset ds = doc.toDataset();
        ds.setFileMetaInfo(dof.newFileMetaInfo(ds,
                        UIDs.ExplicitVRLittleEndian));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(
                srFileName));
        try {
            ds.writeFile(out, null);
        } finally {
            out.close();
        }
    }

    private SRDocument buildSRDocument() {
        Code title = lookupCodeFor(srField("srTitle"));
        Patient patient = srFact.newPatient(srField("srPatientID"),
                toPN(srField("srPatientName")),
                toSex(srField("srPatientSex", null)),
                toDateTime(srField("srPatientBirthdate", null)));
        String studyDesc = srField("srStudyDescription", null);
        Study study = srFact.newStudy(
                maskNullUID(srField("srStudyInstanceUID", studyuid)),
                srField("srStudyID", studyid),
                toDateTime(srField("srStudyDateTime", null)),
                toPN(srField("srReferringPhysican", null)),
                srField("srAccessionNumber", null),
                studyDesc,
                toCodes(studyDesc));
        Series series = srFact.newSRSeries(
                maskNullUID(srField("srSeriesInstanceUID", seriesuid)),
                Integer.parseInt(srField("srSeriesNumber", seriesno)),
                null);
        Equipment equipment = srFact.newEquipment(manufacturer,
                modelname,
                stationname);
        Date obsDateTime = toDateTime(srField("srObservationDateTime", null));
        SRDocument doc = srFact.newSRDocument(patient,
                study,
                series,
                equipment,
                cuid,
                maskNullUID(srField("srSOPInstanceUID", iuid)),
                Integer.parseInt(srField("srInstanceNumber", instno)),
                null,
                TID2000,
                title,
                true);
        doc.setContentDateTime(toDateTime(srField("srContentDateTime", null)));
        doc.setComplete(true);
        Date srVerificationDateTime = toDateTime(srField("srVerificationDateTime",
                null));
        if (srVerificationDateTime != null) {
            doc.setVerified(true);
            doc.addVerification(srFact.newVerification(srVerificationDateTime,
                    toPN(srField("srVerifierName")),
                    null,
                    null));
        }
        doc.appendChild(Content.RelationType.HAS_OBS_CONTEXT, doc
                .createCodeContent(null,
                        null,
                        getCode(LANG_OF_CONTENT),
                        getCode(codes.getString(LANG))));
        doc.appendChild(Content.RelationType.HAS_OBS_CONTEXT, doc
                .createCodeContent(null,
                        null,
                        getCode(OBSERVER_TYPE),
                        getCode(PERSON)));
        doc.appendChild(Content.RelationType.HAS_OBS_CONTEXT, doc
                .createPNameContent(null,
                        null,
                        getCode(PERSON_OBSERVER_NAME),
                        toPN(srField("srObserverName"))));
        for (int i = 0, n = srSections.size(); i < n; i++)
            appendSection(doc, (Section) srSections.get(i));
        return doc;
    }

    private void appendSection(SRDocument doc, Section sec) {
        Content container = doc.createContainerContent(null,
                null,
                lookupCodeFor(sec.heading),
                true);
        Code childType = getCode(codes.getString('%' + codes
                .getString(sec.heading)));
        for (int i = 0, n = sec.size(); i < n; i++) {
            container.appendChild(Content.RelationType.CONTAINS, doc
                    .createTextContent(null, null, childType, sec.get(i)));
        }
        doc.appendChild(Content.RelationType.CONTAINS, container);
    }

    private String srField(String name) {
        String val = (String) srFields.get(name);        
        if (val == null)
            throw new IllegalArgumentException("Missing value of " + name);
        return val;
    }

    private String srField(String name, String def) {
        String val = (String) srFields.get(name);        
        return val != null ? val : def;
    }

    private String maskNullUID(String uid) {
        return uid != null && uid.length() != 0 ? uid : uidgen.createUID(rootuid);
    }

    private String toPN(String s) {
        if (s == null || s.length() == 0) return null;
        return s.replace(' ', '^');
    }

    private Date toDateTime(String s) {
        if (s == null || s.length() == 0) return null;
        int l = Math.min(s.length(), dtf.length());
        try {
            return new SimpleDateFormat(dtf.substring(0, l)).parse(s
                    .substring(0, l));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Not a valid date/time:" + s);
        }
    }

    private Sex toSex(String s) {
        if (s == null || s.length() == 0) return null;
        return Patient.Sex.valueOf(lookupCodeFor(s).getCodeValue());
    }

    private Code[] toCodes(String s) {
        if (s == null || s.length() == 0) return null;
        try {
	        return new Code[] { lookupCodeFor(s)};
		} catch (Exception e) {
			return null;
		}
    }
}
