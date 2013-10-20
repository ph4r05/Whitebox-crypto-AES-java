/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.AES;
import cz.muni.fi.xklinec.whiteboxAES.State;
import cz.muni.fi.xklinec.whiteboxAES.W32b;
import cz.muni.fi.xklinec.whiteboxAES.generator.Generator.Coding;
import java.util.Arrays;
import java.util.Random;
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
        
        // Generate T1 tables - testing mainly this
        g.generateT1Tables();
        
        // No exception here means that it was OK. More complex testing would
        // be very complicated to implement...
    }

    /**
     * Test of generate method, of class Generator.
     */
    public void testGenerate() {
        System.out.println("generate");
        Generator g = new Generator();
        Random rand = new Random();
        
        // External encoding is needed, at least some, generate identities
        ExternalBijections extc = new ExternalBijections();
        g.generateExtEncoding(extc, Generator.WBAESGEN_EXTGEN_ID);
        
        // at first generate pure table AES implementation
        g.setUseIO04x04Identity(true);
        g.setUseIO08x08Identity(true);
        g.setUseMB08x08Identity(true);
        g.setUseMB32x32Identity(true);
        
        // test with testvectors
        g.generate(true, AEShelper.testVect128_key, 16, extc);
        AES AESi = g.getAESi();
        
        // Initialize structures for AES testing
        int r, i, t;
	W32b  ires[] = new W32b[AES.BYTES];	// intermediate result for T2,T3-boxes
	State ares[] = new State[AES.BYTES];	// intermediate result for T1-boxes
        for(i=0; i<AES.BYTES; i++){
            ires[i] = new W32b();
            ares[i] = new State();
        }
        
        //
        // T1 tables + XOR cascade has to be identity
        //
        for(t=0; t<AEShelper.AES_TESTVECTORS; t++){
            State state  = new State(AEShelper.testVect128_plain[t], true);
            // At first we have to put input to T1 boxes directly, no shift rows
            // compute result to ares[16]
            for(i=0; i<AES.BYTES; i++){
                // Note: Tbox is indexed by cols, state by rows - transpose needed here
                ares[i].loadFrom(AESi.getT1()[0][i].lookup(state.get(i)) );
            }

            // now compute XOR cascade from 16 x 128bit result after T1 application.
            AESi.getXorState()[0].xor(ares);
            state.loadFrom(ares[0]);
            
            final byte[] stateRes = state.getState();
            assertEquals("T1 + Xor cascade is not identity and should be here", true, Arrays.equals(stateRes, AEShelper.testVect128_plain[t]));
        }
        
        //
        // Now T3 table + particular XOR box has to be identity!
        //
        for(t=0; t<100; t++){
            for(r=0; r<AES.ROUNDS-1; r++){                
                // test all T3 boxes
                for(i=0; i<State.COLS; i++){       
                    // generate random long for T3 identity testing
                    byte[] cires = new byte[4];
                    rand.nextBytes(cires);
                    
                    // Apply T3 boxes, valid XOR results are in ires[0], ires[4], ires[8], ires[12]
                    // Start from the end, because in ires[i] is our XORing result.
                    ires[12+i].set(AESi.getT3()[r][12+i].lookup(cires[3]));
                    ires[ 8+i].set(AESi.getT3()[r][ 8+i].lookup(cires[2]));
                    ires[ 4+i].set(AESi.getT3()[r][ 4+i].lookup(cires[1]));
                    ires[ 0+i].set(AESi.getT3()[r][ 0+i].lookup(cires[0]));

                    // Apply final XOR cascade after T3 box
                    ires[i].set(AESi.getXor()[r][2*i+1].xor(
                        ires[ 0+i].getLong(), 
                        ires[ 4+i].getLong(), 
                        ires[ 8+i].getLong(), 
                        ires[12+i].getLong()));

                    // assert equality - T3+xor identity
                    assertEquals("T3 box should be identity but is not", true, Arrays.equals(cires, ires[i].get()));
                }
            }
        }
        
        //
        // Test whole AES on test vectors
        //
        for(i=0; i<AEShelper.AES_TESTVECTORS; i++){
            State plain  = new State(AEShelper.testVect128_plain[i], true, true);
            State state  = new State(AEShelper.testVect128_plain[i], true, true);
            State cipher = new State(AEShelper.testVect128_cipher[i], true, false);
            
            AESi.crypt(state);
            
            System.out.println("Testvector index: " + i);
            System.out.println("=====================");
            System.out.println("Testvector plaintext: \n" + plain);
            System.out.println("Testvector ciphertext: \n"+ cipher);
            System.out.println("Enc(plaintext_test): \n" + state);
            
            assertEquals("Cipher output mismatch", true, state.equals(cipher));
        }
    }   
}
