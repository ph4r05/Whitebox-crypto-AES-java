/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES;

import java.io.Serializable;
import java.util.Arrays;

/**
 * AES-128 State
 * @author ph4r05
 */
public class State implements Serializable, Copyable{
    public static final int BYTES = 16;
    public static final int ROWS  = 4;
    public static final int COLS  = BYTES / ROWS;
    private byte[] state;
    private boolean immutable=false;

    public State() {
        
    }

    public State(byte[] state) {
        this.state = state;
    }
    
    public State(byte[] state, boolean copy) {
        if (copy){
            this.state = Arrays.copyOf(state, BYTES);
        } else {
            this.state = state;
        }
    }
    
    /**
     * Per-byte state getter.
     * @param idx
     * @return 
     */
    public byte get(int idx){
        if (idx<0 || idx >= BYTES){
            throw new IllegalArgumentException("Invalid byte requested");
        }
        
        return this.state[idx];
    }
    
    /**
     * Per-byte state setter.
     * @param b
     * @param idx 
     */
    public void set(byte b, int idx){
        if (idx<0 || idx >= BYTES) {
            throw new IllegalArgumentException("Invalid byte requested");
        }
        
        if (state == null){
            throw new NullPointerException("State is not initialized");
        }
        
        if (immutable){
            throw new IllegalAccessError("State is set as immutable, cannot change");
        }
        
        this.state[idx] = b;
    }
    
    /**
     * Returns index to byte array for 2D coordinates, indexed by rows (0 1 2 3)
     * @param i
     * @param j
     * @return 
     */
    public static int getIdx(int i, int j){
        return i*COLS + j;
    }
    
    /**
     * Returns index to byte array for 2D coordinates, indexed by cols (0 4 8 12)
     * @param i
     * @param j
     * @return 
     */
    public static int getCIdx(int i, int j){
        return j*ROWS + i;
    }
    
    /**
     * Returns transposed index for matrix 
     *  00 01 02 03        00 04 08 12
     *  04 05 06 07        01 05 09 13
     *  08 09 10 11  --->  02 06 10 14
     *  12 13 14 15        03 07 11 15
     * 
     * @param idx
     * @return 
     */
    public static int getTIdx(int idx){
        return getCIdx(idx / COLS, idx % ROWS);//  4*((idx)%4) + ((idx)/4);
    }
    
    /**
     * Getter for 2D coordinates, assuming first line indexing: 0 1 2 3
     * @param i row
     * @param j column
     * @return 
     */
    public byte get(int i, int j){
        return get(getIdx(i, j));
    }
    
    /**
     * Getter for 2D coordinates, assuming first line indexing: 0 4 8 12
     * @param i row
     * @param j column
     * @return 
     */
    public byte getC(int i, int j){
        return get(getCIdx(i, j));
    }
    
    /**
     * Getter for transposed index
     * @param i row
     * @param j column
     * @return 
     */
    public byte getT(int idx){
        return get(getTIdx(idx));
    }
    
    /**
     * Getter for 2D coordinates, assuming first line indexing: 0 1 2 3
     * @param i row
     * @param j column
     * @return 
     */
    public void set(byte b, int i, int j){
        set(b, getIdx(i, j));
    }
    
    /**
     * Getter for 2D coordinates, assuming first line indexing: 0 4 8 12
     * @param i row
     * @param j column
     * @return 
     */
    public void setC(byte b, int i, int j){
        set(b, getCIdx(i, j));
    }
    
    /**
     * Getter for transposed index
     * @param i row
     * @param j column
     * @return 
     */
    public void setT(byte b, int idx){
        set(b, getTIdx(idx));
    }
    
    /**
     * State initialization - memory allocation
     */
    public final void init(){
        if (immutable){
            throw new IllegalAccessError("State is set as immutable, cannot change");
        }
        
        state = new byte[BYTES];
    }
    
    /**
     * State initialization - memory allocation
     */
    public static byte[] initExt(){
        return new byte[BYTES];
    }

    /**
     * Whole state getter.
     * 
     * WARNING, if this object is set immutable, it should return copy of an array,
     * but from performance reasons it is not the case here. 
     * 
     * @return 
     */
    public byte[] getState() {
        return state;
    }
    
    /**
     * Whole state getter. 
     * Returns copy of internal representation.
     * 
     * @return 
     */
    public byte[] getStateCopy() {
        return Arrays.copyOf(state, BYTES);
    }

    /**
     * Whole state setter, copy
     * @param state 
     */
    public void setState(final byte[] state) {
        this.setState(state, true);
    }
    
    /**
     * State setter with optional copy.
     * Copy is done via Arrays.copy, so new memory is allocated.
     * 
     * @param state
     * @param copy 
     */
    public void setState(final byte[] state, boolean copy) {
        if (state.length != BYTES) {
            throw new IllegalArgumentException("XOR table has to have 8 sub-tables");
        }
        
        if (immutable){
            throw new IllegalAccessError("State is set as immutable, cannot change");
        }
        
        if (copy){
            this.state = Arrays.copyOf(state, BYTES);
        } else {
            this.state = state;
        }
    }   
    
    /**
     * Loads state data from source to currently allocated memory.
     * @param src 
     */
    public void loadFrom(final State src){
        if (immutable){
            throw new IllegalAccessError("State is set as immutable, cannot change");
        }
        
        System.arraycopy(src.getState(), 0, this.state, 0, BYTES);
    }

    /**
     * Deep copy of objects
     * 
     * @param src
     * @param dst 
     */
    public static void copy(final State src, State dst){
        dst.setState(dst.getState(), true);
    }
    
    /**
     * Returns deep copy of state.
     * 
     * @return 
     */
    public Copyable copy() {
        return new State(this.getState(), true);
    }

    public boolean isImmutable() {
        return immutable;
    }

    public void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Arrays.hashCode(this.state);
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
        final State other = (State) obj;
        if (!Arrays.equals(this.state, other.state)) {
            return false;
        }
        return true;
    }
}
