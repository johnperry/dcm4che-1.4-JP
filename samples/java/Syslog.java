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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.dcm4che.server.PollDirSrv;
import org.dcm4che.server.PollDirSrvFactory;
import org.dcm4che.util.SyslogWriter;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class Syslog implements PollDirSrv.Handler {
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private static ResourceBundle messages = ResourceBundle.getBundle(
         "Syslog", Locale.getDefault());
   
   private SyslogWriter syslog = new SyslogWriter();
   private boolean stdout = false;
   private boolean quite = false;
   private int level = SyslogWriter.LOG_NOTICE;
   private final PollDirSrv pollDirSrv;
   
   // Static --------------------------------------------------------
   private static final LongOpt[] LONG_OPTS = new LongOpt[] {
      new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
      new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
   };
   
   private static void exit(String prompt, boolean error) {
      if (prompt != null)
         System.err.println(prompt);
      if (error)
         System.err.println(messages.getString("try"));
      System.exit(1);
   }

   public static void main(String args[]) throws Exception {
      Getopt g = new Getopt("syslog", args, "czHp:s:t:d:u:f:P:T:D:M:", LONG_OPTS);
      
      Syslog syslog = new Syslog();
      File file = null;
      File pollDir = null;
      long pollPeriod = 5000;
      int c;
      while ((c = g.getopt()) != -1) {
         switch (c) {
            case 'f':
               file = new File(g.getOptarg());
               break;
            case 'P':
               pollDir = new File(g.getOptarg());
               break;
            case 'D':
               syslog.pollDirSrv.setDoneDir(new File(g.getOptarg()));
               break;
            case 'T':
               pollPeriod = Integer.parseInt(g.getOptarg()) * 1000L;
               break;
            case 'p':
               syslog.syslog.setSyslogPort(Integer.parseInt(g.getOptarg()));
               break;
            case 'M':
               syslog.pollDirSrv.setDeltaLastModified(
                     Integer.parseInt(g.getOptarg()) * 1000L);
               break;
            case 's':
               syslog.setFacility(g.getOptarg());
               syslog.setLevel(g.getOptarg());               
               break;
            case 'c':
               syslog.stdout = true;
               break;
            case 'z':
               syslog.quite = true;
               break;
            case 'H':
               syslog.syslog.setPrintHostName(false);
               break;
            case 'd':
               syslog.syslog.setContentPrefix(g.getOptarg());
               break;
            case 't':
               syslog.syslog.setTag(g.getOptarg());
               break;
            case 'u':
               syslog.syslog.setSyslogHost(g.getOptarg());
               break;
            case 'v':
               exit(messages.getString("version"), false);
            case 'h':
               exit(messages.getString("usage"), false);
            case '?':
               exit(null, true);
               break;
         }
      }
      int optind = g.getOptind();
      int argc = args.length - optind;
      if (argc == 0 && file == null && pollDir == null) {
         exit(messages.getString("usage"), false);
      }
      
      if (argc > 0) {
         syslog.send(optind, args);
      }
      
      if (file != null) {
         syslog.send(file);
      }
      
      if (pollDir != null) {
         syslog.pollDirSrv.start(pollDir, pollPeriod);
      }
   }

   // Constructors --------------------------------------------------
   public Syslog() {
      this.pollDirSrv = PollDirSrvFactory.getInstance().newPollDirSrv(this);
   }
          
   // PollDirSrv.Handler Implementation  -------------------------------
   public void openSession() throws Exception {
   }
   
   public boolean process(File file) throws Exception {
      BufferedReader r = new BufferedReader(new FileReader(file));
      try {
         syslog.reset();
         int c;
         while ((c = r.read()) != -1) {
            syslog.write(c);
         }
      } finally {
         try { r.close(); } catch (IOException io) {}
      }
      send();
      return true;
   }
   
   public void closeSession() {
   }
   
   // Public --------------------------------------------------------
   
   // Private -------------------------------------------------------   
   private void send(int off, String[] args) throws IOException {
      syslog.writeHeader(level);
      for (int i = off; i < args.length; ++i) {
         syslog.write(args[i]);
         syslog.write(' ');
      }
      send();
   }
   
   private void send(File file) throws IOException {
      BufferedReader r = new BufferedReader(new FileReader(file));
      try {
         syslog.writeHeader(level);
         int c;
         while ((c = r.read()) != -1) {
            syslog.write(c);
         }
         send();
      } finally {
         try { r.close(); } catch (IOException ignore) {}
      }
   }
   
   private void setLevel(String priority) {
      int pos = priority.indexOf('.');
      level = (pos == -1) ? (Integer.parseInt(priority) & 7)
                             : SyslogWriter.forName(priority.substring(pos+1));
   }
   
   private void setFacility(String priority) {
      int pos = priority.indexOf('.');
      syslog.setFacility((pos == -1 )
               ? (Integer.parseInt(priority) & ~7)
               : SyslogWriter.forName(priority.substring(0, pos)));
   }
   
   private void send() throws IOException {
      if (stdout) {
         syslog.writeTo(System.out);
         System.out.println();
      }
      if (!quite) {
         syslog.flush();
      }
   }
}
