package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.enums.USPermanentResident;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "USA 'Foreign Account Tax Compliance Act' specific informations")
public class Fatca implements Serializable {
    private Boolean bornInUSA;
    private Boolean payTaxInUSA;
    private USPermanentResident permanentResidentOfUSA;
    private Boolean permanentResidentOfUSAForTax;

    @ApiModelProperty(value = "Born in the USA flag")
    public Boolean isBornInUSA() {
        return bornInUSA;
    }

    public void setBornInUSA(Boolean bornInUSA) {
        this.bornInUSA = bornInUSA;
    }

    @ApiModelProperty(value = "Has to pay tax in the USA flag")
    public Boolean isPayTaxInUSA() {
        return payTaxInUSA;
    }

    public void setPayTaxInUSA(Boolean payTaxInUSA) {
        this.payTaxInUSA = payTaxInUSA;
    }

    @ApiModelProperty(value = "Is or was permanent of the USA")
    public USPermanentResident getPermanentResidentOfUSA() {
        return permanentResidentOfUSA;
    }

    public void setPermanentResidentOfUSA(USPermanentResident permanentResidentOfUSA) {
        this.permanentResidentOfUSA = permanentResidentOfUSA;
    }

    @ApiModelProperty(value = "Is paying tax in the USA")
    public Boolean isPermanentResidentOfUSAForTax() {
        return permanentResidentOfUSAForTax;
    }

    public void setPermanentResidentOfUSAForTax(Boolean permanentResidentOfUSAForTax) {
        this.permanentResidentOfUSAForTax = permanentResidentOfUSAForTax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fatca fatca = (Fatca) o;
        return Objects.equals(bornInUSA, fatca.bornInUSA) &&
                Objects.equals(payTaxInUSA, fatca.payTaxInUSA) &&
                permanentResidentOfUSA == fatca.permanentResidentOfUSA &&
                Objects.equals(permanentResidentOfUSAForTax, fatca.permanentResidentOfUSAForTax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bornInUSA, payTaxInUSA, permanentResidentOfUSA, permanentResidentOfUSAForTax);
    }
}
