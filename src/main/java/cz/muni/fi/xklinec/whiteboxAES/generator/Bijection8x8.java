/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

/**
 *
 * @author ph4r05
 */
public class Bijection8x8 {
    public byte coding[];
    public byte invCoding[];

    public Bijection8x8() {
        coding    = new byte[256];
        invCoding = new byte[256];
    }

    public Bijection8x8(byte[] coding, byte[] invCoding) {
        this.coding = coding;
        this.invCoding = invCoding;
    }
}
