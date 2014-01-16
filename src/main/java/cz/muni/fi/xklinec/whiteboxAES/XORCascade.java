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
        for(int i=0; i<BOXES; i++){
            x[i] = new XORBox();
        }
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
