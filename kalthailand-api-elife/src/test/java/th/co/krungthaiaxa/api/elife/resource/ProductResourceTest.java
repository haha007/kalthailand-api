package th.co.krungthaiaxa.api.elife.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.factory.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.factory.RequestFactory;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductAssertUtil;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({ "server.port=0" })
public class ProductResourceTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProductResourceTest.class);

    @Value("${local.server.port}")
    private int port;
    private URI base;
    private RestTemplate template;

    @Before
    public void setUp() throws Exception {
        base = new URI("http://localhost:" + port + "/products/amounts");
        template = new TestRestTemplate();
    }

    private URI buildURI(String path) throws URISyntaxException {
        return new URI("http://localhost:" + port + path);
    }

    private String buildURIString(String path) {
        UriComponentsBuilder builder = null;
        try {
            builder = UriComponentsBuilder.fromUri(buildURI(path));
            return builder.toUriString();
        } catch (URISyntaxException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    @Test
    public void should_return_amounts_and_common_data() throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base);
        ResponseEntity<String> response = template.exchange(builder.toUriString(), PUT, new HttpEntity<>(TestUtil.productQuotation(30, EVERY_MONTH)), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        ProductAmounts productAmounts = TestUtil.getProductAmountsFromJSon(response.getBody());
        assertThat(productAmounts.getCommonData()).isNotNull();
        assertThat(productAmounts.getMinPremium().getValue()).isNotNull();
        assertThat(productAmounts.getMaxPremium().getValue()).isNotNull();
    }

    @Test
    public void should_return_common_data_only() throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base);
        ResponseEntity<String> response = template.exchange(builder.toUriString(), PUT, new HttpEntity<>(TestUtil.productQuotation(30, null)), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        ProductAmounts productAmounts = TestUtil.getProductAmountsFromJSon(response.getBody());
        assertThat(productAmounts.getCommonData()).isNotNull();
        assertThat(productAmounts.getMinPremium()).isNull();
        assertThat(productAmounts.getMaxPremium()).isNull();
    }

    @Test
    public void create_quote_success_iGen() {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGenDefault();
        test_create_quote_success(productQuotation);
    }

    private void test_create_quote_success(ProductQuotation productQuotation) {
        String uri = buildURIString("/quotes") + "?sessionId=" + RequestFactory.generateSession() + "&channelType=" + ChannelType.LINE;
        ResponseEntity<String> response = template.exchange(uri, HttpMethod.POST, new HttpEntity<>(productQuotation), String.class);
        String responseBody = response.getBody();
        Quote quote = ObjectMapperUtil.toObject(JsonUtil.mapper, responseBody, Quote.class);
        ProductAssertUtil.assertCommonDataEnoughDataAfterQuoteCalculation(quote.getCommonData());
        ProductAssertUtil.assertPremiumDataEnoughDataAfterQuoteCalculation(quote.getPremiumsData());
        LOGGER.debug(responseBody);
    }
}
