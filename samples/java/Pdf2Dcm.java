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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4cheri.util.StringUtils;

/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 * @since Aug 23, 2005
 */
public class Pdf2Dcm {

	private static final int BUFFER_SIZE = 2048;

	public static void main(String args[]) throws Exception {
		Getopt g = new Getopt("pdf2dcm.jar", args, "D:");
        Configuration cfg =
            new Configuration(Pdf2Dcm.class.getResource("pdf2dcm.cfg"));

		int c;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'D':
                add(cfg, g.getOptarg());				
				break;
			case '?':
				exit("");
				break;
			}
		}
		int optind = g.getOptind();
		int argc = args.length - optind;
		if (argc < 2) {
			exit("pdf2dcm.jar: Missing argument\n");
		}

		if (argc > 2) {
			exit("pdf2dcm.jar: To many arguments\n");
		}
		encapsulate(cfg, new File(args[optind]) , new File(args[optind+1]));
	}

	private static void add(Configuration cfg, String s) {
        int pos = s.indexOf('=');
        if (pos == -1) {
            cfg.put(s, "");
        } else {
            cfg.put(s.substring(0, pos), s.substring(pos + 1));
        }
	}

	private static void exit(String prompt) {
		System.err.println(prompt);
		System.err.println(USAGE);
		System.exit(1);
	}

	private static final String USAGE = "Usage:\n"
			+ " java -jar pdf2dcm.jar [-D <tagpath>=<value>] ...  <pdf_file> <dcm_file>\n\n"
			+ "Convert PDF Document <pdf_file> in DICOM Encapsulated PDF Document <dcm_file>.\n\n"
			+ "Options:\n"
			+ " -D <tagpath>=<value> Set individual header attribute value.\n"
			+ "                      <tagpath> := <tag> or <tagpath>'/'<tag>\n"
			+ "                      <tag> := <ggggeeee> or <name>\n\n"
			+ "Example:\n"
			+ " java -jar pdf2dcm.jar -DDocumentTitle=\"Cardiac Echo\" \\\n"
			+ "   -D0040A043/00080100=28032-1 -D0040A043/00080102=LN \\\n"
			+ "   -D0040A043/00080104=\"CARDIAC ECHO\" cardiac.pdf cardiac.dcm\n";

	private static void encapsulate(Configuration cfg, File pdfFile,
			File dcmFile) throws IOException {		
		final DcmObjectFactory df = DcmObjectFactory.getInstance();
		Dataset ds = df.newDataset();
		for (Iterator it = cfg.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			put(ds, toTags(StringUtils.split((String) e.getKey(), '/')),
					(String) e.getValue());
		}
		addUIDs(ds);
		addContentDateTime(ds);
		ds.setFileMetaInfo(df.newFileMetaInfo(ds, UIDs.ExplicitVRLittleEndian));
		write(pdfFile, dcmFile, ds);
		System.out.println("Encapsulate " + pdfFile + " into " + dcmFile);
	}

	private static void write(File pdfFile, File dcmFile, Dataset ds)
			throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(pdfFile),
				BUFFER_SIZE);
		try {
			OutputStream out = new BufferedOutputStream(new FileOutputStream(
					dcmFile));
			try {
				ds.writeFile(out, null);
				final int doclen = (int) pdfFile.length();
				ds.writeHeader(out, DcmEncodeParam.EVR_LE,
						Tags.EncapsulatedDocument, VRs.OB, (doclen + 1) & ~1);
				copy(in, out);
				if ((doclen & 1) != 0)
					out.write(0);
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	private static void addUIDs(Dataset ds) {
		UIDGenerator uidgen = UIDGenerator.getInstance();
		if (!ds.contains(Tags.StudyInstanceUID)) {
			ds.putUI(Tags.StudyInstanceUID, uidgen.createUID());
		}
		if (!ds.contains(Tags.SeriesInstanceUID)) {
			ds.putUI(Tags.SeriesInstanceUID, uidgen.createUID());
		}
		if (!ds.contains(Tags.SOPInstanceUID)) {
			ds.putUI(Tags.SOPInstanceUID, uidgen.createUID());
		}
	}

	private static void addContentDateTime(Dataset ds) {
		if (!ds.contains(Tags.ContentDate)) {
			Date now = new Date();
			ds.putDA(Tags.ContentDate, now);
			ds.putTM(Tags.ContentTime, now);
		} else if (!ds.contains(Tags.ContentTime))
			ds.putTM(Tags.ContentTime);
	}

	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buf = new byte[BUFFER_SIZE];
		int read;
		while ((read = in.read(buf, 0, BUFFER_SIZE)) != -1)
			out.write(buf, 0, read);		
	}

	private static void put(Dataset ds, int[] tagPath, String val) {
		Dataset item = ds;
		int lastIndex = tagPath.length - 1;
		for (int i = 0; i < lastIndex; ++i) {
			Dataset tmp = item.getItem(tagPath[i]);
			item = tmp != null ? tmp : item.putSQ(tagPath[i]).addNewItem();
		}
		if (val.length() != 0)
			item.putXX(tagPath[lastIndex], val);
		else if (!item.contains(tagPath[lastIndex]))
			item.putXX(tagPath[lastIndex]);
	}

	private static int[] toTags(String[] tagStr) {
		int[] tags = new int[tagStr.length];
		for (int i = 0; i < tags.length; i++) {
			tags[i] = toTag(tagStr[i]);
		}
		return tags;
	}

	private static int toTag(String tagStr) {
		try {
			return (int) Long.parseLong(tagStr, 16);
		} catch (NumberFormatException e) {
			return Tags.forName(tagStr);
		}
	}

}
