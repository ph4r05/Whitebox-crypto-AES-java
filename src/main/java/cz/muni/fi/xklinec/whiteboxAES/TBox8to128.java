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
 * Lookup table for whitebox AES implementation.
 * Implements 8 -> 128 bit lookup table
 * 
 * @author ph4r05
 */
public class TBox8to128 implements Serializable{
    public static final int IWIDTH=1;
    public static final int OWIDTH=16;
    public static final int ROWS  =1<<(8*IWIDTH);
    
    
    // Main lookup table
    protected State tbl[] = null;

    public TBox8to128() {
        init();
    }
    
    /**
     * Basic lookup, returns direct result
     * @param b
     * @return 
     */
    public State lookup(byte b){
        return tbl[AES.posIdx(b)];
    }
    
    /**
     * Copies resulting state to dst from lookup
     * @param dst
     * @param b 
     */
    public void lookup(State dst, byte b){
        dst.setState(tbl[b].getState(), true);
    }
    
    /**
     * Basic lookup, no copy, returns direct result
     * @param b
     * @return 
     */
    public static State lookup(final State[] tbl, byte b){
        return tbl[b];
    }
    
    /**
     * Copies resulting state to dst from lookup
     * @param dst
     * @param b 
     */
    public static void lookup(final State[] tbl, State dst, byte b){
        dst.setState(tbl[b].getState(), true);
    }
        
    /**
     * Gets whole table.
     * @return 
     */
    public State[] getTbl() {
        return tbl;
    }
    
    /**
     * Sets given table to this object.
     * @param tbl
     * @param copy 
     */
    public final void setTbl(final State[] tbl, boolean copy){
        if (tbl.length != ROWS) {
            throw new IllegalArgumentException("Table has to have exactly " + ROWS + " rows");
        }
        
        if (copy){
            this.tbl = initNew();
            for(int i=0; i<ROWS; i++){
                this.tbl[i] = (State) tbl[i].copy();
                this.tbl[i].setImmutable(true);
            }
        } else {
            this.tbl = tbl;
        }
    }
    
    /**
     * Sets value x to index idx in lookup table.
     * @param x
     * @param idx 
     */
    public void setValue(final State x, int idx){
        this.tbl[idx].loadFrom(x);
    }
    
    /**
     * Initializes internal table - memory allocation.
     */
    public final void init(){
        tbl = initNew();
        for(int i=0; i<ROWS; i++){
            tbl[i] = new State();
        }
    }
    
    public static State[] initNew() {
        return new State[ROWS];
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Arrays.deepHashCode(this.tbl);
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
        final TBox8to128 other = (TBox8to128) obj;
        if (!Arrays.deepEquals(this.tbl, other.tbl)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TBox8to128{" + "tbl=" + tbl + "; size="+(tbl!=null ? tbl.length : -1)+"}";
    }
}
