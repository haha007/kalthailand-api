package th.co.krungthaiaxa.ebiz.api.model;

import th.co.krungthaiaxa.ebiz.api.model.enums.InsuredType;

import java.time.LocalDate;

public class Insured {
    private InsuredType type;
    private Boolean mainInsuredIndicator;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer ageAtSubscription;
    private Integer declaredTaxPercentAtSubscription;
    private Person person;
    private Fatca fatca;
    private Boolean disableOrImmunoDeficient;
    private Boolean hospitalizedInLast6Months;
    private String additionalInformationFreeText;

    public InsuredType getType() {
        return type;
    }

    public void setType(InsuredType type) {
        this.type = type;
    }

    public Boolean getMainInsuredIndicator() {
        return mainInsuredIndicator;
    }

    public void setMainInsuredIndicator(Boolean mainInsuredIndicator) {
        this.mainInsuredIndicator = mainInsuredIndicator;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getAgeAtSubscription() {
        return ageAtSubscription;
    }

    public void setAgeAtSubscription(Integer ageAtSubscription) {
        this.ageAtSubscription = ageAtSubscription;
    }

    public Integer getDeclaredTaxPercentAtSubscription() {
        return declaredTaxPercentAtSubscription;
    }

    public void setDeclaredTaxPercentAtSubscription(Integer declaredTaxPercentAtSubscription) {
        this.declaredTaxPercentAtSubscription = declaredTaxPercentAtSubscription;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Fatca getFatca() {
        return fatca;
    }

    public void setFatca(Fatca fatca) {
        this.fatca = fatca;
    }

    public Boolean getDisableOrImmunoDeficient() {
        return disableOrImmunoDeficient;
    }

    public void setDisableOrImmunoDeficient(Boolean disableOrImmunoDeficient) {
        this.disableOrImmunoDeficient = disableOrImmunoDeficient;
    }

    public Boolean getHospitalizedInLast6Months() {
        return hospitalizedInLast6Months;
    }

    public void setHospitalizedInLast6Months(Boolean hospitalizedInLast6Months) {
        this.hospitalizedInLast6Months = hospitalizedInLast6Months;
    }

    public String getAdditionalInformationFreeText() {
        return additionalInformationFreeText;
    }

    public void setAdditionalInformationFreeText(String additionalInformationFreeText) {
        this.additionalInformationFreeText = additionalInformationFreeText;
    }
}
