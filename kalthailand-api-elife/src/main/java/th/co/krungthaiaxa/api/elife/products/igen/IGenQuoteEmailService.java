package th.co.krungthaiaxa.api.elife.products.igen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.product.PremiumDetail;
import th.co.krungthaiaxa.api.elife.products.AbstractQuoteEmailService;
import th.co.krungthaiaxa.api.elife.utils.EmailSender;

@Service
public class IGenQuoteEmailService extends AbstractQuoteEmailService {

    @Autowired
    public IGenQuoteEmailService(EmailSender emailSender, IGenSaleIllustrationService saleIllustrationService) {
        super(emailSender, saleIllustrationService);
    }

    @Override
    public String getEmailContent(String emailTemplate, Quote quote) {
        String productId = quote.getCommonData().getProductId();
        String emailContent = emailTemplate;
        Insured insured = quote.getInsureds().stream().reduce((first, second) -> second).get();
        PremiumDetail premium = quote.getPremiumsData().getPremiumDetail();
        Integer taxDeclared = (insured.getDeclaredTaxPercentAtSubscription() == null ? 0 : insured.getDeclaredTaxPercentAtSubscription());
        return emailContent.replace("%CREATE_DATE_TIME%", toThaiYear(quote.getCreationDateTime()))
                .replace("%FATCA_QUESTION_LINK%", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%PREMIUM_PERIODICITY%", toThaiPaymentMode(quote.getPremiumsData().getFinancialScheduler().getPeriodicity()))
                .replace("%PREMIUM_VALUE%", toCurrency(getVal(quote.getPremiumsData().getFinancialScheduler().getModalAmount())))
                .replace("%YEARLY_CASH_BACK_END_OF_CONTRACT%", toCurrency(premium.getYearlyCashBacksForEndOfContract().stream().reduce((first, second) -> second).get().getAmount().getValue()))
                .replace("%YEARLY_CASH_BACK_ANNUAL%", toCurrency(premium.getYearlyCashBacksForAnnual().stream().reduce((first, second) -> second).get().getAmount().getValue()))
                .replace("%DEATH_BENEFIT_0%", toCurrency(getVal(premium.getYearlyDeathBenefits().get(0).getAmount())))
                .replace("%DEATH_BENEFIT_3%", toCurrency(getVal(premium.getYearlyDeathBenefits().get(3).getAmount())))
                .replace("%DEATH_BENEFIT_4%", toCurrency(getVal(premium.getYearlyDeathBenefits().get(4).getAmount())))
                .replace("%DEATH_BENEFIT_5%", toCurrency(getVal(premium.getYearlyDeathBenefits().get(5).getAmount())))
                .replace("%TAX_DECLARED%", String.valueOf(taxDeclared))
                .replace("%TAX_YEARLY_DEDUCTION%", toCurrency(premium.getYearlyTaxDeduction().getValue()))
                .replace("%TAX_YEARLY_TOTAL%", toCurrency(premium.getTotalTaxDeduction().getValue()))
                .replace("%LINE_URL%", "'" + getLineURL() + "'")
                .replace("%LINE_FATCA_QUESTION_URL%", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%LINE_QUOTE_PRODUCT_URL%", "'" + getLineURL() + "quote-product/line-" + productId + "'");
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

