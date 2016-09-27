package th.co.krungthaiaxa.api.elife.products.igen;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.AbstractProductSaleIllustrationService;
import th.co.krungthaiaxa.api.elife.products.InterfaceProductSaleIllustrationService;

public class IGenSaleIllustrationService extends AbstractProductSaleIllustrationService{
	
	private final static Logger logger = LoggerFactory.getLogger(IGenSaleIllustrationService.class);

	@Override
	public Pair<byte[], String> generatePDF(Quote quote) throws DocumentException, IOException {
		
		if (logger.isDebugEnabled()) {
            logger.debug(String.format("[%1$s] .....", "generatePDF"));
            logger.debug(String.format("quote is %1$s", quote.toString()));
        }
		
		Integer tbCols = 2;
        PdfPTable table1 = new PdfPTable(tbCols);
        table1.addCell(addData("test for generate pdf sale illustration", getFontHeaderStyle(), tbCols, TB_HORIZONTAL_ALIGN_LEFT, null));
        
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        document.open();
        document.add(table1);
        
        return getPdfArrayByte(document, quote.getPolicyId());
		
	}

}
