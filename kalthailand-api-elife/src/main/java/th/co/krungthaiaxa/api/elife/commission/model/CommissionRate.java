package th.co.krungthaiaxa.api.elife.commission.model;

import th.co.krungthaiaxa.api.elife.products.ProductType;

/**
 * @author khoi.tran on 8/30/16.
 */
public class CommissionRate {
    private CommissionTarget commissionTarget;
    private String unitCode;
    /**
     * PlanCode is the combination productType and packageName (e.g. {@link ProductType#PRODUCT_IFINE}_{@link th.co.krungthaiaxa.api.elife.products.ProductIFinePackage#IFINE1}.
     * Most of the time it's equals to {@link ProductType#getName()}.
     */
    private String planCode;
    private CustomerCategory customerCategory;
    /**
     * Percentage from 0 to 100.
     */
    private double percentage;
}
