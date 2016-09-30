package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * This is the premium data which will be applied for all products.
 */
@ApiModel(description = "Data concerning premiums (price for the coverage and benefit agreed) to all commercial types",
        subTypes = { Product10ECPremium.class })//TODO change it to PremiumsData.class or remove it.
public class PremiumsData implements Serializable {
    private FinancialScheduler financialScheduler;
    private Product10ECPremium product10ECPremium;
    private ProductIBeginPremium productIBeginPremium;
    private ProductIFinePremium productIFinePremium;

    private ProductIGenPremium productIGenPremium;
    private ProductIProtectPremium productIProtectPremium;

    private ProductPremiumDetailBasic premiumDetail;

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

    @ApiModelProperty(value = "iGen specific Premiums Data")
    public ProductIGenPremium getProductIGenPremium() {
        return productIGenPremium;
    }

    public void setProductIGenPremium(ProductIGenPremium productIGenPremium) {
        this.productIGenPremium = productIGenPremium;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PremiumsData that = (PremiumsData) o;
        return Objects.equals(financialScheduler, that.financialScheduler) &&
                Objects.equals(product10ECPremium, that.product10ECPremium) &&
                Objects.equals(productIBeginPremium, that.productIBeginPremium) &&
                Objects.equals(productIFinePremium, that.productIFinePremium) &&
                Objects.equals(productIGenPremium, that.productIGenPremium);
    }

    @Override
    public int hashCode() {
        return Objects.hash(financialScheduler, product10ECPremium, productIBeginPremium, productIFinePremium, productIGenPremium);
    }

    public ProductIProtectPremium getProductIProtectPremium() {
        return productIProtectPremium;
    }

    public void setProductIProtectPremium(ProductIProtectPremium productIProtectPremium) {
        this.productIProtectPremium = productIProtectPremium;
    }

    public ProductPremiumDetailBasic getPremiumDetail() {
        return premiumDetail;
    }

    public void setPremiumDetail(ProductPremiumDetailBasic premiumDetail) {
        this.premiumDetail = premiumDetail;
    }
}
