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

package org.dcm4cheri.hl7;

import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.hl7.MSHSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class HL7MessageImpl implements HL7Message {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private final MSHSegmentImpl msh;
    private final ArrayList segs = new ArrayList(4);
    
    // Static --------------------------------------------------------
    static int indexOfNextCRorLF(byte[] data, int start) {
        for (int i = start;  i < data.length; ++i) {
            if (data[i] == (byte)'\r' || data[i] == (byte)'\n') {
                return i;
            }
            if (data[i] == (byte)'\\') {
                ++i;
            }
        }
        return data.length;
    }
    
    // Constructors --------------------------------------------------
    HL7MessageImpl(byte[] data)
    throws HL7Exception {
        int pos = indexOfNextCRorLF(data, 0);
        msh = new MSHSegmentImpl(data, 0, pos);
        while (++pos < data.length) {
            int nextPos = indexOfNextCRorLF(data, pos);
            int len = nextPos - pos;
            if (len > 0) {
                segs.add(new HL7SegmentImpl(data, pos, len));
            }
            pos = nextPos;
        }
    }
    
    // Public --------------------------------------------------------
    public MSHSegment header() {
        return msh;
    }
    
    public List segments() {
        return Collections.unmodifiableList(segs);
    }
    
    public String toString() {
        return segs.toString();
    }
    
    StringBuffer toVerboseStringBuffer(StringBuffer sb) {
        sb.append(msh.getMessageType()).append('^').append(msh.getTriggerEvent())
        .append(" message\t// ")
        .append(HL7SegmentImpl.getName(msh.getMessageType(), "????"))
        .append(" - ")
        .append(HL7SegmentImpl.getName(msh.getTriggerEvent(), "????"));
        sb.append("\n\t");
        msh.toVerboseStringBuffer(sb);
        for (Iterator it =  segs.iterator(); it.hasNext();) {
            sb.append("\n\t");
            ((HL7SegmentImpl)it.next()).toVerboseStringBuffer(sb);
        }
        return sb;
    }
    
    public String toVerboseString() {
        return toVerboseStringBuffer(new StringBuffer()).toString();
    }
}
