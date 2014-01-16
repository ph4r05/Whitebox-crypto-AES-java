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

/**
 * Input/Output encoding. It is specification for T1 tables for apps using WBAES.
 * 
 * @author ph4r05
 */
public class ExternalBijections {
    private Bijection4x4[][] lfC   = new Bijection4x4[2][2*AESCodingMap.BYTES]; // needs to be initialized on demand...
    private LinearBijection IODM[] = new LinearBijection[2];
    
    /**
     * Allocate memory for mixing bijections
     */
    public void memoryAllocate(){
        for(int r=0; r<2*AESCodingMap.BYTES; r++){
            lfC[0][r] = new Bijection4x4();
            lfC[1][r] = new Bijection4x4();
        }
        
        IODM[0] = new LinearBijection();
        IODM[1] = new LinearBijection();
    }

    public LinearBijection[] getIODM() {
        return IODM;
    }

    public void setIODM(LinearBijection[] IODM) {
        this.IODM = IODM;
    }

    public Bijection4x4[][] getLfC() {
        return lfC;
    }

    public void setLfC(Bijection4x4[][] lfC) {
        this.lfC = lfC;
    }   
    
}
