import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class App {
    static byte[] buf = new byte[1024];
    static Base64.Encoder enc = Base64.getEncoder();
    static Cipher ecipher;
    static Cipher dcipher;

    public static void main(String[] args) throws Exception {

        // Keys.txt file create and open for write
        String filePath = "keys.txt";
        FileWriter fileWriter = new FileWriter(filePath, false);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        /****** Q1 ************/
        printWriter.println("// Question 1");

        // Create key pair generator with RSA
        KeyPairGenerator kpgRSA = KeyPairGenerator.getInstance("RSA");
        // The length of the keys should be at least 1024 bits
        kpgRSA.initialize(1024);

        // Generation an RSA public-private key pairs. Ka(+) and Ka(-)
        KeyPair keyPairRSA = kpgRSA.generateKeyPair();
        PublicKey publicKa = keyPairRSA.getPublic();
        PrivateKey privateKa = keyPairRSA.getPrivate();

        // Create key pair generator with Elliptic-Curve Diffie Helman
        KeyPairGenerator kpgEC = KeyPairGenerator.getInstance("EC");
        // The length of the keys 256 bits
        kpgEC.initialize(256);

        // Generation an Elliptic-Curve Diffie Helman public-private key pairs. Kb(+)
        // and Kb(-)
        KeyPair kpEC = kpgEC.generateKeyPair();
        PublicKey publicKb = kpEC.getPublic();
        PrivateKey privateKb = kpEC.getPrivate();

        // Generation an Elliptic-Curve Diffie Helman public-private key pairs. Kc(+)
        // and Kc(-)
        KeyPair kpEC2 = kpgEC.generateKeyPair();
        PublicKey publicKc = kpEC2.getPublic();
        PrivateKey privateKc = kpEC2.getPrivate();

        // Print file Public-Private key pairs.
        printWriter.println("Public Key Ka(+)\n" + enc.encodeToString(publicKa.getEncoded()));
        printWriter.println("Private Key Ka(-)\n" + enc.encodeToString(privateKa.getEncoded()));
        printWriter.println("Public Key Kb(+)\n" + enc.encodeToString(publicKb.getEncoded()));
        printWriter.println("Private Key Kb(-)\n" + enc.encodeToString(privateKb.getEncoded()));
        printWriter.println("Public Key Kc(+)\n" + enc.encodeToString(publicKc.getEncoded()));
        printWriter.println("Private Key Kc(-)\n" + enc.encodeToString(privateKc.getEncoded()));

        /****** Q2 ************/
        printWriter.println("\n// Question 2");

        // Create SecureRandom
        SecureRandom securerandom = new SecureRandom();
        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");

        // Generation Symetric keys
        keygenerator.init(128, securerandom);
        SecretKey K1 = keygenerator.generateKey();
        printWriter.println("K1: " + new String(enc.encodeToString(K1.getEncoded())));

        keygenerator.init(256, securerandom);
        SecretKey K2 = keygenerator.generateKey();
        printWriter.println("K2: " + new String(enc.encodeToString(K2.getEncoded())));

        // Create RSA cipher
        Cipher cipher = Cipher.getInstance("RSA");

        // K1 encryption and decryption
        cipher.init(Cipher.ENCRYPT_MODE, publicKa);
        byte[] encryptedK1 = cipher.doFinal((enc.encodeToString(K1.getEncoded())).getBytes());
        printWriter.println("Encryption K1 with Ka(+):" + new String(enc.encodeToString(encryptedK1)));
        cipher.init(Cipher.DECRYPT_MODE, privateKa);
        byte[] decryptedK1 = cipher.doFinal(encryptedK1);
        printWriter.println("Decryption K1 with Ka(-):" + new String(decryptedK1));

        // K2 encryption and decryption
        cipher.init(Cipher.ENCRYPT_MODE, publicKa);
        byte[] encryptedK2 = cipher.doFinal((enc.encodeToString(K2.getEncoded())).getBytes());
        printWriter.println("Encryption K2 with Ka(+):" + new String(enc.encodeToString(encryptedK2)));
        cipher.init(Cipher.DECRYPT_MODE, privateKa);
        byte[] decryptedK2 = cipher.doFinal(encryptedK2);
        printWriter.println("Decryption K2 with Ka(-):" + new String(decryptedK2));

        /****** Q3 ************/
        printWriter.println("\n// Question 3");

        // Get message from file
        String message = "";
        File myObj = new File("message.txt");
        try (Scanner myReader = new Scanner(myObj)) {
            while (myReader.hasNextLine()) {
                message = myReader.nextLine();
            }
        }

        // Apply SHA256 Hash algorithm (Obtain the message digest, H(m))
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashInBytes = md.digest(message.getBytes(StandardCharsets.UTF_8));

        // bytes to hex
        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        printWriter.println("H(m): " + sb.toString());

        // Encryption with Ka(-)
        cipher.init(Cipher.ENCRYPT_MODE, privateKa);
        byte[] encrypt_Hm = cipher.doFinal(message.getBytes());
        printWriter.println("Digital Signature: " + new String(enc.encodeToString(encrypt_Hm)));

        // Decryption with Ka(+)
        cipher.init(Cipher.DECRYPT_MODE, publicKa);
        byte[] decHm = cipher.doFinal(encrypt_Hm);
        printWriter.println("Message: " + new String(decHm));

        /****** Q4 ************/
        printWriter.println("\n// Question 4");

        byte[] iv = new byte[16];
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);

        try {
            ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // Encrypt
            long startTime3 = System.nanoTime();
            ecipher.init(Cipher.ENCRYPT_MODE, K1, paramSpec);
            long endTime3 = System.nanoTime();
            encryptAES(new FileInputStream("q4.txt"), new FileOutputStream("q4_enc_k1.txt"));
            long time3 = endTime3 - startTime3;
            printWriter.println("AES128 encryption took " + time3 + " nanosecond");

            // Decrypt
            long startTime4 = System.nanoTime();
            dcipher.init(Cipher.DECRYPT_MODE, K1, paramSpec);
            long endTime4 = System.nanoTime();
            decryptAES(new FileInputStream("q4_enc_k1.txt"), new FileOutputStream("q4_dec_k1.txt"));
            long time4 = endTime4 - startTime4;
            printWriter.println("AES128 decryption took "+time4 +" nanosecond");

        } catch (Exception e) {
            e.printStackTrace();
        }

        /****** Q5 ************/
        printWriter.println("\n// Question 5");

        // Generate a message authentication code (HMAC-SHA256) using K1 the symmetric
        // keys.
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        sha256_HMAC.init(K1);

        printWriter.close();

    }

    public static void encryptAES(InputStream in, OutputStream out){
        try{
            out = new CipherOutputStream(out, ecipher);
            int numRead = 0;
            while ((numRead = in.read(buf)) >= 0){
                out.write(buf, 0, numRead);
            }
            out.close();
        }
        catch (java.io.IOException e){
        }
    }
    
    public static void decryptAES(InputStream in, OutputStream out){
        try{
            in = new CipherInputStream(in, dcipher);
            int numRead = 0;
            while ((numRead = in.read(buf)) >= 0){
                out.write(buf, 0, numRead);
            }
            out.close();
        }
        catch (java.io.IOException e)
        {
        }
    }
}