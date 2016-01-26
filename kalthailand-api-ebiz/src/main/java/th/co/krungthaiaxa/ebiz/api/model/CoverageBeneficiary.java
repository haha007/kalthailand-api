package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.ebiz.api.model.enums.BeneficiaryRelationshipType;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "The beneficiary with his/her respective share of the benefits")
public class CoverageBeneficiary implements Serializable {
    private Person person;
    private BeneficiaryRelationshipType relationship;
    private Double coverageBenefitPercentage;

    @ApiModelProperty(required = true, value = "The beneficiary details")
    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @ApiModelProperty(required = true, value = "The relationship between beneficiary and insured")
    public BeneficiaryRelationshipType getRelationship() {
        return relationship;
    }

    public void setRelationship(BeneficiaryRelationshipType relationship) {
        this.relationship = relationship;
    }

    @ApiModelProperty(required = true, value = "Relative share of the benefit received by the beneficiary for the coverage")
    public Double getCoverageBenefitPercentage() {
        return coverageBenefitPercentage;
    }

    public void setCoverageBenefitPercentage(Double coverageBenefitPercentage) {
        this.coverageBenefitPercentage = coverageBenefitPercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoverageBeneficiary that = (CoverageBeneficiary) o;
        return Objects.equals(person, that.person) &&
                relationship == that.relationship &&
                Objects.equals(coverageBenefitPercentage, that.coverageBenefitPercentage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, relationship, coverageBenefitPercentage);
    }
}