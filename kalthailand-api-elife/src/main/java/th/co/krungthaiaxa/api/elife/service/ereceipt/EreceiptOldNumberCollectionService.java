package th.co.krungthaiaxa.api.elife.service.ereceipt;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author khoi.tran on 10/28/16.
 */
@Service
public class EreceiptOldNumberCollectionService {
    @PostConstruct
    private void collectAllOldEreceiptNumbers() {

    }

    /**
     * @param ereceiptFullNumberBase36 the full number in base36 format. (Doesn't include {@link EreceiptPdfService#ERECEIPT_NUMBER_PREFIX}).
     * @return
     */
    public boolean checkDuplicateIncrementalInOldData(String ereceiptFullNumberBase36) {
        //TODO
        return false;
    }
}
