package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.ebiz.api.model.enums.USPermanentResident;

@ApiModel(description = "USA 'Foreign Account Tax Compliance Act' specific informations")
public class Fatca {
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
}
