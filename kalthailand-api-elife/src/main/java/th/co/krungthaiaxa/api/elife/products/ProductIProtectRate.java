package th.co.krungthaiaxa.api.elife.products;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class ProductIProtectRate {
    @Id
    private String id;
    @Indexed
    private ProductIProtectPackage packageName;
    private String gender;
    private List<Double> taxDeductibleRate;
    private List<Double> nonTaxDeductibleRate;
    private List<Double> nonTaxDeductibleRiskRate;

    public ProductIProtectPackage getPackageName() {
        return packageName;
    }

    public String getGender() {
        return gender;
    }

    public List<Double> getTaxDeductibleRate() {
        return taxDeductibleRate;
    }

    public List<Double> getNonTaxDeductibleRate() {
        return nonTaxDeductibleRate;
    }

    public List<Double> getNonTaxDeductibleRiskRate() {
        return nonTaxDeductibleRiskRate;
    }
}
