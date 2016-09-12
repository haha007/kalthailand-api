package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Data common to all quotes commercial types")
public class CommonData implements Serializable {
    private String productId;
    private String productName;
    private String productCurrency;
    @ApiModelProperty(required = true, value = "Some product has many packages. For example: iFine has packages iFine1, iFine2, iFine3..., iProtect has packages iProtect5, iProtect10, iProtect85.")
    private String packageName;
    private Integer nbOfYearsOfCoverage;
    private Integer nbOfYearsOfPremium;
    private Amount minPremium;
    private Amount maxPremium;
    private Amount minSumInsured;
    private Amount maxSumInsured;
    private Integer minAge;
    private Integer maxAge;

    @ApiModelProperty(required = true, value = "The unique identifier of the product specification the quote is " +
            "based on")
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * @return
     * @deprecated Please use productId and based on that get display name, don't use this field anymore.
     * We still keep it for compatible with old data.
     */
    @Deprecated
    @ApiModelProperty(required = true, value = "The name assigned to the product for marketing purposes")
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName
     * @deprecated view more at {@link #getProductName()}.
     */
    @Deprecated
    public void setProductName(String productName) {
        this.productName = productName;
    }

    @ApiModelProperty(required = true, value = "The default currency for the product")
    public String getProductCurrency() {
        return productCurrency;
    }

    public void setProductCurrency(String productCurrency) {
        this.productCurrency = productCurrency;
    }

    @ApiModelProperty(required = true, value = "The number of years of coverage for the product")
    public Integer getNbOfYearsOfCoverage() {
        return nbOfYearsOfCoverage;
    }

    public void setNbOfYearsOfCoverage(Integer nbOfYearsOfCoverage) {
        this.nbOfYearsOfCoverage = nbOfYearsOfCoverage;
    }

    @ApiModelProperty(required = true, value = "The number of years of premiums / payments expected for the product")
    public Integer getNbOfYearsOfPremium() {
        return nbOfYearsOfPremium;
    }

    public void setNbOfYearsOfPremium(Integer nbOfYearsOfPremium) {
        this.nbOfYearsOfPremium = nbOfYearsOfPremium;
    }

    @ApiModelProperty(required = true, value = "The minimum premium amount")
    public Amount getMinPremium() {
        return minPremium;
    }

    public void setMinPremium(Amount minPremium) {
        this.minPremium = minPremium;
    }

    @ApiModelProperty(required = true, value = "The maximum premium amount")
    public Amount getMaxPremium() {
        return maxPremium;
    }

    public void setMaxPremium(Amount maxPremium) {
        this.maxPremium = maxPremium;
    }

    @ApiModelProperty(required = true, value = "The minimum sum insured")
    public Amount getMinSumInsured() {
        return minSumInsured;
    }

    public void setMinSumInsured(Amount minSumInsured) {
        this.minSumInsured = minSumInsured;
    }

    @ApiModelProperty(required = true, value = "The maximum sum insured")
    public Amount getMaxSumInsured() {
        return maxSumInsured;
    }

    public void setMaxSumInsured(Amount maxSumInsured) {
        this.maxSumInsured = maxSumInsured;
    }

    @ApiModelProperty(required = true, value = "The minimum age to get coverage")
    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    @ApiModelProperty(required = true, value = "The maximum age to get coverage")
    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonData that = (CommonData) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(productName, that.productName) &&
                Objects.equals(nbOfYearsOfCoverage, that.nbOfYearsOfCoverage) &&
                Objects.equals(nbOfYearsOfPremium, that.nbOfYearsOfPremium) &&
                Objects.equals(minPremium, that.minPremium) &&
                Objects.equals(maxPremium, that.maxPremium) &&
                Objects.equals(minSumInsured, that.minSumInsured) &&
                Objects.equals(maxSumInsured, that.maxSumInsured) &&
                Objects.equals(minAge, that.minAge) &&
                Objects.equals(maxAge, that.maxAge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, nbOfYearsOfCoverage, nbOfYearsOfPremium, minPremium, maxPremium, minSumInsured, maxSumInsured, minAge, maxAge);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
