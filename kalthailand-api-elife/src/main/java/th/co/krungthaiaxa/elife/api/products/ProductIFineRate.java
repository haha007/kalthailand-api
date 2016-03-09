package th.co.krungthaiaxa.elife.api.products;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class ProductIFineRate {
    @Id
    private String id;
    @Indexed
    private String planName;
    private Boolean riskOccupation;
    private List<Double> taxDeductibleMaleRate;
    private List<Double> nonTaxDeductibleMaleRate;
    private List<Double> taxDeductibleFemaleRate;
    private List<Double> nonTaxDeductibleFemaleRate;

    public String getPlanName() {
        return planName;
    }

    public Boolean getRiskOccupation() {
        return riskOccupation;
    }

    public List<Double> getTaxDeductibleMaleRate() {
        return taxDeductibleMaleRate;
    }

    public List<Double> getNonTaxDeductibleMaleRate() {
        return nonTaxDeductibleMaleRate;
    }

    public List<Double> getTaxDeductibleFemaleRate() {
        return taxDeductibleFemaleRate;
    }

    public List<Double> getNonTaxDeductibleFemaleRate() {
        return nonTaxDeductibleFemaleRate;
    }
}
