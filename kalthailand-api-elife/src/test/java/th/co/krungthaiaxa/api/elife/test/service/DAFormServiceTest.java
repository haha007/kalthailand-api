package th.co.krungthaiaxa.api.elife.test.service;

import org.apache.commons.lang3.StringUtils;
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
import th.co.krungthaiaxa.api.elife.utils.TestUtil;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.AtpMode;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

import javax.inject.Inject;

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
    private DocumentService documentService;
    @Inject
    private PolicyFactory policyFactory;

    public void testWith(ProductType productType, PeriodicityCode periodicityCode, AtpMode atpMode) {
        String msg = String.format("DAForm test: Product: %s, Periodicity: %s, ATP: %s", productType, periodicityCode, atpMode);
        LOGGER.debug(msg);
        try {
            Policy policy = policyFactory.createPolicyWithPendingValidationStatus(ProductQuotationFactory.constructDefault(productType, periodicityCode, atpMode));
            assertMustHaveDAFormWhenAtpModeIsEnabled(policy, productType, periodicityCode, atpMode);
        } catch (Exception e) {
            if (PeriodicityCode.EVERY_MONTH.equals(periodicityCode) && AtpMode.NO_AUTOPAY.equals(atpMode)) {
                Assert.assertTrue(e instanceof QuoteCalculationException);
            }
        }
    }

    private void assertMustHaveDAFormWhenAtpModeIsEnabled(Policy policy, ProductType productType, PeriodicityCode periodicityCode, AtpMode atpMode) {
        DocumentDownload documentDownload = documentService.findDocumentDownload(policy, DocumentType.DA_FORM);
        if (atpMode == AtpMode.AUTOPAY) {
            Assert.assertNotNull(documentDownload);
            byte[] pdfContent = documentService.getDocumentDownloadContent(documentDownload);
            IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + "/da-form/da-form_" + ProductUtils.validateExistProductName(policy) + DateTimeUtil.formatNowForFilePath() + ".pdf", pdfContent);
            Assert.assertTrue(StringUtils.isNotBlank(documentDownload.getContent()));
        } else {
            String msg = String.format("DAForm test: Product: %s, Periodicity: %s, ATP: %s", productType, periodicityCode, atpMode);
            Assert.assertNull(msg, documentDownload);
        }
    }

    @Test
    public void test_generate_DAForm_for_all_kind_of_products() {
        for (ProductType productType : ProductType.values()) {
            if (productType == ProductType.PRODUCT_IBEGIN || productType == ProductType.PRODUCT_10_EC) {
                continue;
            }
            for (PeriodicityCode periodicityCode : PeriodicityCode.values()) {
                for (AtpMode atpMode : AtpMode.values()) {
                    testWith(productType, periodicityCode, atpMode);
                }
            }
        }
    }
}
