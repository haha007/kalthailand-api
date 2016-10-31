package th.co.krungthaiaxa.api.elife.service.initiation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author khoi.tran on 9/30/16.
 *         Note: never put in in the common module. Otherwise, the InitDataService will run many times.
 *         Only put it inside api-elife because we need only one initiate data service running.
 */
@Service
public class InitDataService {
    @Autowired
    private InitProduct initProduct;

    @PostConstruct
    public void createInitDataIfNecessary() {
        initProduct.createInitData();
    }
}
