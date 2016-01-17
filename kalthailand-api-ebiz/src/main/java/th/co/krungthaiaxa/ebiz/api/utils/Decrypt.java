package th.co.krungthaiaxa.ebiz.api.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;

public class Decrypt {

    public static String decrypt(String encrypted, String secretkey) throws Exception {
        String utf8Encrypted = URLDecoder.decode(encrypted, "UTF-8");
        utf8Encrypted = StringUtils.replaceChars(utf8Encrypted, " ", "+");
        byte[] toDecode = Base64.decodeBase64(utf8Encrypted);

        SecretKeySpec secretKey = new SecretKeySpec(hexToBytes(secretkey), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return new String(cipher.doFinal(toDecode), "UTF-8");
    }

    private static byte[] hexToBytes(String hex) throws Exception {
        byte[] bytes = new byte[hex.getBytes("UTF-8").length / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
