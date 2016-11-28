package th.co.krungthaiaxa.api.elife.utils;

import th.co.krungthaiaxa.api.common.utils.DownloadUtil;
import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.DocumentDownload;
import th.co.krungthaiaxa.api.elife.model.DocumentWithContent;

import javax.servlet.http.HttpServletResponse;

/**
 * @author khoi.tran on 11/28/16.
 */
public class ElifeDownloadUtils {
    public static void writeBytesToResponse(HttpServletResponse response, DocumentWithContent documentWithContent) {
        String fileName = "eLife";
        Document document = documentWithContent.getDocument();
        if (document != null) {
            String refId = document.getReferenceId() == null ? "" : document.getReferenceId();
            fileName = document.getTypeName().name() + "_" + document.getPolicyId() + "_" + refId;
        }
        DocumentDownload documentDownload = documentWithContent.getDocumentDownload();
        DownloadUtil.writeBytesToResponse(response, documentWithContent.getDocumentContent(), fileName, documentDownload.getMimeType());
    }
}
