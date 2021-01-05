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
import org.dcm4che.net.PDU;

import java.io.IOException;

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
class Multicaster implements AssociationListener {
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private final AssociationListener a, b;
   
   // Static --------------------------------------------------------
   public static AssociationListener add(AssociationListener a,
                                         AssociationListener b) {
      if (a == null)  return b;
      if (b == null)  return a;
      return new Multicaster(a, b);
   }
   
   public static AssociationListener remove(AssociationListener l,
                                            AssociationListener oldl) {
      if (l == oldl || l == null) {
         return null;
      } if (l instanceof Multicaster) {
         return ((Multicaster)l).remove(oldl);
      }
      return null;
   }
   // Constructors --------------------------------------------------
   private Multicaster(AssociationListener a, AssociationListener b)
   {
      this.a = a; this.b = b;
   }
   
   // Public --------------------------------------------------------
   
   // AssociationListener implementation ----------------------------
   public void write(Association src, PDU pdu) {
      a.write(src, pdu);
      b.write(src, pdu);
   }
   
   public void write(Association src, Dimse dimse) {
      a.write(src, dimse);
      b.write(src, dimse);
   }
   
   public void received(Association src, PDU pdu) {
      a.received(src, pdu);
      b.received(src, pdu);
   }
   
   public void received(Association src, Dimse dimse) {
      a.received(src, dimse);
      b.received(src, dimse);
   }
   
   public void error(Association src, IOException ioe) {
      a.error(src, ioe);
      b.error(src, ioe);
   }
   
   public void closing(Association src) {
      a.closing(src);
      b.closing(src);
   }
      
   public void closed(Association src) {
       a.closed(src);
       b.closed(src);
    }
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private AssociationListener remove(AssociationListener oldl) {
      if (oldl == a)  return b;
      if (oldl == b)  return a;
      AssociationListener a2 = remove(a, oldl);
      AssociationListener b2 = remove(b, oldl);
      if (a2 == a && b2 == b) {
         return this;
      }
      return add(a2, b2);
   }
   
   // Inner classes -------------------------------------------------
}
