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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

public class PropertiesPanel extends JPanel implements TableModelListener, MouseListener
{
    private final Logger log = Logger.getLogger(PrintSCUFrame.class);
    
    private Properties props;
    /*private String host;
    private String callingAET, calledAET;
    private int port;*/

    static final String[] KEYS =
    {
        "Host",
        "Port",
        "CalledAET",
        "CallingAET",
        "MaxPduSize", //ignored
        "Grouplens",  //ignored
        "SOP.Verification",
        "SOP.BasicGrayscalePrintManagement",
        "SOP.BasicColorPrintManagement",
        "SOP.BasicAnnotationBox",
        "SOP.BasicPrintImageOverlayBox",
        "SOP.PresentationLUT",
        "SOP.PrintJob",
        "SOP.PrinterConfigurationRetrieval",
        "Session.NumberOfCopies",
        "Session.PrintPriority",
        "Session.MediumType",
        "Session.FilmDestination",
        "Session.FilmSessionLabel",
        "Session.MemoryAllocation",
        "Session.OwnerID",
        "FilmBox.ImageDisplayFormat",
        "FilmBox.FilmOrientation",
        "FilmBox.FilmSizeID",
        "FilmBox.RequestedResolutionID",
        "FilmBox.AnnotationDisplayFormatID",
        "FilmBox.MagnificationType",
        "FilmBox.SmoothingType",
        "FilmBox.BorderDensity",
        "FilmBox.EmptyImageDensity",
        "FilmBox.MinDensity",
        "FilmBox.MaxDensity",
        "FilmBox.Trim",
        "FilmBox.ConfigurationInformation",
        "FilmBox.Illumination",
        "FilmBox.ReflectedAmbientLight",
        "ImageBox.Polarity",
        "ImageBox.MagnificationType",
        "ImageBox.SmoothingType",
        "ImageBox.MinDensity",
        "ImageBox.MaxDensity",
        "ImageBox.ConfigurationInformation",
        "ImageBox.RequestedDecimateCropBehavior",
        "ImageBox.RequestedImageSize",
        "LUT.Shape",
        "LUT.Gamma",
        //"LUT.Level",
        //"LUT.ScaleToFitBitDepth",
        //"LUT.ApplyBySCU",
        //"User.SendAspectRatio",
        //"User.RequestedZoom",
        //"User.BurnInInfo",
        "User.BurnInOverlays",
        "User.AutoScale",   //added this
        //"User.BurnInInfo.Properties",
        //"User.BitDepth",
        //"User.InflateBitsAlloc",
        //"User.MinMaxWindowing",
        "Verbose",
        //"DumpCmdsetIntoDir",
        //"DumpDatasetIntoDir",
    };

    private static final Properties DEFAULTS;

    static { //initialize some defaults
        DEFAULTS = new Properties();
        DEFAULTS.put("Host", "localhost");
        DEFAULTS.put("CallingAET", "PrintSCU");
        DEFAULTS.put("CalledAET", "TIANI_PRINT");
        DEFAULTS.put("Port", "6104");
        DEFAULTS.put("FilmBox.ImageDisplayFormat", "STANDARD\\1,1");
        DEFAULTS.put("FilmBox.AnnotationDisplayFormatID", "TITLE");
    }

    private static final String[] PRINT_PRIORITY = {
      "","HIGH","MED","LOW"
    };
    private static final String[] MEDIUM_TYPE = {
      "","PAPER","CLEAR FILM","BLUE FILM"
    };
    private static final String[] FILM_DESTINATION = {
      "","MAGAZINE","PROCESSOR","BIN_1","BIN_2","BIN_3","BIN_4","BIN_5","BIN_6","BIN_7","BIN_8"
    };
    private static final String[] IMAGE_DISPLAY_FORMAT = {
      "STANDARD\\1,1","STANDARD\\2,3","ROW\\2","COL\\2","SLIDE","SUPERSLIDE","CUSTOM\\1"
    };
    private static final String[] FILM_ORIENTATION = {
      "","PORTRAIT","LANDSCAPE"
    };
    private static final String[] FILM_SIZE_ID = {
      "","8INX10IN","10INX12IN","10INX14IN","11INX14IN","14INX14IN","14INX17IN","24CMX24CM","24CMX30CM"
    };
    private static final String[] MAGNIFICATION_TYPE= {
      "","REPLICATE","BILINEAR","CUBIC","NONE"
    };
    private static final String[] DENSITY = {
      "","BLACK","WHITE"
    };
    private static final String[] YES_NO = {
      "","YES","NO"
    };
    private static final String[] REQUESTED_RESOLUTION_ID = {
      "","STANDARD","HIGH"
    };
    private static final String[] POLARITY = {
      "","NORMAL","REVERSE"
    };
    private static final String[] REQUESTED_DECIMATE_CROP_BEHAVIOR = {
      "","DECIMATE","CROP","FAIL"
    };
    private static final String[] SEND_ASPECTRATIO = {
      "Always","IfNot1/1"
    };
    private static final String[] BURNIN_INFO = {
      "No", "IfNoOverlays", "Always"
    };

    static final int LUT_FILE = 0;
    static final int LUT_GAMMA = 1;
    static final int LUT_IDENTITY = 2;
    static final int LUT_LIN_OD = 3;
    static final int LUT_INVERSE = 4;

    private static final String[] LUT_SHAPE = {
      "<file>","<gamma>","IDENTITY","LIN OD","INVERSE"
    };
    private static final String[] LUT_LEVEL = {
      "FilmSession","FilmBox","ImageBox"
    };
    private static final String[] INFLATE_BIT_DEPTH = {
      "Always","IfNonLinear","No"
    };
    private static final String[] VERBOSE = {
      "0","1","2","3","4","5","6"
    };

    //maps property names to their corresponding Strng[] of enumerations for the UI
    private static final Map PROP_ENUMS;

    static {
        PROP_ENUMS = new HashMap();
        PROP_ENUMS.put("Session.FilmDestination", FILM_DESTINATION);
        PROP_ENUMS.put("Session.FilmOrientation", FILM_ORIENTATION);
        PROP_ENUMS.put("Session.MediumType", MEDIUM_TYPE);
        PROP_ENUMS.put("Session.PrintPriority", PRINT_PRIORITY);
        PROP_ENUMS.put("FilmBox.ImageDisplayFormat", IMAGE_DISPLAY_FORMAT);
        PROP_ENUMS.put("FilmBox.FilmSizeID", FILM_SIZE_ID);
        PROP_ENUMS.put("FilmBox.MagnificationType", MAGNIFICATION_TYPE);
        PROP_ENUMS.put("FilmBox.RequestedResolutionID", REQUESTED_RESOLUTION_ID);
        PROP_ENUMS.put("ImageBox.RequestedDecimateCropBehavior",
            REQUESTED_DECIMATE_CROP_BEHAVIOR);
        PROP_ENUMS.put("ImageBox.Polarity", POLARITY);
        PROP_ENUMS.put("User.SendAspectRatio", SEND_ASPECTRATIO);
        PROP_ENUMS.put("User.BurnInInfo", BURNIN_INFO);
        PROP_ENUMS.put("User.BurnInOverlays", YES_NO);
        PROP_ENUMS.put("User.AutoScale", YES_NO);
        PROP_ENUMS.put("Verbose", VERBOSE);
    }

    private PrintSCUFrame printSCUFrame;
    private JTable table;
    private TableModel model;

    PropertiesPanel(PrintSCUFrame printSCUFrame, String fileName)
    {
        super();
        this.printSCUFrame = printSCUFrame;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        //load properties file
        File file = new File(fileName);
        props = loadProperties(file);
        //fill properties table
        table = new JTable(KEYS.length, 2);
        for (int i = 0; i < KEYS.length; i++) {
            table.setValueAt(KEYS[i], i, 0);
            table.setValueAt(props.getProperty(KEYS[i]), i, 1);
        }
        model = table.getModel();
        model.addTableModelListener(this);
        table.addMouseListener(this);
        add(table);
    }

    public void mouseClicked(MouseEvent e)
    {
        int column = table.getSelectedColumn();
        int row = table.getSelectedRow();
        if (column == 0) {
            String prop = (String)table.getValueAt(row, 0);
            String value = props.getProperty(prop);
            String[] choices = (String[])PROP_ENUMS.get(prop);
            if (choices == null)
                return;
            value = (String)JOptionPane.showInputDialog(printSCUFrame,
                "Choose a value:", prop, JOptionPane.QUESTION_MESSAGE,
                null, choices, choices[0]);
            if (value != null) {
                table.setValueAt(value, row, 1);
                if (value.equals(""))
                    props.remove(prop);
                else
                    props.setProperty(prop, value);
            }
        }
    }
    public void mouseExited(MouseEvent e)
    {
    }
    public void mouseEntered(MouseEvent e)
    {
    }
    public void mousePressed(MouseEvent e)
    {
    }
    public void mouseReleased(MouseEvent e)
    {
    }

    public void tableChanged(TableModelEvent e)
    {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (column != 1)
            return;
        String prop = (String)model.getValueAt(row, 0);
        String data = (String)model.getValueAt(row, 1);
        props.put(prop, data);
        printSCUFrame.propertyChanged(prop);
    }

    private Properties loadProperties(File file) {
        Properties props = new Properties(DEFAULTS);
        try {
            props.load(new BufferedInputStream(new FileInputStream(file)));
        }
        catch (FileNotFoundException e) {
            log.warn("No properties file was found, using default settings");
        }
        catch (IOException e) {
            log.warn("Can not load properties file");
        }
        return props;
    }

    String getProperty(String key)
    {
        return props.getProperty(key);
    }
}
