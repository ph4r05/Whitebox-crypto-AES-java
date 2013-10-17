/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
