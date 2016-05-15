package th.co.krungthaiaxa.api.auth.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.auth.KALApiAuth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KALApiAuth.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({"server.port=0"})
public class AuthResourceTest {
    @Value("${kal.api.user.1.name}")
    private String userName1;
    @Value("${kal.api.user.1.password}")
    private String userPassword1;
    @Value("${kal.api.user.2.name}")
    private String userName2;
    @Value("${kal.api.user.2.password}")
    private String userPassword2;
    @Value("${local.server.port}")
    private int port;
    @Value("${jwt.header}")
    private String tokenHeader;

    private RestTemplate template;

    @Before
    public void setUp() throws Exception {
        template = new TestRestTemplate();
    }

    @Test
    public void should_get_a_token() throws IOException, URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        URI authURI = new URI("http://localhost:" + port + "/auth");
        UriComponentsBuilder authURIBuilder = UriComponentsBuilder.fromUri(authURI)
                .queryParam("userName", userName1)
                .queryParam("password", userPassword1);
        ResponseEntity<String> authResponse = template.exchange(authURIBuilder.toUriString(), POST, new HttpEntity<>(headers), String.class);

        assertThat(authResponse.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(authResponse.getBody()).isNotNull();
    }

    @Test
    public void should_validate_token_against_role_1() throws IOException, URISyntaxException {
        HttpHeaders authURIHeaders = new HttpHeaders();
        authURIHeaders.add("Content-Type", "application/json");

        URI authURI = new URI("http://localhost:" + port + "/auth");
        UriComponentsBuilder authURIBuilder = UriComponentsBuilder.fromUri(authURI)
                .queryParam("userName", userName1)
                .queryParam("password", userPassword1);
        ResponseEntity<String> authResponse = template.exchange(authURIBuilder.toUriString(), POST, new HttpEntity<>(authURIHeaders), String.class);

        HttpHeaders validateRoleHeaders = new HttpHeaders();
        validateRoleHeaders.add("Content-Type", "application/json");
        validateRoleHeaders.add(tokenHeader, authResponse.getBody());

        URI validateRoleURI = new URI("http://localhost:" + port + "/auth/validate/ROLE1");
        UriComponentsBuilder validateRoleURIBuilder = UriComponentsBuilder.fromUri(validateRoleURI);
        ResponseEntity<String> validateRoleURIResponse = template.exchange(validateRoleURIBuilder.toUriString(), GET, new HttpEntity<>(validateRoleHeaders), String.class);

        assertThat(validateRoleURIResponse.getStatusCode().value()).isEqualTo(OK.value());
    }

    @Test
    public void should_validate_token_against_role_2_and_3() throws IOException, URISyntaxException {
        HttpHeaders authURIHeaders = new HttpHeaders();
        authURIHeaders.add("Content-Type", "application/json");

        URI authURI = new URI("http://localhost:" + port + "/auth");
        UriComponentsBuilder authURIBuilder = UriComponentsBuilder.fromUri(authURI)
                .queryParam("userName", userName2)
                .queryParam("password", userPassword2);
        ResponseEntity<String> authResponse = template.exchange(authURIBuilder.toUriString(), POST, new HttpEntity<>(authURIHeaders), String.class);

        HttpHeaders validateRoleHeaders = new HttpHeaders();
        validateRoleHeaders.add("Content-Type", "application/json");
        validateRoleHeaders.add(tokenHeader, authResponse.getBody());

        URI validateRole2URI = new URI("http://localhost:" + port + "/auth/validate/ROLE2");
        UriComponentsBuilder validateRole2URIBuilder = UriComponentsBuilder.fromUri(validateRole2URI);
        ResponseEntity<String> validateRole2URIResponse = template.exchange(validateRole2URIBuilder.toUriString(), GET, new HttpEntity<>(validateRoleHeaders), String.class);
        URI validateRole3URI = new URI("http://localhost:" + port + "/auth/validate/ROLE3");
        UriComponentsBuilder validateRole3URIBuilder = UriComponentsBuilder.fromUri(validateRole3URI);
        ResponseEntity<String> validateRole3URIResponse = template.exchange(validateRole3URIBuilder.toUriString(), GET, new HttpEntity<>(validateRoleHeaders), String.class);

        assertThat(validateRole2URIResponse.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(validateRole3URIResponse.getStatusCode().value()).isEqualTo(OK.value());
    }

    @Test
    public void should_not_validate_token_against_role_2() throws IOException, URISyntaxException {
        HttpHeaders authURIHeaders = new HttpHeaders();
        authURIHeaders.add("Content-Type", "application/json");

        URI authURI = new URI("http://localhost:" + port + "/auth");
        UriComponentsBuilder authURIBuilder = UriComponentsBuilder.fromUri(authURI)
                .queryParam("userName", userName1)
                .queryParam("password", userPassword1);
        ResponseEntity<String> authResponse = template.exchange(authURIBuilder.toUriString(), POST, new HttpEntity<>(authURIHeaders), String.class);

        HttpHeaders validateRole1Headers = new HttpHeaders();
        validateRole1Headers.add("Content-Type", "application/json");
        validateRole1Headers.add(tokenHeader, authResponse.getBody());

        URI validateRole1URI = new URI("http://localhost:" + port + "/auth/validate/ROLE2");
        UriComponentsBuilder validateRole1URIBuilder = UriComponentsBuilder.fromUri(validateRole1URI);
        ResponseEntity<String> validateRole1URIResponse = template.exchange(validateRole1URIBuilder.toUriString(), GET, new HttpEntity<>(validateRole1Headers), String.class);

        assertThat(validateRole1URIResponse.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
    }

}
