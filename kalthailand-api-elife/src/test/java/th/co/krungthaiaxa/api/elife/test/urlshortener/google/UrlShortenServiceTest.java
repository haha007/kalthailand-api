package th.co.krungthaiaxa.api.elife.test.urlshortener.google;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.urlshortener.google.UrlShortenerService;

/**
 * @author khoi.tran on 11/29/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class UrlShortenServiceTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(UrlShortenServiceTest.class);
    @Autowired
    private UrlShortenerService urlShortenerService;

    @Test
    public void test_shorten_line_url() {
        String longUrl = "https://line.me/R/ch/1469556370/elife/th/retrypayment?paymentId=123423-a2322&policyId=abc-xyz" + System.currentTimeMillis();
        String shortUrl = urlShortenerService.getShortUrlIfPossible(longUrl);
        LOGGER.info("Shorten URL: " + shortUrl);
        Assert.assertTrue(StringUtils.isNotBlank(shortUrl));
        Assert.assertTrue(shortUrl.length() < longUrl.length());
        Assert.assertNotEquals(shortUrl, longUrl);
    }

}
