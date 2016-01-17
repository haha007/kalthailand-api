package th.co.krungthaiaxa.ebiz.api.utils;

import com.aspose.ocr.ImageStream;
import com.aspose.ocr.License;
import com.aspose.ocr.OcrEngine;

import java.io.InputStream;

public class OCRUtil {
    public static String extractText(InputStream inputStream, int imageStreamFormat) throws Exception {
        // Instantiate an instance of license and set the license file through its path
        License license = new License();
        license.setLicense(OCRUtil.class.getResourceAsStream("/Aspose.OCR.lic"));

        // Create an instance of OcrEngine
        OcrEngine ocr = new OcrEngine();

        // Set image file
        ocr.setImage(ImageStream.fromStream(inputStream, imageStreamFormat));

        // Perform OCR and get extracted text
        if (ocr.process()) {
            return ocr.getText().toString();
        } else {
            throw new Exception("Unable to get any text out of provided image");
        }
    }
}
