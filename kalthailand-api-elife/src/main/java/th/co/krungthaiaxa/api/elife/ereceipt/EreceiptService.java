package th.co.krungthaiaxa.api.elife.ereceipt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.log.LogUtil;
import th.co.krungthaiaxa.api.elife.exception.EreceiptDocumentException;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentReferenceType;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.service.DocumentService;
import th.co.krungthaiaxa.api.elife.service.SigningDocumentService;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static th.co.krungthaiaxa.api.elife.client.SigningClient.PASSWORD_DOB_PATTERN;

/**
 * @author khoi.tran on 10/25/16.
 */
@Service
public class EreceiptService {
    public final static Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentService documentService;
    private final PaymentRepository paymentRepository;
    private final SigningDocumentService signingDocumentService;
    private final EreceiptPdfService ereceiptPdfService;
    private final EreceiptIncrementalService ereceiptIncrementalService;

    @Autowired
    public EreceiptService(DocumentService documentService,
                           PaymentRepository paymentRepository,
                           SigningDocumentService signingDocumentService,
                           EreceiptPdfService ereceiptPdfService,
                           EreceiptIncrementalService ereceiptIncrementalService) {
        this.documentService = documentService;
        this.paymentRepository = paymentRepository;
        this.signingDocumentService = signingDocumentService;
        this.ereceiptPdfService = ereceiptPdfService;
        this.ereceiptIncrementalService = ereceiptIncrementalService;
    }

    public EreceiptNumber generateEreceiptFullNumber(boolean newBusiness) {
        Instant start = LogUtil.logStarting("Generate new ereceipt [start]");
        EreceiptNumber ereceiptNumber = ereceiptIncrementalService.nextValue(newBusiness);
        LogUtil.logFinishing(start, "Generate new ereceipt [finish]: " + ereceiptNumber.getFullNumberForDisplay());
        return ereceiptNumber;
    }

    /**
     * @param policy      policy
     * @param payment     payment entity
     * @param newBusiness is new Bussiness
     * @param accessToken it will be used for sign document
     * @return Document entity as PDF file
     * @throws EreceiptDocumentException if there's something wrong while creating ereceipt pdf.
     */
    public Document addEReceiptPdf(Policy policy, Payment payment, boolean newBusiness, final String accessToken) {
        final Insured mainInsured = ProductUtils.validateMainInsured(policy);
        final String passwordProtected =
                mainInsured.getPerson().getBirthDate().format(DateTimeFormatter.ofPattern(PASSWORD_DOB_PATTERN));
        byte[] decodedNonSignedPdf = ereceiptPdfService.createEreceiptPdf(policy, payment, newBusiness);
        byte[] signedPDFWithPassword = signingDocumentService.signPDFFileWithPassword(decodedNonSignedPdf, passwordProtected, accessToken);
        return addEReceiptPdf(policy, payment, signedPDFWithPassword, "application/pdf");
    }

    /**
     * @param policy         policy
     * @param payment        payment entity
     * @param decodedContent raw pdf file
     * @param mimeType       mimeType
     * @return Document entity as PDF file with password protected
     */
    private Document addEReceiptPdf(Policy policy, Payment payment, byte[] decodedContent, final String mimeType) {
        Document document = documentService.addDocument(
                policy, decodedContent, mimeType, DocumentType.ERECEIPT_PDF,
                DocumentReferenceType.PAYMENT, payment.getPaymentId());
        payment.setReceiptPdfDocument(document);
        paymentRepository.save(payment);
        return document;
    }
}
