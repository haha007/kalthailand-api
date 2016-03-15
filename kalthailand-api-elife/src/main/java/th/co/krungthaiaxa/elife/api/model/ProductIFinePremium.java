package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import th.co.krungthaiaxa.elife.api.model.enums.ProductIFinePackage;

import java.io.Serializable;

@ApiModel(description = "iFine Life Insurance specific Premiums Data")
public class ProductIFinePremium implements Serializable {
    private ProductIFinePackage productIFinePackage;
    private Amount sumInsured;
    private Amount taxDeductible;
    private Amount nonTaxDeductible;
    private Double basicPremiumRate;
    private Double riderPremiumRate;
    private Double riskOccupationCharge;

    public ProductIFinePackage getProductIFinePackage() {
        return productIFinePackage;
    }

    public void setProductIFinePackage(ProductIFinePackage productIFinePackage) {
        this.productIFinePackage = productIFinePackage;
    }

    public Amount getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(Amount sumInsured) {
        this.sumInsured = sumInsured;
    }

    public Amount getTaxDeductible() {
        return taxDeductible;
    }

    public void setTaxDeductible(Amount taxDeductible) {
        this.taxDeductible = taxDeductible;
    }

    public Amount getNonTaxDeductible() {
        return nonTaxDeductible;
    }

    public void setNonTaxDeductible(Amount nonTaxDeductible) {
        this.nonTaxDeductible = nonTaxDeductible;
    }

    public Double getBasicPremiumRate() {
        return basicPremiumRate;
    }

    public void setBasicPremiumRate(Double basicPremiumRate) {
        this.basicPremiumRate = basicPremiumRate;
    }

    public Double getRiderPremiumRate() {
        return riderPremiumRate;
    }

    public void setRiderPremiumRate(Double riderPremiumRate) {
        this.riderPremiumRate = riderPremiumRate;
    }

    public Double getRiskOccupationCharge() {
        return riskOccupationCharge;
    }

    public void setRiskOccupationCharge(Double riskOccupationCharge) {
        this.riskOccupationCharge = riskOccupationCharge;
    }
}
