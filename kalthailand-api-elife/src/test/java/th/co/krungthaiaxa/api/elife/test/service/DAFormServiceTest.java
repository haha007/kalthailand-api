package th.co.krungthaiaxa.api.elife.test.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

import javax.inject.Inject;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class DAFormServiceTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(DAFormServiceTest.class);
    @Inject
    private th.co.krungthaiaxa.api.elife.service.DAFormService DAFormService;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private PolicyFactory policyFactory;

    public void should_generate_DAForm(Policy policy) {
        byte[] pdfContent = DAFormService.generateDAFormPdf(policy);
        File pdfFile = IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + "/da-form/da-form_" + ProductUtils.validateExistProductName(policy) + DateTimeUtil.formatNowForFilePath() + ".pdf", pdfContent);
        assertThat(pdfFile.exists()).isTrue();
        Assert.assertTrue(pdfFile.length() > 1000);
    }

    @Test
    public void test_generate_DAForm_for_all_kind_of_products() {
        for (ProductType productType : ProductType.values()) {
            LOGGER.debug("Test with productType " + productType);
            if (productType == ProductType.PRODUCT_IBEGIN) {
                continue;
            }
            Policy policy = policyFactory.createPolicyWithPendingPaymentStatus(ProductQuotationFactory.constructDefault(productType));
            should_generate_DAForm(policy);
        }

    }
}
