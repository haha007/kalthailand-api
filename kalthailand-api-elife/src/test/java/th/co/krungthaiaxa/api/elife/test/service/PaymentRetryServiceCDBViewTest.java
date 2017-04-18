package th.co.krungthaiaxa.api.elife.test.service;

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
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.data.DeductionFileLine;
import th.co.krungthaiaxa.api.elife.factory.CollectionFileFactory;
import th.co.krungthaiaxa.api.elife.factory.LineServiceMockFactory;
import th.co.krungthaiaxa.api.elife.factory.PaymentFactory;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.RequestFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.line.LineService;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.PaymentInformation;
import th.co.krungthaiaxa.api.elife.model.PaymentNewerCompletedResult;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.repository.CollectionFileRepository;
import th.co.krungthaiaxa.api.elife.repository.DocumentRepository;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.repository.QuoteRepository;
import th.co.krungthaiaxa.api.elife.service.CollectionFileImportingService;
import th.co.krungthaiaxa.api.elife.service.CollectionFileProcessingService;
import th.co.krungthaiaxa.api.elife.service.PaymentFailEmailService;
import th.co.krungthaiaxa.api.elife.service.PaymentRetryService;
import th.co.krungthaiaxa.api.elife.service.PaymentService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.utils.GreenMailUtil;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PaymentRetryServiceCDBViewTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(PaymentRetryServiceCDBViewTest.class);
    @Inject
    private CollectionFileProcessingService rlsService;
    @Inject
    private CollectionFileImportingService collectionFileImportingService;
    private LineService lineService;

    @Inject
    private CollectionFileFactory collectionFileFactory;
    @Inject
    private MongoTemplate mongoTemplate;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Inject
    private PolicyFactory policyFactory;

    @Inject
    private PaymentService paymentService;

    @Inject
    private PaymentRetryService paymentRetryService;

    @Inject
    private PaymentRepository paymentRepository;

    @Inject
    private PolicyRepository policyRepository;

    @Inject
    private DocumentRepository documentRepository;

    @Inject
    private QuoteRepository quoteRepository;

    @Inject
    private CollectionFileRepository collectionFileRepository;

    public void testFullFlowForPaymentFailAndRetrySuccess(ProductQuotation productQuotation) throws FolderException {
        //01 -  Create first fail payment.
        PolicyWithFirstFailPayment policyWithFirstFailPayment = testCreatePolicyAndTheFirstFailPayment(productQuotation, TestUtil.TESTING_EMAIL);
        final String oldPolicyId = policyWithFirstFailPayment.policy.getPolicyId();

        /**
         * This step processes the policy with failed payment will be override policyId to CDB policyId
         * To ensure that getting CDB due date worked.
         * Please input the 
         */
        final String cdbPolicyId = "505-8031971";
        if (Objects.isNull(policyRepository.findByPolicyId(cdbPolicyId))) {
            LOGGER.info("GET DueDate from CDB View for policy {}", cdbPolicyId);
            overridePolicyIdForFailedPayment(policyWithFirstFailPayment, cdbPolicyId, oldPolicyId);
        } else {
            LOGGER.info("GET DueDate from MONGO DB for policy {}", oldPolicyId);
        }

        //Assert
        PaymentNewerCompletedResult paymentNewerCompletedResult = paymentService.findNewerCompletedPaymentInSamePolicy(policyWithFirstFailPayment.failPaymentId);
        Payment failPayment = paymentNewerCompletedResult.getPayment();
        Assert.assertEquals(policyWithFirstFailPayment.failPaymentId, failPayment.getPaymentId());
        Assert.assertNull(paymentNewerCompletedResult.getNewerCompletedPayment());

        //03 -  Retry payment
        setupLineServiceWithResponseCode(LineService.RESPONSE_CODE_SUCCESS);
        testRetryFailedPaymentInCollection(policyWithFirstFailPayment.collectionFile, policyWithFirstFailPayment.policy);

        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, TestUtil.PATH_TEST_RESULT + "/emails");
        Assert.assertTrue(greenMail.getReceivedMessages().length > 0);
        //04 - Revert Policy Id
        overridePolicyIdForFailedPayment(policyWithFirstFailPayment, oldPolicyId, cdbPolicyId);
    }

    @Test
    public void test_iGen_full_flow_for_payment_fail_and_then_retry_success() throws FolderException {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment();
        testFullFlowForPaymentFailAndRetrySuccess(productQuotation);
    }

    private void overridePolicyIdForFailedPayment(PolicyWithFirstFailPayment policyWithFirstFailPayment,
                                                  String newPolicyId, String oldPolicyId) {
        //02 - Change policy Id to CDBView policyId
        CollectionFile newCollectionFile = policyWithFirstFailPayment.collectionFile;
        newCollectionFile.getDeductionFile().getLines()
                .stream()
                .forEach(deductionFileLine -> deductionFileLine.setPolicyNumber(newPolicyId));
        collectionFileRepository.save(newCollectionFile);

        Payment newPayment = paymentRepository.findOne(policyWithFirstFailPayment.failPaymentId);
        newPayment.setPolicyId(newPolicyId);
        paymentRepository.save(newPayment);

        Policy newPolicy = policyWithFirstFailPayment.policy;
        newPolicy.setPolicyId(newPolicyId);
        policyRepository.save(newPolicy);

        List<Document> currentListDoc = documentRepository.findByPolicyId(oldPolicyId);
        currentListDoc.stream().forEach(document -> document.setPolicyId(newPolicyId));
        documentRepository.save(currentListDoc);

        Quote currentQuote = quoteRepository.findByQuoteId(newPolicy.getQuoteId());
        currentQuote.setPolicyId(newPolicyId);
        quoteRepository.save(currentQuote);
    }

    private PolicyWithFirstFailPayment testCreatePolicyAndTheFirstFailPayment(
            ProductQuotation productQuotation,
            String insuredEmail) throws FolderException {
        PolicyWithFirstFailPayment result = new PolicyWithFirstFailPayment();
        mongoTemplate.dropCollection(CollectionFile.class);

        Policy policy = policyFactory.createPolicyWithValidatedStatus(productQuotation, insuredEmail);

        String mockLineResponseFailCode = "4000";
        setupLineServiceWithResponseCode(mockLineResponseFailCode);
        InputStream inputStream = collectionFileFactory.constructCollectionExcelFileWithDefaultPayment(policy.getPolicyId());
        collectionFileImportingService.importCollectionFile(inputStream);
        List<CollectionFile> collectionFileList = rlsService.processLatestCollectionFiles();
        CollectionFile collectionFile = collectionFileList.get(0);
        String paymentId01Fail = getPaymentIdFromFirstLineOfCollectionFile(collectionFile);

        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, TestUtil.PATH_TEST_RESULT + "/emails");
        Assert.assertTrue(greenMail.getReceivedMessages().length > 0);
        greenMail.purgeEmailFromAllMailboxes();

        //Assert
        DeductionFileLine deductionFileLine = getDeductionFileLineByPolicyNumber(collectionFile, policy.getPolicyId());
        Assert.assertEquals(mockLineResponseFailCode, deductionFileLine.getRejectionCode());
        Assert.assertTrue(deductionFileLine.getInformCustomerCode().contains(PaymentFailEmailService.RESPONSE_CODE_EMAIL_SENT_SUCCESS));

        result.collectionFile = collectionFile;
        result.failPaymentId = paymentId01Fail;
        result.policy = policy;
        return result;
    }

    private static class PolicyWithFirstFailPayment {
        private Policy policy;
        private CollectionFile collectionFile;
        private String failPaymentId;
    }

    private String getPaymentIdFromFirstLineOfCollectionFile(CollectionFile collectionFile) {
        return collectionFile.getDeductionFile().getLines().get(0).getPaymentId();
    }

    private RetryPaymentResult testRetryFailedPaymentInCollection(CollectionFile collectionFile,
                                                                  Policy policy) {
        String oldPaymentId = getPaymentIdFromFirstLineOfCollectionFile(collectionFile);
        String orderId = PaymentFactory.generateOrderId();
        String newRegKey = PaymentFactory.generatePaymentRegKey();
        String transId = PaymentFactory.generateTransactionId();
        String accessToken = RequestFactory.generateAccessToken();
        Payment retryPayment = paymentRetryService.retryFailedPayment(
                policy.getPolicyId(), oldPaymentId, orderId, transId, newRegKey, accessToken);

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
        paymentRetryService.setLineService(lineService);
        rlsService.setLineService(lineService);
    }

    private DeductionFileLine getDeductionFileLineByPolicyNumber(CollectionFile collectionFile, String policyNumber) {
        Optional<DeductionFileLine> deductionFileLineOptional = collectionFile.getDeductionFile().getLines().stream().filter(deductionFileLine -> deductionFileLine.getPolicyNumber().equals(policyNumber)).findAny();
        return deductionFileLineOptional.get();
    }
}
