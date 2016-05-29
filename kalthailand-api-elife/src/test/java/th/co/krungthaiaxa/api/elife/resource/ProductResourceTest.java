package th.co.krungthaiaxa.api.elife.resource;

import org.junit.Before;
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
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class ProductResourceTest extends ELifeTest {
    @Value("${local.server.port}")
    private int port;
    private URI base;
    private RestTemplate template;

    @Before
    public void setUp() throws Exception {
        base = new URI("http://localhost:" + port + "/products/amounts");
        template = new TestRestTemplate();
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
}
