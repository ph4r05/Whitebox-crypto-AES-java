/*
 * Copyright (c) 2014, Dusan (Ph4r05) Klinec, Petr Svenda
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the copyright holders nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.AES;
import cz.muni.fi.xklinec.whiteboxAES.State;
import cz.muni.fi.xklinec.whiteboxAES.W32b;
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
     * Test of posMod method, of class Generator.
     */
    public void testPOS_MOD() {
        System.out.println("POS_MOD");
        int result;
        result = Generator.posMod(-1, 8);
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
    public void testGeneratePlain() {
        System.out.println("generatePlain");
        Generator gEnc  = new Generator();
        Generator gDec = new Generator();
        Random rand = new Random();
        
        // External encoding is needed, at least some, generate identities
        ExternalBijections extc = new ExternalBijections();
        gEnc.generateExtEncoding(extc, Generator.WBAESGEN_EXTGEN_ID);
        
        // at first generate pure table AES implementation
        gEnc.setUseIO04x04Identity(true);
        gEnc.setUseIO08x08Identity(true);
        gEnc.setUseMB08x08Identity(true);
        gEnc.setUseMB32x32Identity(true);
        
        gDec.setUseIO04x04Identity(true);
        gDec.setUseIO08x08Identity(true);
        gDec.setUseMB08x08Identity(true);
        gDec.setUseMB32x32Identity(true);
        
        // test with testvectors
        gEnc.generate(true,  AEShelper.testVect128_key, 16, extc);
        AES AESenc = gEnc.getAESi();
        // Decryption
        gDec.generate(false, AEShelper.testVect128_key, 16, extc);
        AES AESdec = gDec.getAESi();
        
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
                ares[i].loadFrom(AESenc.getT1()[0][i].lookup(state.get(i)) );
            }

            // now compute XOR cascade from 16 x 128bit result after T1 application.
            AESenc.getXorState()[0].xor(ares);
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
                    ires[12+i].set(AESenc.getT3()[r][12+i].lookup(cires[3]));
                    ires[ 8+i].set(AESenc.getT3()[r][ 8+i].lookup(cires[2]));
                    ires[ 4+i].set(AESenc.getT3()[r][ 4+i].lookup(cires[1]));
                    ires[ 0+i].set(AESenc.getT3()[r][ 0+i].lookup(cires[0]));

                    // Apply final XOR cascade after T3 box
                    ires[i].set(AESenc.getXor()[r][2*i+1].xor(
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
            State plain  = new State(AEShelper.testVect128_plain[i], true,  false);
            State state  = new State(AEShelper.testVect128_plain[i], true,  false);
            State cipher = new State(AEShelper.testVect128_cipher[i], true, false);
            
            System.out.println("Testvector index: " + i);
            System.out.println("=====================");
            System.out.println("Testvector plaintext: \n" + plain);
            System.out.println("Testvector ciphertext: \n"+ cipher);
            
            // Encrypt
            state.transpose();
            AESenc.crypt(state);
            System.out.println("Enc(plaintext_test): \n" + state);
            assertEquals("Cipher output mismatch", true, state.equals(cipher));
            
            // Decrypt
            cipher.transpose();
            AESdec.crypt(cipher);
            System.out.println("Dec(T(Enc(plaintext_test))): \n" + cipher);
            assertEquals("Cipher output mismatch", true, plain.equals(cipher));
        }
    }
    
    
    /**
     * Test of generate method, of class Generator.
     */
    public void testGenerate() {
        System.out.println("generate");
        Generator gEnc  = new Generator();
        Generator gDec = new Generator();
        Random rand = new Random();
        
        // External encoding is needed, at least some, generate identities
        ExternalBijections extc = new ExternalBijections();
        gEnc.generateExtEncoding(extc, 0);
        
        // at first generate pure table AES implementation
        gEnc.setUseIO04x04Identity(false);
        gEnc.setUseIO08x08Identity(false);
        gEnc.setUseMB08x08Identity(false);
        gEnc.setUseMB32x32Identity(false);
        
        gDec.setUseIO04x04Identity(false);
        gDec.setUseIO08x08Identity(false);
        gDec.setUseMB08x08Identity(false);
        gDec.setUseMB32x32Identity(false);
        
        // Generate AES for encryption
        gEnc.generate(true, AEShelper.testVect128_key, 16, extc);
        AES AESenc = gEnc.getAESi();
        // Generate AES for decryption
        gDec.generate(false, AEShelper.testVect128_key, 16, extc);
        AES AESdec = gDec.getAESi();
        
        //
        // Test whole AES on test vectors
        //
        for(int i=0; i<AEShelper.AES_TESTVECTORS; i++){
            State plain  = new State(AEShelper.testVect128_plain[i], true,  false);
            State state  = new State(AEShelper.testVect128_plain[i], true,  false);
            State cipher = new State(AEShelper.testVect128_cipher[i], true, false);
            
            System.out.println("Testvector index: " + i);
            System.out.println("=====================");
            System.out.println("Testvector plaintext: \n" + plain);
            System.out.println("Testvector ciphertext: \n"+ cipher);
            
            // Encrypt
            state.transpose();
            gEnc.applyExternalEnc(state, extc, true);
            AESenc.crypt(state);
            gEnc.applyExternalEnc(state, extc, false);
            
            System.out.println("Enc(plaintext_test): \n" + state);
            assertEquals("Cipher output mismatch", true, state.equals(cipher));
            
            // Decrypt
            cipher.transpose();
            System.out.println("T(Enc(plaintext_test)): \n" + state);
            gDec.applyExternalEnc(cipher, extc, true);
            AESdec.crypt(cipher);
            gDec.applyExternalEnc(cipher, extc, false);
            System.out.println("Dec(T(Enc(plaintext_test))): \n" + cipher);
            assertEquals("Cipher output mismatch", true, plain.equals(cipher));
        }
    } 
}
