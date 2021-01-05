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

package org.dcm4che.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.util.UIDGenerator;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since Jun 21, 2003
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 *
 */
public class PrintSCU {

    static final AssociationFactory assocFact =
        AssociationFactory.getInstance();
    static final DcmObjectFactory dcmFact = DcmObjectFactory.getInstance();
    static final String[] TS_NO_EXPLICIT_VR = { UIDs.ImplicitVRLittleEndian };
    static final String[] TS_EXPLICIT_VR =
        { UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian };
    static final UIDGenerator UID_GEN = UIDGenerator.getInstance();

    private final AssociationRequestor requestor;
    private final PropertyChangeListener closeListener =
        new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            requestor.removePropertyChangeListener(
                AssociationRequestor.CONNECTED,
                this);
            curFilmSessionIUID = null;
            curFilmBoxIUID = null;
            curPLUT_IUID = null;
            curFilmBox = null;
            curPrinterInfo = null;
            printerInfoTimestamp = 0;
        }
    };
    
    public static final String NORMAL = "NORMAL"; 
    public static final String WARNING = "WARNING"; 
    public static final String ERROR = "ERROR"; 
    private static final String[] EVENT_TYPE_TO_STATUS = {
        NORMAL,
        WARNING,
        ERROR
    };
    
    private final DcmServiceBase printerSOP = new DcmServiceBase(){
        protected Dataset doNEventReport(ActiveAssociation assoc, Dimse rq,
            Command rspCmd)
        throws IOException, DcmServiceException {
            Command cmd = rq.getCommand();
            Dataset ds = rq.getDataset();
            int eventTypeID = cmd.getInt(Tags.EventTypeID, 0);
            checkNEventReport(eventTypeID, ds);
            if (curPrinterInfo == null) {
                curPrinterInfo = dcmFact.newDataset();
            }
            curPrinterInfo.putCS(Tags.PrinterStatus,
                EVENT_TYPE_TO_STATUS[eventTypeID-1]);
            curPrinterInfo.putCS(Tags.PrinterStatusInfo,
                eventTypeID == 1 ? NORMAL 
                    : ds.getString(Tags.PrinterStatusInfo));
            printerInfoTimestamp = System.currentTimeMillis();
            return null;
        }

        private void checkNEventReport(int eventTypeID, Dataset ds) throws DcmServiceException {
            switch (eventTypeID) {
                case 1:
                    break;
                case 2:
                case 3:
                    if (ds == null) {
                        throw new DcmServiceException(
                            Status.InvalidArgumentValue,
                            "Missing Event Information in N-EVENT-REPORT-RQ");
                    }
                    String statusInfo = ds.getString(Tags.PrinterStatusInfo); 
                    if  (statusInfo == null) {
                        throw new DcmServiceException(
                            Status.InvalidArgumentValue,
                            "Missing Printer Status Info in N-EVENT-REPORT-RQ");
                    }
                    break;
                default:
                    throw new DcmServiceException(
                        Status.NoSuchEventType,
                        "Invalid Event Type ID: " + eventTypeID);
            }

        }        
    };

    private boolean negotiateColorPrint = true;
    private boolean negotiateGrayscalePrint = true;
    private boolean negotiatePLUT = true;
    private boolean negotiateAnnotation = true;
    private String[] tsuids = TS_EXPLICIT_VR;
    private boolean createRQwithIUID = false;
    private boolean autoRefPLUT = true;
    private int[] infoAttrList = null;
    private int maxPrinterInfoStale = 5000;
    private long printerInfoTimestamp = 0L;

    private int pcidColorPrint = 0;
    private int pcidGrayscalePrint = 0;
    private int pcidPLUT = 0;
    private int pcidAnnotation = 0;
    private int pcidPrint = 0;

    private String curFilmSessionIUID;
    private String curFilmBoxIUID;
    private String curPLUT_IUID;
    private Dataset curFilmBox;
    private Dataset curPrinterInfo;
    private byte[] buffer = new byte[4096];

    public PrintSCU(AssociationRequestor requestor) {
        this.requestor = requestor;
        this.requestor.bindService(UIDs.Printer, printerSOP);
        updatePresContexts();
    }

    private void updatePresContexts() {
        updateGrayscalePrintPresContext();
        updateColorPrintPresContext();
        updatePLUTPresContext();
        updateAnnotationPresContext();
    }

    private void updateGrayscalePrintPresContext() {
        if (negotiateGrayscalePrint) {
            if (pcidGrayscalePrint == 0) {
                pcidGrayscalePrint =
                    requestor.addPresContext(
                        UIDs.BasicGrayscalePrintManagement,
                        tsuids);
            }
        } else {
            if (pcidGrayscalePrint != 0) {
                requestor.removePresContext(pcidGrayscalePrint);
                pcidGrayscalePrint = 0;
            }
        }
    }

    private void updateColorPrintPresContext() {
        if (negotiateColorPrint) {
            if (pcidColorPrint == 0) {
                pcidColorPrint =
                    requestor.addPresContext(
                        UIDs.BasicColorPrintManagement,
                        tsuids);
            }
        } else {
            if (pcidColorPrint != 0) {
                requestor.removePresContext(pcidColorPrint);
                pcidColorPrint = 0;
            }
        }
    }

    private void updatePLUTPresContext() {
        if (negotiatePLUT) {
            if (pcidPLUT == 0) {
                pcidPLUT =
                    requestor.addPresContext(UIDs.PresentationLUT, tsuids);
            }
        } else {
            if (pcidPLUT != 0) {
                requestor.removePresContext(pcidPLUT);
                pcidPLUT = 0;
            }
        }
    }

    private void updateAnnotationPresContext() {
        if (negotiateAnnotation) {
            if (pcidAnnotation == 0) {
                pcidAnnotation =
                    requestor.addPresContext(UIDs.BasicAnnotationBox, tsuids);
            }
        } else {
            if (pcidAnnotation != 0) {
                requestor.removePresContext(pcidAnnotation);
                pcidAnnotation = 0;
            }
        }
    }

    /**
     * Get whether a presentation context for Grayscale printing was negotiated
     * on the corresponding association.
     * @return true if the PrintSCU has negotiated this presentation context,
     * false otherwise
     */
    public boolean isNegotiateGrayscalePrint() {
        return negotiateGrayscalePrint;
    }

    /**
     * Set whether to negotiate a presentation context for Grayscale printing on
     * the corresponding association.
     * @param negotiateGrayscalePrint true if this presentation syntax should be
     * negotiated, false otherwise
     */
    public void setNegotiateGrayscalePrint(boolean negotiateGrayscalePrint) {
        this.negotiateGrayscalePrint = negotiateGrayscalePrint;
        updateGrayscalePrintPresContext();
    }

    /**
     * Get whether a presentation context for Color printing was negotiated
     * on the corresponding association.
     * @return true if the PrintSCU has negotiated this presentation context,
     * false otherwise
     */
    public boolean isNegotiateColorPrint() {
        return negotiateColorPrint;
    }

    /**
     * @param negotiateColorPrint
     */
    public void setNegotiateColorPrint(boolean negotiateColorPrint) {
        this.negotiateColorPrint = negotiateColorPrint;
        updateColorPrintPresContext();
    }

    /**
     * Get whether a presentation context for sending a Presentation Look-up Table
     * (P-LUT) was negotiated on the corresponding association.
     * @return true if the PrintSCU has negotiated this presentation context,
     * false otherwise
     */
    public boolean isNegotiatePLUT() {
        return negotiatePLUT;
    }

    /**
     * @param negotiatePLUT
     */
    public void setNegotiatePLUT(boolean negotiatePLUT) {
        this.negotiatePLUT = negotiatePLUT;
        updatePLUTPresContext();
    }

    /**
     * Get whether a presentation context for setting Annotations was negotiated
     * on the corresponding association.
     * @return true if the PrintSCU has negotiated this presentation context,
     * false otherwise
     */
    public boolean isNegotiateAnnotation() {
        return negotiateAnnotation;
    }

    /**
     * @param negotiateAnnotation
     */
    public void setNegotiateAnnotation(boolean negotiateAnnotation) {
        this.negotiateAnnotation = negotiateAnnotation;
        updateAnnotationPresContext();
    }

    /**
     * @return true if a UID will be generated by this instance, false if the
     *  user is expected to pass this to create* or set* methods requiring a
     *  UID.
     */
    public boolean isCreateRQwithIUID() {
        return createRQwithIUID;
    }

    /**
     * Set whether a UID should be generated by this instance. Set to false if
     * the user is expected to pass this to create* or set* methods requiring
     * a UID.
     */
    public void setCreateRQwithIUID(boolean createRQwithIUID) {
        this.createRQwithIUID = createRQwithIUID;
    }

    /**
     * @return true if this PrintSCU will automatically place a Referenced P-LUT
     *  Sequence and the reference to a created P-LUT in a created Film Box's
     *  attributes, false otherwise
     */
    public boolean isAutoRefPLUT() {
        return autoRefPLUT;
    }

    /**
     * If true and a P-LUT has been created, this PrintSCU will automatically
     * place a Referenced P-LUT Sequence and the reference to a created P-LUT
     * in a created Film Box's attributes.
     */
    public void setAutoRefPLUT(boolean autoRefPLUT) {
        this.autoRefPLUT = autoRefPLUT;
    }

    /**
     * @return the <code>AssociationRequestor</code> associated with this
     *  PrintSCU
     */
    public AssociationRequestor getRequestor() {
        return requestor;
    }

    /**
     * @return returns whether an Explicit VR (Value Representation) was
     * negotiated on the association.
     */
    public boolean isNegotiateExplicitVR() {
        return tsuids == TS_EXPLICIT_VR;
    }

    /**
     * Set whether an Explicit VR (Value Representation) should be negotiated
     * on the association.
     * @param negotiateExplicitVR true if the PrintSCU should negotiated this
     *  presentation context, false otherwise
     */
    public void setNegotiateExplicitVR(boolean negotiateExplicitVR) {
        if (isNegotiateExplicitVR() == negotiateExplicitVR) {
            return;
        }
        this.tsuids = negotiateExplicitVR ? TS_EXPLICIT_VR : TS_NO_EXPLICIT_VR;
        removePresContexts();
        updatePresContexts();
    }

    private void removePresContexts() {
        if (pcidColorPrint != 0) {
            requestor.removePresContext(pcidColorPrint);
            pcidColorPrint = 0;
        }
        if (pcidGrayscalePrint != 0) {
            requestor.removePresContext(pcidGrayscalePrint);
            pcidGrayscalePrint = 0;
        }
        if (pcidPLUT != 0) {
            requestor.removePresContext(pcidPLUT);
            pcidPLUT = 0;
        }
        if (pcidAnnotation != 0) {
            requestor.removePresContext(pcidAnnotation);
            pcidAnnotation = 0;
        }
    }

    public boolean isGrayscalePrintEnabled() {
        return isEnabled(pcidGrayscalePrint);
    }

    private boolean isEnabled(int pcid) {
        return pcid != 0
            && requestor.isConnected()
            && requestor.getAcceptedTransferSyntaxUID(pcid) != null;
    }

    /**
     * @return true if Color printing is enabled on an active association
     */
    public boolean isColorPrintEnabled() {
        return isEnabled(pcidColorPrint);
    }

    /**
     * @return true if P-LUTs are enabled on an active association
     */
    public boolean isPLUTEnabled() {
        return isEnabled(pcidPLUT);
    }

    /**
     * @return true if Annotations are enabled on an active association
     */
    public boolean isAnnotationEnabled() {
        return isEnabled(pcidAnnotation);
    }

    private static int[] clone(int[] a) {
        return a != null ? (int[]) a.clone() : null;
    }

    /**
     * @return array of printer attributes
     */
    public int[] getInfoAttrList() {
        return clone(infoAttrList);
    }

    /**
     * @param infoAttrList array of printer attributes
     */
    public void setInfoAttrList(int[] infoAttrList) {
        this.infoAttrList = clone(infoAttrList);
    }

    /**
     * Get the printer information validity period.
     * @return the maximum length of time (in milliseconds) to elapse before
     *  the printer information is considered "stale" and will be re-queried
     *  on the next call to a method that needs printer information.
     */
    public int getMaxPrinterInfoStale() {
        return maxPrinterInfoStale;
    }

    /**
     * Set the printer information validity period.
     * @param maxStatusStale the maximum length of time (in milliseconds) to
     *  elapse before the printer information is considered "stale" and will
     *  be re-queried on the next call to a method that needs printer
     *  information.
     */
    public void setMaxPrinterInfoStale(int maxPrinterInfoStale) {
        this.maxPrinterInfoStale = maxPrinterInfoStale;
    }

    public Dataset getPrinterInfo(boolean forceQuery)
        throws InterruptedException, IOException, DcmServiceException {
        if (forceQuery || curPrinterInfo == null
            || System.currentTimeMillis() - printerInfoTimestamp
                > maxPrinterInfoStale) {
            queryPrinterInfo();
        }
        return curPrinterInfo;
    }

    public String getPrinterStatus()
        throws InterruptedException, IOException, DcmServiceException {
        return getPrinterInfo(false).getString(Tags.PrinterStatus);
    }

    public String getPrinterStatusInfo()
        throws InterruptedException, IOException, DcmServiceException {
        return getPrinterInfo(false).getString(Tags.PrinterStatus);
    }

    private static int[] NO_ERROR_STATI =
        {
            Status.Success,
            Status.AttributeValueOutOfRange,
            Status.MinMaxDensityOutOfRange,
            Status.MemoryAllocationNotSupported };

    private int checkStatus(Command rsp) throws DcmServiceException {
        int status = rsp.getStatus();
        for (int i = 0; i < NO_ERROR_STATI.length; i++) {
            if (status == NO_ERROR_STATI[i]) {
                return status;
            }
        }
        throw new DcmServiceException(status, rsp.getString(Tags.ErrorComment));
    }

    private int queryPrinterInfo()
        throws InterruptedException, IOException, DcmServiceException {
        checkAssociation();
        int msgid = requestor.nextMsgID();
        Command nGetRQ = dcmFact.newCommand();
        nGetRQ.initNGetRQ(
            msgid,
            UIDs.Printer,
            UIDs.PrinterSOPInstance,
            infoAttrList);
        Dimse rsp = requestor.invokeAndWaitForRSP(pcidPrint, nGetRQ);
        int status = checkStatus(rsp.getCommand());
        curPrinterInfo = rsp.getDataset();
        if (curPrinterInfo == null) {
            throw new DcmServiceException(
                -1,
                "Missing Attribute List in N-GET-RSP");
        }
        printerInfoTimestamp = System.currentTimeMillis();
        return status;
    }

    /**
     * Creates a P-LUT (Presentation Look-up Table).
     * @param attr The P-LUT attributes. See <i>DICOM ps 3.3, appendix B.18:
     *  Presentation LUT Information Object Definition</i> for more information.
     * @return the UID of the created P-LUT
     * @throws InterruptedException
     * @throws IOException
     * @throws DcmServiceException
     */
    public String createPLUT(Dataset attr)
        throws InterruptedException, IOException, DcmServiceException {
        checkAssociation();
        String iuid = createRQwithIUID ? UID_GEN.createUID() : null;
        int msgid = requestor.nextMsgID();
        Command nCreateRQ = dcmFact.newCommand();
        nCreateRQ.initNCreateRQ(msgid, UIDs.PresentationLUT, iuid);
        Dimse rsp = requestor.invokeAndWaitForRSP(pcidPLUT, nCreateRQ, attr);
        Command nCreateRSP = rsp.getCommand();
        int status = checkStatus(nCreateRSP);
        curPLUT_IUID = checkIUID(iuid, nCreateRSP);
        return curPLUT_IUID;
    }

    /**
     * Creates a P-LUT (Presentation Look-up Table) using a given predfined
     * shape.
     * @param shape The P-LUT shape. See <i>DICOM ps 3.3, appendix B.18:
     *  Presentation LUT Information Object Definition</i> for more information.
     * @return the UID of the created P-LUT
     * @throws InterruptedException
     * @throws IOException
     * @throws DcmServiceException
     */
    public String createPLUT(String shape)
        throws InterruptedException, IOException, DcmServiceException {
        Dataset plut = dcmFact.newDataset();
        plut.putCS(Tags.PresentationLUTShape, shape);
        return createPLUT(plut);
    }

    public int deletePLUT(String iuid)
        throws InterruptedException, IOException, DcmServiceException {
        checkAssociation();
        int msgid = requestor.nextMsgID();
        Command nDeleteRQ = dcmFact.newCommand();
        nDeleteRQ.initNDeleteRQ(msgid, UIDs.PresentationLUT, iuid);
        Dimse rsp = requestor.invokeAndWaitForRSP(pcidPLUT, nDeleteRQ);
        return checkStatus(rsp.getCommand());
    }

    public void checkAssociation() {
        if (!requestor.isConnected()) {
            throw new IllegalStateException("No Association exists");
        }
    }

    public int setAnnotationBox(int index, String text)
        throws InterruptedException, IOException, DcmServiceException {
        if (index < 0 || index >= countAnnotationBoxes()) {
            throw new IndexOutOfBoundsException(
                "index:" + index + ", count:" + countAnnotationBoxes());
        }
        int msgid = requestor.nextMsgID();
        Dataset attr = dcmFact.newDataset();
        attr.putUS(Tags.AnnotationPosition, index + 1);
        attr.putLO(Tags.TextString, text);
        Dataset refAnnBox =
            curFilmBox.getItem(Tags.RefBasicAnnotationBoxSeq, index);
        Command nSetRQ = dcmFact.newCommand();
        nSetRQ.initNSetRQ(
            msgid,
            refAnnBox.getString(Tags.RefSOPClassUID),
            refAnnBox.getString(Tags.RefSOPInstanceUID));
        Dimse rsp = requestor.invokeAndWaitForRSP(pcidAnnotation, nSetRQ, attr);
        Command nSetRSP = rsp.getCommand();
        return checkStatus(nSetRSP);
    }

    private String checkIUID(String iuid, Command nCreateRSP)
        throws DcmServiceException {
        if (iuid == null) {
            iuid = nCreateRSP.getAffectedSOPInstanceUID();
            if (iuid == null) {
                throw new DcmServiceException(
                    -1,
                    "Missing Affected SOP Instance UID in N-CREATE-RSP");
            }
        }
        return iuid;
    }

    /**
     * Creates a Film Session.
     * @param attr The Film Session attributes. See <i>DICOM ps 3.3, appendix B.7:
     *  Basic Film Session Information Object Definition</i> for more information.
     * @param color true if this is a color Film Session, false otherwise
     * @return the status of the created Film Session
     * @throws InterruptedException
     * @throws IOException
     * @throws DcmServiceException
     */
    public int createFilmSession(Dataset attr, boolean color)
        throws InterruptedException, IOException, DcmServiceException {
        pcidPrint = color ? pcidColorPrint : pcidGrayscalePrint;
        String iuid = createRQwithIUID ? UID_GEN.createUID() : null;
        int msgid = requestor.nextMsgID();
        Command nCreateRQ = dcmFact.newCommand();
        nCreateRQ.initNCreateRQ(msgid, UIDs.BasicFilmSession, iuid);
        Dimse rsp = requestor.invokeAndWaitForRSP(pcidPrint, nCreateRQ, attr);
        Command nCreateRSP = rsp.getCommand();
        int status = checkStatus(nCreateRSP);
        curFilmSessionIUID = checkIUID(iuid, nCreateRSP);
        requestor.addPropertyChangeListener(
            AssociationRequestor.CONNECTED,
            closeListener);
        return status;
    }

    private Dataset makeRefSOP(String cuid, String iuid) {
        Dataset refSOP = dcmFact.newDataset();
        refSOP.putUI(Tags.RefSOPClassUID, cuid);
        refSOP.putUI(Tags.RefSOPInstanceUID, iuid);
        return refSOP;
    }

    private void checkSession() {
        if (curFilmSessionIUID == null) {
            throw new IllegalStateException("No current Film Session");
        }
    }

    public int deleteFilmSession()
        throws InterruptedException, IOException, DcmServiceException {
        checkSession();
        int msgid = requestor.nextMsgID();
        Command nDeleteRQ = dcmFact.newCommand();
        nDeleteRQ.initNDeleteRQ(
            msgid,
            UIDs.BasicFilmSession,
            curFilmSessionIUID);
        Dimse rsp = requestor.invokeAndWaitForRSP(pcidPrint, nDeleteRQ);
        Command nDeleteRSP = rsp.getCommand();
        curFilmSessionIUID = null;
        curFilmBoxIUID = null;
        curFilmBox = null;
        return checkStatus(nDeleteRSP);
    }

    /**
     * Creates a Film Box.
     * @param attr The Film Box attributes. See <i>DICOM ps 3.3, appendix B.8:
     *  Basic Film Box Information Object Definition</i> for more information.
     * @return the status of the created Film Box
     * @throws InterruptedException
     * @throws IOException
     * @throws DcmServiceException
     */
    public int createFilmBox(Dataset attr)
        throws InterruptedException, IOException, DcmServiceException {
        checkSession();
        String iuid = createRQwithIUID ? UID_GEN.createUID() : null;
        int msgid = requestor.nextMsgID();
        Command nCreateRQ = dcmFact.newCommand();
        nCreateRQ.initNCreateRQ(msgid, UIDs.BasicFilmBoxSOP, iuid);
        attr.putSQ(Tags.RefFilmSessionSeq).addItem(
            makeRefSOP(UIDs.BasicFilmSession, curFilmSessionIUID));
        ;
        if (autoRefPLUT
            && curPLUT_IUID != null
            && attr.vm(Tags.RefPresentationLUTSeq) == -1) {
            attr.putSQ(Tags.RefPresentationLUTSeq).addItem(
                makeRefSOP(UIDs.PresentationLUT, curPLUT_IUID));
        }
        Dimse rsp = null;
        try {
            rsp = requestor.invokeAndWaitForRSP(pcidPrint, nCreateRQ, attr);
        } finally {
            attr.remove(Tags.RefFilmSessionSeq);
        }
        Command nCreateRSP = rsp.getCommand();
        int status = checkStatus(nCreateRSP);
        curFilmBoxIUID = checkIUID(iuid, nCreateRSP);
        curFilmBox = rsp.getDataset();
        if (curFilmBox == null) {
            throw new DcmServiceException(
                -1,
                "Missing Attribute List in N-CREATE-RSP");
        }
        return status;
    }

    private void checkFilmBox() {
        if (curFilmBox == null) {
            throw new IllegalStateException("No current Film Box");
        }
    }

    public int deleteFilmBox()
        throws InterruptedException, IOException, DcmServiceException {
        checkFilmBox();
        int msgid = requestor.nextMsgID();
        Command nDeleteRQ = dcmFact.newCommand();
        nDeleteRQ.initNDeleteRQ(msgid, UIDs.BasicFilmBoxSOP, curFilmBoxIUID);
        Dimse rsp = requestor.invokeAndWaitForRSP(pcidPrint, nDeleteRQ);
        Command nDeleteRSP = rsp.getCommand();
        curFilmBoxIUID = null;
        curFilmBox = null;
        return checkStatus(nDeleteRSP);
    }

    public int countImageBoxes() {
        checkFilmBox();
        int count = curFilmBox.vm(Tags.RefImageBoxSeq);
        return (count == -1) ? 0 : count;
    }

    public int countAnnotationBoxes() {
        checkFilmBox();
        int count = curFilmBox.vm(Tags.RefBasicAnnotationBoxSeq);
        return (count == -1) ? 0 : count;
    }

    /**
     * Sets a Image Box.
     * @param file The file representing the DICOM image to send
     * @param index The index of the Basic Film Box that this Image Box will be
     *  assigned to.
     * @param attr The Image Box attributes. See <i>DICOM ps 3.3, appendix B.9:
     *  Basic Image Box Information Object Definition</i> for more information.
     * @return the status of the created Image Box
     * @throws InterruptedException
     * @throws IOException
     * @throws DcmServiceException
     */
    public int setImageBox(int index, File file, File psFile, Dataset attr,
        boolean burnInOverlays, boolean autoScale)
        throws InterruptedException, IOException, DcmServiceException {
        if (index < 0 || index >= countImageBoxes()) {
            throw new IndexOutOfBoundsException(
                "index:" + index + ", count:" + countImageBoxes());
        }
        Dataset imageBox = dcmFact.newDataset();
        if (attr != null) {
            imageBox.putAll(attr);
        }
        imageBox.putUS(Tags.ImagePositionOnFilm, index + 1);

        Dataset refImageBox = curFilmBox.getItem(Tags.RefImageBoxSeq, index);
        int msgid = requestor.nextMsgID();
        Command nSetRQ = dcmFact.newCommand();
        nSetRQ.initNSetRQ(
            msgid,
            refImageBox.getString(Tags.RefSOPClassUID),
            refImageBox.getString(Tags.RefSOPInstanceUID));

        Dimse rsp =
            requestor.invokeAndWaitForRSP(
                pcidPrint,
                nSetRQ,
                new PrintSCUDataSource(this, imageBox, file, psFile,
                    burnInOverlays, autoScale));
        return checkStatus(rsp.getCommand());
        //new File(file.getParent() + "/xlut_p03.pre")
    }

    boolean isColorPrint() {
        return pcidPrint == pcidColorPrint;
    }

    byte[] getBuffer() {
        return buffer;
    }

    /**
     * Prints the current Film Box.
     * @return The status of the print request
     * @throws InterruptedException
     * @throws IOException
     * @throws DcmServiceException
     */
    public int printFilmBox()
        throws InterruptedException, IOException, DcmServiceException {
        checkFilmBox();
        int msgid = requestor.nextMsgID();
        Command nActionRQ = dcmFact.newCommand();
        nActionRQ.initNActionRQ(msgid, UIDs.BasicFilmBoxSOP, curFilmBoxIUID, 1);
        Dimse rsp = requestor.invokeAndWaitForRSP(pcidPrint, nActionRQ);
        Command nActionRSP = rsp.getCommand();
        return checkStatus(nActionRSP);
    }

    /**
     * Prints the current Film Session.
     * @return The status of the print request
     * @throws InterruptedException
     * @throws IOException
     * @throws DcmServiceException
     */
    public int printFilmSession()
        throws InterruptedException, IOException, DcmServiceException {
        checkSession();
        int msgid = requestor.nextMsgID();
        Command nActionRQ = dcmFact.newCommand();
        nActionRQ.initNActionRQ(
            msgid,
            UIDs.BasicFilmSession,
            curFilmSessionIUID,
            1);
        Dimse rsp = requestor.invokeAndWaitForRSP(pcidPrint, nActionRQ);
        Command nActionRSP = rsp.getCommand();
        return checkStatus(nActionRSP);
    }
}
