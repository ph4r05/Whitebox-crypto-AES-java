/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.AES;
import cz.muni.fi.xklinec.whiteboxAES.generator.Generator.Coding;
import junit.framework.TestCase;

/**
 *
 * @author ph4r05
 */
public class GeneratorTest extends TestCase {
    
    public GeneratorTest(String testName) {
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
     * Test of POS_MOD method, of class Generator.
     */
    public void testPOS_MOD() {
        System.out.println("POS_MOD");
        int result;
        result = Generator.POS_MOD(-1, 8);
        assertEquals(7, result);
    }

    /**
     * Test of generateExtEncoding method, of class Generator.
     */
    public void testGenerateExtEncoding() {
        System.out.println("generateExtEncoding");
        ExternalBijections extc = new ExternalBijections();
        Generator instance = new Generator();
        instance.generateExtEncoding(extc, 0);
        
        //
        // Test invertibility of matrix
        //
        final LinearBijection[] IODM = extc.getIODM();
        final int ln = IODM.length;
        for(int i=0; i<ln; i++){
            GF2MatrixEx r1 = (GF2MatrixEx) IODM[i].getMb().rightMultiply(IODM[i].getInv());
            assertEquals("Matrix should be unit", true, NTLUtils.isUnit(r1));
            assertEquals("Matrix size mismatch", AES.BYTES*8, IODM[i].getMb().getNumRows());
            assertEquals("Matrix size mismatch", AES.BYTES*8, IODM[i].getMb().getNumColumns());
        }
        
        //
        // Test bijection invertibility
        //
        final Bijection4x4[][] lfC = extc.getLfC();
        final int ln0 = lfC.length;
        final int ln1 = lfC[0].length;
        for(int i=0; i<ln0; i++){
            for(int j=0; j<ln1; j++){
                for(int b=0; b<16; b++){
                    int k = lfC[i][j].coding[b];
                    assertEquals("Bijection is invalid, b^{-1}(b(x))!=x", b, lfC[i][j].invCoding[k]);
                } 
            }
        }
    }

    /**
     * Test of generateT1Tables method, of class Generator.
     */
    public void testGenerateT1Tables() {
        System.out.println("generateT1Tables");
        Generator g = new Generator();
        
        // Initialize internal
        g.initInternal();
        
        // External encoding is needed, at least some, generate identities
        ExternalBijections extc = new ExternalBijections();
        g.generateExtEncoding(extc, Generator.WBAESGEN_EXTGEN_ID);
        g.setExtc(extc);
        
        // 
        g.generateT1Tables();
        
    }

    /**
     * Test of generateXorTable method, of class Generator.
     */
    public void testGenerateXorTable() {
        System.out.println("generateXorTable");
        Coding xorCoding = null;
        byte[] xtb = null;
        Bijection4x4[] bio = null;
        Generator.generateXorTable(xorCoding, xtb, bio);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generateXorCascades method, of class Generator.
     */
    public void testGenerateXorCascades() {
        System.out.println("generateXorCascades");
        Generator instance = new Generator();
        instance.generateXorCascades();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generateXorStateCascades method, of class Generator.
     */
    public void testGenerateXorStateCascades() {
        System.out.println("generateXorStateCascades");
        Generator instance = new Generator();
        instance.generateXorStateCascades();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generate method, of class Generator.
     */
    public void testGenerate() {
        System.out.println("generate");
        boolean encrypt = false;
        byte[] key = null;
        int keySize = 0;
        ExternalBijections ex = null;
        Generator instance = new Generator();
        instance.generate(encrypt, key, keySize, ex);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }   
}
