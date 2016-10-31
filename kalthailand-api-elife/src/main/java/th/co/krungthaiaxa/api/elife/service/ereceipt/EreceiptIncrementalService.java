package th.co.krungthaiaxa.api.elife.service.ereceipt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.common.utils.base36.Base36Util;
import th.co.krungthaiaxa.api.elife.incremental.IncrementalService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author khoi.tran on 10/28/16.
 */
@Service
public class EreceiptIncrementalService {
    public static final Logger LOGGER = LoggerFactory.getLogger(EreceiptIncrementalService.class);
    public static final int MAX_RETRY_FINDING_NEXT_RECEIPT_NUMBER = 100;
    public static final String INCREMENTAL_KEY = "ERECEIPT_NUMBER";
    //In eReceipt pdf, there's maximum 6 digit.
    public static final String MAX_MAIN_NUMBER_BASE36 = "zzzzzz";
    public static final long MAX_MAIN_NUMBER_DECIMAL = Base36Util.toDecimalLong(MAX_MAIN_NUMBER_BASE36);

    private final IncrementalService incrementalService;
    private final EreceiptOldNumberCollectionService ereceiptOldNumberCollectionService;

    @Autowired
    public EreceiptIncrementalService(IncrementalService incrementalService, EreceiptOldNumberCollectionService ereceiptOldNumberCollectionService) {
        this.incrementalService = incrementalService;
        this.ereceiptOldNumberCollectionService = ereceiptOldNumberCollectionService;
    }

    public String nextBase36Value() {
        List<Long> foundReceiptNumbers = new ArrayList<>();
        boolean needNewDecimal = true;
        int retryFindingNextReceiptNumber = 0;
        long nextDecimal = -1;
        while (needNewDecimal) {
            retryFindingNextReceiptNumber++;
            if (retryFindingNextReceiptNumber > MAX_RETRY_FINDING_NEXT_RECEIPT_NUMBER) {
                String msg = String.format("Cannot find the new receipt number after retry %s times. It's was always duplicated to existing receipt numbers. Please recheck the code. The found receiptNumbers: \n %s", retryFindingNextReceiptNumber, ObjectMapperUtil
                        .toSimpleStringMultiLineForEachElement(foundReceiptNumbers));
                throw new UnexpectedException(msg);
            }
            nextDecimal = incrementalService.next(INCREMENTAL_KEY);
            foundReceiptNumbers.add(nextDecimal);
            if (nextDecimal > MAX_MAIN_NUMBER_DECIMAL) {
                throw new UnexpectedException(String.format("Cannot increase incremental number for EreceiptNumber anymore because it reach the maximum value (%s). So it will be reset to 1", nextDecimal));
            }
            needNewDecimal = ereceiptOldNumberCollectionService.checkDuplicateIncrementalInOldData(nextDecimal);

        }
        return Base36Util.toBase36String(nextDecimal);
    }

}
