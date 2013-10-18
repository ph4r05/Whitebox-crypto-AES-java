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
public class AEShelperTest extends TestCase {
    
    public AEShelperTest(String testName) {
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
     * Test of build method, of class AEShelper.
     */
    public void testBuild() {
        System.out.println("build");
        AEShelper a = new AEShelper();
        a.build(true);
        
        // test AES S-box - some fixed tests
        assertEquals("S-box value mismatch", 0x63, a.ByteSub(0));
        assertEquals("S-box value mismatch", 0x76, a.ByteSub(0x0f));
        assertEquals("S-box value mismatch", 0x16, a.ByteSub(0xff));
        assertEquals("S-box value mismatch", 0x0e, a.ByteSub(0xd7));
        assertEquals("S-box value mismatch", 0x3c, a.ByteSub(0x6d));
        
        // S-Box inversion test.
        for(int i=0; i<256; i++){
            int b = a.ByteSub(i);
            b = a.ByteSubInv(b);
            assertEquals("S-box inversion value mismatch", i, b);
        }
        
        // RCON test
        for(int i=0; i<8; i++){
            assertEquals("RCON is invalid", 1<<i, a.RC[i]);
        }
        
        assertEquals("RCON is invalid", 0x1B, a.RC[8]);
        assertEquals("RCON is invalid", 0x36, a.RC[9]);
    }

    /**
     * Test of expandKey method, of class AEShelper.
     
    public void testExpandKey() {
        System.out.println("expandKey");
        GF2Vector expandedKey = null;
        byte[] key = null;
        int size = 0;
        boolean debug = false;
        AEShelper.expandKey(expandedKey, key, size, debug);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
}
