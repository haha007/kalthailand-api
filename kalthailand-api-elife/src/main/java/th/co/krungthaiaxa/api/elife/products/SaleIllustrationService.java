package th.co.krungthaiaxa.api.elife.products;

import org.apache.commons.lang3.tuple.Pair;
import th.co.krungthaiaxa.api.elife.model.Quote;

/**
 * @author khoi.tran on 10/3/16.
 */
public interface SaleIllustrationService {
    Pair<byte[], String> generatePDF(Quote quote);
}
