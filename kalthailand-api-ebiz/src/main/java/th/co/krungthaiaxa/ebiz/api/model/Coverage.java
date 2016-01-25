package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "The coverage of the contract")
public class Coverage implements Serializable {
    @Id
    private String coverageId;
    private String name;
    private List<CoverageBeneficiary> beneficiaries;

    @ApiModelProperty(required = true, value = "The coverage technical Id")
    public String getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    @ApiModelProperty(required = true, value = "The coverage name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(required = true, value = "The beneficiaries of the coverage with their respective share of the benefits")
    public List<CoverageBeneficiary> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(List<CoverageBeneficiary> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coverage coverage = (Coverage) o;
        return Objects.equals(coverageId, coverage.coverageId) &&
                Objects.equals(name, coverage.name) &&
                Objects.equals(beneficiaries, coverage.beneficiaries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coverageId, name, beneficiaries);
    }
}
