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
import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.exception.QuoteCalculationException;
import th.co.krungthaiaxa.api.elife.factory.CollectionFileFactory;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.AtpMode;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.service.CollectionFileProcessingService;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.utils.DocumentAssertHelper;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class DocumentServiceAllProductsTest extends ELifeTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(DocumentServiceAllProductsTest.class);
    @Inject
    private th.co.krungthaiaxa.api.elife.service.DAFormService DAFormService;
    @Inject
    private PolicyService policyService;
    @Inject
    private DocumentService documentService;
    @Inject
    private DocumentAssertHelper documentAssertHelper;
    @Inject
    private PolicyFactory policyFactory;
    @Inject
    private CollectionFileProcessingService collectionFileProcessingService;

    public void testWith(ProductType productType, PeriodicityCode periodicityCode, AtpMode atpMode) {
        String msg = String.format("DAForm test: Product: %s, Periodicity: %s, ATP: %s.", productType, periodicityCode, atpMode);
        LOGGER.debug(msg);
        try {
            Policy policy = policyFactory.createPolicyWithPendingValidationStatus(ProductQuotationFactory.constructDefault(productType, periodicityCode, atpMode));
            assertDocumentWithPendingValidation(policy, productType, periodicityCode, atpMode);

            policy = policyFactory.updateFromPendingValidationToValidated(policy);
            assertDocumentWithValidatedStatus(policy, productType, periodicityCode, atpMode);

            testCollectionFile(policy, productType, periodicityCode, atpMode);
        } catch (Exception e) {
            if (PeriodicityCode.EVERY_MONTH.equals(periodicityCode) && AtpMode.NO_AUTOPAY.equals(atpMode)) {
                Assert.assertTrue(e instanceof QuoteCalculationException);
            } else {
                LOGGER.error(msg + e.getMessage(), e);
                Assert.assertFalse(true);
            }
        }
    }

    private void testCollectionFile(Policy policy, ProductType productType, PeriodicityCode periodicityCode, AtpMode atpMode) {
        InputStream inputStream = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy);
        if (atpMode != AtpMode.AUTOPAY) {
            assertThatThrownBy(
                    () -> collectionFileProcessingService.importCollectionFile(inputStream))
                    .isInstanceOf(BaseException.class);
        } else {
            CollectionFile collectionFile = collectionFileProcessingService.importCollectionFile(inputStream);
            List<CollectionFile> proccessCollectionFiles = collectionFileProcessingService.processLatestCollectionFiles();
            Assert.assertEquals(proccessCollectionFiles.get(0).getId(), collectionFile.getId());
        }

    }

    private void assertDocumentWithPendingValidation(Policy policy, ProductType productType, PeriodicityCode periodicityCode, AtpMode atpMode) {
        String msg = String.format("DAForm test: Product: %s, Periodicity: %s, ATP: %s.", productType, periodicityCode, atpMode);

        assertMustHaveDAFormWhenAtpModeIsEnabled(policy, productType, periodicityCode, atpMode);
        documentAssertHelper.assertHasDocument(policy, DocumentType.APPLICATION_FORM, msg + " Must contain ApplicationForm (not validated)");
        documentAssertHelper.assertHasNoDocument(policy, DocumentType.APPLICATION_FORM_VALIDATED, msg + " Must not contain ApplicationForm (validated)");
        documentAssertHelper.assertHasNoDocument(policy, DocumentType.ERECEIPT_PDF, msg + " Must not contain eReceiptPdf");
    }

    private void assertDocumentWithValidatedStatus(Policy policy, ProductType productType, PeriodicityCode periodicityCode, AtpMode atpMode) {
        String msg = String.format("DAForm test: Product: %s, Periodicity: %s, ATP: %s.", productType, periodicityCode, atpMode);

        assertMustHaveDAFormWhenAtpModeIsEnabled(policy, productType, periodicityCode, atpMode);
        documentAssertHelper.assertHasDocument(policy, DocumentType.APPLICATION_FORM, msg + " Must contain ApplicationForm (not validated)");
        documentAssertHelper.assertHasDocument(policy, DocumentType.APPLICATION_FORM_VALIDATED, msg + " Must contain ApplicationForm (validated)");
        documentAssertHelper.assertHasDocument(policy, DocumentType.ERECEIPT_PDF, msg + " Must contain eReceiptPdf");
    }

    private void assertMustHaveDAFormWhenAtpModeIsEnabled(Policy policy, ProductType productType, PeriodicityCode periodicityCode, AtpMode atpMode) {
        String msg = String.format("DAForm test: Product: %s, Periodicity: %s, ATP: %s", productType, periodicityCode, atpMode);
        if (atpMode == AtpMode.AUTOPAY) {
            documentAssertHelper.assertHasDocument(policy, DocumentType.DA_FORM, msg + " Must contain DAForm");
        } else {
            documentAssertHelper.assertHasNoDocument(policy, DocumentType.DA_FORM, msg + "Must not contain DAForm");
        }
    }

//    @Test
//    public void test() {
//        testWith(ProductType.PRODUCT_IPROTECT, PeriodicityCode.EVERY_QUARTER, AtpMode.NO_AUTOPAY);
//    }

    @Test
    public void test_generate_documents_for_all_kind_of_products() {

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
