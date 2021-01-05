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

import org.dcm4che.data.Dataset;

import java.util.Date;
import org.apache.log4j.Logger;

/** The <code>Content</code> interface is the primary datatype for the entire
 * SR Object Model. It represents a single <i>Content Item</i> in the
 * document tree.
 *
 * @author gunter.zeilinger@tiani.com
 * @version 0.9.9
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 */
public interface Content {
        
    static final Logger log = Logger.getLogger(Content.class);
    // Constants -----------------------------------------------------
    
    /** Enumeration of <i>Relationship Types</i> between the (enclosing)
     * <i>Source Content Item</i> and the <i>Target Content Item</i>.
     * Used by methods {@link #getRelationType getRelationType},
     * {@link #appendChild appendChild},
     * {@link #insertBefore insertBefore},
     * {@link #replaceChild replaceChild}.
     */
    public static final class RelationType {
        
        private String type;
        
        public static final RelationType CONTAINS =
                new RelationType("CONTAINS");
        
        public static final RelationType HAS_PROPERTIES =
                new RelationType("HAS PROPERTIES");
        
        public static final RelationType HAS_OBS_CONTEXT =
                new RelationType("HAS OBS CONTEXT");
        
        public static final RelationType HAS_ACQ_CONTEXT =
                new RelationType("HAS ACQ CONTEXT");
        
        public static final RelationType INFERRED_FROM =
                new RelationType("INFERRED FROM");
        
        public static final RelationType SELECTED_FROM =
                new RelationType("SELECTED FROM");
        
        public static final RelationType HAS_CONCEPT_MOD =
                new RelationType("HAS CONCEPT MOD");
        
        private static final java.util.Map TYPES = new java.util.HashMap(11);
        static {
            TYPES.put("CONTAINS",CONTAINS);
            TYPES.put("HAS PROPERTIES",HAS_PROPERTIES);
            TYPES.put("HAS OBS CONTEXT",HAS_OBS_CONTEXT);
            TYPES.put("HAS ACQ CONTEXT",HAS_ACQ_CONTEXT);
            TYPES.put("INFERRED FROM",INFERRED_FROM);
            TYPES.put("SELECTED FROM",SELECTED_FROM);
            TYPES.put("HAS CONCEPT MOD",HAS_CONCEPT_MOD);
        }
        
        /** Returns <code>RelationType</code> represented by the specified
         * code string.
         *
         * @param s specified code string.
         * @return <code>RelationType</code> represented by the specified
         * string.
         * @throws IllegalArgumentException if the string does not contain
         * a valid code value.
         */
        public static RelationType valueOf(String s) {
            if (s == null || s.length() == 0) {
                log.warn("Missing Relation Type - assume CONTAINS");
                s = "CONTAINS";
            }
            RelationType type = (RelationType)TYPES.get(s);
            if (type == null)
                throw new IllegalArgumentException(s);
            return type;
        }

        private RelationType(java.lang.String type) {
            this.type = type;
        }
        
        /** Returns <code>RelationType</code> represented by the specified
         * code string.
         *
         * @param s specified code string.
         * @return <code>RelationType</code> represented by the specified
         * string.
         * @throws IllegalArgumentException if the string does not contain
         * a valid code value.
         */
        public String toString() {
            return type;
        }        
    }
    
    /** Enumeration of <i>Value Types</i> of <i>Content Items</i>.
     * Used by {@link #getValueType getValueType()}.
     */
    public static final class ValueType {
        
        private String type;
        
        public static final ValueType TEXT =
                new ValueType("TEXT");
        
        public static final ValueType NUM =
                new ValueType("NUM");
        
        public static final ValueType CODE =
                new ValueType("CODE");
        
        public static final ValueType DATETIME =
                new ValueType("DATETIME");
        
        public static final ValueType DATE =
                new ValueType("DATE");
        
        public static final ValueType TIME =
                new ValueType("TIME");
        
        public static final ValueType UIDREF =
                new ValueType("UIDREF");
        
        public static final ValueType PNAME =
                new ValueType("PNAME");
        
        public static final ValueType COMPOSITE =
                new ValueType("COMPOSITE");
        
        public static final ValueType IMAGE =
                new ValueType("IMAGE");
        
        public static final ValueType WAVEFORM =
                new ValueType("WAVEFORM");
        
        public static final ValueType SCOORD =
                new ValueType("SCOORD");
        
        public static final ValueType TCOORD =
                new ValueType("TCOORD");
        
        public static final ValueType CONTAINER =
                new ValueType("CONTAINER");
        
        private static final java.util.Map TYPES = new java.util.HashMap(19);
        static {
            TYPES.put("TEXT",TEXT);
            TYPES.put("NUM",NUM);
            TYPES.put("CODE",CODE);
            TYPES.put("DATETIME",DATETIME);
            TYPES.put("DATE",DATE);
            TYPES.put("TIME",TIME);
            TYPES.put("UIDREF",UIDREF);
            TYPES.put("PNAME",PNAME);
            TYPES.put("COMPOSITE",COMPOSITE);
            TYPES.put("IMAGE",IMAGE);
            TYPES.put("WAVEFORM",WAVEFORM);
            TYPES.put("SCOORD",SCOORD);
            TYPES.put("TCOORD",TCOORD);
            TYPES.put("CONTAINER",CONTAINER);
        }
        
        /** Returns <code>ValueType</code> represented by the specified
         * code string.
         *
         * @param s specified code string.
         * @return code>ValueType</code> represented by the specified
         * string.
         * @throws IllegalArgumentException if the string does not contain
         * a valid code value.
         */
        public static ValueType valueOf(String str) {
            ValueType type = (ValueType)TYPES.get(str);
            if (type == null)
                throw new IllegalArgumentException(str);
            return type;
        }

        private ValueType(java.lang.String type) {
            this.type = type;
        }
        
        /** Returns code string for this <code>ValueType</code>.
         *
         * @return code string for this <code>ValueType</code>.
         */
        public String toString() {
            return type;
        }        
    }
       
    // Public --------------------------------------------------------

    /** Returns the <i>Content Item Identifier<</i> of this content, specifying
     * its position in the document tree. Returns <code>null</code>, if this
     * content is not inserted in the document tree yet.
     *
     * @return <i>Content Item Identifier</i>.
     */    
    public int[] getID();
    
    /** Returns the <i>Concept Name Code</i> of this content. Returns
     * <code>null</code>, if no name is associated with this content.
     *
     * @return <i>Concept Name Code</i> of this <i>Content Item</i>.
     */    
    public Code getName();
    
    /** Sets new <i>Concept Name Code</i> of this content.
     *
     * @param newName new <i>Concept Name Code</i> of this <i>Content Item</i>.
     */    
    public void setName(Code newName);
    
    /** Gets the <code>SRDocument</code> or <code>KeyObject</code> object
     * associated with this content.
     * This is also the <code>SRDocument</code> or <code>KeyObject</code> object
     * used to create this ontent.
     *
     * @return <code>SRDocument</code> or <code>KeyObject</code> object
     * associated with this content.
     */    
    public KeyObject getOwnerDocument();
    
    /** Gets the <i>Observation DateTime</i> associated with this content.
     * Returns <code>null</code> if <code>inherit</code> is <code>false</code>,
     * and there is no Observation DateTime (0040,A032) directly associated with
     * this content. If <code>inherit</code> is <code>true</code>, the
     * function searchs for a parent content with an associated
     * <i>Observation DateTime</i>, and returns that value. If there is also no
     * parent content with an associated <i>Observation DateTime</i>, the
     * function returns the <i>Content DataTime</i> of the associated 
     * <code>SRDocument</code>. 
     *
     * @param inherit specifies whether to inherit value from parents.
     * @return <i>Observation DateTime</i> associated with this content.
     */    
    public Date getObservationDateTime(boolean inherit);   
    
    /** Gets the <code>Template</code> that describes the content of this
     * <i>Content Item</i>. Returns <code>null</code>,
     * if no code>Template</code> is associated with this content. 
     *
     * @return <code>Template</code> that describes the content of this
     * <i>Content Item</i>
     */    
    public Template getTemplate();
    
    /** Gets type of relationship between (enclosing) parent content and this
     * (child) content. Returns <code>null</code>, if this content is not
     * inserted in the document tree yet or it is the root content of the
     * document.
     *
     * @return type of relationship between the parent content and this (child)
     * content.
     */
    public RelationType getRelationType();
    
    /** Gets type of the value encoded in this Content Item.
     *
     * @return type of the value encoded in this Content Item.
     */
    public ValueType getValueType();

    /** Gets (enclosing) parent content. Returns <code>null</code>, if this
     * content is not inserted in the document tree yet or it is the root
     * content of the document.
     *
     * @return parent content.
     */
     public Content getParent();
    
    /** Gets first child of this content. If there is no child content, this
     * returns <code>null</code>.
     *
     * @return first child of this content.
     */    
    public Content getFirstChild();
    
    /** Gets first child with the specified <code>RelationType</code> and 
     * <code>name</code> of this content, If there is no such child content,
     * this returns <code>null</code>.
     *
     * @param rel specifies <code>RelationType</code>; <code>null</code> for
     * any <code>RelationType</code>.
     * @param name specifies <i>Concept Name Code</i>; <code>null</code> for
     * any <i>Concept Name Code</i>.
     * @return first child with the specified <code>RelationType</code> and 
     * <code>name</code> of this content.
     */
    public Content getFirstChildBy(RelationType rel, Code name);
    
    /** Gets last child of this content. If there is no child content, this
     * returns <code>null</code>.
     *
     * @return last child of this content.
     */    
    public Content getLastChild();
    
    /** Gets the content immediately preceding this content. If there is no
     * such content, this returns <code>null</code>.
     *
     * @return immediately preceding content.
     */
    public Content getPreviousSibling();
    
    /** Gets the content immediately following this content. If there is no
     * such content, this returns <code>null</code>.
     *
     * @return immediately following content.
     */
    public Content getNextSibling();
    
    /** Gets the next content following this content with the specified
     * <code>RelationType</code> to its parent and <i>Concept Name Code</i>.
     *
     * @param rel specifies <code>RelationType</code>; <code>null</code> for
     * any <code>RelationType</code>.
     * @param name specifies <i>Concept Name Code</i>; <code>null</code> for
     * any <i>Concept Name Code</i>.
     * @return next content following this content with the specified
     * <code>RelationType</code> to its parent and <i>Concept Name Code</i>.
     */
    public Content getNextSiblingBy(RelationType rel, Code name);
      
    /** Inserts the content <code>newChild</code> with the specified
     * <code>RelationType</code> before the existing child content
     * <code>refChild</code>. If <code>refChild</code> is <code>null</code>,
     * inserts <code>newChild</code> at the end of the list of children.
     *
     * @param rel <code>RelationType</code> of the new child to this content.
     * @param newChild The content to insert.
     * @param refChild The reference content, i.e., the content before which the 
     *   new content must be inserted.
     * @return The content being inserted.
     * @throws IllegalArgumentException 
     * if <code>newChild</code> is not associated to the same {@link SRDocument}
     * as this content, or <code>newChild</code> is already in the tree, or
     * <code>refChild</code> is not a child of this content, or
     * <code>RelationType</code> or the <code>RelationType</code> of
     * <code>newChild</code> violates constraints of the <i>IOD</i>,
     * associated with the <i>SOP Class</i>, specified by the
     * <i>SOP Class UID</i> of the associated {@link SRDocument}.
     */    
    public Content insertBefore(RelationType rel, Content newChild,
            Content refChild);
    
    /** Replaces the child content <code>oldChild</code> with
     * <code>newChild</code> and the specified <code>RelationType</code> in the
     * list of children, and returns the <code>oldChild</code> content.
     *
     * @param rel <code>RelationType</code> of the new child to this content.
     * @param newChild The content to insert.
     * @param oldChild The content to replace.
     * @return The content replaced.
     * @throws IllegalArgumentException 
     * if <code>newChild</code> is not associated to the same {@link SRDocument}
     * as this content, or <code>newChild</code> is already in the tree, or
     * <code>oldChild</code> is not a child of this content, or
     * <code>RelationType</code> or the <code>RelationType</code> of
     * <code>newChild</code> violates constraints of the <i>IOD</i>,
     * associated with the <i>SOP Class</i>, specified by the
     * <i>SOP Class UID</i> of the associated {@link SRDocument}.
     */
    public Content replaceChild(RelationType rel, Content node, Content old);
    
    /** Removes the child content indicated by <code>oldChild</code> from the 
     * list of children, and returns it.
     * @param oldChild The content being removed.
     * @return The content removed.
     * @throws IllegalArgumentException 
     * if <code>oldChild</code> is not a child of this content.
     */    
    public Content removeChild(Content oldChild);
    
    /** Adds the content <code>newChild</code> with the specified
     * <code>RelationType</code> at the end of the list of children.
     *
     * @param rel <code>RelationType</code> of the new child to this content.
     * @param newChild The content to insert.
     * @return The content being inserted.
     * @throws IllegalArgumentException 
     * if <code>newChild</code> is not associated to the same {@link SRDocument}
     * as this content, or <code>newChild</code> is already in the tree, or
     * <code>RelationType</code> or the <code>RelationType</code> of
     * <code>newChild</code> violates constraints of the <i>IOD</i>,
     * associated with the <i>SOP Class</i>, specified by the
     * <i>SOP Class UID</i> of the associated {@link SRDocument}.
     */    
    public Content appendChild(RelationType rel, Content node);
    
    /** Returns whether this content has any children.
     *
     * @return <code>true</code> if this content has any children, 
     *   <code>false</code> otherwise.
     */ 
    public boolean hasChildren();
    
    /** Returns a duplicate of this content. The duplicate content has no
     * parent and is associated with the same {@link SRDocument} object as
     * this content. Use {@link SRDocument#importContent
     * SRDocument.importContent} instead, to change the associated
     * <code>SRDocument</code> object.
     * <p>The <i>Observation DateTime</i> of the duplicate content is explicitly
     * set to {@link #getObservationDateTime this.getObservationDateTime(true) }.
     *
     * @param deep If <code>true</code>, recursively clone the subtree under 
     *   the specified content; if <code>false</code>, clone only the content 
     *   itself.
     * @return The duplicate content.
     */
    public Content clone(boolean deep);
    
    public void toDataset(Dataset ds);
}
