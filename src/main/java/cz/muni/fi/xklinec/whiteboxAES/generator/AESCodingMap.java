/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.AES;
import cz.muni.fi.xklinec.whiteboxAES.State;
/**
 *
 * @author ph4r05
 */
public class AESCodingMap {					      // 15*4*8, used with T1 tables
    public static final int BYTES  = AES.BYTES;
    public static final int ROUNDS = AES.ROUNDS;
    public static final int T1BOXES = AES.T1BOXES;
    public static final int T1Boxes = AES.T1Boxes;   
    
    private GTBox8to128[][]    t1        = null;
    private GXORCascadeState[] xorState  = null;
    private GTBox8to32[][]     t2        = null;
    private GTBox8to32[][]     t3        = null;
    private GXORCascade[][]    xor       = null;
    private boolean            encrypt   = true;
    private int                idx       = 0;
    
    public static int transpose(int idx){
        return State.transpose(idx);
    }
    
    public int shiftOp(int idx){
        return AES.shift(idx, !encrypt);
    }
    
    /**
     * Memory allocation of each box
     */
    public void init(){
        int i,r;
        
        t1        = new GTBox8to128[T1BOXES][BYTES];
        xorState  = new GXORCascadeState[T1BOXES];
        t2        = new GTBox8to32[ROUNDS][BYTES];
        t3        = new GTBox8to32[ROUNDS][BYTES];
        xor       = new GXORCascade[ROUNDS][2*State.COLS];

        for(r=0; r<ROUNDS; r++){
            //
            // XOR state cascade
            //
            if (r<T1BOXES){
                xorState[r] = new GXORCascadeState();
            }
            
            for(i=0; i<BYTES; i++){
                
                //
                // T1 boxes
                //
                if (r<T1BOXES){
                    t1[r][i] = new GTBox8to128();
                }
                
                //
                // T2, T3 boxes
                //
                t2[r][i] = new GTBox8to32();
                t3[r][i] = new GTBox8to32();
                
                //
                // XOR cascade
                //
                if (i < 2*State.COLS){
                    xor[r][i] = new GXORCascade();
                }
            }
        }
    }
    
    /**
     * generate coding map for AES for IO bijections
     */
    public void generateCodingMap(){
        int i,j,r;
        this.idx = 0;
        
        // At first allocate new bijections for T1 output tables
	// Allocate encodings for XOR cascade summing output of T1 boxes
        for(r=0; r<2; r++){
            // Allocate bijections for T1 boxes
            for(i=0; i<BYTES; i++){
                idx = t1[r][i].allocate(idx);
            }
            
            // Allocate XOR cascade state bijections
            // XOR table cascade for T1 out sum, 8,4,2,1 = 15 XOR tables
            // Caution! Last 128-bit XOR table from T1[1] is output from whole cipher -> no allocation for this
            idx = xorState[r].allocate(idx, r==0);
            
            // Connecting part
            xorState[r].connectInternal();
            for(i=0; i<BYTES; i++){
                t1[r][i].connectOut(xorState[r], i);
            }
        }
        
        // Now connect XOR3 tables form R=0 (sums T1 input table) to input of T2 tables
	// Result is stored in last XOR table starting on 448 offset, result is stored in LOW value
	// Note that ShiftRows is done here, every Sbox uses result of ShiftRows operation on its input
	//
	// 128-bit XOR has output indexed by rows, same as state.
	//
	for(i=0; i<BYTES; i++){
            int newIdx = shiftOp(i);
            xorState[0].connectOut(t2[0][newIdx], i);
	}
        
        //
	// In the last round there is only T1 table, with defined output mapping by user (external)
	// so it is not allocated here. There are no XOR tables and T3 tables in 10. round.
	//
	// Thus encode only round 1..9.
	// Last round 9 output coding from XOR2 master table
	// is connected to T1[1] input coding in round 10.
	for(r=0; r<(ROUNDS-1); r++){
            //
            // Allocation part, OUTPUT direction creates/defines new mapping
            //
            for(i=0; i<BYTES; i++){
                idx = t2[r][i].allocate(idx);
                idx = t3[r][i].allocate(idx);
                if (i < 2*State.COLS){
                    idx = xor[r][i].allocate(idx);
                }
            }
            
            // iterate over strips/MC cols
            for(i=0; i<BYTES; i++){
                //
                // Connecting part - connecting allocated codings together
                //
                final int xorCol1 = 2*(i % State.COLS);     //2*(i/State.COLS);
                final int xorCol2 = 2*(i % State.COLS) + 1; //2*(i/State.COLS) + 1;
                final int slot    =    i / State.COLS;
                
                // Connect T2 boxes to XOR input boxes
                t2[r][i].connectOut(xor[r][xorCol1], slot);
                
                // XOR boxes, one per column
                if ((i / State.COLS) == (State.ROWS-1)){
                    // Connect XOR layer 1 to XOR layer 2
                    xor[r][xorCol1].connectInternal();
                }
                
                // Connect result XOR layer 2 to B boxes (T3)
                xor[r][xorCol1].connectOut(t3[r][i], slot);
                
                // Connect B boxes to XOR
                t3[r][i].connectOut(xor[r][xorCol2], slot);
                
                // Connect XOR layer 3 to XOR layer 4
                if ((i / State.COLS) == (State.ROWS-1)){
                    xor[r][xorCol2].connectInternal();
                }
                
                if (r<(ROUNDS-2)){
                    // Connect result XOR layer 4 to T2 boxes in next round
                    xor[r][xorCol2].connectOut(t2[r+1][shiftOp(i)], slot);
                } else {
                    // Connect result XOR layer 4 to T1 boxes in last round; r==8
                    xor[r][xorCol2].connectOut(t1[1][shiftOp(i)], slot);
                }
            }
	}
    }

    public GTBox8to128[][] getT1() {
        return t1;
    }

    public GXORCascadeState[] getXorState() {
        return xorState;
    }

    public GTBox8to32[][] getT2() {
        return t2;
    }

    public GTBox8to32[][] getT3() {
        return t3;
    }

    public GXORCascade[][] getXor() {
        return xor;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public int getIdx() {
        return idx;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }
}
