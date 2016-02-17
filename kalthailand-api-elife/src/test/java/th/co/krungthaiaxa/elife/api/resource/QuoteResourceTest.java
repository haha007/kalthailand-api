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
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.data.SessionQuote;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;
import th.co.krungthaiaxa.elife.api.model.enums.GenderCode;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.model.error.ErrorCode;
import th.co.krungthaiaxa.elife.api.repository.QuoteRepository;
import th.co.krungthaiaxa.elife.api.repository.SessionQuoteRepository;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.StringUtils.replace;

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
    private URI base;
    private RestTemplate template;

    @Inject
    private SessionQuoteRepository sessionQuoteRepository;
    @Inject
    private QuoteRepository quoteRepository;

    @Before
    public void setUp() throws Exception {
        base = new URI("http://localhost:" + port + "/quotes");
        template = new TestRestTemplate(apiUserName, apiUserPassword);
    }

    @Test
    public void should_return_error_when_requesting_on_a_product_that_does_not_exist() throws IOException {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sessionId", randomNumeric(20));
        parameters.add("productId", "Something");
        parameters.add("channelType", ChannelType.LINE.name());

        ResponseEntity<String> response = template.postForEntity(base, parameters, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(error).isEqualToComparingFieldByField(ErrorCode.INVALID_PRODUCT_ID_PROVIDED);
    }

    @Test
    public void should_return_an_empty_quote_object() throws IOException {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sessionId", randomNumeric(20));
        parameters.add("productId", "10EC");
        parameters.add("channelType", ChannelType.LINE.name());

        ResponseEntity<String> response = template.postForEntity(base, parameters, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote = TestUtil.getQuoteFromJSon(response.getBody());
        assertThat(quote).isNotNull();
        assertThat(quote.getPremiumsData()).isNotNull();
        assertThat(quote.getPremiumsData().getFinancialScheduler()).isNotNull();
        assertThat(quote.getPremiumsData().getFinancialScheduler().getPeriodicity()).isNotNull();
        assertThat(quote.getInsureds()).hasSize(1);
        assertThat(quote.getInsureds().get(0)).isNotNull();
        assertThat(quote.getInsureds().get(0).getFatca()).isNotNull();
        assertThat(quote.getInsureds().get(0).getPerson()).isNotNull();
    }

    @Test
    public void should_return_an_updated_quote() throws IOException {
        String sessionId = randomNumeric(20);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sessionId", sessionId);
        parameters.add("productId", "10EC");
        parameters.add("channelType", ChannelType.LINE.name());

        ResponseEntity<String> creationResponse = template.postForEntity(base, parameters, String.class);
        assertThat(creationResponse.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote = TestUtil.getQuoteFromJSon(creationResponse.getBody());
        assertThat(quote).isNotNull();

        String id = quote.getId();
        assertThat(id).isNotNull();

        String jsonQuote = new String(JsonUtil.getJson(quote));
        jsonQuote = replace(jsonQuote, "\"periodicity\":{\"code\":null}", "\"periodicity\":{\"code\":\"EVERY_MONTH\"}");
        jsonQuote = replace(jsonQuote, "\"genderCode\":null", "\"genderCode\":\"MALE\"");

        ResponseEntity<String> updateResponse = template.exchange(base, HttpMethod.PUT, new HttpEntity<>(jsonQuote), String.class);
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
