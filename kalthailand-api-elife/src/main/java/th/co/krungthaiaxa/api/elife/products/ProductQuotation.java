package th.co.krungthaiaxa.api.elife.products;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.iprotect.IProtectPackage;

import java.time.LocalDate;

/**
 * In order to create a quotation, we need to know the input of customer (productType, dateOfBirth, gender, occupation...).
 * So this class will encapsulate the input of user in order to create the quotation.
 */
@ApiModel(description = "Data concerning quote for a product")
public class ProductQuotation {
    private ProductType productType;
    private LocalDate dateOfBirth;
    private GenderCode genderCode;

    /**
     * A customer can input either premiumAmount or sumInsuredAmount.
     * When he inputs premiumAmount, we will calculate sumInsuredAmount (based on premiumAmount).
     * If he inputs sumInsuredAmount, we will calculate premiumAmount (based on sumInsuredAmount)
     */
    private Amount premiumAmount;
    private Amount sumInsuredAmount;
    private PeriodicityCode periodicityCode;
    private Integer nbOfYearsOfPayment;
    private Integer occupationId;
    /**
     * This is the name in one of following enums {@link ProductIFinePackage} or {@link IProtectPackage}
     */
    private String packageName;
    private Integer declaredTaxPercentAtSubscription;

    @ApiModelProperty(value = "The product type")
    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    @ApiModelProperty(value = "The date of birth of insured")
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @ApiModelProperty(value = "The gender of insured")
    public GenderCode getGenderCode() {
        return genderCode;
    }

    public void setGenderCode(GenderCode genderCode) {
        this.genderCode = genderCode;
    }

    @ApiModelProperty(value = "The premium amount")
    public Amount getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(Amount premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    @ApiModelProperty(value = "The sum insured amount")
    public Amount getSumInsuredAmount() {
        return sumInsuredAmount;
    }

    public void setSumInsuredAmount(Amount sumInsuredAmount) {
        this.sumInsuredAmount = sumInsuredAmount;
    }

    @ApiModelProperty(value = "The payment periodicity")
    public PeriodicityCode getPeriodicityCode() {
        return periodicityCode;
    }

    public void setPeriodicityCode(PeriodicityCode periodicityCode) {
        this.periodicityCode = periodicityCode;
    }

    @ApiModelProperty(value = "The plan duration (in years)")
    public Integer getNbOfYearsOfPayment() {
        return nbOfYearsOfPayment;
    }

    public void setNbOfYearsOfPayment(Integer nbOfYearsOfPayment) {
        this.nbOfYearsOfPayment = nbOfYearsOfPayment;
    }

    @ApiModelProperty(value = "The insured occupation Id")
    public Integer getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(Integer occupationId) {
        this.occupationId = occupationId;
    }

    @ApiModelProperty(value = "The product name (such as IFINE1, IFINE2, ...")
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @ApiModelProperty(value = "The tax percent paid at subscruption")
    public Integer getDeclaredTaxPercentAtSubscription() {
        return declaredTaxPercentAtSubscription;
    }

    public void setDeclaredTaxPercentAtSubscription(Integer declaredTaxPercentAtSubscription) {
        this.declaredTaxPercentAtSubscription = declaredTaxPercentAtSubscription;
    }
}
