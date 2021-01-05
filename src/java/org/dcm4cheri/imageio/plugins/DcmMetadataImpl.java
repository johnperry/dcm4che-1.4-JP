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

package org.dcm4cheri.imageio.plugins;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Change history:<br>
 * 2002.06.13, Thomas Hacklaender: Method setDataset(Dataset ds) added.<br>
 * 2002.06.16, Thomas Hacklaender: Methods reset(), setFromTree(String formatName, 
 * Node root) and mergeTree(String formatName, Node root) added. Method 
 * isReadOnly() modified.<br>
 * <br>
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmMetadataImpl extends DcmMetadata {
    private static Logger log = Logger.getLogger(DcmMetadataImpl.class);
    
    static final DcmImageReaderConf conf = DcmImageReaderConf.getInstance();
    
    // 2002.06.13, Thomas Hacklaender: Modifier "final" removed.
    // private final Dataset ds;
    private Dataset ds;
    
    private TagDictionary dict = 
            DictionaryFactory.getInstance().getDefaultTagDictionary();
            
    /** Creates a new instance of DcmMetadata */
    public DcmMetadataImpl(Dataset ds) {
        super(false,
            conf.getExtraStreamMetadataFormatNames(),
            conf.getExtraStreamMetadataFormatClassNames());
        this.ds = ds;
    }
    
    public final Dataset getDataset() {
        return ds;
    }
    
    /**
     * Sets a new Dataset as a base for the metadata.
     * @param ds the new Dataset.
     * @author Thomas Hacklaender
     * @version 2002.06.13
     * @since 1.0.0
     */
    public final void setDataset(Dataset ds) {
        if (ds == null)
            throw new NullPointerException("ds can not be null");
        this.ds = ds;
    }
    
    public final void setDictionary(TagDictionary dict) {
        this.dict = dict;
    }
    
    /**
     * This object supports the mergeTree, setFromTree, and reset methods.
     * @return always false.
     * @author Thomas Hacklaender
     * @version 2002.06.16
     * @since 1.0.0
     */
    public final boolean isReadOnly() {
        return false;
    }
    
    public Node getAsTree(String formatName) {
        if (formatName.equals(nativeMetadataFormatName)) {
            return getTree(formatName, null, null);
        } else if (formatName.equals
                   (IIOMetadataFormatImpl.standardMetadataFormatName)) {
            throw new IllegalArgumentException(
                    IIOMetadataFormatImpl.standardMetadataFormatName
                    + " not supported!");
        } else if (conf.contains(formatName)) {
            return getTree(formatName, conf.getFilterDataset(formatName),
                    conf.getTransformerHandler(formatName));
        } else {
            throw new IllegalArgumentException("Not a recognized format: "
                    + formatName);
        }
    }
    
    private Node getTree(String formatName,
            Dataset filter, TransformerHandler th) {
        final IIOMetadataNode root = new IIOMetadataNode(formatName);
        
        ContentHandler ch = new DefaultHandler() {
            Node curNode = root;
            public void startElement (String uri, String localName,
                                      String qName, Attributes attr) {
                if ("dicomfile".equals(qName)) {
                    return;
                }
                IIOMetadataNode newNode = new IIOMetadataNode(qName);
                for (int i = 0, n = attr.getLength(); i < n; ++i) {
                    String attrName = attr.getQName(i);
                    if (!"pos".equals(attrName)) {
                        newNode.setAttribute(attrName, attr.getValue(i));
                    }
                }
                curNode.appendChild(newNode);
                curNode = newNode;
            }

            public void endElement (String uri, String localName, String qName)
            {
                if ("dicomfile".equals(qName)) {
                    return;
                }
                curNode = curNode.getParentNode();
            }
        };
        
        try {
            if (th != null) {
                th.setResult(new SAXResult(ch));
                ch = th;
            }
            ds.subSet(filter).writeFile(ch, dict);
        } catch (Exception ex) {
            log.error(ex);
            throw new RuntimeException("Exception in getTree", ex);
        }
        return root;
    }
    
    /**
     * Alters the internal state of this DcmMetadata's Dataset object from a tree
     * of XML DOM Nodes whose syntax is defined by the given metadata format. The 
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
     * @author Thomas Hacklaender
     * @version 2002.06.16
     * @since 1.0.0
     */
    public void mergeTree(String formatName, Node root)
    {
        if (formatName.equals(IIOMetadataFormatImpl.standardMetadataFormatName)) {
            throw new IllegalArgumentException(
                IIOMetadataFormatImpl.standardMetadataFormatName
                + " not supported!");
        }
        else if (formatName.equals(nativeMetadataFormatName)) {
            mergeTree(root);
        }
        else {
            throw new IllegalArgumentException("Not a recognized format: "
                                               + formatName);
        }
    }
    
    /**
     * Does a merge by using an identity <code>Transformer</code> to transform
     * from the <code>DOMSource</code> to the <code>SAXResult</code> which is
     * fed the SAX handler of the internal DICOM <code>Dataset</code>.
     * @param root the root <code>Node</code> of a DOM tree
     */
    private void mergeTree(Node root)
    {
        DOMSource src = new DOMSource(root);
        SAXResult res = new SAXResult(ds.getSAXHandler());
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(src, res);
        }
        catch (TransformerConfigurationException tce) {
            log.error(tce);
        }
        catch (TransformerException te) {
            log.error(te);
        }
    }
    
    /**
     * Resets all the data stored in this object to default values to the state 
     * this object was in immediately after construction.
     * @author Thomas Hacklaender
     * @version 2002.06.16
     * @since 1.0.0
     */
    public void reset() {
        ds = DcmObjectFactory.getInstance().newDataset();
    }
    
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
     * @author Thomas Hacklaender
     * @version 2002.06.16
     * @since 1.0.0
     */
    public void setFromTree(String formatName, Node root) {
        if (formatName.equals(IIOMetadataFormatImpl.standardMetadataFormatName)) {
            throw new IllegalArgumentException(
                IIOMetadataFormatImpl.standardMetadataFormatName
                + " not supported!");
        }
        else if (formatName.equals(nativeMetadataFormatName)) {
            reset();
            mergeTree(root);
        }
        else {
            throw new IllegalArgumentException("Not a recognized format: "
                                               + formatName);
        }
    }

}
