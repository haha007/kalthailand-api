package th.co.krungthaiaxa.api.elife.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.data.DeductionFileLine;
import th.co.krungthaiaxa.api.elife.exception.PaymentHasNewerCompletedException;
import th.co.krungthaiaxa.api.elife.factory.CollectionFileFactory;
import th.co.krungthaiaxa.api.elife.factory.PaymentFactory;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.factory.RequestFactory;
import th.co.krungthaiaxa.api.elife.mock.LineServiceMockFactory;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.PaymentInformation;
import th.co.krungthaiaxa.api.elife.model.PaymentNewerCompletedResult;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.utils.GreenMailUtil;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PaymentRetryServiceTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(PaymentRetryServiceTest.class);
    @Inject
    private RLSService rlsService;
    private LineService lineService;
    @Inject
    private MongoTemplate mongoTemplate;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Inject
    private QuoteFactory quoteFactory;
    @Inject
    private PolicyFactory policyFactory;

    @Inject
    private PaymentService paymentService;

    @Inject
    private PaymentRepository paymentRepository;

    private static Policy POLICY;
    private static CollectionFile COLLECTION_FILE;
    private static String PAYMENT_ID_01_FAIL;
    private static Payment PAYMENT_02_RETRY;

    @Test
    public void test01_payment_fail_should_has_result_in_collection() throws FolderException {
        PolicyWithFirstFailPayment policyWithFirstFailPayment = testCreatePolicyAndTheFirstFailPayment();
        POLICY = policyWithFirstFailPayment.policy;
        COLLECTION_FILE = policyWithFirstFailPayment.collectionFile;
        PAYMENT_ID_01_FAIL = policyWithFirstFailPayment.paymentId;
    }

    private PolicyWithFirstFailPayment testCreatePolicyAndTheFirstFailPayment() throws FolderException {
        PolicyWithFirstFailPayment result = new PolicyWithFirstFailPayment();
        mongoTemplate.dropCollection(CollectionFile.class);

        Policy policy = policyFactory.createPolicyForLineWithValidated(30, "khoi.tran.ags@gmail.com");
        String lineResponseCode = "4000";
        setupLineServiceWithResponseCode(lineResponseCode);

        InputStream inputStream = CollectionFileFactory.constructCollectionExcelFile(policy.getPolicyId());
        rlsService.importCollectionFile(inputStream);
        List<CollectionFile> collectionFileList = rlsService.processLatestCollectionFiles();
        CollectionFile collectionFile = collectionFileList.get(0);
        String paymentId01Fail = getPaymentIdFromFirstLineOfCollectionFile(collectionFile);

        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, "test/emails");
        Assert.assertTrue(greenMail.getReceivedMessages().length > 0);
        greenMail.purgeEmailFromAllMailboxes();

        //Assert
        DeductionFileLine deductionFileLine = getDeductionFileLineByPolicyNumber(collectionFile, policy.getPolicyId());
        Assert.assertEquals(lineResponseCode, deductionFileLine.getRejectionCode());
        Assert.assertEquals(PaymentFailEmailService.RESPONSE_CODE_EMAIL_SENT_SUCCESS, deductionFileLine.getInformCustomerCode());

        result.collectionFile = collectionFile;
        result.paymentId = paymentId01Fail;
        result.policy = policy;
        return result;
    }

    private static class PolicyWithFirstFailPayment {
        private Policy policy;
        private CollectionFile collectionFile;
        private String paymentId;
    }

    @Test
    public void test02_validate_no_newer_retrypayment() throws FolderException {
        if (PAYMENT_ID_01_FAIL == null) {
            test01_payment_fail_should_has_result_in_collection();
        }
        PaymentNewerCompletedResult paymentNewerCompletedResult = paymentService.findNewerCompletedPaymentInSamePolicy(PAYMENT_ID_01_FAIL);
        Payment failPayment = paymentNewerCompletedResult.getPayment();
        Assert.assertEquals(PAYMENT_ID_01_FAIL, failPayment.getPaymentId());
        Assert.assertNull(paymentNewerCompletedResult.getNewerCompletedPayment());
    }

    @Test
    public void test03_retry_payment_success_after_the_first_fail() throws FolderException {
        if (POLICY == null || COLLECTION_FILE == null) {
            test01_payment_fail_should_has_result_in_collection();
        }
        //Retry the fail payment:
        setupLineServiceWithResponseCode(LineService.RESPONSE_CODE_SUCCESS);
        RetryPaymentResult retryPaymentResult = retryFailedPaymentInCollection(COLLECTION_FILE, POLICY);
        PAYMENT_02_RETRY = retryPaymentResult.retryPayment;

        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, "test/emails");
        Assert.assertTrue(greenMail.getReceivedMessages().length > 0);
    }

    @Test
    public void test04_validate_has_newer_completed_payment_after_retrying_payment_success() throws FolderException {
        if (PAYMENT_02_RETRY == null) {
            test03_retry_payment_success_after_the_first_fail();
        }
        assertHasNewerCompletedPayment(PAYMENT_ID_01_FAIL);
    }

    private PaymentNewerCompletedResult assertHasNewerCompletedPayment(String paymentId) {
        PaymentNewerCompletedResult paymentNewerCompletedResult = paymentService.findNewerCompletedPaymentInSamePolicy(paymentId);
        Payment failPayment = paymentNewerCompletedResult.getPayment();
        Payment newerCompletedPayment = paymentNewerCompletedResult.getNewerCompletedPayment();
        Assert.assertEquals(paymentId, failPayment.getPaymentId());
        Assert.assertNotNull(paymentNewerCompletedResult.getNewerCompletedPayment());
        Assert.assertNotEquals(paymentId, newerCompletedPayment.getPaymentId());
        Assert.assertEquals(PaymentStatus.COMPLETED, newerCompletedPayment.getStatus());
        return paymentNewerCompletedResult;
    }

    @Test(expected = PaymentHasNewerCompletedException.class)
    public void test05_retry_payment_again_must_be_fail_because_we_retried_success_before() throws FolderException {
        if (PAYMENT_02_RETRY == null) {
            test03_retry_payment_success_after_the_first_fail();
        }
        retryFailedPaymentInCollection(COLLECTION_FILE, POLICY);
    }

    @Test
    public void test_B_01_newpayment_without_retry() throws FolderException {
        PolicyWithFirstFailPayment policyWithFirstFailPayment = testCreatePolicyAndTheFirstFailPayment();
        String paymentId01Fail = policyWithFirstFailPayment.paymentId;

        LOGGER.debug("Payment01: " + paymentId01Fail);
        Payment payment02 = saveCompletedStatusForNextPayment(paymentId01Fail);
        LOGGER.debug("Payment02: " + payment02.getPaymentId());
        PaymentNewerCompletedResult paymentNewerCompletedResult = assertHasNewerCompletedPayment(paymentId01Fail);
        Assert.assertEquals(payment02.getPaymentId(), paymentNewerCompletedResult.getNewerCompletedPayment().getPaymentId());

        Payment payment03 = saveCompletedStatusForNextPayment(payment02.getPaymentId());
        LOGGER.debug("Payment03: " + payment03.getPaymentId());
        paymentNewerCompletedResult = assertHasNewerCompletedPayment(paymentId01Fail);
        Assert.assertEquals(payment02.getPaymentId(), paymentNewerCompletedResult.getNewerCompletedPayment().getPaymentId());

        paymentNewerCompletedResult = assertHasNewerCompletedPayment(payment02.getPaymentId());
        Assert.assertEquals(payment03.getPaymentId(), paymentNewerCompletedResult.getNewerCompletedPayment().getPaymentId());

    }

    private Payment saveCompletedStatusForNextPayment(String paymentId) {
        Payment payment = paymentService.validateExistPayment(paymentId);
        Payment newerPayment = paymentRepository.findOneByPolicyAndNewerDueDate(payment.getPolicyId(), payment.getDueDate());
        Assert.assertNotNull(newerPayment);//It should have newer payment because the PAYMENT_ID_01_FAIL is only the first payment.
        newerPayment.setEffectiveDate(DateTimeUtil.nowLocalDateTimeInThaiZoneId());
        newerPayment.setStatus(PaymentStatus.COMPLETED);
        PaymentFactory.generateRandomValuesForPayment(payment);
        return paymentRepository.save(newerPayment);
    }

    private String getPaymentIdFromFirstLineOfCollectionFile(CollectionFile collectionFile) {
        return collectionFile.getDeductionFile().getLines().get(0).getPaymentId();
    }

    private RetryPaymentResult retryFailedPaymentInCollection(CollectionFile collectionFile, Policy policy) {
        String oldPaymentId = getPaymentIdFromFirstLineOfCollectionFile(collectionFile);
        String orderId = PaymentFactory.generateOrderId();
        String newRegKey = PaymentFactory.generateRegKeyId();
        String transId = PaymentFactory.generateTransactionId();
        String accessToken = RequestFactory.generateAccessToken();
        Payment retryPayment = paymentService.retryFailedPayment(policy.getPolicyId(), oldPaymentId, orderId, transId, newRegKey, accessToken);

        Assert.assertEquals(orderId, retryPayment.getOrderId());
        Assert.assertEquals(transId, retryPayment.getTransactionId());
        Assert.assertEquals(newRegKey, retryPayment.getRegistrationKey());
        Assert.assertNotEquals(oldPaymentId, retryPayment.getPaymentId());
        Payment oldPayment = paymentService.findPaymentById(oldPaymentId);
        Assert.assertEquals(oldPayment.getRetryPaymentId(), retryPayment.getPaymentId());

        Assert.assertTrue(retryPayment.getPaymentInformations().size() > 0);
        for (PaymentInformation paymentInformation : retryPayment.getPaymentInformations()) {
            Assert.assertNotNull(paymentInformation);
        }
        return new RetryPaymentResult(oldPayment, retryPayment);
    }

    private static class RetryPaymentResult {
        private final Payment oldPayment;
        private final Payment retryPayment;

        private RetryPaymentResult(Payment oldPayment, Payment retryPayment) {
            this.oldPayment = oldPayment;
            this.retryPayment = retryPayment;
        }
    }

    private void setupLineServiceWithResponseCode(String lineResponseCode) {
        lineService = LineServiceMockFactory.initServiceWithResponseCode(lineResponseCode);
        paymentService.setLineService(lineService);
        rlsService.setLineService(lineService);
    }

    private DeductionFileLine getDeductionFileLineByPolicyNumber(CollectionFile collectionFile, String policyNumber) {
        Optional<DeductionFileLine> deductionFileLineOptional = collectionFile.getDeductionFile().getLines().stream().filter(deductionFileLine -> deductionFileLine.getPolicyNumber().equals(policyNumber)).findAny();
        return deductionFileLineOptional.get();
    }
}
