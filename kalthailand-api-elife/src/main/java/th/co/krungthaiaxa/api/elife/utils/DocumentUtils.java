package th.co.krungthaiaxa.api.elife.utils;

import th.co.krungthaiaxa.api.elife.model.Document;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;

import java.util.Optional;

/**
 * @author khoi.tran on 11/9/16.
 */
public class DocumentUtils {
    public static Document findDocument(Policy policy, DocumentType documentType) {
        Optional<Document> documentPdf = policy.getDocuments().stream().filter(tmp -> tmp.getTypeName().equals(documentType)).findFirst();
        if (documentPdf.isPresent()) {
            return documentPdf.get();
        } else {
            return null;
        }
    }
}
