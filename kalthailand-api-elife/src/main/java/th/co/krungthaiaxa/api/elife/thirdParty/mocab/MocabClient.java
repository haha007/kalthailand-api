package th.co.krungthaiaxa.api.elife.thirdParty.mocab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.common.utils.EncryptUtil;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.utils.SSLUtils;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;

/**
 * @author tuong.le on 3/3/17.
 */
@Component
public class MocabClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MocabClient.class);

    private static final String APPLICATION_PDF_MIME_TYPE = "application/pdf";

    @Value("${mocab.webservice.url}")
    private String mocabServiceUrl;

    @Value("${mocab.webservice.update-policy-status}")
    private String mocabServiceUpdatePolicyStatusUrl;

    private RestTemplate restTemplate = new RestTemplate();

    public Optional<MocabResponse> sendPdfToMocab(final Policy policy,
                                                  final String pdfBase64Encoded,
                                                  final DocumentType documentType)
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        notNull(pdfBase64Encoded, new ElifeException("Cannot send null document"));
        notNull(documentType, new ElifeException("Cannot send document with no document type"));
        notNull(policy, new ElifeException("Cannot send document on a null policy"));
        notNull(policy.getInsureds().get(0), new ElifeException("Cannot send document with no insured"));
        notNull(policy.getInsureds().get(0).getPerson(), new ElifeException("Cannot send document when insured has no details"));
        notNull(policy.getInsureds().get(0).getPerson().getRegistrations(), new ElifeException("Cannot send document when insured has no ID"));

        String productLogicName = policy.getCommonData().getProductId();
        ProductType productType = ProductUtils.validateExistProductTypeByLogicName(productLogicName);
        final MocabRequest request = new MocabRequest();
        request.setContent(pdfBase64Encoded);
        request.setCustomerName(policy.getInsureds().get(0).getPerson().getTitle() + " " +
                policy.getInsureds().get(0).getPerson().getGivenName() + " " +
                policy.getInsureds().get(0).getPerson().getSurName());
        request.setCustomerTel(policy.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber());
        request.setDocumentType(documentType.name());
        request.setIdCard(policy.getInsureds().get(0).getPerson().getRegistrations().get(0).getId());
        request.setMimeType(APPLICATION_PDF_MIME_TYPE);
        request.setPolicyNumber(policy.getPolicyId());
        request.setPolicyStatus(policy.getStatus());
        request.setProductType(productType.name());
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        final HttpEntity<String> requestWrapper =
                new HttpEntity<>(new String(JsonUtil.getJson(request)), headers);

        return requestClient(mocabServiceUrl, requestWrapper, POST);
    }

    public Optional<MocabResponse> updatePolicyStatusMocab(final Policy policy, final String applicationFormValidatedId) {
        notNull(policy, new ElifeException("Cannot update policy status in Mocab without policy"));
        notNull(policy.getStatus(), new ElifeException("Policy status is invalid"));

        final UpdatePolicyStatusRequest request = new UpdatePolicyStatusRequest();
        request.setPolicyStatus(policy.getStatus());

        if (policy.getStatus().equals(PolicyStatus.VALIDATED)) {
            notNull(applicationFormValidatedId,
                    new ElifeException("ApplicationFormValidatedId is required for VALIDATED policy"));
            request.setKeySign(EncryptUtil.encrypt(applicationFormValidatedId));
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        final HttpEntity<String> requestWrapper =
                new HttpEntity<>(new String(JsonUtil.getJson(request)), headers);

        return requestClient(mocabServiceUpdatePolicyStatusUrl + "/" + policy.getPolicyId(), requestWrapper, PUT);

    }


    private Optional<MocabResponse> requestClient(final String url,
                                                  final HttpEntity<String> request,
                                                  final HttpMethod method) {
        try {
            SSLUtils.disableSslVerification();
            final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            final ResponseEntity<MocabResponse> response =
                    restTemplate.exchange(builder.toUriString(), PUT, request, MocabResponse.class);
            if (HttpStatus.OK.equals(response.getStatusCode()) && !Objects.isNull(response.getBody())) {
                return Optional.of(response.getBody());
            }
        } catch (ResourceAccessException exception) {
            LOGGER.error("Could not Handle the mocab response exception: ", exception);
        }
        return Optional.empty();

    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
