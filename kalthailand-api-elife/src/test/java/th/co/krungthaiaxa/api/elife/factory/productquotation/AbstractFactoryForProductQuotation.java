package th.co.krungthaiaxa.api.elife.factory.productquotation;

import th.co.krungthaiaxa.api.elife.model.enums.AtpMode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;

/**
 * @author khoi.tran on 11/9/16.
 */
public abstract class AbstractFactoryForProductQuotation {
    public abstract ProductQuotation constructDefault();

    public abstract ProductQuotation constructDefault(PeriodicityCode periodicityCode, AtpMode atpMode);

//    public abstract ProductQuotation constructDefault(double paymentAmount, boolean isSumInsured, PeriodicityCode periodicityCode, AtpMode atpMode);
}
