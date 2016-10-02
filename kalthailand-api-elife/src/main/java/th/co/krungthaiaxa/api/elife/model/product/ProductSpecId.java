package th.co.krungthaiaxa.api.elife.model.product;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author khoi.tran on 9/30/16.
 *         It will be used as the cache key, so it's important to override the equals and hashCode.
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

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
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
