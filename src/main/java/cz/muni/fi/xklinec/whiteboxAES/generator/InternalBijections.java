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
 * Holder for all internal bijections for AES.
 * @author ph4r05
 */
public class InternalBijections {
    private LinearBijection MB_L08x08[][]  = new LinearBijection[Generator.MB_CNT_08x08_ROUNDS][Generator.MB_CNT_08x08_PER_ROUND];
    private LinearBijection MB_MB32x32[][] = new LinearBijection[Generator.MB_CNT_32x32_ROUNDS][Generator.MB_CNT_32x32_PER_ROUND];
    private Bijection4x4[] pCoding04x04    = null; // needs to be initialized on demand...
    
    /**
     * Allocate memory for mixing bijections
     */
    public void memoryAllocate(){
        for(int r=0; r<Generator.MB_CNT_08x08_ROUNDS; r++){
            for(int i=0; i<Generator.MB_CNT_08x08_PER_ROUND; i++){
                MB_L08x08[r][i] = new LinearBijection();
            }
        }
        
        for(int r=0; r<Generator.MB_CNT_32x32_ROUNDS; r++){
            for(int i=0; i<Generator.MB_CNT_32x32_PER_ROUND; i++){
                MB_MB32x32[r][i] = new LinearBijection();
            }
        }
    }
    
    public void alloc04x04(int size){
        pCoding04x04 = new Bijection4x4[size];
        for(int i=0; i<size; i++){
            pCoding04x04[i] = new Bijection4x4();
        }
    }
    
    public LinearBijection[][] getMB_L08x08() {
        return MB_L08x08;
    }

    public void setMB_L08x08(LinearBijection[][] MB_L08x08) {
        this.MB_L08x08 = MB_L08x08;
    }

    public LinearBijection[][] getMB_MB32x32() {
        return MB_MB32x32;
    }

    public void setMB_MB32x32(LinearBijection[][] MB_MB32x32) {
        this.MB_MB32x32 = MB_MB32x32;
    }

    public Bijection4x4[] getpCoding04x04() {
        return pCoding04x04;
    }

    public void setpCoding04x04(Bijection4x4[] pCoding04x04) {
        this.pCoding04x04 = pCoding04x04;
    }
}
