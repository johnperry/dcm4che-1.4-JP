import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
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

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 3997 $ $Date: 2006-05-22 13:41:19 +0200 (Mo, 22 Mai 2006) $
 * @since May 22, 2006
 */
public class ListIIORegistry {

    private static final IIORegistry theRegistry =
        IIORegistry.getDefaultInstance();

    /**
     * @param args
     */
    public static void main(String[] args) {
        printImageReader();
        printImageWriter();
        printServiceProviders();
    }

    private static void printImageReader() {
        String[] formats = ImageIO.getReaderFormatNames();
        for (int i = 0; i < formats.length; i++) {
            System.out.println("\nIMAGE READERS FOR FORMAT " + formats[i]); 
             for (Iterator it = ImageIO.getImageReadersByFormatName(formats[i]);
                     it.hasNext();) {
                 System.out.println(it.next().getClass().getName());                
            }
        }        
    }

    private static void printImageWriter() {
        String[] formats = ImageIO.getWriterFormatNames();
        for (int i = 0; i < formats.length; i++) {
            System.out.println("\nIMAGE WRITERS FOR FORMAT " + formats[i]); 
             for (Iterator it = ImageIO.getImageWritersByFormatName(formats[i]);
                     it.hasNext();) {
                 System.out.println(it.next().getClass().getName());                
            }
        }        
    }

    private static void printServiceProviders() {
        for (Iterator iter = theRegistry.getCategories(); iter.hasNext();) {
            Class category = (Class) iter.next();
            System.out.println("\nCATEGORY " + category.getName());
            for (Iterator it = theRegistry.getServiceProviders(category, true);
                    it.hasNext();) {
                System.out.println(it.next().getClass().getName());                
            }
        }
    }

}
