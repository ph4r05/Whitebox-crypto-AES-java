/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Simple lookup table & manipulations; 8 -> 32bit lookup.
 * 
 * Implemented by storing to long. 
 * 
 * @author ph4r05
 */
public class TBox8to32 implements Serializable, Copyable{
    public static final int IWIDTH=1;
    public static final int OWIDTH=4;
    public static final int ROWS  =1<<(8*IWIDTH);
    
    // Main Lookup table
    // represents 1 -> 4 byte mapping
    private long[] tbl = null;
    
    /**
     * Empty constructor
     */
    public TBox8to32() {
        init();
    }
    
    /**
     * Constructor taking given table.
     * @param xorTbl
     * @param copy 
     */
    public TBox8to32(final long[] tbl, boolean copy) {
        setTbl(tbl, copy);
    }
    
    /**
     * Basic lookup
     * @param b
     * @return 
     */
    public long lookup(byte b){
        return tbl[b];
    }
    
    /**
     * Basic lookup
     * @param b
     * @return 
     */
    public static long lookup(final long[] tbl, byte b){
        return tbl[b];
    }

    /**
     * Gets whole table.
     * @return 
     */
    public long[] getTbl() {
        return tbl;
    }
    
    /**
     * Sets given table to this object.
     * @param tbl
     * @param copy 
     */
    public final void setTbl(final long[] tbl, boolean copy){
        if (tbl.length != ROWS) {
            throw new IllegalArgumentException("Table has to have exactly " + ROWS + " rows");
        }
        
        if (copy){
            this.tbl = Arrays.copyOf(tbl, ROWS);
        } else {
            this.tbl = tbl;
        }
    }
    
    /**
     * Static copy of tables.
     * 
     * @param src
     * @param dst 
     */
    public static void copy(final TBox8to32 src, TBox8to32 dst){
        dst.setTbl(src.getTbl(), true);
    }
    
    /**
     * Initializes internal table - memory allocation.
     */
    public final void init(){
        tbl = initNew();
    }
    
    public static long[] initNew() {
        return new long[ROWS];
    }

    public Copyable copy() {
        return new TBox8to32(this.getTbl(), true);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Arrays.hashCode(this.tbl);
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
        final TBox8to32 other = (TBox8to32) obj;
        if (!Arrays.equals(this.tbl, other.tbl)) {
            return false;
        }
        return true;
    }
}
