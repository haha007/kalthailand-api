package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EncryptUtilTest.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EncryptUtilTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptUtilTest.class);

    @Test
    public void should_encrypt_and_decrypt_success() {
        String originalText = "3101202780273";
        String encodedText = EncryptUtil.encrypt(originalText);
        LOGGER.info("Encoded text: \n" + encodedText);
        Assert.assertNotEquals(originalText, encodedText);
        String plainText = EncryptUtil.decrypt(encodedText);
        LOGGER.info("Plain text: \n" + plainText);
        Assert.assertEquals(originalText, plainText);
    }

    @Test
    public void should_decrypt() {
        try {
            String encrypted =
                    "fgQa4KR7J32CkTDI7hcMDZC2CjTHijuPd3zP3KpBiWlqSMR08Di9ypvXzreyUmHUKBHucJc4haYltFFpghRuOdma+lp/Ui5eTGBPGhTn7kQCdcEUjhHZxdRBVWxPQsgFb14YwNFWWKee3mBwGfU1zhJAf/XWlQ9tU2rUSQ/nDulPRq1rgXEuAbMbJ/hc1NPsw0kkqQ2lfpFbcQGAiqezqneHFfxYgwK6/XdA2qHMvB2beIWutt015EUPGuTd02h7E592x1zKqeB/iO19fZf30/re/CGZK5mVJWrCmDd6mKJY+Yeq7mj8/JvNVBR4uJOJHmSPDmyXh5IFx6FIYxCRqA=="
//                    "Ce63NSEBpCGVWlSLIanOJwWTt/E/M6eAMMPgAVtUvjm9cKhil2sqAnFRFGeJSy0wT3sCXGB98u2Qq4wOcbkiTKl9Cgot8gbb6n16z7KVWF+FRq4rzrPHu5ChmMDfA/OLtTmmJKun3LszIqcawMGW46yJcXGWS9umO4UyL1xisHBh/FQBSTMk3DbqVK99QXxwZZpbrCguArMSjArdw2NOjaPTOOVpV/+27emlKMB5IB2okQ+p0MJqISzb9VZeylGENDMXKQQr7G7JS2YSYDLvoyOSPb2IVb8eG47oODkeMoZUf/+MgKcA41eH/Wz4zzvpG01klldgB17olHhi02IE4w=="
//                    "KQRLpgmhYEZr/ueeyLa/TzAHfIzRsQt5uPH3YRBBfqxPT6i57lqSZjaCCdLABr3V6tFQ3pFGYitpO2jXx/EQGbLXHnmnS9DlvTPj8H9gxCEzyOb1w/t5ZLDtIsvpC8CXQsf5kUnc+ntgSkCFBckVhzWnfSIyXnRM5PgVV8pUB8guMAHpl5QcDAYadykPKef8cMfB5EoZ/H/I88lHd22y6X1DywTBU3locnZWhJQ/0svqi+fsslVPIM92wwHlbW0PrqEkaArFaeY3AOyCPwWahZDOihTjG+3TxUyKAhAc7mGr7/O7dNPqWoso+CDDeRBIO8sAK6//lgrnp5T8uqqg=="
                    ;
            byte[] noBase64Bytes = Base64.decodeBase64(encrypted);
            String noBase64 = new String(noBase64Bytes);
            LOGGER.info("No base 64: {}. No base64 length: {}", noBase64, noBase64Bytes.length);
            String plainText = EncryptUtil.decrypt(encrypted);
            LOGGER.info("Plain text: \n" + plainText);
        } catch (Exception ex) {
            LOGGER.info("" + ex.getMessage(), ex);
        }
    }

    @Test
    public void should_decrypt_broken_text() {
        try {
            String encrypted =
//                    "fgQa4KR7J32CkTDI7hcMDZC2CjTHijuPd3zP3KpBiWlqSMR08Di9ypvXzreyUmHUKBHucJc4haYltFFpghRuOdma+lp/Ui5eTGBPGhTn7kQCdcEUjhHZxdRBVWxPQsgFb14YwNFWWKee3mBwGfU1zhJAf/XWlQ9tU2rUSQ/nDulPRq1rgXEuAbMbJ/hc1NPsw0kkqQ2lfpFbcQGAiqezqneHFfxYgwK6/XdA2qHMvB2beIWutt015EUPGuTd02h7E592x1zKqeB/iO19fZf30/re/CGZK5mVJWrCmDd6mKJY+Yeq7mj8/JvNVBR4uJOJHmSPDmyXh5IFx6FIYxCRqA=="
                    "Ce63NSEBpCGVWlSLIanOJwWTt/E/M6eAMMPgAVtUvjm9cKhil2sqAnFRFGeJSy0wT3sCXGB98u2Qq4wOcbkiTKl9Cgot8gbb6n16z7KVWF+FRq4rzrPHu5ChmMDfA/OLtTmmJKun3LszIqcawMGW46yJcXGWS9umO4UyL1xisHBh/FQBSTMk3DbqVK99QXxwZZpbrCguArMSjArdw2NOjaPTOOVpV/+27emlKMB5IB2okQ+p0MJqISzb9VZeylGENDMXKQQr7G7JS2YSYDLvoyOSPb2IVb8eG47oODkeMoZUf/+MgKcA41eH/Wz4zzvpG01klldgB17olHhi02IE4w=="
//                    "KQRLpgmhYEZr/ueeyLa/TzAHfIzRsQt5uPH3YRBBfqxPT6i57lqSZjaCCdLABr3V6tFQ3pFGYitpO2jXx/EQGbLXHnmnS9DlvTPj8H9gxCEzyOb1w/t5ZLDtIsvpC8CXQsf5kUnc+ntgSkCFBckVhzWnfSIyXnRM5PgVV8pUB8guMAHpl5QcDAYadykPKef8cMfB5EoZ/H/I88lHd22y6X1DywTBU3locnZWhJQ/0svqi+fsslVPIM92wwHlbW0PrqEkaArFaeY3AOyCPwWahZDOihTjG+3TxUyKAhAc7mGr7/O7dNPqWoso+CDDeRBIO8sAK6//lgrnp5T8uqqg=="
                    ;
            byte[] noBase64Bytes = Base64.decodeBase64(encrypted);
            byte[] fixedBytes = Arrays.copyOf(noBase64Bytes, 256);
            String fixedEncodeText = new String(Base64.encodeBase64(fixedBytes));

            String plainText = EncryptUtil.decrypt(fixedEncodeText);
//            String noBase64 = new String(noBase64Bytes);
//            LOGGER.info("No base 64: {}. No base64 length: {}", noBase64, noBase64Bytes.length);
//            String plainText = EncryptUtil.decrypt(encrypted);
            LOGGER.info("Plain text: \n" + plainText);
        } catch (Exception ex) {
            LOGGER.info("" + ex.getMessage(), ex);
            Assert.assertFalse(true);
        }
    }

    @Test
    public void should_encrypt() {
        try {
            String plainText = EncryptUtil.encrypt(
                    "RK6C65719XTKZZP");
            LOGGER.info("Plain text: \n" + plainText);
            Assert.assertTrue(true);
        } catch (Exception ex) {
            LOGGER.info("" + ex.getMessage(), ex);
            Assert.assertFalse(true);
        }
    }
}
