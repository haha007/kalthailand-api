package th.co.krungthaiaxa.api.elife.service.initdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.service.migratedata.PremiumsDataMigrationService;

/**
 * @author khoi.tran on 9/30/16.
 */
@Service
public class InitDataService {
    @Autowired
    private PremiumsDataMigrationService premiumsDataMigrationService;
    @Autowired
    private InitProduct initProduct;

    public void createInitDataIfNecessary() {
        initProduct.createInitData();
        premiumsDataMigrationService.migrateData();
    }
}
