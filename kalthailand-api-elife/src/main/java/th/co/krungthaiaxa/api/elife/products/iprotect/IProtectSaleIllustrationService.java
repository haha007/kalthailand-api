package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.service.JasperService;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.model.Quote;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by SantiLik on 3/28/2016.
 */

@Service
public class IProtectSaleIllustrationService {

    private final static Logger logger = LoggerFactory.getLogger(IProtectSaleIllustrationService.class);
    private final static String JASPER_FILE = "/saleillustration/iprotect.jrxml";

    private final JasperService jasperService;
    private final MessageSource messageSource;
    private Locale thLocale = new Locale("th", "");
    private String MONEY_DECIMAL_FORMAT = "#,##0.00";

    @Inject
    public IProtectSaleIllustrationService(JasperService jasperService, MessageSource messageSource) {
        this.jasperService = jasperService;
        this.messageSource = messageSource;
    }

    public Pair<byte[], String> generatePDF(Quote quote) {
        logger.debug(String.format("[%1$s] .....", "generatePDF"));
        logger.debug(String.format("quote is %1$s", quote.toString()));

        byte[] pdfContent = jasperService.exportPdf(JASPER_FILE, quote);
        return Pair.of(pdfContent, "proposal_" + quote.getQuoteId() + "_" + DateTimeUtil.formatNowForFilePath() + ".pdf");
    }

}
