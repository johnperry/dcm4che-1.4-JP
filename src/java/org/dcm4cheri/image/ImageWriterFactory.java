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

package org.dcm4cheri.image;

import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 15249 $ $Date: 2011-04-05 17:03:38 +0200 (Di, 05 Apr 2011) $
 * @since 7.4.2006
 */
public class ImageWriterFactory {
        
    private static final ImageWriterFactory instance = new ImageWriterFactory();
    public static final ImageWriterFactory getInstance() {
        return instance;
    }
    
    private final Properties map = new Properties();
        
    private ImageWriterFactory() {
        ConfigurationUtils.loadPropertiesForClass(map, ImageWriterFactory.class);
    }
    
    public ImageWriter getWriterForTransferSyntax(String tsuid) {
        String s = map.getProperty(tsuid);
        if (s == null)
            throw new UnsupportedOperationException(
                    "No Image Writer available for Transfer Syntax:" + tsuid);
        int delim = s.indexOf(',');
        if (delim == -1)
            throw new ConfigurationException("Missing ',' in " + tsuid + "=" + s); 
        final String formatName = s.substring(0, delim);
        final String className = s.substring(delim+1);

        //try for the class listed in the properties
        for (Iterator it = ImageIO.getImageWritersByFormatName(formatName);
                it.hasNext();) {
            ImageWriter r = (ImageWriter) it.next();
            if (className.equals(r.getClass().getName()))
                return r;
        }
        //no luck; if it's jpeg, then try for a non-CLib version
		final String altClassName = "com.sun.imageio.plugins.jpeg.JPEGImageWriter";
        if (formatName.equals("jpeg")) {
			for (Iterator it = ImageIO.getImageWritersByFormatName(formatName);
					it.hasNext();) {
				ImageWriter r = (ImageWriter) it.next();
				if (className.equals(r.getClass().getName()))
					return r;
	        }
		}
		//no luck, throw the Exception
        throw new ConfigurationException("No Image Writer of class " + className
                + " available for format:" + formatName); 
    }

    public boolean patchJAIJpegLS() {
        return map.containsKey("patchJAIJpegLS");
    }
}
