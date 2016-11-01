package th.co.krungthaiaxa.api.elife.service.migration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.TestUtil;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.service.DocumentService;

import java.util.List;

/**
 * @author khoi.tran on 10/31/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EreceiptOldNumberGenerationServiceTest {
    @Autowired
    EreceiptOldNumberGenerationService ereceiptOldNumberGenerationService;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    DocumentService documentService;

    @Test
    public void test_generate_ereceipt() {
        EreceiptOldNumberResult result = ereceiptOldNumberGenerationService.generateEreceiptNumbersByOldPatternForOldPayments();
        List<Payment> payments = paymentRepository.findByReceiptPdfDocumentNotNull();
        for (Payment payment : payments) {
            String ereceiptFullNumberBase36 = payment.getReceiptNumber().getFullNumberBase36();
            Assert.assertNotNull(ereceiptFullNumberBase36);
//            if (payment.getRetried())
        }
        exportEreceiptPdfAndNumber("NewBusiness", result.getNewBusinessPayments());
        exportEreceiptPdfAndNumber("RetryPayment", result.getRetryPayments());

    }

    private void exportEreceiptPdfAndNumber(String subFolder, List<Payment> payments) {
        for (Payment payment : payments) {
            exportEreceiptPdfAndNumber(subFolder, payment);
        }
    }

    private void exportEreceiptPdfAndNumber(String subFolder, Payment payment) {
        String ereceiptPdfDocumentId = payment.getReceiptPdfDocument().getId();
        DocumentDownload documentDownload = documentService.findDocumentDownload(ereceiptPdfDocumentId);
        byte[] pdfContent = documentService.getDocumentDownloadContent(documentDownload);
        IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + "paymentereceipt/" + subFolder + "/" + payment.getReceiptNumber().getFullNumberBase36() + ".pdf", pdfContent);
    }
}
