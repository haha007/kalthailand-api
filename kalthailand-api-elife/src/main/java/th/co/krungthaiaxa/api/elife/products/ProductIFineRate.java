package th.co.krungthaiaxa.api.elife.products;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "productIFineRate")
public class ProductIFineRate {
    @Id
    private String id;
    @Indexed
    private String planName;
    private String gender;
    private List<Double> taxDeductibleRate;
    private List<Double> nonTaxDeductibleRate;
    private List<Double> nonTaxDeductibleRiskRate;

    public String getPlanName() {
        return planName;
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
