package th.co.krungthaiaxa.api.elife.thirdParty.mocab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;

import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpMethod.POST;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;

/**
 * Created by tuong.le on 3/3/17.
 */
@Component
public class MocabClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MocabClient.class);

    private static final String APPLICATION_PDF_MIME_TYPE = "application/pdf";

    @Value("${mocab.webservice.url}")
    private String mocabServiceUrl;

    private RestTemplate restTemplate = new RestTemplate();

    public Optional<MocabResponse> sendPDFToMocab(final Policy policy, final String pdfBase64Encoded, final DocumentType documentType) {
        notNull(pdfBase64Encoded, new ElifeException("Cannot send null document"));
        notNull(documentType, new ElifeException("Cannot send document with no document type"));
        notNull(policy, new ElifeException("Cannot send document on a null policy"));
        notNull(policy.getInsureds().get(0), new ElifeException("Cannot send document with no insured"));
        notNull(policy.getInsureds().get(0).getPerson(), new ElifeException("Cannot send document when insured has no details"));
        notNull(policy.getInsureds().get(0).getPerson().getRegistrations(), new ElifeException("Cannot send document when insured has no ID"));

        final MocabRequest request = new MocabRequest().builder()
                .setContent(pdfBase64Encoded)
                .setCustomerName(policy.getInsureds().get(0).getPerson().getTitle() + " " +
                        policy.getInsureds().get(0).getPerson().getGivenName() + " " +
                        policy.getInsureds().get(0).getPerson().getSurName())
                .setCustomerTel(policy.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber())
                .setDocumentType(documentType.name())
                .setIdCard(policy.getInsureds().get(0).getPerson().getRegistrations().get(0).getId())
                .setMimeType(APPLICATION_PDF_MIME_TYPE)
                .setPolicyNumber(policy.getPolicyId())
                .setPolicyStatus(policy.getStatus());


        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mocabServiceUrl);
        final HttpEntity<MocabRequest> requestWrapper = new HttpEntity<>(request);
        final ResponseEntity<MocabResponse> response =
                restTemplate.exchange(builder.toUriString(), POST, requestWrapper, MocabResponse.class);

        if (HttpStatus.OK.equals(response.getStatusCode()) && !Objects.isNull(response.getBody())) {
            return Optional.of(response.getBody());
        }
        LOGGER.error("Could not send PDF to Mocab policyId {}", policy.getId());
        return Optional.empty();
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
