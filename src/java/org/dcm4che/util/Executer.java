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

package org.dcm4che.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.dcm4cheri.util.StringUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 4099 $ $Date: 2007-03-26 17:32:30 +0200 (Mo, 26 Mär 2007) $
 * @since 08.07.2004
 */
public class Executer {

    static final Logger log = Logger.getLogger(Executer.class);

    private final String cmd;

    private final Process child;

    private final Thread stdoutReader;

    private final Thread stderrReader;

    private static String[] tokenize(String cmd) {
        ArrayList cmdarray = new ArrayList();
        final int len = cmd.length();
        char[] c = new char[len + 1];
        cmd.getChars(0, len, c, 0);
        c[len] = ' ';
        char delim = '\0';
        for (int i = 0, off = 0; i <= len; ++i) {
            if (delim == '\0') {
                if (c[i] == ' ') continue;
                if (c[i] == '"') {
                    delim = '"';
                    off = i + 1;
                } else {
                    delim = ' ';
                    off = i;
                }
            } else {
                if (c[i] == delim) {
                    int count = i - off;
                    if (count > 0) cmdarray.add(new String(c, off, count));
                    delim = '\0';
                }
            }
        }
        return (String[]) cmdarray.toArray(new String[cmdarray.size()]);
    }

    public Executer(String cmd) throws IOException {
        this(cmd, null, null);
    }

    public Executer(String[] cmdarray) throws IOException {
        this(cmdarray, null, null);
    }

    public Executer(String cmd, OutputStream stdout, OutputStream stderr)
            throws IOException {
        this(tokenize(cmd), stdout, stderr);
    }

    public Executer(String[] cmdarray, OutputStream stdout, OutputStream stderr)
            throws IOException {
        this.cmd = StringUtils.toString(cmdarray, ' ');
        if (log.isDebugEnabled()) log.debug("invoke: " + cmd);
        this.child = Runtime.getRuntime().exec(cmdarray);
        this.stdoutReader = startCopy(child.getInputStream(), stdout);
        this.stderrReader = startCopy(child.getErrorStream(), stderr);
    }

    public final String cmd() {
        return cmd;
    }

    public int waitFor() throws InterruptedException {
        stdoutReader.join();
        stderrReader.join();
        int exit = child.waitFor();
        if (log.isDebugEnabled()) log.debug("exit(" + exit + "): " + cmd);
        return exit;
    }

    public void destroy() {
        child.destroy();
    }

    private Thread startCopy(final InputStream in, final OutputStream out) {
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    int len;
                    byte[] buf = new byte[512];
                    while ((len = in.read(buf)) != -1)
                        if (out != null) out.write(buf, 0, len);
                } catch (IOException e) {
                    log.warn("i/o error reading stdout/stderr of " + cmd, e);
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.warn("i/o exception on close of stdout/stderr of " + cmd, e);
                    }
                }
            }
        });
        t.start();
        return t;
    }
}