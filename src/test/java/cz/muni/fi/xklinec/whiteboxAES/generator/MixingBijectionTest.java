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

import java.util.Random;
import junit.framework.TestCase;
import org.bouncycastle.pqc.math.linearalgebra.IntUtils;

/**
 *
 * @author ph4r05
 */
public class MixingBijectionTest extends TestCase {
    
    public MixingBijectionTest(String testName) {
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
     * Test of invP method, of class MixingBijection.
     */
    public void testInvP() {
        System.out.println("invP");
        Random rnd = new Random();
        
        // probabilistic testing case - random matrix size, rank
        for(int it=0; it<500; it++){
            final int     N        = 2 + rnd.nextInt(66);  // matrix rank
            final boolean fullRank = rnd.nextBoolean();    // should matrix have full rank?
            final int     rank     = fullRank ? N : 2 + rnd.nextInt(N-1); 
            
            // Generate invertible matrix of given size
            MixingBijection instance = new MixingBijection();
            GF2MatrixEx A = instance.generateInvertiblePM(N);
            NormalGF2MatrixHolder h = new NormalGF2MatrixHolder();
            
            // Rank fixing
            if (!fullRank){
                final int srcRow = rnd.nextInt(N);
                
                int[][] Ai = A.getIntArray();
                for(int i=0,r=N; i<N; i++){
                    if (r==rank || i==srcRow) {
                        continue;
                    }
                    
                    Ai[i] = IntUtils.clone(Ai[srcRow]);
                    r-=1;
                }   
            }

            long result = MixingBijection.invP(h, A);
            
            /*
            System.out.println("Matrix2invert: N="+N+"; rank="+rank+"\n" + A.toString());
            System.out.println("After inversion... Rank=" + h.getRank() 
                    + "; Determinant=" + h.getDetetrminant()
                    + "; Inversion matrix: \n" + h.getP()
                    + "; Q matrix: \n" + h.getQ());
            */

            
            GF2MatrixEx PA  = (GF2MatrixEx) h.getP().rightMultiply(A);
            GF2MatrixEx PAQ = (GF2MatrixEx) PA.rightMultiply(h.getQ());

            /*
            System.out.println("P*A:   \n" + PA);
            System.out.println("P*A*Q: \n" + PAQ);
            */
            
            assertEquals(rank, h.getRank());
            assertEquals(rank == N ? 1 : 0, h.getDetetrminant());
            
            // test resulting normal matrix for correct form
            assertEquals("Normalized matrix has invalid form", true, NTLUtils.isNormalizedRank(PAQ, rank));
        }
    }

    public void testGenerateMixingBijection(){
        System.out.println("generateMixingBijection");
        Random rnd = new Random();
        
        MixingBijection instance = new MixingBijection();
        
        // Method is probabilistic, so test multiple times
        for(int i=0; i<100; i++){
            final int mSizeExp = 2 + rnd.nextInt(6);           // matrix dize exponent
            final int mSubExp  = 1 + rnd.nextInt(mSizeExp-1);  // submatrix dize exponent
            final int mSize    = i==0 ? 32 : 1 << mSizeExp;
            final int mSub     = i==0 ?  4 : 1 << mSubExp;
            
            GF2MatrixEx mb = instance.generateMixingBijection(mSize, mSub);
            
            // main test case - each mixing bijection has to be invertible!
            NormalGF2MatrixHolder h = new NormalGF2MatrixHolder();
            mb.normalize(h);
            
            assertEquals("Determinant should be 1", 1, h.getDetetrminant());
            assertEquals("Rank of the matrix does not match", mSize, h.getRank());
        }
    }
}
