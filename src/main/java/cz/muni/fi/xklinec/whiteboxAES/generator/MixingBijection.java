/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import java.security.SecureRandom;

/**
 *
 * @author ph4r05
 */
public class MixingBijection {
    public static final int FSIZE=2;        // field size
    public static final int QSIZE=4;        // block matrix default size
    public static final int ENCODED_OFFSET=8; // where matrix data really start in encoded form
    private SecureRandom rand  = new SecureRandom();
    private boolean      debug = false;
    
    /**
     * Generates random matrix of dimension pxp that is invertible in GF(2).
     * Returns two matrices, M[0] is random pxp regular matrix, M[1] its inverse.
     */
    public static long generateInvertiblePM(GF2MatrixEx[] M, int p, SecureRandom rand){
        GF2MatrixEx[] R = GF2MatrixEx.createRandomRegularMatrixAndItsInverse(p, rand);
        M[0] = R[0];
        M[1] = R[1];
        
        return 1;
    }
    
    /**
     * Generates random matrix of dimension pxp that is invertible in GF(2).
     * Returns two matrices, M[0] is random pxp regular matrix, M[1] its inverse.
     */
    public long generateInvertiblePM(GF2MatrixEx[] M, int p){
        return generateInvertiblePM(M, p, rand);
    }
    
    /**
     * Generates random matrix of dimension pxp that is invertible in GF(2).
     */
    public static GF2MatrixEx generateInvertiblePM(int p, SecureRandom rand){
        GF2MatrixEx[] R = GF2MatrixEx.createRandomRegularMatrixAndItsInverse(p, rand);
        return R[0];
    }
    
    /**
     * Generates random matrix of dimension pxp that is invertible in GF(2).
     */
    public GF2MatrixEx generateInvertiblePM(int p){
        return generateInvertiblePM(p, rand);
    }
    
    /**
     * Extended Inversion version - should return also invertible P,Q matrices in
     * matrix A decomposition PAQ = R where R is in canonical form.
     *
     * Returns rank of matrix
     */
    public static int invP(NormalGF2MatrixHolder h, final GF2MatrixEx A){
        return A.normalize(h);
    }
    
    /**
     * Generates n x n matrix M in canonical form for given rank.
     */
    public static GF2MatrixEx canonical(int rank, int n){
        GF2MatrixEx m = new GF2MatrixEx(n, GF2MatrixEx.MATRIX_TYPE_ZERO);
        for (int i = 0; i < rank; i++) {
            m.set(i, i, 1);
        }
        
        return m;
    }
    
    /**
     * Generates matrix A according to paper
     * [http://eprint.iacr.org/2002/096.pdf] From lemma 1.
     *
     * T = canonical(rank,m) + A is invertible, according to this paper.
     */
    public static GF2MatrixEx generateARankMatrix(int rank, int n) {
        GF2MatrixEx A = new GF2MatrixEx(n, GF2MatrixEx.MATRIX_TYPE_ZERO);
        int i = 0, offset = 0;

        if (rank == 1) {
            // On rank = 1 matrix has special form [1 1; 1 0] and then I
            A.set(0, 0, 1);
            A.set(0, 1, 1);
            A.set(1, 0, 1);
            for (i = 2; i < n; i++) {
                A.set(i, i, 1);
            }
            return A;
        }

        if ((rank % 2) == 1) {
            // First block of matrix is 3x3 in special form [1 1 1; 1 1 0; 1 0 0]
            A.set(0, 0, 1);
            A.set(0, 1, 1);
            A.set(0, 2, 1);
            A.set(1, 0, 1);
            A.set(1, 1, 1);
            A.set(2, 0, 1);
            offset = 3;
        }

        //
        // Merged case - r is odd or even and >= 3
        //

        // For even rank it is easy to construct
        // On diagonals is <rank> copies of matrix [0 1; 1 1]
        // filled with I_2 on rest of blocks
        for (i = 0; i < rank / 2; i++) {
            A.set(2 * i + offset, 2 * i + 1 + offset, 1);
            A.set(2 * i + 1 + offset, 2 * i + offset, 1);
            A.set(2 * i + 1 + offset, 2 * i + 1 + offset, 1);
        }

        // the rest fill with 1 on diagonals (I_{n-r} matrix)
        for (i = rank + offset - 1; i < n; i++) {
            if (i < 0) {
                continue;
            }

            A.set(i, i, 1);
        }
        return A;
    }
   
   /**
    * Generates mixing bijection matrix according to paper [http://eprint.iacr.org/2002/096.pdf].
    * p | t. Will compute matrix A s.t. dimension = t x t and is composed from block of size p x p
    * submatrices.
    */
   public GF2MatrixEx generateMixingBijection(int t, int p){
       return generateMixingBijection(t, p, rand, debug);
   }
    
    /**
    * Generates mixing bijection matrix according to paper [http://eprint.iacr.org/2002/096.pdf].
    * p | t. Will compute matrix A s.t. dimension = t x t and is composed from block of size p x p
    * submatrices.
    */
   public static GF2MatrixEx generateMixingBijection(int t, int p, SecureRandom rand, boolean debug){
	// validate parameters
	if (t<p || (t%p) != 0){
            throw new IllegalArgumentException("Invalid parameters t, p");
	}
        
	// 0. generate M matrix pxp that is invertible
	GF2MatrixEx M = generateInvertiblePM(p, rand);

	// some matrices that we will need, naming according to the paper
	GF2MatrixEx X; 	GF2MatrixEx Y;
	GF2MatrixEx P;	GF2MatrixEx Pinv;
	GF2MatrixEx Q;	GF2MatrixEx Qinv;
	GF2MatrixEx A;
	GF2MatrixEx TMP;
	GF2MatrixEx Minv;
	GF2MatrixEx N;
        NormalGF2MatrixHolder h = new NormalGF2MatrixHolder();
        
        int d;
	int i,j,k;
	int curT = p;			// current size of matrix M
	int tmp;			// current column/row
	for(; curT < t; curT+=p){
            int pBlocksInM=curT/p;	// number of pxp sub-matrices in M

            // 1. X matrix - p x t matrix, generated from M matrix using some row
            X   = new GF2MatrixEx(p, curT);
            tmp = rand.nextInt(pBlocksInM);		// current row
            for(i=p*tmp,k=0; k<p; i++, k++){
                for(j=0; j<curT; j++){
                    X.set(k,j, M.get(i,j));
                }
            }

            // 2. Y matrix - t x p matrix, generated from M matrix using some column
            Y   = new GF2MatrixEx(curT, p);
            tmp = rand.nextInt(pBlocksInM);
            for(i=0; i<curT; i++){
                for(j=p*tmp,k=0; k<p; j++, k++){
                    Y.set(i,k, M.get(i,j));
                }
            }

            // 3. computing invertible P,Q matrices
            Minv = (GF2MatrixEx) M.computeInverse();
            TMP  = (GF2MatrixEx) X.rightMultiply(Minv.rightMultiply(Y)); // TMP = X * Minv * Y;

            if (debug){
                System.out.println("X    matrix:\n" + X +"\n");
                System.out.println("Y    matrix:\n" + Y +"\n");
                System.out.println("Minv matrix:\n" + Minv +"\n");
                System.out.println("TMP  matrix:\n" + TMP +"\n");
            }

            int rank = invP(h, TMP);
            P = h.getP();
            Q = h.getQ();
            d = h.getDetetrminant();

            if (debug){
                System.out.println("TMP rank:" + rank);
                System.out.println("P matrix:\n" + P +"\n");
                System.out.println("Q matrix:\n" + Q +"\n");
            }

            // 4. A matrix
            A = generateARankMatrix(rank, p);
            if (debug){
                System.out.println("A matrix:\n" + A +"\n");
            }

            // 5. resulting matrix
            GF2MatrixEx TMP2;
            N = new GF2MatrixEx(curT + p, curT + p);

            Pinv = (GF2MatrixEx) P.computeInverse();
            Qinv = (GF2MatrixEx) Q.computeInverse();
            TMP2 = (GF2MatrixEx) Pinv.rightMultiply(A.rightMultiply(Qinv)); // TMP + Pinv*A*Qinv;
            TMP2.add(TMP);

            // copy M matrix, M is curT x curT matrix
            for(i=0;i<curT;i++){
                for(j=0;j<curT;j++){
                    N.set(i,j,M.get(i,j));
                }
            }
            // copy X matrix, p x curT
            for(i=0;i<p;i++){
                for(j=0;j<curT;j++){
                    N.set(curT+i,j,X.get(i,j));
                }
            }
            // copy Y matrix, curT x p
            for(i=0;i<curT;i++){
                for(j=0;j<p;j++){
                    N.set(i,curT+j,Y.get(i,j));
                }
            }
            // copy TMP2 matrix, p x p
            for(i=0;i<p;i++){
                for(j=0;j<p;j++){
                    N.set(curT+i,curT+j,TMP2.get(i,j));
                }
            }

            if (debug){
                System.out.println("Intermediate result for curT=" + curT + "; Matrix = \n" + N + "\n");
            }

            M = N;
	}

	return M;
    }
}
