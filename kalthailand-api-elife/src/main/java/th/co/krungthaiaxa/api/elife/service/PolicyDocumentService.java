package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.log.LogUtil;
import th.co.krungthaiaxa.api.elife.ereceipt.EreceiptService;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;

import java.time.Instant;
import java.util.Optional;

import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.APPLICATION_FORM_VALIDATED;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.DA_FORM;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.ERECEIPT_PDF;

//TODO need to be refactored.
@Service
public class PolicyDocumentService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PolicyDocumentService.class);

    private final DocumentService documentService;
    private final ApplicationFormService applicationFormService;
    private final DAFormService daFormService;
    private final PaymentQueryService paymentQueryService;
    private final EreceiptService ereceiptService;

    @Autowired
    public PolicyDocumentService(DocumentService documentService, ApplicationFormService applicationFormService, DAFormService daFormService, PaymentQueryService paymentQueryService, EreceiptService ereceiptService) {
        this.documentService = documentService;
        this.applicationFormService = applicationFormService;
        this.daFormService = daFormService;
        this.paymentQueryService = paymentQueryService;
        this.ereceiptService = ereceiptService;
    }

    public void generateDocumentsForPendingValidationPolicy(Policy policy) {
        Instant start = LogUtil.logStarting("generateDocumentsForPendingValidationPolicy [start]: policyId: " + policy.getPolicyId());

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
        if (ProductUtils.isAtpModeEnable(policy)) {
            Optional<Document> daForm = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(DA_FORM)).findFirst();
            if (!daForm.isPresent()) {
                try {
                    byte[] content = daFormService.generateDAFormPdf(policy);
                    documentService.addDocument(policy, content, "application/pdf", DA_FORM);
                } catch (Exception e) {
                    LOGGER.error("DA form for Policy [" + policy.getPolicyId() + "] has not been generated.", e);
                }
            }
        } else {
            LOGGER.debug("The ATP of this policy is not enable, so the DA Form will not be generated.\n policyId: {}", policy.getPolicyId());
        }
        LogUtil.logFinishing(start, "generateDocumentsForPendingValidationPolicy [finish]: policyId: " + policy.getPolicyId());
    }

    public void generateDocumentsForValidatedPolicy(Policy policy, String token) {
        Instant start = LogUtil.logStarting("generateDocumentsForValidatedPolicy [start]: policyId: " + policy.getPolicyId());
        boolean newBusiness = true;

        // In case previous documents were not generated
        generateDocumentsForPendingValidationPolicy(policy);

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

        Payment firstPayment = paymentQueryService.validateExistFirstPaymentOrderById(policy.getPolicyId());
        Optional<Document> ereceiptPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(ERECEIPT_PDF)).findFirst();
        if (!ereceiptPdf.isPresent()) {
            ereceiptService.addEReceiptPdf(policy, firstPayment, newBusiness, token);
        } else {
            LOGGER.debug("Signed ereceipt pdf already exists.");
        }
        LogUtil.logFinishing(start, "generateDocumentsForValidatedPolicy [finish]: policyId: " + policy.getPolicyId());
    }

}
