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
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.ProductIGenPremium;
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
    private IGenPdfRenderService iGenPdfRenderService;  
    
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
    protected List<Pair<byte[], String>> getQuoteAttachedImage(){
    	return EmailUtil.getDefaultImagePairs();
    }
    
    @Override
    protected Pair<byte[], String> getSaleIllustrationPdf(Quote quote) throws DocumentException, IOException{
    	return iGenPdfRenderService.generateSaleIllustrationPDF(quote);
    }
    
    @Override
	public String getQuoteEmailContent(Quote quote) {
        String emailContent = IOUtil.loadTextFileInClassPath(quoteEmailPath);
        Insured insured = quote.getInsureds().stream().reduce((first, second) -> second).get();
        ProductIGenPremium premium = quote.getPremiumsData().getProductIGenPremium();
        Integer taxDeclared = (insured.getDeclaredTaxPercentAtSubscription()==null?0:insured.getDeclaredTaxPercentAtSubscription());
        return emailContent.replace("%1$s", toThaiYear(quote.getCreationDateTime()))
                .replace("%2$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%3$s", toThaiPaymentMode(quote.getPremiumsData().getFinancialScheduler().getPeriodicity()))
                .replace("%4$s", toCurrency(getVal(quote.getPremiumsData().getFinancialScheduler().getModalAmount())))
                .replace("%5$s", toCurrency(premium.getYearlyCashBacksForEndOfContract().stream().reduce((first, second) -> second).get().getAmount().getValue()))
                .replace("%6$s", toCurrency(premium.getYearlyCashBacksForAnnual().stream().reduce((first, second) -> second).get().getAmount().getValue()))
                .replace("%7$s", toCurrency(getVal(premium.getYearlyDeathBenefits().get(0).getAmount())))
                .replace("%8$s", toCurrency(getVal(premium.getYearlyDeathBenefits().get(3).getAmount())))
                .replace("%9$s", toCurrency(getVal(premium.getYearlyDeathBenefits().get(4).getAmount())))
                .replace("%10$s", toCurrency(getVal(premium.getYearlyDeathBenefits().get(5).getAmount())))
                .replace("%11$s", String.valueOf(taxDeclared))
                .replace("%12$s", toCurrency(premium.getYearlyTaxDeduction().getValue()))
                .replace("%13$s", toCurrency(premium.getTotalTaxDeduction().getValue()))               
                .replace("%14$s", "'" + getLineURL() + "'")
                .replace("%15$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%16$s", "'" + getLineURL() + "quote-product/line-" + productId + "'");
	}
    
    /*
     * must be implement for e receipt email 
     * */

	@Override
	public void sendEreceiptEmail(Policy policy) {
		// TODO Auto-generated method stub
		
	}
	
	/*
     * must be implement for policy booked email 
     * */

	@Override
	public void sendPolicyBookedEmail(Policy policy) {
		// TODO Auto-generated method stub
		
	}
	
	/*
     * must be implement for wrong phone number email 
     * */

	@Override
	public void sendWrongPhoneNumberEmail(Policy policy) {
		// TODO Auto-generated method stub
		
	}
	
	/*
     * must be implement for user not response email 
     * */

	@Override
	public void sendUserNotResponseEmail(Policy policy) {
		// TODO Auto-generated method stub
		
	}
 
}

