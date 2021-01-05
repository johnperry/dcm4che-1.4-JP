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
import org.dcm4che.dict.UIDs;

import org.apache.log4j.Logger;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
abstract class RelationConstraintsImpl implements RelationConstraints {
    static Logger log = Logger.getLogger(RelationConstraintsImpl.class);
    // Constants -----------------------------------------------------     
    public static final RelationConstraints KEY_OBJECT =
            new RelationConstraints() {
        public void check(Content source, Content.RelationType relation,
                Content target) {
            if (!(source instanceof ContainerContent))
                log.warn(
                    "Violation of Rel Constraints for Key Objects - source:"
                    + source);
              
            if (relation == Content.RelationType.CONTAINS) {
                if (!(target instanceof TextContent ||
                        target instanceof CompositeContent))
                    log.warn(
                        "Violation of Rel Constraints for Key Objects - target:"
                        + target);
//                    throw new IllegalArgumentException("" + target);
            } else if (relation == Content.RelationType.HAS_OBS_CONTEXT) {
                if (!(target instanceof TextContent ||
                        target instanceof CodeContent ||
                        target instanceof UIDRefContent ||
                        target instanceof PNameContent))
                    log.warn(
                        "Violation of Rel Constraints for Key Objects - target:"
                        + target);
//                    throw new IllegalArgumentException("" + target);
            } else if (relation == Content.RelationType.HAS_CONCEPT_MOD) {
                if (!(target instanceof CodeContent))
                    log.warn(
                        "Violation of Rel Constraints for Key Objects - target:"
                        + target);
//                    throw new IllegalArgumentException("" + target);
            } else
                log.warn(
                    "Violation of Rel Constraints for Key Objects - rel:"
                    + relation);
//                throw new IllegalArgumentException("" + relation);
        }
    };
    
    public static final RelationConstraints BASIC_TEXT_SR =
            new RelationConstraintsImpl() {
        void checkTarget(Content target) {
            if (target instanceof ReferencedContent ||
                    target instanceof NumContent ||
                    target instanceof SCoordContent ||
                    target instanceof TCoordContent)
                log.warn(
                    "Violation of Rel Constraints for Basic Text SR - target:"
                    + target);
//                throw new IllegalArgumentException("" + target);
        }
        void checkSelectedFrom(Content source, Content target) {
            log.warn(
                "Violation of Rel Constraints for Basic Text SR - rel:"
                + Content.RelationType.SELECTED_FROM);
//            throw new IllegalArgumentException(
//                    "" + Content.RelationType.SELECTED_FROM);
        }
                
    };
  
    public static final RelationConstraints ENHANCED_SR =
            new RelationConstraintsImpl() {
        void checkTarget(Content target) {
            if (target instanceof ReferencedContent) {
                log.warn(
                    "Violation of Rel Constraints for Enhanced SR - target:"
                    + target);
//                throw new IllegalArgumentException("" + target);
            }
        }
    };
    // Attributes ----------------------------------------------------

    // Constructors --------------------------------------------------
    public static RelationConstraints valueOf(String sopClassUID) {
        if (UIDs.BasicTextSR.equals(sopClassUID))
            return BASIC_TEXT_SR;
        if (UIDs.EnhancedSR.equals(sopClassUID))
            return ENHANCED_SR;
        if (UIDs.ComprehensiveSR.equals(sopClassUID))
            return null; // not yet implemented
        if (UIDs.KeyObjectSelectionDocument.equals(sopClassUID))
            return KEY_OBJECT;
        return null;
    }         

    // Methodes ------------------------------------------------------
    static boolean isContainerOrComposite(Content content) {
        return content instanceof ContainerContent
                || content instanceof CompositeContent;
    }
    
    void checkTarget(Content target) {
    }
    
    void checkContains(Content source, Content target) {
        if (!(source instanceof ContainerContent))
            log.warn(
                "Violation of CONTAINS Rel Constraints - source:"
                + source);
//            throw new IllegalArgumentException("" + source);
    }

    void checkHasObsContext(Content source, Content target) {
        if (!(source instanceof ContainerContent))
            log.warn(
                "Violation of HAS OBS CONTEXT Rel Constraints - source:"
                + source);
//            throw new IllegalArgumentException("" + source);
        if (isContainerOrComposite(target))
            log.warn(
                "Violation of HAS OBS CONTEXT Rel Constraints - target:"
                + target);
//            throw new IllegalArgumentException("" + target);
    }

    void checkHasAcqContext(Content source, Content target) {
        if (!isContainerOrComposite(source))
            log.warn(
                "Violation of HAS ACQ CONTEXT Rel Constraints - source:"
                + source);
//            throw new IllegalArgumentException("" + source);
        if (isContainerOrComposite(target))
            log.warn(
                "Violation of HAS ACQ CONTEXT Rel Constraints - source:"
                + target);
//            throw new IllegalArgumentException("" + target);
    }

    void checkHasConceptMod(Content source, Content target) {
        if (!(target instanceof TextContent ||
                target instanceof CodeContent))
            log.warn(
                "Violation of HAS CONCEPT MOD Rel Constraints - target:"
                + target);
//            throw new IllegalArgumentException("" + target);
    }

    void checkHasProperties(Content source, Content target) {
        if (!(source instanceof TextContent ||
                source instanceof CodeContent ||
                source instanceof NumContent)) {
            log.warn(
                "Violation of HAS PROPERTIES Rel Constraints - source:"
                + source);
//            throw new IllegalArgumentException("" + source);
        }
        if (target instanceof ContainerContent) {
            log.warn(
                "Violation of HAS PROPERTIES Rel Constraints - target:"
                + target);
//            throw new IllegalArgumentException("" + target);
        }
    }

    void checkInferredFrom(Content source, Content target) {
        checkHasProperties(source, target);
    }
    
    void checkSelectedFrom(Content source, Content target) {
        if (source instanceof SCoordContent) {
            if (!(target instanceof ImageContent))
                log.warn(
                    "Violation of SELECT FROM Rel Constraints - target:"
                + target);
//                throw new IllegalArgumentException("" + target);
        } else if (source instanceof TCoordContent) {
            if (!(target instanceof SCoordContent ||
                    target instanceof ImageContent ||
                    target instanceof WaveformContent))
                log.warn(
                    "Violation of SELECT FROM Rel Constraints - target:"
                + target);
//                throw new IllegalArgumentException("" + target);
        } else
            log.warn(
                "Violation of SELECT FROM Rel Constraints - source:"
            + source);
//            throw new IllegalArgumentException("" + source);
    }

    public void check(Content source, Content.RelationType relation,
            Content target) {
        
        checkTarget(target);

        if (relation == Content.RelationType.CONTAINS)
            checkContains(source, target);
        else if (relation == Content.RelationType.HAS_OBS_CONTEXT)
            checkHasObsContext(source, target);
        else if (relation == Content.RelationType.HAS_ACQ_CONTEXT)
            checkHasAcqContext(source, target);
        else if (relation == Content.RelationType.HAS_CONCEPT_MOD)
            checkHasConceptMod(source, target);
        else if (relation == Content.RelationType.HAS_PROPERTIES)
            checkHasProperties(source, target);
        else if (relation == Content.RelationType.INFERRED_FROM)
            checkInferredFrom(source, target);
        else if (relation == Content.RelationType.SELECTED_FROM)
            checkSelectedFrom(source, target);
        else
            throw new IllegalArgumentException("" + relation);
    }                    
}