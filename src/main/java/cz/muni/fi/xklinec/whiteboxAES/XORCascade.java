/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES;

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
public class XORCascade {
    public static final int L=0;
    public static final int R=1;
    public static final int C=2;
    private XORBox[] x = null;

    public XORCascade() {
        x = new XORBox[3];
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
        return x[C].xor(x[L].xor(a, b), x[R].xor(c, d));
    }
    
    /**
     * Sets sub XORBoxes
     * @param xtbl 
     */
    public final void setXor(XORBox[] xtbl){
        x = xtbl;
    }
    
    /**
     * Sets one of three XORBoxes
     * @param xtbl
     * @param idx 
     */
    public void setXor(XORBox xtbl, int idx){
        if (x==null){
            throw new NullPointerException("XOR boxes are not initialized, initialize first.");
        }
        
        x[idx] = xtbl;
    }
    
}
