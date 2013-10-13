/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import java.security.SecureRandom;
import org.bouncycastle.pqc.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.math.linearalgebra.IntUtils;

/**
 *
 * @author ph4r05
 */
public class MixingBijection {
    public static final int FSIZE=2;        // field size
    public static final int QSIZE=4;        // block matrix default size
    public static final int ENCODED_OFFSET=8; // where matrix data really start in encoded form
    private SecureRandom rand = new SecureRandom();
    
    /**
     * Generates random matrix of dimension pxp that is invertible in GF(2).
     * Returns two matrices, M[0] is random pxp regular matrix, M[1] its inverse.
     */
    public long generateInvertiblePM(GF2MatrixEx[] M, int p){
        GF2MatrixEx[] R = GF2MatrixEx.createRandomRegularMatrixAndItsInverse(p, rand);
        M[0] = R[0];
        M[1] = R[1];
        
        return 1;
    }
    
    /**
     * Generates random matrix of dimension pxp that is invertible in GF(2).
     */
    public GF2MatrixEx generateInvertiblePM(int p){
        GF2MatrixEx[] R = GF2MatrixEx.createRandomRegularMatrixAndItsInverse(p, rand);
        return R[0];
    }
    
    /**
     * Extended Inversion version - should return also invertible P,Q matrices in
     * matrix A decomposition PAQ = R where R is in canonical form.
     *
     * Returns rank of matrix
     */
    public long invP(NormalGF2MatrixHolder h, final GF2MatrixEx A){
        return A.normalize(h);
    }
}
