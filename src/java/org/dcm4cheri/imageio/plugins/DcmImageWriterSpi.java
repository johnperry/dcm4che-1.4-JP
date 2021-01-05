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

package org.dcm4cheri.imageio.plugins;

import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.spi.*;


/**
 *
 * @author   Thomas Hacklaender
 * @version  2002.6.16
 */
public class DcmImageWriterSpi extends ImageWriterSpi {
  
  static final private String      vendorName = "IFTM GmbH";
  static final private String      version = "1.0";
  static final private String[]    names = { "DICOM" };
  static final private String[]    suffixes = { "dcm" };
  static final private String[]    MIMETypes = { "Application/dicom" };
  static final private String      writerClassName = "org.dcm4cheri.imageio.plugins.DcmImageWriter";
  static final private Class[]     outputTypes = { ImageOutputStream.class };
  static final private String[]    readerSpiNames = null;
  static final private boolean     supportsStandardStreamMetadataFormat = false;
  static final private String      nativeStreamMetadataFormatName = org.dcm4che.imageio.plugins.DcmMetadata.nativeMetadataFormatName;
  static final private String      nativeStreamMetadataFormatClassName = "org.dcm4che.imageio.plugins.DcmMetadataFormat";
  static final private String[]    extraStreamMetadataFormatNames = null;  // ????
  static final private String[]    extraStreamMetadataFormatClassNames = null; // ????
  static final private boolean     supportsStandardImageMetadataFormat = false;
  static final private String      nativeImageMetadataFormatName = null;
  static final private String      nativeImageMetadataFormatClassName = null;
  static final private String[]    extraImageMetadataFormatNames = null;
  static final private String[]    extraImageMetadataFormatClassNames = null;
  
  
  /**
   * Constructs a blank ImageWriterSpi.
   */
  public DcmImageWriterSpi() {
    super(
      vendorName,
      version,
      names,
      suffixes,
      MIMETypes,
      writerClassName,
      outputTypes,
      readerSpiNames,
      supportsStandardStreamMetadataFormat,
      nativeStreamMetadataFormatName,
      nativeStreamMetadataFormatClassName,
      extraStreamMetadataFormatNames,
      extraStreamMetadataFormatClassNames,
      supportsStandardImageMetadataFormat,
      nativeImageMetadataFormatName,
      nativeImageMetadataFormatClassName,
      extraImageMetadataFormatNames,
      extraImageMetadataFormatClassNames 
    );
  }
  
  
  /**
   * Returns true if the ImageWriter implementation associated with this service 
   * provider is able to encode an image with the given layout. The layout (i.e., 
   * the image's SampleModel and ColorModel) is described by an ImageTypeSpecifier 
   * object.<br>
   * A return value of true is not an absolute guarantee of successful encoding; 
   * the encoding process may still produce errors due to factors such as I/O 
   * errors, inconsistent or malformed data structures, etc. The intent is that 
   * a reasonable inspection of the basic structure of the image be performed in 
   * order to determine if it is within the scope of the encoding format.<br>
   * @param type an ImageTypeSpecifier specifying the layout of the image to be 
   *             written.
   * @return allways true.
   * @throws IllegalArgumentException if type is null.
   */
  public boolean canEncodeImage(ImageTypeSpecifier type)
      throws IllegalArgumentException
  {
    return true;
  }
  
  
  /**
   * Returns an instance of the ImageWriter implementation associated with this 
   * service provider. The returned object will initially be in an initial state 
   * as if its reset method had been called.<br>
   * @param extension a plug-in specific extension object, which may be null. This
   *                  implementation does not support any extensions.
   * @return an ImageWriter instance.
   * @throws IOException if the attempt to instantiate the writer fails.
   * @throws IllegalArgumentException if the ImageWriter's constructor throws an 
   *         IllegalArgumentException to indicate that the extension object is 
   *         unsuitable.
   */
  public ImageWriter createWriterInstance(Object extension)
    throws IOException
  {
        return new DcmImageWriter(this);
  }
  
  
  /**
   * Returns the Locale associated with this writer.
   * @return the Locale.
   */
  public String getDescription(Locale locale)
  {
    return "DICOM image writer";
  }
  
}
