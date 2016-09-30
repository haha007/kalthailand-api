package th.co.krungthaiaxa.api.elife.model;

/**
 * @author khoi.tran on 9/30/16.
 */
public class ProductSpecId {
    private String productLogicName;
    private String productPackageName;

    public ProductSpecId() {
    }

    public ProductSpecId(String productLogicName, String productPackageName) {
        this.productLogicName = productLogicName;
        this.productPackageName = productPackageName;
    }

    public String getProductPackageName() {
        return productPackageName;
    }

    public void setProductPackageName(String productPackageName) {
        this.productPackageName = productPackageName;
    }

    public String getProductLogicName() {
        return productLogicName;
    }

    public void setProductLogicName(String productLogicName) {
        this.productLogicName = productLogicName;
    }
}
