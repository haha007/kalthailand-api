package th.co.krungthaiaxa.api.elife.products.igen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.product.PremiumDetail;
import th.co.krungthaiaxa.api.elife.products.AbstractQuoteEmailService;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailHelper;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailService;
import th.co.krungthaiaxa.api.elife.service.SigningDocumentService;

@Service
public class IGenQuoteEmailService extends AbstractQuoteEmailService {

    @Autowired
    public IGenQuoteEmailService(ElifeEmailService axaEmailService,
                                 ElifeEmailHelper axaEmailHelper,
                                 IGenSaleIllustrationService saleIllustrationService,
                                 SigningDocumentService signingDocumentService) {
        super(axaEmailService, axaEmailHelper, saleIllustrationService, signingDocumentService);
    }

    @Override
    public String getEmailContent(final String emailTemplate, final Quote quote) {
        String productId = quote.getCommonData().getProductId();
        Insured insured = quote.getInsureds().stream().reduce((first, second) -> second).get();
        PremiumDetail premium = quote.getPremiumsData().getPremiumDetail();
        Integer taxDeclared = (insured.getDeclaredTaxPercentAtSubscription() == null ? 0 : insured.getDeclaredTaxPercentAtSubscription());
        return emailTemplate.replace("%CREATE_DATE_TIME%", getAxaEmailHelper().toThaiYear(quote.getCreationDateTime()))
                .replace("%FATCA_QUESTION_LINK%", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%PREMIUM_PERIODICITY%", getAxaEmailHelper().toThaiPaymentMode(quote.getPremiumsData().getFinancialScheduler().getPeriodicity()))
                .replace("%PREMIUM_VALUE%", getAxaEmailHelper().toCurrencyValue(getVal(quote.getPremiumsData().getFinancialScheduler().getModalAmount())))
                .replace("%YEARLY_CASH_BACK_END_OF_CONTRACT%", getAxaEmailHelper().toCurrencyValue(premium.getYearlyCashBacksForEndOfContract().stream().reduce((first, second) -> second).get().getAmount().getValue()))
                .replace("%YEARLY_CASH_BACK_ANNUAL%", getAxaEmailHelper().toCurrencyValue(premium.getYearlyCashBacksForAnnual().stream().reduce((first, second) -> second).get().getAmount().getValue()))
                .replace("%DEATH_BENEFIT_0%", getAxaEmailHelper().toCurrencyValue(getVal(premium.getYearlyDeathBenefits().get(0).getAmount())))
                .replace("%DEATH_BENEFIT_3%", getAxaEmailHelper().toCurrencyValue(getVal(premium.getYearlyDeathBenefits().get(3).getAmount())))
                .replace("%DEATH_BENEFIT_4%", getAxaEmailHelper().toCurrencyValue(getVal(premium.getYearlyDeathBenefits().get(4).getAmount())))
                .replace("%DEATH_BENEFIT_5%", getAxaEmailHelper().toCurrencyValue(getVal(premium.getYearlyDeathBenefits().get(5).getAmount())))
                .replace("%TAX_DECLARED%", String.valueOf(taxDeclared))
                .replace("%TAX_YEARLY_DEDUCTION%", getAxaEmailHelper().toCurrencyValue(premium.getYearlyTaxDeduction().getValue()))
                .replace("%TAX_YEARLY_TOTAL%", getAxaEmailHelper().toCurrencyValue(premium.getTotalTaxDeduction().getValue()))
                .replace("%LINE_URL%", "'" + getLineURL() + "'")
                .replace("%LINE_FATCA_QUESTION_URL%", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%LINE_QUOTE_PRODUCT_URL%", "'" + getLineURL() + "quote-product/line-" + productId + "'");
    }

}

