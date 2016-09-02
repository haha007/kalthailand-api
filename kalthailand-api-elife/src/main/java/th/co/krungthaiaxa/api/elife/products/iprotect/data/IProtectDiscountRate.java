package th.co.krungthaiaxa.api.elife.products.iprotect.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "productIProtectDiscountRate")
public class IProtectDiscountRate {
    @Id
    private String id;
    @Indexed
    private IProtectPackage packageName;
    /**
     * This is the sumInsured before discount
     */
    private double sumInsured;
    private double discountRate;

    public IProtectPackage getPackageName() {
        return packageName;
    }

    public void setPackageName(IProtectPackage packageName) {
        this.packageName = packageName;
    }

    public double getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(double sumInsured) {
        this.sumInsured = sumInsured;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
