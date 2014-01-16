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
 * Simple lookup table & manipulations; 8 -> 32bit lookup.
 * 
 * Implemented by storing to long. 
 * 
 * @author ph4r05
 */
public class TBox8to32 implements Serializable, Copyable{
    public static final int IWIDTH=1;
    public static final int OWIDTH=4;
    public static final int ROWS  =1<<(8*IWIDTH);
    
    // Main Lookup table
    // represents 1 -> 4 byte mapping
    protected long[] tbl = null;
    
    /**
     * Empty constructor
     */
    public TBox8to32() {
        init();
    }
    
    /**
     * Constructor taking given table.
     * @param xorTbl
     * @param copy 
     */
    public TBox8to32(final long[] tbl, boolean copy) {
        setTbl(tbl, copy);
    }
    
    /**
     * Basic lookup
     * @param b
     * @return 
     */
    public long lookup(byte b){
        return tbl[AES.posIdx(b)];
    }
    
    /**
     * Basic lookup
     * @param b
     * @return 
     */
    public static long lookup(final long[] tbl, byte b){
        return tbl[b];
    }

    /**
     * Gets whole table.
     * @return 
     */
    public long[] getTbl() {
        return tbl;
    }
    
    /**
     * Sets given table to this object.
     * @param tbl
     * @param copy 
     */
    public final void setTbl(final long[] tbl, boolean copy){
        if (tbl.length != ROWS) {
            throw new IllegalArgumentException("Table has to have exactly " + ROWS + " rows");
        }
        
        if (copy){
            this.tbl = Arrays.copyOf(tbl, ROWS);
        } else {
            this.tbl = tbl;
        }
    }
    
    /**
     * Static copy of tables.
     * 
     * @param src
     * @param dst 
     */
    public static void copy(final TBox8to32 src, TBox8to32 dst){
        dst.setTbl(src.getTbl(), true);
    }
    
    /**
     * Initializes internal table - memory allocation.
     */
    public final void init(){
        tbl = initNew();
    }
    
    public static long[] initNew() {
        return new long[ROWS];
    }

    public Copyable copy() {
        return new TBox8to32(this.getTbl(), true);
    }
    
    /**
     * Sets value x to index idx in lookup table.
     * @param x
     * @param idx 
     */
    public void setValue(long x, int idx){
        this.tbl[idx] = x;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Arrays.hashCode(this.tbl);
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
        final TBox8to32 other = (TBox8to32) obj;
        if (!Arrays.equals(this.tbl, other.tbl)) {
            return false;
        }
        return true;
    }
}
