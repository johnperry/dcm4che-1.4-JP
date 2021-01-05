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

package org.dcm4che.srom;

import java.util.Date;

import org.dcm4che.Implementation;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;

/**
 * Factory for DICOM SR Documents.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex A.35 Structured Report Document Information Object Definitions"
 */
public abstract class SRDocumentFactory {

    private static SRDocumentFactory instance = (SRDocumentFactory)
            Implementation.findFactory("dcm4che.srom.SRDocumentFactory");

    public static SRDocumentFactory getInstance() {
      return instance;
    }

    /**
     * Creates a new <i>SOP Class/SOP Instance</i> pair.
     *
     * @param refSOPClassUID  the DICOM <i>Referenced SOP Class UID</i>.
     * @param refSOPInstanceUID  the <i>Referenced SOP Instance UID</i>.
     *
     * @return  a new <i>SOP Class/SOP Instance</i> pair.
     */
    public abstract RefSOP newRefSOP(String refSOPClassUID,
            String refSOPInstanceUID);
    
    
    /**
     * Creates a new <code>srom</code> Patient object.
     *
     * @param patientID  the patients ID.
     * @param patientName  the DICOM (VR={@link org.dcm4che.dict.VRs#PN PN})
     *                     patient name.
     * @param patientSex  the patients sex.
     * @param patientBirthDate  the patients birth date.
     *
     * @return  a new <code>srom</code> Patient object.
     */
    public abstract Patient newPatient(String patientID, String patientName,
            Patient.Sex patientSex, Date patientBirthDate);
    
    /**
     * Creates a new <code>srom</code> Study object.
     *
     * @param studyInstanceUID  the study instance UID.
     * @param studyID  the study ID.
     * @param studyDateTime  the study date time.
     * @param referringPhysicianName  the referring physician name.
     * @param accessionNumber  the accession number.
     * @param studyDescription  the study description.
     * @param procCodes  the procedure codes.
     *
     * @return  a new <code>srom</code> Study object.
     */
    public abstract Study newStudy(String studyInstanceUID, String studyID,
            Date studyDateTime, String referringPhysicianName,
            String accessionNumber, String studyDescription,
            Code[] procCodes);
    
    
    /**
     * Creates a new <code>srom</code> Series object.
     *
     * @param modality  the modality.
     * @param seriesInstanceUID  the series instance UID.
     * @param seriesNumber  the series number.
     * @param refStudyComponent  the referenced Study as
     *                           <i>SOP Class/SOP Instance</i> pair.
     *
     * @return  a new <code>srom</code> Series object.
     */
    public abstract Series newSeries(String modality,
            String seriesInstanceUID, int seriesNumber,
            RefSOP refStudyComponent);
    
    /**
     * Creates a new <code>srom</code> Series object for DICOM SR.
     * A convenience method for DICOM SR. The modality value will
     * be set to <code>SR</code>.
     *
     * @param seriesInstanceUID  the series instance UID.
     * @param seriesNumber  the series number.
     * @param refStudyComponent  the referenced Study as
     *                           <i>SOP Class/SOP Instance</i> pair.
     *
     * @return  a new <code>srom</code> Series object.
     */
    public abstract Series newSRSeries(String seriesInstanceUID,
            int seriesNumber, RefSOP refStudyComponent);
    
    /**
     * Creates a new <code>srom</code> Series object for DICOM KO.
     * A convenience method for DICOM KO. The modality value will
     * be set to <code>KO</code>.
     *
     * @param seriesInstanceUID  the series instance UID.
     * @param seriesNumber  the series number.
     * @param refStudyComponent  the referenced Study as
     *                           <i>SOP Class/SOP Instance</i> pair.
     *
     * @return  a new <code>srom</code> Series object.
     */
    public abstract Series newKOSeries(String seriesInstanceUID,
            int seriesNumber, RefSOP refStudyComponent);
    
    /**
     * Creates a new <code>srom</code> Equipment object.
     *
     * @param manufacturer  the manufacturer.
     * @param modelName  the model name.
     * @param stationName  the station name.
     *
     * @return  a new <code>srom</code> Equipment object.
     */
    public abstract Equipment newEquipment(String manufacturer,
            String modelName, String stationName);
    
    /**
     * Creates a new <code>srom</code> Code object.
     *
     * @param codeValue  the code value.
     * @param codingSchemeDesignator  the coding scheme designator.
     * @param codingSchemeVersion  the coding scheme version
     * @param codeMeaning  the code meaning.
     *
     * @return  a new <code>srom</code> Code object.
     */
    public abstract Code newCode(String codeValue, String codingSchemeDesignator,
            String codingSchemeVersion, String codeMeaning);
    
    /**
     * Creates a new <code>srom</code> Code object.
     * A convenience method if the conding scheme designator
     * unambiguously identifies this coding scheme.
     *
     * @param codeValue  the code value.
     * @param codingSchemeDesignator  the coding scheme designator.
     * @param codeMeaning  the code meaning.
     *
     * @return  a new <code>srom</code> Code object.
     */
    public abstract Code newCode(String codeValue,
            String codingSchemeDesignator, String codeMeaning);
    
    /**
     * Creates a new <code>srom</code> Template object.
     *
     * @param templateIdentifier  the Template identifier.
     * @param mappingResource  the mapping resource that defines the template.
     * @param templateVersion  the version of the Template
     * @param templateLocalVersion  the local version number of the Template.
     *
     * @return  a new <code>srom</code> Template object.
     */
    public abstract Template newTemplate(String templateIdentifier,
            String mappingResource,
            Date templateVersion,
            Date templateLocalVersion);
    
    /**
     * Creates a new <code>srom</code> Template object.
     * A convenience methods one could use if the
     * <i>Template Identifier</i>  and <i>Mapping Resource</i>
     * are sufficient to identify the template unambiguously.
     *
     * @param templateIdentifier  the Template identifier.
     * @param mappingResource  the mapping resource that defines the template.
     *
     * @return  a new <code>srom</code> Template object.
     */
    public abstract Template newTemplate(String templateIdentifier,
            String mappingResource);
    
    /**
     * Creates a new <code>srom</code> Verification object.
     *
     * @param time  date and time of verification by the verifying observer.
     * @param observerName  the verifying observer name.
     * @param observerOrg  the verifying organization.
     * @param observerCode  coded identifier of the verifying observer.
     *
     * @return  a new <code>srom</code> Verification object.
     */
    public abstract Verification newVerification(Date time,
            String observerName,
            String observerOrg,
            Code observerCode);
    
    /**
     * Creates a new <code>srom</code> Request object.
     *
     * @param studyInstanceUID  the study instance UID.
     * @param accessionNumber  the accession number
     * @param fillerOrderNumber  the filler order number.
     * @param placerOrderNumber  the placer order number.
     * @param procedureID  the procedure ID.
     * @param procedureDescription  the procedure description.
     * @param procedureCode  the requested procedure code.
     *
     * @return  a new <code>srom</code> Request object.
     */
    public abstract Request newRequest(String studyInstanceUID,
            String accessionNumber,
            String fillerOrderNumber,
            String placerOrderNumber,
            String procedureID,
            String procedureDescription,
            Code procedureCode);
    
    /**
     * Creates a new <code>srom</code> SOPInstanceRef object.
     *
     * @param refSOPClassUID  the referenced SOP class UID.
     * @param refSOPInstanceUID  the referenced SOP instance UID.
     * @param seriesInstanceUID  the series instance UID.
     * @param studyInstanceUID  the study instance UID.
     *
     * @return  a new <code>srom</code> SOPInstanceRef object.
     */
    public abstract SOPInstanceRef newSOPInstanceRef(String refSOPClassUID,
            String refSOPInstanceUID,
            String seriesInstanceUID,
            String studyInstanceUID);
    
    public abstract IconImage newIconImage(int rows, int columns,
            byte[] pixelData);
    
    /**
     * Creates a new <code>srom</code> TCOORD Sample Position.
     *
     * @param indexes  Array of samples within a multiplex group specifying
     *                 temporal points of the referenced data.
     *
     * @return  a new <code>srom</code> TCOORD Sample Position.
     */
    public abstract TCoordContent.Positions.Sample newSamplePositions(
            int[] indexes);
    
    /**
     * Creates a new <code>srom</code> TCOORD Relative Position.
     *
     * @param offsets  Temporal points for reference by number
     *                 of seconds after start of data.
     *
     * @return  a new <code>srom</code> TCOORD Relative Position.
     */
    public abstract TCoordContent.Positions.Relative
            newRelativePositions(float[] offsets);
    
    /**
     * Creates a new <code>srom</code> TCOORD Absolute Position.
     *
     * @param dateTimes  Temporal points for reference by absolute time.
     *
     * @return  a new <code>srom</code> TCOORD Absolute Position.
     */
    public abstract TCoordContent.Positions.Absolute
            newAbsolutePositions(Date[] dateTimes);
    
    /**
     * Creates a new <code>srom</code> <i>Key Object Selection</i> document.
     *
     * @param patient  the srom Patient object to use with this KO.
     * @param study  the srom Study object to use with this KO.
     * @param series  the srom Series object to use with this KO.
     * @param equipment  the srom Equipment object to use with this KO.
     * @param sopInstanceUID  the SOP instance UID.
     * @param instanceNumber  the instance number.
     * @param obsDateTime  the date time of the observation.
     * @param title  the KO title as srom code object.
     * @param separate  whether the document content should the
     *                  interpreted as continuous text or separate sections.
     *
     * @return  a new <code>srom</code> <i>Key Object Selection</i> document.
     */
    public abstract KeyObject newKeyObject(Patient patient, Study study,
            Series series, Equipment equipment, String sopInstanceUID,
            int instanceNumber, Date obsDateTime, Code title, boolean separate);
    
    /**
     * Creates a new <code>srom</code> <i>Structured Report</i> document.
     *
     * @param patient  the srom Patient object to use with this SR.
     * @param study  the srom Study object to use with this SR.
     * @param series  the srom Series object to use with this SR.
     * @param equipment  the srom Equipment object to use with this SR.
     * @param sopClassUID  the SOP class UID either
     *                     <code>1.2.840.10008.5.1.4.1.1.88.11</code> for
     *                     <i>Basic Text SR</i>  or
     *                     <code>1.2.840.10008.5.1.4.1.1.88.22</code> for
     *                     <i>Enhanced SR</i>  or
     *                     <code>1.2.840.10008.5.1.4.1.1.88.33</code> for
     *                     <i>Comprehensive SR</i>.
     * @param sopInstanceUID  the SOP instance UID.
     * @param instanceNumber  the instance number.
     * @param obsDateTime  the date time of the observation.
     * @param template  the srom Template object to use with this SR.
     * @param title  the SR title as srom Code object.
     * @param separate  whether the document content should the
     *                  interpreted as continuous text or separate sections.
     *
     * @return  a new <code>srom</code> <i>Structured Report</i> document.
     */
    public abstract SRDocument newSRDocument(Patient patient, Study study,
            Series series, Equipment equipment, String sopClassUID,
            String sopInstanceUID, int instanceNumber, Date obsDateTime,
            Template template, Code title, boolean separate);
    
    public abstract Patient newPatient(Dataset ds) throws DcmValueException;
    
    public abstract Study newStudy(Dataset ds) throws DcmValueException;
    
    public abstract Series newSeries(Dataset ds) throws DcmValueException;
    
    public abstract Equipment newEquipment(Dataset ds) throws DcmValueException;
    
    public abstract Code newCode(Dataset ds);

    /**
     * Creates a <code>Code</code> object from the <code>String</code>
     * representation.
     * <p>
     * The format of the String representation must match:
     * <blockquote><pre>
     * (&lt;code-value&gt;, &lt;coding-scheme-designator&gt;, "&lt;coding-meaning&gt;")
     * </pre></blockquote>
     * or with specifying also a coding scheme version:
     * <blockquote><pre>
     * (&lt;code-value&gt;, &lt;coding-scheme-designator&gt; [&lt;coding-scheme-version&gt;], "&lt;coding-meaning&gt;")
     * </pre></blockquote>
     * @param spec
     *            the <code>String</code> to parse as a Code.
     * @return <code>Code</code> object of the <code>String</code>
     *         representation
     */
    public abstract Code newCode(String spec);

    public abstract RefSOP newRefSOP(Dataset ds) throws DcmValueException;
    
    public abstract IconImage newIconImage(Dataset ds) throws DcmValueException;
    
    public abstract Template newTemplate(Dataset ds) throws DcmValueException;
    
    public abstract SRDocument newSRDocument(Dataset ds) throws DcmValueException;
    
    public abstract KeyObject newKeyObject(Dataset ds) throws DcmValueException;
    
    public abstract HL7SRExport newHL7SRExport(
        String sendingApplication, String sendingFacility,
        String receivingApplication, String receivingFacility);
    
}//end interface SRDocumentFactory
