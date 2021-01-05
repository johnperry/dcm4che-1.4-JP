package org.dcm4che.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UIDGeneratorTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(UIDGeneratorTest.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(UIDGeneratorTest.class);
        return suite;
    }

    public UIDGeneratorTest(String name) {
        super(name);
    }
    
    public void testCreateUID() {
        String uid = UIDGenerator.getInstance().createUID();
        assertEquals(true, uid.matches("[0-2]((\\.0)|(\\.[1-9][0-9]*))*"));
        assertEquals(true, uid.length() <= 64);
    }

}
