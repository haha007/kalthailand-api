package th.co.krungthaiaxa.ebiz.api.resource;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.ebiz.api.KalApiApplication;
import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.model.SessionQuote;
import th.co.krungthaiaxa.ebiz.api.model.enums.ChannelType;
import th.co.krungthaiaxa.ebiz.api.model.enums.GenderCode;
import th.co.krungthaiaxa.ebiz.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.ebiz.api.repository.QuoteRepository;
import th.co.krungthaiaxa.ebiz.api.repository.SessionQuoteRepository;
import th.co.krungthaiaxa.ebiz.api.utils.JsonUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.StringUtils.replace;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("dev")
@IntegrationTest({"server.port=0"})
public class QuoteResourceTest {
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
		template = new TestRestTemplate();
	}

    @Test
    public void should_return_an_empty_quote_object() throws IOException {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sessionId", RandomStringUtils.randomNumeric(20));
        parameters.add("channelType", ChannelType.LINE.name());

        ResponseEntity<String> response = template.postForEntity(base, parameters, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote = ResourceTestUtil.getQuoteFromJSon(response.getBody());
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
        String sessionId = RandomStringUtils.randomNumeric(20);
        MultiValueMap<String, String> creationParameters = new LinkedMultiValueMap<>();
        creationParameters.add("sessionId", sessionId);
        creationParameters.add("channelType", ChannelType.LINE.name());

        ResponseEntity<String> creationResponse = template.postForEntity(base, creationParameters, String.class);
        assertThat(creationResponse.getStatusCode().value()).isEqualTo(OK.value());
        Quote quote = ResourceTestUtil.getQuoteFromJSon(creationResponse.getBody());
        assertThat(quote).isNotNull();

        String technicalQuoteId = quote.getTechnicalId();
        assertThat(technicalQuoteId).isNotNull();

        String jsonQuote = new String(JsonUtil.getJson(quote));
        jsonQuote = replace(jsonQuote, "\"periodicity\":{\"code\":null}", "\"periodicity\":{\"code\":\"EVERY_MONTH\"}");
        jsonQuote = replace(jsonQuote, "\"genderCode\":null", "\"genderCode\":\"MALE\"");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(base.toString())
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", ChannelType.LINE.name())
                .queryParam("jsonQuote", jsonQuote);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> updateResponse = template.exchange(builder.build().encode().toUri(), HttpMethod.PUT, entity, String.class);
        assertThat(updateResponse.getStatusCode().value()).isEqualTo(OK.value());

        // check database values
        SessionQuote sessionQuote = sessionQuoteRepository.findBySessionIdAndChannelType(sessionId, ChannelType.LINE);
        assertThat(sessionQuote.getQuoteTechId()).isEqualTo(technicalQuoteId);
        Quote savedQuote = quoteRepository.findOne(technicalQuoteId);
        assertThat(savedQuote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()).isEqualTo(PeriodicityCode.EVERY_MONTH);
        assertThat(savedQuote.getInsureds().get(0).getPerson().getGenderCode()).isEqualTo(GenderCode.MALE);

        //check returned JSon
        quote = ResourceTestUtil.getQuoteFromJSon(updateResponse.getBody());
        assertThat(quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode()).isEqualTo(PeriodicityCode.EVERY_MONTH);
        assertThat(quote.getInsureds().get(0).getPerson().getGenderCode()).isEqualTo(GenderCode.MALE);
    }
}
