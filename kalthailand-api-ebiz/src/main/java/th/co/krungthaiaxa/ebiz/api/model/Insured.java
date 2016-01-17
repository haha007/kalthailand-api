package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.ebiz.api.model.enums.InsuredType;

import java.time.LocalDate;

public class Insured {
    private InsuredType type;
    private Boolean mainInsuredIndicator;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer ageAtSubscription;
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

    public Boolean isMainInsuredIndicator() {
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

    @ApiModelProperty(value = "End date of coverage for the insured")
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

    public Boolean isDisableOrImmunoDeficient() {
        return disableOrImmunoDeficient;
    }

    public void setDisableOrImmunoDeficient(Boolean disableOrImmunoDeficient) {
        this.disableOrImmunoDeficient = disableOrImmunoDeficient;
    }

    public Boolean isHospitalizedInLast6Months() {
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
