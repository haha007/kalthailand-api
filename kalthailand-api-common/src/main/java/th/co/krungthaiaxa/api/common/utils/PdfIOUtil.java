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
        try {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, content);

            document.open();
            document.add(pdfPTable);
            document.close();
            content.close();
            return content.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new FileIOException("Cannot write pdfpTable to bytes: " + pdfPTable.getSummary(), e);
        }
    }
}
