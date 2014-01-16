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

import org.bouncycastle.pqc.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.math.linearalgebra.IntUtils;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println("ceil: [" + (15 >>> 5) + "]"); 
        System.out.println("ceil: [" + (31 >>> 5) + "]"); 
        System.out.println("ceil: [" + (32 >>> 5) + "]"); 
        System.out.println("ceil: [" + (36 >>> 5) + "]"); 
        System.out.println("ceil: [" + (63 >>> 5) + "]"); 
        System.out.println("ceil: [" + (64 >>> 5) + "]"); 
        System.out.println("ceil: [" + (66 >>> 5) + "]"); 
        
        GF2Matrix m = new GF2Matrix(33,  GF2Matrix.MATRIX_TYPE_RANDOM_REGULAR);
        System.out.println("Matrix: " + m.toString());
        
        final int[][] a = m.getIntArray();
        for(int i=0; i<m.getNumRows(); i++){
            System.out.println("MatrixEnc["+i+"]: [" + Utils.toBinaryString(a[i])+ "]");
        }
        
        System.out.println("MatrixEncLen: [" + m.getEncoded().length + "]");
        System.out.println("MatrixEncHex: [" + IntUtils.toHexString(m.getRow(0)) + "]");
    }
}
