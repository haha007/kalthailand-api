package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.exeption.EncryptException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class EncryptUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptUtil.class);

    private static final String PRIVATE_KEY_FILE_PATH = "/encryption/private_key.der";
    private static final String PUBLIC_KEY_FILE_PATH = "/encryption/public_key.der";

    private static final PrivateKey PRIVATE_KEY = EncryptKeyFileUtil.loadPrivateKey(PRIVATE_KEY_FILE_PATH);
    private static final PublicKey PUBLIC_KEY = EncryptKeyFileUtil.loadPublicKey(PUBLIC_KEY_FILE_PATH);

    public static String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(EncryptKeyFileUtil.ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, PUBLIC_KEY);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            return new String(Base64.encodeBase64(cipherText));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException ex) {
            throw new EncryptException("Error while encrypt. " + ex.getMessage(), ex);
        }
    }

    public static String decrypt(String encodedText) {
        try {
            Cipher cipher = Cipher.getInstance(EncryptKeyFileUtil.ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY);
            byte[] cipherText = cipher.doFinal(Base64.decodeBase64(encodedText));
            return new String(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException ex) {
            throw new EncryptException("Error while decrypt. " + ex.getMessage(), ex);
        }
    }
}
