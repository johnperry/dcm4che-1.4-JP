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

package org.dcm4cheri.srom;

import org.dcm4che.srom.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class KeyObjectImpl extends ContainerContentImpl implements KeyObject {
    // Constants -----------------------------------------------------
    // Attributes ----------------------------------------------------
    protected Patient patient;
    protected Study study;
    protected Series series;
    protected Equipment equipment ;
    protected final String modality;
    
    protected final String sopClassUID;
    protected String sopInstanceUID;
    protected String specificCharacterSet;
    protected Long instanceCreationDateTime;
    protected String instanceCreatorUID;    
    
    protected int instanceNumber = 1;
    protected long contentDateTime = Calendar.getInstance().getTime().getTime();

    protected final List requests = new LinkedList();
    protected final List currentEvidence = new LinkedList();
    protected final List identicalDocuments = new LinkedList();

    protected final RelationConstraints relConstraints;

    // Constructors --------------------------------------------------
    KeyObjectImpl(Patient patient, Study study, Series series,
            Equipment equipment, String sopInstanceUID, int instanceNumber,
            Date obsDateTime, Code title, boolean separate) {
        this(patient, study, series, equipment,
                UIDs.KeyObjectSelectionDocument, sopInstanceUID, instanceNumber,
                obsDateTime, TemplateImpl.TID_2010, title, separate, "KO");
    }
    
    protected KeyObjectImpl(Patient patient, Study study, Series series,
            Equipment equipment, String sopClassUID, String sopInstanceUID,
            int instanceNumber, Date obsDateTime, Template template,
            Code title, boolean separate, String modality) {
        super(null, obsDateTime, template, title, separate);
        super.owner = this;
        if (sopClassUID.length() == 0)
            throw new IllegalArgumentException();
        if (sopInstanceUID.length() == 0)
            throw new IllegalArgumentException();
        if (title == null)
            throw new NullPointerException();
        this.sopClassUID = sopClassUID;
        this.sopInstanceUID = sopInstanceUID;
        this.instanceNumber = instanceNumber;
        this.modality = modality;
        this.relConstraints = RelationConstraintsImpl.valueOf(sopClassUID);
        setPatient(patient);
        setStudy(study);
        setSeries(series);
        setEquipment(equipment);
    }

    static KeyObject newKeyObject(Dataset ds) throws DcmValueException {
        String sopClassUID = ds.getString(Tags.SOPClassUID);
        if (!UIDs.KeyObjectSelectionDocument.equals(sopClassUID))
            throw new IllegalArgumentException(sopClassUID);

        Template template = new TemplateImpl(
                ds.getItem(Tags.ContentTemplateSeq));
        if (!TemplateImpl.TID_2010.equals(template))
            throw new IllegalArgumentException(template.toString());
        
        if (!"CONTAINER".equals(ds.getString(Tags.ValueType))) {
            throw new IllegalArgumentException(ds.getString(Tags.ValueType));
        }
        KeyObjectImpl ko = new KeyObjectImpl(new PatientImpl(ds),
                new StudyImpl(ds),  new SeriesImpl(ds),  new EquipmentImpl(ds),
                ds.getString(Tags.SOPInstanceUID),
                ds.getInt(Tags.InstanceNumber, -1),
                ds.getDate(Tags.ObservationDateTime),
                new CodeImpl(ds.getItem(Tags.ConceptNameCodeSeq)),
                "SEPARATE".equals(ds.getString(Tags.ContinuityOfContent)));
        ko.init(ds);
        return ko;
    }

    protected void init(Dataset ds) throws DcmValueException {
        setSpecificCharacterSet(ds.getString(Tags.SpecificCharacterSet));
        setInstanceCreationDateTime(ds.getDateTime(Tags.InstanceCreationDate,
                Tags.InstanceCreationTime));
        setContentDateTime(ds.getDateTime(Tags.ContentDate, Tags.ContentTime));
        initRequests(ds.get(Tags.RefRequestSeq));
        initSOPInstanceRefList(this.currentEvidence,
                ds.get(Tags.CurrentRequestedProcedureEvidenceSeq));
        initSOPInstanceRefList(this.identicalDocuments,
                ds.get(Tags.IdenticalDocumentsSeq));
        super.initChilds(this, ds);
    }

    protected void initRequests(DcmElement sq) throws DcmValueException {
        if (sq == null)
            return;
        
        for (int i = 0, n = sq.countItems(); i < n; ++i) {
            requests.add(new RequestImpl(sq.getItem(i)));
        }
    }
    
    protected void initSOPInstanceRefList(List list, DcmElement sq)
            throws DcmValueException {
        if (sq == null)
            return;
        
        for (int i = 0, n = sq.countItems(); i < n; ++i) {
            Dataset ds = sq.getItem(i);
            String studyInstanceUID = ds.getString(Tags.StudyInstanceUID);
            DcmElement sq2 = ds.get(Tags.RefSeriesSeq);
            for (int i2 = 0, n2 = sq2.countItems(); i2 < n2; ++i2) {
                Dataset ds2 = sq2.getItem(i2);
                String seriesInstanceUID = ds2.getString(Tags.SeriesInstanceUID);
                DcmElement sq3 = ds2.get(Tags.RefSOPSeq);
                if (sq3 != null) {
                    for (int i3 = 0, n3 = sq3.countItems(); i3 < n3; ++i3) {
                        Dataset ds3 = sq3.getItem(i3);
                        list.add(new SOPInstanceRefImpl(
                                ds3.getString(Tags.RefSOPClassUID),
                                ds3.getString(Tags.RefSOPInstanceUID),
                                seriesInstanceUID, studyInstanceUID));
                    }
                }
            }
        }
    }
    
    // Methodes ------------------------------------------------------
    public String toString() {
        return "Key Object Selection Document[" 
                        + getName().getCodeMeaning()
                 + "," + getSOPInstanceUID() 
                 + ",#" + getInstanceNumber()
                 + "," + getContentDateTime()
                 + "]";
    }

    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        if (patient == null)
            throw new NullPointerException();
        
        this.patient = patient;
    }
    
    public Study getStudy() {
        return study;
    }
    
    public void setStudy(Study study) {
        if (study == null)
            throw new NullPointerException();
        
        this.study = study;
    }
    
    public Series getSeries() {
        return series;
    }
    
    public void setSeries(Series series) {
        if (!modality.equals(series.getModality()))
            throw new IllegalArgumentException(series.getModality());
        
        this.series = series;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = new EquipmentImpl(equipment);
    }

    public RelationConstraints getRelationConstraints() {
        return relConstraints;
    }
    
    public String getSOPClassUID() {
        return sopClassUID;
    }
        
    public String getSOPInstanceUID() {
        return sopInstanceUID;
    }

    public void setSOPInstanceUID(String sopInstanceUID) {
        if (sopInstanceUID.length() == 0)
            throw new IllegalArgumentException();
        
        this.sopInstanceUID = sopInstanceUID;
    }

    public String getSpecificCharacterSet() {
        return specificCharacterSet;
    }
    
    public void setSpecificCharacterSet(String specificCharacterSet) {
        this.specificCharacterSet = specificCharacterSet;
    }

    public Date getInstanceCreationDateTime() {
        return instanceCreationDateTime != null
            ? new Date(instanceCreationDateTime.longValue()) : null;
    }        
    
    public void setInstanceCreationDateTime(Date instanceCreationDateTime) {
        this.instanceCreationDateTime = instanceCreationDateTime != null
            ? new Long(instanceCreationDateTime.getTime()) : null;
    }
    
    public String getInstanceCreatorUID() {
        return instanceCreatorUID;
    }

    public void setInstanceCreatorUID(String instanceCreatorUID) {
        this.instanceCreatorUID = instanceCreatorUID;
    }

    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public void setContentDateTime(Date contentDateTime) {
        this.contentDateTime = contentDateTime.getTime();
    }

    public Date getContentDateTime() {
        return new Date(contentDateTime);
    }

    
    protected static List checkList(Object[] a) {
        if (a == null)
            return Collections.EMPTY_LIST;
        List list = Arrays.asList(a);
        int i = list.indexOf(null);
        if (i != -1) {
            throw new NullPointerException("[" + i + "]");
        }
        return list;
    }
    
    protected static SOPInstanceRef findSOPInstanceRef(List list, RefSOP ref) {
        Object o;
        for (Iterator it = list.iterator(); it.hasNext();) {
            if (ref.equals(o = it.next()))
                return (SOPInstanceRef)o;
        }
        return null;
    }
    
    public Request[] getRequests() {
        return (Request[])requests.toArray(RequestImpl.EMPTY_ARRAY);
    }
    
    public void setRequests(Request[] newRequests) {
        List list = checkList(newRequests);
        requests.clear();
        requests.addAll(list);
    }
    
    public boolean addRequest(Request request) {
        if (request == null)
            throw new NullPointerException();
        
        if (requests.indexOf(request) != -1)
            return false;

        return requests.add(request);
    }
    
    public boolean removeRequest(Request request) {
        return requests.remove(request);
    }

    public SOPInstanceRef[] getCurrentEvidence() {
        return (SOPInstanceRef[])currentEvidence.toArray(
                SOPInstanceRefImpl.EMPTY_ARRAY);
    }

    public SOPInstanceRef findCurrentEvidence(RefSOP ref) {
        return findSOPInstanceRef(currentEvidence, ref);
    }

    public void setCurrentEvidence(SOPInstanceRef[] refs) {
        List list = checkList(refs);
        currentEvidence.clear();
        currentEvidence.addAll(list);
    }
    
    public boolean addCurrentEvidence(SOPInstanceRef ref) {
        if (ref == null)
            throw new NullPointerException();

        if (currentEvidence.indexOf(ref) != -1)
            return false;

        return currentEvidence.add(ref);
    }
    
    public boolean removeCurrentEvidence(RefSOP ref) {
        return currentEvidence.remove(ref);
    }

    public SOPInstanceRef[] getIdenticalDocuments() {
        return (SOPInstanceRef[])identicalDocuments.toArray(
                SOPInstanceRefImpl.EMPTY_ARRAY);
    }
    
    public void setIdenticalDocuments(SOPInstanceRef[] refs) {
        List list = checkList(refs);
        identicalDocuments.clear();
        identicalDocuments.addAll(list);
    }
    
    public boolean addIdenticalDocument(SOPInstanceRef ref) {
        if (ref == null)
            throw new NullPointerException();

        if (identicalDocuments.indexOf(ref) != -1)
            return false;

        return identicalDocuments.add(ref);
    }
    
    public boolean removeIdenticalDocument(RefSOP ref) {
        return identicalDocuments.remove(ref);
    }

    private static Content getChild(Content parent, int index) {
        if (index < 0)
            throw new IllegalArgumentException();
        int count = index;
        for (Content child = parent.getFirstChild(); child != null;
                    child = child.getNextSibling()) {
            if (--count == 0)
                return child;
        }
        return null;
    } 

    public Content getContent(int[] id) {
        if (id.length == 0 || id[0] != 1)
            throw new IllegalArgumentException();

        Content cur = this;
        for (int i = 1; i < id.length; ++i)
            cur = getChild(cur,id[i]);
        return cur;
    }
    
    public ContainerContent createContainerContent(
            Date obsDateTime, Template template, Code name, boolean separate) {
        return new ContainerContentImpl(this, obsDateTime, template, name,
                separate);
    }
    
    public TextContent createTextContent(
            Date obsDateTime, Template template, Code name, String text) {
        return new TextContentImpl(this, obsDateTime, template, name, text);
    }
    
    public PNameContent createPNameContent(
            Date obsDateTime, Template template, Code name, String pname) {
        return new PNameContentImpl(this, obsDateTime, template, name, pname);
    }
    
    public UIDRefContent createUIDRefContent(
            Date obsDateTime, Template template, Code name, String uid) {
        return new UIDRefContentImpl(this, obsDateTime, template, name, uid);
    }
    
    public CodeContent createCodeContent(
            Date obsDateTime, Template template, Code name, Code code) {
        return new CodeContentImpl(this, obsDateTime, template, name, code);
    }

    public NumContent createNumContent(
            Date obsDateTime, Template template, Code name,
            float value, Code unit) {
        return new NumContentImpl(this, obsDateTime, template, name,
                new Float(value), unit, null);
    }

    public NumContent createNumContent(
            Date obsDateTime, Template template, Code name,
            Float value, Code unit, Code qualifier) {
        return new NumContentImpl(this, obsDateTime, template, name,
        		value, unit, qualifier);
    }

    public DateContent createDateContent(
            Date obsDateTime, Template template, Code name, Date date) {
        return new DateContentImpl(this, obsDateTime, template, name, date);
    }
    
    public TimeContent createTimeContent(
            Date obsDateTime, Template template, Code name, Date time) {
        return new TimeContentImpl(this, obsDateTime, template, name, time);
    }
    
    public DateTimeContent createDateTimeContent(
            Date obsDateTime, Template template, Code name, Date dateTime) {
        return new DateTimeContentImpl(this, obsDateTime, template, name,
                dateTime);
    }
    
    public CompositeContent createCompositeContent(
            Date obsDateTime, Template template, Code name, RefSOP refSOP) {
        return new CompositeContentImpl(this, obsDateTime, template, name,
                refSOP);
    }

    public ImageContent createImageContent(Date obsDateTime, Template template,
            Code name, RefSOP refSOP, int[] frameNumbers,
            RefSOP refPresentationSOP, IconImage iconImage) {
        return new ImageContentImpl(this, obsDateTime, template, name,
                refSOP, frameNumbers, refPresentationSOP, iconImage);
    }

    public WaveformContent createWaveformContent(
            Date obsDateTime, Template template, Code name,
            RefSOP refSOP, int[] channelNumbers) {
        return new WaveformContentImpl(this, obsDateTime, template, name,
                refSOP, channelNumbers);
    }

    public SCoordContent.Point createPointSCoordContent(
            Date obsDateTime, Template template, Code name,
            float[] graphicData) {
        return new SCoordContentImpl.Point(this, obsDateTime, template, name,
                graphicData);
    }
    
    public SCoordContent.MultiPoint createMultiPointSCoordContent(
            Date obsDateTime, Template template, Code name,
            float[] graphicData) {
        return new SCoordContentImpl.MultiPoint(this, obsDateTime, template,
                name, graphicData);
    }
    
    public SCoordContent.Polyline createPolylineSCoordContent(
            Date obsDateTime, Template template, Code name,
            float[] graphicData) {
        return new SCoordContentImpl.Polyline(this, obsDateTime, template, name,
                graphicData);
    }
    
    public SCoordContent.Circle createCircleSCoordContent(
            Date obsDateTime, Template template, Code name,
            float[] graphicData) {
        return new SCoordContentImpl.Circle(this, obsDateTime, template, name,
                graphicData);
    }
    
    public SCoordContent.Ellipse createEllipseSCoordContent(
            Date obsDateTime, Template template, Code name,
            float[] graphicData) {
        return new SCoordContentImpl.Ellipse(this, obsDateTime, template, name,
                graphicData);
    }
    
    public TCoordContent.Point createPointTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions) {
        return new TCoordContentImpl.Point(this, obsDateTime, template, name,
                positions);
    }
    
    public TCoordContent.MultiPoint createMultiPointTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions) {
        return new TCoordContentImpl.MultiPoint(this, obsDateTime, template,
                name, positions);
    }
    
    public TCoordContent.Segment createSegmentTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions) {
        return new TCoordContentImpl.Segment(this, obsDateTime, template, name,
                positions);
    }
    
    public TCoordContent.MultiSegment createMultiSegmentTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions) {
        return new TCoordContentImpl.MultiSegment(this, obsDateTime, template,
                name, positions);
    }
    
    public TCoordContent.Begin createBeginTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions) {
        return new TCoordContentImpl.Begin(this, obsDateTime, template, name,
                positions);
    }
    
    public TCoordContent.End createEndTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions) {
        return new TCoordContentImpl.End(this, obsDateTime, template, name,
                positions);
    }
    
    public ReferencedContent createReferencedContent(Content refContent) {
        return new ReferencedContentImpl(this, refContent);
    }

    public ReferencedContent createReferencedContent(int[] refContentId) {
        return new ReferencedContentImpl(this, refContentId);
    }

    public Content importContent(Content content, boolean deep) {
        return ((ContentImpl)content).clone(this, deep, true);
    }
    
    protected static void sopInstanceRefListToSQ(List list, DcmElement sq) {
        HashMap studyMap = new HashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            SOPInstanceRef ref = (SOPInstanceRef)it.next();
            HashMap seriesMap = (HashMap)studyMap.get(ref.getStudyInstanceUID());
            if (seriesMap == null)
                studyMap.put(ref.getStudyInstanceUID(), seriesMap = new HashMap());
            
            List refSOPList = (List)seriesMap.get(ref.getSeriesInstanceUID());
            if (refSOPList == null) {
                seriesMap.put(ref.getSeriesInstanceUID(),
                        refSOPList = new LinkedList());
            }
            refSOPList.add(ref);
        }
        for (Iterator it = studyMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            Dataset study = sq.addNewItem();
            study.putUI(Tags.StudyInstanceUID, (String)entry.getKey());
            DcmElement refSeriesSeq = study.putSQ(Tags.RefSeriesSeq);
            for (Iterator it2 = ((Map)entry.getValue()).entrySet().iterator();
                    it2.hasNext();) {
                Map.Entry entry2 = (Map.Entry)it2.next();
                Dataset series = refSeriesSeq.addNewItem();
                series.putUI(Tags.SeriesInstanceUID, (String)entry2.getKey());
                DcmElement refSOPSeq = series.putSQ(Tags.RefSOPSeq);
                for (Iterator it3 = ((List)entry2.getValue()).iterator();
                        it3.hasNext();) {
                    ((SOPInstanceRef)it3.next()).toDataset(
                            refSOPSeq.addNewItem());
                }
            }
        }
    }
    
    public void toDataset(Dataset ds) {
        if (specificCharacterSet != null) {
            ds.putCS(Tags.SpecificCharacterSet, specificCharacterSet);
        }
        ds.putUI(Tags.SOPClassUID, sopClassUID);
        ds.putUI(Tags.SOPInstanceUID, sopInstanceUID);
        ds.putIS(Tags.InstanceNumber, instanceNumber);
        if (instanceCreationDateTime != null) {
            Date dt = new Date(instanceCreationDateTime.longValue());
            ds.putDA(Tags.InstanceCreationDate, dt);
            ds.putTM(Tags.InstanceCreationTime, dt);
        }
        Date dt = getContentDateTime();
        ds.putDA(Tags.ContentDate, dt);
        ds.putTM(Tags.ContentTime, dt);        
        
        patient.toDataset(ds);
        study.toDataset(ds);
        series.toDataset(ds);
        equipment.toDataset(ds);

        if (!requests.isEmpty()) {
            DcmElement sq = ds.putSQ(Tags.RefRequestSeq);
            for (Iterator it =requests.iterator(); it.hasNext();) {
                ((Request)it.next()).toDataset(sq.addNewItem());
            }
        }        
        if (!currentEvidence.isEmpty()) {
            sopInstanceRefListToSQ(currentEvidence,
                    ds.putSQ(Tags.CurrentRequestedProcedureEvidenceSeq));
        }
        if (!identicalDocuments.isEmpty()) {
            sopInstanceRefListToSQ(currentEvidence,
                    ds.putSQ(Tags.IdenticalDocumentsSeq));
        }
        super.toDataset(ds);
    }
    
    public Dataset toDataset() {
        Dataset ds = dsfact.newDataset();
        toDataset(ds);
        return ds;
    }
}
