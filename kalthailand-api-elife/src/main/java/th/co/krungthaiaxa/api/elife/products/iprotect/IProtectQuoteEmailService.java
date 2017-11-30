package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.LocaleUtil;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.products.AbstractQuoteEmailService;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailHelper;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailService;
import th.co.krungthaiaxa.api.elife.service.SigningDocumentService;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * @author khoi.tran on 8/31/16.
 */
@Service
public class IProtectQuoteEmailService extends AbstractQuoteEmailService {
    @Inject
    public IProtectQuoteEmailService(ElifeEmailService axaEmailService,
                                     ElifeEmailHelper axaEmailHelper,
                                     IProtectSaleIllustrationService iProtectSaleIllustrationService,
                                     SigningDocumentService signingDocumentService) {
        super(axaEmailService, axaEmailHelper, iProtectSaleIllustrationService, signingDocumentService);
    }

    @Override
    protected String getEmailContent(final String emailTemplate, final Quote quote) {
        String dcFormat = "#,##0.00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DecimalFormat dcf = new DecimalFormat(dcFormat);
        Insured mainInsured = ProductUtils.validateExistMainInsured(quote);
        Integer taxDeclared = mainInsured.getDeclaredTaxPercentAtSubscription();
        if (taxDeclared == null) {
            taxDeclared = 0;
        }
        return emailTemplate.replace("%1$s", quote.getCreationDateTime().plusYears(543).format(formatter))
                .replace("%2$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%3$s", dcf.format(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()))
                .replace("%4$s", dcf.format(quote.getPremiumsData().getProductIProtectPremium().getDeathBenefit().getValue()))
                .replace("%5$s", dcf.format(quote.getPremiumsData().getProductIProtectPremium().getSumInsured().getValue()))
                .replace("%6$s", dcf.format(quote.getPremiumsData().getProductIProtectPremium().getYearlyTaxDeduction().getValue()))
                .replace("%7$s", dcf.format(quote.getPremiumsData().getProductIProtectPremium().getTotalTaxDeduction().getValue()))
                .replace("%8$s", "'" + getLineURL() + "'")
                .replace("%9$s", "'" + getLineURL() + "fatca-questions/" + quote.getQuoteId() + "'")
                .replace("%10$s", "'" + getLineURL() + "quote-product/line-iProtect" + "'")
                .replace("%11$s", String.valueOf(taxDeclared))
                .replace("%12$s", getAxaEmailHelper().getMessageSource().getMessage("payment.mode." + quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString(), null, LocaleUtil.THAI_LOCALE));
    }
}
