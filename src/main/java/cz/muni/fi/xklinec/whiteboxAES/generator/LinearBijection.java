/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

/**
 * Linear bijection based on GF2Matrix.
 * 
 * Can be used for mixing bijections
 * 
 * @author ph4r05
 */
public class LinearBijection {
    private GF2MatrixEx mb;
    private GF2MatrixEx inv;

    public GF2MatrixEx getMb() {
        return mb;
    }

    public void setMb(GF2MatrixEx mb) {
        this.mb = mb;
    }

    public GF2MatrixEx getInv() {
        return inv;
    }

    public void setInv(GF2MatrixEx inv) {
        this.inv = inv;
    }
}
