package cz.muni.fi.xklinec.whitebox;

import cz.muni.fi.xklinec.whiteboxAES.Utils;
import org.bouncycastle.pqc.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.math.linearalgebra.IntUtils;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println("ceil: [" + (15 >>> 5) + "]"); 
        System.out.println("ceil: [" + (31 >>> 5) + "]"); 
        System.out.println("ceil: [" + (32 >>> 5) + "]"); 
        System.out.println("ceil: [" + (36 >>> 5) + "]"); 
        System.out.println("ceil: [" + (63 >>> 5) + "]"); 
        System.out.println("ceil: [" + (64 >>> 5) + "]"); 
        System.out.println("ceil: [" + (66 >>> 5) + "]"); 
        
        GF2Matrix m = new GF2Matrix(33,  GF2Matrix.MATRIX_TYPE_RANDOM_REGULAR);
        System.out.println("Matrix: " + m.toString());
        
        final int[][] a = m.getIntArray();
        for(int i=0; i<m.getNumRows(); i++){
            System.out.println("MatrixEnc["+i+"]: [" + Utils.toBinaryString(a[i])+ "]");
        }
        
        System.out.println("MatrixEncLen: [" + m.getEncoded().length + "]");
        System.out.println("MatrixEncHex: [" + IntUtils.toHexString(m.getRow(0)) + "]");
    }
}
