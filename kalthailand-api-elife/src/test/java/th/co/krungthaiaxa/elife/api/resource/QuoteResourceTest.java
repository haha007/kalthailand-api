package th.co.krungthaiaxa.elife.api.resource;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.data.SessionQuote;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.GenderCode;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.repository.QuoteRepository;
import th.co.krungthaiaxa.elife.api.repository.SessionQuoteRepository;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.util.StringUtils.replace;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.INVALID_PRODUCT_ID_PROVIDED;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.getErrorFromJSon;
import static th.co.krungthaiaxa.elife.api.resource.TestUtil.getQuoteFromJSon;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class QuoteResourceTest {
    @Value("${api.security.user.name}")
    private String apiUserName;
    @Value("${api.security.user.password}")
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
    }

    @Test
    public void should_return_error_when_get_quote_with_wrong_id() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/quotes/123456789");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", randomNumeric(20))
                .queryParam("channelType", ChannelType.LINE.name());

        ResponseEntity<String> response = template.getForEntity(builder.toUriString(), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        Error error = getErrorFromJSon(response.getBody());
        assertThat(error).isEqualToComparingFieldByField(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED);
    }

    @Test
    public void should_return_error_when_get_quote_with_good_id_and_wrong_session_id() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);
        URI createURI = new URI("http://localhost:" + port + "/quotes");
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sessionId", sessionId);
        parameters.add("productId", "10EC");
        parameters.add("channelType", ChannelType.LINE.name());

        ResponseEntity<String> createResponse = template.postForEntity(createURI, parameters, String.class);
        Quote quote = getQuoteFromJSon(createResponse.getBody());

        URI base = new URI("http://localhost:" + port + "/quotes/" + quote.getQuoteId());
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", randomNumeric(20))
                .queryParam("channelType", ChannelType.LINE.name());

        ResponseEntity<String> response = template.getForEntity(builder.toUriString(), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        Error error = getErrorFromJSon(response.getBody());
        assertThat(error).isEqualToComparingFieldByField(QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED);
    }

    @Test
    public void should_return_quote_when_get_quote_with_good_id_and_good_session_id() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);
        URI createURI = new URI("http://localhost:" + port + "/quotes");
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sessionId", sessionId);
        parameters.add("productId", "10EC");
        parameters.add("channelType", ChannelType.LINE.name());

        ResponseEntity<String> createResponse = template.postForEntity(createURI, parameters, String.class);
        Quote quote = getQuoteFromJSon(createResponse.getBody());

        URI base = new URI("http://localhost:" + port + "/quotes/" + quote.getQuoteId());
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", ChannelType.LINE.name());

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
                .queryParam("channelType", ChannelType.LINE.name());

        ResponseEntity<String> response = template.getForEntity(builder.toUriString(), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(response.getBody()).isEqualTo("\"\"");
    }

    @Test
    public void should_return_last_quote() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);
        URI createURI = new URI("http://localhost:" + port + "/quotes");
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sessionId", sessionId);
        parameters.add("productId", "10EC");
        parameters.add("channelType", ChannelType.LINE.name());

        template.postForEntity(createURI, parameters, String.class);
        template.postForEntity(createURI, parameters, String.class);
        ResponseEntity<String> createResponse = template.postForEntity(createURI, parameters, String.class);
        Quote quote = getQuoteFromJSon(createResponse.getBody());

        URI latestQuoteURI = new URI("http://localhost:" + port + "/quotes/latest");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(latestQuoteURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", ChannelType.LINE.name());

        ResponseEntity<String> latestResponse = template.getForEntity(builder.toUriString(), String.class);
        assertThat(latestResponse.getStatusCode().value()).isEqualTo(OK.value());
        Quote latestQuote = getQuoteFromJSon(latestResponse.getBody());
        assertThat(latestQuote).isEqualTo(quote);
    }

    @Test
    public void should_return_error_when_requesting_on_a_product_that_does_not_exist() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/quotes");
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sessionId", randomNumeric(20));
        parameters.add("productId", "Something");
        parameters.add("channelType", ChannelType.LINE.name());

        ResponseEntity<String> response = template.postForEntity(base, parameters, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        Error error = getErrorFromJSon(response.getBody());
        assertThat(error).isEqualToComparingFieldByField(INVALID_PRODUCT_ID_PROVIDED);
    }

    @Test
    public void should_always_return_a_new_quote_object_for_the_same_sessionId() throws IOException, URISyntaxException {
        URI base = new URI("http://localhost:" + port + "/quotes");
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sessionId", randomNumeric(20));
        parameters.add("productId", "10EC");
        parameters.add("channelType", ChannelType.LINE.name());

        ResponseEntity<String> response1 = template.postForEntity(base, parameters, String.class);
        assertThat(response1.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote1 = TestUtil.getQuoteFromJSon(response1.getBody());
        assertThat(quote1).isNotNull();

        ResponseEntity<String> response2 = template.postForEntity(base, parameters, String.class);
        assertThat(response2.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote2 = TestUtil.getQuoteFromJSon(response2.getBody());
        assertThat(quote2).isNotNull();
        assertThat(quote2).isNotEqualTo(quote1);

        ResponseEntity<String> response3 = template.postForEntity(base, parameters, String.class);
        assertThat(response3.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote3 = TestUtil.getQuoteFromJSon(response3.getBody());
        assertThat(quote3).isNotNull();
        assertThat(quote3).isNotEqualTo(quote2);
    }

    @Test
    public void should_return_an_updated_quote() throws IOException, URISyntaxException {
        String sessionId = randomNumeric(20);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sessionId", sessionId);
        parameters.add("productId", "10EC");
        parameters.add("channelType", ChannelType.LINE.name());

        URI creationURI = new URI("http://localhost:" + port + "/quotes");
        ResponseEntity<String> creationResponse = template.postForEntity(creationURI, parameters, String.class);
        assertThat(creationResponse.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote = TestUtil.getQuoteFromJSon(creationResponse.getBody());
        assertThat(quote).isNotNull();

        String id = quote.getId();
        assertThat(id).isNotNull();

        String jsonQuote = new String(JsonUtil.getJson(quote));
        jsonQuote = replace(jsonQuote, "\"periodicity\":{\"code\":null}", "\"periodicity\":{\"code\":\"EVERY_MONTH\"}");
        jsonQuote = replace(jsonQuote, "\"genderCode\":null", "\"genderCode\":\"MALE\"");

        URI updateURI = new URI("http://localhost:" + port + "/quotes/" + quote.getQuoteId());
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(updateURI)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", ChannelType.LINE.name());
        ResponseEntity<String> updateResponse = template.exchange(builder.toUriString(), HttpMethod.PUT, new HttpEntity<>(jsonQuote), String.class);
        assertThat(updateResponse.getStatusCode().value()).isEqualTo(OK.value());

        // check database values
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, ChannelType.LINE);
        Quote savedQuote = quoteRepository.findOne(id);
        assertThat(sessionQuote.getQuotes()).containsExactly(savedQuote);
        Assertions.assertThat(savedQuote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()).isEqualTo(PeriodicityCode.EVERY_MONTH);
        Assertions.assertThat(savedQuote.getInsureds().get(0).getPerson().getGenderCode()).isEqualTo(GenderCode.MALE);

        //check returned JSon
        quote = TestUtil.getQuoteFromJSon(updateResponse.getBody());
        Assertions.assertThat(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()).isEqualTo(PeriodicityCode.EVERY_MONTH);
        Assertions.assertThat(quote.getInsureds().get(0).getPerson().getGenderCode()).isEqualTo(GenderCode.MALE);
    }
}
