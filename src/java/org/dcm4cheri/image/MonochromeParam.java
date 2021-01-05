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
import org.dcm4che.image.ColorModelParam;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class MonochromeParam extends BasicColorModelParam  {
   private static final Logger log = Logger.getLogger(MonochromeParam.class);   
   
   private final int inverse;
   private final float slope, intercept;
   private final float[] center, width;
   private Dataset voilut;
   private final int hashcode;
   private final byte[] pv2dll;
   private final int andmask;
   private final int pvBits;
   
   private final static float[] EMPTY = {};
   private final static float[] toFloats(String[] a) {
      if (a == null) {
          return EMPTY;
      } else {
          float[] f = new float[a.length];
          for (int i = 0 ; i < f.length ; i++)
              f[i] = Float.parseFloat(a[i]);
          return f;
      }
   }
   private final float correctSlope(float f) {
      return f == 0.f || Math.abs(f) > max ? 1.f : f;
   }
   private final static int inBits(int len) {
      for (int i = 8, n = 256; i <= 16; ++i, n <<= 1) {
         if (n == len) {
            return i;
         }
      }
      throw new IllegalArgumentException("pv2dll length: " + len);
   }
   
   /** Creates a new instance of MonochromeParam
    * @param ds is the dicom data set header
    * @param inverse1 is true if the is MONOCHROME1 
    * @param pv2dll is the p-value to digital driving level LUT
    * 	This had better either match the number of pixel values, OR
    *   it needs to be sized as a power of 2.  It does NOT need to 
    *   otherwise match the number of pixel values (that is, a 256 element
    *   table works just fine for 12 bit data)
    */
   public MonochromeParam(Dataset ds, boolean inverse1, byte[] pv2dll) {
      super(ds);
      this.inverse = inverse1 ? -1 : 0;
      this.slope = correctSlope(Float.parseFloat(ds.getString(Tags.RescaleSlope, "1.0")));
      this.intercept = Float.parseFloat(ds.getString(Tags.RescaleIntercept, "0.0"));
      this.center = toFloats(ds.getStrings(Tags.WindowCenter));
      this.width = toFloats(ds.getStrings(Tags.WindowWidth));
      for (int i = 0; i < width.length; ++i) {
         if (width[i] <= 0.f) {
            width[i] = (max - min) / slope;
         }
      }
      this.voilut = center.length == 0 ? ds.getItem(Tags.VOILUTSeq) : null;
      this.pv2dll = pv2dll;
      this.pvBits = inBits(pv2dll.length);
      // Exclude all high-bit data (overlays typically)
      this.andmask = (1<<pvBits)-1;
      this.hashcode = hashcode(dataType, inverse, min, max,
         slope, intercept, center, width, pv2dll);      
   }
   
   private static int hashcode(int dataType, int inverse, int min, int max,
      float slope, float intercept, float[] center, float[] width,
      byte[] pv2dll)
   {
      StringBuffer sb = new StringBuffer();
      sb.append(dataType).append(inverse).append(min).append(max)
              .append(slope).append(intercept).append(pv2dll);
      if (Math.min(center.length, width.length) != 0) {
         sb.append(center[0]).append(width[0]);
      }
      return sb.toString().hashCode();
   }
   
   private MonochromeParam(MonochromeParam other, float center1, float width1,
      boolean inverse1)
   {
      super(other);
      this.inverse = inverse1 ? -1 : 0;
      this.slope = other.slope;
      this.intercept = other.intercept;
      this.center = new float[] { center1 };
      this.width = new float[] { width1 };
      this.pv2dll = other.pv2dll;
      this.pvBits = other.pvBits;
      this.andmask = other.andmask;
      this.hashcode = hashcode(dataType, inverse, min, max,
      slope, intercept, center, width, pv2dll);
   }
   
   public boolean isMonochrome() {
   		return true;
   }
   
   public ColorModelParam update(float center, float width, boolean inverse) {
      if (width < 0) {
         throw new IllegalArgumentException("width: " + width);
      }
      return new MonochromeParam(this, center, width, inverse);
   }
   
   public int hashCode() {
      return hashcode;
   }
   
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof MonochromeParam)) {
         return false;
      }
      MonochromeParam other = (MonochromeParam)o;
      if (this.getNumberOfWindows() == 0) {
         if (other.getNumberOfWindows() != 0) {
            return false;
         }
      } else {
         if (other.getNumberOfWindows() == 0
         || this.center[0] != other.center[0]
         || this.width[0] != other.width[0]) {
            return false;
         }
      }
      return this.inverse == other.inverse
         && this.intercept == other.intercept
         && this.slope == other.slope
         && this.max == other.max
         && this.min == other.min
         && this.pv2dll == other.pv2dll;
   }
   
   public final float getRescaleSlope() {
      return slope;
   }
   
   public final float getRescaleIntercept() {
      return intercept;
   }
   
   public final float getWindowCenter(int index) {
      return center[index];
   }
   
   public final float getWindowWidth(int index) {
      return width[index];
   }
   
   public final int getNumberOfWindows() {
      return Math.min(center.length, width.length);
   }
   
   public final Dataset getVOILUT() {
       return voilut;
   }
    
   public final boolean isCacheable() {
      return voilut == null;
   }
   
   public final boolean isInverse() {
      return inverse != 0;
   }
   
   public final float toMeasureValue(int pxValue) {
      return toSampleValue(pxValue)*slope + intercept;
   }
   
   public final int toPixelValue(float measureValue) {
      return (int)((measureValue - intercept) / slope);
   }
   
   private static int toARGB(byte grey) {
      int v = grey & 0xff;
      return 0xff000000 // alpha
         | (v << 16)
         | (v << 8)
         | (v);
   }
   
   public ColorModel newColorModel() {
      // This won't work if there is an actual LUT for the modality LUT
      int[] cmap = new int[size];
      if (voilut != null) {
          createCMAPfromVOILUT(cmap);
      } else {
          float centerV, widthV;
          if (getNumberOfWindows() == 0) {
        	  centerV = slope*(max + min)/2f+intercept;
        	  widthV = (max-min) * slope;
          } else {
        	  centerV = center[0];
        	  widthV = width[0];
          }
          log.debug("window level "+centerV+","+widthV + " intercept, slope="+intercept+","+slope + " isInverse "+isInverse());
          createCMAP(cmap, (centerV - intercept)/slope,widthV/slope);
      }
      return new IndexColorModel(bits, size, cmap, 0, false, -1, dataType);
   }
   
/** Create a colour map to digital driving levels
    * 
    * @param cmap is the map to fill
    * @param c is the center
    * @param w is the width
    */
   private void createCMAP(int[] cmap, float c, float wFlt) {
	  // w is ok as an int here, as the width should be integral
	  int w = (int) wFlt;
      int u = (int) (c - (wFlt/2));
      int o = u + w;
      int cmin = toARGB(pv2dll[0]);
      int cmax = toARGB(pv2dll[pv2dll.length-1]);
      int useInverse = this.inverse;
      // It seems that MONOCHROME1 images that are signed data don't need to be inverted??? See mlut_17.dcm from the IHE Image Consistency test.  I don't see anything immediately wrong with the code, but that does appear to be what is happening.
      if( min<0 ) useInverse = 0;
      if (u > 0) {
         Arrays.fill(cmap, 0, Math.min(u,max),
            useInverse == 0 ? cmin : cmax);
      }
      if (o < max) {
         Arrays.fill(cmap, Math.max(0,o), max,
            useInverse == 0 ? cmax : cmin);
      }
      for (int i = Math.max(0,u), n = Math.min(o,max); i < n; ++i) {
         cmap[i] = toARGB(pv2dll[(((i-u)<<pvBits) / w ^ useInverse) & andmask]);
         //if( (i % 120) == 0 ) log.info("cmap["+i+"]="+(cmap[i] & 0xFF) );
      }
      if (min == 0) {
         return; // all done for unsigned px val
      }
      if (u > min) {
         Arrays.fill(cmap, size>>1, Math.min(u+size, size),
            useInverse == 0 ? cmin : cmax);
      }
      if (o < 0) {
         Arrays.fill(cmap, Math.max(o+size,size>>1), size,
            useInverse == 0 ? cmax : cmin);
      }
      for (int i = Math.max(min,u), n = Math.min(o,0); i < n; ++i) {
         cmap[i+size] = toARGB(pv2dll[(((i-u)<<pvBits) / w ^ useInverse) & andmask]);
         //if( i % 120 == 0 ) log.info("cmap["+(i+size)+"]="+(cmap[i+size] & 0xFF) );
      }
   }

   private void createCMAPfromVOILUT(int[] cmap) {
       int[] lutDescriptor = voilut.getInts(Tags.LUTDescriptor);
       byte[] lutData = voilut.getByteBuffer(Tags.LUTData).array();
       int lutLength = lutDescriptor[0] != 0 ? lutDescriptor[0] : 0x10000;
       int lutOffset = lutDescriptor[1];
       // adjust VR=US to VR=SS if signed or negative intercept
       if (lutOffset > 0 && (lutOffset & 0x8000) != 0 
               && (min < 0 || intercept < 0)) {
           lutOffset |= 0xFFFF0000;
       }
       int lutBits = lutDescriptor[2];
       if (lutData.length == lutLength) {
           if (lutBits != 8) {
               throw new IllegalArgumentException(
                       "VOI LUT Bit Depth in Descriptor:" + lutBits
                       + " does not match 8 bits alloacted of VOI LUT Data");
           }
           if (isInverse()) {
               createInverseCMAPfrom8bitVOILUT(cmap, lutData, lutOffset);
           } else {
               createCMAPfrom8bitVOILUT(cmap, lutData, lutOffset);
           }
       } else if (lutData.length == (lutLength<<1)) {
           if (bits < lutBits) {
               if (!containsBitsInHighByte(lutData, (-1<<(bits-8)) & 0xff)) {
                   log.info("Detect Agfa ADC VOI LUT bug "
                           + "=> assume VOI LUT Bit Depth = " + bits);
                   lutBits = bits;
               }
           }
           if (isInverse()) {
               createInverseCMAPfrom16bitVOILUT(cmap, lutData, lutOffset,
                       lutBits);
           } else {
               createCMAPfrom16bitVOILUT(cmap, lutData, lutOffset, lutBits);
           }
       } else {
           throw new IllegalArgumentException(
                   "VOI LUT Data Length in Descriptor:" + lutDescriptor[0]
                   + " does not match length of VOI LUT Data:"
                   + lutData.length);
       }
   }

   private boolean containsBitsInHighByte(byte[] lutData, int bits) {
       for (int i = lutData.length - 1; i > 0; i--,i--) {
           if ((lutData[i] & bits) != 0) {
               return true;
           }
       }
       return false;
   }

   private void createCMAPfrom8bitVOILUT(int[] cmap, byte[] lutData,
           int offset) {
       int u = (int) ((offset - intercept) / slope);
       int o = u + (int) (lutData.length / slope);
       int lshift = pvBits - 8;
       int cmin = toARGB(pv2dll[(lutData[0] & 0xff)<<lshift]);
       int cmax = toARGB(pv2dll[(lutData[lutData.length-1] & 0xff)<<lshift]);
       if (u > 0) {
           Arrays.fill(cmap, 0, Math.min(u,max), cmin);
       }
       if (o < max) {
           Arrays.fill(cmap, Math.max(0,o), max, cmax);
       }
       for (int i = Math.max(0,u), n = Math.min(o,max), j = i-u; i < n;) {
           cmap[i++] = toARGB(pv2dll[(lutData[j++] & 0xff)<<lshift]);
       }
       if (min == 0) {
           return; // all done for unsigned px val
       }
       if (u > min) {
           Arrays.fill(cmap, size>>1, Math.min(u+size, size), cmin);
       }
       if (o < 0) {
           Arrays.fill(cmap, Math.max(o+size,size>>1), size, cmax);
       }
       for (int i = Math.max(min,u), n = Math.min(o,0), j = i-u; i < n;) {
           cmap[size + i++] = toARGB(pv2dll[(lutData[j++] & 0xff)<<lshift]);
       }
   }

   private void createInverseCMAPfrom8bitVOILUT(int[] cmap, byte[] lutData,
           int offset) {
       int u = (int) ((offset - intercept) / slope);
       int o = u + (int) (lutData.length / slope);
       int lshift = pvBits - 8;
       int cmin = toARGB(pv2dll[pv2dll.length - 1
                              - ((lutData[0] & 0xff)<<lshift)]);
       int cmax = toARGB(pv2dll[pv2dll.length - 1
                              - ((lutData[lutData.length-1] & 0xff)<<lshift)]);
       if (u > 0) {
           Arrays.fill(cmap, 0, Math.min(u,max), cmin);
       }
       if (o < max) {
           Arrays.fill(cmap, Math.max(0,o), max, cmax);
       }
       for (int i = Math.max(0,u), n = Math.min(o,max), j = i-u; i < n;) {
           cmap[i++] = toARGB(pv2dll[pv2dll.length - 1
                                - ((lutData[j++] & 0xff)<<lshift)]);
       }
       if (min == 0) {
           return; // all done for unsigned px val
       }
       if (u > min) {
           Arrays.fill(cmap, size>>1, Math.min(u+size, size), cmin);
       }
       if (o < 0) {
           Arrays.fill(cmap, Math.max(o+size,size>>1), size, cmax);
       }
       for (int i = Math.max(min,u), n = Math.min(o,0), j = i-u; i < n;) {
           cmap[size + i++] = toARGB(pv2dll[pv2dll.length - 1
                                          - ((lutData[j++] & 0xff)<<lshift)]);
       }
   }

   private void createCMAPfrom16bitVOILUT(int[] cmap, byte[] lutData,
           int offset, int lutBits) {
       int u = (int) ((offset - intercept) / slope);
       int o = u + (int) ((lutData.length>>1) / slope);
       int rshift = lutBits - pvBits;
       int lshift = -rshift;
       if (rshift < 0) {
           rshift = 0;
       } else {
           lshift = 0;
       }
       int cmin = toARGB(pv2dll[(((lutData[0] & 0xff)
               |(lutData[1] & 0xff)<<8)<<lshift)>>rshift]);
       int cmax = toARGB(pv2dll[(((lutData[lutData.length-2] & 0xff)
               |(lutData[lutData.length-1] & 0xff)<<8)<<lshift)>>rshift]);
       if (u > 0) {
           Arrays.fill(cmap, 0, Math.min(u,max), cmin);
       }
       if (o < max) {
           Arrays.fill(cmap, Math.max(0,o), max, cmax);
       }
       for (int i = Math.max(0,u), n = Math.min(o,max), j = (i-u)<<1; i < n;) {
           cmap[i++] = toARGB(pv2dll[(((lutData[j++] & 0xff)
                   |(lutData[j++] & 0xff)<<8)<<lshift)>>rshift]);
       }
       if (min == 0) {
           return; // all done for unsigned px val
       }
       if (u > min) {
           Arrays.fill(cmap, size>>1, Math.min(u+size, size), cmin);
       }
       if (o < 0) {
           Arrays.fill(cmap, Math.max(o+size,size>>1), size, cmax);
       }
       for (int i = Math.max(min,u), n = Math.min(o,0), j = (i-u)<<1; i < n;) {
           cmap[size + i++] = toARGB(pv2dll[(((lutData[j++] & 0xff)
                   |(lutData[j++] & 0xff)<<8)<<lshift)>>rshift]);
       }
   }

   private void createInverseCMAPfrom16bitVOILUT(int[] cmap, byte[] lutData,
           int offset, int lutBits) {
       int u = (int) ((offset - intercept) / slope);
       int o = u + (int) ((lutData.length>>1) / slope);
       int rshift = lutBits - pvBits;
       int lshift = -rshift;
       if (rshift < 0) {
           rshift = 0;
       } else {
           lshift = 0;
       }
       int cmin = toARGB(pv2dll[pv2dll.length - 1 - ((((lutData[0] & 0xff)
               |(lutData[1] & 0xff)<<8)<<lshift)>>rshift)]);
       int cmax = toARGB(pv2dll[pv2dll.length - 1
             - ((((lutData[lutData.length-2] & 0xff)
                 |(lutData[lutData.length-1] & 0xff)<<8)<<lshift)>>rshift)]);
       if (u > 0) {
           Arrays.fill(cmap, 0, Math.min(u,max), cmin);
       }
       if (o < max) {
           Arrays.fill(cmap, Math.max(0,o), max, cmax);
       }
       for (int i = Math.max(0,u), n = Math.min(o,max), j = (i-u)<<1; i < n;) {
           cmap[i++] = toARGB(pv2dll[pv2dll.length - 1
                 - ((((lutData[j++] & 0xff)
                     |(lutData[j++] & 0xff)<<8)<<lshift)>>rshift)]);
       }
       if (min == 0) {
           return; // all done for unsigned px val
       }
       if (u > min) {
           Arrays.fill(cmap, size>>1, Math.min(u+size, size), cmin);
       }
       if (o < 0) {
           Arrays.fill(cmap, Math.max(o+size,size>>1), size, cmax);
       }
       for (int i = Math.max(min,u), n = Math.min(o,0), j = (i-u)<<1; i < n;) {
           cmap[size + i++] = toARGB(pv2dll[pv2dll.length - 1
                 - ((((lutData[j++] & 0xff)
                     |(lutData[j++] & 0xff)<<8)<<lshift)>>rshift)]);
       }
   }
}
