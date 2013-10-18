/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.AES;
import cz.muni.fi.xklinec.whiteboxAES.State;
import cz.muni.fi.xklinec.whiteboxAES.T1Box;
import cz.muni.fi.xklinec.whiteboxAES.T2Box;
import cz.muni.fi.xklinec.whiteboxAES.T3Box;
import cz.muni.fi.xklinec.whiteboxAES.Utils;
import cz.muni.fi.xklinec.whiteboxAES.XORCascade;
import cz.muni.fi.xklinec.whiteboxAES.XORCascadeState;
import java.security.SecureRandom;
import org.bouncycastle.pqc.math.linearalgebra.GF2mField;

/**
 *
 * @author ph4r05
 */
public class Generator {
    // CODING CONSTANTS
    public static final int  NO_CODING          = 0x00000000;  // IDENTITY CODING
    public static final int  UNASSIGNED_CODING  = 0xFFFFFFFF;  // INVALID CODING
    public static final int  UNUSED_CODING      = 0xFFFFFFFE;  // This coding is not in use (XOR tables use only lower 4 bits for result)
    public static boolean    USE_IDENTITY_CODING(int idx){
        return ((idx) == NO_CODING || (idx) == UNASSIGNED_CODING || (idx) == UNUSED_CODING);
    }
    // VALID CODINGS ORDINARY NUMBER IS FROM 0x00000000 TO 0xFFFFFFFE (TOTAL COUNT == 2^32 - 1) 

    // CODING SIZE TYPE
    public static final int  COD_BITS_UNASSIGNED = 0x00;
    public static final int  COD_BITS_4          = 0x01;
    public static final int  COD_BITS_8          = 0x02;
    public static final int  COD_BITS_8_EXT      = 0x04;
    public static final int  COD_BITS_EXT        = 0x20;

    // MIXING BIJECTION TYPE
    public static final int  MB_IDENTITY = 0x00;
    public static final int  MB_8x8      = 0x01;
    public static final int  MB_32x32    = 0x02;
    public static final int  MB_128x128  = 0x04;

    // MIXING BIJECTION COUNTS
    public static final int  MB_CNT_08x08_ROUNDS    = 9;
    public static final int  MB_CNT_08x08_PER_ROUND = 16;
    public static final int  MB_CNT_32x32_ROUNDS    = 9;
    public static final int  MB_CNT_32x32_PER_ROUND = 4;

    // NUMBER OF XOR TABLES FOR ONE T1 TABLE
    public static final int  XTB_CNT_T1 = 480;
    
    // EXTERNAL ENCODINGS
    public static final int WBAESGEN_EXTGEN_fCID=1;          // lfC[0]  in ExtEncoding will be identity
    public static final int WBAESGEN_EXTGEN_lCID=2;          // lfC[1]  in ExtEncoding will be identity
    public static final int WBAESGEN_EXTGEN_IDMID=4;         // IODM[0] in ExtEncoding will be identity
    public static final int WBAESGEN_EXTGEN_ODMID=8;         // IODM[1] in ExtEncoding will be identity
    
    // whole ExtEncoding will be identity
    public static final int WBAESGEN_EXTGEN_ID = (WBAESGEN_EXTGEN_fCID | WBAESGEN_EXTGEN_lCID | WBAESGEN_EXTGEN_IDMID | WBAESGEN_EXTGEN_ODMID);
    
    public static final int shiftRowsLBijection[] = {
        0, 13, 10, 7,
        4,  1, 14, 11,
        8,  5,  2, 15,
        12, 9,  6,  3
    };
    
    public static final int shiftRowsLBijectionInv[] = {
        0,  5, 10, 15,
        4,  9, 14,  3,
        8, 13,  2,  7,
       12,  1,  6, 11
    };
    
    public static int nextTbox(int idx, boolean encrypt){
        return encrypt ? shiftRowsLBijection[idx] : shiftRowsLBijectionInv[idx];
    }
    
    //
    //  HIGHLOW, DEFINE TWO 4-BITS CODING FOR 8-BITS ARGUMENT
    //
    public static class HighLow {
        public byte type = COD_BITS_UNASSIGNED;   // CODING SIZE TYPE. CURRENTLY DEFINED COD_BITS_4 & COD_BITS_8   
        public int H = NO_CODING;       // HIGH 4-BITS CODING (H == L for COD_BITS_8)
        public int L = NO_CODING;       // LOW 4-BITS CODING
    }
    
    //
    //  CODING, DEFINE INPUT AND OUTPUT WBACR AES CODING FOR 8-BITS ARGUMENT
    //
    public static class Coding {
        public HighLow IC;  
        public HighLow OC;

        public Coding() {
            IC = new HighLow();
            OC = new HighLow();
        }
    }

    //
    //  4-BITS TO 4-BITS BIJECTION 
    //
    public static class Coding4x4Table{
        public Bijection4x4   coding;
        public Bijection4x4   invCoding;          // SPEED OPTIMALIZATION, CAN BE ALSO COMPUTED FROM coding MEMBER (DUE TO BIJECTION PROPERTY)

        public Coding4x4Table() {
            coding    = new Bijection4x4();
            invCoding = new Bijection4x4();
        }
    }
    
    //
    //  8-BITS TO 8-BITS BIJECTION 
    //
    public static class Coding8x8Table{
        public Bijection8x8   coding;
        public Bijection8x8   invCoding;          // SPEED OPTIMALIZATION, CAN BE ALSO COMPUTED FROM coding MEMBER (DUE TO BIJECTION PROPERTY)

        public Coding8x8Table() {
            coding    = new Bijection8x8();
            invCoding = new Bijection8x8();
        }
    }
    
    public static class XORCODING {
        public Coding xtb[];
        public final int width;

        public XORCODING(Coding[] xtb) {
            this.xtb = xtb;
            this.width = xtb.length;
        }

        public XORCODING(int width) {
            this.width = width;
            this.xtb   = new Coding[width];
            for(int i=0; i<width; i++){
                this.xtb[i] = new Coding();
            }
        }
    }
    
    //
    // Generic coding for 8bit input argument coding
    //
    public static class W08xZZCODING {
        public HighLow   IC;
        public HighLow   OC[];          // SPEED OPTIMALIZATION, CAN BE ALSO COMPUTED FROM coding MEMBER (DUE TO BIJECTION PROPERTY)
        public final int width;

        public W08xZZCODING(HighLow IC, HighLow[] OC) {
            this.IC = IC;
            this.OC = OC;
            this.width = OC.length;
        }

        public W08xZZCODING(int width) {
            this.width = width;
            IC = new HighLow();
            OC = new HighLow[width];
            for(int i=0; i<width; i++){
                OC[i] = new HighLow(); 
            }
        }
    }
    
    //
    // Coding for T2 and T3 boxes, 8bit -> 32bit
    //
    public static class W08x32Coding extends W08xZZCODING{
        public W08x32Coding() {
            super(4);
        }
    }
    
    //
    // Coding for T1 boxes, 8bit -> 128bit
    //
    public static class W08x128Coding extends W08xZZCODING{
        public W08x128Coding() {
            super(16);
        }
    }
         
    public static void assrt(boolean condition){
        if (!condition){
            assert(condition);
            throw new AssertionError("Condition wasn't met");
        }
    }
    
    // Positive modulo
    public static int POS_MOD(int a, int m){
        return (((a) % (m)) < 0 ? ((a) % (m)) + (m) : (a) % (m));
    }
    
    /**
     * HI(xxxxyyyy) = 0000xxxx
     * @param x
     * @return 
     */
    public static byte HI(byte x){
        return (byte) (((x) >>> 4) & 0xF);
    }
    
    /**
     * LO(xxxxyyyy) = 0000yyyy
     * @param x
     * @return 
     */ 
    public static byte LO(byte x){
        return (byte) ((x) & 0xF);
    }
    
    /**
     * HILO(qqqqwwww, rrrrtttt) = wwwwtttt
     * @param h
     * @param l
     * @return 
     */
    public static byte HILO(byte h, byte l){
        return (byte) ((((h) & 0xF) << 4) | (l & 0xF));
    }
    
    //
    // Allocates new 4X4 encodings for 08xZZ tables (T1,T2,T3) from given offset (can be used to allocate also T1)
    // Allocation = generate unique bijection ID for particular IO box.
    // Only OC (output coding) is generated = donor of the bijection. IC = acceptor and is set by CONNECT* macros
    // From other tables OC fields.
    //
    public static int ALLOCW08xZZCodingEx(W08xZZCODING cod, int ofs, int idx) {
        for(int i=0; i<cod.width; i++){
            cod.OC[(ofs)+i].type = COD_BITS_4;
            
            // To avoid overwriting already allocated encodings.
            assrt(cod.OC[(ofs)+i].H==NO_CODING); 
            assrt(cod.OC[(ofs)+i].L==NO_CODING);
            
            cod.OC[(ofs)+i].L = ++(idx);
            cod.OC[(ofs)+i].H = ++(idx);
        }
        
        return idx;
    }
    
    //
    // Allocates new 4X4 encodings for 08x32 tables (T2,T3) from given offset (can be used to allocate also T1)
    // Allocation = generate unique bijection ID for particular IO box.
    // Only OC (output coding) is generated = donor of the bijection. IC = acceptor and is set by CONNECT* macros
    // From other tables OC fields.
    //
    public static int ALLOCW08x32CodingEx(W08x32Coding cod, int ofs, int idx) {
        return ALLOCW08xZZCodingEx(cod, ofs, idx);
    }
    
    public static int ALLOCW08x32CodingEx(W08x32Coding cod, int idx) {
        return ALLOCW08x32CodingEx(cod, 0, idx);
    }
    
    //
    // Allocate T1 tables - generate bijection IDs for output side of the table (128-bit wide)
    //
    public static int ALLOCW08x128Coding(W08x128Coding cod, int idx) {
        return ALLOCW08xZZCodingEx(cod, 0, idx);
    }
    
    //
    // Allocates new output coding for 4-bit XOR boxes XTB[offset+0 - offset+7], altogether 32 bit XOR table
    // Recall that output of XOR is stored in LOW part, thus upper is unused -> no allocation for upper part.
    //
    public static int ALLOCXORCoding(XORCODING xtb, int offset, int idx, int len) {
        for(int i=0; i<len; i++){
            // To avoid overwriting already allocated encodings.
            assrt(xtb.xtb[(offset)+i].OC.L==NO_CODING);
            
            xtb.xtb[(offset)+i].OC.type = COD_BITS_4;
            xtb.xtb[(offset)+i].OC.H    = UNUSED_CODING;
            xtb.xtb[(offset)+i].OC.L    = ++(idx);
        }
        
        return idx;
    }
    
    
    //
    // Connects OUTPUT coding of 32bit wide boxes (T2,T3) to INPUT coding of XOR boxes, 32bit wide. 
    // Each XOR box accepts 2 arguments, first in HIGH part, second in LOW part, thus when associating
    // mapping from one particular W32box we are using either HIGH or LOW parts. 
    //
    public static void CONNECT_W08x32_TO_XOR_EX(W08xZZCODING cod, XORCODING xtb, boolean HL, int offsetL, int offsetR) {
        // Connects 32 bit output to XOR encoding with 4bit width, thus 8 connections are needed.
        for (int i=0; i<4; i++){
            // To avoid overwriting already allocated encodings.
            assrt(HL ? xtb.xtb[(offsetL)+2*i  ].IC.H==NO_CODING : xtb.xtb[(offsetL)+2*i  ].IC.L==NO_CODING);
            assrt(HL ? xtb.xtb[(offsetL)+2*i+1].IC.H==NO_CODING : xtb.xtb[(offsetL)+2*i+1].IC.L==NO_CODING);
            // To avoid assigning empty/invalid encoding.
            assrt(cod.OC[(offsetR)+i].H!=NO_CODING && cod.OC[(offsetR)+i].H!=UNASSIGNED_CODING); 
            assrt(cod.OC[(offsetR)+i].L!=NO_CODING && cod.OC[(offsetR)+i].L!=UNASSIGNED_CODING); 
            
            if (HL){
                xtb.xtb[(offsetL)+2*i  ].IC.H = cod.OC[(offsetR)+i].H;
                xtb.xtb[(offsetL)+2*i+1].IC.H = cod.OC[(offsetR)+i].L;
            } else {
                xtb.xtb[(offsetL)+2*i  ].IC.L = cod.OC[(offsetR)+i].H;
                xtb.xtb[(offsetL)+2*i+1].IC.L = cod.OC[(offsetR)+i].L;
            }
        }
    }
    
    public static void CONNECT_W08x32_TO_XOR_H_EX(W08xZZCODING cod, XORCODING xtb, int offsetL, int offsetR){
        CONNECT_W08x32_TO_XOR_EX(cod, xtb, true, offsetL, offsetR);
    }
    
    public static void CONNECT_W08x32_TO_XOR_L_EX(W08xZZCODING cod, XORCODING xtb, int offsetL, int offsetR){
        CONNECT_W08x32_TO_XOR_EX(cod, xtb, false, offsetL, offsetR);
    }

    public static void CONNECT_W08x32_TO_XOR(W08xZZCODING cod, XORCODING xtb, boolean HL, int offset) {
        CONNECT_W08x32_TO_XOR_EX(cod, xtb, HL, offset, 0);
    }

    public static void CONNECT_W08x32_TO_XOR_H(W08xZZCODING cod, XORCODING xtb, int offset) {
        CONNECT_W08x32_TO_XOR_H_EX(cod, xtb, offset, 0);
    }

    public static void CONNECT_W08x32_TO_XOR_L(W08xZZCODING cod, XORCODING xtb, int offset) {
        CONNECT_W08x32_TO_XOR_L_EX(cod, xtb, offset, 0);
    }

    //
    // Connects OUTPUT coding for XOR tables to INPUT coding of XOR tables on lower layer.
    // Has effect of combining result of 2XOR tables to input of 1 XOR table.
    //
    // Recall that XOR result is always stored in lower part of XOR, thus on the left side we
    // are using OC.L;
    //
    // 1 XOR table accepts input from 2 sources. 
    // In HIGH part is first argument, in LOW part is the second. Same functionality as
    // in CONNECT_W08x32_TO_XOR macro
    //
    // This macro accepts XOR tables 32bit wide.
    public static void CONNECT_XOR_TO_XOR(XORCODING xtb1, int offset1, XORCODING xtb3, int offset3, boolean HL) {
        for (int i = 0; i < 8; i++) {
            // To avoid overwriting already connected encoding.
            assrt(HL ? xtb3.xtb[(offset3) + i].IC.H == NO_CODING : xtb3.xtb[(offset3) + i].IC.L == NO_CODING);
            // To avoid assigning empty encodings.
            assrt(xtb1.xtb[(offset1) + i].OC.L != NO_CODING && xtb1.xtb[(offset1) + i].OC.L != UNASSIGNED_CODING);

            if (HL) {
                xtb3.xtb[(offset3) + i].IC.H = xtb1.xtb[(offset1) + i].OC.L;
            } else {
                xtb3.xtb[(offset3) + i].IC.L = xtb1.xtb[(offset1) + i].OC.L;
            }
        }
    }

    public static void CONNECT_XOR_TO_XOR_128(XORCODING xtb1, int offset1, XORCODING xtb3, int offset3, boolean HL) {
        CONNECT_XOR_TO_XOR(xtb1, (offset1)+0,  xtb3, (offset3)+0,  HL);        
        CONNECT_XOR_TO_XOR(xtb1, (offset1)+8,  xtb3, (offset3)+8,  HL);        
        CONNECT_XOR_TO_XOR(xtb1, (offset1)+16, xtb3, (offset3)+16, HL);        
        CONNECT_XOR_TO_XOR(xtb1, (offset1)+24, xtb3, (offset3)+24, HL);
    }

    public static void CONNECT_XOR_TO_XOR_H(XORCODING xtb1, int offset1, XORCODING xtb3, int offset3) {
        CONNECT_XOR_TO_XOR(xtb1, offset1, xtb3, offset3, true);
    }
    
    public static void CONNECT_XOR_TO_XOR_L(XORCODING xtb1, int offset1, XORCODING xtb3, int offset3){
        CONNECT_XOR_TO_XOR(xtb1, offset1, xtb3, offset3, false);
    }
    
    public static void CONNECT_XOR_TO_XOR_128_H(XORCODING xtb1, int offset1, XORCODING xtb3, int offset3){
        CONNECT_XOR_TO_XOR_128(xtb1, offset1, xtb3, offset3, true);
    }
    
    public static void CONNECT_XOR_TO_XOR_128_L(XORCODING xtb1, int offset1, XORCODING xtb3, int offset3){
        CONNECT_XOR_TO_XOR_128(xtb1, offset1, xtb3, offset3, false);
    }

    //
    // Connects 8bit output from 2 consecutive XOR tables to 8b input of W08xZZ table
    //
    public static void CONNECT_XOR_TO_W08x32(XORCODING xtb, int offset, W08xZZCODING cod) {
        cod.IC.type = xtb.xtb[(offset)+0].OC.type;     
        
        // Asserts checks if someone is not trying to overwrite already allocated
        // mappings on inputs (INPUT). If there is already some coding, no
        // re-assign is allowed.
        // It is also checked if output mapping has some meaningful coding
        // if somebody is trying to assing it somewhere.
        assrt(cod.IC.H==NO_CODING);
        assrt(xtb.xtb[(offset)+0].OC.L!=UNASSIGNED_CODING && xtb.xtb[(offset)+0].OC.L!=NO_CODING); 
        assrt(cod.IC.L==NO_CODING);
        assrt(xtb.xtb[(offset)+1].OC.L!=UNASSIGNED_CODING && xtb.xtb[(offset)+1].OC.L!=NO_CODING); 

        cod.IC.H = xtb.xtb[(offset)+0].OC.L;                                          
        cod.IC.L = xtb.xtb[(offset)+1].OC.L;                                          
    }
    
    /**
     * Encodes with IO bijection src byte according to hl scheme.
     * @param src
     * @param hl
     * @param inverse
     * @param tbl4
     * @param tbl8
     * @return 
     */
    public static byte iocoding_encode08x08(byte src, HighLow hl, boolean inverse, Bijection4x4[] tbl4, Bijection8x8[] tbl8){
        if (hl.type == COD_BITS_4){
            return inverse ?
                  HILO(
                	USE_IDENTITY_CODING(hl.H) ? HI(src) : tbl4[hl.H].invCoding[HI(src)],
                	USE_IDENTITY_CODING(hl.L) ? LO(src) : tbl4[hl.L].invCoding[LO(src)])

                : HILO(
                	USE_IDENTITY_CODING(hl.H) ? HI(src) : tbl4[hl.H].coding[HI(src)],
                	USE_IDENTITY_CODING(hl.L) ? LO(src) : tbl4[hl.L].coding[LO(src)]);
        } else if (hl.type == COD_BITS_8){
            assrt(tbl8 != null);
            return inverse ?
                  (USE_IDENTITY_CODING(hl.L) ? src : tbl8[hl.L].invCoding[src])
                : (USE_IDENTITY_CODING(hl.L) ? src : tbl8[hl.L].coding[src]);
        }
        
        return src; 
    }
    
    /**
     * Encodes with IO bijection src byte according to Coding scheme.
     * @param src
     * @param coding
     * @param encodeInput
     * @param tbl4
     * @param tbl8
     * @return 
     */
    public static byte iocoding_encode08x08(byte src, Coding coding, boolean encodeInput, Bijection4x4[] tbl4, Bijection8x8[] tbl8){
	    HighLow hl = encodeInput ? coding.IC : coding.OC;
	    return iocoding_encode08x08(src, hl, encodeInput, tbl4, tbl8);
    }
    
    /**
     * Encodes with IO bijection src 32bit argument according to Coding scheme.
     * @param src
     * @param coding
     * @param encodeInput
     * @param tbl4
     * @param tbl8
     * @return 
     */
    public static long iocoding_encode32x32(long src, W08x32Coding coding, boolean encodeInput, Bijection4x4[] tbl4, Bijection8x8[] tbl8){
    	// encoding input - special case, input is just 8bit wide
        long dst = 0;
        if (encodeInput){
            dst |= Utils.byte2long(iocoding_encode08x08(Utils.long2byte(src, 0), coding.IC, encodeInput, tbl4, tbl8), 0);
            dst |= Utils.byte2long(iocoding_encode08x08(Utils.long2byte(src, 1), coding.IC, encodeInput, tbl4, tbl8), 1);
            dst |= Utils.byte2long(iocoding_encode08x08(Utils.long2byte(src, 2), coding.IC, encodeInput, tbl4, tbl8), 2);
            dst |= Utils.byte2long(iocoding_encode08x08(Utils.long2byte(src, 3), coding.IC, encodeInput, tbl4, tbl8), 3);
        } else {
            dst |= Utils.byte2long(iocoding_encode08x08(Utils.long2byte(src, 0), coding.OC[0], encodeInput, tbl4, tbl8), 0);
            dst |= Utils.byte2long(iocoding_encode08x08(Utils.long2byte(src, 1), coding.OC[1], encodeInput, tbl4, tbl8), 1);
            dst |= Utils.byte2long(iocoding_encode08x08(Utils.long2byte(src, 2), coding.OC[2], encodeInput, tbl4, tbl8), 2);
            dst |= Utils.byte2long(iocoding_encode08x08(Utils.long2byte(src, 3), coding.OC[3], encodeInput, tbl4, tbl8), 3);
        }
        return dst;
    }

    /**
     * Encodes with IO bijection src 128bit argument according to Coding scheme.
     * @param dst
     * @param src
     * @param coding
     * @param encodeInput
     * @param tbl4
     * @param tbl8 
     */
    public static void iocoding_encode128x128(State dst, State src, W08x128Coding coding, boolean encodeInput, Bijection4x4[] tbl4, Bijection8x8[] tbl8) {
        // encoding input - special case, input is just 8bit wide
        if (encodeInput) {
            for (int i = 0; i < State.BYTES; i++) {
                dst.set(iocoding_encode08x08(src.get(i), coding.IC, encodeInput, tbl4, tbl8), i);
            }
        } else {
            for (int i = 0; i < State.BYTES; i++) {
                dst.set(iocoding_encode08x08(src.get(i), coding.OC[i], encodeInput, tbl4, tbl8), i);
            }
        }
    }
    
    private AEShelper AESh;
    private AES AESi;
    private AESCodingMap AESMap;
    private InternalBijections io;
    private ExternalBijections extc;
    private boolean debug = false;
    private SecureRandom rand = new SecureRandom();
    
    private boolean useDualAESARelationsIdentity=false;
    private boolean useDualAESIdentity=false;
    private boolean useDualAESSimpeAlternate=false;
    private boolean useIO04x04Identity=false;
    private boolean useIO08x08Identity=true;
    private boolean useMB08x08Identity=false;
    private boolean useMB32x32Identity=false;

    /**
     * Generates mixing bijections (linear transformations) for AES algorithm.
     * @param io
     * @param L08x08rounds
     * @param MB32x32rounds
     * @param MB08x08Identity
     * @param MB32x32Identity
     * @return 
     */
    public int generateMixingBijections(InternalBijections io, 
            int L08x08rounds, int MB32x32rounds, 
            boolean MB08x08Identity, boolean MB32x32Identity){
        
	int r,i;
        LinearBijection[][] L08x08  = io.getMB_L08x08();
        LinearBijection[][] MB32x32 = io.getMB_MB32x32();

	// Generate all required 8x8 mixing bijections.
	       for (r = 0; r < L08x08rounds; r++) {
            for (i = 0; i < MB_CNT_08x08_PER_ROUND; i++) {
                if (!MB08x08Identity) {
                    final GF2MatrixEx m    = MixingBijection.generateMixingBijection(8, 4, rand, debug);
                    final GF2MatrixEx minv = (GF2MatrixEx) m.computeInverse();

                    L08x08[r][i].setMb(m);
                    L08x08[r][i].setInv(minv);
                } else {
                    L08x08[r][i].setMb( new GF2MatrixEx(8, GF2MatrixEx.MATRIX_TYPE_UNIT));
                    L08x08[r][i].setInv(new GF2MatrixEx(8, GF2MatrixEx.MATRIX_TYPE_UNIT));
                }
            }
        }

	       // Generate all required 32x32 mixing bijections.
        for (r = 0; r < MB32x32rounds; r++) {
            for (i = 0; i < MB_CNT_32x32_PER_ROUND; i++) {
                if (!MB32x32Identity) {
                    final GF2MatrixEx m    = MixingBijection.generateMixingBijection(32, 4, rand, debug);
                    final GF2MatrixEx minv = (GF2MatrixEx) m.computeInverse();

                    MB32x32[r][i].setMb(m);
                    MB32x32[r][i].setInv(minv);
                } else {
                    MB32x32[r][i].setMb( new GF2MatrixEx(32, GF2MatrixEx.MATRIX_TYPE_UNIT));
                    MB32x32[r][i].setInv(new GF2MatrixEx(32, GF2MatrixEx.MATRIX_TYPE_UNIT));
                }
            }
        }
        return 0;
    }

    /**
     * Generate mixing bijections for AES, for current instance.
     * @param identity
     * @return 
     */
    public int generateMixingBijections(boolean identity){
        return generateMixingBijections(io, MB_8x8, MB_32x32, identity, identity);
    }
    
    public int generate4X4Bijections(Bijection4x4[] tbl, int size, boolean identity){
            int i=0,c=0;
            for(; i<size; i++){
                    // HINT: if you are debugging IO problems, try to turn on and off some bijections,
                    // you can very easily localize the problem.

                    //if (i>=0x3c0) identity=true;
                    c |= generate4X4Bijection(tbl[i], identity);
            }

            return c;
    }

    public int generate8X8Bijections(Bijection8x8[] tbl, int size, boolean identity){
            int i=0,c=0;
            for(; i<size; i++){
                    c |= generate8X8Bijection(tbl[i], identity);
            }

            return c;
    }

    public int generate4X4Bijection(Bijection4x4 tbl, boolean identity) {
        if (!identity) {
            return GenUtils.generateRandomBijection(tbl.coding, tbl.invCoding, 16, true, rand);
        } else {
            byte i;
            for (i = 0; i < 16; i++) {
                tbl.coding[i] = i;
                tbl.invCoding[i] = i;
            }

            return 0;
        }
    }

    public int generate8X8Bijection(Bijection8x8 tbl, boolean identity) {
        if (!identity) {
            return GenUtils.generateRandomBijection(tbl.coding, tbl.invCoding, 256, true, rand);
        } else {
            int i;
            for (i = 0; i < 256; i++) {
                tbl.coding[i] = (byte)i;
                tbl.invCoding[i] = (byte)i;
            }

            return 0;
        }
    }
    
    /**
     * Generates external encodings randomly. 
     * @param extc
     * @param flags can determine whether some of generated bijections are identities.
     */
    public void generateExtEncoding(ExternalBijections extc, int flags){
	int k;
        
        // Initialize memory if empty
        extc.memoryAllocate();

	// generate 8x8 bijections at first
	for(k=0; k<2; k++){
            boolean identity = (k==0 && (flags & WBAESGEN_EXTGEN_fCID) > 0) || (k==1 && (flags & WBAESGEN_EXTGEN_lCID) > 0);
            generate4X4Bijections(extc.getLfC()[k], 2*AES.BYTES, identity);
	}

	// generate mixing bijection
	for(k=0; k<2; k++){
            boolean identity = (k==0 && (flags & WBAESGEN_EXTGEN_IDMID) > 0) || (k==1 && (flags & WBAESGEN_EXTGEN_ODMID) > 0);
            if (!identity){
                final GF2MatrixEx m    = MixingBijection.generateMixingBijection(128, 4, rand, debug);
                final GF2MatrixEx minv = (GF2MatrixEx) m.computeInverse();

                extc.getIODM()[k].setMb(m);
                extc.getIODM()[k].setInv(minv);
            } else {
                extc.getIODM()[k].setMb( new GF2MatrixEx(128, GF2MatrixEx.MATRIX_TYPE_UNIT));
                extc.getIODM()[k].setInv(new GF2MatrixEx(128, GF2MatrixEx.MATRIX_TYPE_UNIT));
            }
	}
    }
    
    /**
     * Generates input T1 tables.
     */
    public void generateT1Tables() {
        // To initialize T1[1] map, coding map is needed, since it takes input from last round, for this we need key material
        // to add S-box to T1[1], so it is not done here...
        int i, j;
        int b;

        final Bijection4x4[][] lfC = extc.getLfC();
        final LinearBijection[] IODM = extc.getIODM();
        State mapResult = new State();

        // At first initialize T1[0]
        for (i = 0; i < AES.BYTES; i++) {
            // i-th T1 table, indexed by cols

            // Build tables - for each byte
            for (b = 0; b < 256; b++) {
                int bb = b;
                mapResult.zero();

                // Decode with IO encoding
                bb = HILO(lfC[0][2 * i + 0].invCoding[HI((byte)b)], lfC[0][2 * i + 1].invCoding[LO((byte)b)]);
                // Transform bb to matrix, to perform mixing bijection operation (matrix multiplication)
                GF2MatrixEx tmpMat = new GF2MatrixEx(128, 1);
                // builds binary matrix [0 0 bb 0 0 0 0 0 0 0 0 0 0 0 0 0]^T, if i==2;
                NTLUtils.putByteAsColVector(tmpMat, (byte)bb, i * 8, 0);
                // Build MB multiplication result
                tmpMat = (GF2MatrixEx) IODM[0].getInv().rightMultiply(tmpMat);
                // Encode 128-bit wide output to map result
                for (j = 0; j < AES.BYTES; j++) {
                    mapResult.set(NTLUtils.colBinaryVectorToByte(tmpMat, 8 * j, 0), j);
                }
                // Encode mapResult with out encoding of T1 table
                iocoding_encode128x128(mapResult, mapResult, AESMap.getT1()[0][i].getCod(), false, io.getpCoding04x04(), null);
                // Store result value to lookup table
                AESi.getT1()[0][i].setValue(mapResult, b);
            }
        }
    }
    
    /**
     * Simple routine to generate one XOR table. 
     * @param xorCoding
     * @param xtb 
     */
    public static void generateXorTable(Coding xorCoding, byte[] xtb, Bijection4x4[] bio){
	for(int b=0; b<256; b++){
            int	bb = b;
            bb = iocoding_encode08x08((byte)bb, xorCoding.IC, true, bio, null);
            bb = HI((byte)bb) ^ LO((byte)bb);
            bb = iocoding_encode08x08((byte)bb, xorCoding.OC, false, bio, null);
            xtb[b] = (byte) bb;
	}
    }
    
    /**
     * Generates whole XOR cascade with 32bit input & output argument. No External 
     * encoding is used thus can be done here.
     */
    public void generateXorCascades(){
        final GXORCascade[][] xorMap = AESMap.getXor();
              XORCascade[][]  xor    = AESi.getXor();
        
        for(int r=0; r<AES.ROUNDS; r++){
            for(int i=0; i<2*State.COLS; i++){
                xorMap[r][i].generateTables(xor[r][i], this);
            }
        }
    }
    
    /**
     * Generates whole XOR cascade with 128bit input & output argument. 
     * 
     */
    public void generateXorStateCascades(){
        final GXORCascadeState[] xorMap = AESMap.getXorState();
              XORCascadeState[]  xor    = AESi.getXorState();
        
        xorMap[0].generateTables(xor[0], this);
        xorMap[1].generateTables(xor[1], this);
    }
    
    /**
     * Initializes internal structures prior generate().
     * Memory allocation, ...
     */
    public void initInternal(){
        AESh   = new AEShelper();
        AESi   = new AES();
        AESMap = new AESCodingMap();
        io     = new InternalBijections();
        
        // allocate memory needed
        System.out.println("Memory allocation...");
        AESi.init();
        AESMap.init();
        io.memoryAllocate();
    }
    
    /**
     * Generate whitebox AES tables.
     * @param encrypt
     * @param key
     * @param keySize
     * @param ex 
     */
    public void generate(boolean encrypt, byte[] key, int keySize, ExternalBijections ex){
        this.initInternal();
        extc = ex;
        
        System.out.println("AES initialization");
        AESh.build(encrypt);
        final GF2mField field = AESh.getField();
        
        // Create coding map. This step is always constant for each AES
        // but can be modified during debuging new features (enable/disable bijections).
        System.out.println("Coding map generation...");
        AESMap.generateCodingMap();
        
        // set external encodings to XORCascadeState
        AESMap.getXorState()[1].setExternalOut(extc.getLfC()[1]);
        
        // Allocate space for IO bijections
        io.alloc04x04(AESMap.getIdx());
        
        // Generate 4x4 IO bijections
        System.out.println("Generating IO bijections...");
        generate4X4Bijections(io.getpCoding04x04(), AESMap.getIdx(), useIO04x04Identity);
        
        // Generate mixing bijections
        System.out.println("Generating mixing bijections...");
        generateMixingBijections(io, MB_8x8, MB_32x32, useMB08x08Identity, useMB32x32Identity);
        
        // Init T1[0] tables - for the first round
	System.out.println("Generating first round tables (T1) ");
        generateT1Tables();
	
        // Generate round keys
        System.out.println("Computing key schedule ");
        byte[] keySchedule = AESh.keySchedule(key, keySize, debug);
        
        // Generate all XOR cascades
        System.out.println("Generating all 32bit XOR tables");
        this.generateXorCascades();
        this.generateXorStateCascades();
        
        // Generate cipher based tables
        int i,j,k,b;
        // pre-load bijections
        final LinearBijection[][] eMB_L08x08 = io.getMB_L08x08();
        final LinearBijection[][] eMB_MB32x32 = io.getMB_MB32x32();
        final GTBox8to128[][] t1C = AESMap.getT1();
        final GTBox8to32[][] t2C  = AESMap.getT2();
        final GTBox8to32[][] t3C  = AESMap.getT3();        
        final Bijection4x4[] pCoding04x04 = io.getpCoding04x04();
        final Bijection8x8[] pCoding08x08 = null;
        final LinearBijection[] IODM = extc.getIODM();
        final Bijection4x4[][] lfC   = extc.getLfC();
        
        T1Box[][] t1 = AESi.getT1();
        T2Box[][] t2 = AESi.getT2();
        T3Box[][] t3 = AESi.getT3();
        
        // Precompute L lookup table, L_k stripes
        byte Lr_k_table[][] = new byte[4][256];
        GF2MatrixEx Lr_k[] = new GF2MatrixEx[4];

        // Generate tables for AES
        for (int r = 0; r < AES.ROUNDS; r++) {
            System.out.println("Generating tables for round = " + (r + 1));

            // Iterate by mix cols/sections/dual AES-es
            for (i = 0; i < State.COLS; i++) {

                //
                // Build L lookup table from L_k stripes using shiftRowsLBijection (Lr_k is just simplification for indexes)
                // Now we are determining Lbox that will be used in next round.
                // Also pre-compute lookup tables by matrix multiplication
                for (j = 0; r < (AES.ROUNDS - 1) && j < State.COLS; j++) {
                    Lr_k[j] = eMB_L08x08[r][nextTbox(i * State.COLS + j, encrypt)].getMb();
                    for (b = 0; b < 256; b++) {
                        GF2MatrixEx tmpMat = new GF2MatrixEx(8, 1);
                        NTLUtils.putByteAsColVector(tmpMat, (byte) b, 0, 0);

                        // multiply with 8x8 mixing bijection to obtain transformed value
                        tmpMat = (GF2MatrixEx) Lr_k[j].rightMultiply(tmpMat);

                        // convert back to byte value
                        Lr_k_table[j][b] = NTLUtils.colBinaryVectorToByte(tmpMat, 0, 0);
                    }
                }

                //
                // T table construction (Type2, if r=last one, then T1); j iterates over rows
                //
                for (j = 0; j < State.ROWS; j++) {
                    final int idx = j * State.COLS + i; // index to state array, iterating by cols;

                    System.out.println("T[" + r + "][" + i + "][" + j + "] key = 16*" + r
                            + " + " + ((int) AES.shift(j * 4 + i, encrypt))
                            + " = " + (keySchedule[16 * r + AES.shift(j * 4 + i, encrypt)])
                            + "; idx=" + idx);

                    // Build tables - for each byte
                    for (b = 0; b < 256; b++) {
                        int tmpGF2E;
                        long mapResult;
                        GF2MatrixEx mPreMB;
                        GF2mMatrixEx mcres;
                        int bb = b;

                        // In the first round we apply codings from T1 tables.
                        // Decode input with IO coding
                        // For the last round, INPUT coding is for T1 box, otherwise for T2 box
                        if (r < (AES.ROUNDS - 1)) {
                            bb = iocoding_encode08x08((byte) bb, t2C[r][idx].getCod().IC, true, pCoding04x04, pCoding08x08);
                        } else {
                            bb = iocoding_encode08x08((byte) bb, t1C[r][idx].getCod().IC, true, pCoding04x04, pCoding08x08);
                        }

                        tmpGF2E = bb;

                        //
                        // Mixing bijection - removes effect induced in previous round (inversion here)
                        // Note: for DualAES, data from prev round comes here in prev Dual AES encoding, with applied bijection
                        // on them. Reversal = apply inverse of mixing bijection, undo prev Dual AES, do cur Dual AES
                        // Scheme: Tapply_cur( TapplyInv_prev( L^{-1}_{r-1}(x) ) )
                        //
                        // Implementation: matrix multiplication in GF2.
                        // Inversion to transformation used in previous round in T3 box (so skip this in first round).
                        if (r > 0) {
                            GF2MatrixEx tmpMat = new GF2MatrixEx(8, 1);
                            NTLUtils.putByteAsColVector(tmpMat, (byte) tmpGF2E, 0, 0);

                            tmpMat = (GF2MatrixEx) eMB_L08x08[r - 1][idx].getInv().rightMultiply(tmpMat);
                            tmpGF2E = NTLUtils.colBinaryVectorToByte(tmpMat, 0, 0);
                        }

                        //
                        // Encryption scenario:
                        // Build T_i box by composing with round key
                        //
                        // White box implementation:
                        // shiftRows(state)
                        // addRoundKey(state, shiftRows(ApplyT(K_{r-1}))) when indexing rounds from 1 and key from 0
                        //   K_{r-1} is AES key for default AES,
                        //   apply = linear transformation (multiplication by matrix T from dual AES) for changing default AES to dual AES.
                        //
                        // Rewritten to form:
                        // shiftRows(state)
                        // addRoundKey(state, ApplyT(shiftRows(K_r)))
                        //
                        // K_{r}  [x][y] = keySchedule[r][i] [16*(r)   + x*4 + y]
                        // in this round we want to work with AES from same dual AES, thus we are choosing
                        // keySchedule[r][i]. Also we have to take effect of ShiftRows() into account, thus apply
                        // ShiftRows() transformation on key indexes.
                        //
                        // Implementation in one section (i) corresponds to one column (0,5,10,15) are indexes taken
                        // for computation in one section in WBAES. Inside section (column) we are iterating over
                        // rows (j). Key is serialized by rows.
                        if (encrypt) {
                            int tmpKey = keySchedule[16 * r + State.transpose(AES.shift(idx, encrypt))];
                            tmpGF2E = field.add(tmpGF2E & 0xff, tmpKey & 0xff) & 0xff;
                        } else {
                            if (r == 0) {
                                // Decryption & first round => add k_10 to state.
                                // Same logic applies here
                                // AddRoundKey(State, k_10)  | -> InvShiftRows(State)
                                // InvShiftRows(State)       | -> AddRoundKey(State, InvShiftRows(k_10))
                                int tmpKey = keySchedule[16 * AES.ROUNDS + State.transpose(AES.shift(idx, encrypt))];
                                tmpGF2E = field.add(tmpGF2E & 0xff, tmpKey & 0xff) & 0xff;
                            }
                        }


                        // SBox transformation with dedicated AES for this round and section
                        // Encryption: ByteSub
                        // Decryption: ByteSubInv
                        int tmpE = encrypt ? AESh.ByteSub(tmpGF2E) : AESh.ByteSubInv(tmpGF2E);

                        // Decryption case:
                        // T(x) = Sbox(x) + k
                        if (!encrypt) {
                            tmpE = field.add(tmpE & 0xff, keySchedule[16 * (AES.ROUNDS - r - 1) + State.transpose(idx)] & 0xff) & 0xff;
                        }

                        // If we are in last round we also have to add k_10, not affected by ShiftRows()
                        // And more importantly, build T1
                        if (r == AES.ROUNDS - 1) {
                            // Adding last encryption key (k_10) by special way is performed only in encryption
                            if (encrypt) {
                                tmpE = field.add(tmpE & 0xff, keySchedule[16 * (r + 1) + State.transpose(idx)] & 0xff) & 0xff;
                            }

                            // Now we use output encoding G and quit, no MixColumn or Mixing bijections here.
                            State mapResult128 = new State();
                            bb = tmpE;

                            // Transform bb to matrix, to perform mixing bijection operation (matrix multiplication)
                            GF2MatrixEx tmpMat2 = new GF2MatrixEx(128, 1);
                            // builds binary matrix [0 0 bb 0 0 0 0 0 0 0 0 0 0 0 0 0], if curByte==2
                            NTLUtils.putByteAsColVector(tmpMat2, (byte) bb, (i * State.COLS + j) * 8, 0);
                            // Build MB multiplication result
                            tmpMat2 = (GF2MatrixEx) IODM[1].getMb().rightMultiply(tmpMat2);
                            // Encode 128-bit wide output to map result
                            for (int jj = 0; jj < 16; jj++) {
                                mapResult128.set(NTLUtils.colBinaryVectorToByte(tmpMat2, jj * 8, 0), jj);
                            }
                            // Encode mapResult with out encoding of T1 table
                            iocoding_encode128x128(mapResult128, mapResult128, t1C[1][idx].getCod(), false, pCoding04x04, pCoding08x08);
                            // Store result value to lookup table
                            t1[1][idx].getTbl()[b].setState(mapResult128.getState());
                            continue;
                        }

                        //
                        // MixColumn, Mixing bijection part
                        //	only in case 1..9 round

                        // Build [0 tmpE 0 0]^T stripe where tmpE is in j-th position
                        GF2mMatrixEx zj = new GF2mMatrixEx(field, 4, 1);
                        zj.set(j, 0, tmpE);

                        // Multiply with MC matrix from our AES dedicated for this round, only in 1..9 rounds (not in last round)
                        if (encrypt) {
                            mcres = r < (AES.ROUNDS - 1) ? AESh.getMixColMat().rightMultiply(zj) : zj;
                        } else {
                            mcres = r < (AES.ROUNDS - 1) ? AESh.getMixColInvMat().rightMultiply(zj) : zj;
                        }

                        // Apply 32x32 Mixing bijection, mPreMB is initialized to GF2MatrixEx with 32x1 dimensions,
                        // GF2E values are encoded to binary column vectors
                        mPreMB = NTLUtils.GF2mMatrix_to_GF2Matrix_col(mcres, 8);
                        mPreMB = (GF2MatrixEx) eMB_MB32x32[r][i].getMb().rightMultiply(mPreMB);

                        //
                        // TESTING - multiply by inversion
                        //
                        // Convert transformed vector back to values
                        mapResult = NTLUtils.GF2Matrix_to_long(mPreMB, 0, 0);

                        // Encode mapResult with out encoding
                        mapResult = iocoding_encode32x32(mapResult, t2C[r][idx].getCod(), false, pCoding04x04, pCoding08x08);
                        // Store result value to lookup table
                        t2[r][idx].getTbl()[b] = mapResult;
                    }
                }

                // In final round there are no more XOR and T3 boxes
                if (r == AES.ROUNDS - 1) {
                    continue;
                }

                //
                // B table construction (Type3) - just mixing bijections and L strip
                //
                for (j = 0; j < State.COLS; j++) {
                    final int idx = j * State.COLS + i; // index to state array, iterating by cols;

                    // Build tables - for each byte
                    for (b = 0; b < 256; b++) {
                        long mapResult;
                        int bb = b;
                        // Decode with IO encoding
                        bb = iocoding_encode08x08((byte) b, t3C[r][idx].getCod().IC, true, pCoding04x04, pCoding08x08);
                        // Transform bb to matrix, to perform mixing bijection operation (matrix multiplication)
                        GF2MatrixEx tmpMat = new GF2MatrixEx(32, 1);
                        // builds binary matrix [0 0 bb 0], if j==2
                        NTLUtils.putByteAsColVector(tmpMat, (byte) bb, j * 8, 0);
                        // Build MB multiplication result
                        tmpMat = (GF2MatrixEx) eMB_MB32x32[r][i].getInv().rightMultiply(tmpMat);
                        // Encode using L mixing bijection (another matrix multiplication)
                        // Map bytes from result via L bijections
                        mapResult = 0;
                        mapResult |= Utils.byte2long(Lr_k_table[0][NTLUtils.colBinaryVectorToByte(tmpMat, 8 * 0, 0)], 0);
                        mapResult |= Utils.byte2long(Lr_k_table[1][NTLUtils.colBinaryVectorToByte(tmpMat, 8 * 1, 0)], 1);
                        mapResult |= Utils.byte2long(Lr_k_table[2][NTLUtils.colBinaryVectorToByte(tmpMat, 8 * 2, 0)], 2);
                        mapResult |= Utils.byte2long(Lr_k_table[3][NTLUtils.colBinaryVectorToByte(tmpMat, 8 * 3, 0)], 3);
                        // Encode mapResult with out encoding
                        mapResult = iocoding_encode32x32(mapResult, t3C[r][idx].getCod(), false, pCoding04x04, pCoding08x08);
                        // Store result value to lookup table
                        t3[r][idx].getTbl()[b] = mapResult;
                        // cout << "T3["<<r<<"]["<<i<<"]["<<j<<"]["<<b<<"] = "; dumpW32b(mapResult);
                    }
                }
            }
        }
    }

    public AES getAESi() {
        return AESi;
    }

    public void setAESi(AES AESi) {
        this.AESi = AESi;
    }

    public ExternalBijections getExtc() {
        return extc;
    }

    public void setExtc(ExternalBijections extc) {
        this.extc = extc;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public SecureRandom getRand() {
        return rand;
    }

    public void setRand(SecureRandom rand) {
        this.rand = rand;
    }

    public boolean isUseDualAESARelationsIdentity() {
        return useDualAESARelationsIdentity;
    }

    public void setUseDualAESARelationsIdentity(boolean useDualAESARelationsIdentity) {
        this.useDualAESARelationsIdentity = useDualAESARelationsIdentity;
    }

    public boolean isUseDualAESIdentity() {
        return useDualAESIdentity;
    }

    public void setUseDualAESIdentity(boolean useDualAESIdentity) {
        this.useDualAESIdentity = useDualAESIdentity;
    }

    public boolean isUseDualAESSimpeAlternate() {
        return useDualAESSimpeAlternate;
    }

    public void setUseDualAESSimpeAlternate(boolean useDualAESSimpeAlternate) {
        this.useDualAESSimpeAlternate = useDualAESSimpeAlternate;
    }

    public boolean isUseIO04x04Identity() {
        return useIO04x04Identity;
    }

    public void setUseIO04x04Identity(boolean useIO04x04Identity) {
        this.useIO04x04Identity = useIO04x04Identity;
    }

    public boolean isUseIO08x08Identity() {
        return useIO08x08Identity;
    }

    public void setUseIO08x08Identity(boolean useIO08x08Identity) {
        this.useIO08x08Identity = useIO08x08Identity;
    }

    public boolean isUseMB08x08Identity() {
        return useMB08x08Identity;
    }

    public void setUseMB08x08Identity(boolean useMB08x08Identity) {
        this.useMB08x08Identity = useMB08x08Identity;
    }

    public boolean isUseMB32x32Identity() {
        return useMB32x32Identity;
    }

    public void setUseMB32x32Identity(boolean useMB32x32Identity) {
        this.useMB32x32Identity = useMB32x32Identity;
    }

    public AESCodingMap getAESMap() {
        return AESMap;
    }

    public InternalBijections getIo() {
        return io;
    }
    
}
