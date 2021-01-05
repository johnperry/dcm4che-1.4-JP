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

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.image.ColorModelParam;

import java.awt.image.ColorModel;
import java.util.WeakHashMap;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class ColorModelFactoryImpl extends ColorModelFactory {
	private static final Logger log = Logger.getLogger(ColorModelFactoryImpl.class);
	private static final WeakHashMap cache = new WeakHashMap();
   
   private final static byte[] P2D_IDENTITY = new byte[256];
   static { for (int i = 0; i < 256; ++i) P2D_IDENTITY[i] = (byte)i; }
   
   /** Creates a new instance of ColorModelFactoryImpl */
   public ColorModelFactoryImpl() {
   }
   
   public ColorModel getColorModel(ColorModelParam param) {
	  log.debug("Getting a color model from "+param);
      if (!param.isCacheable()) {
         return param.newColorModel();
      }
      ColorModel cm = (ColorModel)cache.get(param);
      if (cm == null) {
         cache.put(param, cm = param.newColorModel());
      }
      return cm;
   }
   
   public ColorModelParam makeParam(Dataset ds) {
      return makeParam(ds, null);
   }
   
   public ColorModelParam makeParam(Dataset ds, byte[] pv2dll) {
      String pmi =
         ds.getString(Tags.PhotometricInterpretation, "MONOCHROME2");
      if ("PALETTE COLOR".equals(pmi)) {
         return new PaletteColorParam(ds);
      }
      boolean mono1 = "MONOCHROME1".equals(pmi);
      if (!mono1 && !"MONOCHROME2".equals(pmi)) {
         throw new UnsupportedOperationException("pmi: " + pmi);
      }
      String pLUTShape = ds.getString(Tags.PresentationLUTShape);
      return new MonochromeParam(ds,
         pLUTShape == null ? mono1 : "INVERSE".equals(pLUTShape),
         pv2dll == null ? P2D_IDENTITY : pv2dll);
   }
   
}
