package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.enums.InsuredType;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Data concerning the insured user")
public class Insured implements Serializable {
    private InsuredType type;
    private Boolean mainInsuredIndicator = false;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer ageAtSubscription;
    private Integer professionId;
    private String professionName;
    private String professionDescription;
    private String employerName;
    private String annualIncome;
    private List<String> incomeSources = new ArrayList<>();
    private Person person;
    private Fatca fatca;
    private HealthStatus healthStatus;
    private Integer declaredTaxPercentAtSubscription;
    private String additionalInformationFreeText;
    private List<String> insuredPreviousAgents = new ArrayList<>();

    @ApiModelProperty(value = "Insured's type")
    public InsuredType getType() {
        return type;
    }

    public void setType(InsuredType type) {
        this.type = type;
    }

    @ApiModelProperty(value = "Indicates whether the Insured is the main insured on the policy (e.g. main Driver Insured or main Healthcare Insured")
    public Boolean getMainInsuredIndicator() {
        return mainInsuredIndicator;
    }

    public void setMainInsuredIndicator(Boolean mainInsuredIndicator) {
        this.mainInsuredIndicator = mainInsuredIndicator;
    }

    @ApiModelProperty(value = "Start date of coverage for the insured. This is calculated by back end API and cannot be set by client.")
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @ApiModelProperty(value = "End date of coverage for the insured. This is calculated by back end API and cannot be set by client.")
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @ApiModelProperty(value = "Insured person's at the time of the policy subscription")
    public Integer getAgeAtSubscription() {
        return ageAtSubscription;
    }

    public void setAgeAtSubscription(Integer ageAtSubscription) {
        this.ageAtSubscription = ageAtSubscription;
    }

    @ApiModelProperty(value = "Profession ID of the self-insured")
    public Integer getProfessionId() {
        return professionId;
    }

    public void setProfessionId(Integer professionId) {
        this.professionId = professionId;
    }

    @ApiModelProperty(value = "Profession of the self-insured. This is calculated by back end API and cannot be set by client.")
    public String getProfessionName() {
        return professionName;
    }

    public void setProfessionName(String professionName) {
        this.professionName = professionName;
    }

    @ApiModelProperty(value = "Job description")
    public String getProfessionDescription() {
        return professionDescription;
    }

    public void setProfessionDescription(String professionDescription) {
        this.professionDescription = professionDescription;
    }

    @ApiModelProperty(value = "Employer's name")
    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    @ApiModelProperty(value = "Annual Income")
    public String getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(String annualIncome) {
        this.annualIncome = annualIncome;
    }

    @ApiModelProperty(value = "Income Source")
    public List<String> getIncomeSources() {
        return incomeSources;
    }

    public void addIncomeSource(String incomeSource) {
        this.incomeSources.add(incomeSource);
    }

    @ApiModelProperty(value = "Declared tax percentage the insured pays at subscription")
    public Integer getDeclaredTaxPercentAtSubscription() {
        return declaredTaxPercentAtSubscription;
    }

    public void setDeclaredTaxPercentAtSubscription(Integer declaredTaxPercentAtSubscription) {
        this.declaredTaxPercentAtSubscription = declaredTaxPercentAtSubscription;
    }

    @ApiModelProperty(value = "Data concerning the insured details")
    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @ApiModelProperty(value = "USA Fatca details of the insured")
    public Fatca getFatca() {
        return fatca;
    }

    public void setFatca(Fatca fatca) {
        this.fatca = fatca;
    }

    @ApiModelProperty(value = "Health status of the insured")
    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    @ApiModelProperty(value = "Free additional text the user may have included")
    public String getAdditionalInformationFreeText() {
        return additionalInformationFreeText;
    }

    public void setAdditionalInformationFreeText(String additionalInformationFreeText) {
        this.additionalInformationFreeText = additionalInformationFreeText;
    }

    @ApiModelProperty(value = "List of previous agents of the insured. This is used to calculate commision amount.")
    public List<String> getInsuredPreviousAgents() {
        return insuredPreviousAgents;
    }

    public void addInsuredPreviousAgent(String insuredPreviousAgent) {
        insuredPreviousAgents.add(insuredPreviousAgent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Insured insured = (Insured) o;
        return type == insured.type &&
                Objects.equals(mainInsuredIndicator, insured.mainInsuredIndicator) &&
                Objects.equals(startDate, insured.startDate) &&
                Objects.equals(endDate, insured.endDate) &&
                Objects.equals(ageAtSubscription, insured.ageAtSubscription) &&
                Objects.equals(professionName, insured.professionName) &&
                Objects.equals(professionDescription, insured.professionDescription) &&
                Objects.equals(employerName, insured.employerName) &&
                Objects.equals(annualIncome, insured.annualIncome) &&
                Objects.equals(incomeSources, insured.incomeSources) &&
                Objects.equals(person, insured.person) &&
                Objects.equals(fatca, insured.fatca) &&
                Objects.equals(healthStatus, insured.healthStatus) &&
                Objects.equals(declaredTaxPercentAtSubscription, insured.declaredTaxPercentAtSubscription) &&
                Objects.equals(additionalInformationFreeText, insured.additionalInformationFreeText) &&
                Objects.equals(insuredPreviousAgents, insured.insuredPreviousAgents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, mainInsuredIndicator, startDate, endDate, ageAtSubscription, professionName, professionDescription, employerName, annualIncome, incomeSources, person, fatca, healthStatus, declaredTaxPercentAtSubscription, additionalInformationFreeText, insuredPreviousAgents);
    }
}