package th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.data.CustomerAnniversary20;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.model.CustomerAnniversary20Form;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.repository.CustomerAnniversary20Repository;

/**
 * @author khoi.tran on 12/26/16.
 */
@Service
public class CustomerAnniversary20Service {
    private final BeanValidator beanValidator;
    private final CustomerAnniversary20Repository customerAnniversary20Repository;

    @Autowired
    public CustomerAnniversary20Service(BeanValidator beanValidator,
                                        CustomerAnniversary20Repository customerAnniversary20Repository) {
        this.beanValidator = beanValidator;
        this.customerAnniversary20Repository = customerAnniversary20Repository;
    }

    public CustomerAnniversary20 createCustomerAnniversary20(CustomerAnniversary20Form customerAnniversary20Form) {
        beanValidator.validate(customerAnniversary20Form);
        final CustomerAnniversary20 persistentData = new CustomerAnniversary20(customerAnniversary20Form);

        return customerAnniversary20Repository.save(persistentData);
    }
}
