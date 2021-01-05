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

package org.dcm4cheri.data;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.ConfigurationError;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DatasetSerializer;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.image.ColorModelFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.LocatorImpl;

/**
 *  Implementation of <code>Dataset</code> container objects.
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author     <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since      March 2002
 * @version    $Revision: 15346 $ $Date: 2011-04-25 20:51:15 +0200 (Mo, 25 Apr 2011) $
 * @see        "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 */
abstract class BaseDatasetImpl extends DcmObjectImpl implements Dataset {

   private FileMetaInfo fmi = null;

   private int[] grTags = new int[8];

   private int[] grLens = new int[8];

   private int grCount = 0;

   /**  Description of the Field */
   protected int totLen = 0;

   private static SAXTransformerFactory tfFactory;

   private static Templates templates;

   private static TagDictionary tagDictionary;

   private static SAXTransformerFactory getTransformerFactory() {
       if (tfFactory == null) {
           tfFactory = (SAXTransformerFactory) TransformerFactory
               .newInstance();
       }
       return tfFactory;
   }

   private static Templates getTemplates() {
       if (templates == null) {
           InputStream in = BaseDatasetImpl.class
               .getResourceAsStream("dump2.xsl");
           try {
               templates = getTransformerFactory()
               .newTemplates(new StreamSource(in));
           } catch (Exception e) {
               throw new ConfigurationError(
                   "Failed to load/compile dump2.xsl", e);
           }
       }
       return templates;
   }

   private static TagDictionary getTagDictionary() {
       if (tagDictionary == null) {
           DictionaryFactory df = DictionaryFactory.getInstance();
           tagDictionary = df.getDefaultTagDictionary();
       }
       return tagDictionary;
   }

   /**
    *  Sets the fileMetaInfo attribute of the BaseDatasetImpl object
    *
    * @param  fmi  The new fileMetaInfo value
    * @return      Description of the Return Value
    */
   public final Dataset setFileMetaInfo(FileMetaInfo fmi) {
       this.fmi = fmi;
       return this;
   }

   /**
    *  Gets the fileMetaInfo attribute of the BaseDatasetImpl object
    *
    * @return    The fileMetaInfo value
    */
   public FileMetaInfo getFileMetaInfo() {
       return fmi;
   }

   /**
    *  Description of the Method
    *
    * @return    Description of the Return Value
    */
   public String toString() {
       return "Dataset[size=" + size() + "]";
   }

   private int[] ensureCapacity(int[] old, int n) {
       if (n <= old.length) { return old; }
       int[] retval = new int[old.length << 1];
       System.arraycopy(old, 0, retval, 0, old.length);
       return retval;
   }

   /**
    *  Description of the Method
    *
    * @param  param  Description of the Parameter
    * @return        Description of the Return Value
    */
   public int calcLength(DcmEncodeParam param) {
       totLen = 0;
       grCount = 0;

       int curGrTag;

       int prevGrTag = -1;
       synchronized (list) {
           for (Iterator iter = iterator(); iter.hasNext();) {
               DcmElement el = (DcmElement) iter.next();
               curGrTag = el.tag() & 0xffff0000;
               if (curGrTag != prevGrTag) {
                   grCount++;
                   grTags = ensureCapacity(grTags, grCount + 1);
                   grLens = ensureCapacity(grLens, grCount + 1);
                   grTags[grCount - 1] = prevGrTag = curGrTag;
                   grLens[grCount - 1] = 0;
               }
               grLens[grCount - 1] += (param.explicitVR && !VRs
                   .isLengthField16Bit(el.vr())) ? 12 : 8;
               if (el instanceof ValueElement) {
                   grLens[grCount - 1] += el.length();
               } else if (el instanceof FragmentElement) {
                   grLens[grCount - 1] += ((FragmentElement) el).calcLength();
               } else {
                   grLens[grCount - 1] += ((SQElement) el).calcLength(param);
               }
           }
       }
       grTags[grCount] = -1;
       if (!param.skipGroupLen) {
           totLen += grCount * 12;
       }
       for (int i = 0; i < grCount; ++i) {
           totLen += grLens[i];
       }
       return totLen;
   }

   /**
    *  Description of the Method
    *
    * @return    Description of the Return Value
    */
   public int length() {
       return totLen;
   }

   /**  Description of the Method */
   public void clear() {
       super.clear();
       totLen = 0;
   }

   /**
    *  Description of the Method
    *
    * @param  handler          Description of the Parameter
    * @param  param            Description of the Parameter
    * @exception  IOException  Description of the Exception
    */
   public void writeDataset(DcmHandler handler, DcmEncodeParam param)
   throws IOException {
       synchronized (list) {
           if (!(param.skipGroupLen && param.undefItemLen && param.undefSeqLen)) {
               calcLength(param);
           }
           handler.startDataset();
           handler.setDcmDecodeParam(param);
           doWrite(handler, param);
           handler.endDataset();
       }
   }

   private void doWrite(DcmHandler handler, DcmEncodeParam param)
   throws IOException {
       int grIndex = 0;
       for (Iterator iter = iterator(); iter.hasNext();) {
           DcmElement el = (DcmElement) iter.next();
           if (!param.skipGroupLen
               && grTags[grIndex] == (el.tag() & 0xffff0000)) {
               byte[] b4 = new byte[4];
               ByteBuffer.wrap(b4).order(param.byteOrder)
               .putInt(grLens[grIndex]);
               handler.startElement(grTags[grIndex], VRs.UL, el
                   .getStreamPosition());
               handler.value(b4, 0, 4);
               handler.endElement();
               ++grIndex;
           }
           if (el instanceof SQElement) {
               int len = param.undefSeqLen ? -1 : el.length();
               handler.startElement(el.tag(), VRs.SQ, el.getStreamPosition());
               handler.startSequence(len);
               for (int j = 0, m = el.countItems(); j < m;) {
                   BaseDatasetImpl ds = (BaseDatasetImpl) el.getItem(j);
                   int itemlen = param.undefItemLen ? -1 : ds.length();
                   handler.startItem(++j, ds.getItemOffset(), itemlen);
                   ds.doWrite(handler, param);
                   handler.endItem(itemlen);
               }
               handler.endSequence(len);
               handler.endElement();
           } else if (el instanceof FragmentElement) {
               long offset = el.getStreamPosition();
               handler.startElement(el.tag(), el.vr(), offset);
               handler.startSequence(-1);
               if (offset != -1L) {
                   offset += 12;
               }
               for (int j = 0, m = el.countItems(); j < m;) {
                   ByteBuffer bb = el.getDataFragment(j, param.byteOrder);
                   handler.fragment(++j,
                       offset,
                       bb.array(),
                       bb.arrayOffset(),
                       bb.limit());
                   if (offset != -1L) {
                       offset += (bb.limit() + 9) & (~1);
                   }
               }
               handler.endSequence(-1);
               handler.endElement();
           } else {
               int len = el.length();
               handler.startElement(el.tag(), el.vr(), el.getStreamPosition());
               ByteBuffer bb = el.getByteBuffer(param.byteOrder);
               handler.value(bb.array(), bb.arrayOffset(), bb.limit());
               handler.endElement();
           }
       }
   }

   /**
    *  Description of the Method
    *
    * @param  out              Description of the Parameter
    * @param  param            Description of the Parameter
    * @exception  IOException  Description of the Exception
    */
   public void writeDataset(OutputStream out, DcmEncodeParam param)
   throws IOException {
       if (param == null) {
           param = DcmDecodeParam.IVR_LE;
       }
       DeflaterOutputStream deflater = null;
       writeDataset(new DcmStreamHandlerImpl(param.deflated
           ? deflater = new DeflaterOutputStream(out, 
                   new Deflater(Deflater.DEFAULT_COMPRESSION, true)) : out),
                   param);
       if (deflater != null)
           deflater.finish();
   }

   /**
    *  Description of the Method
    *
    * @param  out              Description of the Parameter
    * @param  param            Description of the Parameter
    * @exception  IOException  Description of the Exception
    */
   public void writeFile(OutputStream out, DcmEncodeParam param)
   throws IOException {
       FileMetaInfo fmi = getFileMetaInfo();
       if (fmi != null) {
           param = checkCompatibility(fmi, param);
           fmi.write(out);
       }
       writeDataset(out, param);
   }

   /**
    *  Description of the Method
    *
    * @param  out              Description of the Parameter
    * @param  param            Description of the Parameter
    * @exception  IOException  Description of the Exception
    */
   public void writeDataset(ImageOutputStream out, DcmEncodeParam param)
   throws IOException {
       if (param == null) {
           param = DcmDecodeParam.IVR_LE;
       }
       DeflaterOutputStream deflater = null;
       writeDataset(param.deflated ? new DcmStreamHandlerImpl(
           deflater = new DeflaterOutputStream(new OutputStreamAdapter(out)))
           : new DcmStreamHandlerImpl(out), param);
       if (deflater != null)
           deflater.finish();
   }

   private DcmEncodeParam checkCompatibility(FileMetaInfo fmi,
       DcmEncodeParam param) {
       DcmEncodeParam fmiParam = DcmDecodeParam.valueOf(fmi
           .getTransferSyntaxUID());
       if (param == null) { return fmiParam; }
       if (param.byteOrder == fmiParam.byteOrder
           && param.explicitVR == fmiParam.explicitVR
           && param.encapsulated == fmiParam.encapsulated
           && param.deflated == fmiParam.deflated) { return param; }
       throw new IllegalArgumentException("param: " + param
           + " does not match with " + fmi);
   }

   /**
    *  Description of the Method
    *
    * @param  out              Description of the Parameter
    * @param  param            Description of the Parameter
    * @exception  IOException  Description of the Exception
    */
   public void writeFile(ImageOutputStream out, DcmEncodeParam param)
   throws IOException {
       FileMetaInfo fmi = getFileMetaInfo();
       if (fmi != null) {
           param = checkCompatibility(fmi, param);
           fmi.write(out);
       }
       writeDataset(out, param);
   }

   public void writeFile(File f, DcmEncodeParam param) throws IOException {
       OutputStream in = new BufferedOutputStream(new FileOutputStream(f));
       try {
           writeFile(in, param);
       } finally {
           try {
               in.close();
           } catch (IOException ignore) {
           }
       }
   }

   /**
    *  Description of the Method
    *
    * @param  ch               Description of the Parameter
    * @param  dict             Description of the Parameter
    * @exception  IOException  Description of the Exception
    */
   public void writeDataset(ContentHandler ch, TagDictionary dict)
   throws IOException {
       writeDataset(new DcmHandlerAdapter(ch, dict), DcmDecodeParam.EVR_LE);
   }

   public void writeDataset2(ContentHandler ch, TagDictionary dict,
       int[] excludeTags, int excludeValueLengthLimit, File basedir)
       throws IOException {
       writeDataset(new DcmHandlerAdapter2(ch, dict, excludeTags,
           excludeValueLengthLimit, basedir),
           DcmDecodeParam.EVR_LE);
   }

   public void dumpDataset(OutputStream out, Map param) throws IOException {
       dumpDataset(new StreamResult(out), param, 128);
   }

   public void dumpDataset(OutputStream out, Map param, int excludeValueLengthLimit)
   throws IOException {
       dumpDataset(new StreamResult(out), param, excludeValueLengthLimit);
   }

   public void dumpDataset(Writer w, Map param) throws IOException {
       dumpDataset(new StreamResult(w), param, 128);
   }

   public void dumpDataset(Writer w, Map param, int excludeValueLengthLimit)
   throws IOException {
       dumpDataset(new StreamResult(w), param, excludeValueLengthLimit);
   }

   private void dumpDataset(Result result, Map param, int excludeValueLengthLimit)
   throws IOException {
       TransformerHandler th;
       try {
           th = getTransformerFactory().newTransformerHandler(getTemplates());
           if (param != null) {
               Transformer t = th.getTransformer();
               for (Iterator it = param.entrySet().iterator(); it.hasNext();) {
                   Map.Entry e = (Map.Entry) it.next();
                   t.setParameter((String) e.getKey(), e.getValue());
               }
           }
       } catch (Exception e) {
           throw new ConfigurationError("Failed to initialize XSLT", e);
       }
       th.setDocumentLocator(new LocatorImpl());
       th.setResult(result);
       writeDataset2(th, getTagDictionary(), null, excludeValueLengthLimit, null);
   }

   /**
    *  Description of the Method
    *
    * @param  ch               Description of the Parameter
    * @param  dict             Description of the Parameter
    * @exception  IOException  Description of the Exception
    */
   public void writeFile(ContentHandler ch, TagDictionary dict)
   throws IOException {
       DcmHandlerAdapter xml = new DcmHandlerAdapter(ch, dict);
       xml.startDcmFile();
       FileMetaInfo fmi = getFileMetaInfo();
       if (fmi != null) {
           fmi.write(xml);
       }
       writeDataset(xml, DcmDecodeParam.EVR_LE);
       xml.endDcmFile();
   }

   public void writeFile2(ContentHandler ch, TagDictionary dict,
       int[] excludeTags, int excludeValueLengthLimit, File basedir)
       throws IOException {
       DcmHandlerAdapter2 xml = new DcmHandlerAdapter2(ch, dict, excludeTags,
           excludeValueLengthLimit, basedir);
       xml.startDcmFile();
       FileMetaInfo fmi = getFileMetaInfo();
       if (fmi != null) {
           fmi.write(xml);
       }
       writeDataset(xml, DcmDecodeParam.EVR_LE);
       xml.endDcmFile();
   }

   /**
    *  Description of the Method
    *
    * @param  fromTag  Description of the Parameter
    * @param  toTag    Description of the Parameter
    * @return          Description of the Return Value
    */
   public Dataset subSet(int fromTag, int toTag) {
       return new FilterDataset.Segment(this, fromTag, toTag);
   }

   /**
    *  Description of the Method
    *
    * @param  filter  Description of the Parameter
    * @return         Description of the Return Value
    */
   public Dataset subSet(Dataset filter) {
       return new FilterDataset.Selection(this, filter);
   }

   public Dataset subSet(int[] tags, int[] vrs, boolean exclude, boolean excludePrivate) {
       return new FilterDataset.TagFilter(this, tags, vrs, exclude, excludePrivate);
   }

   public Dataset subSet(int[] tags, int[] vrs) {
       return new FilterDataset.TagFilter(this, tags, vrs, false, false);
   }

   public Dataset exclude(int[] tags, int[] vrs) {
       return new FilterDataset.TagFilter(this, tags, vrs, true, false);
   }

   public Dataset subSet(int[] tags, boolean exclude, boolean excludePrivate) {
       return new FilterDataset.TagFilter(this, tags, null, exclude, excludePrivate);
   }

   public Dataset subSet(int[] tags) {
       return new FilterDataset.TagFilter(this, tags, null, false, false);
   }

   public Dataset exclude(int[] tags) {
       return new FilterDataset.TagFilter(this, tags, null, true, false);
   }
   
   public Dataset excludePrivate() {
       return new FilterDataset.ExcludePrivate(this);
   }

   /**
    *  Description of the Method
    *
    * @param  keys          Description of the Parameter
    * @param  ignorePNCase  Description of the Parameter
    * @return               Description of the Return Value
    */
   public boolean match(Dataset keys, boolean ignorePNCase, boolean ignoreEmpty) {
       if (keys == null) { return true; }
       SpecificCharacterSet keyCS = keys.getSpecificCharacterSet();
       for (Iterator iter = keys.iterator(); iter.hasNext();) {
           if (!match((DcmElementImpl) iter.next(),
               ignorePNCase,
               ignoreEmpty,
               keyCS)) { return false; }
       }
       return true;
   }

   private boolean match(DcmElementImpl key, boolean ignorePNCase,
       boolean ignoreEmpty, SpecificCharacterSet keyCS) {
       final int tag = key.tag();
       // ignore Character Set Attribute in key
       if (tag == Tags.SpecificCharacterSet) return true;
       DcmElementImpl e = (DcmElementImpl) get(tag);
       if (e == null) { return ignoreEmpty || key.isEmpty(); }
       return e.match(key, ignorePNCase, ignoreEmpty, keyCS, getSpecificCharacterSet());
   }

   /**
    *  Description of the Method
    *
    * @return                                    Description of the Return Value
    * @exception  java.io.ObjectStreamException  Description of the Exception
    */
   protected Object writeReplace() throws java.io.ObjectStreamException {
       return new DatasetSerializer(this);
   }

   //used by toBufferedImage()
   private final static ColorSpace sRGB = ColorSpace
       .getInstance(ColorSpace.CS_sRGB);

   //used by toBufferedImage()
   private final static ImageTypeSpecifier RGB_PLANE = ImageTypeSpecifier
       .createBanded(sRGB,
       new int[] { 0, 1, 2},
       new int[] { 0, 0, 0},
       DataBuffer.TYPE_BYTE,
       false,
       false);

   //used by toBufferedImage()
   private final static ImageTypeSpecifier RGB_PIXEL = ImageTypeSpecifier
       .createInterleaved(sRGB,
       new int[] { 0, 1, 2},
       DataBuffer.TYPE_BYTE,
       false,
       false);

   /**
    *  Description of the Method
    *
    * @return    Description of the Return Value
    */
   public BufferedImage toBufferedImage() {
       return toBufferedImage(1);
   }

   /**
    *  Description of the Method
    *
    * @param  frame  Description of the Parameter
    * @return        Description of the Return Value
    */
   public BufferedImage toBufferedImage(int frame) {
       int width = getInt(Tags.Columns, -1);
       int height = getInt(Tags.Rows, -1);

       if (width == -1 || height == -1) { throw new IllegalStateException(
           "Illegal width/height: width = " + width + ", height = "
           + height); }

       int bitsAllocd = getInt(Tags.BitsAllocated, -1);
       int bitsStored = getInt(Tags.BitsStored, -1);
       int highBit = getInt(Tags.HighBit, -1);
       int pixelRep = getInt(Tags.PixelRepresentation, -1);
       String pmi = getString(Tags.PhotometricInterpretation, null);
       int spp = getInt(Tags.SamplesPerPixel, -1);
       int planarConf = getInt(Tags.PlanarConfiguration, -1);
       ByteBuffer pixelData = getByteBuffer(Tags.PixelData);

       //some error checking
       if (bitsAllocd == -1 || bitsStored == -1 || highBit == -1
           || pixelRep == -1 || pmi == null || spp == -1
           || (planarConf == -1 && spp > 1) || pixelData == null) { throw new IllegalStateException(
               "Missing required Image Pixel Module attributes"); }

       if (!((pmi.equals("RGB") && spp == 3)
       || (pmi.equals("PALETTE COLOR") && spp == 1)
       || (pmi.equals("MONOCHROME1") && spp == 1) || (pmi
           .equals("MONOCHROME2") && spp == 1))) { throw new IllegalStateException(
               "Invalid Photometric Interpretation (" + pmi
               + ") and Samples per Pixel (" + spp + ") configuration"); }

       int dataBufType;
       boolean signed = (pixelRep == 1);
       if (planarConf == -1) {
           planarConf = 1;
       }

       if (bitsAllocd == 8) {
           dataBufType = DataBuffer.TYPE_BYTE;
       } else if (bitsAllocd == 16) {
           dataBufType = DataBuffer.TYPE_USHORT;
       } else {
           throw new IllegalStateException("Bits Allocated must be 8 or 16, "
               + bitsAllocd + " is not supported");
       }

       if (highBit != bitsStored - 1) { throw new IllegalStateException(
           "High bit must be Bits Stored - 1 " + highBit
           + " is not supported"); }

       BufferedImage bi = null;

       //create BufferedImage
       if (pmi.equals("RGB")) {
           switch (planarConf) {
               case 0:
                   bi = RGB_PIXEL.createBufferedImage(width, height);
                   break;
               case 1:
                   bi = RGB_PLANE.createBufferedImage(width, height);
                   break;
               default:
                   throw new IllegalStateException(
                       "Invalid Planar Configuration for RGB");
           }
       } else if (pmi.equals("MONOCHROME1") || pmi.equals("MONOCHROME2")
       || pmi.equals("PALETTE COLOR")) {
           ColorModelFactory cmFactory = ColorModelFactory.getInstance();
           bi = new ImageTypeSpecifier(cmFactory.getColorModel(cmFactory
               .makeParam(this)), new PixelInterleavedSampleModel(
               dataBufType, 1, 1, 1, 1, new int[] { 0}))
               .createBufferedImage(width, height);
       }

       //read pixeldata into the BufferedImage
       DataBuffer dataBuf = bi.getRaster().getDataBuffer();
       Object dest = null;
       if (planarConf == 0) {//read single bank for PixelInterleavedModel
           switch (dataBufType) {
               case DataBuffer.TYPE_BYTE:
                   dest = ((DataBufferByte) dataBuf).getData();
                   break;
               case DataBuffer.TYPE_USHORT:
                   dest = ((DataBufferUShort) dataBuf).getData();
                   break;
           }
           readPixelData(pixelData,
               dest,
               dataBufType,
               frame,
               bitsAllocd,
               bitsStored,
               highBit,
               signed,
               spp,
               width,
               height);
       } else {
           for (int i = 0; i < spp; i++) {//read each bank of BandedSampleModel seperately
               switch (dataBufType) {
                   case DataBuffer.TYPE_BYTE:
                       dest = ((DataBufferByte) dataBuf).getData(i);
                       break;
                   case DataBuffer.TYPE_USHORT:
                       dest = ((DataBufferUShort) dataBuf).getData(i);
                       break;
               }
               readPixelData(pixelData,
                   dest,
                   dataBufType,
                   frame,
                   bitsAllocd,
                   bitsStored,
                   highBit,
                   signed,
                   1,
                   width,
                   height);
           }
       }

       return bi;
   }

   //only supports Bits Allocated 8 or 16. signed, hb, bs are ignored.
   private void readPixelData(ByteBuffer pixelData, Object dest,
       int destDataType, int frame, int ba, int bs, int hb,
       boolean signed, int spp, int width, int height) {
       final int size = width * height * spp;
       final int frameSize = size * (ba / 8);
       int i = 0;

       //seek to frame offset
       if (frame * frameSize > pixelData.limit()) { throw new IllegalArgumentException(
           "Bad frame number: " + frame); }
       frame--;
       pixelData.position(frameSize * frame);
       //fill dest
       switch (destDataType) {
           case DataBuffer.TYPE_BYTE:
               byte[] bufByte = (byte[]) dest;
               while (i < size) {
                   bufByte[i++] = pixelData.get();
               }
               break;
           case DataBuffer.TYPE_USHORT:
               short[] bufUShort = (short[]) dest;
               while (i < size) {
                   bufUShort[i++] = pixelData.getShort();
               }
               break;
       }
   }

   /**
    *  Description of the Method
    *
    * @param  bi  Description of the Parameter
    */
   public void putBufferedImage(BufferedImage bi) {
       putBufferedImage(bi, null, true);
   }

   /**
    *  Description of the Method
    *
    * @param  bi            Description of the Parameter
    * @param  sourceRegion  Description of the Parameter
    */
   public void putBufferedImage(BufferedImage bi, Rectangle sourceRegion) {
       putBufferedImage(bi, sourceRegion, true);
   }

   /**
    *  Description of the Method
    *
    * @param  bi                          Description of the Parameter
    * @param  sourceRegion                Description of the Parameter
    * @param  writeIndexedAsPaletteColor  Description of the Parameter
    */
   public void putBufferedImage(BufferedImage bi, Rectangle sourceRegion,
       boolean writeIndexedAsPaletteColor) {
       //choose proper photometric interpretation
       int dataType = bi.getType();
       boolean writeAsMono = (dataType == BufferedImage.TYPE_BYTE_GRAY || dataType == BufferedImage.TYPE_USHORT_GRAY);

       //place Image Pixel Module and image data
       if (writeAsMono) {
           putBufferedImageAsMonochrome(bi, sourceRegion, true);
       } else {
           boolean writeAsRGB = !(writeIndexedAsPaletteColor && bi
               .getColorModel() instanceof IndexColorModel);

           if (writeAsRGB) {
               putBufferedImageAsRgb(bi, sourceRegion);
           } else {
               putBufferedImageAsPaletteColor(bi, sourceRegion);
           }
       }
   }

   /**
    *  Description of the Method
    *
    * @param  bi            Description of the Parameter
    * @param  sourceRegion  Description of the Parameter
    */
   public void putBufferedImageAsRgb(BufferedImage bi, Rectangle sourceRegion) {
       Rectangle rect;
       Rectangle sourceRect;

       rect = new Rectangle(bi.getWidth(), bi.getHeight());
       if (sourceRegion == null) {
           sourceRect = rect;
       } else {
           sourceRect = rect.intersection(sourceRegion);
       }
       //IllegalArgumentException thrown if sourceRect is empty
       if (sourceRect.isEmpty()) { throw new IllegalArgumentException(
           "Source region is empty." + this); }

       int width = sourceRect.width;
       int height = sourceRect.height;
       boolean signed = false;
       int bitsAllocd = 8;
       int bitsStored = 8;//forcing 8-bits per component!
       int highBit = 7;

       // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
       putUS(Tags.SamplesPerPixel, 3);// Type 1
       putUS(Tags.BitsAllocated, bitsAllocd);// Type 1
       putUS(Tags.BitsStored, bitsStored);// Type 1
       putUS(Tags.HighBit, highBit);// Type 1
       putCS(Tags.PhotometricInterpretation, "RGB");// Type 1
       putUS(Tags.Rows, height);// Type 1
       putUS(Tags.Columns, width);// Type 1
       putUS(Tags.PixelRepresentation, (signed) ? 1 : 0);// Type 1; 0x0=unsigned int, 0x1=2's complement
       putUS(Tags.PlanarConfiguration, 0);// Type 1C, if SamplesPerPixel > 1, should not present otherwise
       putIS(Tags.PixelAspectRatio, new int[] { 1, 1});// Type 1C, if vertical/horizontal != 1

       byte[] rgbOut = new byte[width * height * 3];
       int dataType = bi.getData().getDataBuffer().getDataType();
       ColorModel cm = bi.getColorModel();
       int[] pixels = bi.getRGB(sourceRect.x,
           sourceRect.y,
           width,
           height,
           (int[]) null,
           0,
           width);
       int ind = 0;

       for (int i = 0; i < pixels.length; i++) {
           rgbOut[ind++] = (byte) ((pixels[i] >> 16) & 0xff);
           rgbOut[ind++] = (byte) ((pixels[i] >> 8) & 0xff);
           rgbOut[ind++] = (byte) (pixels[i] & 0xff);
           /*rgbOut[ind++] = (byte)cm.getRed(pixels[i]);
            rgbOut[ind++] = (byte)cm.getGreen(pixels[i]);
            rgbOut[ind++] = (byte)cm.getBlue(pixels[i]);*/
       }

       //set pixeldata
       putOB(Tags.PixelData, ByteBuffer.wrap(rgbOut));// Type 1; OB or OW
   }

   /**
    *  Description of the Method
    *
    * @param  bi                  Description of the Parameter
    * @param  sourceRegion        Description of the Parameter
    * @param  writeAsMonochrome2  Description of the Parameter
    */
   public void putBufferedImageAsMonochrome(BufferedImage bi,
       Rectangle sourceRegion, boolean writeAsMonochrome2) {
       Rectangle rect;
       Rectangle sourceRect;

       rect = new Rectangle(bi.getWidth(), bi.getHeight());
       if (sourceRegion == null) {
           sourceRect = rect;
       } else {
           sourceRect = rect.intersection(sourceRegion);
       }
       //IllegalArgumentException thrown if sourceRect is empty
       if (sourceRect.isEmpty()) { throw new IllegalArgumentException(
           "Source region is empty." + this); }

       int dataType = bi.getType();
       int width = sourceRect.width;
       int height = sourceRect.height;
       boolean signed = false;
       int bitsAllocated = (dataType == BufferedImage.TYPE_BYTE_GRAY) ? 8 : 16;
       int bitsStored = bitsAllocated;
       int highBit = bitsStored - 1;

       // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
       putUS(Tags.SamplesPerPixel, 1);// Type 1
       //TODO need seperate cases for encoding monochrome1/2 ??
       if (writeAsMonochrome2) {
           putCS(Tags.PhotometricInterpretation, "MONOCHROME2");
       } // Type 1
       else {
           putCS(Tags.PhotometricInterpretation, "MONOCHROME1");
       }// Type 1
       putUS(Tags.Rows, height);// Type 1
       putUS(Tags.Columns, width);// Type 1
       putUS(Tags.BitsAllocated, bitsAllocated);// Type 1
       putUS(Tags.BitsStored, bitsStored);// Type 1
       putUS(Tags.HighBit, highBit);// Type 1
       putUS(Tags.PixelRepresentation, (signed) ? 1 : 0);// Type 1; 0x0=unsigned int, 0x1=2's complement
       putIS(Tags.PixelAspectRatio, new int[] { 1, 1});// Type 1C, if vertical/horizontal != 1

       int max;// Type 1C, if vertical/horizontal != 1

       int min;// Type 1C, if vertical/horizontal != 1

       int value;
       int[] pixels = bi.getRaster().getPixels(sourceRect.x,
           sourceRect.y,
           width,
           height,
           (int[]) null);

       //find min/max
       min = max = pixels[0];
       for (int i = 1; i < pixels.length; i++) {
           value = pixels[i];
           if (value > max) {
               max = value;
           } else if (value < min) {
               min = value;
           }
       }

       //write pixels to byte buffer
       byte[] out;
       ByteBuffer byteBuf;

       // hacklaender, 2006.04.18: Block replaced
       // if (bitsAllocated == 8) {
       //     out = new byte[pixels.length];
       //     for (int i = 1; i < pixels.length; i++) {
       //         out[i] = (byte) (pixels[(i % 2 == 0) ? i + 1 : i - 1] & 0xff);
       //     }
       // } else {//bitsAllocated == 16
       //     out = new byte[pixels.length * 2];
       //     for (int i = 1; i < pixels.length;) {
       //         out[i++] = (byte) ((pixels[i] >> 8) & 0xff);
       //         out[i++] = (byte) (pixels[i] & 0xff);
       //     }
       // }
       // Set Pixel data
       if (bitsAllocated <= 8) {

           //bitsAllocated <= 8

           out = new byte[pixels.length];

           // Pixeldata must be OW if transfer syntax is Implicit VR/Little Endian
           for (int pixelsIdx = 0; pixelsIdx < pixels.length; pixelsIdx++) {
               out[pixelsIdx] = (byte) (pixels[(pixelsIdx % 2 == 0) ? pixelsIdx + 1 : pixelsIdx - 1] & 0xff);
           }
           byteBuf = ByteBuffer.wrap(out);
           putOW(Tags.PixelData, byteBuf);

           // Pixeldata may be OW or OB
           // for (int pixelsIdx = 0; pixelsIdx < pixels.length; pixelsIdx++) {
           //     out[pixelsIdx] = (byte) (pixels[pixelsIdx] & 0xff);
           // }
           // byteBuf = ByteBuffer.wrap(out);
           // putOB(Tags.PixelData, byteBuf);

       } else {

           //bitsAllocated > 8

           out = new byte[pixels.length * 2];
           int outIdx = 0;
           for (int pixelsIdx = 0; pixelsIdx < pixels.length; pixelsIdx++) {
               out[outIdx++] = (byte) ((pixels[pixelsIdx] >> 8) & 0xff);
               out[outIdx++] = (byte) (pixels[pixelsIdx] & 0xff);
           }
           byteBuf = ByteBuffer.wrap(out);
           putOW(Tags.PixelData, byteBuf);
       }

       // hacklaender, 2006.04.18
       // byteBuf = ByteBuffer.wrap(out);
       // set pixeldata
       // putOW(Tags.PixelData, byteBuf);

       //set min/max pixel values
       putSS(Tags.SmallestImagePixelValue, min);// Type 3
       putSS(Tags.LargestImagePixelValue, max);// Type 3

       // Image IE, Modality LUT Module, PS 3.3 - C.11.1, U
       // don't overwrite if the Dataset already contains a Rescale Intercept/Slope
       // hacklaender, 2006.04.18
       // float rs = getFloat(Tags.RescaleSlope, 1).floatValue();
       // float ri = getFloat(Tags.RescaleIntercept, 0).floatValue();
       float rs = getFloat(Tags.RescaleSlope, (float) 1.0);
       float ri = getFloat(Tags.RescaleIntercept, (float) 0.0);
       if (!contains(Tags.RescaleIntercept)) {
           putDS(Tags.RescaleIntercept, ri);// Type 1C; ModalityLUTSequence is not present
           putDS(Tags.RescaleSlope, rs);// Type 1C; ModalityLUTSequence is not present
           putLO(Tags.RescaleType, "PIXELVALUE");// Type 1C; ModalityLUTSequence is not present; arbitrary text
       }

       // Image IE, VOI LUT Module, PS 3.3 - C.11.2, U
       // don't overwrite if the Dataset already contains a Window Center/Width
       if (!contains(Tags.WindowCenter)) {
           String[] wc = { Float.toString((rs * (max + min)) / 2 + ri)};
           putDS(Tags.WindowCenter, wc);// Type 3
           String[] ww = { Float.toString((rs * (max - min)) / 2)};
           putDS(Tags.WindowWidth, ww);// Type 1C; WindowCenter is present
       }
   }

   /**
    *  Description of the Method
    *
    * @param  bi            Description of the Parameter
    * @param  sourceRegion  Description of the Parameter
    */
   public void putBufferedImageAsPaletteColor(BufferedImage bi,
       Rectangle sourceRegion) {
       Rectangle rect;
       Rectangle sourceRect;

       rect = new Rectangle(bi.getWidth(), bi.getHeight());
       if (sourceRegion == null) {
           sourceRect = rect;
       } else {
           sourceRect = rect.intersection(sourceRegion);
       }
       //IllegalArgumentException thrown if sourceRect is empty
       if (sourceRect.isEmpty()) { throw new IllegalArgumentException(
           "Source region is empty." + this); }

       int dataType = bi.getData().getDataBuffer().getDataType();
       ColorModel cm = bi.getColorModel();
       IndexColorModel icm;

       if (!(cm instanceof IndexColorModel)) {
           throw new IllegalArgumentException(
               "BufferedImage's ColorModel must be an IndexColorModel to represent"
               + " as a \"PALETTE COLOR\" DICOM image");
       } else {
           icm = (IndexColorModel) cm;
       }

       //write palette
       final int maxPaletteSize = 65536;
       final int paletteSize = icm.getMapSize();
       final int paletteIndexSize = icm.getPixelSize();

       //System.out.println("icm.getMapSize() = " + paletteSize);
       //System.out.println("icm.getPixelSize() = " + paletteIndexSize);

       //sanity check on palette size
       if (paletteSize > maxPaletteSize) { throw new IllegalArgumentException(
           "BufferedImage contains a palette that is too large to be"
           + " encoded as a DICOM Color LUT (" + paletteSize + ")"); }

       //sanity check on pixel size (the index into palette)
       if (paletteIndexSize == 0 || paletteIndexSize > 16) { throw new UnsupportedOperationException(
           "BufferedImages with a pixel size of "
           + paletteIndexSize
           + " bits are not supported, only 1 to 16 bits are supported"); }

       int width = sourceRect.width;
       int height = sourceRect.height;
       boolean signed = false;
       int bitsAllocd = (paletteIndexSize <= 8) ? 8 : 16;
       int bitsStored = bitsAllocd;
       int highBit = bitsStored - 1;

       // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
       putUS(Tags.SamplesPerPixel, 1);// Type 1
       putCS(Tags.PhotometricInterpretation, "PALETTE COLOR");// Type 1
       putUS(Tags.Rows, height);// Type 1
       putUS(Tags.Columns, width);// Type 1
       putUS(Tags.BitsAllocated, bitsAllocd);// Type 1
       putUS(Tags.BitsStored, bitsStored);// Type 1
       putUS(Tags.HighBit, highBit);// Type 1
       putUS(Tags.PixelRepresentation, (signed) ? 1 : 0);// Type 1; 0x0=unsigned int, 0x1=2's complement
       putIS(Tags.PixelAspectRatio, new int[] { 1, 1});// Type 1C, if vertical/horizontal != 1

       //write palette descriptor
       ByteBuffer pDescriptor;
       byte[] rPalette;
       byte[] gPalette;
       byte[] bPalette;
       ByteBuffer rByteBuffer;
       ByteBuffer gByteBuffer;
       ByteBuffer bByteBuffer;

       pDescriptor = ByteBuffer.allocate(3 * 2);
       pDescriptor.putShort((short) ((paletteSize == maxPaletteSize) ? 0
           : paletteSize));// number of entries
       pDescriptor.putShort((short) 0);// first stored pixel value mapped
       pDescriptor.putShort((short) 8);// number of bits, always 8 for IndexColorModel's internal color component tables

       putXX(Tags.RedPaletteColorLUTDescriptor, VRs.US, pDescriptor);// Type 1C; US/US or SS/US
       putXX(Tags.GreenPaletteColorLUTDescriptor, VRs.US, pDescriptor);// Type 1C; US/US or SS/US
       putXX(Tags.BluePaletteColorLUTDescriptor, VRs.US, pDescriptor);// Type 1C; US/US or SS/US

       rPalette = new byte[paletteSize];
       gPalette = new byte[paletteSize];
       bPalette = new byte[paletteSize];
       icm.getReds(rPalette);
       icm.getGreens(gPalette);
       icm.getBlues(bPalette);

       //default is ByteOrder.BIG_ENDIAN byte ordering
       rByteBuffer = ByteBuffer.allocate(paletteSize);
       gByteBuffer = ByteBuffer.allocate(paletteSize);
       bByteBuffer = ByteBuffer.allocate(paletteSize);

       //grab word chunks and place words in buffer
       for (int idx = 0; idx < paletteSize; idx += 2) {
           rByteBuffer
               .putShort((short) ((rPalette[idx + 1] << 8) + rPalette[idx]));
           gByteBuffer
               .putShort((short) ((gPalette[idx + 1] << 8) + gPalette[idx]));
           bByteBuffer
               .putShort((short) ((bPalette[idx + 1] << 8) + bPalette[idx]));
       }

       putOW(Tags.RedPaletteColorLUTData, rByteBuffer);// Type 1C; US or SS or OW
       putOW(Tags.GreenPaletteColorLUTData, gByteBuffer);// Type 1C; US or SS or OW
       putOW(Tags.BluePaletteColorLUTData, bByteBuffer);// Type 1C; US or SS or OW

       //read pixels as int's, should be one sample per each pixel (palette index)
       int[] pixels = bi.getRaster().getPixels(sourceRect.x,
           sourceRect.y,
           width,
           height,
           (int[]) null);
       byte[] indOut;
       int ind = 0;
       //put pixeldata (must be OW if transfer syntax is Implicit VR/Little Endian)
       if (paletteIndexSize <= 8) {
           indOut = new byte[width * height];
           for (int i = 0; i < pixels.length; i++) {
               indOut[i] = (byte) (pixels[(i % 2 == 0) ? i + 1 : i - 1] & 0xff);
           }
           putOW(Tags.PixelData, ByteBuffer.wrap(indOut));// Type 1; OW
       } else if (paletteIndexSize > 8 && paletteIndexSize <= 16) {
           indOut = new byte[width * height * 2];
           for (int i = 0; i < pixels.length; i++) {
               indOut[ind++] = (byte) ((pixels[i] >> 8) & 0xff);
               indOut[ind++] = (byte) (pixels[i] & 0xff);
           }
           putOW(Tags.PixelData, ByteBuffer.wrap(indOut));// Type 1; OW
       }
   }
}


