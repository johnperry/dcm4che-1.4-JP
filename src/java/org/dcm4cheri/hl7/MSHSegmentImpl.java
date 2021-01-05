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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import org.dcm4che.hl7.HL7;
import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.MSHSegment;

/**
 * <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created    March 13, 2003
 * @see        <related>
 * @version    $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 */
public class MSHSegmentImpl extends HL7SegmentImpl implements MSHSegment
{
    // Constants -----------------------------------------------------
    private final static byte[] START_WITH = {
            (byte) 'M', (byte) 'S', (byte) 'H', (byte) '|',
            (byte) '^', (byte) '~', (byte) '\\', (byte) '&', (byte) '|',
            };
    private final static byte[] ACK = {
            (byte) '|', (byte) '|', (byte) '|',
            (byte) 'A', (byte) 'C', (byte) 'K', (byte) '|', (byte) 'A'
            };
    private final static byte[] MSA = {
            (byte) '\r', (byte) 'M', (byte) 'S', (byte) 'A', (byte) '|'
            };
    private final static byte[] ERR = {
            (byte) '\r', (byte) 'E', (byte) 'R', (byte) 'R', (byte) '|'
            };
    private final static byte[] AA = {
            (byte) 'A', (byte) 'A'
            };
    private final static byte[] AE = {
            (byte) 'A', (byte) 'E'
            };
    private final static byte[] AR = {
            (byte) 'A', (byte) 'R'
            };
    private final static HashMap csMap = new HashMap();
    static {
        csMap.put("", "ISO_IR 100");
        csMap.put("ASCII", "ISO_IR 100");
        csMap.put("8859/1", "ISO_IR 100");
        csMap.put("8859/2", "ISO_IR 101");
        csMap.put("8859/3", "ISO_IR 109");
        csMap.put("8859/4", "ISO_IR 110");
        csMap.put("8859/5", "ISO_IR 144");
        csMap.put("8859/6", "ISO_IR 127");
        csMap.put("8859/7", "ISO_IR 126");
        csMap.put("8859/8", "ISO_IR 138");
        csMap.put("8859/9", "ISO_IR 148");
    }


    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    MSHSegmentImpl(byte[] data)
        throws HL7Exception
    {
        this(data, 0, HL7MessageImpl.indexOfNextCRorLF(data, 0));
    }


    MSHSegmentImpl(byte[] data, int off, int len)
        throws HL7Exception
    {
        super(data, off, len);
        for (int i = 0; i < START_WITH.length; ++i) {
            if (data[off + i] != START_WITH[i]) {
                throw new IllegalArgumentException(toString());
            }
        }
    }

    // Public --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public int size()
    {
        return super.size() + 1;
    }


    /**
     *  Description of the Method
     *
     * @param  seq  Description of the Parameter
     * @return      Description of the Return Value
     */
    public String get(int seq)
    {
        switch (seq) {
            case 0:
                return "MSH";
            case 1:
                return "|";
            default:
                return super.get(seq - 1);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  seq  Description of the Parameter
     * @param  rep  Description of the Parameter
     * @return      Description of the Return Value
     */
    public String get(int seq, int rep)
    {
        if (seq < 2) {
            throw new IllegalArgumentException("seq: " + seq);
        }
        return super.get(seq - 1, rep);
    }


    /**
     *  Description of the Method
     *
     * @param  seq   Description of the Parameter
     * @param  rep   Description of the Parameter
     * @param  comp  Description of the Parameter
     * @return       Description of the Return Value
     */
    public String get(int seq, int rep, int comp)
    {
        if (seq < 2) {
            throw new IllegalArgumentException("seq: " + seq);
        }
        return super.get(seq - 1, rep, comp);
    }


    /**
     *  Description of the Method
     *
     * @param  seq   Description of the Parameter
     * @param  rep   Description of the Parameter
     * @param  comp  Description of the Parameter
     * @param  sub   Description of the Parameter
     * @return       Description of the Return Value
     */
    public String get(int seq, int rep, int comp, int sub)
    {
        if (seq < 2) {
            throw new IllegalArgumentException("seq: " + seq);
        }
        return super.get(seq - 1, rep, comp, sub);
    }


    /**
     *  Description of the Method
     *
     * @param  seq  Description of the Parameter
     * @param  out  Description of the Parameter
     */
    public void writeTo(int seq, ByteArrayOutputStream out)
    {
        if (seq < 2) {
            throw new IllegalArgumentException("seq: " + seq);
        }
        super.writeTo(seq - 1, out);
    }


    /**
     *  Gets the messageControlID attribute of the MSHSegmentImpl object
     *
     * @return    The messageControlID value
     */
    public String getMessageControlID()
    {
        return get(HL7.MSHMessageControlID);
    }


    /**
     *  Gets the receivingApplication attribute of the MSHSegmentImpl object
     *
     * @return    The receivingApplication value
     */
    public String getReceivingApplication()
    {
        return get(HL7.MSHReceivingApplication);
    }


    /**
     *  Gets the receivingFacility attribute of the MSHSegmentImpl object
     *
     * @return    The receivingFacility value
     */
    public String getReceivingFacility()
    {
        return get(HL7.MSHReceivingFacility);
    }


    /**
     *  Gets the sendingApplication attribute of the MSHSegmentImpl object
     *
     * @return    The sendingApplication value
     */
    public String getSendingApplication()
    {
        return get(HL7.MSHSendingApplication);
    }


    /**
     *  Gets the sendingFacility attribute of the MSHSegmentImpl object
     *
     * @return    The sendingFacility value
     */
    public String getSendingFacility()
    {
        return get(HL7.MSHSendingFacility);
    }


    /**
     *  Gets the messageType attribute of the MSHSegmentImpl object
     *
     * @return    The messageType value
     */
    public String getMessageType()
    {
        return get(HL7.MSHMessageType, 1, 1);
    }


    /**
     *  Gets the triggerEvent attribute of the MSHSegmentImpl object
     *
     * @return    The triggerEvent value
     */
    public String getTriggerEvent()
    {
        return get(HL7.MSHMessageType, 1, 2);
    }


    /**
     *  Gets the characterSet attribute of the MSHSegmentImpl object
     *
     * @return    The characterSet value
     */
    public String getCharacterSet()
    {
        return get(HL7.MSHCharacterSet);
    }


    /**
     *  Gets the characterSetAsISO_IR attribute of the MSHSegmentImpl object
     *
     * @return    The characterSetAsISO_IR value
     */
    public String getCharacterSetAsISO_IR()
    {
        return (String) csMap.get(getCharacterSet());
    }


    private void writeTo(byte[] b, ByteArrayOutputStream out)
    {
        out.write(b, 0, b.length);
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public byte[] makeACK_AA()
    {
        return ack(AA, null, null, null);
    }


    /**
     *  Description of the Method
     *
     * @param  errText     Description of the Parameter
     * @param  errCode     Description of the Parameter
     * @param  errComment  Description of the Parameter
     * @return             Description of the Return Value
     */
    public byte[] makeACK_AR(String errText, String errCode, String errComment)
    {
        return ack(AR, errText, errCode, errComment);
    }


    /**
     *  Description of the Method
     *
     * @param  errText     Description of the Parameter
     * @param  errCode     Description of the Parameter
     * @param  errComment  Description of the Parameter
     * @return             Description of the Return Value
     */
    public byte[] makeACK_AE(String errText, String errCode, String errComment)
    {
        return ack(AE, errText, errCode, errComment);
    }


    byte[] ack(byte[] ackCode, String errText, String errCode, String errComment)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream(64);
        writeTo(START_WITH, out);
        writeTo(HL7.MSHReceivingApplication, out);
        out.write('|');
        writeTo(HL7.MSHReceivingFacility, out);
        out.write('|');
        writeTo(HL7.MSHSendingApplication, out);
        out.write('|');
        writeTo(HL7.MSHSendingFacility, out);
        writeTo(ACK, out);
        writeTo(HL7.MSHMessageControlID, out);
        out.write('|');
        writeTo(HL7.MSHProcessingID, out);
        out.write('|');
        writeTo(HL7.MSHVersionID, out);
        writeTo(MSA, out);
        writeTo(ackCode, out);
        out.write('|');
        writeTo(HL7.MSHMessageControlID, out);
        if (errText != null || errCode != null) {
            out.write('|');
            if (errText != null) {
                writeTo(errText.getBytes(), out);
            }
            if (errCode != null) {
                out.write('|');
                out.write('|');
                out.write('|');
                writeTo(errCode.getBytes(), out);
            }
        }
        if (errComment != null) {
            writeTo(ERR, out);
            writeTo(errComment.getBytes(), out);
        }
        out.write('\r');
        return out.toByteArray();
    }
}

