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

package org.dcm4cheri.server;

import org.dcm4che.server.PollDirSrv;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

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
public class PollDirSrvImpl implements PollDirSrv
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   static final Logger log = Logger.getLogger("dcm4che.server.PollDirSrv");
   private static int instCount = 0;
   private String name = "PollDirSrv-" + ++instCount;

   private final Handler handler;
   private Timer timer = null;
   private Comparator sortCrit = null;
   private File pollDir = null;;
   private File doneDir = null;
   private long pollPeriod;
   private int counter = 0;
   private int doneCount = 0;
   private int failCount = 0;
   private int openCount = 0;
   private int failOpenCount = 0;
   private long deltaLastModified = 1000;
   private long openRetryPeriod = 60000;
   
   private final FileFilter filter = new FileFilter() {      
      public boolean accept(File pathname) {
         String name = pathname.getName();
         for (int pos = 0; name.charAt(pos) == '#'; ++pos) {
            if (((counter >> pos) & 1 )!= 0)
               return false;
         }
         return pathname.lastModified() + deltaLastModified
               < System.currentTimeMillis();            
      }
   };
      
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public PollDirSrvImpl(Handler handler) {
      if (handler == null)
         throw new NullPointerException();
      
      this.handler = handler;
   }
   
   // Public --------------------------------------------------------
   public String toString() {      
      return timer == null ? name + "[not running]"
         : name + "[poll " + pollDir + " all " + (pollPeriod/1000f) + " s]";
   }
      
   // PollDirSrv implementation ----------------------------------------
   public void setSortCrit(Comparator sortCrit) {
      this.sortCrit = sortCrit;
   }
   
   public File getDoneDir() {
      return doneDir;
   }
   
   public void setDoneDir(File doneDir) {
      this.doneDir = doneDir;
   }
   
   public long getOpenRetryPeriod() {
      return openRetryPeriod;
   }
   
   public void setOpenRetryPeriod(long openRetryPeriod) {
      this.openRetryPeriod = openRetryPeriod;
   }
   
   public long getDeltaLastModified() {
      return deltaLastModified;
   }
   
   public void setDeltaLastModified(long deltaLastModified) {
      this.deltaLastModified = deltaLastModified;
   }

   public final int getDoneCount() {
      return doneCount;
   }
   
   public final int getFailCount() {
      return failCount;
   }
   
   public final int getOpenCount() {
      return openCount;
   }
   
   public final int getFailOpenCount() {
      return failOpenCount;
   }
   
   public void resetCounter() {
      counter = 0;
      doneCount = 0;
      failCount = 0;
      openCount = 0;
      failOpenCount = 0;
   }
   
   public void start(File pollDir, long pollPeriod) {
      if (!pollDir.isDirectory()) 
         throw new IllegalArgumentException("pollDir: " + pollDir);      
      if (pollPeriod < 0)
         throw new IllegalArgumentException("pollPeriod: " + pollPeriod);
      if (timer != null)
         throw new IllegalStateException("Already running");
            
      this.pollDir = pollDir;
      this.pollPeriod = pollPeriod;
      this.timer = new Timer(false);     
      log.info("Start " + this);
      timer.schedule(new TimerTask() {
            public void run() { execute(); }
         }, 0, pollPeriod);
   }

   public void stop() {
      if (timer == null)
         return;
      log.info("Stop " + this);
      timer.cancel();
      timer = null;
   }
      
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private void execute() {
      NDC.push(name);
      if (log.isDebugEnabled())
         log.debug("poll " + pollDir);
      
      File[] files = pollDir.listFiles(filter);
      if (files.length > 0) {
         for(;;) {
            try {
               log.debug("open session");
               handler.openSession();
                ++openCount;
               break;
            } catch (Exception e) {            
               ++failOpenCount;
               log.error("open session failed:", e);
               try {
                  Thread.sleep(openRetryPeriod);
               } catch (InterruptedException ie) {
                  log.warn(ie);
               }
            }
         }
         do {
            if (sortCrit != null) {
               Arrays.sort(files, sortCrit);                        
            }
            for (int i = 0; i < files.length; ++i) {
               try {
                  log.info("process " + files[i]);
                  if (handler.process(files[i])) {
                     ++doneCount;
                     success(files[i]);
                  }
               } catch (Exception e) {
                  ++failCount;
                  log.error("process " + files[i] + " failed!", e);
                  failed(files[i]);
               }
            }
            files = pollDir.listFiles(filter);
         } while (files.length > 0);
         handler.closeSession();
      }
      ++counter;
      NDC.pop();
   }
   
   private void success(File file) {
      if (doneDir != null) {
         moveFile(file, new File(doneDir, file.getName()));
      } else {
         if (!file.delete()) {
            log.error("could not delete " + file);
         }
      }
   }
      
   private void failed(File file) {
      moveFile(file, new File(file.getParentFile(), "#" + file.getName()));
   }
   
   private void moveFile(File from, File to) {
      if (from.renameTo(to)) {
         if (log.isDebugEnabled())
            log.debug("rename " + from + " to " + to);
      } else {
         log.error("could not rename " + from + " to " + to);
      }
   }
   // Inner classes -------------------------------------------------
}
