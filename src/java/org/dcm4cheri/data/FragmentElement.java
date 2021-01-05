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

import org.dcm4che.data.DcmElement;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4cheri.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;


/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
abstract class FragmentElement extends DcmElementImpl {
    
    private final ArrayList list = new ArrayList();

    /** Creates a new instance of ElementImpl */
    public FragmentElement(int tag) {
        super(tag);
    }
    
    public final int vm(SpecificCharacterSet cs) {
        return list.size();
    }
    
    public final int countItems() {
        return list.size();
    }
    
    public final boolean hasDataFragments() {
       return true;
    }
    
    public final ByteBuffer getDataFragment(int index) {
        return index < list.size() ? (ByteBuffer)list.get(index) : null;
    }
    
    public final ByteBuffer getDataFragment(int index, ByteOrder byteOrder) {
        ByteBuffer data = getDataFragment(index) ;
        if (data != null && data.order() != byteOrder) {
            swapOrder(data);
        }
        return data;
    }
    
    public final int getDataFragmentLength(int index) {
        return (((ByteBuffer)list.get(index)).limit()+1) & ~1;
    }

    public String getString(int index, Charset cs) {
        return getBoundedString(Integer.MAX_VALUE, index, cs);
    }

    public String getBoundedString(int maxLen, int index, Charset cs) {
        return index < list.size()
                ? StringUtils.promptValue(vr(), (ByteBuffer)list.get(index), maxLen)
                : null;
    }

    public String[] getStrings(Charset cs) {
       return getBoundedStrings(Integer.MAX_VALUE, cs);
    }
    
    public String[] getBoundedStrings(int maxLen, Charset cs) {
        String[] a = new String[list.size()];
        for (int i = 0; i < a.length; ++i)
            a[i] = StringUtils.promptValue(vr(), (ByteBuffer)list.get(i), maxLen);
        return a;
    }
    
    int calcLength() {
        int len = 8;
        for (int i = 0, n = list.size(); i < n; ++i)
            len += getDataFragmentLength(i) + 8;
        return len;
    }
    
    public void addDataFragment(ByteBuffer data) {
        list.add(data != null ? data : EMPTY_VALUE);
    }

    protected void swapOrder(ByteBuffer data) {
        data.order(swap(data.order()));
    }
    
    private static final class OB extends FragmentElement {
        OB(int tag) {
            super(tag);
        }
        public final int vr() {
            return VRs.OB;
        }
    }

    public static DcmElement createOB(int tag) {
        return new FragmentElement.OB(tag);
    }
 
    private static final class OF extends FragmentElement {
        OF(int tag) {
            super(tag);
        }
        public final int vr() {
            return VRs.OF;
        }

        public void addDataFragment(ByteBuffer data) {
            if ((data.limit() & 3) != 0) {
                log.warn("Ignore odd length fragment of "
                    + Tags.toString(tag) + " OF #" + data.limit());
                data = null;
            }
            super.addDataFragment(data);
        }

        protected void swapOrder(ByteBuffer data) {
            swapInts(data);
        }
    }

    public static DcmElement createOF(int tag) {
        return new FragmentElement.OF(tag);
    }

    private static final class OW extends FragmentElement {
        OW(int tag) {
            super(tag);
        }
        public final int vr() {
            return VRs.OW;
        }

        public void addDataFragment(ByteBuffer data) {
            if ((data.limit() & 1) != 0) {
                log.warn("Ignore odd length fragment of "
                    + Tags.toString(tag) + " OW #" + data.limit());
                data = null;
            }
            super.addDataFragment(data);
        }

        protected void swapOrder(ByteBuffer data) {
            swapWords(data);
        }
    }

    public static DcmElement createOW(int tag) {
        return new FragmentElement.OW(tag);
    }
 
    private static final class UN extends FragmentElement {
        UN(int tag) {
            super(tag);
        }
        public final int vr() {
            return VRs.UN;
        }
    }

    public static DcmElement createUN(int tag) {
        return new FragmentElement.UN(tag);
    }

    public String toString() {
       StringBuffer sb = new StringBuffer(DICT.toString(tag));
       sb.append(",").append(VRs.toString(vr()));
       if (!isEmpty()) {
          for (int i = 0, n = list.size(); i < n; ++i) {
              sb.append("\n\tFrag-").append(i+1)
                .append(",#").append(getDataFragmentLength(i)).append("[")
                .append(StringUtils.promptValue(vr(), (ByteBuffer)list.get(i), 64))
                .append("]");
          }
       }
       return sb.toString();
    }
}