package th.co.krungthaiaxa.api.elife.test.resource;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.io.FileUtils;
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
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.policyPremiumNotification.model.PolicyPremiumNoticeRequest;
import th.co.krungthaiaxa.api.elife.policyPremiumNotification.model.PolicyPremiumNoticeSMSRequest;
import th.co.krungthaiaxa.api.elife.test.utils.GreenMailUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;

@RunWith(SpringJUnit4ClassRunner.class)
@IntegrationTest({ "server.port=0" })
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
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
        URI paymentURI = new URI(baseUrl + "/policies/" + policyNumber + "/premium/cdb");
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(paymentURI).queryParam("insuredDob", LocalDate.now());
        ResponseEntity<String> responseEntity = template.exchange(uriComponentsBuilder.toUriString(), HttpMethod.GET, null, String.class);
        LOGGER.debug(responseEntity.getBody());
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void can_send_premium_notice_email() throws IOException, URISyntaxException {
        String policyNumber = "502-0123456";
        String urlString = baseUrl + "/policies/" + policyNumber + "/premium/email";
        PolicyPremiumNoticeRequest policyPremiumNoticeRequest = new PolicyPremiumNoticeRequest();
        policyPremiumNoticeRequest.setInsuredDob(LocalDate.now().minusYears(10));

        ResponseEntity<String> responseEntity = template.postForEntity(urlString, policyPremiumNoticeRequest, String.class);

        LOGGER.debug("Response: " + responseEntity.getBody());
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, TestUtil.PATH_TEST_RESULT + "/emails");
    }

    @Test
    public void can_send_premium_notice_sms() throws IOException, URISyntaxException {
        String policyNumber = "502-0123456";
        String urlString = baseUrl + "/policies/" + policyNumber + "/premium/sms";

        PolicyPremiumNoticeSMSRequest policyPremiumNoticeRequest = new PolicyPremiumNoticeSMSRequest();
        policyPremiumNoticeRequest.setInsuredDob(LocalDate.now().minusYears(10));
        policyPremiumNoticeRequest.setCompanyCode("MOCK_COMPANY_CODE");

        ResponseEntity<String> responseEntity = template.postForEntity(urlString, policyPremiumNoticeRequest, String.class);

        LOGGER.debug("Response: " + responseEntity.getBody());
//        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void can_send_premium_notice_pdf() throws IOException, URISyntaxException {
        String policyNumber = "502-0123456";
        String urlString = baseUrl + "/policies/" + policyNumber + "/premium/pdf";

        PolicyPremiumNoticeRequest policyPremiumNoticeRequest = new PolicyPremiumNoticeRequest();
        policyPremiumNoticeRequest.setInsuredDob(LocalDate.now().minusYears(10));

        ResponseEntity<byte[]> responseEntity = template.postForEntity(urlString, policyPremiumNoticeRequest, byte[].class);

        //Write result to pdf file
        String resultFilePath = TestUtil.PATH_TEST_RESULT + "pdf/policy-premium-notice_" + System.currentTimeMillis() + ".pdf";
        FileUtils.writeByteArrayToFile(new File(resultFilePath), responseEntity.getBody());

        //Assert
        int fileSize = (responseEntity.getBody().length / 1204);
        LOGGER.debug("Response: " + fileSize + " KB");
        Assert.assertTrue(fileSize > 0);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
