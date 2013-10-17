/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.generator.Generator.W08x128Coding;

/**
 *
 * @author ph4r05
 */
public class GTBox8to128 implements IOEncoding {
    protected Generator.W08x128Coding cod;
    
    public GTBox8to128() {
        super();
        cod = new Generator.W08x128Coding();
    }
    
    /**
     * Allocates IO encodings.
     * @param idx
     * @return 
     */
    public final int allocate(int idx){
        idx = Generator.ALLOCW08x128Coding(cod, idx);
        return idx;
    }
    
    /**
     * Connects output of this box to input of XOR cascade.
     * Slot gives particular input slot in XOR cascade. Slots: [0-15]
     * TODO: FINISH IMPLEMENTATION!
     * @param c
     * @param slot 
     */
    public void connectOut(GXORCascadeState c, int slot){
        Generator.XORCODING[] xcod = c.getCod();
        for(int i=0; i<4; i++){
            Generator.CONNECT_W08x32_TO_XOR_EX(cod, xcod[slot/2], (slot%2) == 0, i*8, i*4);
        }
    }
    
    public W08x128Coding getCod() {
        return cod;
    }
    
    public int getIOInputWidth() {
        return 1;
    }

    public int getIOOutputWidth() {
        return 16;
    }

    public int getIOInputSlots() {
        return 1;
    }
    
    
}
