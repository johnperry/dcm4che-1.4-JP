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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A demo application that allows various aspects of an ImageReadParam
 * to be modified interactively.
 *
 * @version 0.5
 *
 * @author Daniel Rice
 */
public class ImageReadParamDemo extends JPanel 
    implements ChangeListener, ItemListener {

    ReadParamPanel readParamPanel;

    ImagePanel imagePanel;

    ImageReader reader;

    ImageReadParam param;

    JPanel controlPanel;
    JSlider imageIndexSlider;
    JSlider sourceXOffsetSlider;
    JSlider sourceYOffsetSlider;
    JSlider sourceWidthSlider;
    JSlider sourceHeightSlider;
    JSlider sourceXSubsamplingSlider;
    JSlider sourceYSubsamplingSlider;
    JSlider subsamplingXOffsetSlider;
    JSlider subsamplingYOffsetSlider;
    JSlider destXOffsetSlider;
    JSlider destYOffsetSlider;

    JTabbedPane imageTabbedPane;

    // JButton readImageButton;

    int numImages;
    int imageIndex = 0;

    int imageWidth;
    int imageHeight;
    int sourceXOffset = 0;
    int sourceYOffset = 0;
    int sourceWidth;
    int sourceHeight;
    int sourceXSubsampling = 1;
    int sourceYSubsampling = 1;
    int subsamplingXOffset = 0;
    int subsamplingYOffset = 0;
    int destXOffset = 0;
    int destYOffset = 0;
    int[] sourceBands = null;
    int[] destinationBands = null;

    JComboBox minPassComboBox;
    JComboBox numPassesComboBox;
    JComboBox bandsComboBox;

    int minPass = 0;
    int numPasses = Integer.MAX_VALUE;

    public String[] minPassStrings = {
        "Start with pass 0",
        "Start with pass 1",
        "Start with pass 2",
        "Start with pass 3",
        "Start with pass 4",
        "Start with pass 5",
        "Start with pass 6"
    };

    public String[] numPassesStrings = {
        "Decode all passes",
        "Decode 1 pass",
        "Decode 2 passes",
        "Decode 3 passes",
        "Decode 4 passes",
        "Decode 5 passes",
        "Decode 6 passes",
        "Decode 7 passes",
    };

    public int[][] bandSubsets = {
        null, null,
        { 0 }, { 0 },
        { 0 }, { 1 },
        { 0 }, { 2 },
        { 1 }, { 0 },
        { 1 }, { 1 },
        { 1 }, { 2 },
        { 2 }, { 0 },
        { 2 }, { 1 },
        { 2 }, { 2 },
        { 0, 1 }, { 0, 1 },
        { 0, 1 }, { 1, 0 },
        { 0, 1 }, { 0, 2 },
        { 0, 1 }, { 2, 0 },
        { 0, 1 }, { 1, 2 },
        { 0, 1 }, { 2, 1 },
        { 0, 2 }, { 0, 1 },
        { 0, 2 }, { 1, 0 },
        { 0, 2 }, { 0, 2 },
        { 0, 2 }, { 2, 0 },
        { 0, 2 }, { 1, 2 },
        { 0, 2 }, { 2, 1 },
        { 1, 2 }, { 0, 1 },
        { 1, 2 }, { 1, 0 },
        { 1, 2 }, { 0, 2 },
        { 1, 2 }, { 2, 0 },
        { 1, 2 }, { 1, 2 },
        { 1, 2 }, { 2, 1 },
        { 0, 1, 2 }, { 0, 1, 2 },
        { 0, 1, 2 }, { 0, 2, 1 },
        { 0, 1, 2 }, { 1, 0, 2 },
        { 0, 1, 2 }, { 1, 2, 0 },
        { 0, 1, 2 }, { 1, 2, 0 },
        { 0, 1, 2 }, { 2, 0, 1 },
        { 0, 1, 2 }, { 2, 1, 0 }
    };

    private static JSlider createSlider(JPanel target,
                                        ChangeListener listener,
                                        String label,
                                        int min, int max, int value,
                                        int majorTickSpacing,
                                        boolean paintTicks) {
        JSlider js = new JSlider(min, max, value);
        js.setSnapToTicks(true);
        js.addChangeListener(listener);
        js.setMajorTickSpacing(majorTickSpacing);
        js.setPaintTicks(paintTicks);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2));
        panel.add(new JLabel(label, SwingConstants.CENTER));
        panel.add(js);
        target.add(panel);

        return js;
    }

    private void createSliders(ChangeListener listener,
                               int imageWidth, int imageHeight,
                               int numImages) {
        this.imageIndexSlider = createSlider(controlPanel, listener,
                                             "Image Index",
                                             0, numImages - 1, 0,
                                             1, true);
        this.sourceXOffsetSlider = createSlider(controlPanel, listener,
                                                "Source X Offset",
                                                0, imageWidth, 0,
                                                0, false);
        this.sourceWidthSlider = createSlider(controlPanel, listener,
                                              "Width",
                                              1, imageWidth, imageWidth,
                                              0, false);
        this.sourceYOffsetSlider = createSlider(controlPanel, listener,
                                                "Source Y Offset",
                                                0, imageHeight, 0,
                                                0, false);
        this.sourceHeightSlider = createSlider(controlPanel, listener,
                                               "Height",
                                               1, imageHeight, imageHeight,
                                               0, false);
        this.sourceXSubsamplingSlider = createSlider(controlPanel, listener,
                                                     "X Subsampling",
                                                     1, 10, 1,
                                                     1, true);
        this.sourceYSubsamplingSlider = createSlider(controlPanel, listener,
                                                     "Y Subsampling",
                                                     1, 10, 1,
                                                     1, true);
        this.subsamplingXOffsetSlider = createSlider(controlPanel, listener,
                                                     "X Subsampling Offset",
                                                     0, 0, 0,
                                                     1, true);
        this.subsamplingYOffsetSlider = createSlider(controlPanel, listener,
                                                     "Y Subsampling Offset",
                                                     0, 0, 0,
                                                     1, true);
        this.destXOffsetSlider = createSlider(controlPanel, listener,
                                              "Dest X Offset",
                                              -50, imageWidth, 0,
                                              0, false);
        this.destYOffsetSlider = createSlider(controlPanel, listener,
                                              "Dest Y Offset",
                                              -50, imageHeight, 0,
                                              0, false);
    }

    private void createPassesComboBoxes() {
        this.minPassComboBox = new JComboBox();
        for (int i = 0; i < minPassStrings.length; i++) {
            minPassComboBox.addItem(minPassStrings[i]);
        }
        minPassComboBox.addItemListener(this);
        controlPanel.add(minPassComboBox);
        
        this.numPassesComboBox = new JComboBox();
        for (int i = 0; i < numPassesStrings.length; i++) {
            numPassesComboBox.addItem(numPassesStrings[i]);
        }
        numPassesComboBox.addItemListener(this);
        controlPanel.add(numPassesComboBox);
    }

    private void setupBandsComboBox(int numBands) {
        for (int i = 0; i < bandSubsets.length/2; i++) {
            int[] sbands = bandSubsets[2*i];
            int[] dbands = bandSubsets[2*i + 1];

            // Only include entries whose maximum band is less than numBands
            int maxBand = 0;

            String s = "Source bands = ";
            if (sbands == null) {
                s += "null";
            } else {
                maxBand = sbands[0];
                s += "{ " + sbands[0];
                for (int j = 1; j < sbands.length; j++) {
                    maxBand = Math.max(maxBand, sbands[j]);
                    s += ", " + sbands[j];
                }
                s += " }";
            }
            s += ", Dest bands = ";
            if (dbands == null) {
                s += "null";
            } else {
                s += "{ " + dbands[0];
                maxBand = Math.max(maxBand, dbands[0]);
                for (int j = 1; j < dbands.length; j++) {
                    maxBand = Math.max(maxBand, dbands[j]);
                    s += ", " + dbands[j];
                }
                s += " }";
            }

            if (maxBand < numBands) {
                bandsComboBox.addItem(s);
            }
        }
    }

    private void createControlPanel(ChangeListener listener,
                                    int imageWidth, int imageHeight,
                                    int numBands,
                                    int numImages) {
        this.controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(14, 1));
        
        createSliders(listener, imageWidth, imageHeight, numImages);
        createPassesComboBoxes();

        this.bandsComboBox = new JComboBox();
        setupBandsComboBox(numBands);
        bandsComboBox.addItemListener(this);
        controlPanel.add(bandsComboBox);
    }

    public ImageReadParamDemo(ImageReader reader) throws IOException {
        setLayout(new BorderLayout());
        
        this.reader = reader;
        this.numImages = reader.getNumImages(true);
        this.imageIndex = 0;
        this.imageWidth = reader.getWidth(imageIndex);
        this.imageHeight = reader.getHeight(imageIndex);
        this.sourceWidth = imageWidth;
        this.sourceHeight = imageHeight;

        this.param = reader.getDefaultReadParam();
        param.setSourceRegion(new Rectangle(sourceXOffset, sourceYOffset,
                                            sourceWidth, sourceHeight));

        BufferedImage rawImage = reader.read(imageIndex);
        this.readParamPanel = new ReadParamPanel(rawImage, param);
        JScrollPane p0 = new JScrollPane(readParamPanel);

        this.imagePanel = new ImagePanel(rawImage);
        JScrollPane p1 = new JScrollPane(imagePanel);
        
        this.imageTabbedPane = new JTabbedPane();
        imageTabbedPane.add("Full Image", p0);
        imageTabbedPane.add("Loaded Image", p1);
        add(imageTabbedPane, BorderLayout.CENTER);
        
        createControlPanel(this, imageWidth, imageHeight,
                           rawImage.getSampleModel().getNumBands(),
                           numImages);
        add(controlPanel, BorderLayout.EAST);
    }

    public void stateChanged(ChangeEvent e) {
        JSlider js = (JSlider)e.getSource();
        int value = js.getValue();

        boolean changed = false;

        if (js == imageIndexSlider) {
            try {
                this.imageIndex = value;
                this.imageWidth = reader.getWidth(imageIndex);
                this.imageHeight = reader.getHeight(imageIndex);

                this.sourceXOffset = Math.min(sourceXOffset, imageWidth - 1);
                this.sourceYOffset = Math.min(sourceYOffset, imageHeight - 1);
                this.sourceWidth = Math.min(sourceWidth, imageWidth);
                this.sourceHeight = Math.min(sourceHeight, imageHeight);
                this.destXOffset = Math.min(destXOffset, imageWidth - 1);
                this.destYOffset = Math.min(destYOffset, imageHeight - 1);
                
                sourceXOffsetSlider.setMaximum(imageWidth);
                sourceXOffsetSlider.setValue(sourceXOffset);

                sourceYOffsetSlider.setMaximum(imageHeight);
                sourceYOffsetSlider.setValue(sourceYOffset);

                sourceWidthSlider.setMaximum(imageWidth);
                sourceWidthSlider.setValue(sourceWidth);

                sourceHeightSlider.setMaximum(imageHeight);
                sourceHeightSlider.setValue(sourceHeight);

                destXOffsetSlider.setValue(destXOffset);
                destYOffsetSlider.setValue(destYOffset);

                BufferedImage rawImage = reader.read(imageIndex);
                imagePanel.setImage(rawImage);

                bandsComboBox.removeAllItems();
                setupBandsComboBox(rawImage.getSampleModel().getNumBands());

                param.setSourceRegion(new Rectangle(sourceXOffset,
                                                    sourceYOffset,
                                                    sourceWidth,
                                                    sourceHeight));
                readParamPanel.setImage(rawImage, param);
            } catch (IOException ioe) {
            }
        } else if (js == sourceXOffsetSlider) {
            if (this.sourceXOffset != value) {
                this.sourceXOffset = value;
                changed = true;
            }
        } else if (js == sourceYOffsetSlider) {
            if (this.sourceYOffset != value) {
                this.sourceYOffset = value;
                changed = true;
            }
        } else if (js == sourceWidthSlider) {
            if (this.sourceWidth != value) {
                this.sourceWidth = value;
                changed = true;
            }
        } else if (js == sourceHeightSlider) {
            if (this.sourceHeight != value) {
                this.sourceHeight = value;
                changed = true;
            }
        } else if (js == sourceXSubsamplingSlider) {
            if (this.sourceXSubsampling != value) {
                this.sourceXSubsampling = value;
                subsamplingXOffsetSlider.setMaximum(sourceXSubsampling - 1);
                subsamplingXOffset = Math.min(subsamplingXOffset,
                                              sourceXSubsampling - 1);
                changed = true;
            }
        } else if (js == sourceYSubsamplingSlider) {
            if (this.sourceYSubsampling != value) {
                this.sourceYSubsampling = value;
                subsamplingYOffsetSlider.setMaximum(sourceYSubsampling - 1);
                subsamplingYOffset = Math.min(subsamplingYOffset,
                                              sourceYSubsampling - 1);
                changed = true;
            }
        } else if (js == subsamplingXOffsetSlider) {
            if (this.subsamplingXOffset != value) {
                this.subsamplingXOffset = value;
                changed = true;
            }
        } else if (js == subsamplingYOffsetSlider) {
            if (this.subsamplingYOffset != value) {
                this.subsamplingYOffset = value;
                changed = true;
            }
        } else if (js == destXOffsetSlider) {
            if (this.destXOffset != value) {
                this.destXOffset = value;
                changed = true;
            }
        } else if (js == destYOffsetSlider) {
            if (this.destYOffset != value) {
                this.destYOffset = value;
                changed = true;
            }
        }
        
        subsamplingYOffset = Math.min(subsamplingYOffset,
                                      sourceYSubsampling - 1);

        param.setSourceRegion(new Rectangle(sourceXOffset, sourceYOffset,
                                            sourceWidth, sourceHeight));
        param.setSourceSubsampling(sourceXSubsampling, sourceYSubsampling,
                                   subsamplingXOffset, subsamplingYOffset);
        param.setDestinationOffset(new Point(destXOffset, destYOffset));

        readParamPanel.repaint();

        if (changed) {
            readImage();
        }
    }

    public void readImage() {
        new ReadThread(reader, imageIndex, param, imagePanel).start();
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }

        String item = (String)e.getItem();
        if (e.getSource() == minPassComboBox) {
            for (int i = 0; i < minPassStrings.length; i++) {
                if (item == minPassStrings[i]) {
                    this.minPass = i;
                    break;
                }
            }
        } else if (e.getSource() == numPassesComboBox) {
            for (int i = 0; i < numPassesStrings.length; i++) {
                if (item == numPassesStrings[i]) {
                    if (i == 0) {
                        this.numPasses = Integer.MAX_VALUE;
                    } else {
                        this.numPasses = i;
                    }
                    break;
                }
            }
        } else if (e.getSource() == bandsComboBox) {
            int index = bandsComboBox.getSelectedIndex();
            this.sourceBands = bandSubsets[2*index];
            this.destinationBands = bandSubsets[2*index + 1];
        }

        param.setSourceProgressivePasses(minPass, numPasses);
        param.setSourceBands(sourceBands);
        param.setDestinationBands(destinationBands);

        readImage();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: java ImageReadParamDemo <imagefile>");
            System.exit(0);
        }

        File f = new File(args[0]);
        if (!f.exists()) {
            System.out.println("File " + f + " does not exist!");
            System.exit(0);
        }

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

        try {
            // Allow random access to multiple images
            reader.setInput(iis, false);
        
            JPanel p = new ImageReadParamDemo(reader);
            JFrame jf = new JFrame("ImageReadParam Demo");
            jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jf.getContentPane().add(p);
            jf.pack();
            jf.setLocation(100, 100);
            jf.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}

/**
 * A JPanel that displays the original image, with an overlay indicating
 * the effects of the ImageReadParam.
 */
class ReadParamPanel extends JPanel {

    static final Color myGray = new Color(204, 204, 204);

    BufferedImage theImage = null;
    int imageWidth;
    int imageHeight;

    ImageReadParam param;

    public ReadParamPanel(BufferedImage theImage,
                          ImageReadParam param) {
        this.theImage = theImage;
        this.param = param;
        this.imageWidth = theImage.getWidth();
        this.imageHeight = theImage.getHeight();
        setPreferredSize(new Dimension(imageWidth, imageHeight));
    }

    public void setImage(BufferedImage theImage, ImageReadParam param) {
        this.theImage = theImage;
        this.param = param;
        this.imageWidth = theImage.getWidth();
        this.imageHeight = theImage.getHeight();
        repaint();
    }

    public void paint(Graphics g) {
        Rectangle rect = param.getSourceRegion();
        int sourceXOffset = rect.x;
        int sourceYOffset = rect.y;
        int sourceWidth = rect.width;
        int sourceHeight = rect.height;
        int sourceXSubsampling = param.getSourceXSubsampling();
        int sourceYSubsampling = param.getSourceYSubsampling();
        int subsamplingXOffset = param.getSubsamplingXOffset();
        int subsamplingYOffset = param.getSubsamplingYOffset();

        sourceWidth = Math.min(sourceWidth, imageWidth - sourceXOffset);
        sourceHeight = Math.min(sourceHeight, imageHeight - sourceYOffset);

        g.setColor(myGray);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(theImage, 0, 0, null);
        
        g.setColor(Color.yellow);
        g.drawRect(sourceXOffset, sourceYOffset,
                   sourceWidth - 1, sourceHeight - 1);

        g.setColor(Color.red);
        if (sourceXSubsampling != 1 || sourceYSubsampling != 1) {
            for (int j = sourceYOffset + subsamplingYOffset;
                 j < sourceYOffset + sourceHeight;
                 j += sourceYSubsampling) {
                for (int i = sourceXOffset + subsamplingXOffset;
                     i < sourceXOffset + sourceWidth;
                     i += sourceXSubsampling) {
                    g.fillRect(i, j, 1, 1);
                }
            }
        }        
    }
}

/**
 * A JPanel that displays the loaded image.
 */
class ImagePanel extends JPanel {

    static final Color myGray = new Color(204, 204, 204);

    BufferedImage theImage = null;

    public ImagePanel(BufferedImage theImage) {
        this.theImage = theImage;
        int origWidth = Math.max(theImage.getWidth(), 100);
        int origHeight = Math.max(theImage.getHeight(), 100);
        setPreferredSize(new Dimension(origWidth, origHeight));
    }

    public void setImage(BufferedImage theImage) {
        this.theImage = theImage;
        repaint();
    }

    public void paint(Graphics g) {
        g.setColor(myGray);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(theImage, 0, 0, null);
    }
}

/**
 * A Thread used to read the image and redisplay it as it is updated.
 */
class ReadThread extends Thread implements IIOReadUpdateListener {
    
    ImageReader reader;
    int imageIndex;
    ImageReadParam param;
    ImagePanel imagePanel;
    
    public ReadThread(ImageReader reader,
                      int imageIndex,
                      ImageReadParam param,
                      ImagePanel imagePanel) {
        this.reader = reader;
        this.imageIndex = imageIndex;
        this.param = param;
        this.imagePanel = imagePanel;
    }
    
    // IIOReadUpdateListener
    
    public void passStarted(ImageReader source,
                            BufferedImage theImage,
                            int pass,
                            int minPass, int maxPass,
                            int minX, int minY,
                            int periodX, int periodY,
                            int[] bands) {
        imagePanel.setImage(theImage);
        Thread.currentThread().yield();
    }
    
    public void imageUpdate(ImageReader source,
                            BufferedImage theImage,
                            int minX, int minY,
                            int width, int height,
                            int periodX, int periodY,
                            int[] bands) {
        imagePanel.repaint(minX, minY, width, height);
        Thread.currentThread().yield();
    }
    
    public void passComplete(ImageReader source,
                             BufferedImage theImage) {
        imagePanel.repaint(0, 0,
                           theImage.getWidth(), theImage.getHeight());
        Thread.currentThread().yield();
    }
    
    public void thumbnailPassStarted(ImageReader source,
                                     BufferedImage theThumbnail,
                                     int pass,
                                     int minPass, int maxPass,
                                     int minX, int minY,
                                     int periodX, int periodY,
                                     int[] bands) {
    }
    
    public void thumbnailUpdate(ImageReader source,
                                BufferedImage theThumbnail,
                                int minX, int minY,
                                int width, int height,
                                int periodX, int periodY,
                                int[] bands) {
    }
    
    public void thumbnailPassComplete(ImageReader source,
                                      BufferedImage theThumbnail) {
    }

    private static boolean isLocked = false;
    
    private synchronized void getMutex() {
        while (isLocked) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        isLocked = true;
    }
    
    private synchronized void releaseMutex() {
        isLocked = false;
        notifyAll();
    }
    
    public void run() {
        // Only allow one thread to run at a time
        getMutex();
        
        long startTime = System.currentTimeMillis();
        BufferedImage bi = null;
        try {
            reader.addIIOReadUpdateListener(this);
            bi = reader.read(imageIndex, param);
            reader.removeIIOReadUpdateListener(this);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(0);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Read image in " +
                           (endTime - startTime) + " ms");
        imagePanel.setImage(bi);
        
        releaseMutex();
    }
}
