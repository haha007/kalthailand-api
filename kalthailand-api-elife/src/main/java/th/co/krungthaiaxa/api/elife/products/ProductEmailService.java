package th.co.krungthaiaxa.api.elife.products;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.EmailException;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.products.igen.IGenQuoteEmailService;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectQuoteEmailService;
import th.co.krungthaiaxa.api.elife.service.EmailService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;

/**
 * @author khoi.tran on 10/3/16.
 *         This class is used to send email for all product types.
 */
@Service
public class ProductEmailService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProductEmailService.class);

    private final EmailService emailService;
    private final IGenQuoteEmailService iGenEmailService;
    private final IProtectQuoteEmailService iProtectEmailService;
    private final QuoteService quoteService;

    @Autowired
    public ProductEmailService(EmailService emailService, IGenQuoteEmailService iGenEmailService, IProtectQuoteEmailService iProtectEmailService, QuoteService quoteService) {
        this.emailService = emailService;
        this.iGenEmailService = iGenEmailService;
        this.iProtectEmailService = iProtectEmailService;
        this.quoteService = quoteService;
    }

    public void sendQuoteEmail(String quoteId, String sessionId, ChannelType channelType, String base64Image, String accessToken) {
        Quote quote = quoteService.validateExistQuote(quoteId, sessionId, channelType);
        sendQuoteEmail(quote, base64Image, accessToken);
    }

    public void sendQuoteEmail(Quote quote, String base64Image, String accessToken) {

        String productId = quote.getCommonData().getProductId();

        if (productId.equals(ProductType.PRODUCT_10_EC.getLogicName())) {
            emailService.sendQuote10ECEmail(quote, base64Image, accessToken);
        } else if (productId.equals(ProductType.PRODUCT_IFINE.getLogicName())) {
            emailService.sendQuoteiFineEmail(quote, accessToken);
        } else if (productId.equals(ProductType.PRODUCT_IPROTECT.getLogicName())) {
            iProtectEmailService.sendQuoteEmail(quote, accessToken);
        } else if (productId.equals(ProductType.PRODUCT_IGEN.getLogicName())) {
            iGenEmailService.sendQuoteEmail(quote, accessToken);
        } else {
            throw new EmailException("This function is not yet supported for product " + productId);
        }

    }
}
