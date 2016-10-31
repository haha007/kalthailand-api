package th.co.krungthaiaxa.api.elife.service.initiation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.products.igen.IGenInitDataService;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectInitDataService;

/**
 * @author khoi.tran on 9/30/16.
 */
@Service
public class InitProduct {
    private final IProtectInitDataService iProtectInitDataService;
    private final IGenInitDataService iGenInitDataService;

    @Autowired
    public InitProduct(IProtectInitDataService iProtectInitDataService, IGenInitDataService iGenInitDataService) {
        this.iProtectInitDataService = iProtectInitDataService;
        this.iGenInitDataService = iGenInitDataService;
    }

    public void createInitData() {
        iProtectInitDataService.initData();
        iGenInitDataService.initData();
    }
}
