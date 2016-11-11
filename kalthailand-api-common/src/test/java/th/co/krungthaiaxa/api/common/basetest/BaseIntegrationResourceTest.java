package th.co.krungthaiaxa.api.common.basetest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({ "server.port=0" })
public abstract class BaseIntegrationResourceTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(BaseIntegrationResourceTest.class);
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Autowired
    ObjectMapper objectMapper;

    protected final RestTemplate restTemplate = new TestRestTemplate();

    @Value("http://localhost:${local.server.port}")
    protected String baseUrl;

    public <T> void assertResponseClass(ResponseEntity<String> responseEntity, HttpStatus expectHttpStatus, Class<T> expectedResponseClass) {
        String jsonString = responseEntity.getBody();
        if (expectedResponseClass != null && expectedResponseClass != String.class) {
            ObjectMapperUtil.toObject(objectMapper, jsonString, expectedResponseClass);
        }
        Assert.assertEquals(expectHttpStatus, responseEntity.getStatusCode());
    }

    public void assertError(ResponseEntity<String> responseEntity, HttpStatus expectHttpStatus, String expectedErrorCode) {
        String jsonString = responseEntity.getBody();
        if (expectedErrorCode != null) {
            Error error = ObjectMapperUtil.toObject(objectMapper, jsonString, Error.class);
            Assert.assertEquals(expectedErrorCode, error.getCode());
        }
        Assert.assertEquals(expectHttpStatus, responseEntity.getStatusCode());
    }

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