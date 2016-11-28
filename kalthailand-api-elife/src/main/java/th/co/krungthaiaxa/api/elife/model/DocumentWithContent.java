package th.co.krungthaiaxa.api.elife.model;

/**
 * @author khoi.tran on 11/28/16.
 *         This is the wrapper class for {@link Document} and {@link DocumentDownload}
 */
public class DocumentWithContent {
    private Document document;
    private DocumentDownload documentDownload;
    private byte[] documentContent;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public DocumentDownload getDocumentDownload() {
        return documentDownload;
    }

    public void setDocumentDownload(DocumentDownload documentDownload) {
        this.documentDownload = documentDownload;
    }

    public byte[] getDocumentContent() {
        return documentContent;
    }

    public void setDocumentContent(byte[] documentContent) {
        this.documentContent = documentContent;
    }
}
