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
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import java.util.Date;
/**
 * @author gunter.zeilinger@tiani.com
 * @version 1.0
 */
abstract class ContentImpl implements org.dcm4che.srom.Content {
	// Constants -----------------------------------------------------
	static final DcmObjectFactory dsfact = DcmObjectFactory.getInstance();
	// Attributes ----------------------------------------------------
	protected KeyObjectImpl owner;
	protected RelationType relation;
	protected ContentImpl parent;
	protected ContentImpl firstChild;
	protected ContentImpl lastChild;
	protected ContentImpl next;
	protected ContentImpl prev;
	// Constructors --------------------------------------------------
	ContentImpl(KeyObject owner) {
		this.owner = (KeyObjectImpl) owner;
	}
	static ContentImpl newContent(KeyObject owner, Dataset ds)
			throws DcmValueException {
		int[] refContentId = ds.getInts(Tags.RefContentItemIdentifier);
		if (refContentId != null) {
			return new ReferencedContentImpl(owner, refContentId);
		}
		ContentImpl retval = newContent(owner, ds, ds
				.getDate(Tags.ObservationDateTime), TemplateImpl.newTemplate(ds
				.getItem(Tags.ContentTemplateSeq)), CodeImpl.newCode(ds
				.getItem(Tags.ConceptNameCodeSeq)), ds
				.getString(Tags.ValueType));
		retval.initChilds(owner, ds);
		return retval;
	}
	static ContentImpl newContent(KeyObject owner, Dataset ds,
			Date obsDateTime, Template template, Code name, String type)
			throws DcmValueException {
		if ("CONTAINER".equals(type)) {
			return new ContainerContentImpl(owner, obsDateTime, template, name,
					"SEPARATE".equals(ds.getString(Tags.ContinuityOfContent)));
		}
		if ("TEXT".equals(type)) {
			return new TextContentImpl(owner, obsDateTime, template, name, ds
					.getString(Tags.TextValue));
		}
		if ("NUM".equals(type)) {
			Dataset mv = ds.getItem(Tags.MeasuredValueSeq);
			Dataset mq = ds.getItem(Tags.NumericValueQualifierCodeSeq);
			return new NumContentImpl(owner, obsDateTime, template, name,
					mv != null ? mv.getFloat(Tags.NumericValue) : null,
					mv != null ? CodeImpl.newCode(mv
							.getItem(Tags.MeasurementUnitsCodeSeq)) : null,
					CodeImpl.newCode(ds
							.getItem(Tags.NumericValueQualifierCodeSeq)));
		}
		if ("CODE".equals(type)) {
			return new CodeContentImpl(owner, obsDateTime, template, name,
					CodeImpl.newCode(ds.getItem(Tags.ConceptCodeSeq)));
		}
		if ("DATETIME".equals(type)) {
			return new DateTimeContentImpl(owner, obsDateTime, template, name,
					ds.getDate(Tags.DateTime));
		}
		if ("DATE".equals(type)) {
			return new DateContentImpl(owner, obsDateTime, template, name, ds
					.getDate(Tags.Date));
		}
		if ("TIME".equals(type)) {
			return new TimeContentImpl(owner, obsDateTime, template, name, ds
					.getDate(Tags.Time));
		}
		if ("UIDREF".equals(type)) {
			return new UIDRefContentImpl(owner, obsDateTime, template, name, ds
					.getString(Tags.UID));
		}
		if ("PNAME".equals(type)) {
			return new PNameContentImpl(owner, obsDateTime, template, name, ds
					.getString(Tags.PersonName));
		}
		if ("COMPOSITE".equals(type)) {
			return new CompositeContentImpl(owner, obsDateTime, template, name,
					RefSOPImpl.newRefSOP(ds.getItem(Tags.RefSOPSeq)));
		}
		if ("IMAGE".equals(type)) {
			Dataset sop = ds.getItem(Tags.RefSOPSeq);
			Dataset pr = sop.getItem(Tags.RefSOPSeq);
			Dataset icon = sop.getItem(Tags.IconImageSeq);
			return new ImageContentImpl(owner, obsDateTime, template, name,
					RefSOPImpl.newRefSOP(sop),
					sop.getInts(Tags.RefFrameNumber), RefSOPImpl.newRefSOP(pr),
					IconImageImpl.newIconImage(icon));
		}
		if ("WAVEFORM".equals(type)) {
			Dataset sop = ds.getItem(Tags.RefSOPSeq);
			return new WaveformContentImpl(owner, obsDateTime, template, name,
					RefSOPImpl.newRefSOP(sop), sop
							.getInts(Tags.RefWaveformChannels));
		}
		if ("SCOORD".equals(type)) {
			String graphicType = ds.getString(Tags.GraphicType);
			if ("POINT".equals(graphicType)) {
				return new SCoordContentImpl.Point(owner, obsDateTime,
						template, name, ds.getFloats(Tags.GraphicData));
			}
			if ("MULTIPOINT".equals(graphicType)) {
				return new SCoordContentImpl.MultiPoint(owner, obsDateTime,
						template, name, ds.getFloats(Tags.GraphicData));
			}
			if ("POLYLINE".equals(graphicType)) {
				return new SCoordContentImpl.Polyline(owner, obsDateTime,
						template, name, ds.getFloats(Tags.GraphicData));
			}
			if ("CIRCLE".equals(graphicType)) {
				return new SCoordContentImpl.Circle(owner, obsDateTime,
						template, name, ds.getFloats(Tags.GraphicData));
			}
			if ("ELLIPSE".equals(graphicType)) {
				return new SCoordContentImpl.Ellipse(owner, obsDateTime,
						template, name, ds.getFloats(Tags.GraphicData));
			}
			throw new IllegalArgumentException(graphicType);
		}
		if ("TCOORD".equals(type)) {
			String rangeType = ds.getString(Tags.TemporalRangeType);
			TCoordContent.Positions pos = TCoordContentImpl.newPositions(ds);
			if ("POINT".equals(rangeType)) {
				return new TCoordContentImpl.Point(owner, obsDateTime,
						template, name, pos);
			}
			if ("MULTIPOINT".equals(rangeType)) {
				return new TCoordContentImpl.MultiPoint(owner, obsDateTime,
						template, name, pos);
			}
			if ("SEGMENT".equals(rangeType)) {
				return new TCoordContentImpl.Segment(owner, obsDateTime,
						template, name, pos);
			}
			if ("MULTISEGMENT".equals(rangeType)) {
				return new TCoordContentImpl.MultiSegment(owner, obsDateTime,
						template, name, pos);
			}
			if ("BEGIN".equals(rangeType)) {
				return new TCoordContentImpl.Begin(owner, obsDateTime,
						template, name, pos);
			}
			if ("END".equals(rangeType)) {
				return new TCoordContentImpl.End(owner, obsDateTime, template,
						name, pos);
			}
			throw new IllegalArgumentException(rangeType);
		}
		throw new IllegalArgumentException(type);
	} // Methodes ------------------------------------------------------
	StringBuffer prompt() {
		StringBuffer sb = appendID(new StringBuffer(), getID());
		if (relation != null)
			sb.append(relation.toString().toLowerCase()).append(' ');
		return sb;
	}
	static StringBuffer appendID(StringBuffer sb, int[] id) {
		if (id == null)
			return sb.append("(null)");
		sb.append('(').append(id[0]);
		for (int i = 1; i < id.length; ++i) {
			sb.append('.').append(id[i]);
		}
		return sb.append(')');
	}
	static String promptID(int[] id) {
		return appendID(new StringBuffer(), id).toString();
	}
	public int[] getID() {
		int n = 1;
		ContentImpl r = this;
		for (ContentImpl p = parent; p != null; r = p, p = p.parent) {
			++n;
		}
		if (r != owner)
			return null;
		int[] id = new int[n];
		for (ContentImpl p = parent, c = this; p != null; c = p, p = p.parent) {
			id[--n] = 1;
			for (ContentImpl cur = p.firstChild; cur != c; cur = cur.next) {
				++id[n];
			}
		}
		id[0] = 1;
		return id;
	}
	public KeyObject getOwnerDocument() {
		return owner;
	}
	public RelationType getRelationType() {
		return relation;
	}
	public Content getParent() {
		return parent;
	}
	public boolean hasChildren() {
		return (getFirstChild() != null);
	}
	public Content getFirstChild() {
		return firstChild;
	}
	private static Content find(RelationType rel, Code name, Content start) {
		for (Content cur = start; cur != null; cur = cur.getNextSibling()) {
			if ((rel == null || rel == cur.getRelationType())
					&& (name == null || name.equals(cur.getName())))
				return cur;
		}
		return null;
	}
	public Content getFirstChildBy(RelationType rel, Code name) {
		return find(rel, name, getFirstChild());
	}
	public Content getLastChild() {
		return lastChild;
	}
	public Content getPreviousSibling() {
		return prev;
	}
	public Content getNextSibling() {
		return next;
	}
	public Content getNextSiblingBy(RelationType rel, Code name) {
		return find(rel, name, next);
	}
	public Content appendChild(RelationType rel, Content newnode) {
		return insertBefore(rel, newnode, null);
	}
	public Content insertBefore(RelationType rel, Content newnode,
			Content refnode) {
		if (rel == null) {
			throw new NullPointerException();
		}
		if (newnode == null) {
			throw new NullPointerException();
		}
		ContentImpl newNode = (ContentImpl) newnode;
		ContentImpl refNode = (ContentImpl) refnode;
		if (newNode == owner) {
			throw new IllegalArgumentException();
		}
		if (newNode.owner != owner) {
			throw new IllegalArgumentException();
		}
		if (newNode.parent != null) {
			throw new IllegalArgumentException();
		}
		if (refnode != null && refNode.parent != this) {
			throw new IllegalArgumentException();
		}
		if (owner.getRelationConstraints() != null) {
			owner.getRelationConstraints().check(this, rel, newnode);
		}
		newNode.parent = this;
		newNode.relation = rel;
		newNode.next = refNode;
		if (refNode == null) {
			newNode.prev = lastChild;
			lastChild = newNode;
		} else {
			newNode.prev = refNode.prev;
			refNode.prev = newNode;
		}
		if (newNode.prev == null) {
			firstChild = newNode;
		} else {
			newNode.prev.next = newNode;
		}
		return newnode;
	}//end insertBefore()
	public Content replaceChild(RelationType rel, Content newnode,
			Content oldnode) {
		if (oldnode == null) {
			throw new NullPointerException();
		}
		if (newnode == oldnode) {
			return newnode;
		}
		insertBefore(rel, newnode, oldnode);
		return removeChild(oldnode);
	}
	public Content removeChild(Content oldnode) {
		ContentImpl oldNode = (ContentImpl) oldnode;
		if (oldNode == null) {
			throw new NullPointerException();
		}
		if (oldNode.parent != this) {
			throw new IllegalArgumentException("" + oldnode
					+ " is NOT a child of " + this);
		}
		if (oldNode.prev != null) {
			oldNode.prev.next = oldNode.next;
		} else {
			firstChild = oldNode.next;
		}
		if (oldNode.next != null) {
			oldNode.next.prev = oldNode.prev;
		} else {
			lastChild = oldNode.prev;
		}
		oldNode.parent = oldNode.prev = oldNode.next = null;
		return oldnode;
	}
	public Content clone(boolean deep) {
		return clone(owner, deep, true);
	}
	Content clone(KeyObject newOwner, boolean deep, boolean inheritObsDateTime) {
		Content clone = clone(newOwner, inheritObsDateTime);
		if (deep) {
			for (ContentImpl child = firstChild; child != null; child = child.next) {
				clone.appendChild(child.relation, child.clone(newOwner, true,
						false));
			}
		}
		return clone;
	}
	abstract Content clone(KeyObject newOwner, boolean inheritObsDateTime);
	public void toDataset(Dataset ds) {
		if (relation != null) { // root content has no relation!
			ds.putCS(Tags.RelationshipType, relation.toString());
		}
		ds.putCS(Tags.ValueType, getValueType().toString());
		DcmElement cnSq = ds.putSQ(Tags.ConceptNameCodeSeq);
		Code name = getName();
		if (name != null) {
			name.toDataset(cnSq.addNewItem());
		}
		Template tpl = getTemplate();
		if (tpl != null) {
			tpl.toDataset(ds.putSQ(Tags.ContentTemplateSeq).addNewItem());
		}
		Date obsDateTime = getObservationDateTime(false);
		if (obsDateTime != null) {
			ds.putDT(Tags.ObservationDateTime, obsDateTime);
		}
		if (firstChild != null) {
			DcmElement sq = ds.putSQ(Tags.ContentSeq);
			for (Content child = firstChild; child != null; child = child
					.getNextSibling()) {
				child.toDataset(sq.addNewItem());
			}
		}
	}
	protected void initChilds(KeyObject owner, Dataset ds)
			throws DcmValueException {
		DcmElement sq = ds.get(Tags.ContentSeq);
		if (sq == null) {
			return;
		}
		for (int i = 0, n = sq.countItems(); i < n; ++i) {
			Dataset child = sq.getItem(i);
			appendChild(Content.RelationType.valueOf(child
					.getString((Tags.RelationshipType))), newContent(owner,
					child));
		}
	}
}
