package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.client.SigningClient;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.service.ereceipt.EreceiptService;

import java.util.Optional;

import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM_VALIDATED;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.DA_FORM;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.ERECEIPT_PDF;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;

//TODO need to be refactored.
@Service
public class PolicyDocumentService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PolicyDocumentService.class);

    private final DocumentService documentService;
    private final ApplicationFormService applicationFormService;
    private final DAFormService daFormService;
    private final SigningClient signingClient;
    private final PaymentQueryService paymentQueryService;
    private final EreceiptService ereceiptService;

    @Autowired
    public PolicyDocumentService(DocumentService documentService, ApplicationFormService applicationFormService, DAFormService daFormService, SigningClient signingClient, PaymentQueryService paymentQueryService, EreceiptService ereceiptService) {
        this.documentService = documentService;
        this.applicationFormService = applicationFormService;
        this.daFormService = daFormService;
        this.signingClient = signingClient;
        this.paymentQueryService = paymentQueryService;
        this.ereceiptService = ereceiptService;
    }

    public void generateNotValidatedPolicyDocuments(Policy policy) {
        // Generate NOT validated Application Form
        Optional<Document> notValidatedApplicationForm = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(APPLICATION_FORM)).findFirst();
        if (!notValidatedApplicationForm.isPresent()) {
            try {
                byte[] content = applicationFormService.generateNotValidatedApplicationForm(policy);
                documentService.addDocument(policy, content, "application/pdf", APPLICATION_FORM);
            } catch (Exception e) {
                LOGGER.error("NOT validated Application form for Policy [" + policy.getPolicyId() + "] has not been generated.", e);
            }
        }

        // Generate DA Form if necessary
        if (policy.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().equals(EVERY_MONTH)) {
            Optional<Document> daForm = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(DA_FORM)).findFirst();
            if (!daForm.isPresent()) {
                try {
                    byte[] content = daFormService.generateDAForm(policy);
                    documentService.addDocument(policy, content, "application/pdf", DA_FORM);
                } catch (Exception e) {
                    LOGGER.error("DA form for Policy [" + policy.getPolicyId() + "] has not been generated.", e);
                }
            }
        } else {
            LOGGER.info("Policy is not in monthly payment and DA Form should not be generated.");
        }

        LOGGER.info("Default documents for policy [" + policy.getPolicyId() + "] have been created.");
    }

    public void generateValidatedPolicyDocuments(Policy policy, String token) {
        boolean isTheFirstPaymentSession = true;

        // In case previous documents were not generated
        generateNotValidatedPolicyDocuments(policy);

        // Generate validated Application Form
        Optional<Document> validatedApplicationForm = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(APPLICATION_FORM_VALIDATED)).findFirst();
        if (!validatedApplicationForm.isPresent()) {
            try {
                byte[] content = applicationFormService.generateValidatedApplicationForm(policy);
                documentService.addDocument(policy, content, "application/pdf", APPLICATION_FORM_VALIDATED);
                LOGGER.debug("Validated Application form has been added to Policy.");
            } catch (Exception e) {
                LOGGER.error("Validated Application form for Policy [" + policy.getPolicyId() + "] has not been generated.", e);
            }
        } else {
            LOGGER.debug("Validated Application form already exists.");
        }

        // Generate Ereceipt as Image
        Payment firstPayment = paymentQueryService.validateExistFirstPaymentOrderById(policy.getPolicyId());
//        byte[] ereceiptImage = null;
//        Optional<Document> documentImage = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_IMAGE)).findFirst();
//        if (!documentImage.isPresent()) {
//            try {
//                ereceiptImage = ereceiptService.createEreceiptImage(policy, firstPayment, true);
//                ereceiptService.addEReceiptDocument(policy, firstPayment, ereceiptImage, "image/png", ERECEIPT_IMAGE);
//                LOGGER.info("Ereceipt image has been added to Policy.");
//            } catch (Exception e) {
//                LOGGER.error("Image Ereceipt for Policy [" + policy.getPolicyId() + "] has not been generated.", e);
//            }
//        } else {
//            LOGGER.info("ereceipt image already exists.");
//            ereceiptImage = Base64.getDecoder().decode(documentService.findDocumentDownload(documentImage.get().getId()).getContent().getBytes());
//        }

        // Generate Ereceipt as PDF
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        if (!documentPdf.isPresent()) {
            ereceiptService.addEreceiptPdf(policy, firstPayment, isTheFirstPaymentSession, token);
//            try {
//                byte[] decodedNonSignedPdf = ereceiptService.createEreceiptPdf(ereceiptImage);
//                byte[] encodedNonSignedPdf = Base64.getEncoder().encode(decodedNonSignedPdf);
//                byte[] encodedSignedPdf = signingClient.getEncodedSignedPdfDocument(encodedNonSignedPdf, token);
//                byte[] decodedSignedPdf = Base64.getDecoder().decode(encodedSignedPdf);
//                ereceiptService.addEReceiptDocument(policy, firstPayment, decodedSignedPdf, "application/pdf", ERECEIPT_PDF);
//                LOGGER.info("Ereceipt pdf has been added to Policy.");
//            } catch (Exception e) {
//                LOGGER.error("PDF Ereceipt for Policy [" + policy.getPolicyId() + "] has not been generated.", e);
//            }
        } else {
            LOGGER.debug("Signed ereceipt pdf already exists.");
        }
        LOGGER.debug("Extra documents for policy [" + policy.getPolicyId() + "] have been created.");
    }

}
