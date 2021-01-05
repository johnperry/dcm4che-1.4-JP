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

package org.dcm4cheri.net;

import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;

import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplifeid and specialized version of
 * EDU.oswego.cs.dl.util.concurrent.FutureResult
 * in Doug Lee's util.concurrent package.
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author  <a href="http://g.oswego.edu/index.html">Doug Lee</a>
 * @version $Revision: 12524 $ $Date: 2009-12-18 12:20:36 +0100 (Fr, 18 Dez 2009) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class FutureRSPImpl
implements DimseListener, AssociationListener, FutureRSP {
   
   // Constants -----------------------------------------------------
   private long setAfterCloseTO = 500;
   
   // Attributes ----------------------------------------------------
   private boolean closed = false;
   private boolean ready = false;
   private Dimse rsp = null;
   private final ArrayList pending = new ArrayList();
   private IOException exception = null;
   private final Association assoc;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public FutureRSPImpl(Association assoc) {
       this.assoc = assoc;
       assoc.addAssociationListener(this);
   }
   
   // Public --------------------------------------------------------
   
   // FutureRSP implementation ----------------------------------------------
   public synchronized void set(Dimse rsp) {
      assoc.removeAssociationListener(this);
      this.rsp = rsp;
      ready = true;
      notifyAll();
   }
   
   public synchronized void setException(IOException ex) {
      assoc.removeAssociationListener(this);
      exception = ex;
      ready = true;
      notifyAll();
   }
   
   public synchronized Dimse get()
   throws InterruptedException, IOException {
      while (!ready && !closed) wait();
      
      // handle reverse order of last rsp and close indication, caused
      // by lausy Thread synchronisation
      if (!ready) wait(setAfterCloseTO);
      
      return doGet();
   }
   
   public synchronized List listPending() {
      return Collections.unmodifiableList(pending);      
   }
   
   public synchronized IOException getException() {
      return exception;
   }
   
   public synchronized boolean isReady() {
      return ready;
   }
   
   public synchronized Dimse peek() {
      return rsp;
   }
   
   // DimseListener implementation ---------------------------------
   public void dimseReceived(Association assoc, Dimse dimse) {
      if (dimse.getCommand().isPending()) {
          pending.add(dimse);
      } else {
         set(dimse);
      }
   }
   
   // AssociationListener implementation ----------------------------
   public void write(Association src, PDU pdu) {
   }
   
   public void received(Association src, Dimse dimse) {
   }
   
   public void error(Association src, IOException ioe) {
      setException(ioe);
   }
   
   public void closing(Association src) {
   }
   
   synchronized public void closed(Association src) {
      closed = true;
      notifyAll();
   }
   
   public void write(Association src, Dimse dimse) {
   }
   
   public void received(Association src, PDU pdu) {
   }
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private Dimse doGet() throws IOException {
      if (exception != null)
         throw exception;
      return rsp;
   }
   // Inner classes -------------------------------------------------
}
