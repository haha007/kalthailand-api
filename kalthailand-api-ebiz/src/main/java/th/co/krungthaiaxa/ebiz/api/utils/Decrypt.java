package th.co.krungthaiaxa.ebiz.api.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;

public class Decrypt {
    private static final String DEFAULT_KEY = "d2e045a09586928a5cddfbf54bbb59cc";

    public static String decrypt(String encrypted, String secretkey) throws Exception {
        try {
            return doDecrypt(encrypted, secretkey);
        } catch (Exception e) {
            return doDecrypt(encrypted, DEFAULT_KEY);
        }
    }

    private static String doDecrypt(String encrypted, String secretkey) throws Exception {
        String utf8Encrypted = URLDecoder.decode(encrypted, "UTF-8");
        utf8Encrypted = StringUtils.replaceChars(utf8Encrypted, " ", "+");
        byte[] toDecode = Base64.decodeBase64(utf8Encrypted);

        byte[] bytes = new byte[secretkey.getBytes("UTF-8").length / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(secretkey.substring(i * 2, i * 2 + 2), 16);
        }

        SecretKeySpec secretKey = new SecretKeySpec(bytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return new String(cipher.doFinal(toDecode), "UTF-8");
    }
}
