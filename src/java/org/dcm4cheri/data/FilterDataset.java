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

package org.dcm4cheri.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.SpecificCharacterSet;

import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.imageio.stream.ImageInputStream;

import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public abstract class FilterDataset extends BaseDatasetImpl implements Dataset {
    
    protected final BaseDatasetImpl backend;
    
    /** Creates a new instance of DatasetView */
    public FilterDataset(Dataset backend) {
        this.backend = (BaseDatasetImpl)backend;
    }
                    
    public abstract boolean filter(int tag, int vr);
 
    public boolean contains(int tag) {
        return get(tag) != null;
    }

    public int vm(int tag) {
        DcmElement el = get(tag);
        return el != null ? el.vm(backend.getSpecificCharacterSet()) : -1;
    }

    public int size() {
        int count = 0;
        DcmElement el;
        for (Iterator iter = backend.iterator(); iter.hasNext();) {
            el = (DcmElement) iter.next();
            if (filter(el.tag(), el.vr())) {
                ++count;
            }
        }
        return count;
    }

    public DcmElement get(int tag) {
        DcmElement el = backend.get(tag);
        return el != null && filter(el.tag(), el.vr()) ? el : null;
    }
    
    public Iterator iterator() {
        final Iterator backendIter = backend.iterator();
        return new Iterator() {
            private DcmElement next = findNext();
            private DcmElement findNext() {
                DcmElement el;
                while (backendIter.hasNext()) {
                    el = (DcmElement)backendIter.next();
                    if (filter(el.tag(), el.vr())) {
                        return el;
                    }
                }
                return null;
            }
            
            public boolean hasNext() {
                return next != null;
            }
            
            public Object next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                DcmElement retval = next;
                next = findNext();
                return retval;
            }
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public SpecificCharacterSet getSpecificCharacterSet() {
        return backend.getSpecificCharacterSet();
    }
    
    public Dataset getParent() {
        return backend.getParent();
    }

    public void setPrivateCreatorID(String privateCreatorID) {
        backend.setPrivateCreatorID(privateCreatorID);
    }       

    public String getPrivateCreatorID() {
        return backend.getPrivateCreatorID();
    }
    
    public long getItemOffset() {
        return backend.getItemOffset();
    }

    public DcmHandler getDcmHandler() {
        throw new UnsupportedOperationException();
    }

    public DefaultHandler getSAXHandler() {
        throw new UnsupportedOperationException();
    }
    
    protected DcmElement put(DcmElement el) {
       if (!filter(el.tag(), el.vr())) {
          throw new IllegalArgumentException(
            "" + el + " does not fit in this sub DataSet");
       }
       return backend.put(el);
    }
    
    public DcmElement remove(int tag) {
        DcmElement el = backend.get(tag);
        return (el != null && filter(tag, el.vr())) ? backend.remove(tag) : null;
    }
    
    public void clear() {
      ArrayList toRemove = new ArrayList();
      for (Iterator iter = backend.iterator(); iter.hasNext();) {
         DcmElement el = (DcmElement)iter.next();
         if (filter(el.tag(), el.vr())) {
            toRemove.add(el);
         }
      }
      for (int i = 0, n = toRemove.size(); i < n; ++i) {
         backend.remove(((DcmElement)toRemove.get(i)).tag());
      }
    }

    public Dataset setItemOffset(long itemOffset) {
        throw new UnsupportedOperationException();
    }
    
    public void readDataset(InputStream in, DcmDecodeParam param, int stopTag)
            throws IOException {
        throw new UnsupportedOperationException();
    }    

    public void readFile(InputStream in, FileFormat format, int stopTag)
            throws IOException {
        throw new UnsupportedOperationException();
    }    
    
    public void readFile(ImageInputStream iin, FileFormat format, int stopTag)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    public void readFile(File f, FileFormat format, int stopTag)
	    	throws IOException {
        throw new UnsupportedOperationException();
	}

    public DcmElement putXX(int tag, int vr) {
        if (!filter(tag, vr)) {
            throw new IllegalArgumentException(
              "Tag " + Tags.toString(tag) + ", VR " + VRs.toString(vr) + " does not fit in this sub DataSet");
        }
        return backend.putXX(tag, vr);
    }
    
    static final class Selection extends FilterDataset {
        private final Dataset filter;
        Selection(Dataset backend, Dataset filter) {
            super(backend);
            this.filter = filter;
        }

        public  boolean filter(int tag, int vr) {
            return filter == null || filter.contains(tag);
        }

        public DcmElement get(int tag) {
            if (filter == null) {
                return backend.get(tag);
            }
            DcmElement filterEl = filter.get(tag);
            if (filterEl == null) {
                return null;
            }
            
            DcmElement el = backend.get(tag);
            if (!(el instanceof SQElement)) {
                return el;
            }
            if (!(filterEl instanceof SQElement)) {
                log.warn("VR mismatch - dataset:" + el
                        + ", filter:" + filterEl);
                return el;
            }
            Dataset item = filterEl.getItem();
            if (item == null || item.isEmpty()) {
                return el;
            }
            return new FilterSQElement((SQElement)el, item);
        }
    }

    static final class ExcludePrivate extends FilterDataset {
        ExcludePrivate(Dataset backend) {
            super(backend);
        }

        public boolean filter(int tag, int vr) {
            return !Tags.isPrivate(tag);
        }

        public DcmElement get(int tag) {
            if (Tags.isPrivate(tag)) return null;
            DcmElement el = backend.get(tag);
            if (!(el instanceof SQElement)) {
                return el;
            }
            return new ExcludePrivateSQElement((SQElement)el);
        }
    }
    
    static final class Segment extends FilterDataset {
        private long fromTag;
        private long toTag;
        Segment(Dataset backend, int fromTag, int toTag) {
            super(backend);
            this.fromTag = fromTag & 0xFFFFFFFFL;
            this.toTag = toTag & 0xFFFFFFFFL;
            if (this.fromTag > this.toTag) {
               throw new IllegalArgumentException(
                  "fromTag:" + Tags.toString(fromTag)
                  + " greater toTag:" + Tags.toString(toTag));
            }
        }
        
        public int size() {
            int count = 0;
            long ltag;
            for (Iterator iter = backend.iterator(); iter.hasNext();) {
                ltag = ((DcmElement)iter.next()).tag() & 0xFFFFFFFFL;
                if (ltag < fromTag) continue;
                if (ltag >= toTag) break;
                ++count;
            }
            return count;
        }

        public boolean filter(int tag, int vr) {
            long ltag = tag & 0xFFFFFFFFL;
            return ltag >= fromTag && ltag < toTag;
        }
    } 

    static final int[] EMPTY_INT = {};
    
    static final class TagFilter extends FilterDataset {
        private final int[] tags;        
        private final int[] vrs;
		private final boolean exclude;
		private final boolean excludePrivate;
        TagFilter(Dataset backend, int[] tags, int[] vrs, boolean exclude, 
                boolean excludePrivate) {
            super(backend);
            this.tags = tags == null ? EMPTY_INT : (int[]) tags.clone();
            this.exclude = exclude;
            this.excludePrivate = excludePrivate;
            this.vrs = vrs == null ? EMPTY_INT : (int[]) vrs.clone();
            Arrays.sort(this.tags);
        }
        
        public boolean filter(int tag, int vr) {
            return !(excludePrivate && Tags.isPrivate(tag))
            	&& (containsVR(vr) || (Arrays.binarySearch(tags, tag)) >= 0) ? 
                        !exclude : exclude;
        }

        private boolean containsVR(int vr) {
            for (int i = 0; i < vrs.length; i++) {
                if (vrs[i] == vr) {
                    return true;
                }
            }
            return false;
        }
        
    } 
        
}
