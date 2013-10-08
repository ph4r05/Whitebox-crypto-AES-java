/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents (128bit) XOR (128bit) box for whitebox transformation.
 * Holds XOR tables and performs operation. Only for whitebox runtime,
 * not for design.
 * 
 * Short & simple methods are good candidates for inlining.
 * @see {ftp://ftp.glenmccl.com/pub/free/jperf.pdf}
 * 
 * @author ph4r05
 */
public class XORBoxState implements Serializable{
    public static final int WIDTH = State.BYTES;
    public static final int BOXES = WIDTH*2;    // input size of 1 box is 4 bits
    
    // Main XOR table 
    // represents 8 -> 4 bit XOR tables
    private byte[][] xor = null;
    
    /**
     * Empty constructor
     */
    public XORBoxState() {
        init();
    }
    
    /**
     * XOR box constructor with initialized xor table.
     * @param xorTbl
     * @param copy 
     */
    public XORBoxState(final byte[][] xorTbl, boolean copy) {
        setXor(xorTbl, copy);
    }
        
    /**
     * Performs XOR on two 128 bit operands a,b.
     * 
     * @param a
     * @param b
     * @return 
     */
    public State xor(State a, State b){
        return xor(xor, a, b);
    }
    
    /**
     * Performs XOR on two 128 bit operands a,b.
     * Uses provided XOR table.
     * 
     * @param xor
     * @param a
     * @param b
     * @return 
     */
    public static State xor(final byte[][] xor , State a, State b){
        byte[] s = State.initExt();
        final byte ar[] = a.getState();
        final byte br[] = b.getState();
        for (int i=0; i<WIDTH; i++){
            s[i] = (byte)( 
                    (xor[2*i+0][(((ar[i] >> 4) & 0xF) << 4) | ((br[i] >> 4) & 0xF)] << 4)
                  | (xor[2*i+1][(( ar[i]       & 0xF) << 4) | ( br[i]       & 0xF)]     ) 
                   );
        }

        return new State(s);
    }
    
    
    /**
     * Performs XOR on two 128 bit operands a,b while storing result to a.
     * @param a
     * @param b
     * @return 
     */
    public State xorA(State a, State b){
        return xorA(xor, a, b);
    }
    
    /**
     * Performs XOR on two 128 bit operands a,b while storing result to a.
     * Uses provided XOR table.
     * 
     * @param a
     * @param b
     * @return 
     */
    public static State xorA(final byte[][] xor, State a, State b){
              byte ar[] = a.getState();
        final byte br[] = b.getState();
        for (int i=0; i<WIDTH; i++){
            ar[i] = (byte)( 
                    (xor[2*i+0][(((ar[i] >> 4) & 0xF) << 4) | ((br[i] >> 4) & 0xF)] << 4)
                  | (xor[2*i+1][(( ar[i]       & 0xF) << 4) | ( br[i]       & 0xF)]     ) 
                   );
        }
        
        a.setState(ar, false);
        return a;
    }

    /**
     * Sets XOR box internal table
     * @param xorTbl
     * @param copy      - if yes array is copied
     */
    public final void setXor(final byte[][] xorTbl, boolean copy){
        if (xorTbl.length != BOXES) {
            throw new IllegalArgumentException("XOR table has to have 8 sub-tables");
        }
        
        if (copy){
            xor = new byte[BOXES][];
            for (int i=0; i < BOXES; i++) {
                xor[i] = Arrays.copyOf(xorTbl[i], 256);
            }
        } else {
            this.xor = xorTbl;
        }
    }
    
    /**
     * Sets XOR box internal table
     * @param xorTbl 
     */
    public void setXor(final byte[][] xorTbl){
        setXor(xorTbl, true);
    }
    
    /**
     * Sets 1 sub-table in XOR table with 8 rows.
     * @param xorTbl
     * @param idx
     * @param copy 
     */
    public final void setPartXor(final byte[] xorTbl, final int idx, final boolean copy){
        if (xorTbl.length != 256) {
            throw new IllegalArgumentException("XOR table has to have 256 rows");
        }
        
        if (xor == null){
            throw new NullPointerException("Internal table is completely null, initialize xor table");
        }
        
        if (copy){     
            xor[idx] = Arrays.copyOf(xorTbl, 256);
        } else {
            this.xor[idx] = xorTbl;
        }
    }
    
    /**
     * Sets 1 sub-table in XOR table with 8 rows.
     * @param xorTbl
     * @param idx
     * @param copy 
     */
    public void setPartXor(final byte[] xorTbl, final int idx){
        setPartXor(xorTbl, idx, true);
    }
    
    /**
     * Initializes XOR tables - memory allocation
     */
    public final void init(){
        xor = new byte[BOXES][];
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Arrays.deepHashCode(this.xor);
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
        final XORBoxState other = (XORBoxState) obj;
        if (!Arrays.deepEquals(this.xor, other.xor)) {
            return false;
        }
        return true;
    }
}
