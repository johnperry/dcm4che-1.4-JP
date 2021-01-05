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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.PixelDataReader;
import org.dcm4che.image.PixelDataFactory;
import org.dcm4che.image.PixelDataWriter;

/**
 * @author jforaci
 */
public class PixelDataTest
{
    private final static PixelDataFactory pdFact = PixelDataFactory.getInstance();

    private static void readAndRewrite(File inFile, File outFile)
        throws IOException
    {
        //read
        ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(inFile)));
        DcmParser dcmParser = DcmParserFactory.getInstance().newDcmParser(iis);
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        dcmParser.setDcmHandler(ds.getDcmHandler());
        dcmParser.parseDcmFile(null, Tags.PixelData);
        PixelDataReader pdReader = pdFact.newReader(ds, iis,
            dcmParser.getDcmDecodeParam().byteOrder, dcmParser.getReadVR());
        System.out.println("reading " + inFile + "...");
        pdReader.readPixelData(false);
        //write
        ImageOutputStream out = ImageIO.createImageOutputStream(new BufferedOutputStream(
            new FileOutputStream(outFile)));
        DcmEncodeParam dcmEncParam = DcmEncodeParam.IVR_LE;
        ds.writeDataset(out, dcmEncParam);
        ds.writeHeader(out, dcmEncParam, Tags.PixelData, dcmParser.getReadVR(), dcmParser.getReadLength());
        System.out.println("writing " + outFile + "...");
        PixelDataWriter pdWriter = pdFact.newWriter(pdReader.getPixelDataArray(), false, ds, out,
            dcmParser.getDcmDecodeParam().byteOrder, dcmParser.getReadVR());
        pdWriter.writePixelData();
        out.flush();
        out.close();
        System.out.println("done!");
    }
    
    public static void main(String[] args)
    {
        if (args.length == 0) {
            System.err.println("PixelDataTest: Error: Too few parameters");
            System.out.println("Usage: PixelDataTest <dicom-file>");
            System.exit(1);
        }
        for (int i = 0; i < args.length; i++) {
            try {
                File inFile = new File(args[i]),
                     outFile = new File(inFile.getAbsolutePath() + ".TEST");
                readAndRewrite(inFile, outFile);
            }
            catch (IOException ioe) {
                System.err.println("FAILED: " + ioe);
            }
        }
    }
}
