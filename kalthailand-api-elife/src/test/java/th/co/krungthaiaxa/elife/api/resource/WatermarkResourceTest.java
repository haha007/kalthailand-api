package th.co.krungthaiaxa.elife.api.resource;

import org.apache.commons.io.IOUtils;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.model.error.Error;
import th.co.krungthaiaxa.elife.api.model.error.ErrorCode;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("dev")
@IntegrationTest({"server.port=0"})
public class WatermarkResourceTest {
    @Value("${api.security.user.name}")
    private String apiUserName;
    @Value("${api.security.user.password}")
    private String apiUserPassword;
    @Value("${local.server.port}")
    private int port;
    private URI base;
    private RestTemplate template;

    @Value("${path.store.watermarked.image}")
    private String storePath;

    @Before
    public void setUp() throws Exception {
        this.base = new URI("http://localhost:" + port + "/watermark/upload");
        template = new TestRestTemplate(apiUserName, apiUserPassword);
    }

    @Test
    public void should_return_error_when_no_parameters() throws IOException {
        ResponseEntity<String> response = template.postForEntity(base, null, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
    }

    @Test
    public void should_return_error_when_base64image_is_not_base64() throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("base64Image", "something");
        map.add("type", "png");

        ResponseEntity<String> response = template.postForEntity(base, map, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(error.getCode()).isEqualTo(ErrorCode.WATERMARK_IMAGE_INPUT_NOT_READABLE.getCode());
    }

    @Test
    public void should_return_error_when_image_is_too_small() throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("base64Image", getBase64("/images/small.png"));
        map.add("type", "png");

        ResponseEntity<String> response = template.postForEntity(base, map, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_ACCEPTABLE.value());
    }

    @Test
    public void should_return_error_when_sending_a_file_that_is_not_an_image() throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("base64Image", getBase64("/texts/sampleTextFile.txt"));
        map.add("type", "png");

        ResponseEntity<String> response = template.postForEntity(base, map, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(UNSUPPORTED_MEDIA_TYPE.value());
    }

    @Test
    public void should_return_ok_and_create_file_in_store_path() throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("base64Image", getBase64("/images/big.png"));
        map.add("type", "png");

        ResponseEntity<String> response = template.postForEntity(base, map, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(response.getBody()).isNotNull();
        assertThat(Base64.getDecoder().decode(response.getBody())).isNotNull();
    }

    private String getBase64(String file) {
        try {
            byte[] content = IOUtils.toByteArray(this.getClass().getResourceAsStream(file));
            return new String(Base64.getEncoder().encode(content));
        } catch (IOException e) {
            return null;
        }
    }
}
