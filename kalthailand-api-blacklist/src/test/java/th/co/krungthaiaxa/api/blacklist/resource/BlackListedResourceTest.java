package th.co.krungthaiaxa.api.blacklist.resource;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.blacklist.KalApiBlacklist;
import th.co.krungthaiaxa.api.blacklist.filter.KalApiTokenFilter;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiBlacklist.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class BlackListedResourceTest {
    @Value("${kal.api.auth.header}")
    private String tokenHeader;
    @Value("${local.server.port}")
    private int port;
    @Inject
    private KalApiTokenFilter kalApiTokenFilter;
    private TestRestTemplate restTemplate;
    private RestTemplate fakeAuthRestTemplate;

    @Before
    public void setup() {
        restTemplate = new TestRestTemplate();
        fakeAuthRestTemplate = mock(RestTemplate.class);
        kalApiTokenFilter.setTemplate(fakeAuthRestTemplate);
    }

    @Test
    public void should_return_error_when_no_document_received() throws IOException, URISyntaxException {
        when(fakeAuthRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(getAuthResponseOk());

        HttpEntity entity = new HttpEntity<>(null, getHeadersWithFakeToken());

        URI base = new URI("http://localhost:" + port + "/blacklist/isblacklist");
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(base)
                .queryParam("thaiId", "something");
        ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(), GET, entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
    }

    private HttpHeaders getHeadersWithFakeToken() throws URISyntaxException {
        HttpHeaders result = new HttpHeaders();
        result.add("Content-Type", "application/json");
        result.add(tokenHeader, "1234567890");

        return result;
    }

    private ResponseEntity<String> getAuthResponseOk() {
        return new ResponseEntity<>("", HttpStatus.OK);
    }
}
