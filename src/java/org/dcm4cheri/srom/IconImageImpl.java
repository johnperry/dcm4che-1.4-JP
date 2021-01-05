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
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;

import org.dcm4che.srom.IconImage;

import java.util.Arrays;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class IconImageImpl implements IconImage {
    
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private final int rows;
    private final int columns;
    private final byte[] pixeldata;

    // Constructors --------------------------------------------------
    public IconImageImpl(int rows, int columns, byte[] pixeldata) {
        if (rows <= 0) {
            throw new IllegalArgumentException("Rows: " + rows);
        }
    
        if (columns <= 0) {
            throw new IllegalArgumentException("Columns: " + columns);
        }
        
        if (pixeldata != null && pixeldata.length != rows * columns) {
            throw new IllegalArgumentException("Length of Pixel Data ["
                + pixeldata.length + "] do not match Rows [" + rows
                + "] x Columns [" + columns + "]");
        }
    
        this.rows = rows;
        this.columns = columns;
        this.pixeldata = pixeldata != null
            ? pixeldata
            : new byte[rows * columns];
    }

    public IconImageImpl(Dataset ds) throws DcmValueException {
        this(ds.getInt(Tags.Rows, -1), ds.getInt(Tags.Columns, -1),
            ds.getByteBuffer(Tags.PixelData).array());
    }
    
    public static IconImage newIconImage(Dataset ds) throws DcmValueException {
        return ds != null ? new IconImageImpl(ds) : null;
    }
    
    // Public --------------------------------------------------------
    public String toString() {
        return "Icon[" + columns + "x" + rows + "]"; 
    }

    public void toDataset(Dataset ds) {
        ds.putUS(Tags.SamplesPerPixel, 1);
        ds.putCS(Tags.PhotometricInterpretation, "MONOCHROME2");
        ds.putUS(Tags.Rows, rows);
        ds.putUS(Tags.Columns, columns);
        ds.putUS(Tags.BitsAllocated, 8);
        ds.putUS(Tags.BitsStored, 8);
        ds.putUS(Tags.HighBit, 7);
        ds.putUS(Tags.PixelRepresentation, 0);
        ds.putOB(Tags.PixelData, pixeldata);
    }    
    
    public final boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof IconImageImpl))
            return false;
    
        IconImageImpl icon = (IconImageImpl)o;
        return icon.rows == rows && icon.columns == columns
            && Arrays.equals(icon.pixeldata, pixeldata);
    }
    
    public final int getRows() {
        return rows;
    }
    
    public final int getColumns() {
        return columns;
    }
    
    public final byte[] getPixelData() {
        return pixeldata;
    }
    
}
