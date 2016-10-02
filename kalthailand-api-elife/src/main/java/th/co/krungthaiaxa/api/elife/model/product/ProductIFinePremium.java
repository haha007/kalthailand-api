package th.co.krungthaiaxa.api.elife.model.product;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.products.ProductIFinePackage;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "iFine Life Insurance specific Premiums Data")
public class ProductIFinePremium implements Serializable {
    private ProductIFinePackage productIFinePackage;
    private Double basicPremiumRate;
    private Double riderPremiumRate;
    private Double riskOccupationCharge;
    private Amount sumInsured;
    private Amount accidentSumInsured;
    private Amount healthSumInsured;
    private Amount hospitalizationSumInsured;
    private Amount taxDeductible;
    private Amount nonTaxDeductible;
    private Amount deathByAccident;
    private Amount deathByAccidentInPublicTransport;
    private Amount disabilityFromAccidentMin;
    private Amount disabilityFromAccidentMax;
    private Amount lossOfHandOrLeg;
    private Amount lossOfSight;
    private Amount lossOfHearingMin;
    private Amount lossOfHearingMax;
    private Amount lossOfSpeech;
    private Amount lossOfCorneaForBothEyes;
    private Amount lossOfFingersMin;
    private Amount lossOfFingersMax;
    private Amount noneCurableBoneFracture;
    private Amount legsShortenBy5cm;
    private Amount burnInjuryMin;
    private Amount burnInjuryMax;
    private Amount medicalCareCost;

    @ApiModelProperty(value = "iFine package name")
    public ProductIFinePackage getProductIFinePackage() {
        return productIFinePackage;
    }

    public void setProductIFinePackage(ProductIFinePackage productIFinePackage) {
        this.productIFinePackage = productIFinePackage;
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

    @ApiModelProperty(value = "Death by accident")
    public Amount getDeathByAccident() {
        return deathByAccident;
    }

    public void setDeathByAccident(Amount deathByAccident) {
        this.deathByAccident = deathByAccident;
    }

    @ApiModelProperty(value = "Death by Accidents in Public Places or Public Transportation (e.g. Cinema, Bus, Train, etc.)")
    public Amount getDeathByAccidentInPublicTransport() {
        return deathByAccidentInPublicTransport;
    }

    public void setDeathByAccidentInPublicTransport(Amount deathByAccidentInPublicTransport) {
        this.deathByAccidentInPublicTransport = deathByAccidentInPublicTransport;
    }

    @ApiModelProperty(value = "Minimum amount for physical disabilities benefit from accidents")
    public Amount getDisabilityFromAccidentMin() {
        return disabilityFromAccidentMin;
    }

    public void setDisabilityFromAccidentMin(Amount disabilityFromAccidentMin) {
        this.disabilityFromAccidentMin = disabilityFromAccidentMin;
    }

    @ApiModelProperty(value = "Maximum amount for physical disabilities benefit from accidents")
    public Amount getDisabilityFromAccidentMax() {
        return disabilityFromAccidentMax;
    }

    public void setDisabilityFromAccidentMax(Amount disabilityFromAccidentMax) {
        this.disabilityFromAccidentMax = disabilityFromAccidentMax;
    }

    @ApiModelProperty(value = "Loss of use of hands or legs (1 side or both sides)")
    public Amount getLossOfHandOrLeg() {
        return lossOfHandOrLeg;
    }

    public void setLossOfHandOrLeg(Amount lossOfHandOrLeg) {
        this.lossOfHandOrLeg = lossOfHandOrLeg;
    }

    @ApiModelProperty(value = "Loss of ability to see (1 eye or both eyes)")
    public Amount getLossOfSight() {
        return lossOfSight;
    }

    public void setLossOfSight(Amount lossOfSight) {
        this.lossOfSight = lossOfSight;
    }

    @ApiModelProperty(value = "Minimum amount for Loss of ability to hear")
    public Amount getLossOfHearingMin() {
        return lossOfHearingMin;
    }

    public void setLossOfHearingMin(Amount lossOfHearingMin) {
        this.lossOfHearingMin = lossOfHearingMin;
    }

    @ApiModelProperty(value = "Maximum amount for Loss of ability to hear")
    public Amount getLossOfHearingMax() {
        return lossOfHearingMax;
    }

    public void setLossOfHearingMax(Amount lossOfHearingMax) {
        this.lossOfHearingMax = lossOfHearingMax;
    }

    @ApiModelProperty(value = "Loss of ability to speak")
    public Amount getLossOfSpeech() {
        return lossOfSpeech;
    }

    public void setLossOfSpeech(Amount lossOfSpeech) {
        this.lossOfSpeech = lossOfSpeech;
    }

    @ApiModelProperty(value = "Loss of cornea for both eyes")
    public Amount getLossOfCorneaForBothEyes() {
        return lossOfCorneaForBothEyes;
    }

    public void setLossOfCorneaForBothEyes(Amount lossOfCorneaForBothEyes) {
        this.lossOfCorneaForBothEyes = lossOfCorneaForBothEyes;
    }

    @ApiModelProperty(value = "Minimum amount for Loss of hand fingers or foot fingers (depend on number of joints)")
    public Amount getLossOfFingersMin() {
        return lossOfFingersMin;
    }

    public void setLossOfFingersMin(Amount lossOfFingersMin) {
        this.lossOfFingersMin = lossOfFingersMin;
    }

    @ApiModelProperty(value = "Maximum amount for Loss of hand fingers or foot fingers (depend on number of joints)")
    public Amount getLossOfFingersMax() {
        return lossOfFingersMax;
    }

    public void setLossOfFingersMax(Amount lossOfFingersMax) {
        this.lossOfFingersMax = lossOfFingersMax;
    }

    @ApiModelProperty(value = "Leg bones or kneecap fracture and unable to be cured")
    public Amount getNoneCurableBoneFracture() {
        return noneCurableBoneFracture;
    }

    public void setNoneCurableBoneFracture(Amount noneCurableBoneFracture) {
        this.noneCurableBoneFracture = noneCurableBoneFracture;
    }

    @ApiModelProperty(value = "Leg(s) is shorten at least 5 cm")
    public Amount getLegsShortenBy5cm() {
        return legsShortenBy5cm;
    }

    public void setLegsShortenBy5cm(Amount legsShortenBy5cm) {
        this.legsShortenBy5cm = legsShortenBy5cm;
    }

    @ApiModelProperty(value = "Minimum amount for Burn Injury (more than 2% of whole body)")
    public Amount getBurnInjuryMin() {
        return burnInjuryMin;
    }

    public void setBurnInjuryMin(Amount burnInjuryMin) {
        this.burnInjuryMin = burnInjuryMin;
    }

    @ApiModelProperty(value = "Maximum amount for Burn Injury (more than 2% of whole body)")
    public Amount getBurnInjuryMax() {
        return burnInjuryMax;
    }

    public void setBurnInjuryMax(Amount burnInjuryMax) {
        this.burnInjuryMax = burnInjuryMax;
    }

    @ApiModelProperty(value = "Cost of medical care from accidents (per each time)")
    public Amount getMedicalCareCost() {
        return medicalCareCost;
    }

    public void setMedicalCareCost(Amount medicalCareCost) {
        this.medicalCareCost = medicalCareCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductIFinePremium that = (ProductIFinePremium) o;
        return productIFinePackage == that.productIFinePackage &&
                Objects.equals(basicPremiumRate, that.basicPremiumRate) &&
                Objects.equals(riderPremiumRate, that.riderPremiumRate) &&
                Objects.equals(riskOccupationCharge, that.riskOccupationCharge) &&
                Objects.equals(sumInsured, that.sumInsured) &&
                Objects.equals(accidentSumInsured, that.accidentSumInsured) &&
                Objects.equals(healthSumInsured, that.healthSumInsured) &&
                Objects.equals(hospitalizationSumInsured, that.hospitalizationSumInsured) &&
                Objects.equals(taxDeductible, that.taxDeductible) &&
                Objects.equals(nonTaxDeductible, that.nonTaxDeductible) &&
                Objects.equals(deathByAccident, that.deathByAccident) &&
                Objects.equals(deathByAccidentInPublicTransport, that.deathByAccidentInPublicTransport) &&
                Objects.equals(disabilityFromAccidentMin, that.disabilityFromAccidentMin) &&
                Objects.equals(disabilityFromAccidentMax, that.disabilityFromAccidentMax) &&
                Objects.equals(lossOfHandOrLeg, that.lossOfHandOrLeg) &&
                Objects.equals(lossOfSight, that.lossOfSight) &&
                Objects.equals(lossOfHearingMin, that.lossOfHearingMin) &&
                Objects.equals(lossOfHearingMax, that.lossOfHearingMax) &&
                Objects.equals(lossOfSpeech, that.lossOfSpeech) &&
                Objects.equals(lossOfCorneaForBothEyes, that.lossOfCorneaForBothEyes) &&
                Objects.equals(lossOfFingersMin, that.lossOfFingersMin) &&
                Objects.equals(lossOfFingersMax, that.lossOfFingersMax) &&
                Objects.equals(noneCurableBoneFracture, that.noneCurableBoneFracture) &&
                Objects.equals(legsShortenBy5cm, that.legsShortenBy5cm) &&
                Objects.equals(burnInjuryMin, that.burnInjuryMin) &&
                Objects.equals(burnInjuryMax, that.burnInjuryMax) &&
                Objects.equals(medicalCareCost, that.medicalCareCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productIFinePackage, basicPremiumRate, riderPremiumRate, riskOccupationCharge, sumInsured, accidentSumInsured, healthSumInsured, hospitalizationSumInsured, taxDeductible, nonTaxDeductible, deathByAccident, deathByAccidentInPublicTransport, disabilityFromAccidentMin, disabilityFromAccidentMax, lossOfHandOrLeg, lossOfSight, lossOfHearingMin, lossOfHearingMax, lossOfSpeech, lossOfCorneaForBothEyes, lossOfFingersMin, lossOfFingersMax, noneCurableBoneFracture, legsShortenBy5cm, burnInjuryMin, burnInjuryMax, medicalCareCost);
    }
}
