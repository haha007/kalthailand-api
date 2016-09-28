package th.co.krungthaiaxa.api.elife.products.igen;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;

import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.AbstractProductEmailService;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.ProductUtils;
import th.co.krungthaiaxa.api.elife.utils.EmailUtil;

@Service
public class IGenEmailService extends AbstractProductEmailService {	

	private final String productId = ProductType.PRODUCT_IGEN.getLogicName();
	
	public final String quoteEmailPath = "/email-content/email-quote-"+productId+"-content.txt";
    
    @Inject
    private IGenSaleIllustrationService saleIllustrationGenerator;  
    
    /*
     * must be implement for product id
     * */
    
    @Override
    protected String getProductId(){
    	return productId;
    }
    
    /*
     * must be implement for quote email 
     * */
    
    @Override
    protected List<Pair<byte[], String>> getAttachedImage(){
    	return EmailUtil.getDefaultImagePairs();
    }
    
    @Override
    protected Pair<byte[], String> getSaleIllustrationPdf(Quote quote) throws DocumentException, IOException{
    	return saleIllustrationGenerator.generatePDF(quote);
    }
    
    @Override
	public String getQuoteEmailContent(Quote quote) {
		String dcFormat = "#,##0.00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DecimalFormat dcf = new DecimalFormat(dcFormat);
        String emailContent = IOUtil.loadTextFileInClassPath(quoteEmailPath);
        return emailContent.replace("%1$s", "dummy")
                .replace("%2$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%3$s", "dummy")
                .replace("%4$s", dcf.format(0.0))
                .replace("%5$s", dcf.format(0.0))
                .replace("%6$s", dcf.format(0.0))
                .replace("%7$s", dcf.format(0.0))
                .replace("%8$s", dcf.format(0.0))
                .replace("%9$s", dcf.format(0.0))
                .replace("%10$s", dcf.format(0.0))
                .replace("%11$s", "20")
                .replace("%12$s", dcf.format(0.0))
                .replace("%13$s", dcf.format(0.0))               
                .replace("%14$s", "'" + getLineURL() + "'")
                .replace("%15$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%16$s", "'" + getLineURL() + "quote-product/line-" + productId + "'");
	}
 
}

