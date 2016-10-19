package th.co.krungthaiaxa.api.elife.resource;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({ "server.port=0" })
public abstract class BaseResourceTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(BaseResourceTest.class);
    @Rule
    private final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    private final RestTemplate restTemplate = new TestRestTemplate();

    @Value("http://localhost:${local.server.port}")
    private String baseUrl;


    //GETTER /////////////////////////////////
    public String getBaseUrl() {
        return baseUrl;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public GreenMailRule getGreenMail() {
        return greenMail;
    }
}
