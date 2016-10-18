package th.co.krungthaiaxa.api.elife.resource;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({ "server.port=0" })
public class PolicyCDBResourceTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(PolicyCDBResourceTest.class);
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);
    @Value("${local.server.port}")
    private int port;
    @Value("http://localhost:${local.server.port}")
    private String baseUrl;
    private URI base;
    private RestTemplate template;

    @Before
    public void setUp() throws Exception {
        base = new URI("http://localhost:" + port + "/policies");
        template = new TestRestTemplate();
    }

    @Test
    public void can_get_policy_cdb() throws IOException, URISyntaxException {
        String policyNumber = "502-0123456";
        URI paymentURI = new URI(baseUrl + "/policies/" + policyNumber + "/cdb");
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(paymentURI)
                .queryParam("insuredDob", LocalDate.now())
                .queryParam("orderId", "myOrderId")
                .queryParam("registrationKey", "myRegistrationKey")
                .queryParam("transactionId", "myTransactionId");
        ResponseEntity<String> responseEntity = template.exchange(uriComponentsBuilder.toUriString(), HttpMethod.GET, null, String.class);
        LOGGER.debug(responseEntity.getBody());
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
