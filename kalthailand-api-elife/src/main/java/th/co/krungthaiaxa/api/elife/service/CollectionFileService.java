package th.co.krungthaiaxa.api.elife.service;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;

import java.util.function.Function;

/**
 * @author khoi.tran on 11/30/16.
 */
@Service
public class CollectionFileService {

    public final static String COLLECTION_FILE_SHEET_NAME = "LFDISC6";
    public final static Integer COLLECTION_FILE_NUMBER_OF_COLUMNS = 6;
    public final static String COLLECTION_FILE_COLUMN_NAME_1 = "M92DOC6";
    public final static String COLLECTION_FILE_COLUMN_NAME_2 = "M92BANK6";
    public final static String COLLECTION_FILE_COLUMN_NAME_3 = "M92BKCD6";
    public final static String COLLECTION_FILE_COLUMN_NAME_4 = "M92PNO6";
    public final static String COLLECTION_FILE_COLUMN_NAME_5 = "M92PMOD6";
    public final static String COLLECTION_FILE_COLUMN_NAME_6 = "M92PRM6";

    public static final Function<PeriodicityCode, String> PAYMENT_MODE = periodicityCode -> {
        if (periodicityCode.equals(PeriodicityCode.EVERY_YEAR)) {
            return "A";
        } else if (periodicityCode.equals(PeriodicityCode.EVERY_HALF_YEAR)) {
            return "S";
        } else if (periodicityCode.equals(PeriodicityCode.EVERY_QUARTER)) {
            return "Q";
        } else {
            return "M";
        }
    };
}
