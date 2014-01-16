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
 * 32bit vector
 * @author ph4r05
 */
public class W32b implements Serializable, Copyable{
    public static final int WIDTH = 4;
    
    // Internal representation
    protected byte[] b = null;

    public W32b() {
        init();
    }

    public W32b(byte[] b, boolean copy) {
        if (copy){
            this.b = Arrays.copyOf(b, WIDTH);
        } else {
            this.b = b;
        }
    }
    
    public W32b(long a){
        init();
        Utils.long2byte(this.b, a);
    }
    
    /**
     * Initializes object - memory allocation for internal representation.
     */
    public final void init(){
        this.b = initNew();
    }
    
    /**
     * Returns new allocated memory for internal representation.
     * @return 
     */
    public static byte[] initNew(){
        return new byte[WIDTH];
    }
    
    /**
     * Returns internal representation.
     * 
     * WARNING, if this object is set immutable, it should return copy of an array,
     * but from performance reasons it is not the case here. 
     * 
     * @return 
     */
    public byte[] get() {
        return b;
    }
    
    /**
     * Returns in long format
     * @return 
     */
    public long getLong(){
        return Utils.byte2long(get());
    }
    
    /**
     * Returns copy of internal representation.
     * 
     * @return 
     */
    public byte[] getCopy(){
        return Arrays.copyOf(b, WIDTH);
    }

    /**
     * Whole setter, copy
     * @param state 
     */
    public void set(byte[] b) {
        this.set(b, true);
    }
    
    /**
     * Initializes value from long.
     * Value is written directly to internal representation.
     * 
     * @param a 
     */
    public void set(long a){
        Utils.long2byte(this.b, a);
    }
    
    /**
     * State setter with optional copy
     * @param state
     * @param copy 
     */
    public void set(byte[] b, boolean copy) {
        if (b.length != WIDTH) {
            throw new IllegalArgumentException("Width has to be " + WIDTH);
        }
        
        if (copy){
            this.b = Arrays.copyOf(b, WIDTH);
        } else {
            this.b = b;
        }
    } 
    
    /**
     * Loads W32b data from source to currently allocated memory.
     * @param src 
     */
    public void loadFrom(final W32b src){
        System.arraycopy(src.get(), 0, this.b, 0, WIDTH);
    }

    public Copyable copy() {
        return new W32b(get(), true);
    }
    
    public static long W32b2long(W32b w){
        return Utils.byte2long(w.get());
    }
    
    public static byte[] W32b2byte(W32b w){
        return w.get();
    }
    
    public static void long2W32b(long a, W32b w){
        w.set(a);
    }
    
    public static void byte2W32b(byte[] b, W32b w){
        w.set(b);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Arrays.hashCode(this.b);
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
        final W32b other = (W32b) obj;
        if (!Arrays.equals(this.b, other.b)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        if (b==null){
            return "W32b{b=null}";
        }
        
        StringBuilder sb = new StringBuilder();
        final int ln = b.length;
        for(int i=0; i<ln; i++){
            sb.append(String.format("0x%02X", b[i] & 0xff));
            if ((i+1)!=ln){
                sb.append(", ");
            }
        }
        
        return "W32b{" + "b=" + sb.toString() + ";mem="+b+"}";
    }
}
