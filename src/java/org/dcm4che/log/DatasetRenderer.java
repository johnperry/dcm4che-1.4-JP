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
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4che.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.or.ObjectRenderer;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.SystemUtils;

/**
 * @author franz.willer
 *
 * Log4j renderer for Dataset objects.
 * <p>
 * This renderer is used to improve performance for DEBUG level. 
 * <p>
 * <DL>
 * <DT>Configuration in log4j.xml (first tag in configuration tag!):</DT>
 * <DD>   &lt;renderer renderedClass="org.dcm4che.data.Dataset" renderingClass="org.dcm4che.log.DatasetRenderer" /&gt;</DD>
 * </DL>
 */
public class DatasetRenderer implements ObjectRenderer {

	private static Map dumpParam = new Hashtable();
	private static int excludeValueLengthLimit = 128;
	
	/**
	 * Configures the DatasetRenderer with default dump parameters.
	 * <p>
	 * <dl>
	 * <dt>dump parameters:"</dt>
	 * <dd>  maxlen: max len of a single line. (default=128)</dd>
	 * <dd>  vallen: max len of the (text) value. (default=64)</dd>
	 * <dd>  prefix: line prefix, used to indent the lines.(default='\t')</dd>
	 * <dd>  excludeValueLengthLimit:used to exclude attributes with values greater this limit. (default=128)</dd>
	 * </dl>
	 * This parameters can be defined in the system parameter 'DatasetRenderer.dumpParam':
	 * <p>
	 * &lt;maxlen&gt;,&lt;vallen&gt;,&lt;prefix&gt;,&lt;excludeValueLengthLimit&gt;
	 *
	 */
	static {
		String cfg = SystemUtils.getSystemProperty("DatasetRenderer.dumpParam", "128,64,\t,128");
		StringTokenizer st = new StringTokenizer( cfg, ",");
		if ( st.hasMoreTokens() ) dumpParam.put("maxlen", Integer.valueOf( st.nextToken() ));
		if ( st.hasMoreTokens() ) dumpParam.put("vallen", Integer.valueOf( st.nextToken() ));
		if ( st.hasMoreTokens() ) dumpParam.put("prefix", st.nextToken() );
		if ( st.hasMoreTokens() ) excludeValueLengthLimit = Integer.parseInt( st.nextToken() );
	}
	/**
	 * Returns dump parameter maxlen, vallen and prefix in a Map.
	 * 
	 * @return dump parameter.
	 */
	public static Map getDumpParam() {
		return dumpParam;
	}
	
	/**
	 * Returns the limit of value length to exclude attributes.
	 * <p>
	 * If an attribute value exceeds this limit, it will be excluded from the dump.
	 * 
	 * @param limit Number of characters from the String representation of the value.
	 */
	public static void setExcludeValueLengthLimit( int limit ) {
		excludeValueLengthLimit = limit;
	}
	/**
	 * Render a Dataset object.
	 * 
	 * @see org.apache.log4j.or.ObjectRenderer#doRender(java.lang.Object)
	 */
	public String doRender(Object arg0) {
		StringWriter w = new StringWriter();
        try {
        	Dataset ds = (Dataset) arg0;
            ds.dumpDataset(w, dumpParam, excludeValueLengthLimit);
            return w.toString();
        } catch (Exception e) {
        	e.printStackTrace( new PrintWriter( w ));
            return "Failed to dump dataset:" + w;
        }
	}

}
