package th.co.krungthaiaxa.api.elife.ereceipt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.client.SigningClient;
import th.co.krungthaiaxa.api.elife.exception.EreceiptDocumentException;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentReferenceType;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.service.DocumentService;

import java.util.Base64;

/**
 * @author khoi.tran on 10/25/16.
 */
@Service
public class EreceiptService {
    public final static Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentService documentService;
    private final PaymentRepository paymentRepository;
    private final SigningClient signingClient;
    private final EreceiptPdfService ereceiptPdfService;
    private final EreceiptIncrementalService ereceiptIncrementalService;

    @Autowired
    public EreceiptService(DocumentService documentService, PaymentRepository paymentRepository, SigningClient signingClient, EreceiptPdfService ereceiptPdfService, EreceiptIncrementalService ereceiptIncrementalService) {
        this.documentService = documentService;
        this.paymentRepository = paymentRepository;
        this.signingClient = signingClient;
        this.ereceiptPdfService = ereceiptPdfService;
        this.ereceiptIncrementalService = ereceiptIncrementalService;
    }

    public EreceiptNumber generateEreceiptFullNumber(boolean newBusiness) {
        return ereceiptIncrementalService.nextValue(newBusiness);
    }

    /**
     * @param policy
     * @param payment
     * @param newBusiness
     * @param accessToken it will be used for sign document
     * @return
     * @throws EreceiptDocumentException if there's something wrong while creating ereceipt pdf.
     */
    public Document addEreceiptPdf(Policy policy, Payment payment, boolean newBusiness, String accessToken) {
        byte[] decodedNonSignedPdf = ereceiptPdfService.createEreceiptPdf(policy, payment, newBusiness);
        byte[] encodedNonSignedPdf = Base64.getEncoder().encode(decodedNonSignedPdf);
        byte[] encodedSignedPdf = signingClient.getEncodedSignedPdfDocument(encodedNonSignedPdf, accessToken);
        byte[] decodedSignedPdf = Base64.getDecoder().decode(encodedSignedPdf);
        return addEreceiptPdf(policy, payment, decodedSignedPdf, "application/pdf");
    }

    public Document addEreceiptPdf(Policy policy, Payment payment, byte[] decodedContent, String mimeType) {
        Document document = documentService.addDocument(policy, decodedContent, mimeType, DocumentType.ERECEIPT_PDF, DocumentReferenceType.PAYMENT, payment.getPaymentId());
        payment.setReceiptPdfDocument(document);
        paymentRepository.save(payment);
        return document;
    }
}