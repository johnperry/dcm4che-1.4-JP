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
 * Thomas Hacklaender <hacklaender@iftm.de>
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

package org.dcm4che.imageio.plugins;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.TagDictionary;

import org.w3c.dom.Node;

/**
 * ImageIO Meta Data for DICOM image.
 * Change history:<br>
 * 2002.06.13, Thomas Hacklaender: Method setDataset(Dataset ds) added.<br>
 * 2002.06.16, Thomas Hacklaender: Methods reset(), setFromTree(String formatName, 
 * Node root) and mergeTree(String formatName, Node root) added. Method 
 * isReadOnly() modified.<br>
 * <br>
 * @author  gunter.zeilinger@tiani.com
 * @author Thomas Hacklaender
 * @version 2002.06.13
 */
public abstract class DcmMetadata extends javax.imageio.metadata.IIOMetadata {

    public static final String nativeMetadataFormatName = 
            "dcm4che_imageio_dicom_1.0";

    protected DcmMetadata(boolean standardMetadataFormatSupported,
            String[] extraMetadataFormatNames,
            String[] extraMetadataFormatClassNames) {
        super(standardMetadataFormatSupported, 
              nativeMetadataFormatName,
              "org.dcm4che.imageio.plugins.DcmMetadataFormat",
              extraMetadataFormatNames,
              extraMetadataFormatClassNames);
    }

    public abstract Dataset getDataset();
    
    /**
     * Sets a new Dataset as a base for the metadata.
     * @param ds the new Dataset.
     */
    public abstract void setDataset(Dataset ds);

    public abstract void setDictionary(TagDictionary dict);
    
    /**
     * Sets the internal state of this IIOMetadata object from a tree of XML 
     * DOM Nodes whose syntax is defined by the given metadata format. The 
     * previous state is discarded. If the tree's structure or contents are 
     * invalid, an IIOInvalidTreeException will be thrown.
     * @param formatName the desired metadata format.
     * @param root an XML DOM Node object forming the root of a tree.
     * @throws IllegalStateException if this object is read-only.
     * @throws IllegalArgumentException if formatName is null or is not one of 
     *         the names returned by getMetadataFormatNames.
     * @throws IIOInvalidTreeException if the tree cannot be parsed successfully 
     *         using the rules of the given format.
     */
    public abstract void setFromTree(String formatName, Node root);
    
    /**
     * Alters the internal state of this IIOMetadata object from a tree of 
     * XML DOM Nodes whose syntax is defined by the given metadata format. The 
     * previous state is altered only as necessary to accomodate the nodes that 
     * are present in the given tree. If the tree structure or contents are 
     * invalid, an IIOInvalidTreeException will be thrown.
     * @param formatName the desired metadata format.
     * @param root an XML DOM Node object forming the root of a tree.
     * @throws IllegalStateException if this object is read-only.
     * @throws IllegalArgumentException if formatName is null or is not one of 
     *         the names returned by getMetadataFormatNames.
     * @throws IIOInvalidTreeException if the tree cannot be parsed successfully 
     *         using the rules of the given format.
     */
    public abstract void mergeTree(String formatName, Node root);
    
    /**
     * Resets all the data stored in this object to default values to the state 
     * this object was in immediately after construction.
     * @since 1.0.0
     */
    public abstract void reset();
    
}
