package th.co.krungthaiaxa.api.elife.products;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import com.itextpdf.text.DocumentException;

import th.co.krungthaiaxa.api.elife.model.Quote;

public interface InterfaceProductSaleIllustrationService {
	
	/*
	 * need to be implement for all of sale illustration pdf generated
	 * */
	
	Pair<byte[], String> generatePDF(Quote quote)throws DocumentException, IOException;

}
