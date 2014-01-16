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
import java.security.SecureRandom;
import org.bouncycastle.pqc.math.linearalgebra.GF2Vector;
import org.bouncycastle.pqc.math.linearalgebra.IntUtils;
import org.bouncycastle.pqc.math.linearalgebra.LittleEndianConversions;
import org.bouncycastle.pqc.math.linearalgebra.Matrix;
import org.bouncycastle.pqc.math.linearalgebra.Permutation;
import org.bouncycastle.pqc.math.linearalgebra.Vector;

/**
 * This class describes some operations with matrices over finite field GF(2)
 * and is used in ecc and MQ-PKC (also has some specific methods and
 * implementation)
 */
public class GF2MatrixEx extends Matrix
{
    public final static int BLOCKEXP = 5;             // 2^BLOCKEXP = size of one storage block
    public final static int INTSIZE  = 1 << BLOCKEXP; // size of one storage block
    public final static int INTMASK  = INTSIZE - 1;   // masking to get remainder after DIV INTSIZE 
    
    /**
     * For the matrix representation the array of type int[][] is used, thus one
     * element of the array keeps 32 elements of the matrix (from one row and 32
     * columns)
     */
    private int[][] matrix;

    /**
     * the length of each array representing a row of this matrix, computed as
     * <tt>(numColumns + 31) / 32</tt>
     */
    private int length;

    /**
     * Create the matrix from encoded form.
     *
     * @param enc the encoded matrix
     */
    public GF2MatrixEx(byte[] enc)
    {
        if (enc.length < 9)
        {
            throw new ArithmeticException(
                "given array is not an encoded matrix over GF(2)");
        }

        numRows = LittleEndianConversions.OS2IP(enc, 0);
        numColumns = LittleEndianConversions.OS2IP(enc, 4);

        int n = ((numColumns + 7) >>> 3) * numRows;

        if ((numRows <= 0) || (n != (enc.length - 8)))
        {
            throw new ArithmeticException(
                "given array is not an encoded matrix over GF(2)");
        }

        length = (numColumns + (INTSIZE-1)) >>> BLOCKEXP;
        matrix = new int[numRows][length];

        // number of "full" integer
        int q = numColumns >> BLOCKEXP;
        // number of bits in non-full integer
        int r = numColumns & 0x1f;

        int count = 8;
        for (int i = 0; i < numRows; i++)
        {
            for (int j = 0; j < q; j++, count += 4)
            {
                matrix[i][j] = LittleEndianConversions.OS2IP(enc, count);
            }
            for (int j = 0; j < r; j += 8)
            {
                matrix[i][q] ^= (enc[count++] & 0xff) << j;
            }
        }
    }

    /**
     * Create the matrix with the contents of the given array. The matrix is not
     * copied. Unused coefficients are masked out.
     *
     * @param numColumns the number of columns
     * @param matrix     the element array
     */
    public GF2MatrixEx(int numColumns, int[][] matrix)
    {
        if (matrix[0].length != (numColumns + (INTSIZE-1)) >> BLOCKEXP)
        {
            throw new ArithmeticException(
                "Int array does not match given number of columns.");
        }
        this.numColumns = numColumns;
        numRows = matrix.length;
        length = matrix[0].length;
        int rest = numColumns & 0x1f;
        int bitMask;
        if (rest == 0)
        {
            bitMask = 0xffffffff;
        }
        else
        {
            bitMask = (1 << rest) - 1;
        }
        for (int i = 0; i < numRows; i++)
        {
            matrix[i][length - 1] &= bitMask;
        }
        this.matrix = matrix;
    }

    /**
     * Create an nxn matrix of the given type.
     *
     * @param n            the number of rows (and columns)
     * @param typeOfMatrix the martix type (see {@link Matrix} for predefined
     *                     constants)
     */
    public GF2MatrixEx(int n, char typeOfMatrix)
    {
        this(n, typeOfMatrix, new java.security.SecureRandom());
    }

    /**
     * Create an nxn matrix of the given type.
     *
     * @param n            the matrix size
     * @param typeOfMatrix the matrix type
     * @param sr           the source of randomness
     */
    public GF2MatrixEx(int n, char typeOfMatrix, SecureRandom sr)
    {
        if (n <= 0)
        {
            throw new ArithmeticException("Size of matrix is non-positive.");
        }

        switch (typeOfMatrix)
        {

        case Matrix.MATRIX_TYPE_ZERO:
            assignZeroMatrix(n, n);
            break;

        case Matrix.MATRIX_TYPE_UNIT:
            assignUnitMatrix(n);
            break;

        case Matrix.MATRIX_TYPE_RANDOM_LT:
            assignRandomLowerTriangularMatrix(n, sr);
            break;

        case Matrix.MATRIX_TYPE_RANDOM_UT:
            assignRandomUpperTriangularMatrix(n, sr);
            break;

        case Matrix.MATRIX_TYPE_RANDOM_REGULAR:
            assignRandomRegularMatrix(n, sr);
            break;

        default:
            throw new ArithmeticException("Unknown matrix type.");
        }
    }

    /**
     * Copy constructor.
     *
     * @param a another {@link GF2MatrixEx}
     */
    public GF2MatrixEx(GF2MatrixEx a)
    {
        numColumns = a.getNumColumns();
        numRows = a.getNumRows();
        length = a.getLength();
        matrix = new int[a.getIntArray().length][];
        for (int i = 0; i < matrix.length; i++)
        {
            matrix[i] = IntUtils.clone(a.getIntArray()[i]);
        }

    }

    /**
     * create the mxn zero matrix
     */
    public GF2MatrixEx(int m, int n)
    {
        if ((n <= 0) || (m <= 0))
        {
            throw new ArithmeticException("size of matrix is non-positive");
        }

        assignZeroMatrix(m, n);
    }

    /**
     * Create the mxn zero matrix.
     *
     * @param m number of rows
     * @param n number of columns
     */
    public void assignZeroMatrix(int m, int n)
    {
        numRows = m;
        numColumns = n;
        length = (n + (INTSIZE-1)) >>> BLOCKEXP;
        matrix = new int[numRows][length];
        for (int i = 0; i < numRows; i++)
        {
            for (int j = 0; j < length; j++)
            {
                matrix[i][j] = 0;
            }
        }
    }

    /**
     * Create the mxn unit matrix.
     *
     * @param n number of rows (and columns)
     */
    public void assignUnitMatrix(int n)
    {
        numRows = n;
        numColumns = n;
        length = (n + (INTSIZE-1)) >>> BLOCKEXP;
        matrix = new int[numRows][length];
        for (int i = 0; i < numRows; i++)
        {
            for (int j = 0; j < length; j++)
            {
                matrix[i][j] = 0;
            }
        }
        for (int i = 0; i < numRows; i++)
        {
            int rest = i & 0x1f;
            matrix[i][i >>> BLOCKEXP] = 1 << rest;
        }
    }

    /**
     * Create a nxn random lower triangular matrix.
     *
     * @param n  number of rows (and columns)
     * @param sr source of randomness
     */
    public void assignRandomLowerTriangularMatrix(int n, SecureRandom sr)
    {
        numRows = n;
        numColumns = n;
        length = (n + (INTSIZE-1)) >>> BLOCKEXP;
        matrix = new int[numRows][length];
        for (int i = 0; i < numRows; i++)
        {
            int q = i >>> BLOCKEXP;
            int r = i & 0x1f;
            int s = (INTSIZE-1) - r;
            r = 1 << r;
            for (int j = 0; j < q; j++)
            {
                matrix[i][j] = sr.nextInt();
            }
            matrix[i][q] = (sr.nextInt() >>> s) | r;
            for (int j = q + 1; j < length; j++)
            {
                matrix[i][j] = 0;
            }

        }

    }

    /**
     * Create a nxn random upper triangular matrix.
     *
     * @param n  number of rows (and columns)
     * @param sr source of randomness
     */
    public void assignRandomUpperTriangularMatrix(int n, SecureRandom sr)
    {
        numRows = n;
        numColumns = n;
        length = (n + (INTSIZE-1)) >>> BLOCKEXP;
        matrix = new int[numRows][length];
        int rest = n & 0x1f;
        int help;
        if (rest == 0)
        {
            help = 0xffffffff;
        }
        else
        {
            help = (1 << rest) - 1;
        }
        for (int i = 0; i < numRows; i++)
        {
            int q = i >>> BLOCKEXP;
            int r = i & 0x1f;
            int s = r;
            r = 1 << r;
            for (int j = 0; j < q; j++)
            {
                matrix[i][j] = 0;
            }
            matrix[i][q] = (sr.nextInt() << s) | r;
            for (int j = q + 1; j < length; j++)
            {
                matrix[i][j] = sr.nextInt();
            }
            matrix[i][length - 1] &= help;
        }

    }

    /**
     * Create an nxn random regular matrix.
     *
     * @param n  number of rows (and columns)
     * @param sr source of randomness
     */
    public void assignRandomRegularMatrix(int n, SecureRandom sr)
    {
        numRows = n;
        numColumns = n;
        length = (n + (INTSIZE-1)) >>> BLOCKEXP;
        matrix = new int[numRows][length];
        GF2MatrixEx lm = new GF2MatrixEx(n, Matrix.MATRIX_TYPE_RANDOM_LT, sr);
        GF2MatrixEx um = new GF2MatrixEx(n, Matrix.MATRIX_TYPE_RANDOM_UT, sr);
        GF2MatrixEx rm = (GF2MatrixEx)lm.rightMultiply(um);
        Permutation perm = new Permutation(n, sr);
        int[] p = perm.getVector();
        for (int i = 0; i < n; i++)
        {
            System.arraycopy(rm.getIntArray()[i], 0, matrix[p[i]], 0, length);
        }
    }

    /**
     * Create a nxn random regular matrix and its inverse.
     *
     * @param n  number of rows (and columns)
     * @param sr source of randomness
     * @return the created random regular matrix and its inverse
     */
    public static GF2MatrixEx[] createRandomRegularMatrixAndItsInverse(int n,
                                                                     SecureRandom sr)
    {

        GF2MatrixEx[] result = new GF2MatrixEx[2];

        // ------------------------------------
        // First part: create regular matrix
        // ------------------------------------

        // ------
        int length = (n + (INTSIZE-1)) >>> BLOCKEXP;
        GF2MatrixEx lm = new GF2MatrixEx(n, Matrix.MATRIX_TYPE_RANDOM_LT, sr);
        GF2MatrixEx um = new GF2MatrixEx(n, Matrix.MATRIX_TYPE_RANDOM_UT, sr);
        GF2MatrixEx rm = (GF2MatrixEx)lm.rightMultiply(um);
        Permutation p = new Permutation(n, sr);
        int[] pVec = p.getVector();

        int[][] matrix = new int[n][length];
        for (int i = 0; i < n; i++)
        {
            System.arraycopy(rm.getIntArray()[pVec[i]], 0, matrix[i], 0, length);
        }

        result[0] = new GF2MatrixEx(n, matrix);

        // ------------------------------------
        // Second part: create inverse matrix
        // ------------------------------------

        // inverse to lm
        GF2MatrixEx invLm = new GF2MatrixEx(n, Matrix.MATRIX_TYPE_UNIT);
        for (int i = 0; i < n; i++)
        {
            int rest = i & 0x1f;
            int q = i >>> BLOCKEXP;
            int r = 1 << rest;
            for (int j = i + 1; j < n; j++)
            {
                int b = (lm.getIntArray()[j][q]) & r;
                if (b != 0)
                {
                    for (int k = 0; k <= q; k++)
                    {
                        invLm.getIntArray()[j][k] ^= invLm.getIntArray()[i][k];
                    }
                }
            }
        }
        // inverse to um
        GF2MatrixEx invUm = new GF2MatrixEx(n, Matrix.MATRIX_TYPE_UNIT);
        for (int i = n - 1; i >= 0; i--)
        {
            int rest = i & 0x1f;
            int q = i >>> BLOCKEXP;
            int r = 1 << rest;
            for (int j = i - 1; j >= 0; j--)
            {
                int b = (um.getIntArray()[j][q]) & r;
                if (b != 0)
                {
                    for (int k = q; k < length; k++)
                    {
                        invUm.getIntArray()[j][k] ^= invUm.getIntArray()[i][k];
                    }
                }
            }
        }

        // inverse matrix
        result[1] = (GF2MatrixEx)invUm.rightMultiply(invLm.rightMultiply(p));

        return result;
    }

    /**
     * @return the array keeping the matrix elements
     */
    public int[][] getIntArray()
    {
        return matrix;
    }

    /**
     * @return the length of each array representing a row of this matrix
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Return the row of this matrix with the given index.
     *
     * @param index the index
     * @return the row of this matrix with the given index
     */
    public int[] getRow(int index)
    {
        return matrix[index];
    }

    /**
     * Returns encoded matrix, i.e., this matrix in byte array form
     *
     * @return the encoded matrix
     */
    public byte[] getEncoded()
    {
        int n = (numColumns + 7) >>> 3;
        n *= numRows;
        n += 8;
        byte[] enc = new byte[n];

        LittleEndianConversions.I2OSP(numRows, enc, 0);
        LittleEndianConversions.I2OSP(numColumns, enc, 4);

        // number of "full" integer
        int q = numColumns >>> BLOCKEXP;
        // number of bits in non-full integer
        int r = numColumns & 0x1f;

        int count = 8;
        for (int i = 0; i < numRows; i++)
        {
            for (int j = 0; j < q; j++, count += 4)
            {
                LittleEndianConversions.I2OSP(matrix[i][j], enc, count);
            }
            for (int j = 0; j < r; j += 8)
            {
                enc[count++] = (byte)((matrix[i][q] >>> j) & 0xff);
            }

        }
        return enc;
    }


    /**
     * Returns the percentage of the number of "ones" in this matrix.
     *
     * @return the Hamming weight of this matrix (as a ratio).
     */
    public double getHammingWeight()
    {
        double counter = 0.0;
        double elementCounter = 0.0;
        int rest = numColumns & 0x1f;
        int d;
        if (rest == 0)
        {
            d = length;
        }
        else
        {
            d = length - 1;
        }

        for (int i = 0; i < numRows; i++)
        {

            for (int j = 0; j < d; j++)
            {
                int a = matrix[i][j];
                for (int k = 0; k < INTSIZE; k++)
                {
                    int b = (a >>> k) & 1;
                    counter = counter + b;
                    elementCounter = elementCounter + 1;
                }
            }
            int a = matrix[i][length - 1];
            for (int k = 0; k < rest; k++)
            {
                int b = (a >>> k) & 1;
                counter = counter + b;
                elementCounter = elementCounter + 1;
            }
        }

        return counter / elementCounter;
    }

    /**
     * Check if this is the zero matrix (i.e., all entries are zero).
     *
     * @return <tt>true</tt> if this is the zero matrix
     */
    public boolean isZero()
    {
        for (int i = 0; i < numRows; i++)
        {
            for (int j = 0; j < length; j++)
            {
                if (matrix[i][j] != 0)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get the quadratic submatrix of this matrix consisting of the leftmost
     * <tt>numRows</tt> columns.
     *
     * @return the <tt>(numRows x numRows)</tt> submatrix
     */
    public GF2MatrixEx getLeftSubMatrix()
    {
        if (numColumns <= numRows)
        {
            throw new ArithmeticException("empty submatrix");
        }
        int length = (numRows + (INTSIZE-1)) >> BLOCKEXP;
        int[][] result = new int[numRows][length];
        int bitMask = (1 << (numRows & 0x1f)) - 1;
        if (bitMask == 0)
        {
            bitMask = -1;
        }
        for (int i = numRows - 1; i >= 0; i--)
        {
            System.arraycopy(matrix[i], 0, result[i], 0, length);
            result[i][length - 1] &= bitMask;
        }
        return new GF2MatrixEx(numRows, result);
    }

    /**
     * Compute the full form matrix <tt>(this | Id)</tt> from this matrix in
     * left compact form, where <tt>Id</tt> is the <tt>k x k</tt> identity
     * matrix and <tt>k</tt> is the number of rows of this matrix.
     *
     * @return <tt>(this | Id)</tt>
     */
    public GF2MatrixEx extendLeftCompactForm()
    {
        int newNumColumns = numColumns + numRows;
        GF2MatrixEx result = new GF2MatrixEx(numRows, newNumColumns);

        int ind = numRows - 1 + numColumns;
        for (int i = numRows - 1; i >= 0; i--, ind--)
        {
            // copy this matrix to first columns
            System.arraycopy(matrix[i], 0, result.matrix[i], 0, length);
            // store the identity in last columns
            result.matrix[i][ind >> BLOCKEXP] |= 1 << (ind & 0x1f);
        }

        return result;
    }

    /**
     * Get the submatrix of this matrix consisting of the rightmost
     * <tt>numColumns-numRows</tt> columns.
     *
     * @return the <tt>(numRows x (numColumns-numRows))</tt> submatrix
     */
    public GF2MatrixEx getRightSubMatrix()
    {
        if (numColumns <= numRows)
        {
            throw new ArithmeticException("empty submatrix");
        }

        int q = numRows >> BLOCKEXP;
        int r = numRows & 0x1f;

        GF2MatrixEx result = new GF2MatrixEx(numRows, numColumns - numRows);

        for (int i = numRows - 1; i >= 0; i--)
        {
            // if words have to be shifted
            if (r != 0)
            {
                int ind = q;
                // process all but last word
                for (int j = 0; j < result.length - 1; j++)
                {
                    // shift to correct position
                    result.matrix[i][j] = (matrix[i][ind++] >>> r)
                        | (matrix[i][ind] << (INTSIZE - r));
                }
                // process last word
                result.matrix[i][result.length - 1] = matrix[i][ind++] >>> r;
                if (ind < length)
                {
                    result.matrix[i][result.length - 1] |= matrix[i][ind] << (INTSIZE - r);
                }
            }
            else
            {
                // no shifting necessary
                System.arraycopy(matrix[i], q, result.matrix[i], 0,
                    result.length);
            }
        }
        return result;
    }

    /**
     * Compute the full form matrix <tt>(Id | this)</tt> from this matrix in
     * right compact form, where <tt>Id</tt> is the <tt>k x k</tt> identity
     * matrix and <tt>k</tt> is the number of rows of this matrix.
     *
     * @return <tt>(Id | this)</tt>
     */
    public GF2MatrixEx extendRightCompactForm()
    {
        GF2MatrixEx result = new GF2MatrixEx(numRows, numRows + numColumns);

        int q = numRows >> BLOCKEXP;
        int r = numRows & 0x1f;

        for (int i = numRows - 1; i >= 0; i--)
        {
            // store the identity in first columns
            result.matrix[i][i >> BLOCKEXP] |= 1 << (i & 0x1f);

            // copy this matrix to last columns

            // if words have to be shifted
            if (r != 0)
            {
                int ind = q;
                // process all but last word
                for (int j = 0; j < length - 1; j++)
                {
                    // obtain matrix word
                    int mw = matrix[i][j];
                    // shift to correct position
                    result.matrix[i][ind++] |= mw << r;
                    result.matrix[i][ind] |= mw >>> (INTSIZE - r);
                }
                // process last word
                int mw = matrix[i][length - 1];
                result.matrix[i][ind++] |= mw << r;
                if (ind < result.length)
                {
                    result.matrix[i][ind] |= mw >>> (INTSIZE - r);
                }
            }
            else
            {
                // no shifting necessary
                System.arraycopy(matrix[i], 0, result.matrix[i], q, length);
            }
        }

        return result;
    }

    /**
     * Compute the transpose of this matrix.
     *
     * @return <tt>(this)<sup>T</sup></tt>
     */
    public Matrix computeTranspose()
    {
        int[][] result = new int[numColumns][(numRows + (INTSIZE-1)) >>> BLOCKEXP];
        for (int i = 0; i < numRows; i++)
        {
            for (int j = 0; j < numColumns; j++)
            {
                int qs = j >>> BLOCKEXP;
                int rs = j & 0x1f;
                int b = (matrix[i][qs] >>> rs) & 1;
                int qt = i >>> BLOCKEXP;
                int rt = i & 0x1f;
                if (b == 1)
                {
                    result[j][qt] |= 1 << rt;
                }
            }
        }

        return new GF2MatrixEx(numRows, result);
    }

    /**
     * Compute the inverse of this matrix.
     *
     * @return the inverse of this matrix (newly created).
     * @throws ArithmeticException if this matrix is not invertible.
     */
    public Matrix computeInverse()
    {
        if (numRows != numColumns)
        {
            throw new ArithmeticException("Matrix is not invertible.");
        }

        // clone this matrix
        int[][] tmpMatrix = new int[numRows][length];
        for (int i = numRows - 1; i >= 0; i--)
        {
            tmpMatrix[i] = IntUtils.clone(matrix[i]);
        }

        // initialize inverse matrix as unit matrix
        int[][] invMatrix = new int[numRows][length];
        for (int i = numRows - 1; i >= 0; i--)
        {
            int q = i >> BLOCKEXP;
            int r = i & 0x1f;
            invMatrix[i][q] = 1 << r;
        }

        // simultaneously compute Gaussian reduction of tmpMatrix and unit
        // matrix
        for (int i = 0; i < numRows; i++)
        {
            // i = q * INTSIZE + (i mod INTSIZE)
            int q = i >> BLOCKEXP;
            int bitMask = 1 << (i & 0x1f);
            // if diagonal element is zero
            if ((tmpMatrix[i][q] & bitMask) == 0)
            {
                boolean foundNonZero = false;
                // find a non-zero element in the same column
                for (int j = i + 1; j < numRows; j++)
                {
                    if ((tmpMatrix[j][q] & bitMask) != 0)
                    {
                        // found it, swap rows ...
                        foundNonZero = true;
                        swapRows(tmpMatrix, i, j);
                        swapRows(invMatrix, i, j);
                        // ... and quit searching
                        j = numRows;
                        continue;
                    }
                }
                // if no non-zero element was found ...
                if (!foundNonZero)
                {
                    // ... the matrix is not invertible
                    throw new ArithmeticException("Matrix is not invertible.");
                }
            }

            // normalize all but i-th row
            for (int j = numRows - 1; j >= 0; j--)
            {
                if ((j != i) && ((tmpMatrix[j][q] & bitMask) != 0))
                {
                    addToRow(tmpMatrix[i], tmpMatrix[j], q);
                    addToRow(invMatrix[i], invMatrix[j], 0);
                }
            }
        }

        return new GF2MatrixEx(numColumns, invMatrix);
    }

    /**
     * Compute the product of a permutation matrix (which is generated from an
     * n-permutation) and this matrix.
     *
     * @param p the permutation
     * @return {@link GF2MatrixEx} <tt>P*this</tt>
     */
    public Matrix leftMultiply(Permutation p)
    {
        int[] pVec = p.getVector();
        if (pVec.length != numRows)
        {
            throw new ArithmeticException("length mismatch");
        }

        int[][] result = new int[numRows][];

        for (int i = numRows - 1; i >= 0; i--)
        {
            result[i] = IntUtils.clone(matrix[pVec[i]]);
        }

        return new GF2MatrixEx(numRows, result);
    }

    /**
     * compute product a row vector and this matrix
     *
     * @param vec a vector over GF(2)
     * @return Vector product a*matrix
     */
    public Vector leftMultiply(Vector vec)
    {

        if (!(vec instanceof GF2Vector))
        {
            throw new ArithmeticException("vector is not defined over GF(2)");
        }

        if (vec.getLength() != numRows)
        {
            throw new ArithmeticException("length mismatch");
        }

        int[] v = ((GF2Vector)vec).getVecArray();
        int[] res = new int[length];

        int q = numRows >> BLOCKEXP;
        int r = 1 << (numRows & 0x1f);

        // compute scalar products with full words of vector
        int row = 0;
        for (int i = 0; i < q; i++)
        {
            int bitMask = 1;
            do
            {
                int b = v[i] & bitMask;
                if (b != 0)
                {
                    for (int j = 0; j < length; j++)
                    {
                        res[j] ^= matrix[row][j];
                    }
                }
                row++;
                bitMask <<= 1;
            }
            while (bitMask != 0);
        }

        // compute scalar products with last word of vector
        int bitMask = 1;
        while (bitMask != r)
        {
            int b = v[q] & bitMask;
            if (b != 0)
            {
                for (int j = 0; j < length; j++)
                {
                    res[j] ^= matrix[row][j];
                }
            }
            row++;
            bitMask <<= 1;
        }
        
        return new GF2Vector(numColumns, res);
    }

    /**
     * Compute the product of the matrix <tt>(this | Id)</tt> and a column
     * vector, where <tt>Id</tt> is a <tt>(numRows x numRows)</tt> unit
     * matrix.
     *
     * @param vec the vector over GF(2)
     * @return <tt>(this | Id)*vector</tt>
     */
    public Vector leftMultiplyLeftCompactForm(Vector vec)
    {
        if (!(vec instanceof GF2Vector))
        {
            throw new ArithmeticException("vector is not defined over GF(2)");
        }

        if (vec.getLength() != numRows)
        {
            throw new ArithmeticException("length mismatch");
        }

        int[] v = ((GF2Vector)vec).getVecArray();
        int[] res = new int[(numRows + numColumns + (INTSIZE-1)) >>> BLOCKEXP];

        // process full words of vector
        int words = numRows >>> BLOCKEXP;
        int row = 0;
        for (int i = 0; i < words; i++)
        {
            int bitMask = 1;
            do
            {
                int b = v[i] & bitMask;
                if (b != 0)
                {
                    // compute scalar product part
                    for (int j = 0; j < length; j++)
                    {
                        res[j] ^= matrix[row][j];
                    }
                    // set last bit
                    int q = (numColumns + row) >>> BLOCKEXP;
                    int r = (numColumns + row) & 0x1f;
                    res[q] |= 1 << r;
                }
                row++;
                bitMask <<= 1;
            }
            while (bitMask != 0);
        }

        // process last word of vector
        int rem = 1 << (numRows & 0x1f);
        int bitMask = 1;
        while (bitMask != rem)
        {
            int b = v[words] & bitMask;
            if (b != 0)
            {
                // compute scalar product part
                for (int j = 0; j < length; j++)
                {
                    res[j] ^= matrix[row][j];
                }
                // set last bit
                int q = (numColumns + row) >>> BLOCKEXP;
                int r = (numColumns + row) & 0x1f;
                res[q] |= 1 << r;
            }
            row++;
            bitMask <<= 1;
        }

        return new GF2Vector(numRows + numColumns, res);
    }

    /**
     * Compute the product of this matrix and a matrix A over GF(2).
     *
     * @param mat a matrix A over GF(2)
     * @return matrix product <tt>this*matrixA</tt>
     */
    public Matrix rightMultiply(Matrix mat)
    {
        if (!(mat instanceof GF2MatrixEx))
        {
            throw new ArithmeticException("matrix is not defined over GF(2)");
        }

        if (mat.getNumRows() != numColumns)
        {
            throw new ArithmeticException("length mismatch");
        }

        GF2MatrixEx a = (GF2MatrixEx)mat;
        GF2MatrixEx result = new GF2MatrixEx(numRows, mat.getNumColumns());

        int d;
        int rest = numColumns & 0x1f;
        if (rest == 0)
        {
            d = length;
        }
        else
        {
            d = length - 1;
        }
        for (int i = 0; i < numRows; i++)
        {
            int count = 0;
            for (int j = 0; j < d; j++)
            {
                int e = matrix[i][j];
                for (int h = 0; h < INTSIZE; h++)
                {
                    int b = e & (1 << h);
                    if (b != 0)
                    {
                        for (int g = 0; g < a.length; g++)
                        {
                            result.matrix[i][g] ^= a.matrix[count][g];
                        }
                    }
                    count++;
                }
            }
            int e = matrix[i][length - 1];
            for (int h = 0; h < rest; h++)
            {
                int b = e & (1 << h);
                if (b != 0)
                {
                    for (int g = 0; g < a.length; g++)
                    {
                        result.matrix[i][g] ^= a.matrix[count][g];
                    }
                }
                count++;
            }

        }

        return result;
    }

    /**
     * Compute the product of this matrix and a permutation matrix which is
     * generated from an n-permutation.
     *
     * @param p the permutation
     * @return {@link GF2MatrixEx} <tt>this*P</tt>
     */
    public Matrix rightMultiply(Permutation p)
    {

        int[] pVec = p.getVector();
        if (pVec.length != numColumns)
        {
            throw new ArithmeticException("length mismatch");
        }

        GF2MatrixEx result = new GF2MatrixEx(numRows, numColumns);

        for (int i = numColumns - 1; i >= 0; i--)
        {
            int q = i >>> BLOCKEXP;
            int r = i & 0x1f;
            int pq = pVec[i] >>> BLOCKEXP;
            int pr = pVec[i] & 0x1f;
            for (int j = numRows - 1; j >= 0; j--)
            {
                result.matrix[j][q] |= ((matrix[j][pq] >>> pr) & 1) << r;
            }
        }

        return result;
    }

    /**
     * Compute the product of this matrix and the given column vector.
     *
     * @param vec the vector over GF(2)
     * @return <tt>this*vector</tt>
     */
    public Vector rightMultiply(Vector vec)
    {
        if (!(vec instanceof GF2Vector))
        {
            throw new ArithmeticException("vector is not defined over GF(2)");
        }

        if (vec.getLength() != numColumns)
        {
            throw new ArithmeticException("length mismatch");
        }

        int[] v = ((GF2Vector)vec).getVecArray();
        int[] res = new int[(numRows + (INTSIZE-1)) >>> BLOCKEXP];

        for (int i = 0; i < numRows; i++)
        {
            // compute full word scalar products
            int help = 0;
            for (int j = 0; j < length; j++)
            {
                help ^= matrix[i][j] & v[j];
            }
            // compute single word scalar product
            int bitValue = 0;
            for (int j = 0; j < INTSIZE; j++)
            {
                bitValue ^= (help >>> j) & 1;
            }
            // set result bit
            if (bitValue == 1)
            {
                res[i >>> BLOCKEXP] |= 1 << (i & 0x1f);
            }
        }

        return new GF2Vector(numRows, res);
    }

    /**
     * Compute the product of the matrix <tt>(Id | this)</tt> and a column
     * vector, where <tt>Id</tt> is a <tt>(numRows x numRows)</tt> unit
     * matrix.
     *
     * @param vec the vector over GF(2)
     * @return <tt>(Id | this)*vector</tt>
     */
    public Vector rightMultiplyRightCompactForm(Vector vec)
    {
        if (!(vec instanceof GF2Vector))
        {
            throw new ArithmeticException("vector is not defined over GF(2)");
        }

        if (vec.getLength() != numColumns + numRows)
        {
            throw new ArithmeticException("length mismatch");
        }

        int[] v = ((GF2Vector)vec).getVecArray();
        int[] res = new int[(numRows + (INTSIZE-1)) >>> BLOCKEXP];

        int q = numRows >> BLOCKEXP;
        int r = numRows & 0x1f;

        // for all rows
        for (int i = 0; i < numRows; i++)
        {
            // get vector bit
            int help = (v[i >> BLOCKEXP] >>> (i & 0x1f)) & 1;

            // compute full word scalar products
            int vInd = q;
            // if words have to be shifted
            if (r != 0)
            {
                int vw = 0;
                // process all but last word
                for (int j = 0; j < length - 1; j++)
                {
                    // shift to correct position
                    vw = (v[vInd++] >>> r) | (v[vInd] << (INTSIZE - r));
                    help ^= matrix[i][j] & vw;
                }
                // process last word
                vw = v[vInd++] >>> r;
                if (vInd < v.length)
                {
                    vw |= v[vInd] << (INTSIZE - r);
                }
                help ^= matrix[i][length - 1] & vw;
            }
            else
            {
                // no shifting necessary
                for (int j = 0; j < length; j++)
                {
                    help ^= matrix[i][j] & v[vInd++];
                }
            }

            // compute single word scalar product
            int bitValue = 0;
            for (int j = 0; j < INTSIZE; j++)
            {
                bitValue ^= help & 1;
                help >>>= 1;
            }

            // set result bit
            if (bitValue == 1)
            {
                res[i >> BLOCKEXP] |= 1 << (i & 0x1f);
            }
        }

        return new GF2Vector(numRows, res);
    }

    /**
     * Compare this matrix with another object.
     *
     * @param other another object
     * @return the result of the comparison
     */
    @Override
    public boolean equals(Object other)
    {

        if (!(other instanceof GF2MatrixEx))
        {
            return false;
        }
        GF2MatrixEx otherMatrix = (GF2MatrixEx)other;

        if ((numRows != otherMatrix.numRows)
            || (numColumns != otherMatrix.numColumns)
            || (length != otherMatrix.length))
        {
            return false;
        }

        for (int i = 0; i < numRows; i++)
        {
            if (!IntUtils.equals(matrix[i], otherMatrix.matrix[i]))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * @return the hash code of this matrix
     */
    @Override
    public int hashCode()
    {
        int hash = (numRows * (INTSIZE-1) + numColumns) * (INTSIZE-1) + length;
        for (int i = 0; i < numRows; i++)
        {
            hash = hash * (INTSIZE-1) + matrix[i].hashCode();
        }
        return hash;
    }

    /**
     * @return a human readable form of the matrix
     */
    public String toString()
    {
        int rest = numColumns & 0x1f;
        int d;
        if (rest == 0)
        {
            d = length;
        }
        else
        {
            d = length - 1;
        }

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < numRows; i++)
        {
            buf.append(i).append(": ");
            for (int j = 0; j < d; j++)
            {
                int a = matrix[i][j];
                for (int k = 0; k < INTSIZE; k++)
                {
                    int b = (a >>> k) & 1;
                    if (b == 0)
                    {
                        buf.append('0');
                    }
                    else
                    {
                        buf.append('1');
                    }
                }
                buf.append(' ');
            }
            int a = matrix[i][length - 1];
            for (int k = 0; k < rest; k++)
            {
                int b = (a >>> k) & 1;
                if (b == 0)
                {
                    buf.append('0');
                }
                else
                {
                    buf.append('1');
                }
            }
            buf.append('\n');
        }

        return buf.toString();
    }

    /**
     * Swap two rows of the given matrix.
     *
     * @param matrix the matrix
     * @param first  the index of the first row
     * @param second the index of the second row
     */
    public static void swapRows(int[][] matrix, int first, int second)
    {
        int[] tmp = matrix[first];
        matrix[first] = matrix[second];
        matrix[second] = tmp;
    }
    
    /**
     * Sets GF2 element to given row vector position.
     * @param row
     * @param col
     * @param b 
     */
    public static void set(int[]row, int col, int b){
        if (b==0){
           row[col >>> BLOCKEXP] &= ~(1 << (col & INTMASK));
        } else {
           row[col >>> BLOCKEXP] |=  (1 << (col & INTMASK));
        }
    }
    
    /**
     * Sets GF2 element on given 2D coordinates for given matrix.
     * 
     * @param matrix
     * @param row
     * @param col
     * @param b 
     */
    public static void set(int[][]matrix, int row, int col, int b){
        set(matrix[row], col, b);
    }
    
    
    /**
     * Sets GF2 element on given 2D coordinates.
     * 
     * @param row
     * @param col
     * @param b 
     */
    public void set(int row, int col, int b){
        set(matrix, row, col, b);
    }
    
    /**
     * Sets GF2 element on given 2D coordinates.
     * Synonym for set() for backward compatibility with NTL.
     * 
     * @param row
     * @param col
     * @param b 
     */
    public void put(int row, int col, int b){
        set(matrix, row, col, b);
    }
    
    /**
     * Returns whether given GF2 in given row vector is set.
     * 
     * @param row
     * @param col
     * @return 
     */
    public static boolean isSet(int[] row, int col){
        return (row[col >>> BLOCKEXP] & (1 << (col & INTMASK))) != 0;
    }
    
    /**
     * Returns whether given GF2 in given matrix is set.
     * 
     * @param matrix
     * @param row
     * @param col
     * @return 
     */
    public static boolean isSet(int[][] matrix, int row, int col){
        return isSet(matrix[row], col);
    }
    
    /**
     * Returns whether given GF2 is set.
     * 
     * @param matrix
     * @param row
     * @param col
     * @return 
     */
    public boolean isSet(int row, int col){
        return isSet(matrix[row], col);
    }
    
    /**
     * Returns GF2 element on given 2D coordinates on given row vector.
     * @param row
     * @param col
     * @return 
     */
    public static int get(int[] row, int col){
        return (row[col >>> BLOCKEXP] & (1 << (col & INTMASK))) >> (col & INTMASK);
    }
    
    /**
     * Returns GF2 element on given 2D coordinates on given matrix.
     * 
     * @param row
     * @param col
     * @return 
     */
    public static int get(int[][] matrix, int row, int col){
        return get(matrix[row], col);
    }
    
    /**
     * Returns GF2 element on given 2D coordinates
     * @param row
     * @param col
     * @return 
     */
    public int get(int row, int col){
        return get(matrix, row, col);
    }
    
    public static void swapCols(int[][]matrix, int a, int b){
       final int amask = 1 << (a & INTMASK);
       final int bmask = 1 << (b & INTMASK);
       final int numRows = matrix.length;
       for(int i=0; i<numRows; i++){
           final int va = matrix[i][a >>> BLOCKEXP] & amask;
           set(matrix, i, a, matrix[i][b >>> BLOCKEXP] & bmask);
           set(matrix, i, b, va);
       }  
    }
    
    /**
     * Swaps two columns on this matrix.
     * 
     * @param a
     * @param b 
     */
    public void swapCols(int a, int b){
       swapCols(matrix, a, b);
    }

    /**
     * Partially add one row to another.
     *
     * @param fromRow    the addend
     * @param toRow      the row to add to
     * @param startIndex the array index to start from
     */
    public static void addToRow(int[] fromRow, int[] toRow, int startIndex)
    {
        for (int i = toRow.length - 1; i >= startIndex; i--)
        {
            toRow[i] = fromRow[i] ^ toRow[i];
        }
    }
    
    /**
     * Add a to column j of x. ALIAS RESTRICTION: a should not alias any row of
     * x.
     * 
     * @param matrix
     * @param j
     * @param vector 
     */
    public static void addToCol(int[][] matrix, int j, int[] a, int alen){
        int n = matrix.length;
        if (a.length < alen || alen!= n || j < 0 || matrix[0] == null || (j>>>BLOCKEXP) >= matrix[0].length) {
            throw new IllegalArgumentException("Vector does not match matrix or illegal column coordinate");
        }
        
        final int wj   = j >>> BLOCKEXP;
        final int bj   = j & INTMASK;
        final int mask = 1 << bj;
        for (int i = 0; i < n; i++) {
            if ((a[wj] & mask) != 0) {
                matrix[i][wj] ^= mask;
            }
        }        
    }
    
   /**
    * Extended Inversion version - returns P,Q matrices in
    * matrix A decomposition PAQ = R where R is in canonical form.
    *
    * Canonical form = zero matrix with chain of ones on main diagonal with 
    * length of the rank of the matrix.
    *
    * Returns rank of matrix
    */
   public static int normalize(NormalGF2MatrixHolder h, final GF2MatrixEx A)
   {
      int n = A.getNumRows();
      if (A.getNumColumns() != n){
         throw new IllegalArgumentException("solve: nonsquare matrix");
      }

      if (n == 0) {
          h.setP(new GF2MatrixEx(0, MATRIX_TYPE_ZERO));
          h.setDetetrminant(1);
      }

      int i, j, k, pos;
      int rank=n;

      //
      // Gauss Jordan Elimination. Matrix M is extended version
      // with 2*N columns. Copy of A is in the left half, unity
      // matrix is in the right half.
      //
      GF2MatrixEx M  = new GF2MatrixEx(n, 2*n);

      // Initializing Q matrix as unit matrix, will correspond to
      // column operations performed to obtain canonical form.
      // Since matrix is represented as array of vectors (rows),
      // we will work with transpose version of matrix.
      GF2MatrixEx P = new GF2MatrixEx(n, MATRIX_TYPE_ZERO);
      GF2MatrixEx Q = new GF2MatrixEx(n, MATRIX_TYPE_UNIT);
      Q = (GF2MatrixEx) Q.computeTranspose();

      // Generate M matrix - left half contains A, right half unity matrix
            int[][] Pi = P.getIntArray();
            int[][] Qi = Q.getIntArray();
            int[][] Mi = M.getIntArray();
      final int[][] Ai = A.getIntArray();
      for (i = 0; i < n; i++) {
          // Copy A[] to the left part of the matrix
          System.arraycopy(Ai[i], 0, Mi[i], 0, ((n-1) >>> GF2MatrixEx.BLOCKEXP)+1);

          // unit matrix in second half
          final int q = (n+i) >> GF2MatrixEx.BLOCKEXP;   // number of "full" integer
          final int r = (n+i) & 0x1f;                    // number of bits in non-full integer; 0x1f=32dec
          Mi[i][q] |= 1 << r;
      }

       // Finding leading ones on k-th position.
       int wn = ((2*n) + GF2MatrixEx.INTMASK)/GF2MatrixEx.INTSIZE;
       for (k = 0; k < n; k++) {
           int wk = k >>> GF2MatrixEx.BLOCKEXP;
           int bk = k & GF2MatrixEx.INTMASK;
           int k_mask = 1 << bk;

           //System.out.println("Intermediate result in step=" + k + "; Matrix\n" + M.toString());

           // Find leading one in rows on k-th position in row.
           // Search in interval [k,n] (thus from current row down).
           pos = -1;
           for (i = k; i < n; i++) {
               if ((Mi[i][wk] & k_mask) != 0) {
                   pos = i;
                   break;
               }
           }

           //System.out.println("Line pos: [" + pos + "] has leading 1 on [" + k + "]. position");

           if (pos == -1) {
               // If here it means there is no row in matrix that has leading
               // 1 on k-th position.
               //
               // Thus now look in rows [k,n] and find some row that has
               // 1 element on position > k. Then we will perform column swap
               // to obtain 1 element on desired position = k. This change has to be
               // reflected to Q matrix.
               //
               // Finding unit element on position k+1 in all rows [k,n].
               // If fails, look for unit element on position k+2 in all rows [k,n]...
               int kk, ii, colpos = -1;
               for (kk = k + 1; kk < n; kk++) {
                   int kwk = kk >>> GF2MatrixEx.BLOCKEXP;
                   int kbk = kk &   GF2MatrixEx.INTMASK;
                   int kk_mask = 1 << kbk;
                   colpos = kk;

                   // Find leading one in rows on kk-th position in row.
                   // Search in interval [k,n] (thus from current row down).
                   //System.out.println("Looking for leading 1 element in column: " + kk + "; mask: " + kk_mask);

                   pos = -1;
                   for (ii = k; ii < n; ii++) {
                       if ((Mi[ii][kwk] & kk_mask) != 0) {
                           pos = ii;
                           break;
                       }
                   }
                   if (pos != -1) {
                       break;
                   }
               }

               if (pos == -1) {
                   // No such column exists, thus just simply null rest of columns in Q matrix
                   // to obtain canonical form of product P*A*Q.
                   rank = k;

                   //System.out.println("No such column exists, we are already in canonical form;"
                   //        + "nulling all columns from: " + k + "; Rank: " + rank);

                   for (kk = k; kk < n; kk++) {
                       for (ii = 0; ii < n; ii++) {
                           Qi[kk][ii >>> GF2MatrixEx.BLOCKEXP] &= ~(1 << (ii & GF2MatrixEx.INTMASK));
                       }
                   }

                   break;
               }

               //System.out.println("Swaping column [" + k + "] with column [" + colpos + "]. Matrix: ");

               // Do column swap to obtain 1 on desired k-th position from colpos-th position.
               // pos = row index with leading 1
               // k   = column with leading 1
               // colpos = column that has leading 1 > k.
               GF2MatrixEx.swapCols(Mi, k, colpos);

               // reflect this swap to Q matrix, swap rows (transpose form)
               GF2MatrixEx.swapRows(Qi, k, colpos);
               //System.out.println(M + "\nQmatrix: \n" + Q + "\n");
           }

           if (pos != -1) {
               // row number <pos> has leading one on k-th position
               if (k != pos) {
                   //System.out.println("Swap line " + pos + " with line " + k);
                   GF2MatrixEx.swapRows(Mi, k, pos);
               }

               // M[i] = M[i] + M[k]*M[i,k]
               // Normalizing all rows in matrix but k.
               // Previously we were normalizing only rows down the k
               for (i = 0; i < n; i++) {
                   if ((i != k) && ((Mi[i][wk] & k_mask) != 0)){
                       GF2MatrixEx.addToRow(Mi[k], Mi[i], 0);
                   }
               }
           }
       }

       //System.out.println("After elimination: \n" + M);

       // Building inverse matrix
       P = M.getRightSubMatrix();

       // transpose Q matrix finally
       Q = (GF2MatrixEx) Q.computeTranspose();

       h.setQ(Q);
       h.setP(P);
       h.setDetetrminant(rank == n ? 1 : 0);
       h.setRank(rank);
       return rank;
   }
   
    /**
    * Extended Inversion version - returns P,Q matrices in
    * matrix A decomposition PAQ = R where R is in canonical form.
    *
    * Canonical form = zero matrix with chain of ones on main diagonal with 
    * length of the rank of the matrix.
    *
    * Returns rank of matrix
    */
   public int normalize(NormalGF2MatrixHolder h)
   {
       return normalize(h, this);
   }
   
   /**
    * Adds matrix m to this matrix
    * 
    * @param m
    * @return A
    */
   public void add(GF2MatrixEx m){
       final int rows = this.getNumRows();
       final int cols = this.getNumColumns();
       if (cols != m.getNumColumns() || rows!=m.getNumRows()){
           throw new IllegalArgumentException("Matrix dimension mismatch");
       }
       
       final int[][] mi = m.getIntArray();
       final int len    = ((cols -1) >>> GF2MatrixEx.BLOCKEXP)+1;
       for(int i=0; i<rows; i++){
           for(int j=0; j<len; j++){
               matrix[i][j] ^= mi[i][j];
           }
       }
   }
}
