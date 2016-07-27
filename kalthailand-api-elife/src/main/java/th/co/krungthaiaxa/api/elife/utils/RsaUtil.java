package th.co.krungthaiaxa.api.elife.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import th.co.krungthaiaxa.api.elife.service.ApplicationFormService;

public class RsaUtil {

    private static final String KEY_FILE_PATH = "/opt/keys/";
    private static final String PRIVATE_KEY_NAME = "private.key";
    private static final String PUBLIC_KEY_NAME = "public.key";
    public static final String ALGORITHM = "RSA";

    private final static Logger logger = LoggerFactory.getLogger(RsaUtil.class);

    /*
     * input = string encrypted in base64 string
     * output = string decrypted in string
     * */
    public static String decrypt(String text) {
        System.out.println("text:" + text);
        logger.info("decrypt ===============>");
        if (!areKeysPresent()) {
            //generateKeys();
            logger.error("cannot find key .....");
        }
        if (text.length() < 14) {
            return text;
        }
        String privateKeyFileString = getPathFileName() + PRIVATE_KEY_NAME;
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(privateKeyFileString));
        } catch (IOException e) {
            logger.error("Error while decrypt 00001 .....", e);
        }
        PrivateKey privateKey = null;
        try {
            privateKey = (PrivateKey) inputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            logger.error("Error while decrypt 00002 .....", e);
        }
        byte[] dectyptedText = null;
        try {
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            dectyptedText = cipher.doFinal(Base64.decodeBase64(text));
        } catch (Exception ex) {
            logger.error("Error while decrypt 00003 .....", ex);
        }
        return new String(dectyptedText);
    }

    /*
     * input = string to encrypt
     * output = string encrypted in base64 string
     * */
    public static String encrypt(String text) {
        logger.info("encrypt ===============>");
        if (!areKeysPresent()) {
            //generateKeys();
            logger.error("cannot find key .....");
        }
        if (text.length() > 13) {
            return text;
        }
        String publicKeyFileString = getPathFileName() + PUBLIC_KEY_NAME;
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(publicKeyFileString));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        PublicKey publicKey = null;
        try {
            publicKey = (PublicKey) inputStream.readObject();
        } catch (ClassNotFoundException | IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        byte[] cipherText = null;
        try {
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            logger.error("Error while encrypt 00001 .....", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("Error while encrypt 00002 .....", e);
            }
            inputStream = null;
        }
        return new String(Base64.encodeBase64(cipherText));
    }

    private static String getPathFileName() {
        if ((new File(KEY_FILE_PATH)).exists() == true) {
            // for linux
            return KEY_FILE_PATH;
        } else {
            // for windows
            return "C:\\keys\\";
        }
    }

    public static void generateKeys() {
        String privateKeyFileString = getPathFileName() + PRIVATE_KEY_NAME;
        String publicKeyFileString = getPathFileName() + PUBLIC_KEY_NAME;
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(2048);
            final KeyPair key = keyGen.generateKeyPair();

            File privateKeyFile = new File(privateKeyFileString);
            File publicKeyFile = new File(publicKeyFileString);

            // Create files to store public and private key
            if (privateKeyFile.getParentFile() != null) {
                privateKeyFile.getParentFile().mkdirs();
            }
            privateKeyFile.createNewFile();

            if (publicKeyFile.getParentFile() != null) {
                publicKeyFile.getParentFile().mkdirs();
            }
            publicKeyFile.createNewFile();

            // Saving the Public key in a file
            ObjectOutputStream publicKeyOS = new ObjectOutputStream(
                    new FileOutputStream(publicKeyFile));
            publicKeyOS.writeObject(key.getPublic());
            publicKeyOS.close();

            // Saving the Private key in a file
            ObjectOutputStream privateKeyOS = new ObjectOutputStream(
                    new FileOutputStream(privateKeyFile));
            privateKeyOS.writeObject(key.getPrivate());
            privateKeyOS.close();
        } catch (Exception e) {
            logger.error("Error while generateKeys .....", e);
        }

    }

    private static boolean areKeysPresent() {
        String privateKeyFileString = getPathFileName() + PRIVATE_KEY_NAME;
        String publicKeyFileString = getPathFileName() + PUBLIC_KEY_NAME;
        File privateKey = new File(privateKeyFileString);
        File publicKey = new File(publicKeyFileString);
        if (privateKey.exists() && publicKey.exists()) {
            return true;
        }
        return false;
    }

    private byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = null;
        try {
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            logger.error("Error while areKeysPresent .....", e);
        }
        return cipherText;
    }

    private String decrypt(byte[] text, PrivateKey key) {
        byte[] dectyptedText = null;
        try {
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            dectyptedText = cipher.doFinal(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new String(dectyptedText);
    }

    /**
     * Test the EncryptionUtil
     */
      /*
      public static void main(String[] args) {
		  
		String privateKeyFileString = PRIVATE_KEY_FILE_PATH + PRIVATE_KEY_NAME;
		String publicKeyFileString = PUBLIC_KEY_FILE_PATH + PUBLIC_KEY_NAME;

	    try {

	      // Check if the pair of keys are present else generate those.
	      if (!areKeysPresent()) {
	        // Method generates a pair of keys using the RSA algorithm and stores it
	        // in their respective files
	        generateKeys();
	      }

	      final String originalText = "3101202780273";
	      ObjectInputStream inputStream = null;

	      // Encrypt the string using the public key
	      inputStream = new ObjectInputStream(new FileInputStream(publicKeyFileString));
	      final PublicKey publicKey = (PublicKey) inputStream.readObject();
	      final byte[] cipherText = encrypt(originalText, publicKey);

	      // Decrypt the cipher text using the private key.
	      inputStream = new ObjectInputStream(new FileInputStream(privateKeyFileString));
	      final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
	      final String plainText = decrypt(cipherText, privateKey);

	      // Printing the Original, Encrypted and Decrypted Text
	      System.out.println("Original: " + originalText);
	      System.out.println("Encrypted: " +cipherText.toString());
	      System.out.println("Decrypted: " + plainText);

	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }
	  */

}
