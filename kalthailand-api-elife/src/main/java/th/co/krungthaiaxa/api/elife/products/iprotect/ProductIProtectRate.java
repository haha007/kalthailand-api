package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This class is different from {@link IProtectPredefinedRate}.
 * This is the data related to specific customer, while {@link IProtectPredefinedRate} is pre-defined data of product.
 */
@Document(collection = "productIProtectRate")
public class ProductIProtectRate {
    @Id
    private String id;
    @Indexed
    private IProtectPackage packageName;
    @Indexed
    private String gender;

    private Double rate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IProtectPackage getPackageName() {
        return packageName;
    }

    public void setPackageName(IProtectPackage packageName) {
        this.packageName = packageName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}
