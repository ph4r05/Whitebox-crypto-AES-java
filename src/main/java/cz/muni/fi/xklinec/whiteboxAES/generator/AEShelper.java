/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.State;
import org.bouncycastle.pqc.math.linearalgebra.GF2mField;

/**
 *
 * @author ph4r05
 */
public class AEShelper {
    public static final int POLYNOMIAL     = 0x11B;
    public static final int GENERATOR      = 0x03;
    public static final int DEGREE         = 8;
    public static final int AES_FIELD_SIZE = 1<<8;
    
    public static final byte testVect128_key[] = new byte[]{
             (byte)0x2b, (byte)0x7e, (byte)0x15, (byte)0x16, (byte)0x28, (byte)0xae, 
             (byte)0xd2, (byte)0xa6, (byte)0xab, (byte)0xf7, (byte)0x15, (byte)0x88, 
             (byte)0x09, (byte)0xcf, (byte)0x4f, (byte)0x3c };

    public static final byte testVect128_plain[][] = new byte[][]{
            {(byte)0x32, (byte)0x43, (byte)0xf6, (byte)0xa8, (byte)0x88, (byte)0x5a, (byte)0x30, (byte)0x8d, 
             (byte)0x31, (byte)0x31, (byte)0x98, (byte)0xa2, (byte)0xe0, (byte)0x37, (byte)0x07, (byte)0x34},
            {(byte)0x6b, (byte)0xc1, (byte)0xbe, (byte)0xe2, (byte)0x2e, (byte)0x40, (byte)0x9f, (byte)0x96, 
             (byte)0xe9, (byte)0x3d, (byte)0x7e, (byte)0x11, (byte)0x73, (byte)0x93, (byte)0x17, (byte)0x2a},
            {(byte)0xae, (byte)0x2d, (byte)0x8a, (byte)0x57, (byte)0x1e, (byte)0x03, (byte)0xac, (byte)0x9c, 
             (byte)0x9e, (byte)0xb7, (byte)0x6f, (byte)0xac, (byte)0x45, (byte)0xaf, (byte)0x8e, (byte)0x51},
            {(byte)0x30, (byte)0xc8, (byte)0x1c, (byte)0x46, (byte)0xa3, (byte)0x5c, (byte)0xe4, (byte)0x11, 
             (byte)0xe5, (byte)0xfb, (byte)0xc1, (byte)0x19, (byte)0x1a, (byte)0x0a, (byte)0x52, (byte)0xef},
            {(byte)0xf6, (byte)0x9f, (byte)0x24, (byte)0x45, (byte)0xdf, (byte)0x4f, (byte)0x9b, (byte)0x17, 
             (byte)0xad, (byte)0x2b, (byte)0x41, (byte)0x7b, (byte)0xe6, (byte)0x6c, (byte)0x37, (byte)0x10}
    };

    public static final byte testVect128_cipher[][] = new byte[][]{
            {(byte)0x39, (byte)0x25, (byte)0x84, (byte)0x1d, (byte)0x02, (byte)0xdc, (byte)0x09, (byte)0xfb, 
             (byte)0xdc, (byte)0x11, (byte)0x85, (byte)0x97, (byte)0x19, (byte)0x6a, (byte)0x0b, (byte)0x32},
            {(byte)0x3a, (byte)0xd7, (byte)0x7b, (byte)0xb4, (byte)0x0d, (byte)0x7a, (byte)0x36, (byte)0x60, 
             (byte)0xa8, (byte)0x9e, (byte)0xca, (byte)0xf3, (byte)0x24, (byte)0x66, (byte)0xef, (byte)0x97},
            {(byte)0xf5, (byte)0xd3, (byte)0xd5, (byte)0x85, (byte)0x03, (byte)0xb9, (byte)0x69, (byte)0x9d, 
             (byte)0xe7, (byte)0x85, (byte)0x89, (byte)0x5a, (byte)0x96, (byte)0xfd, (byte)0xba, (byte)0xaf},
            {(byte)0x43, (byte)0xb1, (byte)0xcd, (byte)0x7f, (byte)0x59, (byte)0x8e, (byte)0xce, (byte)0x23, 
             (byte)0x88, (byte)0x1b, (byte)0x00, (byte)0xe3, (byte)0xed, (byte)0x03, (byte)0x06, (byte)0x88},
            {(byte)0x7b, (byte)0x0c, (byte)0x78, (byte)0x5e, (byte)0x27, (byte)0xe8, (byte)0xad, (byte)0x3f, 
             (byte)0x82, (byte)0x23, (byte)0x20, (byte)0x71, (byte)0x04, (byte)0x72, (byte)0x5d, (byte)0xd4}
    };

    public static final byte testVect256_key[] = new byte[]{
             (byte)0x60, (byte)0x3d, (byte)0xeb, (byte)0x10, (byte)0x15, (byte)0xca, (byte)0x71, (byte)0xbe, 
             (byte)0x2b, (byte)0x73, (byte)0xae, (byte)0xf0, (byte)0x85, (byte)0x7d, (byte)0x77, (byte)0x81,
             (byte)0x1f, (byte)0x35, (byte)0x2c, (byte)0x07, (byte)0x3b, (byte)0x61, (byte)0x08, (byte)0xd7, 
             (byte)0x2d, (byte)0x98, (byte)0x10, (byte)0xa3, (byte)0x09, (byte)0x14, (byte)0xdf, (byte)0xf4
    };

    public static final byte testVect256_plain[][] = new byte[][]{
            {(byte)0x6b, (byte)0xc1, (byte)0xbe, (byte)0xe2, (byte)0x2e, (byte)0x40, (byte)0x9f, (byte)0x96, 
             (byte)0xe9, (byte)0x3d, (byte)0x7e, (byte)0x11, (byte)0x73, (byte)0x93, (byte)0x17, (byte)0x2a},
            {(byte)0xae, (byte)0x2d, (byte)0x8a, (byte)0x57, (byte)0x1e, (byte)0x03, (byte)0xac, (byte)0x9c, 
             (byte)0x9e, (byte)0xb7, (byte)0x6f, (byte)0xac, (byte)0x45, (byte)0xaf, (byte)0x8e, (byte)0x51},
            {(byte)0x30, (byte)0xc8, (byte)0x1c, (byte)0x46, (byte)0xa3, (byte)0x5c, (byte)0xe4, (byte)0x11, 
             (byte)0xe5, (byte)0xfb, (byte)0xc1, (byte)0x19, (byte)0x1a, (byte)0x0a, (byte)0x52, (byte)0xef},
            {(byte)0xf6, (byte)0x9f, (byte)0x24, (byte)0x45, (byte)0xdf, (byte)0x4f, (byte)0x9b, (byte)0x17, 
             (byte)0xad, (byte)0x2b, (byte)0x41, (byte)0x7b, (byte)0xe6, (byte)0x6c, (byte)0x37, (byte)0x10}
    };

    public static final byte testVect256_cipher[][] = new byte[][]{
            {(byte)0xf3, (byte)0xee, (byte)0xd1, (byte)0xbd, (byte)0xb5, (byte)0xd2, (byte)0xa0, (byte)0x3c, 
             (byte)0x06, (byte)0x4b, (byte)0x5a, (byte)0x7e, (byte)0x3d, (byte)0xb1, (byte)0x81, (byte)0xf8},
            {(byte)0x59, (byte)0x1c, (byte)0xcb, (byte)0x10, (byte)0xd4, (byte)0x10, (byte)0xed, (byte)0x26, 
             (byte)0xdc, (byte)0x5b, (byte)0xa7, (byte)0x4a, (byte)0x31, (byte)0x36, (byte)0x28, (byte)0x70},
            {(byte)0xb6, (byte)0xed, (byte)0x21, (byte)0xb9, (byte)0x9c, (byte)0xa6, (byte)0xf4, (byte)0xf9, 
             (byte)0xf1, (byte)0x53, (byte)0xe7, (byte)0xb1, (byte)0xbe, (byte)0xaf, (byte)0xed, (byte)0x1d},
            {(byte)0x23, (byte)0x30, (byte)0x4b, (byte)0x7a, (byte)0x39, (byte)0xf9, (byte)0xf3, (byte)0xff, 
             (byte)0x06, (byte)0x7d, (byte)0x8d, (byte)0x8f, (byte)0x9e, (byte)0x24, (byte)0xec, (byte)0xc7}
    };
    
    protected GF2mField field;
    protected int g[]             = new int[AES_FIELD_SIZE];
    protected int gInv[]          = new int[AES_FIELD_SIZE];
    protected int sbox[]          = new int[AES_FIELD_SIZE];
    protected int sboxAffine[]    = new int[AES_FIELD_SIZE];
    protected int sboxAffineInv[] = new int[AES_FIELD_SIZE];
    
    protected int mixColModulus[]      = new int[5];
    protected int mixColMultiply[]     = new int[4];
    protected int mixColMultiplyInv[]  = new int[4];
    
    public static final int RCNUM = 16;
    protected int RC[] = new int[RCNUM];
    protected GF2mMatrixEx mixColMat;
    protected GF2mMatrixEx mixColInvMat;
    
    /**
     * Initializes AES constans (S-box, T-box, RC for key schedule).
     * 
     * @param encrypt 
     */
    public void build(boolean encrypt){
        field = new GF2mField(8, POLYNOMIAL);
        System.out.println(field);
        
        int i,c,cur = 1;
	gInv[0] = -1;
	for(i=0; i<AES_FIELD_SIZE; i++){
            g[i] = cur;
            gInv[cur] = i;
            cur = field.mult(cur, GENERATOR);
	}
        
        // 2. compute GF(256) element inverses in terms of generator exponent
	sbox[0] = -1;
	for(i=1; i<AES_FIELD_SIZE; i++){
            sbox[i] = 255-i;
	}
        
        GF2MatrixEx tmpM = new GF2MatrixEx(8, 1);
        GF2MatrixEx afM  = getDefaultAffineMatrix(encrypt);
        byte        afC  = getDefaultAffineConstByte(encrypt);
        
        
        // Computing whole Sboxes with inversion + affine transformation in generic AES
        // Normal Sbox:      S(x) = const    +   A(x^{-1})
	// Sbox in Dual AES: G(x) = T(const) + T(A(T^{-1}(x^{-1})))
	for(i=0; i<AES_FIELD_SIZE; i++){
            int tmpRes;

            // i is now long representation, gInv transforms it to exponent power to obtain inverse.
            // Also getLong(g[gInv[i]]) == i
            int transValue = i==0 ? 0 : g[255-gInv[i]];

            // tmpM = col vector of transValue
            NTLUtils.zero(tmpM);
            NTLUtils.putByteAsColVector(tmpM, (byte)transValue, 0, 0);
            
            // const + A(x^{-1})
            GF2MatrixEx resMatrix = (GF2MatrixEx) afM.rightMultiply(tmpM);
            tmpRes = (byte) field.add(NTLUtils.colBinaryVectorToByte(resMatrix, 0, 0), afC) & 0xff;
            
            sboxAffine[i] = tmpRes;
            sboxAffineInv[tmpRes] = i;

            // Inversion, idea is the same, i is the long representation of element in GF, apply inverted affine transformation and take inverse
            // Ax^{-1} + c is input to this transformation
            //              [A^{-1} * (A{x^-1} + c) + d]^{-1} is this transformation;
            // correctness: [A^{-1} * (Ax^-1   + c) + d]^{-1} =
            //				[A^{-1}Ax^{-1} + A^{-1}c + d]^{-1} =	//	A^{-1}c = d
            //				[x^{-1}        + 0]^{-1} =
            //				x
            //
            // Computation is useless, we have inversion of transformation right from transformation above
            // by simply swapping indexes. This is just for validation purposes to show, that it really works and how
	}

	// 6. MixColumn operations
	// modulus x^4 + 1
	mixColModulus[0] = g[0];
	mixColModulus[4] = g[0];

	// 03 x^3 + 01 x^2 + 01 x + 02
	mixColMultiply[0] = g[25];
	mixColMultiply[1] = g[0];
	mixColMultiply[2] = g[0];
	mixColMultiply[3] = g[1];

	// inverse polynomial
	mixColMultiplyInv[0] = g[223];
	mixColMultiplyInv[1] = g[199];
	mixColMultiplyInv[2] = g[238];
	mixColMultiplyInv[3] = g[104];

	// MixCols multiplication matrix based on mult polynomial -  see Rijndael description of this.
	// Polynomials have coefficients in GF(256).
	mixColMat    = new GF2mMatrixEx(field, 4, 4);
        mixColInvMat = new GF2mMatrixEx(field, 4, 4);
	for(i=0; i<4; i++){
            for(c=0; c<4; c++){
                mixColMat.set(i, c, mixColMultiply[(i+4-c) % 4]);
                mixColInvMat.set(i, c, mixColMultiplyInv[(i+4-c) % 4]);
            }
	}

	// Round key constant RC (for key schedule) obeys this reccurence:
	// RC[0] = 1
	// RC[i] = '02' * RC[i-1] = x * RC[i-1] = x^{i-1} `mod` R(X)
	RC[0] = g[0];
	for(i=1; i<RCNUM; i++){
		RC[i] = field.mult(g[25], RC[i-1]);
	}
    }
    
    /**
     * Number of rounds of AES depends on key size
     */
    public static int getNumberOfRounds(int keySize){ 
        return keySize/4+6; 
    }
    
    /**
     * Positive modulo 4
     * @param a
     * @return 
     */
    public static int mod4(int a){ 
        int c = a % 4; 
        return c<0 ? c+4 : c; 
    }
    
    /**
     * Returns default affine matrix transformation for S-box.
     * @param encrypt
     * @return 
     */
    public static GF2MatrixEx getDefaultAffineMatrix(boolean encrypt){
        GF2MatrixEx r = new GF2MatrixEx(8, 8);
        
        if (encrypt){
            NTLUtils.putByteAsRowVector(r, (byte)0x8F, 0, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0xC7, 1, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0xE3, 2, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0xF1, 3, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0xF8, 4, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0x7C, 5, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0x3E, 6, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0x1F, 7, 0);
        } else {
            NTLUtils.putByteAsRowVector(r, (byte)0x25, 0, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0x92, 1, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0x49, 2, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0xA4, 3, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0x52, 4, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0x29, 5, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0x94, 6, 0);
            NTLUtils.putByteAsRowVector(r, (byte)0x4A, 7, 0);         
        }
        
        return r;
    }
    
    /**
     * Default affine constant for affine transformation for S-box.
     * @param encrypt
     * @return 
     */
    public static byte getDefaultAffineConstByte(boolean encrypt){
        return encrypt ? (byte)0x63 : (byte)0x05;
    }
    
    /**
     * Returns affine constant for affine transformation for S-box as a col vector.
     * @param encrypt
     * @return 
     */
    public static GF2MatrixEx getDefaultAffineConst(boolean encrypt){
        GF2MatrixEx r = new GF2MatrixEx(8,1);
        NTLUtils.putByteAsColVector(r, getDefaultAffineConstByte(encrypt), 0, 0);
        return r;
    }
    
    /**
     * Returns number of all round keys together. 
     * 
     * @param keySize
     * @return 
     */
    public static int getRoundKeysSize(int keySize){
        return (4 * State.COLS * (getNumberOfRounds(keySize) + 1));
    }
    
    /**
     * AES key schedule.
     * 
     * @param roundKeys
     * @param key
     * @param keySize 
     */
    public byte[] keySchedule(byte[] key, int size, boolean debug){
        /* current expanded keySize, in bytes */
	int currentSize = 0;
	int rconIteration = 0;
	int i,j;
	int roundKeysSize = getRoundKeysSize(size);

	byte tmp;
        byte[] t = new byte[4]; //vec_GF2E t(INIT_SIZE, 4);
        byte[] roundKeys = new byte[roundKeysSize];
        if (debug) {
            System.out.println("Expanded key size will be: " + roundKeysSize);
        }

        /* set the 16,24,32 bytes of the expanded key to the input key */
        for (i = 0; i < size; i++) {
            roundKeys[i] = key[i];
        }

        currentSize += size;
        while (currentSize < roundKeysSize) {
            if (debug) {
                System.out.println("CurrentSize: " + currentSize + "; expandedKeySize: " + roundKeysSize);
            }

            /* assign the previous 4 bytes to the temporary value t */
            for (i = 0; i < 4; i++) {
                t[i] = roundKeys[(currentSize - 4) + i];
            }

            /**
             * every 16,24,32 bytes we apply the core schedule to t and
             * increment rconIteration afterwards
             */
            if (currentSize % size == 0) {
                //core(t, rconIteration++);
		/* rotate the 32-bit word 8 bits to the left */
                tmp = t[0];
                t[0] = t[1];
                t[1] = t[2];
                t[2] = t[3];
                t[3] = tmp;
                /* apply S-Box substitution on all 4 parts of the 32-bit word */
                for (j = 0; j < 4; ++j) {
                    if (debug) {
                        System.out.println("Sboxing key t[" + j
                                + "]=" + t[j]
                                + "=" + NTLUtils.chex(t[j])
                                + "; sboxval: " + NTLUtils.chex(sboxAffine[t[j]]));
                    }

                    // Apply S-box to t[j]
                    t[j] = (byte) sboxAffine[t[j]];

                    if (debug) {
                        System.out.println(" after Sbox = " + t[j] + "=" + NTLUtils.chex(t[j]));
                    }
                }
                
                /* XOR the output of the rcon operation with i to the first part (leftmost) only */
                t[0] = (byte) ((byte) field.add(t[0], RC[rconIteration++]) & 0xff);

                if (debug) {
                    System.out.println("; after XOR with RC[" + NTLUtils.chex(RC[rconIteration - 1]) + "] = " + t[0] + " = " + NTLUtils.chex(t[0]));
                }
            }

            /* For 256-bit keys, we add an extra sbox to the calculation */
            if (size == 32 && ((currentSize % size) == 16)) {
                for (i = 0; i < 4; i++) {
                    t[i] = (byte) sboxAffine[t[i]];
                }
            }
            
            /* We XOR t with the four-byte block 16,24,32 bytes before the new expanded key.
             * This becomes the next four bytes in the expanded key.
             */
            for (i = 0; i < 4; i++) {
                roundKeys[currentSize] = (byte) ((byte) field.add(roundKeys[currentSize - size], t[i]) & 0xff);

                if (debug) {
                    System.out.println("t[" + i + "] = " + NTLUtils.chex(t[i]));
                }

                currentSize++;
            }
        }

        return roundKeys;
    }

    /**
     * AES S-box.
     * @param e
     * @return 
     */
    public int ByteSub(int e){
        return sboxAffine[e];
    }

    /**
     * AES S-box on whole state array.
     * @param state 
     */
    public void ByteSub(State state) {
        int i, j;
        for (i = 0; i < State.ROWS; i++) {
            for (j = 0; j < State.COLS; j++) {
                state.set((byte) sboxAffine[state.get(i, j)], i, j);
            }
        }
    }

    /**
     * AES S-box inverse.
     * @param e
     * @return 
     */
    public int ByteSubInv(int e){
        return sboxAffineInv[e];
    }

    /**
     * AES S-box inverse on whole state array.
     * @param state 
     */
    public void ByteSubInv(State state){
        int i, j;
        for (i = 0; i < State.ROWS; i++) {
            for (j = 0; j < State.COLS; j++) {
                state.set((byte) sboxAffineInv[state.get(i, j)], i, j);
            }
        }
    }

    /**
     * Adds specified round key to state array.
     * @param state
     * @param expandedKey
     * @param offset 
     */
    public void AddRoundKey(State state, byte[] expandedKey, int offset){
        int i,j;
        for(i=0; i<State.ROWS; i++){
            for(j=0; j<State.COLS; j++){
                state.set((byte) field.add(state.get(i, j), expandedKey[offset + j*4+i]) , i, j);
            }
        }
    }

    /**
     * Shift Rows operation on state array, in-place.
     * @param state 
     */
    public void ShiftRows(State state) {
        // 1. row = no shift. 2. row = cyclic shift to the left by 1
        // for AES with Nb=4, left shift for rows are: 1=1, 2=2, 3=3.
        byte tmp;
        int i, j;
        for (i = 1; i < State.ROWS; i++) {
            for (j = 1; j <= i; j++) {
                tmp = state.get(i, 0);
                state.set(state.get(i, 1), i, 0);
                state.set(state.get(i, 2), i, 1);
                state.set(state.get(i, 3), i, 2);
                state.set(tmp, i, 3);
            }
        }
    }

    /**
     * Inverse of Shift Rows operation on state array, in-place.
     * @param state 
     */
    public void ShiftRowsInv(State state) {
        // 1. row = no shift. 2. row = cyclic shift to the left by 1
        // for AES with Nb=4, left shift for rows are: 1=1, 2=2, 3=3.
        byte tmp;
        int i, j;
        for (i = 1; i < State.ROWS; i++) {
            for (j = 1; j <= i; j++) {
                tmp = state.get(i, 3);
                state.set(state.get(i, 2), i, 3);
                state.set(state.get(i, 1), i, 2);
                state.set(state.get(i, 0), i, 1);
                state.set(tmp, i, 0);
            }
        }
    }

    /**
     * MixColumn operation on all columns on state matrix.
     * @param state 
     */
    public void MixColumn(State state) {
        int i, j;
        GF2mMatrixEx resMat;
        GF2mMatrixEx tmpMat = new GF2mMatrixEx(field, 4, 1);

        for (i = 0; i < State.COLS; i++) {
            // copy i-th column to 4*1 matrix - for multiplication
            for (j = 0; j < State.ROWS; j++) {
                tmpMat.set(j, 0, state.get(j, i));
            }

            resMat = mixColMat.rightMultiply(tmpMat);

            // copy result back to i-th column
            for (j = 0; j < State.ROWS; j++) {
                state.set((byte) resMat.get(j, 0), j, i);
            }
        }
    }

    /**
     * Inverse MixColumn operation on all columns on state matrix.
     * @param state 
     */
    public void MixColumnInv(State state){
        int i,j;
        GF2mMatrixEx resMat;
        GF2mMatrixEx tmpMat = new GF2mMatrixEx(field, 4, 1);

        for (i = 0; i < State.COLS; i++) {
            // copy i-th column to 4*1 matrix - for multiplication
            for (j = 0; j < State.ROWS; j++) {
                tmpMat.set(j, 0, state.get(j, i));
            }

            resMat = mixColInvMat.rightMultiply(tmpMat);

            // copy result back to i-th column
            for (j = 0; j < State.ROWS; j++) {
                state.set((byte) resMat.get(j, 0), j, i);
            }
        }
    }

    public GF2mField getField() {
        return field;
    }

    public int[] getG() {
        return g;
    }

    public int[] getgInv() {
        return gInv;
    }

    public int[] getSbox() {
        return sbox;
    }

    public int[] getSboxAffine() {
        return sboxAffine;
    }

    public int[] getSboxAffineInv() {
        return sboxAffineInv;
    }

    public int[] getMixColModulus() {
        return mixColModulus;
    }

    public int[] getMixColMultiply() {
        return mixColMultiply;
    }

    public int[] getMixColMultiplyInv() {
        return mixColMultiplyInv;
    }

    public int[] getRC() {
        return RC;
    }

    public GF2mMatrixEx getMixColMat() {
        return mixColMat;
    }

    public GF2mMatrixEx getMixColInvMat() {
        return mixColInvMat;
    }
}
