package th.co.krungthaiaxa.ebiz.api.model;

import th.co.krungthaiaxa.ebiz.api.model.enums.USPermanentResident;

public class Fatca {
    private Boolean bornInUSA;
    private Boolean payTaxInUSA;
    private USPermanentResident permanentResidentOfUSA;
    private Boolean permanentResidentOfUSAForTax;

    public Boolean isBornInUSA() {
        return bornInUSA;
    }

    public void setBornInUSA(Boolean bornInUSA) {
        this.bornInUSA = bornInUSA;
    }

    public Boolean isPayTaxInUSA() {
        return payTaxInUSA;
    }

    public void setPayTaxInUSA(Boolean payTaxInUSA) {
        this.payTaxInUSA = payTaxInUSA;
    }

    public USPermanentResident getPermanentResidentOfUSA() {
        return permanentResidentOfUSA;
    }

    public void setPermanentResidentOfUSA(USPermanentResident permanentResidentOfUSA) {
        this.permanentResidentOfUSA = permanentResidentOfUSA;
    }

    public Boolean isPermanentResidentOfUSAForTax() {
        return permanentResidentOfUSAForTax;
    }

    public void setPermanentResidentOfUSAForTax(Boolean permanentResidentOfUSAForTax) {
        this.permanentResidentOfUSAForTax = permanentResidentOfUSAForTax;
    }
}
