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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Print out all the metadata found in a list of image files.
 *
 * @version 0.5
 *
 * @author Daniel Rice
 */
public class ImageDumper {

    void displayMetadata(Node root) {
        displayMetadata(root, 0);
    }

    void indent(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
    }

    private void displayMetadata(Node node, int level) {
        // Print node name and attribute names and values
        indent(level);
        System.out.print("<" + node.getNodeName());
        NamedNodeMap map = node.getAttributes();
        if (map != null) {
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                System.out.print(" " + attr.getNodeName() +
                                 "=\"" +
                                 attr.getNodeValue() +
                                 "\"");
            }
        }

        // If the node is an IIOMetadataNode, print information
        // about the user object
        if (node instanceof IIOMetadataNode) {
            Object o = ((IIOMetadataNode)node).getUserObject();
            if (o != null) {
                System.out.print(" userObject=\"");
                System.out.print(o.getClass().getName());
                if (o instanceof byte[]) {
                    byte[] b = (byte[])o;
                    for (int i = 0; i < b.length; i++) {
                        System.out.print(" ");
                        System.out.print(b[i] & 0xff);
                    }
                } else {
                }
                System.out.print("\")");
            }
        }

        // Visit the children recursively
        Node child = node.getFirstChild();
        if (child != null) {
            System.out.println(">");
            while (child != null) {
                displayMetadata(child, level + 1);
                child = child.getNextSibling();
            }
            indent(level);
            System.out.println("</" + node.getNodeName() + ">");
        } else {
            System.out.println("/>");
        }
    }

    public ImageDumper(File f) throws IOException {
        System.out.println("\n********** " + f + ":");
        
        ImageInputStream iis = null;
        try {
            iis = ImageIO.createImageInputStream(f);
        } catch (IOException ioe) {
            System.out.println("I/O exception obtaining a stream!");
            System.exit(0);
        }

        if (iis == null) {
            System.out.println("Unable to get a stream!");
            System.exit(0);
        }

        Iterator iter = ImageIO.getImageReaders(iis);
        ImageReader reader = null;
        while (iter.hasNext()) {
            reader = (ImageReader)iter.next();
            System.out.println("Using " +
                               reader.getClass().getName() +
                               " to read.");
            break;
        }

        if (reader == null) {
            System.err.println("Unable to find a reader!");
            System.exit(0);
        }

        reader.setInput(iis, true);
        
        int numImages = reader.getNumImages(true);
        System.out.println("\nThe file contains " + numImages + " image"
                           + (numImages == 1 ? "" : "s") + ".");
        System.out.println();

        IIOMetadata sm = reader.getStreamMetadata();
        if (sm == null) {
            System.out.println("The file contains no stream metadata.");
        } else {
            System.out.println("Stream metadata:");
            String nativeFormatName = sm.getNativeMetadataFormatName();
            displayMetadata(sm.getAsTree(nativeFormatName));
            String[] extraFormatNames = sm.getExtraMetadataFormatNames();
            if (extraFormatNames != null) {
                for (int i = 0; i < extraFormatNames.length; ++i) {
                    System.out.println("Extra Stream metadata - "
                            + extraFormatNames[i]);
                    displayMetadata(sm.getAsTree(extraFormatNames[i]));
                }
            }   
        }

        for (int i = 0; i < numImages; i++) {
            System.out.println("\n---------- Image #" + i + " ----------");
            System.out.println();

            int width = reader.getWidth(i);
            System.out.println("width = " + width);

            int height = reader.getHeight(i);
            System.out.println("height = " + height);

            int numThumbnails = reader.getNumThumbnails(i);
            System.out.println("numThumbnails = " + numThumbnails);

            for (int j = 0; i < numThumbnails; j++) {
                System.out.println("  width = " +
                                   reader.getThumbnailWidth(i, j) + 
                                   ", height = " +
                                   reader.getThumbnailHeight(i, j));
            }

            IIOMetadata im = reader.getImageMetadata(i);
            if (im == null) {
                System.out.println("\nThe image has no metadata.");
            } else {
                System.out.println("\nImage metadata:");
                String nativeFormatName = im.getNativeMetadataFormatName();
                displayMetadata(im.getAsTree(nativeFormatName));
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("usage: java ImageDumper filename [...]");
        }

        for (int i = 0; i < args.length; i++) {
            File f = new File(args[i]);
            if (!f.exists()) {
                System.out.println("File " + f + " does not exist!");
                System.exit(0);
            }
            
            try {
                new ImageDumper(f);
            } catch (IOException io) {
                System.err.println("IOException: " + io);
            }
        }
    }
}
