package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Data concerning the policy")
@Document
public class Policy {
    @Id
    private String id;
    @Indexed
    private String policyId;
    @Indexed
    private String quoteId;
    private CommonData commonData;
    private PremiumsDataLifeInsurance premiumsData;
    private List<Insured> insureds = new ArrayList<>();
    private List<Coverage> coverages = new ArrayList<>();
    @DBRef
    private List<Payment> payments = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(required = true, value = "ID of the policy. This may be displayed to user")
    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    @ApiModelProperty(required = true, value = "ID of the quote the policy is coming from")
    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    @ApiModelProperty(required = true, value = "Data common to all policies commercial types")
    public CommonData getCommonData() {
        return commonData;
    }

    public void setCommonData(CommonData commonData) {
        this.commonData = commonData;
    }

    @ApiModelProperty(value = "Data concerning premiums (price for the coverage and benefit agreed for the policy)")
    public PremiumsDataLifeInsurance getPremiumsData() {
        return premiumsData;
    }

    public void setPremiumsData(PremiumsDataLifeInsurance premiumsData) {
        this.premiumsData = premiumsData;
    }

    @ApiModelProperty(value = "List of all insured parties stated on the policy")
    public List<Insured> getInsureds() {
        return Collections.unmodifiableList(insureds);
    }

    public void addInsured(Insured insured) {
        insureds.add(insured);
    }

    @ApiModelProperty(value = "List of all coverages of the policy")
    public List<Coverage> getCoverages() {
        return coverages;
    }

    public void addCoverage(Coverage coverage) {
        coverages.add(coverage);
    }

    @ApiModelProperty(value = "List of all policy payments")
    public List<Payment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Policy policy = (Policy) o;
        return Objects.equals(id, policy.id) &&
                Objects.equals(policyId, policy.policyId) &&
                Objects.equals(quoteId, policy.quoteId) &&
                Objects.equals(commonData, policy.commonData) &&
                Objects.equals(premiumsData, policy.premiumsData) &&
                Objects.equals(insureds, policy.insureds) &&
                Objects.equals(coverages, policy.coverages) &&
                Objects.equals(payments, policy.payments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, policyId, quoteId, commonData, premiumsData, insureds, coverages, payments);
    }
}
