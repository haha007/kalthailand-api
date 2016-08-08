package th.co.krungthaiaxa.api.common.utils;


import sun.security.rsa.RSAPublicKeyImpl;
import th.co.krungthaiaxa.api.common.exeption.FileIOException;
import th.co.krungthaiaxa.api.common.exeption.FileNotFoundException;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;

import javax.crypto.KeyGenerator;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * This is the new file which will replace RsaUtil in api-elife module.
 * <p>
 * Method 1:
 * Generate a 2048-bit RSA private key
 * $ openssl genrsa -out private_key.pem 2048
 * <p>
 * Convert private Key to PKCS#8 format (so Java can read it)
 * $ openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
 * <p>
 * Output public key portion in DER format (so Java can read it)
 * $ openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der
 * <p>
 * <p>
 * Method 2:
 * ssh-keygen -f "/opt/keys/new_rsa" -t rsa
 * <p>
 * Result:
 * new_rsa (private file)
 * new_rsa.pub (public file)
 *
 * openssl rsa -in new_rsa -out new_rsa.der -outform DER -pubout writing RSA key
 * ssh-keygen -f new_rsa.pub -e -m PKCS8 | openssl pkey -pubin -outform DER

 */
public class EncryptKeyFileUtil {
    public static final String ENCRYPTION_ALGORITHM = "RSA";
    private static final KeyFactory KEY_FACTORY = initKeyFactory();

    private static KeyFactory initKeyFactory() {
        try {
            return KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new UnexpectedException("Cannot create KeyFactory: " + e.getMessage(), e);
        }
    }

    public static PrivateKey loadPrivateKey(String filename) {
        try {
            KeyFactory factory = KEY_FACTORY;
            byte[] content = loadFile(filename);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(content);
            return factory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            String msg = String.format("Cannot read the private key from file '%s': %s", filename, e.getMessage());
            throw new UnexpectedException(msg, e);
        }
    }

    public static PublicKey loadPublicKey(String filename) {
        try {
            KeyFactory factory = KEY_FACTORY;
            byte[] content = loadFile(filename);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(content);
            return factory.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            String msg = String.format("Cannot read the public key from file '%s': %s", filename, e.getMessage());
            throw new UnexpectedException(msg, e);
        }
    }

    public static byte[] loadFile(String fileName) {
        File f = new File(fileName);
        FileInputStream fis = null;
        DataInputStream dis = null;
        try {
            fis = new FileInputStream(f);
            dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int) f.length()];
            dis.readFully(keyBytes);
            return keyBytes;
        } catch (IOException e) {
            throw new FileNotFoundException(String.format("Cannot load file '%s'", fileName));
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                throw new FileNotFoundException(String.format("Cannot close file reader '%s'", fileName));
            }
        }
    }

    

}
