/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.Utils;
import org.bouncycastle.pqc.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.math.linearalgebra.GF2Vector;
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
     * @param i
     * @param j 
     */
    public static void putByteAsColVector(GF2MatrixEx m, byte c, int i, int j){
        for(int k=0; k<8; k++){
            m.set(i+k, j, c & (1<<k));
        }
    }
    
    /**
     * Assembles 8bit number (BYTE / unsigned char) from bit representation in column vector. LSB first.
     * @param src
     * @param i
     * @param j
     * @return 
     */
    public static byte ColBinaryVectorToByte(GF2MatrixEx src, int i, int j){
        byte res = 0;
        for(int k=0; k<8; k++){
            res |= src.get(i+k, j)==0 ? 0 : 1<<k;
        }
        
        return res;
    }
    
    
}
