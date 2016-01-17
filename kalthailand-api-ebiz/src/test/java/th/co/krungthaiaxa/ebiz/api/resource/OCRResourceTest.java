package th.co.krungthaiaxa.ebiz.api.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.ebiz.api.KalApiApplication;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("dev")
@IntegrationTest({"server.port=0"})
public class OCRResourceTest {
	@Value("${local.server.port}")
	private int port;
	private URI base;
	private RestTemplate template;

    @Value("${path.store.watermarked.image}")
    private String storePath;

	@Before
	public void setUp() throws Exception {
		this.base = new URI("http://localhost:" + port + "/validate/id");
		template = new TestRestTemplate();
	}

	@Test
	public void should_return_error_when_no_parameters() throws IOException {
		ResponseEntity<String> response = template.postForEntity(base, null, String.class);
		assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
	}

	@Test
	public void should_return_error_when_no_image_sent() throws IOException {
		ResponseEntity<String> response = template.postForEntity(base, null, String.class);
		assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
	}

    @Test
    public void should_return_true_when_text_should_be_found_in_image() throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("image", new ClassPathResource("/images/thaiTextWithNumber.png"));
        map.add("type", "png");
        map.add("id", "1 2345 67890 12 3");

        ResponseEntity<String> response = template.postForEntity(base, map, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(response.getBody()).isEqualTo("true");
    }

    @Test
    public void should_return_false_when_text_should_be_found_in_image() throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("image", new ClassPathResource("/images/thaiTextWithNumber.png"));
        map.add("type", "png");
        map.add("id", "SomethingSomething");

        ResponseEntity<String> response = template.postForEntity(base, map, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(response.getBody()).isEqualTo("false");
    }
}
