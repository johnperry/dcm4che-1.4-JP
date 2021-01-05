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
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
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

package org.dcm4cheri.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.dcm4che.util.SystemUtils;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Id
 * @since Jun 26, 2006
 */
class ConfigurationUtils {

    public static void loadPropertiesForClass(Properties map, Class c) {
        String key = c.getName();
        String val = SystemUtils.getSystemProperty(key, null);
        URL url;
        if (val == null) {
            val = key.replace('.','/') + ".properties";
            url = getResource(c, val);
        } else {
            try {
                url = new URL(val);
            } catch (MalformedURLException e) {
                url = getResource(c, val);
            }
        }
        try {
            InputStream is = url.openStream();
            try {
                map.load(is);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new ConfigurationException("failed not load resource:", e); 
        }
    }

	private static URL getResource(Class c, String val) {
		URL url;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null || (url = cl.getResource(val)) == null) {
			if ((url = c.getClassLoader().getResource(val)) == null) {
				throw new ConfigurationException("missing resource: " + val);
			}
		}
		return url;
	}

}
