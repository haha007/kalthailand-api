package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.elife.api.model.enums.ProductIFinePackage;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "iFine Life Insurance specific Premiums Data")
public class ProductIFinePremium implements Serializable {
    private ProductIFinePackage productIFinePackage;
    private Amount sumInsured;
    private Amount accidentSumInsured;
    private Amount healthSumInsured;
    private Amount hospitalizationSumInsured;
    private Amount taxDeductible;
    private Amount nonTaxDeductible;
    private Double basicPremiumRate;
    private Double riderPremiumRate;
    private Double riskOccupationCharge;

    @ApiModelProperty(value = "iFine package name")
    public ProductIFinePackage getProductIFinePackage() {
        return productIFinePackage;
    }

    public void setProductIFinePackage(ProductIFinePackage productIFinePackage) {
        this.productIFinePackage = productIFinePackage;
    }

    @ApiModelProperty(value = "Total sum insured by the product. This is calculated by back end API based on chosen package")
    public Amount getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(Amount sumInsured) {
        this.sumInsured = sumInsured;
    }

    @ApiModelProperty(value = "Sum insured in case of accident. This is calculated by back end API based on chosen package.")
    public Amount getAccidentSumInsured() {
        return accidentSumInsured;
    }

    public void setAccidentSumInsured(Amount accidentSumInsured) {
        this.accidentSumInsured = accidentSumInsured;
    }

    @ApiModelProperty(value = "Sum insured in case of health disability. This is calculated by back end API based on chosen package.")
    public Amount getHealthSumInsured() {
        return healthSumInsured;
    }

    public void setHealthSumInsured(Amount healthSumInsured) {
        this.healthSumInsured = healthSumInsured;
    }

    @ApiModelProperty(value = "Sum insured in case of hospitalization. This is calculated by back end API based on chosen package.")
    public Amount getHospitalizationSumInsured() {
        return hospitalizationSumInsured;
    }

    public void setHospitalizationSumInsured(Amount hospitalizationSumInsured) {
        this.hospitalizationSumInsured = hospitalizationSumInsured;
    }

    @ApiModelProperty(value = "Deductible tax amount")
    public Amount getTaxDeductible() {
        return taxDeductible;
    }

    public void setTaxDeductible(Amount taxDeductible) {
        this.taxDeductible = taxDeductible;
    }

    @ApiModelProperty(value = "Non deductible tax amount")
    public Amount getNonTaxDeductible() {
        return nonTaxDeductible;
    }

    public void setNonTaxDeductible(Amount nonTaxDeductible) {
        this.nonTaxDeductible = nonTaxDeductible;
    }

    @ApiModelProperty(value = "Premium rate for basic")
    public Double getBasicPremiumRate() {
        return basicPremiumRate;
    }

    public void setBasicPremiumRate(Double basicPremiumRate) {
        this.basicPremiumRate = basicPremiumRate;
    }

    @ApiModelProperty(value = "Premium rate for rider")
    public Double getRiderPremiumRate() {
        return riderPremiumRate;
    }

    public void setRiderPremiumRate(Double riderPremiumRate) {
        this.riderPremiumRate = riderPremiumRate;
    }

    @ApiModelProperty(value = "Risk occupation charge")
    public Double getRiskOccupationCharge() {
        return riskOccupationCharge;
    }

    public void setRiskOccupationCharge(Double riskOccupationCharge) {
        this.riskOccupationCharge = riskOccupationCharge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductIFinePremium that = (ProductIFinePremium) o;
        return productIFinePackage == that.productIFinePackage &&
                Objects.equals(sumInsured, that.sumInsured) &&
                Objects.equals(taxDeductible, that.taxDeductible) &&
                Objects.equals(nonTaxDeductible, that.nonTaxDeductible) &&
                Objects.equals(basicPremiumRate, that.basicPremiumRate) &&
                Objects.equals(riderPremiumRate, that.riderPremiumRate) &&
                Objects.equals(riskOccupationCharge, that.riskOccupationCharge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productIFinePackage, sumInsured, taxDeductible, nonTaxDeductible, basicPremiumRate, riderPremiumRate, riskOccupationCharge);
    }
}
