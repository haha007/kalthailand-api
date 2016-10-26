package th.co.krungthaiaxa.api.elife.utils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import th.co.krungthaiaxa.api.common.exeption.FileIOException;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.PdfIOUtil;
import th.co.krungthaiaxa.api.common.utils.PdfUtil;
import th.co.krungthaiaxa.api.elife.TestUtil;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author khoi.tran on 10/26/16.
 */
public class PdfTestUtil {
    private static final boolean invertY = true;

    /**
     * When you use invertY is false, this method will write the first line with position on it. 574
     */
    private static final float Y_TOP_WHICH_DRAWN_IN_NORMAL_POSITION = 574.0f;
    private static final float Y_TOP_INSPECT = 97.48f;
    private static final BaseFont BASE_FONT = loadBaseFont();

    public static void writePositionsOnPdfTemplate(String templateClassPath) {
        InputStream pdfTemplateInputStream = IOUtil.loadInputStreamFromClassPath(templateClassPath);
        byte[] bytes = writePositionsOnPdfTemplate(pdfTemplateInputStream);
        IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + templateClassPath, bytes);
    }

    public static byte[] writePositionsOnPdfTemplate(InputStream pdfTemplateInputStream) {
        try (ByteArrayOutputStream content = new ByteArrayOutputStream()) {
            PdfReader pdfReader = new PdfReader(pdfTemplateInputStream);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, content);
            writePagePosition(pdfStamper.getOverContent(1));

            pdfStamper.close();
            content.close();
            byte[] result = content.toByteArray();
            return result;
        } catch (DocumentException | IOException ex) {
            throw new FileIOException(String.format("Generate eReceipt pdf [error]: Error message: %s", ex.getMessage()), ex);
        }
    }

    public static void writePagePosition(PdfContentByte page) {
        int numOfCols = 5;
        for (int row = 0; row < 300; row++) {
            for (int col = 0; col < 100; col++) {
                int x = col * (numOfCols * 10) + (10 * (row % numOfCols));
                int y = row * 2;
                writeText(page, "." + x + "x" + y, new Point(x, y));
            }
        }
    }

    private static void writeText(PdfContentByte page, String text, Point point) {
        float actualY = point.y;
        if (invertY) {
            actualY = Y_TOP_WHICH_DRAWN_IN_NORMAL_POSITION + Y_TOP_INSPECT - point.y;
        }
        PdfUtil.writeText(page, BASE_FONT, text, (float) point.getX(), actualY, PdfUtil.TINY);
    }

    private static BaseFont loadBaseFont() {
        return PdfIOUtil.loadFontFromClassPath("ANGSAB_1.ttf", "/ereceipt/ANGSAB_1.TTF");
    }
}
