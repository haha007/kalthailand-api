package th.co.krungthaiaxa.elife.api.products;

import th.co.krungthaiaxa.elife.api.model.Amount;
import th.co.krungthaiaxa.elife.api.model.enums.GenderCode;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;

import java.time.LocalDate;

public class ProductQuotation {
    private String productId;
    private LocalDate dateOfBirth;
    private GenderCode genderCode;
    private Amount premiumAmount;
    private Amount sumInsuredAmount;
    private PeriodicityCode periodicityCode;
    private Integer nbOfYearsOfPayment;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public GenderCode getGenderCode() {
        return genderCode;
    }

    public void setGenderCode(GenderCode genderCode) {
        this.genderCode = genderCode;
    }

    public Amount getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(Amount premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public Amount getSumInsuredAmount() {
        return sumInsuredAmount;
    }

    public void setSumInsuredAmount(Amount sumInsuredAmount) {
        this.sumInsuredAmount = sumInsuredAmount;
    }

    public PeriodicityCode getPeriodicityCode() {
        return periodicityCode;
    }

    public void setPeriodicityCode(PeriodicityCode periodicityCode) {
        this.periodicityCode = periodicityCode;
    }

    public Integer getNbOfYearsOfPayment() {
        return nbOfYearsOfPayment;
    }

    public void setNbOfYearsOfPayment(Integer nbOfYearsOfPayment) {
        this.nbOfYearsOfPayment = nbOfYearsOfPayment;
    }
}
