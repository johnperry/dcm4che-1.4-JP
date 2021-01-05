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

package org.dcm4che.hl7;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-05 18:26:16 +0200 (Mi, 05 Okt 2005) $
 * @since September 21, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class HL7 {
    
    // Constants -----------------------------------------------------
    // MSH Field
    public static final int MSHFieldSeparator = 1;
    public static final int MSHEncodingCharacters = 2;
    public static final int MSHSendingApplication = 3;
    public static final int MSHSendingFacility = 4;
    public static final int MSHReceivingApplication = 5;
    public static final int MSHReceivingFacility = 6;
    public static final int MSHDateTimeOfMessage = 7;
    public static final int MSHSecurity = 8;
    public static final int MSHMessageType = 9;
    public static final int MSHMessageControlID = 10;
    public static final int MSHProcessingID = 11;
    public static final int MSHVersionID = 12;
    public static final int MSHSequenceNumber = 13;
    public static final int MSHContinuationPointer = 14;
    public static final int MSHAcceptAcknowledgmentType = 15;
    public static final int MSHApplicationAcknowledgmentType = 16;
    public static final int MSHCountryCode = 17;
    public static final int MSHCharacterSet = 18;
    public static final int MSHPrincipalLanguageOfMessage = 19;

    // MSA Segment
    public static final int MSAAcknowledgmentCode = 1;
    public static final int MSAMessageControlIDToBeingRespondedTo = 2;
    public static final int MSATextMessage = 3;
    public static final int MSAExpectedSequenceNumber = 4;
    public static final int MSADelayedAcknowledgmentType = 5;
    public static final int MSAErrorCondition = 6;

    // ERR Segment
    public static final int ERRErrorCodeAndLocation = 1;

    // EVN Segment
    public static final int EVNEventTypeCode = 1;
    public static final int EVNRecordedDateTime = 2;
    public static final int EVNDateTimePlannedEvent = 3;
    public static final int EVNEventReasonCode = 4;
    public static final int EVNOperatorID = 5;
    public static final int EVNEventOccurred = 6;

    // PID Segment
    public static final int PIDSetID = 1;
    public static final int PIDPatientIDExternalID = 2;
    public static final int PIDPatientIDInternalID = 3;
    public static final int PIDAlternatePatientID = 4;
    public static final int PIDPatientName = 5;
    public static final int PIDMotherMaidenName = 6;
    public static final int PIDDateTimeOfBirth = 7;
    public static final int PIDSex = 8;
    public static final int PIDPatientAlias = 9;
    public static final int PIDRace = 10;
    public static final int PIDPatientAddress = 11;
    public static final int PIDCountyCode = 12;
    public static final int PIDPhoneNumberHome = 13;
    public static final int PIDPhoneNumberBusiness = 14;
    public static final int PIDPrimaryLanguage = 15;
    public static final int PIDMaritalStatus = 16;
    public static final int PIDReligion = 17;
    public static final int PIDPatientAccountNumber = 18;
    public static final int PIDSSNNumberPatient = 19;
    public static final int PIDDriverLicenseNumber = 20;
    public static final int PIDMotherIdentifier = 21;
    public static final int PIDEthnicGroup = 22;
    public static final int PIDBirthPlace = 23;
    public static final int PIDMultipleBirthIndicator = 24;
    public static final int PIDBirthOrder = 25;
    public static final int PIDCitizenship = 26;
    public static final int PIDVeteransMilitaryStatus = 27;
    public static final int PIDNationality = 28;
    public static final int PIDPatientDeathDateAndTime = 29;
    public static final int PIDPatientDeathIndicator = 30;

    // PV1 Segment
    public static final int PV1SetID = 1;
    public static final int PV1PatientClass = 2;
    public static final int PV1AssignedPatientLocation = 3;
    public static final int PV1AdmissionType = 4;
    public static final int PV1PreadmitNumber = 5;
    public static final int PV1PriorPatientLocation = 6;
    public static final int PV1AttendingDoctor = 7;
    public static final int PV1ReferringDoctor = 8;
    public static final int PV1ConsultingDoctor = 9;
    public static final int PV1HospitalService = 10;
    public static final int PV1TemporaryLocation = 11;
    public static final int PV1PreadmitTestIndicator = 12;
    public static final int PV1ReadmissionIndicator = 13;
    public static final int PV1AdmitSource = 14;
    public static final int PV1AmbulatoryStatus = 15;
    public static final int PV1VIPIndicator = 16;
    public static final int PV1AdmittingDoctor = 17;
    public static final int PV1PatientType = 18;
    public static final int PV1VisitNumber = 19;
    public static final int PV1FinancialClass = 20;
    public static final int PV1ChargePriceIndicator = 21;
    public static final int PV1CourtesyCode = 22;
    public static final int PV1CreditRating = 23;
    public static final int PV1ContractCode = 24;
    public static final int PV1ContractEffectiveDate = 25;
    public static final int PV1ContractAmount = 26;
    public static final int PV1ContractPeriod = 27;
    public static final int PV1InterestCode = 28;
    public static final int PV1TransferToBadDebtCode = 29;
    public static final int PV1TransferToBadDebtDate = 30;
    public static final int PV1BadDebtAgencyCode = 31;
    public static final int PV1BadDebtTransferAmount = 32;
    public static final int PV1BadDebtRecoveryAmount = 33;
    public static final int PV1DeleteAccountIndicator = 34;
    public static final int PV1DeleteAccountDate = 35;
    public static final int PV1DischargeDisposition = 36;
    public static final int PV1DischargedToLocation = 37;
    public static final int PV1DietType = 38;
    public static final int PV1ServicingFacility = 39;
    public static final int PV1BedStatus = 40;
    public static final int PV1AccountStatus = 41;
    public static final int PV1PendingLocation = 42;
    public static final int PV1PriorTemporaryLocation = 43;
    public static final int PV1AdmitDateTime = 44;
    public static final int PV1DischargeDateTime = 45;
    public static final int PV1CurrentPatientBalance = 46;
    public static final int PV1TotalCharges = 47;
    public static final int PV1TotalAdjustments = 48;
    public static final int PV1TotalPayments = 49;
    public static final int PV1AlternateVisitID = 50;
    public static final int PV1VisitIndicator = 51;
    public static final int PV1OtherHealthcareProvider = 52;

    // AL1 Segment
    public static final int AL1SetID = 1;
    public static final int AL1AllergyType = 2;
    public static final int AL1AllergyCodeMnemonicDescription = 3;
    public static final int AL1AllergySeverity = 4;
    public static final int AL1AllergyReaction = 5;
    public static final int AL1IdentificationDate = 6;

    // MRG Segment
    public static final int MRGSetID = 1;
    public static final int MRGPriorPatientIDInternal = 1;
    public static final int MRGPriorAlternatePatientID = 2;
    public static final int MRGPriorPatientAccountNumber = 3;
    public static final int MRGPriorPatientIDExternal = 4;
    public static final int MRGPriorVisitNumber = 5;
    public static final int MRGPriorAlternateVisitID = 6;
    public static final int MRGPriorPatientName = 7;

    // ORC Segment
    public static final int ORCOrderControl = 1;
    public static final int ORCPlacerOrderNumber = 2;
    public static final int ORCFillerOrderNumber = 3;
    public static final int ORCPlacerGroupNumber = 4;
    public static final int ORCOrderStatus = 5;
    public static final int ORCResponseFlag = 6;
    public static final int ORCQuantityTiming = 7;
    public static final int ORCParent = 8;
    public static final int ORCDateTimeOfTransaction = 9;
    public static final int ORCEnteredBy = 10;
    public static final int ORCVerifiedBy = 11;
    public static final int ORCOrderingProvider = 12;
    public static final int ORCEntererLocation = 13;
    public static final int ORCCallBackPhoneNumber = 14;
    public static final int ORCOrderEffectiveDateTime = 15;
    public static final int ORCOrderControlCodeReason = 16;
    public static final int ORCEnteringOrganization = 17;
    public static final int ORCEnteringDevice = 18;
    public static final int ORCActionBy = 19;
    
    // OBR Segment
    public static final int OBRSetID = 1;
    public static final int OBRPlacerOrderNumber = 2;
    public static final int OBRFillerOrderNumber = 3;
    public static final int OBRUniversalServiceID = 4;
    public static final int OBRPriority = 5;
    public static final int OBRRequestedDateTime = 6;
    public static final int OBRObservationDateTime = 7;
    public static final int OBRObservationEndDateTime = 8;
    public static final int OBRCollectionVolume = 9;
    public static final int OBRCollectorIdentifier = 10;
    public static final int OBRSpecimenActionCode = 11;
    public static final int OBRDangerCode = 12;
    public static final int OBRRelevantClinicalInfo = 13;
    public static final int OBRSpecimenReceivedDateTime = 14;
    public static final int OBRSpecimenSource = 15;
    public static final int OBROrderingProvider = 16;
    public static final int OBROrderCallbackPhoneNumber = 17;
    public static final int OBRPlacerfield1 = 18;
    public static final int OBRPlacerfield2 = 19;
    public static final int OBRFillerField1 = 20;
    public static final int OBRFillerField2 = 21;
    public static final int OBRResultsRptStatusChngDateTime = 22;
    public static final int OBRChargeToPractice = 23;
    public static final int OBRDiagnosticServSectID = 24;
    public static final int OBRResultStatus = 25;
    public static final int OBRParentResult = 26;
    public static final int OBRQuantityTiming = 27;
    public static final int OBRResultCopiesTo = 28;
    public static final int OBRParent = 29;
    public static final int OBRTransportationMode = 30;
    public static final int OBRReasonForStudy = 31;
    public static final int OBRPrincipalResultInterpreter = 32;
    public static final int OBRAssistantResultInterpreter = 33;
    public static final int OBRTechnician = 34;
    public static final int OBRTranscriptionist = 35;
    public static final int OBRScheduledDateTime = 36;
    public static final int OBRNumberofSampleContainers = 37;
    public static final int OBRTransportLogisticsOfCollectedSample = 38;
    public static final int OBRCollectorComment = 39;
    public static final int OBRTransportArrangementResponsibility = 40;
    public static final int OBRTransportArranged = 41;
    public static final int OBREscortRequired = 42;
    public static final int OBRPlannedPatientTransportComment = 43;
    public static final int OBRProcedureCode = 44;

    // OBX Segment
    public static final int OBXSetID = 1;
    public static final int OBXValueType = 2;
    public static final int OBXObservationIdentifier = 3;
    public static final int OBXObservationSubID = 4;
    public static final int OBXObservationValue = 5;
    public static final int OBXUnits = 6;
    public static final int OBXReferencesRange = 7;
    public static final int OBXAbnormalFlags = 8;
    public static final int OBXProbability = 9;
    public static final int OBXNatureOfAbnormalTest = 10;
    public static final int OBXObservResultStatus = 11;
    public static final int OBXDateLastObsNormalValues = 12;
    public static final int OBXUserDefinedAccessChecks = 13;
    public static final int OBXDateTimeOfTheObservation = 14;
    public static final int OBXProducerID = 15;
    public static final int OBXResponsibleObserver = 16;
    public static final int OBXObservationMethod = 17;

    // Variables -----------------------------------------------------
    
    // Constructors --------------------------------------------------
    private HL7() {
    }

    // Methods -------------------------------------------------------
    public static void check(HL7Segment seg, int[] required)
    throws HL7Exception {
        for (int i = 0; i < required.length; ++i) {
            if (seg.get(required[i]).length() == 0) {
                throw new HL7Exception.AR("Missing " + seg.id() + '-' + i);
            }
        }
    }    
    
    public static String xpnToDcmPN(HL7Segment seg, int seq) {
        return toDcmPN(seg, seq, 1);
    }
    public static String xcnToDcmPN(HL7Segment seg, int seq) {
        return toDcmPN(seg, seq, 2);
    }

    public static String toDcmPN(HL7Segment seg, int seq, int cmp1) {
        return seg.get(seq).length() == 0 ? "" :
            trimPN(
                seg.get(seq,1,cmp1) + '^'
              + seg.get(seq,1,cmp1+1) + '^'
              + seg.get(seq,1,cmp1+2) + '^' 
              + seg.get(seq,1,cmp1+4) + '^'
              + seg.get(seq,1,cmp1+3));
    }

    private static String trimPN(String pn) {
        int end = pn.length() - 1;
        while (end > 0 && pn.charAt(end) == '^')
            --end;
            
        return pn.substring(0, end+1);
    }
}
