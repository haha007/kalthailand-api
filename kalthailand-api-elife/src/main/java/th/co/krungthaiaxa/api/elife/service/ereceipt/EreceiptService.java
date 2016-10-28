package th.co.krungthaiaxa.api.elife.service.ereceipt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
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

import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.ERECEIPT_PDF;

/**
 * @author khoi.tran on 10/25/16.
 */
@Service
public class EreceiptService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);
    private final static String ERECEIPT_IMAGE_FILE_NAME_PREFIX = "ereceipts_merged";
    private final static String ERECEIPT_TEMPLATE_FILE_NAME = "AGENT-WHITE-FINAL.jpg";
    private final static int LINE_POS_REF2 = 576;//px
    private static final int LINE_POS_PERSON_REGID = 495;//px
    private final static double CELL_WIDTH = 28.0;

    private final DocumentService documentService;
    private final PaymentRepository paymentRepository;
    private final SigningClient signingClient;
    private final EreceiptPdfService ereceiptPdfService;

    @Autowired
    public EreceiptService(DocumentService documentService, PaymentRepository paymentRepository, SigningClient signingClient, EreceiptPdfService ereceiptPdfService) {
        this.documentService = documentService;
        this.paymentRepository = paymentRepository;
        this.signingClient = signingClient;
        this.ereceiptPdfService = ereceiptPdfService;
    }

    /**
     * @param policy
     * @param payment
     * @param firstPayment
     * @param accessToken  it will be used for sign document
     * @return
     * @throws EreceiptDocumentException if there's something wrong while creating ereceipt pdf.
     */
    public Document addEreceiptPdf(Policy policy, Payment payment, boolean firstPayment, String accessToken) {
        byte[] decodedNonSignedPdf = ereceiptPdfService.createEreceiptPdf(policy, payment, firstPayment);
        byte[] encodedNonSignedPdf = Base64.getEncoder().encode(decodedNonSignedPdf);
        byte[] encodedSignedPdf = signingClient.getEncodedSignedPdfDocument(encodedNonSignedPdf, accessToken);
        byte[] decodedSignedPdf = Base64.getDecoder().decode(encodedSignedPdf);
        return addEReceiptDocument(policy, payment, decodedSignedPdf, "application/pdf", ERECEIPT_PDF);
    }

    public Document addEReceiptDocument(Policy policy, Payment payment, byte[] decodedContent, String mimeType, DocumentType documentType) {
        Document document = documentService.addDocument(policy, decodedContent, mimeType, documentType, DocumentReferenceType.PAYMENT, payment.getPaymentId());
        if (documentType == DocumentType.ERECEIPT_IMAGE) {
            payment.setReceiptImageDocument(document);
        } else if (documentType == DocumentType.ERECEIPT_PDF) {
            payment.setReceiptPdfDocument(document);
        } else {
            throw new UnexpectedException("There's something really wrong here. You can only call this method if your documentType is either " + DocumentType.ERECEIPT_IMAGE + " or " + DocumentType.ERECEIPT_PDF);
        }
        paymentRepository.save(payment);
        return document;
    }

}
