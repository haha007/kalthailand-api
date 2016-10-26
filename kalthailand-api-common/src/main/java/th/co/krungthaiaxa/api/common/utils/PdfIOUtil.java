package th.co.krungthaiaxa.api.common.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.exeption.FileIOException;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;

import java.io.IOException;

/**
 * @author khoi.tran on 10/3/16.
 */
public class PdfIOUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(PdfIOUtil.class);

    public static BaseFont loadFontFromClassPath(String fontName, String fontClassPath) {
        try {
            byte[] bytes = IOUtil.loadBinaryFileInClassPath(fontClassPath);
            return BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
        } catch (DocumentException | IOException e) {
            throw new UnexpectedException("Unable to load embed font file: " + e.getMessage(), e);
        }
    }

    public static byte[] writeToBytes(PdfPTable pdfPTable) {
        ByteArrayOutputStream content = null;
        Document document = null;
        try {
            content = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, content);

            document.open();
            document.add(pdfPTable);
            document.close();
            return content.toByteArray();
        } catch (DocumentException e) {
            throw new FileIOException("Cannot write pdfpTable to bytes: " + pdfPTable.getSummary(), e);
        } finally {
            IOUtil.closeIfPossible(content);
        }
    }
}
