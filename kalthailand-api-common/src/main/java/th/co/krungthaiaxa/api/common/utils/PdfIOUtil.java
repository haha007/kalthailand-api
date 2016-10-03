package th.co.krungthaiaxa.api.common.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import th.co.krungthaiaxa.api.common.exeption.FileIOException;

import java.io.IOException;

/**
 * @author khoi.tran on 10/3/16.
 */
public class PdfIOUtil {
    public static byte[] writeToBytes(PdfPTable pdfPTable) {
        ByteArrayOutputStream content = null;
        Document document = null;
        try {
            content = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, content);

            document.open();
            document.add(pdfPTable);

            return content.toByteArray();
        } catch (DocumentException e) {
            throw new FileIOException("Cannot write pdfpTable to bytes: " + pdfPTable.getSummary(), e);
        } finally {
            try {
                if (content != null) {
                    content.close();
                }
                if (document != null) {
                    document.close();
                }
            } catch (IOException e) {
                throw new FileIOException("Cannot close Pdf writer", e);
            }
        }
    }
}
