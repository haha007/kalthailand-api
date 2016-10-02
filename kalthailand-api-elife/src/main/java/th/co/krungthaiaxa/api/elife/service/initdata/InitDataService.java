package th.co.krungthaiaxa.api.elife.service.initdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author khoi.tran on 9/30/16.
 */
@Service
public class InitDataService {
    @Autowired
    private InitProduct initProduct;

    public void createInitDataIfNecessary() {
        initProduct.createInitData();
    }
}
