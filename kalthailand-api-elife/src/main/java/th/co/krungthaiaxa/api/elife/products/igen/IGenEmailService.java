package th.co.krungthaiaxa.api.elife.products.igen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.product.PremiumDetail;
import th.co.krungthaiaxa.api.elife.products.AbstractQuoteEmailService;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.utils.EmailSender;

@Service
public class IGenEmailService extends AbstractQuoteEmailService {

    private final String productId = ProductType.PRODUCT_IGEN.getLogicName();

    public final String quoteEmailPath = "/email-content/email-quote-" + productId + "-content.txt";

    @Autowired
    public IGenEmailService(EmailSender emailSender, IGenSaleIllustrationService saleIllustrationService) {
        super(emailSender, saleIllustrationService);
    }

    @Override
    public String getEmailContent(String emailTemplate, Quote quote) {
        String emailContent = emailTemplate;
        Insured insured = quote.getInsureds().stream().reduce((first, second) -> second).get();
        PremiumDetail premium = quote.getPremiumsData().getPremiumDetail();
        Integer taxDeclared = (insured.getDeclaredTaxPercentAtSubscription() == null ? 0 : insured.getDeclaredTaxPercentAtSubscription());
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
//
//    /*
//     * must be implement for e receipt email
//     * */
//
//    @Override
//    public void sendEreceiptEmail(Policy policy) {
//        // TODO Auto-generated method stub
//
//    }
//
//	/*
//     * must be implement for policy booked email
//     * */
//
//    @Override
//    public void sendPolicyBookedEmail(Policy policy) {
//        // TODO Auto-generated method stub
//
//    }
//
//	/*
//     * must be implement for wrong phone number email
//     * */
//
//    @Override
//    public void sendWrongPhoneNumberEmail(Policy policy) {
//        // TODO Auto-generated method stub
//
//    }
//
//	/*
//     * must be implement for user not response email
//     * */
//
//    @Override
//    public void sendUserNotResponseEmail(Policy policy) {
//        // TODO Auto-generated method stub
//
//    }

}

