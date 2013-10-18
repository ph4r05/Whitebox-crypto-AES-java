/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.AES;
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
     * Test of build method, of class AEShelper.
     */
    public void testKeySchedule() {
        System.out.println("keySchedule");
        AEShelper a = new AEShelper();
        a.build(true);
        
        // test sample key schedule
        byte[] roundKey = a.keySchedule(AEShelper.testVect128_key, 16, false);
        
        // test copy of key
        for(int i=0; i<16; i++){
            assertEquals("Key schedule is invalid", AEShelper.testVect128_key[i],  roundKey[i]);
        }
        
        // test key schedule for the last round. Alg. is iterative and current 
        // round depends on the last one -> it is enough to test the last one.
        // Source: http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf
        final byte[] roundKeyFinal = new byte[] {
            (byte)0xd0, (byte)0x14, (byte)0xf9, (byte)0xa8,
            (byte)0xc9, (byte)0xee, (byte)0x25, (byte)0x89,
            (byte)0xe1, (byte)0x3f, (byte)0x0c, (byte)0xc8,
            (byte)0xb6, (byte)0x63, (byte)0x0c, (byte)0xa6
        };
        
        for(int i=0; i<16; i++){
            assertEquals("Key schedule is invalid; last round check", 
                    roundKeyFinal[i],
                    roundKey[AES.BYTES * AES.ROUNDS + i]);
        }
    }
}
