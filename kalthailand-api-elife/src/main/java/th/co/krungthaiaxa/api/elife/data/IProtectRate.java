package th.co.krungthaiaxa.api.elife.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;

/**
 * This class is different from {@link IProtectRate}.
 * This is the data related to specific customer, while {@link IProtectRate} is pre-defined data of product.
 */
@Document(collection = "productIProtectRate")
public class IProtectRate {
    @Id
    private String id;
    @Indexed
    private IProtectPackage packageName;
    @Indexed
    private GenderCode gender;
    @Indexed
    private Integer age;
    private Double premiumRate;

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

    public GenderCode getGender() {
        return gender;
    }

    public void setGender(GenderCode gender) {
        this.gender = gender;
    }

    public Double getPremiumRate() {
        return premiumRate;
    }

    public void setPremiumRate(Double premiumRate) {
        this.premiumRate = premiumRate;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
