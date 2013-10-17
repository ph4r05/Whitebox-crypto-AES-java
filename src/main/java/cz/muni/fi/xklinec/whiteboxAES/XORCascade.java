/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Implements basic XOR cascade 4x32 bit -> 32 bit.
 * 
 * XORCascade works like this:
 * 
 *   [ a] [ b] [ c] [ d]
 *    \    /     \   /  
 *     [ab]      [cd] 
 *       \       /    
 *        \     /     
 *        [abcd]
 * 
 * @author ph4r05
 */
public class XORCascade implements Serializable{
    public static final int L=0;
    public static final int R=1;
    public static final int C=2;
    
    public static final int WIDTH = 4;
    public static final int BOXES = WIDTH-1;
    
    protected XORBox[] x = null;

    public XORCascade() {
        x = new XORBox[BOXES];
    }
    
    public XORCascade(XORBox[] xtbl) {
        setXor(xtbl);
    }
    
    /**
     * Returns: (a XOR b) XOR (c XOR d)
     * @param a
     * @param b
     * @param c
     * @param d
     * @return 
     */
    public long xor(long a, long b, long c, long d){
        return xor(x, a, b, c, d);
    }
    
    /**
     * Returns: (a XOR b) XOR (c XOR d)
     * Uses provided XOR table.
     * 
     * @param a
     * @param b
     * @param c
     * @param d
     * @return 
     */
    public static long xor(final XORBox[] x, long a, long b, long c, long d){
        return x[C].xor(x[L].xor(a, b), x[R].xor(c, d));
    }
    
    /**
     * Sets sub XORBoxes, no copy, just assignment
     * @param xtbl 
     */
    public final void setXor(XORBox[] xtbl){
        x = xtbl;
    }
    
    /**
     * Sets one of three XORBoxes, no copy, just assignment
     * @param xtbl
     * @param idx 
     */
    public void setXor(XORBox xtbl, int idx){
        if (x==null){
            throw new NullPointerException("XOR boxes are not initialized, initialize first.");
        }
        
        x[idx] = xtbl;
    }

    public XORBox[] getX() {
        return x;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Arrays.deepHashCode(this.x);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XORCascade other = (XORCascade) obj;
        if (!Arrays.deepEquals(this.x, other.x)) {
            return false;
        }
        return true;
    }
}
