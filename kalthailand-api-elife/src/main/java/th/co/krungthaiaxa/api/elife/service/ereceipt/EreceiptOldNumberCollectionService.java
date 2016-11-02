package th.co.krungthaiaxa.api.elife.service.ereceipt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.service.migration.EreceiptOldNumberGenerationService;

import javax.annotation.PostConstruct;

/**
 * @author khoi.tran on 10/28/16.
 */
@Service
public class EreceiptOldNumberCollectionService {
    //TODO we can remove this injection after Running the OldReceiptNumber in the first time.
    //This injection will make sure that the oldReceiptNumber will be generated before this class is constructed.
    @Autowired
    private final EreceiptOldNumberGenerationService ereceiptOldNumberGenerationService;

    public EreceiptOldNumberCollectionService(EreceiptOldNumberGenerationService ereceiptOldNumberGenerationService) {this.ereceiptOldNumberGenerationService = ereceiptOldNumberGenerationService;}

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
