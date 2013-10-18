/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.Utils;
import org.bouncycastle.pqc.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.math.linearalgebra.LittleEndianConversions;

/**
 *
 * @author ph4r05
 */
public class NTLUtils {
    public static final int ENCODED_OFFSET=8; // where matrix data really start in encoded form
    
    /**
     * Generates encoded form for GF2Matrix of given size.
     * @param rows
     * @param cols
     * @return 
     */
    public static byte[] generateGF2MatrixEncodedForm(int rows, int cols){
        // Need to generate encoded form.
        final int rowBytes = Utils.ceil(cols / 8.0);
        final int size     = rowBytes * rows;
        byte[] encForm      = new byte[ENCODED_OFFSET + size];
        LittleEndianConversions.I2OSP(rows, encForm, 0);
        LittleEndianConversions.I2OSP(cols, encForm, 4);
        
        return encForm;
    }
    
    /**
     * Generates zero GF2 matrix with <rows> rows and <cols> columns.
     * @param rows
     * @param cols
     * @return 
     */
    public static GF2Matrix generateGF2Matrix(int rows, int cols){
        return new GF2Matrix(generateGF2MatrixEncodedForm(rows, cols));
    }
    
    /**
     * Generates zero GF2 matrix with <rows> rows and <cols> columns.
     * @param rows
     * @param cols
     * @return 
     */
    public static GF2Matrix generateGF2Matrix(long rows, long cols){
        return new GF2Matrix(generateGF2MatrixEncodedForm((int)rows, (int)cols));
    }
    
    /**
     * Takes 8bit number (BYTE / unsigned char) and stores its bit representation to col vector
     * starting at given coordinates to array (may be mat_GF2). LSB first.
     * 
     * @param m
     * @param src
     * @param i     row
     * @param j     column
     */
    public static void putByteAsColVector(GF2MatrixEx m, byte c, int i, int j){
        for(int k=0; k<8; k++){
            m.set(i+k, j, c & (1<<k));
        }
    }
    
    
    /**
     * Takes 8bit number (BYTE / unsigned char) and stores its bit representation to row vector
     * starting at given coordinates to array (may be mat_GF2). LSB first.
     * 
     * @param m
     * @param src
     * @param i     row
     * @param j     column
     */
    public static void putByteAsRowVector(GF2MatrixEx m, byte c, int i, int j){
        for(int k=0; k<8; k++){
            m.set(i, j+k, c & (1<<k));
        }
    }
    
    /**
     * Assembles 8bit number (BYTE / unsigned char) from bit representation in column vector. LSB first.
     * @param src
     * @param i     row
     * @param j     column
     * @return 
     */
    public static byte ColBinaryVectorToByte(final GF2MatrixEx src, int i, int j){
        byte res = 0;
        for(int k=0; k<8; k++){
            res |= src.get(i+k, j)==0 ? 0 : 1<<k;
        }
        
        return res;
    }
    
    /**
     * Assembles 8bit number (BYTE / unsigned char) from bit representation in column vector. LSB first.
     * @param src
     * @param i     row
     * @param j     column
     * @return 
     */
    public static byte RowBinaryVectorToByte(final GF2MatrixEx src, int i, int j){
        byte res = 0;
        for(int k=0; k<8; k++){
            res |= src.get(i, j+k)==0 ? 0 : 1<<k;
        }
        
        return res;
    }
    
    /**
     * Sets whole matrix to zero
     * 
     * @param m
     * @return 
     */
    public static void zero(GF2MatrixEx m){
        if (m.getNumRows()==0 || m.getNumColumns()==0) {
            return;
        }
        
        int[][] mi  = m.getIntArray();        
        final int k = mi[0].length;
        for(int i=0; i<mi.length; i++){
            for(int j=0; j<mi[i].length; j++){
                mi[i][j] = 0;
            }
        }
    }
    
    public static String chex(int n) {
        // call toUpperCase() if that's required
        return String.format("0x%02X", n);
    }
    
    
    /**
     * Converts matrix consisting of GF2E elements to binary matrix from element
     * representation, coding binary elements to columns. LSB is first in the
     * row, what is consistent with GenericAES.
     */
    public static GF2MatrixEx GF2mMatrix_to_GF2Matrix_col(final GF2mMatrixEx src, int elemLen) {
        int i, j, k, n = src.getNumRows(), m = src.getNumColumns();

        GF2MatrixEx dst = new GF2MatrixEx(elemLen * n, m);
        for (i = 0; i < n; i++) {
            for (j = 0; j < m; j++) {
                int curElem = src.get(i, j);
                for (k = 0; k < elemLen; k++) {
                    dst.set(i * elemLen + k, j, (curElem >>> k) & 0x1); //k <= xdeg ? curX[k] : 0);
                }
            }
        }

        return dst;
    }
    
    /**
     * Converts column of 32 binary values to W32b value
     * @param src
     * @param row
     * @param col
     * @return 
     */
    public static long GF2Matrix_to_long(final GF2MatrixEx src, int row, int col){
        //assert((src.NumRows()) < (row*8));
        //assert((src.NumCols()) < col);
        long dst = 0;
        dst |= Utils.byte2long(ColBinaryVectorToByte(src, row+8*0, col), 0);
        dst |= Utils.byte2long(ColBinaryVectorToByte(src, row+8*1, col), 1);
        dst |= Utils.byte2long(ColBinaryVectorToByte(src, row+8*2, col), 2);
        dst |= Utils.byte2long(ColBinaryVectorToByte(src, row+8*3, col), 3);
        return dst;
    }

}
