package th.co.krungthaiaxa.api.elife.products;

import com.itextpdf.text.DocumentException;
import org.apache.commons.lang3.tuple.Pair;
import th.co.krungthaiaxa.api.elife.model.Quote;

import java.io.IOException;

public interface InterfaceProductPdfRenderService {

    Pair<byte[], String> generateSaleIllustrationPDF(Quote quote) throws DocumentException, IOException;
//	Pair<byte[], String> generateEreceiptPDF(Policy policy)throws DocumentException, IOException;
//	Pair<byte[], String> generateDaFormPDF(Policy policy)throws DocumentException, IOException;
//	Pair<byte[], String> generateApplicationFormNotValidatedPDF(Policy policy)throws DocumentException, IOException;
//	Pair<byte[], String> generateApplicationFormValidatedPDF(Policy policy)throws DocumentException, IOException;

}
