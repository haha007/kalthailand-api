package th.co.krungthaiaxa.api.elife.test.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;
import static th.co.krungthaiaxa.api.elife.utils.TestUtil.getErrorFromJSon;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class LineResourceTest extends ELifeTest {
    @Value("${local.server.port}")
    private int port;
    private URI base;
    private RestTemplate template;

    @Before
    public void setUp() throws Exception {
        base = new URI("http://localhost:" + port + "/decrypt");
        template = new TestRestTemplate();
    }

    @Test
    public void should_return_a_line_id() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("value", "CmknoozyMpJSi3pzrA6bbpVa8zrx7uBe28zP/lNi//CxTXNlxIPat1xUmBRWTRH3hZT9wkXpV8PJmEP4Gc11v8IseAY88sDud1H4sX63cUku/jPGx9erFweiBSw8l4yEyUAZ7YS+/q6TX43GkWOEXM9JCu9qPCcNm/RB6k0WE+YW6CpFpnW82D8X8aEU3Jz0Q9OLiIrrBVrTQSBzxz+RrC14r18lNDjFsi5Lv5ZefmLjGxgHogU1pCvDwsookbUR1zxNcYmspuQ+5XQcGpLOEbTQbbFXJaT2nAYZKXDAv46z9lZDr1YPRzNQObA9PS3N7nPT7fjg5TxHyL2pYQVkDg==");

        ResponseEntity<String> response = template.getForEntity(builder.toUriString(), String.class);
        //assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        //assertThat(response.getBody()).isEqualTo("{\"mid\":\"u1478b30e2039b6525218600c91fcf0a1\"}");
    }

    @Test
    public void should_return_error_when_token_is_invalid() throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("value", "somethingWrong");

        ResponseEntity<String> response = template.getForEntity(builder.toUriString(), String.class);
        Error error = getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.UNABLE_TO_DECRYPT.getCode());
    }

    @Test
    public void should_get_404_error_when_mid_empty() throws IOException, URISyntaxException {
        URI createURI = new URI("http://localhost:" + port + "/line/bc");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("mid", "");
        ResponseEntity<String> response = template.exchange(createBuilder.toUriString(), GET, null, String.class);
        Error error = JsonUtil.mapper.readValue(response.getBody(), Error.class);
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.UNABLE_TO_GET_LINE_BC.getCode());
    }

    @Test
    public void should_return_error_when_mid_is_unknown() throws IOException, URISyntaxException {
        String sessionId = " u53cb613d9269dd6875f60249402b4542";

        URI createURI = new URI("http://localhost:" + port + "/line/bc");
        UriComponentsBuilder createBuilder = UriComponentsBuilder.fromUri(createURI)
                .queryParam("mid", sessionId);
        ResponseEntity<String> response = template.exchange(createBuilder.toUriString(), GET, null, String.class);
        Error error = getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.UNABLE_TO_GET_LINE_BC.getCode());
    }

}