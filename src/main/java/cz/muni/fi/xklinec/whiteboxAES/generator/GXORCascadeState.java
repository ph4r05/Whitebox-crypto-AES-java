/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.XORCascadeState;
import cz.muni.fi.xklinec.whiteboxAES.generator.Generator.XORCODING;

/**
 *
 * @author ph4r05
 */
public class GXORCascadeState implements IOEncoding{
    protected Generator.XORCODING cod[];
    
    public GXORCascadeState() {
        super();
        cod = new Generator.XORCODING[XORCascadeState.BOXES];
        for(int i=0; i<XORCascadeState.BOXES; i++){
            cod[i] = new Generator.XORCODING(2*XORCascadeState.WIDTH);
        }
    }

    public GXORCascadeState(XORCODING[] cod) {
        this.cod = cod;
    }
    
    /**
     * Allocates XOR cascade coding.
     * @param idx
     * @return 
     */
    public final int allocate(int idx, boolean allocateOutput){
        for(int i=0; i<XORCascadeState.BOXES; i++){
            if (!allocateOutput && (i+1) == XORCascadeState.BOXES) {
                break;
            }
            
            idx = Generator.ALLOCXORCoding(cod[i], 0, idx, 2*XORCascadeState.WIDTH);
        }
        
        return idx;
    }
    
    /**
     * Allocates XOR cascade coding.
     * @param idx
     * @return 
     */
    public final int allocate(int idx){
        return allocate(idx, true);
    }
    
    /**
     * Connect cascade from the inside.
     * Connects layer 1 to layer 2 in XOR component.
     * 
     */
    public final void connectInternal(){        
        // Connect output from two XOR boxes to input of one XOR box
        Generator.CONNECT_XOR_TO_XOR_H(cod[0], 0, cod[3], 0);
        Generator.CONNECT_XOR_TO_XOR_L(cod[1], 0, cod[3], 0);
        
         // offset in x[] for current stage
        int XORoffset = 0;  
        // j is XOR stage number
        for(int j=0; j<3; j++){
            // Number of iterations in each stage is 8,4,2,1. i.e. 2^3, 2^2, 2^1, 2^0
            final int iterationsInStage = 1 << (3-j);
            // Step of XORing neighbouring states in one stage; 1st: 0+1, 2+3,...; 2nd: 0+2, 2+4,...; 3rd 0+4,...
            final int xorBoxStep        = 1 <<    j;  
            // performing XOR inside one 
            for(int i=0; i<iterationsInStage; i+=2){
                // Position to state[] for current stage
                Generator.CONNECT_XOR_TO_XOR_128_H(cod[XORoffset + i+0], 0, cod[XORoffset + iterationsInStage + i/2], 0);
                Generator.CONNECT_XOR_TO_XOR_128_L(cod[XORoffset + i+1], 0, cod[XORoffset + iterationsInStage + i/2], 0);
            }
            
            XORoffset += iterationsInStage;
        }
    }
    
    /**
     * Connects output of this box to input of 8bit table.
     * Slot gives particular output slot in XOR cascade.
     * 
     * @param c
     * @param slot 
     */
    public void connectOut(GTBox8to32 c, int slot){
        Generator.W08x32Coding cod1 = c.getCod();
        Generator.CONNECT_XOR_TO_W08x32(cod[XORCascadeState.BOXES-1], 2*slot, cod1);
    }

    public XORCODING[] getCod() {
        return cod;
    }

    public int getIOInputWidth() {
        return XORCascadeState.WIDTH;
    }

    public int getIOOutputWidth() {
        return XORCascadeState.WIDTH;
    }

    public int getIOInputSlots() {
        return XORCascadeState.WIDTH;
    }
}
