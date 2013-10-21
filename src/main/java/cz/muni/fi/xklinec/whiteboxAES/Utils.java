/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES;

/**
 *
 * @author ph4r05
 */
public class Utils {
    private static final int    BIG_ENOUGH_INT   = 16 * 1024;
    private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5;
    
    /**
     * Binary logarithm
     * 
     * credits: http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
     * @param bits
     * @return 
     */
    public static int binlog( int bits ) // returns 0 for bits=0
    {
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        return log + ( bits >>> 1 );
    }
    
    /**
     * Converts byte representation to long.
     * Maximal byte size is 4.
     * 
     * 1st byte in b[0] is mapped to LSB in long.
     * 
     * @param b
     * @return 
     */
    public static long byte2long(byte[] b){
        return    ((long) b[0] & 0xff)
               | (((long) b[1] & 0xff) << 8)
               | (((long) b[2] & 0xff) << 16)
               | (((long) b[3] & 0xff) << 24);
    }
    
    /**
     * Converts long representation to bytes.
     * Uses provided byte[] with length at least 4.
     * 
     * LSB in long will be 1st bit in byte[0].
     */
    public static void long2byte(byte[] b, long a){
        b[0] = (byte) ( a        & 0xFF);
        b[1] = (byte) ((a >>> 8)  & 0xFF);
        b[2] = (byte) ((a >>> 16) & 0xFF);
        b[3] = (byte) ((a >>> 24) & 0xFF);
    }
    
    /**
     * Converts long representation to bytes.
     * 
     * LSB in long will be 1st bit in byte[0].
     */
    public static byte[] long2byte(long a){
        byte[] b = new byte[4];
        long2byte(b, a);
        return b;       
    }
    
    /**
     * Converts long representation to bytes, returns selected part of long.
     * 
     * LSB in long will be 1st bit in byte[0].
     */
    public static byte long2byte(long a, int idx){
        return (byte) ((a >>> (8*idx))  & 0xFF);
    }
    
    /**
     * Converts byte representation to long form on given position - by idx.
     * Can be used multiple times on same long and OR results together.
     * 
     * LSB in long will be 1st bit in byte[0].
     */
    public static long byte2long(byte a, int idx){
        return (long) ((long)(a & 0xff) << (8*idx));
    }
    
     public static int ceil(double x) {
        return BIG_ENOUGH_INT - (int)(BIG_ENOUGH_FLOOR-x);
    }
     
     public static String toBinaryString(int[] input) {
        String result = "";
        int i;
        for (i = 0; i < input.length; i++) {
            int e = input[i];
            for (int ii = 0; ii < 32; ii++) {
                int b = (e >>> ii) & 1;
                result += b;
            }
            if (i != input.length - 1) {
                result += " ";
            }
        }
        return result;
    }
}
