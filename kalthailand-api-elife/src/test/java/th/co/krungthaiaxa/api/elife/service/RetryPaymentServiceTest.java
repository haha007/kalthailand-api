package th.co.krungthaiaxa.api.elife.service;

import com.icegreen.greenmail.junit.GreenMailRule;
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
import th.co.krungthaiaxa.api.elife.factory.CollectionFileFactory;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.mock.LineServiceMockFactory;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.utils.GreenMailUtil;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class RetryPaymentServiceTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(RetryPaymentServiceTest.class);
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

    @Test
    public void test01_retrypayment_success_after_the_first_fail() {
        mongoTemplate.dropCollection(CollectionFile.class);

        POLICY = policyFactory.createPolicyForLineWithValidated(30, "khoi.tran.ags@gmail.com");

        lineService = LineServiceMockFactory.initServiceWithResponseCode("4000");
        paymentService.setLineService(lineService);
        rlsService.setLineService(lineService);

        InputStream inputStream = CollectionFileFactory.initCollectionExcelFile(POLICY.getPolicyId());
        rlsService.importCollectionFile(inputStream);
        List<CollectionFile> collectionFileList = rlsService.processLatestCollectionFiles();
        CollectionFile collectionFile = collectionFileList.get(0);

        GreenMailUtil.writeReceiveMessagesToFiles(greenMail, "test/emails");
        Assert.assertTrue(greenMail.getReceivedMessages().length > 0);

        //Retry the fail payment:
        lineService = LineServiceMockFactory.initServiceDefault();
        paymentService.setLineService(lineService);
        rlsService.setLineService(lineService);

        String paymentId = collectionFile.getDeductionFile().getLines().get(0).getPaymentId();
        String orderId = RandomStringUtils.randomNumeric(10);
        String newRegKey = RandomStringUtils.randomNumeric(15);
        String transId = RandomStringUtils.randomNumeric(20);
        String accessToken = RandomStringUtils.randomAlphanumeric(25);
        paymentService.retryFailedPayment(POLICY.getPolicyId(), paymentId, orderId, transId, newRegKey, accessToken);
    }


}
