package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApiModel(description = "Data concerning the policy")
@Document
public class Policy {
    @Id
    private String technicalId;
    @Indexed
    private String policyId;
    @Indexed
    private String quoteFunctionalId;
    private CommonData commonData;
    private PremiumsDataLifeInsurance premiumsData;
    private List<Insured> insureds = new ArrayList<>();
    private List<Coverage> coverages = new ArrayList<>();
    @DBRef
    private List<Payment> payments = new ArrayList<>();

    @ApiModelProperty(required = true, value = "Technical ID of the policy. This should not be displayed to user")
    public String getTechnicalId() {
        return technicalId;
    }

    public void setTechnicalId(String technicalId) {
        this.technicalId = technicalId;
    }

    @ApiModelProperty(required = true, value = "Functional ID of the policy. This may be displayed to user")
    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    @ApiModelProperty(required = true, value = "Functional ID of the quote the policy is coming from")
    public String getQuoteFunctionalId() {
        return quoteFunctionalId;
    }

    public void setQuoteFunctionalId(String quoteId) {
        this.quoteFunctionalId = quoteId;
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
}
