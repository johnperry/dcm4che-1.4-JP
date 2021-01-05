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

package org.dcm4cheri.util;

import org.apache.log4j.Logger;

/**
 * Leader/Follower Thread Pool 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 6194 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020519 gunter zeilinger:</b>
 * <ul>
 * <li> initial import 
 * </ul>
 */
public class LF_ThreadPool
{
   // Constants -----------------------------------------------------
   private static final Logger log =
         Logger.getLogger(LF_ThreadPool.class);
   
   // Attributes ----------------------------------------------------
   private final Handler handler;
   private boolean shutdown = false;
   private Thread leader = null;
   private Object mutex = new Object();
   private int waiting = 0;
   private int running = 0;
   private int maxRunning = 0;
   private int maxWaiting = -1;
   private final int instNo = ++instCount;
   private int threadNo = 0;
   private final String name;
   
   // Static --------------------------------------------------------
   private static int instCount = 0;
   
   // Constructors --------------------------------------------------
   public LF_ThreadPool(Handler handler, String name) {
      if (handler == null)
         throw new NullPointerException();
      
      this.handler = handler;
      this.name = name;
   }
   
   // Public --------------------------------------------------------
   public int waiting()
   {
      return waiting;
   }
   
   public int running()
   {
      return running;
   }
   
   public boolean isShutdown()
   {
      return shutdown;
   }
   
   public int getMaxRunning()
   {
      return maxRunning;
   }
   
   public void setMaxRunning(int maxRunning)
   {
      if (maxRunning < 0)
         throw new IllegalArgumentException("maxRunning: " + maxRunning);
            
      this.maxRunning = maxRunning;
   }
   
   public int getMaxWaiting()
   {
      return maxWaiting;
   }
   
   public void setMaxWaiting(int maxWaiting)
   {
      if (maxWaiting < -1)
         throw new IllegalArgumentException("maxWaiting: " + maxWaiting);
            
      this.maxWaiting = maxWaiting;
   }
   
   public String toString()
   {
      return "LF_ThreadPool-" + instNo + "[leader:"
            + (leader == null ? "null" : leader.getName())
            + ", waiting:" + waiting
            + ", running: " + running + "(" + maxRunning
            + "), shutdown: " + shutdown + "]";
   }
   
   public void join()
   {
      log.debug("Thread: " + Thread.currentThread().getName() + " JOIN ThreadPool " + name);
      try {
	      while (!shutdown && (running == 0 || maxWaiting == -1 || waiting < maxWaiting)
	              && (maxRunning == 0 || (waiting + running) < maxRunning))
	      {
	         synchronized (mutex)
	         {
	            while (leader != null)
	            {
	               if (log.isDebugEnabled())
	                  log.debug("" + this + " - "
	                     + Thread.currentThread().getName() + " enter wait()");
	               ++waiting;
	               try { mutex.wait(); }
	               catch (InterruptedException ie)
	               {
	                  log.error(ie);
	               }
	               finally { --waiting; }
	               if (log.isDebugEnabled())
	                  log.debug("" + this + " - "
	                     + Thread.currentThread().getName() + " awaked");
	            }
	            if (shutdown)
	               return;
	
	            leader = Thread.currentThread();
	            if (log.isDebugEnabled())
	               log.debug("" + this + " - New Leader"); 
	            ++running;
	         }
	         try {  
	            do {
	               handler.run(this);
	            } while (!shutdown && leader == Thread.currentThread());
	         } catch (Throwable th) {
	            log.warn("Exception thrown in " + Thread.currentThread().getName(), th);
	            shutdown();
	         } finally { synchronized (mutex) { --running; } }
	      }
      } finally {
          log.debug("Thread: " + Thread.currentThread().getName() + " LEFT ThreadPool " + name);
      }
   }
   
   public boolean promoteNewLeader()
   {
      if (shutdown)
         return false;
      
      // only the current leader can promote the next leader
      if (leader != Thread.currentThread())
         throw new IllegalStateException();
      
      leader = null;
      
      // notify (one) waiting thread in join()
      synchronized (mutex) {
         if (waiting > 0)
         {
            if (log.isDebugEnabled())
               log.debug("" + this + " - promote new leader by notify"); 
            mutex.notify();
            return true;
         }
      }
            
      // if there is no waiting thread,
      // and the maximum number of running threads is not yet reached,
      if (maxRunning != 0 && running >= maxRunning) {
         if (log.isDebugEnabled())
            log.debug("" + this + " - Max number of threads reached"); 
         return false;
      }
      
      // start a new one
      if (log.isDebugEnabled())
         log.debug("" + this + " - promote new leader by add new Thread");
      addThread(
         new Runnable() {
            public void run() { join(); }
         }
      );
      
      return true;
   }
   
   public void shutdown() {
      if (log.isDebugEnabled())
         log.debug("" + this + " - shutdown"); 
      shutdown = true;
      leader = null;
      synchronized (mutex)
      {
         mutex.notifyAll();
      }
   }
         
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   // may be overloaded to take new thread from convential thread pool
   protected void addThread(Runnable r) {
       new Thread(r, name + "-" + (++threadNo)).start();
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   public interface Handler {
      void run(LF_ThreadPool pool);
   }
}
