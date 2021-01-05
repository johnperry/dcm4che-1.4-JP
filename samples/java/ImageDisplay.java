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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class ImageDisplay extends JPanel {

    private static final int PREFERRED_WIDTH = 240;
    private static final int PREFERRED_HEIGHT = 80;

    private JFileChooser filechooser = null;

    // Used only if ImageDisplay is an application 
    private JFrame frame = null;

    // Used only if ImageDisplay is an applet 
    private ImageDisplayApplet applet = null;

    /** Creates a new instance of ImageDisplay */
    public ImageDisplay(ImageDisplayApplet applet) {
        this(applet, null);
    }

    public ImageDisplay(ImageDisplayApplet applet, GraphicsConfiguration gc) {

	this.applet = applet;

        if (applet == null) {
            frame = new JFrame(gc);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

	setLayout(new BorderLayout());

	setPreferredSize(new Dimension(PREFERRED_WIDTH,PREFERRED_HEIGHT));
        
        Action openAction = new AbstractAction("Open File",
                new ImageIcon(getClass().getResource("/open.gif"))) {
            public void actionPerformed(ActionEvent e) {
                if (filechooser == null) {
                    filechooser = new JFileChooser();
                    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                }

                if (filechooser.showOpenDialog(ImageDisplay.this) ==
                        JFileChooser.APPROVE_OPTION) {
                    open(filechooser.getSelectedFile());
                }
            }
        };
        
        JToolBar bar = new JToolBar();
        bar.add(new ToolBarButton(openAction));
        add(bar, BorderLayout.NORTH);

        if(applet == null) {
	    // put ImageDisplay in a frame and show it
	    frame.setTitle("Image Display - Control Panel");
	    frame.getContentPane().add(this, BorderLayout.CENTER);
	    frame.pack();
	    frame.setVisible(true);
	} 
    }
    
    private void open(File f) {
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(f);
            Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
            ImageReader reader = (ImageReader)iter.next();
            reader.setInput(iis, false);
            JPanel p = new ImageBox(reader);
            //JPanel p = new ImageBox(f);
            JFrame jf = new JFrame("ImageDisplay - Display Panel");
            jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            jf.getContentPane().add(p);
            jf.pack();
            jf.setSize(Math.min(jf.getWidth(),800),
                    Math.min(jf.getHeight(),600));
            jf.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ImageDisplay Main. Called only if we're an application, not an applet.
     */
    public static void main(String[] args) {
    // Create ImageDisplay on the default monitor
	ImageDisplay display = new ImageDisplay(null, GraphicsEnvironment.
                                             getLocalGraphicsEnvironment().
                                             getDefaultScreenDevice().
                                             getDefaultConfiguration());
    }
}

class ToolBarButton extends JButton {
    public ToolBarButton(Action a) {
        super((Icon)a.getValue(Action.SMALL_ICON));
        String toolTip = (String)a.getValue(Action.SHORT_DESCRIPTION);
        if (toolTip == null)
            toolTip = (String)a.getValue(Action.NAME);
        if (toolTip != null)
            setToolTipText(toolTip);
        addActionListener(a);
    }
}