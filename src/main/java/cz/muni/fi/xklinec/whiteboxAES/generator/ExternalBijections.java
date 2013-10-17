/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

/**
 * Input/Output encoding. It is specification for T1 tables for apps using WBAES.
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
