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
package cz.muni.fi.xklinec.whiteboxAES;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Implements basic XOR cascade 16x128 bit -> 128 bit
 *
 * [01] [23] [45] [67] [89] [1011] [1213] [1415]
 *  0     1   2     3   4     5      6      7
 *   \   /     \   /     \   /        \    /
 *  [0123]    [4567]   [891011]    [12131415]
 *     8         9        10          11
 *      \       /          \          /
 *       \     /            \        /
 *      [01234567]       [89101112131415]
 *          12                  13
 *           \                 /
 *            \               /
 *        [0123456789101112131415]
 *                   14
 *
 * @author ph4r05
 */
public class XORCascadeState implements Serializable{
    public static final int WIDTH  = State.BYTES;
    public static final int BOXES  = WIDTH-1;               // input size of 1 box is 4 bits; WIDTH-1 operations needed to XOR N operands.
    public static final int STAGES = Utils.binlog(WIDTH);  // log(WIDTH) / log(2). Number of stages of XORs
    protected XORBoxState[] x = null;

    public XORCascadeState() {
        x = new XORBoxState[BOXES];
        for(int i=0; i<BOXES; i++){
            x[i] = new XORBoxState();
        }
    }
    
    public XORCascadeState(XORBoxState[] xtbl) {
        setXor(xtbl);
    }
    
    /**
     * Returns: XOR of 16xState, result will be returned to the states[0]
     * 
     * @param states
     * @return 
     */
    public State xorSafe(State[] states){
        if (states==null) {
            throw new NullPointerException("Input state is null");
        }
        
        if (states.length!=WIDTH) {
            throw new IllegalArgumentException("There has to be exactly " + WIDTH + " elements, currently=" + states.length);
        }
        
        return xor(states);
    }
    
    /**
     * Returns: XOR of 16xState, result will be returned to the states[0].
     * No input check is performed.
     * 
     * @param states
     * @return 
     */
    public State xor(State[] states){
        return xor(x, states);
    }
    
    /**
     * Returns: XOR of 16xState, result will be returned to the states[0].
     * No input check is performed. 
     * Own XOR box is provided.
     * 
     * @param x
     * @param states
     * @return 
     */
    public static State xor(XORBoxState[] x, State[] states){
        // offset in x[] for current stage
        int XORoffset = 0;  
        // j is XOR stage number
        for(int j=0; j<4; j++){
            // Number of iterations in each stage is 8,4,2,1. i.e. 2^3, 2^2, 2^1, 2^0
            final int iterationsInStage = 1 << (3-j);
            // Step of XORing neighbouring states in one stage; 1st: 0+1, 2+3,...; 2nd: 0+2, 2+4,...; 3rd 0+4,...
            final int xorBoxStep        = 1 <<    j;  
            // performing XOR inside one 
            for(int i=0; i<iterationsInStage; i++){
                // Position to state[] for current stage
                int pos = 0;
                x[XORoffset + i].xorA(states[2*i*xorBoxStep], states[2*i*xorBoxStep+xorBoxStep]);  // states[2*i] := states[2*i] XOR states[2*i+1];
            }
            
            XORoffset += iterationsInStage;
        }
        
        return states[0];
    }
    
    /**
     * Sets sub XORBoxes
     * @param xtbl 
     */
    public final void setXor(XORBoxState[] xtbl){
        x = xtbl;
    }
    
    /**
     * Sets one of three XORBoxes
     * @param xtbl
     * @param idx 
     */
    public void setXor(XORBoxState xtbl, int idx){
        if (x==null){
            throw new NullPointerException("XOR boxes are not initialized, initialize first.");
        }
        
        x[idx] = xtbl;
    }

    public XORBoxState[] getX() {
        return x;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Arrays.deepHashCode(this.x);
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
        final XORCascadeState other = (XORCascadeState) obj;
        return Arrays.deepEquals(this.x, other.x);
    }
}
