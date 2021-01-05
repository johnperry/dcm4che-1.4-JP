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

package org.dcm4che.tools.printscu;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dcm4che.client.AssociationRequestor;
import org.dcm4che.client.PrintSCU;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.util.UIDGenerator;

public class PrintSCUFrame extends JFrame
{
    public static final String DEFAULT_PROPERTIES_FILE = "PrintSCU.properties";

    private static final int DEF_WIDTH = 600, DEF_HEIGHT = 500;

    private final Logger log = Logger.getLogger(PrintSCUFrame.class);

    private AssociationRequestor assocRq = new AssociationRequestor();
    private PrintSCU printSCU;
    private String curPLutUid;
    private int nextImageBoxIndex;
    private int nextAnnIndex;
    private boolean colorMode = false;
    private boolean applySeparatePresState = false;
    private Action actConnect, actRelease, actCreateFilmSession, actDeleteFilmSession,
        actCreateFilmBox, actDeleteFilmBox, actCreateImageBox, actCreatePlut, actCreateAnnotation,
        actDeletePlut, actPrintFilmSession, actPrintFilmBox, actExit;
    private File lastFile = null; //for JFileChooser to remember last dir
    private JFileChooser chooser = new JFileChooser();
    private DcmObjectFactory dcmFactory = DcmObjectFactory.getInstance();
    private UIDGenerator uidGen = UIDGenerator.getInstance();
    private JSplitPane panel;
    private JPanel btnPanel;
    private PropertiesPanel propPanel;

    public static final class PrintSCUConfigurationException extends RuntimeException
    {
        PrintSCUConfigurationException() { super(); }
        PrintSCUConfigurationException(String msg) { super(msg); }
    }

    PrintSCUFrame()
    {
        Container contentPane = this.getContentPane();
        btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(2, 3));
        propPanel = new PropertiesPanel(this, DEFAULT_PROPERTIES_FILE);
        JScrollPane scrollingPanel = new JScrollPane(propPanel);
        contentPane.add(panel = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, btnPanel, scrollingPanel));
        btnPanel.setMinimumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT/4));
        propPanel.setMinimumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT/8));
        //Main Menus
        JMenuBar mnubar = new JMenuBar();
        setJMenuBar(mnubar);
        JMenu mnuFile = new JMenu("File");
        mnubar.add(mnuFile);
        // File menu
        actExit = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            };
        actExit.putValue(Action.NAME,"Exit");
        JMenuItem mnuExit = new JMenuItem(actExit);
        mnuFile.add(mnuExit);
        //set size
        setSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));

        //Print SCP related actions
        
        //Connect
        actConnect = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                //connect
                Integer anInt;
                String aString;
                
                try {
                    if ((anInt = getIntegerFromProperty("MaxPduSize")) != null)
                        assocRq.setMaxPDULength(anInt.intValue());
                    if ((aString = getStringFromProperty("CallingAET")) == null)
                        throw new PrintSCUConfigurationException();
                    assocRq.setCallingAET(aString);
                    if ((aString = getStringFromProperty("CalledAET")) == null)
                        throw new PrintSCUConfigurationException();
                    assocRq.setCalledAET(aString);
                    if ((aString = getStringFromProperty("Host")) == null)
                        throw new PrintSCUConfigurationException();
                    assocRq.setHost(aString);
                    if ((anInt = getIntegerFromProperty("Port")) == null)
                        throw new PrintSCUConfigurationException();
                    assocRq.setPort(anInt.intValue());
                }
                catch (PrintSCUConfigurationException e1) {
                    JOptionPane.showMessageDialog(PrintSCUFrame.this, e1);
                }
                
                printSCU = new PrintSCU(assocRq);
                printSCU.setAutoRefPLUT(true); //always create P-LUT when Film Box is created
                printSCU.setCreateRQwithIUID(true);
                printSCU.setNegotiatePLUT(true);
                printSCU.setNegotiateAnnotation(true);
                printSCU.setNegotiateColorPrint(colorMode);
                printSCU.setNegotiateGrayscalePrint(!colorMode);
                curPLutUid = new String();
                try {
                    assocRq.connect();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                    return;
                }
                setEnabled(false);
                actCreateFilmSession.setEnabled(true);
                actRelease.setEnabled(true);
            }
        };
        actConnect.putValue(Action.NAME, "Connect");

        //Release
        actRelease = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                //release
                try {
                    assocRq.release();
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                printSCU = null;
                onDisconnect();
                actConnect.setEnabled(true);
            }
        };
        actRelease.putValue(Action.NAME, "Release");

        //Create Session
        actCreateFilmSession = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                Dataset attr = dcmFactory.newDataset();
                String prop;
                Integer propInt;
                
                if ((propInt = getIntegerFromProperty("Session.NumberOfCopies")) != null)
                    attr.putIS(Tags.NumberOfCopies, propInt.intValue());
                if ((prop = getStringFromProperty("Session.PrintPriority")) != null)
                    attr.putCS(Tags.PrintPriority, prop);
                if ((prop = getStringFromProperty("Session.MediumType")) != null)
                    attr.putCS(Tags.MediumType, prop);
                if ((prop = getStringFromProperty("Session.FilmDestination")) != null)
                    attr.putCS(Tags.FilmDestination, prop);
                if ((prop = getStringFromProperty("Session.FilmSessionLabel")) != null)
                    attr.putLO(Tags.FilmSessionLabel, prop);
                if ((propInt = getIntegerFromProperty("Session.MemoryAllocation")) != null)
                    attr.putIS(Tags.MemoryAllocation, propInt.intValue());
                if ((prop = getStringFromProperty("Session.OwnerID")) != null)
                    attr.putSH(Tags.OwnerID, prop);

                //dump to log
                dump(attr, "Film Session");
                
                try {
                    printSCU.createFilmSession(attr, colorMode);
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                actCreateFilmBox.setEnabled(true);
                actCreatePlut.setEnabled(true);
                setEnabled(false);
                actDeleteFilmSession.setEnabled(true);
            }
        };
        actCreateFilmSession.putValue(Action.NAME, "Create FilmSession");

        //Create FilmBox
        actCreateFilmBox = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                Dataset attr = dcmFactory.newDataset();
                String prop;
                Integer propInt;
                
                if ((prop = getStringFromProperty("FilmBox.ImageDisplayFormat")) != null)
                    attr.putST(Tags.ImageDisplayFormat, prop);
                if ((prop = getStringFromProperty("FilmBox.FilmOrientation")) != null)
                    attr.putCS(Tags.FilmOrientation, prop);
                if ((prop = getStringFromProperty("FilmBox.FilmSizeID")) != null)
                    attr.putCS(Tags.FilmSizeID, prop);
                if ((prop = getStringFromProperty("FilmBox.RequestedResolutionID")) != null)
                    attr.putCS(Tags.RequestedResolutionID, prop);
                if ((prop = getStringFromProperty("FilmBox.AnnotationDisplayFormatID")) != null)
                    attr.putCS(Tags.AnnotationDisplayFormatID, prop);
                if ((prop = getStringFromProperty("FilmBox.MagnificationType")) != null)
                    attr.putCS(Tags.MagnificationType, prop);
                if ((prop = getStringFromProperty("FilmBox.SmoothingType")) != null)
                    attr.putCS(Tags.SmoothingType, prop);
                if ((prop = getStringFromProperty("FilmBox.BorderDensity")) != null)
                    attr.putCS(Tags.BorderDensity, prop);
                if ((prop = getStringFromProperty("FilmBox.EmptyImageDensity")) != null)
                    attr.putCS(Tags.EmptyImageDensity, prop);
                if ((propInt = getIntegerFromProperty("FilmBox.MinDensity")) != null)
                    attr.putUS(Tags.MinDensity, propInt.intValue());
                if ((propInt = getIntegerFromProperty("FilmBox.MaxDensity")) != null)
                    attr.putUS(Tags.MaxDensity, propInt.intValue());
                if ((prop = getStringFromProperty("FilmBox.Trim")) != null)
                    attr.putCS(Tags.Trim, prop);
                if ((prop = getStringFromProperty("FilmBox.ConfigurationInformation")) != null)
                    attr.putST(Tags.ConfigurationInformation, prop);
                if ((propInt = getIntegerFromProperty("FilmBox.Illumination")) != null)
                    attr.putUS(Tags.Illumination, propInt.intValue());
                if ((propInt = getIntegerFromProperty("FilmBox.ReflectedAmbientLight")) != null)
                    attr.putUS(Tags.ReflectedAmbientLight, propInt.intValue());

                //dump to log
                dump(attr, "Film Box");
                
                try {
                    printSCU.createFilmBox(attr);
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                nextImageBoxIndex = 0;
                nextAnnIndex = 0;
                actCreateImageBox.setEnabled(true);
                setEnabled(false);
                actDeleteFilmBox.setEnabled(true);
                actCreatePlut.setEnabled(false);
                actDeletePlut.setEnabled(false);
                actCreateAnnotation.setEnabled(true);
            }
        };
        actCreateFilmBox.putValue(Action.NAME, "Create FilmBox");

        //Create ImageBox
        actCreateImageBox = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                File file, psFile = null;
                if (chooser.showOpenDialog(PrintSCUFrame.this)
                        == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    Dataset attr = dcmFactory.newDataset();
                    String prop;
                    Integer propInt;
                    String configInfo;

                    if ((prop = getStringFromProperty("FilmBox.Polarity")) != null)
                        attr.putCS(Tags.Polarity, prop);
                    if ((prop = getStringFromProperty("FilmBox.MagnificationType")) != null)
                        attr.putCS(Tags.MagnificationType, prop);
                    if ((prop = getStringFromProperty("FilmBox.SmoothingType")) != null)
                        attr.putCS(Tags.SmoothingType, prop);
                    if ((propInt = getIntegerFromProperty("FilmBox.MinDensity")) != null)
                        attr.putUS(Tags.MinDensity, propInt.intValue());
                    if ((propInt = getIntegerFromProperty("FilmBox.MaxDensity")) != null)
                        attr.putUS(Tags.MaxDensity, propInt.intValue());
                    if ((prop = getStringFromProperty("FilmBox.RequestedDecimateCropBehavior")) != null)
                        attr.putCS(Tags.RequestedDecimateCropBehavior, prop);
                    if ((prop = getStringFromProperty("FilmBox.RequestedImageSize")) != null)
                        attr.putDS(Tags.RequestedImageSize, prop);
                    configInfo = getStringFromProperty("FilmBox.ConfigurationInformation");
                    
                    try {
                        if (curPLutUid == null) {
                            if ((prop = getStringFromProperty("LUT.Gamma")) != null) {
                                if (configInfo == null)
                                    configInfo = "gamma=" + prop;
                                else
                                    configInfo = configInfo + "\\gamma=" + prop;
                            }
                            else if ((prop = getStringFromProperty("LUT.Shape")) != null) {
                                curPLutUid = printSCU.createPLUT(prop);
                            }
                            else
                                throw new PrintSCUConfigurationException(
                                    "You need to either create a P-LUT, set LUT.Shape, or LUT.Gamma");
                        }
                        //finally write config info (with the plut gamma placed, if it exists)
                        if (configInfo != null)
                            attr.putST(Tags.ConfigurationInformation, configInfo);
                        //dump to log
                        dump(attr, "Image Box");
                        //create image box
                        Boolean burnInOverlays = getBooleanFromProperty("User.BurnInOverlays"),
                                autoScale = getBooleanFromProperty("User.AutoScale");
                        if (applySeparatePresState
                            && chooser.showOpenDialog(PrintSCUFrame.this)
                                == JFileChooser.APPROVE_OPTION) {
                            psFile = chooser.getSelectedFile();
                        }
                        printSCU.setImageBox(nextImageBoxIndex++, file, psFile, attr,
                            (burnInOverlays != null) ? burnInOverlays.booleanValue() : false,
                            (autoScale != null) ? autoScale.booleanValue() : true);
                    }
                    catch (PrintSCUConfigurationException e1) {
                        JOptionPane.showMessageDialog(PrintSCUFrame.this, e1);
                    }
                    catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        return;
                    }
                    catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        return;
                    }
                    catch (DcmServiceException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        return;
                    }
                    actPrintFilmSession.setEnabled(true);
                    actPrintFilmBox.setEnabled(true);
                    if (nextImageBoxIndex >= printSCU.countImageBoxes())
                        setEnabled(false);
                }
            }
        };
        actCreateImageBox.putValue(Action.NAME, "Add ImageBox");

        //Create Annotation
        actCreateAnnotation = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                Dataset attr = dcmFactory.newDataset();
                
                String text = (String)JOptionPane.showInputDialog(PrintSCUFrame.this,
                    "Enter annotation text:", "Text" + (nextAnnIndex + 1));
                if (text == null)
                    return;

                try {
                    printSCU.setAnnotationBox(nextAnnIndex++, text);
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (nextAnnIndex >= printSCU.countAnnotationBoxes())
                    setEnabled(false);
            }
        };
        actCreateAnnotation.putValue(Action.NAME, "Add Annotation");

        //Create P-LUT
        actCreatePlut = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                String shape;
                Dataset ds = dcmFactory.newDataset();
                
                if (chooser.showOpenDialog(PrintSCUFrame.this) != JFileChooser.APPROVE_OPTION)
                    return;
                File file = chooser.getSelectedFile();
                try {
                    DcmParser parser = DcmParserFactory.getInstance().newDcmParser(
                        new BufferedInputStream(new FileInputStream(file)));
                    parser.setDcmHandler(ds.getDcmHandler());
                    parser.parseDcmFile(null, -1);
                    if (ds.vm(Tags.PresentationLUTSeq) == -1)
                        throw new IOException();
                }
                catch (FileNotFoundException e1) {
                    JOptionPane.showMessageDialog(PrintSCUFrame.this,
                        "Could not open file: " + file);
                    return;
                }
                catch (IOException e1) {
                    JOptionPane.showMessageDialog(PrintSCUFrame.this,
                        "Could not read file: " + file);
                    return;
                }
                
                try {
                    curPLutUid = printSCU.createPLUT(ds);
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                setEnabled(false);
                actDeletePlut.setEnabled(true);
            }
        };
        actCreatePlut.putValue(Action.NAME, "Create P-LUT");

        //Delete FilmSession
        actDeleteFilmSession = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    printSCU.deleteFilmSession();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                setEnabled(false);
                actCreateFilmBox.setEnabled(false);
                actDeleteFilmBox.setEnabled(false);
                actCreateImageBox.setEnabled(false);
                actCreatePlut.setEnabled(false);
                actDeletePlut.setEnabled(false);
                actCreateAnnotation.setEnabled(false);
                actPrintFilmSession.setEnabled(false);
                actPrintFilmBox.setEnabled(false);
                actCreateFilmSession.setEnabled(true);
            }
        };
        actDeleteFilmSession.putValue(Action.NAME, "Delete FilmSession");

        //Delete FilmBox
        actDeleteFilmBox = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    printSCU.deleteFilmBox();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                setEnabled(false);
                actCreateFilmBox.setEnabled(true);
                actCreateImageBox.setEnabled(false);
                actPrintFilmBox.setEnabled(false);
                actPrintFilmSession.setEnabled(false);
                actCreatePlut.setEnabled(true);
                actDeletePlut.setEnabled(true);
                actCreateAnnotation.setEnabled(false);
            }
        };
        actDeleteFilmBox.putValue(Action.NAME, "Delete FilmBox");

        //Delete P-LUT
        actDeletePlut = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    printSCU.deletePLUT(curPLutUid);
                    curPLutUid = null;
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                setEnabled(false);
                actCreatePlut.setEnabled(true);
            }
        };
        actDeletePlut.putValue(Action.NAME, "Delete P-LUT");

        //Print FilmSession
        actPrintFilmSession = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    printSCU.printFilmSession();
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
            }
        };
        actPrintFilmSession.putValue(Action.NAME, "Print FilmSession");

        //Print FilmBox
        actPrintFilmBox = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    printSCU.printFilmBox();
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
            }
        };
        actPrintFilmBox.putValue(Action.NAME, "Print FilmBox");
        
        //disable all buttons
        onDisconnect();
        
        //set up buttons for commands
        JPanel subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        subBtnPanel.add(new JLabel("Print Server"));
        JButton btnConnect = new JButton(actConnect);
        subBtnPanel.add(btnConnect);
        JButton btnRelease = new JButton(actRelease);
        subBtnPanel.add(btnRelease);
        
        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        subBtnPanel.add(new JLabel("Film Session"));
        JButton btnCreateFilmSession = new JButton(actCreateFilmSession);
        subBtnPanel.add(btnCreateFilmSession);
        JButton btnDeleteFilmSession = new JButton(actDeleteFilmSession);
        subBtnPanel.add(btnDeleteFilmSession);

        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        subBtnPanel.add(new JLabel("Presentation LUT"));
        JButton btnCreatePlut = new JButton(actCreatePlut);
        subBtnPanel.add(btnCreatePlut);
        JButton btnDeletePlut = new JButton(actDeletePlut);
        subBtnPanel.add(btnDeletePlut);
        
        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        subBtnPanel.add(new JLabel("Film Box"));
        JButton btnCreateFilmBox = new JButton(actCreateFilmBox);
        subBtnPanel.add(btnCreateFilmBox);
        JButton btnDeleteFilmBox = new JButton(actDeleteFilmBox);
        subBtnPanel.add(btnDeleteFilmBox);

        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        subBtnPanel.add(new JLabel("Image Box"));
        JButton btnCreateImageBox = new JButton(actCreateImageBox);
        subBtnPanel.add(btnCreateImageBox);
        JCheckBox chkUseSeparatePresState = new JCheckBox(new AbstractAction("Apply Presentation State")
            {
                public void actionPerformed(ActionEvent e)
                {
                    applySeparatePresState = !applySeparatePresState;
                }
            });
        chkUseSeparatePresState.setSelected(applySeparatePresState);
        chkUseSeparatePresState.setToolTipText("Enables you to choose a separate Presentation State object to apply to the chosen DICOM image");
        subBtnPanel.add(chkUseSeparatePresState);
        
        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        subBtnPanel.add(new JLabel("Annotation"));
        JButton btnCreateAnnotation = new JButton(actCreateAnnotation);
        subBtnPanel.add(btnCreateAnnotation);
        
        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        subBtnPanel.add(new JLabel("Print"));
        JButton btnPrintFilmSession = new JButton(actPrintFilmSession);
        subBtnPanel.add(btnPrintFilmSession);
        JButton btnPrintFilmBox = new JButton(actPrintFilmBox);
        subBtnPanel.add(btnPrintFilmBox);
        
        //update from all properties
        propertyChanged(null);
    }

    //propertyName == null, means all need to be updated
    public void propertyChanged(String propertyName)
    {
        //Verbose
        if (propertyName == null || "Verbose".equals(propertyName)) {
            Integer verbose;
            if ((verbose = getIntegerFromProperty("Verbose")) != null) {
                switch (verbose.intValue()) {
                    case 0:
                        log.setLevel(Level.OFF);
                        break;
                    case 1:
                        log.setLevel(Level.FATAL);
                        break;
                    case 2:
                        log.setLevel(Level.ERROR);
                        break;
                    case 3:
                        log.setLevel(Level.WARN);
                        break;
                    case 4:
                        log.setLevel(Level.INFO);
                        break;
                    case 5:
                        log.setLevel(Level.DEBUG);
                        break;
                    case 6:
                        log.setLevel(Level.ALL);
                        break;
                }
            }
            else
                log.setLevel(Level.WARN);
        }
    }

    PrintSCUFrame(String title)
    {
        this();
        setTitle(title);
    }

    protected void dump(Dataset ds, String from)
    {
        StringWriter out = new StringWriter();
        try {
            ds.dumpDataset(out, null);
        }
        catch (IOException ioe) {
            log.warn("Could not dump attributes for " + from);
        }
        log.info(out.toString());
    }

    protected String getStringFromProperty(String propertyName)
    {
        return (String)getFromProperty(propertyName, String.class);
    }
    protected Integer getIntegerFromProperty(String propertyName)
    {
        return (Integer)getFromProperty(propertyName, Integer.class);
    }
    protected Boolean getBooleanFromProperty(String propertyName)
    {
        return (Boolean)getFromProperty(propertyName, Boolean.class);
    }

    /* 
     * Passing an unknown Class (or missing property value) returns null to caller
     */
    private final Object getFromProperty(String propertyName, Class argType)
    {
        String prop;
        Object ret = null;
        
        if ((prop = propPanel.getProperty(propertyName)) != null) {
            try {
                if (argType == String.class)
                    ret = prop;
                else if (argType == Integer.class)
                    ret = Integer.valueOf(prop);
                else if (argType == Boolean.class)
                    ret = Boolean.valueOf("true".equalsIgnoreCase(prop)
                                          || "yes".equalsIgnoreCase(prop)
                                          || "1".equals(prop));
            }
            catch (NumberFormatException e) {
                log.warn(propertyName + " is an invalid number");
            }
        }
        if (ret != null) {
            log.debug("Setting property " + propertyName + " = " + ret);
        }
        return ret;
    }

    private void onDisconnect()
    {
        actRelease.setEnabled(false);
        actCreateFilmSession.setEnabled(false);
        actCreateFilmBox.setEnabled(false);
        actCreateImageBox.setEnabled(false);
        actCreatePlut.setEnabled(false);
        actCreateAnnotation.setEnabled(false);
        actPrintFilmSession.setEnabled(false);
        actPrintFilmBox.setEnabled(false);
        actDeleteFilmSession.setEnabled(false);
        actDeleteFilmBox.setEnabled(false);
        actDeletePlut.setEnabled(false);
    }

    /* for abnormal exit */
    private static void exit(String msg)
    {
        System.out.println(msg);
        System.out.println(USAGE);
        System.exit(1);
    }

    private final static String USAGE =
            "Usage: java -jar printSCU.jar [OPTIONS]\n\n" +
            "Connects to a DICOM Print Service Class Provider.\n" +
            "Options:\n" +
            " -h --help        show this help and exit\n";

    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        LongOpt[] longopts = {
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h')
            };

        Getopt g = new Getopt("printSCU", args, "t:a:h", longopts, true);
        try {
            int c;
            while ((c = g.getopt()) != -1) {
                switch (c) {
                    case 'h':
                    case '?':
                        exit("");
                        break;
                }
            }
            int optind = g.getOptind();
            int argc = args.length - optind;
            if (argc != 0) {
                exit("printSCU: wrong number of arguments\n");
            }
            PrintSCUFrame printSCU = new PrintSCUFrame("Print SCU Client");
            printSCU.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            printSCU.show();
        }
        catch (IllegalArgumentException e) {
            exit("printSCU: illegal argument - " + e.getMessage() + "\n");
        }
    }
}
