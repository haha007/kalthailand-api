package th.co.krungthaiaxa.api.elife.service.ereceipt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.utils.base36.Base36Util;
import th.co.krungthaiaxa.api.elife.incremental.IncrementalService;

/**
 * @author khoi.tran on 10/28/16.
 */
@Service
public class EreceiptIncrementalService {
    private final static Logger LOGGER = LoggerFactory.getLogger(EreceiptIncrementalService.class);

    public static final String INCREMENTAL_KEY = "ERECEIPT_NUMBER";
    public static final String MAX_BASE36_VALUE = "zzzzzz";
    public static final long MAX_DECIMAL_VALUE = Base36Util.toDecimalLong(MAX_BASE36_VALUE);

    private final IncrementalService incrementalService;
    private final EreceiptOldNumberCollectionService ereceiptOldNumberCollectionService;

    @Autowired
    public EreceiptIncrementalService(IncrementalService incrementalService, EreceiptOldNumberCollectionService ereceiptOldNumberCollectionService) {
        this.incrementalService = incrementalService;
        this.ereceiptOldNumberCollectionService = ereceiptOldNumberCollectionService;
    }

    public String nextBase36Value() {
        boolean needNewDecimal = true;
        long nextDecimal = -1;
        while (needNewDecimal) {
            nextDecimal = incrementalService.next(INCREMENTAL_KEY);
            if (nextDecimal > MAX_DECIMAL_VALUE) {
                throw new UnexpectedException(String.format("Cannot increase incremental number for EreceiptNumber anymore because it reach the maximum value (%s). So it will be reset to 1", nextDecimal));
            }
            needNewDecimal = ereceiptOldNumberCollectionService.checkDuplicateIncrementalInOldData(nextDecimal);
        }
        return Base36Util.toBase36String(nextDecimal);
    }

}
