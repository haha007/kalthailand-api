package th.co.krungthaiaxa.api.elife.test.resource;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.common.utils.RequestUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.AtpMode;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.utils.HttpResponseAssertUtil;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
@IntegrationTest({ "server.port=0" })
public class PolicyResourceTest extends ELifeTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);
    @Value("${local.server.port}")
    private int port;
    private URI base;
    private RestTemplate template;

    @Inject
    private PolicyRepository policyRepository;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PolicyFactory policyFactory;

    @Before
    public void setUp() throws Exception {
        base = new URI("http://localhost:" + port + "/policies");
        template = new TestRestTemplate();
    }

    @Test
    public void success_queried_policies() {
        Policy policy = policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment());
        String urlParameters = createParametersForQueryPolicies(policy);
        String url = baseUrl + "/policies" + urlParameters;
        ResponseEntity<String> responseEntity = template.getForEntity(url, String.class);

        List<Policy> policies = HttpResponseAssertUtil.assertResponsePageClass(objectMapper, responseEntity, Policy.class);
        Assert.assertTrue(!policies.isEmpty());
        for (Policy ipolicy : policies) {
            Assert.assertNotNull(ipolicy.getPolicyId());
            Assert.assertNotNull(ipolicy.getCommonData().getProductId());
            Assert.assertNotNull(ipolicy.getStatus());
            Assert.assertNotNull(ipolicy.getInsureds().get(0).getStartDate());
            Assert.assertNotNull(ipolicy.getInsureds().get(0).getPerson().getLineId());
        }
    }

    @Test
    public void success_export_queried_policies() {
        Policy policy = policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment());
        String urlParameters = createParametersForQueryPolicies(policy);
        String url = baseUrl + "/policies/extract/download" + urlParameters;
        ResponseEntity<byte[]> responseEntity = template.getForEntity(url, byte[].class);
        IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + "/policies/export/" + System.currentTimeMillis() + ".xls", responseEntity.getBody());
    }

    @Test
    public void success_get_one_policy() {
        Policy policy = policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment());
        String url = baseUrl + "/policies/" + policy.getPolicyId();
        ResponseEntity<String> responseEntity = template.getForEntity(url, String.class);
        Policy policyResult = HttpResponseAssertUtil.assertResponseClass(objectMapper, responseEntity, Policy.class);

        Assert.assertNotNull(policyResult.getPolicyId());
        Assert.assertNotNull(policyResult.getCommonData().getProductId());
        Assert.assertNotNull(policyResult.getStatus());
        Assert.assertNotNull(policyResult.getInsureds().get(0).getStartDate());
        Assert.assertNotNull(policyResult.getInsureds().get(0).getPerson().getLineId());
        Assert.assertTrue(!policyResult.getPayments().isEmpty());
    }

    private String createParametersForQueryPolicies(Policy policy) {
        String urlParameters = generateParameters(
                policy.getPolicyId(),
                ProductType.PRODUCT_IGEN,
                LocalDateTime.now(),
                LocalDateTime.now(),
                PolicyStatus.VALIDATED,
                PeriodicityCode.EVERY_MONTH,
                AtpMode.AUTOPAY.getNumValue(),
                null,
                0,
                1);
        return urlParameters;
    }

    private String generateParameters(String policyId, ProductType productType, LocalDateTime fromDate, LocalDateTime toDate, PolicyStatus status, PeriodicityCode periodicityCode, Integer atpModeId, Boolean nonEmptyAgentCode, Integer pageNumber, Integer pageSize) {
        String fromDateString = fromDate != null ? DateTimeFormatter.ISO_DATE_TIME.format(fromDate) : null;
        String toDateString = toDate != null ? DateTimeFormatter.ISO_DATE_TIME.format(toDate) : null;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pageNumber", pageNumber);
        parameters.put("pageSize", pageSize);
        parameters.put("policyId", policyId);
        parameters.put("productType", productType);
        parameters.put("status", status);
        parameters.put("fromDate", fromDateString);
        parameters.put("toDate", toDateString);
        parameters.put("periodicityCode", periodicityCode);
        parameters.put("atpModeId", atpModeId);
        parameters.put("nonEmptyAgentCode", nonEmptyAgentCode);
        return RequestUtil.generateRequestParameters(parameters);
    }

    @Test
    public void should_return_error_when_creating_policy_from_none_existing_quote() throws IOException {
        String jsonQuote = TestUtil.getJSon(TestUtil.quote(TestUtil.product10ECService()));
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", randomNumeric(20))
                .queryParam("channelType", ChannelType.LINE.name());
        ResponseEntity<String> response = template.exchange(builder.toUriString(), POST, new HttpEntity<>(jsonQuote), String.class);

        Error error = TestUtil.getErrorFromJSon(response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
        assertThat(error.getCode()).isEqualTo(ErrorCode.QUOTE_DOES_NOT_EXIST.getCode());
    }

    @Test
    public void should_return_a_policy_object() throws IOException {
        Policy policy = getPolicy();

        // check database values
        Policy savedPolicy = policyRepository.findOne(policy.getId());
        assertThat(savedPolicy.getId()).isNotNull();
        assertThat(savedPolicy.getPolicyId()).isNotNull();
    }

    @Test
    public void should_return_policy_payment_list() throws IOException, URISyntaxException {
        Policy policy = getPolicy();

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/payments");
        ResponseEntity<String> paymentResponse = template.getForEntity(paymentURI, String.class);
        List<Payment> payments = TestUtil.getPaymentsFromJSon(paymentResponse.getBody());
        assertThat(paymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        assertThat(payments).hasSize(6);
    }

    @Test
    public void should_update_payment_transaction_id_and_registration_key_but_not_the_status() throws IOException, URISyntaxException {
        Policy policy = policyFactory.createPolicyWithPendingPaymentStatus(ProductQuotationFactory.constructIGenDefault());

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/update/status/pendingValidation");
        UriComponentsBuilder updatePaymentBuilder = UriComponentsBuilder.fromUri(paymentURI)
                .queryParam("paymentId", policy.getPayments().get(0).getPaymentId())
                .queryParam("orderId", "myOrderId")
                .queryParam("regKey", "myRegistrationKey")
                .queryParam("transactionId", "myTransactionId");
        ResponseEntity<String> updatePaymentResponse = template.exchange(updatePaymentBuilder.toUriString(), PUT, null, String.class);
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(OK.value());

        Policy updatedPolicy = TestUtil.getPolicyFromJSon(updatePaymentResponse.getBody());
        assertThat(updatePaymentResponse.getStatusCode().value()).isEqualTo(OK.value());
        Payment firstPayment = updatedPolicy.getPayments().get(0);
        Assertions.assertThat(firstPayment.getStatus()).isEqualTo(PaymentStatus.NOT_PROCESSED);
        assertThat(firstPayment.getTransactionId()).isEqualTo("myTransactionId");
        assertThat(firstPayment.getRegistrationKey()).isEqualTo("myRegistrationKey");
        Assert.assertEquals("myOrderId", firstPayment.getOrderId());
    }

    @Test
    public void cannot_update_status_policy_to_pendingValidation_when_missing_regKey() throws IOException, URISyntaxException {
        Policy policy = policyFactory.createPolicyWithPendingPaymentStatus(ProductQuotationFactory.constructIProtectDefaultWithMonthlyPayment());

        URI paymentURI = new URI("http://localhost:" + port + "/policies/" + policy.getPolicyId() + "/update/status/pendingValidation");
        UriComponentsBuilder updatePaymentBuilder = UriComponentsBuilder.fromUri(paymentURI)
                .queryParam("paymentId", policy.getPayments().get(0).getPaymentId())
                .queryParam("orderId", "myOrderId")
                .queryParam("regKey", "")
                .queryParam("transactionId", "myTransactionId");
        ResponseEntity<String> updatePaymentResponse = template.exchange(updatePaymentBuilder.toUriString(), PUT, null, String.class);
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, updatePaymentResponse.getStatusCode());

        String responsBody = updatePaymentResponse.getBody();
        Error error = ObjectMapperUtil.toObject(JsonUtil.mapper, responsBody, Error.class);
        Assert.assertEquals(ErrorCode.ERROR_CODE_BAD_ARGUMENT, error.getCode());
        Assert.assertEquals("regKey", error.getDetails());
    }

    private Policy getPolicy() throws IOException {
        String sessionId = randomNumeric(20);

        Quote quote = quoteService.createQuote(sessionId, ChannelType.LINE, TestUtil.productQuotation(35, PeriodicityCode.EVERY_YEAR));
        TestUtil.quote(quote, TestUtil.beneficiary(100.0));
        quote = quoteService.updateProfessionNameAndCheckBlackList(quote, "token");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(base)
                .queryParam("sessionId", sessionId)
                .queryParam("channelType", ChannelType.LINE.name());
        ResponseEntity<String> response = template.exchange(builder.toUriString(), POST, new HttpEntity<>(TestUtil.getJSon(quote)), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(OK.value());
        return TestUtil.getPolicyFromJSon(response.getBody());
    }

}
