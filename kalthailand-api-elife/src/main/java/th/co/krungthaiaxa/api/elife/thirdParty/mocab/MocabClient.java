package th.co.krungthaiaxa.api.elife.thirdParty.mocab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
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

        disableSslVerification();
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mocabServiceUrl);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        final HttpEntity<String> requestWrapper =
                new HttpEntity<>(new String(JsonUtil.getJson(request)), headers);
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

    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
