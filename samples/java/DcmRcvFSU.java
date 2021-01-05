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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirBuilder;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirBuilderPref;
import org.dcm4che.media.DirWriter;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.util.UIDGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 */
class DcmRcvFSU implements AssociationListener, Runnable
{      
   // Constants -----------------------------------------------------
   static final Logger log = Logger.getLogger("DcmRcv");
   
   // Attributes ----------------------------------------------------
   private static final Random RND = new Random();
   private static final DirBuilderFactory dirFact =
         DirBuilderFactory.getInstance();
   private static final DcmObjectFactory objFact =
         DcmObjectFactory.getInstance();

   private final DirBuilderPref dirPref = dirFact.newDirBuilderPref();
   private final boolean autocommit;
   private final File dicomdir;
   private final File dir;
   private final int[] fileIDTags;
   private final String fsid;
   private final String fsuid;
   private DirWriter writer = null;
   private DirBuilder builder = null;
   private final LinkedList queue = new LinkedList();
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public DcmRcvFSU(File dicomdir, Configuration cfg) {
      this.dicomdir = dicomdir;
      this.dir = dicomdir.getParentFile();
      fileIDTags = toTags(cfg.tokenize(cfg.getProperty(
            "fs-file-id", "StudyDate,StudyID,SeriesNumber,InstanceNumber")));
      fsid = cfg.getProperty("fs-id","", "<none>", "");
      fsuid = cfg.getProperty("fs-uid","", "<auto>", "");
      autocommit = !"<yes>".equals(cfg.getProperty("fs-lazy-update", ""));
      initDirBuilderPref(cfg);
   }
         
   private static int[] toTags(String[] names) {
      int[] retval = new int[names.length];
      for (int i = 0; i < names.length; ++i) {
         retval[i] = Tags.forName(names[i]);
      }
      return retval;
   }
   
   private void initDirBuilderPref(Configuration cfg) {
      HashMap map = new HashMap();
      for (Enumeration en = cfg.keys(); en.hasMoreElements();) {
         addDirBuilderPrefElem(map, (String)en.nextElement());
      }
      for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
         Map.Entry entry = (Map.Entry)it.next();
         dirPref.setFilterForRecordType(
            (String)entry.getKey(),
            (Dataset)entry.getValue());
      }
   }

   private void addDirBuilderPrefElem(HashMap map, String key) {
      if (!key.startsWith("dir."))
         return;
      
      int pos2 = key.lastIndexOf('.');
      String type = key.substring(4,pos2).replace('_',' ');
      Dataset ds = (Dataset)map.get(type);
      if (ds == null) {
         map.put(type, ds = objFact.newDataset());
      }
      ds.putXX(Tags.forName(key.substring(pos2+1)));
   }
      
   // Public --------------------------------------------------------
   public File toFile(Dataset ds) {
      File file = dir;
      for (int i = 0; i < fileIDTags.length; ++i) {
         file = new File(file, toFileID(ds, fileIDTags[i]));
      }
      File parent = file.getParentFile();
      while (file.exists()) {
         file = new File(parent, 
                  Integer.toHexString(RND.nextInt()).toUpperCase());
      }
      return file;
   }
      
    private String toFileID(Dataset ds, int tag) {
	String s = ds.getString(tag);
	if (s == null || s.length() == 0)
            return "__NULL__";
	char[] in = s.toUpperCase().toCharArray();
	char[] out = new char[Math.min(8,in.length)];
	for (int i = 0; i < out.length; ++i) {
            out[i] = in[i] >= '0' && in[i] <= '9'
		|| in[i] >= 'A' && in[i] <= 'Z'
		? in[i] : '_';
	}
	return new String(out);
    }

   public void schedule(final File file, final Dataset ds) {
      synchronized (queue) {
         queue.addLast(new Runnable(){
            public void run() {
               try {
                  update(file, ds);
               } catch (IOException ioe) {
                  ioe.printStackTrace();
               }
            }
         });
         queue.notify();
      }
   }
   // Runnable implementation ---------------------------------------
   public void run() {
      try {
         for(;;) {
            getJob().run();
         }
      } catch (InterruptedException ie) {
         ie.printStackTrace();
      }
   }
   
   // Y overrides ---------------------------------------------------
   public void write(Association src, PDU pdu) {
   }

   public void received(Association src, PDU pdu){
   }

   public void write(Association src, Dimse dimse){
   }

   public void received(Association src, Dimse dimse){
   }

   public void error(Association src, IOException ioe){
   }

   public void closing(Association src) {
   }

   public void closed(Association src) {
      if (writer != null && !autocommit) {
         try {
            writer.commit();
            log.info("M-WRITE " + dicomdir);
         } catch (IOException ioe) {
            ioe.printStackTrace();
         }
      }
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   private Runnable getJob() throws InterruptedException{
      synchronized (queue) {
         while (queue.isEmpty()) {
            queue.wait();
         }
         return (Runnable)queue.removeFirst();
      }
   }
   
   private synchronized void update(File file, Dataset ds)
   throws IOException {
      initBuilder();
      builder.addFileRef(writer.toFileIDs(file), ds);
      if (autocommit) {
         writer.commit();
         log.info("M-WRITE " + dicomdir);
      }
   }
   
   private void initBuilder()
   throws IOException {
      if (dicomdir.exists()) {
         if (builder != null)
            return;
         writer = dirFact.newDirWriter(dicomdir, null);
         log.info("M-WRITE " + dicomdir);
      } else {
         String uid = fsuid.length() != 0 ? fsuid
                        : UIDGenerator.getInstance().createUID();
         writer = dirFact.newDirWriter(dicomdir, uid, fsid, null, null, null);
      }
      builder = dirFact.newDirBuilder(writer, dirPref);
    }
      
   // Inner classes -------------------------------------------------
}
