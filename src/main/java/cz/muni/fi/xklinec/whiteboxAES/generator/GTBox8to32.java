/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
