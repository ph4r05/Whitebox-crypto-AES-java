/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import junit.framework.TestCase;

/**
 *
 * @author ph4r05
 */
public class AESCodingMapTest extends TestCase {
    
    public AESCodingMapTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of generateCodingMap method, of class AESCodingMap.
     */
    public void testGenerateCodingMap() {
        System.out.println("generateCodingMap");
        AESCodingMap i = new AESCodingMap();
        i.init();
        i.generateCodingMap();
       
        // Complex testing of this method would be very complicated so for now
        // it is enough that no assert was triggered by invalid allocation
        // or connection to non-existing bijection.
    }
}
