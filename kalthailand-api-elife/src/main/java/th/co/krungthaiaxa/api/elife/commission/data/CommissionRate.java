package th.co.krungthaiaxa.api.elife.commission.data;

import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import java.util.List;

/**
 * @author khoi.tran on 8/30/16.
 */
@Document(collection = "commissionRate")
public class CommissionRate {

    private String unitCode;
    /**
     * PlanCode is the combination productType and packageName (e.g. {@link ProductType#PRODUCT_IFINE}_{@link th.co.krungthaiaxa.api.elife.products.ProductIFinePackage#IFINE1}.
     * Most of the time it's equals to {@link ProductType#getName()}.
     */
    private String planCode;
    private CustomerCategory customerCategory;
    private List<CommissionTargetGroupRate> targetTypeRates;
}
