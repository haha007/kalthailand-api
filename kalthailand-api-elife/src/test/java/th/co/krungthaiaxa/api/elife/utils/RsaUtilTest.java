package th.co.krungthaiaxa.api.elife.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;
import javax.mail.internet.InternetAddress;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Registration;

import org.apache.commons.codec.binary.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class RsaUtilTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(RsaUtilTest.class);

    @Test
    public void should_encryp_text() {
        assertThat(RsaUtil.encrypt("3101202780273").length()).isGreaterThan(13);
    }

    @Test
    public void should_decrypt_text() {
        String originalText = "3101202780273";
        String encodedText = RsaUtil.encrypt(originalText);
        LOGGER.info("Encoded text: \n" + encodedText);
        Assert.assertNotEquals(originalText, encodedText);
        String plainText = RsaUtil.decrypt(encodedText);
        LOGGER.info("Plain text: \n" + plainText);
        Assert.assertEquals(originalText, plainText);
    }

}
