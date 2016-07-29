package th.co.krungthaiaxa.api.common.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

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
}
