package th.co.krungthaiaxa.api.elife.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;

/**
 * In the future, we don't need separated premium rate classes for each product anymore (IEC10Rate, IGenRate...).
 * Everything can be stored in the same data structure like this.
 */
@Document(collection = "productPremiumRate")
@CompoundIndexes({
        @CompoundIndex(name = "uniquekeys_idx", unique = true, def = "{'productId': 1, 'packageName': 1, 'gender': 1, 'age': 1}")
})
public class ProductPremiumRate {
    @Id
    private String id;
    @Indexed
    private String productId;
    @Indexed
    private String packageName;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
