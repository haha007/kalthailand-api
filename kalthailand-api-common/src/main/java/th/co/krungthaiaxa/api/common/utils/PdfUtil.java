package th.co.krungthaiaxa.api.common.utils;

import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;

/**
 * @author khoi.tran on 10/25/16.
 */
public class PdfUtil {
    public static final float TINY = 4f;
    public static final float VERY_SMALL_SIZE = 7f;
    public static final float SMALL_SIZE = 10f;
    public static final float MEDIUM_SIZE = 13f;
    public static final float BIG_SIZE = 25f;

    public static void writeText(PdfContentByte pdfContentByte, BaseFont font, String text, float x, float y, float fontSize) {
        pdfContentByte.beginText();
        pdfContentByte.setFontAndSize(font, fontSize);
        pdfContentByte.setTextMatrix(x, y);
        pdfContentByte.showText(text);
        pdfContentByte.endText();
    }
}
