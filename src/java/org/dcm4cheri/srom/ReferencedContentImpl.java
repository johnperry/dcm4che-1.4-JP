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

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

import org.dcm4che.srom.*;
import java.util.Date;
import java.util.NoSuchElementException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class ReferencedContentImpl extends ContentImpl
        implements org.dcm4che.srom.ReferencedContent {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    protected Content refContent;
    protected int[] refContentID;

    // Constructors --------------------------------------------------
    ReferencedContentImpl(KeyObject owner, Content refContent) {
        super(owner);
        if (refContent.getOwnerDocument() != owner)
            throw new IllegalArgumentException();

        if (refContent.getParent() == null)
            throw new IllegalArgumentException();
        
        if (refContent instanceof ReferencedContent)
            throw new IllegalArgumentException();
        
        this.refContent = refContent;
    }
    
    ReferencedContentImpl(KeyObject owner, int[] refContentID) {
        super(owner);
        this.refContentID = refContentID;
//        this.refContent = owner.getContent(this.refContentID = refContentID);
    }

    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        if (newOwner != owner)
            throw new IllegalArgumentException("" + newOwner);
        
        return new ReferencedContentImpl(newOwner, getRefContent().getID());
    }

    // Methodes ------------------------------------------------------
    public String toString() {
        StringBuffer sb = prompt().append(refContent != null ? "=>" : "->");
        appendID(sb, refContent != null ? refContent.getID() : refContentID);
        return sb.toString();
    }
    
    public void toDataset(Dataset ds) {
        ds.putCS(Tags.RelationshipType, relation.toString());
        ds.putUL(Tags.RefContentItemIdentifier, getRefContent().getID());
    }    
    
    public Content getRefContent() {
        if (refContent != null)
            return refContent;

        refContent = owner.getContent(refContentID);
        if (refContent == null)
            throw new NoSuchElementException(ContentImpl.promptID(refContentID));

        return refContent;
    }

    public Content insertBefore(RelationType rel, Content newnode, Content refnode) {
        throw new UnsupportedOperationException();
    }
    
    public ValueType getValueType() {
        return getRefContent().getValueType();
    }
/*    
    public Content getFirstChild() {
        return getRefContent().getFirstChild();
    }

    public Content getLastChild() {
        return getRefContent().getLastChild();
    }
  */  
    public Date getObservationDateTime(boolean inherit) {
        return getRefContent().getObservationDateTime(inherit);
    }

    public Code getName() {
        return getRefContent().getName();
    }

    public void setName(Code newName) {
        throw new UnsupportedOperationException();
    }

    public Template getTemplate() {
        return getRefContent().getTemplate();
    }
}
