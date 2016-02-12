package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "The coverage of the contract")
public class Coverage implements Serializable {
    @Id
    private String id;
    private String name;
    private List<CoverageBeneficiary> beneficiaries = new ArrayList<>();

    @ApiModelProperty(required = true, value = "The coverage Id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void addBeneficiary(CoverageBeneficiary beneficiary) {
        beneficiaries.add(beneficiary);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coverage coverage = (Coverage) o;
        return Objects.equals(id, coverage.id) &&
                Objects.equals(name, coverage.name) &&
                Objects.equals(beneficiaries, coverage.beneficiaries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, beneficiaries);
    }
}
