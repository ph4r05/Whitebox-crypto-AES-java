/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import java.util.Random;
import junit.framework.TestCase;
import org.bouncycastle.pqc.math.linearalgebra.IntUtils;
import org.bouncycastle.pqc.math.linearalgebra.Matrix;

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

            long result = instance.invP(h, A);
            
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
            boolean matrixOK=true;
            for(int i=0; i<N; i++){
                for(int j=0; j<N; j++){
                    // test on zero outside of main diagonal
                    if (i!=j && PAQ.isSet(i, j)) {
                        matrixOK=false;
                        break;
                    }
                    
                    // test on ones on main diagonal
                    if (i==j){
                        // test for one on main diagonal for rank
                        if (i<rank && !PAQ.isSet(i, j)){
                            matrixOK=false;
                            break;
                        }
                        
                        // test for zero on main diagonal
                        if (i>=rank && PAQ.isSet(i, j)){
                            matrixOK=false;
                            break;
                        }
                    }
                }
            }
            
            assertEquals("Normalized matrix has invalid form", true, matrixOK);
        }
    }
}
