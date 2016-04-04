package th.co.krungthaiaxa.elife.api.resource;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.data.SessionQuote;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.repository.QuoteRepository;
import th.co.krungthaiaxa.elife.api.repository.SessionQuoteRepository;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.GenderCode.FEMALE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_10_EC;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IBEGIN;
import static th.co.krungthaiaxa.elife.api.products.ProductType.PRODUCT_IFINE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class QuoteResourceTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);
    @Value("${security.api.user.name}")
    private String apiUserName;
    @Value("${security.api.user.password}")
    private String apiUserPassword;
    @Value("${local.server.port}")
    private int port;
    private RestTemplate template;

    @Inject
    private SessionQuoteRepository sessionQuoteRepository;
    @Inject
    private QuoteRepository quoteRepository;

    @Before
    public void setUp() throws Exception {
        template = new TestRestTemplate(apiUserName, apiUserPassword);
        greenMail.start();
    }

    @After
    public void tearDown() {
        greenMail.stop();
    }

    @Test
    public void should_return_a_10ec_product() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);

        URI createURI = new URI("http://localhost:" + port + "/quotes");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> createResponse = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation(PRODUCT_10_EC)), String.class);
        Quote quote = getQuoteFromJSon(createResponse.getBody());
        assertThat(quote.getPremiumsData().getProduct10ECPremium()).isNotNull();
        assertThat(quote.getPremiumsData().getProductIBeginPremium()).isNull();
        assertThat(quote.getPremiumsData().getProductIFinePremium()).isNull();
    }

    @Test
    public void should_return_a_iBegin_product() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);

        URI createURI = new URI("http://localhost:" + port + "/quotes");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> createResponse = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation(PRODUCT_IBEGIN)), String.class);
        Quote quote = getQuoteFromJSon(createResponse.getBody());
        assertThat(quote.getPremiumsData().getProduct10ECPremium()).isNull();
        assertThat(quote.getPremiumsData().getProductIBeginPremium()).isNotNull();
        assertThat(quote.getPremiumsData().getProductIFinePremium()).isNull();
    }

    @Test
    public void should_return_a_iFine_product() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);

        URI createURI = new URI("http://localhost:" + port + "/quotes");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> createResponse = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation(PRODUCT_IFINE)), String.class);
        Quote quote = getQuoteFromJSon(createResponse.getBody());
        assertThat(quote.getPremiumsData().getProduct10ECPremium()).isNull();
        assertThat(quote.getPremiumsData().getProductIBeginPremium()).isNull();
        assertThat(quote.getPremiumsData().getProductIFinePremium()).isNotNull();
    }

    @Test
    public void should_return_error_when_get_quote_with_wrong_id() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/quotes/123456789");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", randomNumeric(20))
                .queryParam("channelType", LINE.name());

        ResponseEntity<String> response = template.getForEntity(builder.toUriString(), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        Error error = getErrorFromJSon(response.getBody());
        assertThat(error).isEqualToComparingFieldByField(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED);
    }

    @Test
    public void should_return_error_when_get_quote_with_good_id_and_wrong_session_id() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);

        URI createURI = new URI("http://localhost:" + port + "/quotes");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> createResponse = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation()), String.class);
        Quote quote = getQuoteFromJSon(createResponse.getBody());

        URI base = new URI("http://localhost:" + port + "/quotes/" + quote.getQuoteId());
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", randomNumeric(20))
                .queryParam("channelType", LINE.name());

        ResponseEntity<String> response = template.getForEntity(builder.toUriString(), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        Error error = getErrorFromJSon(response.getBody());
        assertThat(error).isEqualToComparingFieldByField(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED);
    }

    @Test
    public void should_return_quote_when_get_quote_with_good_id_and_good_session_id() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);

        URI createURI = new URI("http://localhost:" + port + "/quotes");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> createResponse = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation()), String.class);
        Quote quote = getQuoteFromJSon(createResponse.getBody());

        URI base = new URI("http://localhost:" + port + "/quotes/" + quote.getQuoteId());
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());

        ResponseEntity<String> response = template.getForEntity(builder.toUriString(), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        Quote returnedQuote = getQuoteFromJSon(createResponse.getBody());
        assertThat(returnedQuote).isEqualTo(quote);
    }

    @Test
    public void should_return_empty_last_quote() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/quotes/latest");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", randomNumeric(20))
                .queryParam("channelType", LINE.name());

        ResponseEntity<String> response = template.getForEntity(builder.toUriString(), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(response.getBody()).isEqualTo("\"\"");
    }

    @Test
    public void should_return_last_quote_out_of_3() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);

        URI createURI = new URI("http://localhost:" + port + "/quotes");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation()), String.class);
        template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation()), String.class);
        ResponseEntity<String> createResponse = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation()), String.class);
        Quote quote = getQuoteFromJSon(createResponse.getBody());

        URI latestQuoteURI = new URI("http://localhost:" + port + "/quotes/latest");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(latestQuoteURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());

        ResponseEntity<String> latestResponse = template.getForEntity(builder.toUriString(), String.class);
        assertThat(latestResponse.getStatusCode().value()).isEqualTo(OK.value());
        Quote latestQuote = getQuoteFromJSon(latestResponse.getBody());
        assertThat(latestQuote).isEqualTo(quote);
    }

    @Test
    public void should_always_return_a_new_quote_object_for_the_same_sessionId() throws IOException, URISyntaxException {
        URI createURI = new URI("http://localhost:" + port + "/quotes");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("sessionId", randomNumeric(20))
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> response1 = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation()), String.class);
        assertThat(response1.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote1 = getQuoteFromJSon(response1.getBody());
        assertThat(quote1).isNotNull();

        ResponseEntity<String> response2 = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation()), String.class);
        assertThat(response2.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote2 = getQuoteFromJSon(response2.getBody());
        assertThat(quote2).isNotNull();
        assertThat(quote2).isNotEqualTo(quote1);

        ResponseEntity<String> response3 = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation()), String.class);
        assertThat(response3.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote3 = getQuoteFromJSon(response3.getBody());
        assertThat(quote3).isNotNull();
        assertThat(quote3).isNotEqualTo(quote2);
    }

    @Test
    public void should_return_an_updated_quote() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);
        URI createURI = new URI("http://localhost:" + port + "/quotes");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> creationResponse = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation(35, EVERY_YEAR)), String.class);
        assertThat(creationResponse.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote = getQuoteFromJSon(creationResponse.getBody());
        assertThat(quote).isNotNull();
        assertThat(quote.getId()).isNotNull();

        quote(quote, beneficiary(100.0));
        String jsonQuote = new String(JsonUtil.getJson(quote));

        URI updateURI = new URI("http://localhost:" + port + "/quotes/" + quote.getQuoteId());
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(updateURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> updateResponse = template.exchange(builder.toUriString(), PUT, new HttpEntity<>(jsonQuote), String.class);
        assertThat(updateResponse.getStatusCode().value()).isEqualTo(OK.value());

        // check database values
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, LINE);
        Quote savedQuote = quoteRepository.findOne(quote.getId());
        assertThat(sessionQuote.getQuotes()).containsExactly(savedQuote);
        assertThat(savedQuote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()).isEqualTo(EVERY_YEAR);
        assertThat(savedQuote.getInsureds().get(0).getPerson().getGenderCode()).isEqualTo(FEMALE);

        //check returned JSon
        quote = getQuoteFromJSon(updateResponse.getBody());
        assertThat(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()).isEqualTo(EVERY_YEAR);
        assertThat(quote.getInsureds().get(0).getPerson().getGenderCode()).isEqualTo(FEMALE);
    }

    @Test
    public void should_send_email_api_with_image_as_request_body() throws IOException, URISyntaxException, MessagingException {
        String sessionId = randomNumeric(20);

        URI createURI = new URI("http://localhost:" + port + "/quotes");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> creationResponse = template.exchange(createBuilder.toUriString(), POST, new HttpEntity<>(productQuotation()), String.class);
        assertThat(creationResponse.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote = getQuoteFromJSon(creationResponse.getBody());

        quote(quote, beneficiary(100.0));
        String jsonQuote = new String(JsonUtil.getJson(quote));

        URI updateURI = new URI("http://localhost:" + port + "/quotes/" + quote.getQuoteId());
        UriComponentsBuilder updateBuilder = UriComponentsBuilder.fromUri(updateURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> updateResponse = template.exchange(updateBuilder.toUriString(), PUT, new HttpEntity<>(jsonQuote), String.class);
        assertThat(updateResponse.getStatusCode().value()).isEqualTo(OK.value());

        InputStream inputStream = this.getClass().getResourceAsStream("/graph.jpg");
        String base64Graph = Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream));

        URI emailURI = new URI("http://localhost:" + port + "/quotes/" + quote.getQuoteId() + "/email");
        UriComponentsBuilder emailBuilder = UriComponentsBuilder.fromUri(emailURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", LINE.name());
        ResponseEntity<String> emailResponse = template.exchange(emailBuilder.toUriString(), POST, new HttpEntity<>(base64Graph), String.class);
        assertThat(emailResponse.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        String bodyAsString = decodeSimpleBody(getBody(greenMail.getReceivedMessages()[0]));
        assertThat(bodyAsString).contains("<div>เงินจ่ายคืนตามกรมธรรม์ประกันภัยและเงินครบกำหนดสัญญา (ณ สิ้นปี)</div>");
    }

    private static String decodeSimpleBody(String encodedBody) throws MessagingException, IOException {
        InputStream inputStream = MimeUtility.decode(new ByteArrayInputStream(encodedBody.getBytes("UTF-8")), "quoted-printable");
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] bytes = new byte[encodedBody.length()];
        int last = bufferedInputStream.read(bytes);
        return new String(bytes, 0, last);
    }
}
