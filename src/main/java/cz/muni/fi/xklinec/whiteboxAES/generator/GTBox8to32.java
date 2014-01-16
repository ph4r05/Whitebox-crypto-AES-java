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
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.generator.Generator.W08x32Coding;
import cz.muni.fi.xklinec.whiteboxAES.generator.Generator.XORCODING;

/**
 *
 * @author ph4r05
 */
public class GTBox8to32 implements IOEncoding{
    protected Generator.W08x32Coding cod;
    
    public GTBox8to32() {
        super();
        cod = new Generator.W08x32Coding();
    }
    
    /**
     * Allocates IO encodings.
     * @param idx
     * @return 
     */
    public final int allocate(int idx){
        idx = Generator.ALLOCW08x32CodingEx(cod, idx);
        return idx;
    }
    
    /**
     * Connects output of this box to input of XOR cascade.
     * Slot gives particular input slot in XOR cascade.
     * 
     * @param c
     * @param slot 
     */
    public void connectOut(GXORCascade c, int slot){
        XORCODING[] xcod = c.getCod();
        Generator.CONNECT_W08x32_TO_XOR(cod, xcod[slot/2], (slot%2) == 0, 0);
    }
    
    public byte ioEncodeInput(byte src, boolean inverse, InternalBijections ib){
        return Generator.iocoding_encode08x08(src, this.cod.IC, inverse, ib.getpCoding04x04(), null);
    }
    
    public long ioEncodeOutput(long src, InternalBijections ib){
        return Generator.iocoding_encode32x32(src, this.cod, false, ib.getpCoding04x04(), null);
    }

    public W08x32Coding getCod() {
        return cod;
    }

    public int getIOInputWidth() {
        return 1;
    }

    public int getIOOutputWidth() {
        return 4;
    }

    public int getIOInputSlots() {
        return 1;
    }
    
}
