package th.co.krungthaiaxa.api.elife.products;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.util.concurrent.CycleDetectingLockFactory.Policy;
import com.itextpdf.text.DocumentException;

import th.co.krungthaiaxa.api.elife.model.Quote;

public interface InterfaceProductPdfRenderService {
	
	Pair<byte[], String> generateSaleIllustrationPDF(Quote quote)throws DocumentException, IOException;
	Pair<byte[], String> generateEreceiptPDF(Policy policy)throws DocumentException, IOException;
	Pair<byte[], String> generateDaFormPDF(Policy policy)throws DocumentException, IOException;
	Pair<byte[], String> generateApplicationFormNotValidatedPDF(Policy policy)throws DocumentException, IOException;
	Pair<byte[], String> generateApplicationFormValidatedPDF(Policy policy)throws DocumentException, IOException;

}
