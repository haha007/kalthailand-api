package th.co.krungthaiaxa.api.elife.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.service.DocumentService;

import javax.inject.Inject;

/**
 * @author khoi.tran on 11/9/16.
 */
@Component
public class DocumentAssertHelper {
    @Inject
    private DocumentService documentService;

    public void assertHasDocument(Policy policy, DocumentType documentType, String msg) {
        DocumentDownload documentDownload = documentService.findDocumentDownload(policy, documentType);
        Assert.assertNotNull(msg, documentDownload);
        byte[] pdfContent = documentService.getDocumentDownloadContent(documentDownload);
        PeriodicityCode periodicityCode = ProductUtils.getPeriodicityCode(policy);
        String fileName = String.format("%s_%s_%s_%s_%s_%s.pdf",
                DateTimeUtil.formatNowForFileShortPath(),
                policy.getPolicyId(),
                policy.getCommonData().getProductId(),
                policy.getStatus(),
                periodicityCode,
                documentType);
        IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + "/documents/" + fileName, pdfContent);
        Assert.assertTrue(msg, StringUtils.isNotBlank(documentDownload.getContent()));
    }

    public void assertHasNoDocument(Policy policy, DocumentType documentType, String msg) {
        DocumentDownload documentDownload = documentService.findDocumentDownload(policy, documentType);
        Assert.assertNull(msg, documentDownload);
    }
}
