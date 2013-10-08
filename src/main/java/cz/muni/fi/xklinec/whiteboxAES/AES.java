/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES;

import java.util.Arrays;

/**
 * Main AES whitebox table implementation.
 * 
 * @author ph4r05
 */
public class AES {
    public static final int BYTES  = State.BYTES;
    public static final int ROUNDS = 10;
    public static final int T1BOXES = 2;
    public static final int T1Boxes = 2;
    public static final int shiftRows[] = {
        0,   1,  2,  3,
        5,   6,  7,  4,
        10, 11,  8,  9,
        15, 12, 13, 14
    };
    
    public static final int shiftRowsInv[] = {
        0,  1,  2,  3,
        7,  4,  5,  6,
       10, 11,  8,  9,
       13, 14, 15, 12
    };
    
    private T1Box[][]         t1       = new T1Box[T1BOXES][State.BYTES];
    private XORCascadeState[] xorState = new XORCascadeState[T1BOXES];
    private T2Box[][]         t2       = new T2Box[ROUNDS][State.BYTES];
    private T3Box[][]         t3       = new T3Box[ROUNDS][State.BYTES];
    private XORCascade[][]    xor      = new XORCascade[ROUNDS][2*State.COLS*ROUNDS];
    private boolean           encrypt  = true;

    /**
     * Encryption OR decryption - depends on generated tables
     * @param in 
     */
    public void crypt(State in){
        int r=0, i=0;
	W32b  ires[] = new W32b[BYTES];	// intermediate result for T2,T3-boxes
	State ares[] = new State[BYTES];	// intermediate result for T1-boxes
        
        
        // At first we have to put input to T1 boxes directly, no shift rows
	// compute result to ares[16]
	for(i=0; i<BYTES; i++){
            // Note: Tbox is indexed by cols, state by rows - transpose needed here
            ares[i].loadFrom( t1[0][i].lookup(in.get(i)) );
        }
        
        // now compute XOR cascade from 16 x 128bit result after T1 application.
        xorState[0].xor(ares);
        
        // Compute 9 rounds of T2 boxes
        // TODO: 
        
        
    }
    
    /**
     * Returns needed shift operation according to cipher direction (enc vs. dec).
     * 
     * @param encrypt
     * @return 
     */
    public static int[] getShift(boolean encrypt){
        return encrypt ? shiftRows : shiftRowsInv;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Arrays.deepHashCode(this.t1);
        hash = 89 * hash + Arrays.deepHashCode(this.xorState);
        hash = 89 * hash + Arrays.deepHashCode(this.t2);
        hash = 89 * hash + Arrays.deepHashCode(this.t3);
        hash = 89 * hash + Arrays.deepHashCode(this.xor);
        hash = 89 * hash + (this.encrypt ? 1 : 0);
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
        final AES other = (AES) obj;
        if (!Arrays.deepEquals(this.t1, other.t1)) {
            return false;
        }
        if (!Arrays.deepEquals(this.xorState, other.xorState)) {
            return false;
        }
        if (!Arrays.deepEquals(this.t2, other.t2)) {
            return false;
        }
        if (!Arrays.deepEquals(this.t3, other.t3)) {
            return false;
        }
        if (!Arrays.deepEquals(this.xor, other.xor)) {
            return false;
        }
        if (this.encrypt != other.encrypt) {
            return false;
        }
        return true;
    }
}
