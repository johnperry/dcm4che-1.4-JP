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
 * Thomas Hacklaender, FTM Institut fuer Telematik in der Medizin GmbH
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Thomas Hacklaender <hacklaender@iftm.de>
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

package org.dcm4che.imageio.plugins;

import java.util.Locale;

import javax.imageio.ImageWriteParam;

import org.dcm4che.data.DcmEncodeParam;


/**
 * A class describing how a DICOM stream shold be encoded.
 *
 * @author   Thomas Hacklaender
 * @version  2002.06.16
 */
public abstract class DcmImageWriteParam extends ImageWriteParam {

  
  /**
   * Constructs an empty ImageWriteParam. It is up to the subclass to set up the 
   * instance variables properly.
   */
  public DcmImageWriteParam() {
    super();
  }

  
  /**
   * Constructs an ImageWriteParam set to use a given Locale.
   * @param locale a Locale to be used to localize compression type names and 
   *               quality descriptions, or null.
   */
  public DcmImageWriteParam(Locale locale) {
    super(locale);
  }
  
  
  /**
   * Set the property writeFMI.
   * @param writeFMI true, if the file-meta-information block (Part 10) should
   *                 be written.
   */
  public abstract void setWriteFMI(boolean writeFMI);
  
  
  /**
   * Returns the property writeFMI.
   * @return true, if the file-meta-information block (Part 10) should be written.
   *         The default value is true.
   */
  public abstract boolean isWriteFMI();
  
  public abstract void setDcmEncodeParameters(DcmEncodeParam dcmEncodeParams);
  public abstract DcmEncodeParam getDcmEncodeParameters();
  
  //public abstract void setPixelRepresentation(boolean signed);
  //public abstract boolean getPixelRepresentation();
  
  /**
   * Set the property monochrome2.
   * @param monochrome2 true, if the Photometric Interpretation should be 
   *                    MONOCHROME2. Otherwise it will be set to MONOCHROME1.
   */
  public abstract void setMONOCHROME2(boolean monochrome2);
  
  
  /**
   * Returns the property monochrome2.
   * @return true, if the Photometric Interpretation should be MONOCHROME2.
   *         The default value is true.
   */
  public abstract boolean isMONOCHROME2();
  
  
  /**
   * Set the property bitsStored.
   * The number of bits stored. Should be smaller than BitsAllocated (depending
   * on the Raster of the BufferedImage to write).
   * @param bitsStored the number of bits to store. A value of -1 denotes, that
   *                   the DcmImageWriter should choose the best value.
   */
  //public abstract void setBitsStored(int bitsStored);
  
  
  /**
   * Returns the property bitsStored.
   * @return the number of bits to store. A value of -1 denotes, that the
   *         DcmImageWriter should choose the best value.
   */
  //public abstract int getBitsStored();
  
  
  /**
   * Set the property highBit.
   * The highest bit position in the pixel data. Should be smaller than BitsStored. 
   * A value of -1 denotes, that the DcmImageWriter should choose the best value.
   * @param highBit the highest bit position in the pixel data. A value of -1 
   *                denotes, that the DcmImageWriter should choose the best value.
   */
  //public abstract void setHighBit(int highBit);
  
  
  /**
   * Returns the property highBit.
   * @return the highest bit position in the pixel data. A value of -1 denotes,
   *         that the DcmImageWriter should choose the best value.
   */
  //public abstract int getHighBit();
  
  
  /**
   * Set the property writeAlwaysRGB.
   * @param writeAlwaysRGB true, if all images should be written with Photometric 
   *                       Interpretation RGB.
   */
  public abstract void setWriteAlwaysRGB(boolean writeAlwaysRGB);
  
  
  /**
   * Returns the property writeAlwaysRGB.
   * @return true, if all images should be written with the Photometric 
   *         Interpretation RGB. The default value is false.
   */
  public abstract boolean isWriteAlwaysRGB();
  
  
  /**
   * Set the property writeIndexedAsRGB.
   * @param writeIndexedAsRGB true, if BufferedImages of type TYPE_BYTE_INDEXED  
   *                          should be written with the Photometric Interpretation RGB.
   */
  public abstract void setWriteIndexedAsRGB(boolean writeIndexedAsRGB);
  
  
  /**
   * Returns the property writeIndexedAsRGB.
   * @return true, if BufferedImages of type TYPE_BYTE_INDEXED  
   *         should be written with the Photometric Interpretation RGB.
   *         The default value is true.
   */
  public abstract boolean isWriteIndexedAsRGB();
  
}
