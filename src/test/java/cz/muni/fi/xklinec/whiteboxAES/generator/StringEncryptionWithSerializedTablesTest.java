package cz.muni.fi.xklinec.whiteboxAES.generator;

import cz.muni.fi.xklinec.whiteboxAES.AES;
import cz.muni.fi.xklinec.whiteboxAES.State;

import java.io.*;
import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

public class StringEncryptionWithSerializedTablesTest extends TestCase {
    private final static String TEST_STRING = "testString123";
    private String encryptorTableFileName = "AESenc.ser";
    private String decryptorTableFileName = "AESdec.ser";

    public void testGenerate() {
        Generator gEnc = new Generator();
        Generator gDec = new Generator();

        // External encoding is needed, at least some, generate identities
        ExternalBijections extc = new ExternalBijections();
        gEnc.generateExtEncoding(extc, 0);

        // at first generate pure table AES implementation
        gEnc.setUseIO04x04Identity(false);
        gEnc.setUseIO08x08Identity(false);
        gEnc.setUseMB08x08Identity(false);
        gEnc.setUseMB32x32Identity(false);

        gDec.setUseIO04x04Identity(false);
        gDec.setUseIO08x08Identity(false);
        gDec.setUseMB08x08Identity(false);
        gDec.setUseMB32x32Identity(false);

        // Generate AES for encryption
        gEnc.generate(true, AEShelper.testVect128_key, 16, extc);
        AES AESenc = gEnc.getAESi();
        // Generate AES for decryption
        gDec.generate(false, AEShelper.testVect128_key, 16, extc);
        AES AESdec = gDec.getAESi();

        serialize(encryptorTableFileName, AESenc);
        serialize(decryptorTableFileName, AESdec);

        byte[] fileContent = TEST_STRING.getBytes();

        State plain = new State(fileContent, true, false);
        State state = new State(fileContent, true, false);

        // Encrypt
        state.transpose();
        gEnc.applyExternalEnc(state, extc, true);
        AESenc.crypt(state);
        gEnc.applyExternalEnc(state, extc, false);

        AES AESencDeser = deserialize(encryptorTableFileName);
        AES AESdecDeser = deserialize(decryptorTableFileName);

        gEnc.setAESi(AESencDeser);
        gDec.setAESi(AESdecDeser);


        byte[] fileEncContent = state.getStateCopy();
        State cipher = new State(fileEncContent, true, false);

        // Decrypt
        cipher.transpose();
        gDec.applyExternalEnc(cipher, extc, true);
        AESdecDeser.crypt(cipher);
        gDec.applyExternalEnc(cipher, extc, false);
        assertEquals("Cipher bytes mismatch", true, plain.equals(cipher));
        assertEquals("Cipher string mismatch", TEST_STRING, new String(cipher.getStateCopy(), StandardCharsets.UTF_8).replaceAll("\u0000.*", ""));
    }

    private static AES deserialize(String tableFileName) {
        AES aes = null;
        try {
            FileInputStream fileIn = new FileInputStream(tableFileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            aes = (AES) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return aes;
    }


    private void serialize(String encryptorTableFileName, AES AESenc) {
        try {
            FileOutputStream fileOut = new FileOutputStream(encryptorTableFileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(AESenc);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in " + encryptorTableFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
