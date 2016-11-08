package th.co.krungthaiaxa.api.elife.model.product;

import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;

/**
 * @author khoi.tran on 9/30/16.
 */
public class ProductSpec {
    private String productLogicName;
    private String packageName;

    /**
     * View more at {@link ProductUtils#CURRENCY_THB}
     */
    private String productCurrency;
    /**
     * This is the insured age when calculating quotation.
     */
    private Integer insuredAgeMin;
    private Integer insuredAgeMax;
    /**
     * This is the maximum insured age which can be covered (protected) in the future.
     */
    private Integer insuredCoverageAgeMax;
    private Integer insuredCoverageYears;
    private Integer insuredPaymentYears;
    private Double taxDeductionPerYearMax;
    /**
     * If it's null, then it will be calculated from premiumMin
     */
    private Amount sumInsuredMin;
    private Amount sumInsuredMax;
    /**
     * If it's null, then it will be calculated from sumInsuredMin
     */
    private Amount premiumMin;
    private Amount premiumMax;
    /**
     * If this value is MONTH, then premiumMin is the minimum of premium in a MONTH.
     * If this value is YEAR, then premiumMin is the minimum of premium in a YEAR.
     * ...
     */
    private PeriodicityCode premiumLimitsPeriodicityCode;
    private boolean samePremiumRateAllAges;
    private boolean samePremiumRateAllGender;

    //TODO refactor: should load from DB.
    /**
     * Should be loaded from DB.
     */
    private Double dividendRateInNormalYear;
    private Double dividendRateInLastYear;
    private Double dividendInterestRateForEndOfContract;
    private Double dividendInterestRateForAnnual;

    public String getProductLogicName() {
        return productLogicName;
    }

    public void setProductLogicName(String productLogicName) {
        this.productLogicName = productLogicName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProductCurrency() {
        return productCurrency;
    }

    public void setProductCurrency(String productCurrency) {
        this.productCurrency = productCurrency;
    }

    public Integer getInsuredAgeMin() {
        return insuredAgeMin;
    }

    public void setInsuredAgeMin(Integer insuredAgeMin) {
        this.insuredAgeMin = insuredAgeMin;
    }

    public Integer getInsuredAgeMax() {
        return insuredAgeMax;
    }

    public void setInsuredAgeMax(Integer insuredAgeMax) {
        this.insuredAgeMax = insuredAgeMax;
    }

    public Integer getInsuredCoverageAgeMax() {
        return insuredCoverageAgeMax;
    }

    public void setInsuredCoverageAgeMax(Integer insuredCoverageAgeMax) {
        this.insuredCoverageAgeMax = insuredCoverageAgeMax;
    }

    public Integer getInsuredCoverageYears() {
        return insuredCoverageYears;
    }

    public void setInsuredCoverageYears(Integer insuredCoverageYears) {
        this.insuredCoverageYears = insuredCoverageYears;
    }

    public Integer getInsuredPaymentYears() {
        return insuredPaymentYears;
    }

    public void setInsuredPaymentYears(Integer insuredPaymentYears) {
        this.insuredPaymentYears = insuredPaymentYears;
    }

    public Double getTaxDeductionPerYearMax() {
        return taxDeductionPerYearMax;
    }

    public void setTaxDeductionPerYearMax(Double taxDeductionPerYearMax) {
        this.taxDeductionPerYearMax = taxDeductionPerYearMax;
    }

    public Amount getSumInsuredMin() {
        return sumInsuredMin;
    }

    public void setSumInsuredMin(Amount sumInsuredMin) {
        this.sumInsuredMin = sumInsuredMin;
    }

    public Amount getSumInsuredMax() {
        return sumInsuredMax;
    }

    public void setSumInsuredMax(Amount sumInsuredMax) {
        this.sumInsuredMax = sumInsuredMax;
    }

    public Amount getPremiumMin() {
        return premiumMin;
    }

    public void setPremiumMin(Amount premiumMin) {
        this.premiumMin = premiumMin;
    }

    public Amount getPremiumMax() {
        return premiumMax;
    }

    public void setPremiumMax(Amount premiumMax) {
        this.premiumMax = premiumMax;
    }

    public Double getDividendRateInNormalYear() {
        return dividendRateInNormalYear;
    }

    public void setDividendRateInNormalYear(Double dividendRateInNormalYear) {
        this.dividendRateInNormalYear = dividendRateInNormalYear;
    }

    public Double getDividendRateInLastYear() {
        return dividendRateInLastYear;
    }

    public void setDividendRateInLastYear(Double dividendRateInLastYear) {
        this.dividendRateInLastYear = dividendRateInLastYear;
    }

    public Double getDividendInterestRateForEndOfContract() {
        return dividendInterestRateForEndOfContract;
    }

    public void setDividendInterestRateForEndOfContract(Double dividendInterestRateForEndOfContract) {
        this.dividendInterestRateForEndOfContract = dividendInterestRateForEndOfContract;
    }

    public Double getDividendInterestRateForAnnual() {
        return dividendInterestRateForAnnual;
    }

    public void setDividendInterestRateForAnnual(Double dividendInterestRateForAnnual) {
        this.dividendInterestRateForAnnual = dividendInterestRateForAnnual;
    }

    public PeriodicityCode getPremiumLimitsPeriodicityCode() {
        return premiumLimitsPeriodicityCode;
    }

    public void setPremiumLimitsPeriodicityCode(PeriodicityCode premiumLimitsPeriodicityCode) {
        this.premiumLimitsPeriodicityCode = premiumLimitsPeriodicityCode;
    }

    public boolean isSamePremiumRateAllAges() {
        return samePremiumRateAllAges;
    }

    public void setSamePremiumRateAllAges(boolean samePremiumRateAllAges) {
        this.samePremiumRateAllAges = samePremiumRateAllAges;
    }

    public boolean isSamePremiumRateAllGender() {
        return samePremiumRateAllGender;
    }

    public void setSamePremiumRateAllGender(boolean samePremiumRateAllGender) {
        this.samePremiumRateAllGender = samePremiumRateAllGender;
    }
}
