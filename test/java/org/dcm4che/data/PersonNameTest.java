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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4che.data;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 4106 $ $Date: 2007-05-03 13:08:35 +0200 (Do, 03 Mai 2007) $
 * @since May 3, 2007
 */
public class PersonNameTest extends TestCase {

    public PersonNameTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PersonNameTest.class);
        return suite;
    }
    
    public void testToComponentGroupMatch() {
        DcmObjectFactory f = DcmObjectFactory.getInstance();
        assertEquals("Patient^*", f.newPersonName("Patient").toComponentGroupMatch());
        assertEquals("Pat*", f.newPersonName("Pat*").toComponentGroupMatch());
        assertEquals("Patient^Name^*", f.newPersonName("Patient^Name").toComponentGroupMatch());
        assertEquals("Patient^N*", f.newPersonName("Patient^N*").toComponentGroupMatch());
        assertEquals("Pat*^N*", f.newPersonName("Pat*^N*").toComponentGroupMatch());
        assertEquals("Pat*N*", f.newPersonName("Pat*N*").toComponentGroupMatch());
        assertEquals("Patient^Name^Name2^*", f.newPersonName("Patient^Name^Name2").toComponentGroupMatch());
        assertEquals("Patient^*^N*", f.newPersonName("Patient^^N*").toComponentGroupMatch());
        assertEquals("Patient^*^*N^*", f.newPersonName("Patient^^*N").toComponentGroupMatch());
        assertEquals("Patient^*^*N^Dr^*", f.newPersonName("Patient^^*N^Dr").toComponentGroupMatch());
        assertEquals("Patient^*^*N^*^Sen", f.newPersonName("Patient^^*N^^Sen").toComponentGroupMatch());
    }


}
