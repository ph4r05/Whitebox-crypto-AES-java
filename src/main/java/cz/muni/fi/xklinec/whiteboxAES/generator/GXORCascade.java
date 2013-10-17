/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.XORBox;
import cz.muni.fi.xklinec.whiteboxAES.XORCascade;
import cz.muni.fi.xklinec.whiteboxAES.generator.Generator.W08x128Coding;
import cz.muni.fi.xklinec.whiteboxAES.generator.Generator.W08x32Coding;
import cz.muni.fi.xklinec.whiteboxAES.generator.Generator.XORCODING;

/**
 *
 * @author ph4r05
 */
public class GXORCascade implements IOEncoding{
    public static final int XOR_BOXES = XORCascade.BOXES;
    protected Generator.XORCODING cod[];
    
    public GXORCascade() {
        super();
        cod = new Generator.XORCODING[XOR_BOXES];
        cod[0] = new Generator.XORCODING(XORBox.BOXES);
        cod[1] = new Generator.XORCODING(XORBox.BOXES);
        cod[2] = new Generator.XORCODING(XORBox.BOXES);
    }
    
    /**
     * Allocates XOR cascade coding.
     * @param idx
     * @return 
     */
    public final int allocate(int idx){
        idx = Generator.ALLOCXORCoding(cod[0], 0, idx, XORBox.BOXES);
        idx = Generator.ALLOCXORCoding(cod[1], 0, idx, XORBox.BOXES);
        idx = Generator.ALLOCXORCoding(cod[2], 0, idx, XORBox.BOXES);
        return idx;
    }
    
    /**
     * Connect cascade from the inside.
     * Connects layer 1 to layer 2 in XOR component.
     * 
     */
    public final void connectInternal(){        
        // Connect output from two XOR boxes to input of one XOR box
        Generator.CONNECT_XOR_TO_XOR_H(cod[0], 0, cod[2], 0);
        Generator.CONNECT_XOR_TO_XOR_L(cod[1], 0, cod[2], 0);
    }

    /**
     * Connects output of this box to input of 8bit table.
     * Slot gives particular output slot in XOR cascade.
     * 
     * @param c
     * @param slot 
     */
    public void connectOut(GTBox8to32 c, int slot){
        W08x32Coding cod1 = c.getCod();
        Generator.CONNECT_XOR_TO_W08x32(cod[XORCascade.BOXES-1], 2*slot, cod1);
    }
    
    /**
     * Connects output of this box to input of 8bit table.
     * Slot gives particular output slot in XOR cascade.
     * 
     * @param c
     * @param slot 
     */
    public void connectOut(GTBox8to128 c, int slot){
        W08x128Coding cod1 = c.getCod();
        Generator.CONNECT_XOR_TO_W08x32(cod[XORCascade.BOXES-1], 2*slot, cod1);
    }
    
    /**
     * Generates XOR tables for particular XOR cascade.
     * 
     * @param c
     * @param g 
     */
    public void generateTables(XORCascade c, Generator g){
        final Bijection4x4[] pCoding04x04 = g.getIo().getpCoding04x04();
        XORBox[] x = c.getX();
        
        // Iterate over each 32bit XOR box
        for(int i=0; i<XOR_BOXES; i++){
            // Get whole XOR table; tbl[XORBox.BOXES][256]
            byte[][] tbl = x[i].getTbl();
            for(int j=0; j<XORBox.BOXES; j++){
                Generator.generateXorTable(cod[i].xtb[j], tbl[j], pCoding04x04);
            }
        }
    }
    
    public XORCODING[] getCod() {
        return cod;
    }

    public int getIOInputWidth() {
        return XORCascade.WIDTH;
    }

    public int getIOOutputWidth() {
        return XORCascade.WIDTH;
    }

    public int getIOInputSlots() {
        return XORCascade.WIDTH;
    }
    
}
