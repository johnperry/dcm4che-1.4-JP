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

package org.dcm4che.data;

import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.Dataset;

import java.io.*;
import junit.framework.*;

/**
 *
 * @author gunter.zeilinger@tiani.com
 */
public class DatasetSerializerTest extends TestCase {
   
   public DatasetSerializerTest(java.lang.String testName) {
      super(testName);
   }
   
   public static void main(java.lang.String[] args) {
      junit.textui.TestRunner.run(suite());
   }
   
   public static Test suite() {
      TestSuite suite = new TestSuite(DatasetSerializerTest.class);
      return suite;
   }
   
   private static final File OUT_FILE = new File("data/TMP_TEST");
   private static final String EVR_LE = "data/examplef9.dcm";
   private static final String PART10_EVR_LE = "data/6AF8_10";
   private static final String JPEG_70 = "data/CT-MONO2-16-chest";
   private Dataset ds;
   
   protected void setUp() throws Exception {
      ds = DcmObjectFactory.getInstance().newDataset();
   }

   protected void tearDown() throws Exception {
      if (OUT_FILE.exists()) {
         OUT_FILE.delete();
      }
   }
   
   public void testEVR_LE() throws Exception {
      DataInputStream in = new DataInputStream(
      new BufferedInputStream(new FileInputStream(EVR_LE)));
      try {
         ds.readFile(in, null, -1);
      } finally {
         try { in.close(); } catch (Exception ignore) {}
      }
      ObjectOutputStream oo = new ObjectOutputStream(
      new BufferedOutputStream(new FileOutputStream(OUT_FILE)));
      try {
         oo.writeObject(ds);
      } finally {
         try { oo.close(); } catch (Exception ignore) {}
      }
      ObjectInputStream oin = new ObjectInputStream(
      new BufferedInputStream(new FileInputStream(OUT_FILE)));
      try {
         Dataset ds2 = (Dataset)oin.readObject();
         assertEquals(ds.size(), ds2.size());
      } finally {
         try { oin.close(); } catch (Exception ignore) {}
      }
   }
   
   public void testPART10_EVR_LE() throws Exception {
      DataInputStream in = new DataInputStream(
      new BufferedInputStream(new FileInputStream(PART10_EVR_LE)));
      try {
         ds.readFile(in, null, -1);
      } finally {
         try { in.close(); } catch (Exception ignore) {}
      }
      ObjectOutputStream oo = new ObjectOutputStream(
      new BufferedOutputStream(new FileOutputStream(OUT_FILE)));
      try {
         oo.writeObject(ds);
      } finally {
         try { oo.close(); } catch (Exception ignore) {}
      }
      ObjectInputStream oin = new ObjectInputStream(
      new BufferedInputStream(new FileInputStream(OUT_FILE)));
      try {
         Dataset ds2 = (Dataset)oin.readObject();
         assertEquals(ds.size(), ds2.size());
      } finally {
         try { oin.close(); } catch (Exception ignore) {}
      }
   }
   
   public void testJPEG_70() throws Exception {
      DataInputStream in = new DataInputStream(
      new BufferedInputStream(new FileInputStream(JPEG_70)));
      try {
         ds.readFile(in, null, -1);
      } finally {
         try { in.close(); } catch (Exception ignore) {}
      }
      ObjectOutputStream oo = new ObjectOutputStream(
      new BufferedOutputStream(new FileOutputStream(OUT_FILE)));
      try {
         oo.writeObject(ds);
      } finally {
         try { oo.close(); } catch (Exception ignore) {}
      }
      ObjectInputStream oin = new ObjectInputStream(
      new BufferedInputStream(new FileInputStream(OUT_FILE)));
      try {
         Dataset ds2 = (Dataset)oin.readObject();
         assertEquals(ds.size(), ds2.size());
      } finally {
         try { oin.close(); } catch (Exception ignore) {}
      }
   }
}
