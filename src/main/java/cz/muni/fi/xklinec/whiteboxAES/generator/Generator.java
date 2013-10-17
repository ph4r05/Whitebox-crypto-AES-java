/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.AES;
import cz.muni.fi.xklinec.whiteboxAES.State;
import cz.muni.fi.xklinec.whiteboxAES.Utils;
import cz.muni.fi.xklinec.whiteboxAES.W32b;
import java.security.SecureRandom;

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
    public static final int  COD_BITS_8_EXT      = 0x03;

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
    
    //
    //  HIGHLOW, DEFINE TWO 4-BITS CODING FOR 8-BITS ARGUMENT
    //
    public static class HighLow {
        public byte type;   // CODING SIZE TYPE. CURRENTLY DEFINED COD_BITS_4 & COD_BITS_8   
        public int H;       // HIGH 4-BITS CODING (H == L for COD_BITS_8)
        public int L;       // LOW 4-BITS CODING
    }
    
    //
    //  CODING, DEFINE INPUT AND OUTPUT WBACR AES CODING FOR 8-BITS ARGUMENT
    //
    public static class Coding {
        public HighLow IC;  
        public HighLow OC;
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
            
            assrt(cod.OC[(ofs)+i].H==UNASSIGNED_CODING); 
            assrt(cod.OC[(ofs)+i].L==UNASSIGNED_CODING);
            
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
            assrt(xtb.xtb[(offset)+i].OC.L==UNASSIGNED_CODING);
            
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
        for (int i=0; i<8; i++){
            assrt(HL ? xtb.xtb[(offsetL)+i].IC.H==UNASSIGNED_CODING : xtb.xtb[(offsetL)+i].IC.L==UNASSIGNED_CODING);
            assrt(cod.OC[(offsetR)+i].H!=UNASSIGNED_CODING); 
            
            if (HL){
                xtb.xtb[(offsetL)+i].IC.H = cod.OC[(offsetR)+i].H;
            } else {
                xtb.xtb[(offsetL)+i].IC.L = cod.OC[(offsetR)+i].H;
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
            assrt(HL ? xtb3.xtb[(offset3) + i].IC.H == UNASSIGNED_CODING : xtb3.xtb[(offset3) + i].IC.L == UNASSIGNED_CODING);
            assrt(xtb1.xtb[(offset1) + i].OC.L != UNASSIGNED_CODING);

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
        assrt(cod.IC.H==UNASSIGNED_CODING);
        assrt(xtb.xtb[(offset)+0].OC.L!=UNASSIGNED_CODING); 
        assrt(cod.IC.L==UNASSIGNED_CODING);
        assrt(xtb.xtb[(offset)+1].OC.L!=UNASSIGNED_CODING); 

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
            return GenUtils.generateRandomBijection(tbl.coding, tbl.invCoding, MB_8x8, true, rand);
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
            byte i;
            for (i = 0; i < 256; i++) {
                tbl.coding[i] = i;
                tbl.invCoding[i] = i;
            }

            return 0;
        }
    }
    
    /**
     * Generates input T1 tables.
     */
    public void generateT1Tables() {
        // To initialize T1[1] map, coding map is needed, since it takes input from last round, for this we need key material
        // to add S-box to T1[1], so it is not done here...
        int i, j;
        byte b;

        final Bijection4x4[][] lfC = extc.getLfC();
        final LinearBijection[] IODM = extc.getIODM();
        State mapResult = new State();

        // At first initialize T1[0]
        for (i = 0; i < AES.BYTES; i++) {
            // i-th T1 table, indexed by cols

            // Build tables - for each byte
            for (b = 0; b < 256; b++) {
                byte bb = b;
                mapResult.zero();

                // Decode with IO encoding
                bb = HILO(lfC[0][2 * i + 0].invCoding[HI(b)], lfC[0][2 * i + 1].invCoding[LO(b)]);
                // Transform bb to matrix, to perform mixing bijection operation (matrix multiplication)
                GF2MatrixEx tmpMat = new GF2MatrixEx(128, 1);
                // builds binary matrix [0 0 bb 0 0 0 0 0 0 0 0 0 0 0 0 0]^T, if i==2;
                NTLUtils.putByteAsColVector(tmpMat, bb, i * 8, 0);
                // Build MB multiplication result
                tmpMat = (GF2MatrixEx) IODM[0].getInv().rightMultiply(tmpMat);
                // Encode 128-bit wide output to map result
                for (j = 0; j < AES.BYTES; j++) {
                    mapResult.set(NTLUtils.ColBinaryVectorToByte(tmpMat, 8 * j, 0), j);
                }
                // Encode mapResult with out encoding of T1 table
                iocoding_encode128x128(mapResult, mapResult, AESMap.getT1()[0][i].getCod(), false, io.getpCoding04x04(), null);
                // Store result value to lookup table
                AESi.getT1()[0][i].setValue(mapResult, b);
            }
        }
    }
    
    public void generate(boolean encrypt){
        AESi   = new AES();
        AESMap = new AESCodingMap();
        io     = new InternalBijections();
        extc   = new ExternalBijections();
        
        // allocate memory needed
        System.out.println("Memory allocation...");
        AESi.init();
        AESMap.init();
        io.memoryAllocate();
        extc.memoryAllocate();
        
        // Create coding map. This step is always constant for each AES
        // but can be modified during debuging new features (enable/disable bijections).
        System.out.println("Coding map generation...");
        AESMap.generateCodingMap();
        
        // Allocate space for IO bijections
        io.alloc04x04(AESMap.getIdx());
        
        // Generate 4x4 IO bijections
        System.out.println("Generating IO bijections...");
        generate4X4Bijections(io.getpCoding04x04(), AESMap.getIdx(), useIO04x04Identity);
        
        // Generate mixing bijections
        System.out.println("Generating mixing bijections...");
        generateMixingBijections(io, MB_8x8, MB_32x32, useMB08x08Identity, useMB32x32Identity);
    }
}
