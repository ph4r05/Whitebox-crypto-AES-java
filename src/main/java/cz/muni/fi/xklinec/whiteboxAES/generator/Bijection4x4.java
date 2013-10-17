/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

/**
 *
 * @author ph4r05
 */
public class Bijection4x4 {
    public byte coding[];
    public byte invCoding[];

    public Bijection4x4() {
        coding    = new byte[16];
        invCoding = new byte[16];
    }

    public Bijection4x4(byte[] coding, byte[] invCoding) {
        this.coding = coding;
        this.invCoding = invCoding;
    }

    
}
