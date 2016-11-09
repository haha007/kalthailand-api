package th.co.krungthaiaxa.api.elife.model.product;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import th.co.krungthaiaxa.api.elife.model.FinancialScheduler;

import java.io.Serializable;

/**
 * This is the premium data which will be applied for all products.
 */
@ApiModel(description = "Data concerning premiums (price for the coverage and benefit agreed) to all commercial types")
public class PremiumsData implements Serializable {
    private FinancialScheduler financialScheduler;
    private Product10ECPremium product10ECPremium;
    private ProductIBeginPremium productIBeginPremium;
    private ProductIFinePremium productIFinePremium;

    private ProductIProtectPremium productIProtectPremium;

    private PremiumDetail premiumDetail;

    @ApiModelProperty(value = "The definition of how the premiums must be paid")
    public FinancialScheduler getFinancialScheduler() {
        return financialScheduler;
    }

    public void setFinancialScheduler(FinancialScheduler financialScheduler) {
        this.financialScheduler = financialScheduler;
    }

    @ApiModelProperty(value = "10 EC specific Premiums Data")
    public Product10ECPremium getProduct10ECPremium() {
        return product10ECPremium;
    }

    public void setProduct10ECPremium(Product10ECPremium product10ECPremium) {
        this.product10ECPremium = product10ECPremium;
    }

    @ApiModelProperty(value = "iBegin specific Premiums Data")
    public ProductIBeginPremium getProductIBeginPremium() {
        return productIBeginPremium;
    }

    public void setProductIBeginPremium(ProductIBeginPremium productIBeginPremium) {
        this.productIBeginPremium = productIBeginPremium;
    }

    @ApiModelProperty(value = "iFine specific Premiums Data")
    public ProductIFinePremium getProductIFinePremium() {
        return productIFinePremium;
    }

    public void setProductIFinePremium(ProductIFinePremium productIFinePremium) {
        this.productIFinePremium = productIFinePremium;
    }

    public ProductIProtectPremium getProductIProtectPremium() {
        return productIProtectPremium;
    }

    public void setProductIProtectPremium(ProductIProtectPremium productIProtectPremium) {
        this.productIProtectPremium = productIProtectPremium;
    }

    public PremiumDetail getPremiumDetail() {
        return premiumDetail;
    }

    public void setPremiumDetail(PremiumDetail premiumDetail) {
        this.premiumDetail = premiumDetail;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
