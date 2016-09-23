package th.co.krungthaiaxa.api.elife.service;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.lang3.RandomStringUtils;
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
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.data.DeductionFileLine;
import th.co.krungthaiaxa.api.elife.exception.PaymentHasNewerCompletedException;
import th.co.krungthaiaxa.api.elife.factory.CollectionFileFactory;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.mock.LineServiceMockFactory;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.PaymentInformation;
import th.co.krungthaiaxa.api.elife.model.Policy;
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

    private static Policy POLICY;
    private static CollectionFile COLLECTION_FILE;
    private static Payment RETRY_PAYMENT;

    @Test
    public void test01_payment_fail_should_has_result_in_collection() throws FolderException {
        mongoTemplate.dropCollection(CollectionFile.class);

        POLICY = policyFactory.createPolicyForLineWithValidated(30, "khoi.tran.ags@gmail.com");
        String lineResponseCode = "4000";
        setupLineServiceWithResponseCode(lineResponseCode);

        InputStream inputStream = CollectionFileFactory.initCollectionExcelFile(POLICY.getPolicyId());
        rlsService.importCollectionFile(inputStream);
        List<CollectionFile> collectionFileList = rlsService.processLatestCollectionFiles();
        COLLECTION_FILE = collectionFileList.get(0);

        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, "test/emails");
        Assert.assertTrue(greenMail.getReceivedMessages().length > 0);
        greenMail.purgeEmailFromAllMailboxes();

        //Assert
        DeductionFileLine deductionFileLine = getDeductionFileLineByPolicyNumber(COLLECTION_FILE, POLICY.getPolicyId());
        Assert.assertEquals(lineResponseCode, deductionFileLine.getRejectionCode());
        Assert.assertEquals(PaymentFailEmailService.RESPONSE_CODE_EMAIL_SENT_SUCCESS, deductionFileLine.getInformCustomerCode());

    }

    @Test
    public void test02_retry_payment_success_after_the_first_fail() throws FolderException {
        if (POLICY == null || COLLECTION_FILE == null) {
            test01_payment_fail_should_has_result_in_collection();
        }
        //Retry the fail payment:
        setupLineServiceWithResponseCode(LineService.RESPONSE_CODE_SUCCESS);
        RetryPaymentResult retryPaymentResult = retryFailedPaymentInCollection(COLLECTION_FILE, POLICY);
        RETRY_PAYMENT = retryPaymentResult.retryPayment;

        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, "test/emails");
        Assert.assertTrue(greenMail.getReceivedMessages().length > 0);
    }

    @Test(expected = PaymentHasNewerCompletedException.class)
    public void test03_retry_payment_again_the_second_time() throws FolderException {
        if (RETRY_PAYMENT == null) {
            test02_retry_payment_success_after_the_first_fail();
        }
        retryFailedPaymentInCollection(COLLECTION_FILE, POLICY);
    }

    private RetryPaymentResult retryFailedPaymentInCollection(CollectionFile collectionFile, Policy policy) {
        String oldPaymentId = collectionFile.getDeductionFile().getLines().get(0).getPaymentId();
        String orderId = RandomStringUtils.randomNumeric(10);
        String newRegKey = RandomStringUtils.randomNumeric(15);
        String transId = RandomStringUtils.randomNumeric(20);
        String accessToken = RandomStringUtils.randomAlphanumeric(25);
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
