package th.co.krungthaiaxa.api.elife.products;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.itextpdf.text.DocumentException;

import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.utils.EmailSender;

@Component
public abstract class AbstractProductEmailService implements InterfaceProductEmailService {	
	
	protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractProductEmailService.class);
	
	@Inject
	protected EmailSender emailSender;	
	@Value("${email.name}")
    protected String fromEmail;	
    @Value("${email.subject.quote}")
    protected String emailQuoteSubject;
    @Value("${line.app.id}")
    protected String lineId;
    @Inject
    private MessageSource messageSource;
    private Locale thLocale = new Locale("th",""); 
    private final String dcFormat = "#,##0.00";
    private DecimalFormat dcf = new DecimalFormat(dcFormat);
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /*
     * all mini function need in email
     * */
    
    protected String getLineURL() {
    	return "https://line.me/R/ch/" + lineId + "/elife/th/";
    }
    
    protected String toCurrency(Double value){
    	return dcf.format(value);
    }
    
    protected String toThaiYear(LocalDateTime time){
    	return time.plusYears(543).format(formatter);
    }
    
    protected String toThaiPaymentMode(Periodicity due){
    	return messageSource.getMessage("payment.mode." + due.getCode().toString(), null, thLocale);
    }
    
    protected Double getVal(Amount amount){
    	return amount.getValue();
    }
    
    /*
     * for sending email quote
     * */
    
    @Override
	public void sendQuoteEmail(Quote quote) {
		LOGGER.info("Sending quote "+getProductId()+" email...");
        List<Pair<byte[], String>> base64ImgFileNames = getQuoteAttachedImage(); 
        List<Pair<byte[], String>> attachments = new ArrayList();
        try {
			attachments.add(getSaleIllustrationPdf(quote));
		} catch (DocumentException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);
        emailSender.sendEmail(fromEmail, mainInsured.getPerson().getEmail(), emailQuoteSubject, getQuoteEmailContent(quote), base64ImgFileNames, attachments);
        LOGGER.info("Quote "+getProductId()+" email sent!");
	}
    
    /*
     * abstract for unique product id
     * */
    
    protected abstract String getProductId();
    
    /*
     * abstract for quote email
     * */
    
    protected abstract List<Pair<byte[], String>> getQuoteAttachedImage();
    
    protected abstract Pair<byte[], String> getSaleIllustrationPdf(Quote quote) throws DocumentException, IOException;
    
    protected abstract String getQuoteEmailContent(Quote quote);
}
