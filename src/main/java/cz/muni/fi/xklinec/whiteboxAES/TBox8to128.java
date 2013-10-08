/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Lookup table for whitebox AES implementation.
 * Implements 8 -> 128 bit lookup table
 * 
 * @author ph4r05
 */
public class TBox8to128 implements Serializable{
    public static final int IWIDTH=1;
    public static final int OWIDTH=16;
    public static final int ROWS  =1<<(8*IWIDTH);
    
    
    // Main lookup table
    private State tbl[] = null;
    
    /**
     * Basic lookup, returns direct result
     * @param b
     * @return 
     */
    public State lookup(byte b){
        return tbl[b];
    }
    
    /**
     * Copies resulting state to dst from lookup
     * @param dst
     * @param b 
     */
    public void lookup(State dst, byte b){
        dst.setState(tbl[b].getState(), true);
    }
    
    /**
     * Basic lookup, no copy, returns direct result
     * @param b
     * @return 
     */
    public static State lookup(final State[] tbl, byte b){
        return tbl[b];
    }
    
    /**
     * Copies resulting state to dst from lookup
     * @param dst
     * @param b 
     */
    public static void lookup(final State[] tbl, State dst, byte b){
        dst.setState(tbl[b].getState(), true);
    }
        
    /**
     * Gets whole table.
     * @return 
     */
    public State[] getTbl() {
        return tbl;
    }
    
    /**
     * Sets given table to this object.
     * @param tbl
     * @param copy 
     */
    public final void setTbl(final State[] tbl, boolean copy){
        if (tbl.length != ROWS) {
            throw new IllegalArgumentException("Table has to have exactly " + ROWS + " rows");
        }
        
        if (copy){
            this.tbl = initNew();
            for(int i=0; i<ROWS; i++){
                this.tbl[i] = (State) tbl[i].copy();
                this.tbl[i].setImmutable(true);
            }
        } else {
            this.tbl = tbl;
        }
    }
    
    /**
     * Initializes internal table - memory allocation.
     */
    public final void init(){
        tbl = initNew();
    }
    
    public static State[] initNew() {
        return new State[ROWS];
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Arrays.deepHashCode(this.tbl);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TBox8to128 other = (TBox8to128) obj;
        if (!Arrays.deepEquals(this.tbl, other.tbl)) {
            return false;
        }
        return true;
    }
}
