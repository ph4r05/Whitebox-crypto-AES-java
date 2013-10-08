/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES;

/**
 * Main AES whitebox table implementation.
 * 
 * @author ph4r05
 */
public class AES {
    public static final int ROUNDS = 10;
    public static final int T1BOXES = 2;
    public static final int T1Boxes = 2;
    
    
    private T1Box[]   t1 = new T1Box[2];
    private T2Box[][] t2 = new T2Box[ROUNDS][State.BYTES];
    private T3Box[][] t3 = new T3Box[ROUNDS][State.BYTES];
    


}
