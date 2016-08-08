package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Data concerning the policy")
@Document
public class Policy implements Serializable {
    @Id
    private String id;
    @Indexed
    private String policyId;
    @Indexed
    private String quoteId;
    private PolicyStatus status;
    private CommonData commonData;
    private PremiumsData premiumsData;
    private List<Insured> insureds = new ArrayList<>();
    private List<Coverage> coverages = new ArrayList<>();
    @DBRef
    private List<Payment> payments = new ArrayList<>();
    @DBRef
    private List<th.co.krungthaiaxa.api.elife.model.Document> documents = new ArrayList<>();
    private String validationAgentCode;
    private String validationAgentName;

    private Instant validationDateTime;
    private Instant creationDateTime;
    private Instant lastUpdateDateTime;

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

    @ApiModelProperty(required = true, value = "The status of the policy")
    public PolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyStatus status) {
        this.status = status;
    }

    @ApiModelProperty(required = true, value = "Data common to all policies commercial types")
    public CommonData getCommonData() {
        return commonData;
    }

    public void setCommonData(CommonData commonData) {
        this.commonData = commonData;
    }

    @ApiModelProperty(value = "Data concerning premiums (price for the coverage and benefit agreed for the policy)")
    public PremiumsData getPremiumsData() {
        return premiumsData;
    }

    public void setPremiumsData(PremiumsData premiumsData) {
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

    @ApiModelProperty(value = "List of all policy documents")
    public List<th.co.krungthaiaxa.api.elife.model.Document> getDocuments() {
        return Collections.unmodifiableList(documents);
    }

    public void addDocument(th.co.krungthaiaxa.api.elife.model.Document document) {
        documents.add(document);
    }

    @ApiModelProperty(value = "The agent code of the agent who validate the policy")
    public String getValidationAgentCode() {
        return validationAgentCode;
    }

    public void setValidationAgentCode(String validationAgentCode) {
        this.validationAgentCode = validationAgentCode;
    }

    @ApiModelProperty(value = "The name of the agent who validate the policy")
    public String getValidationAgentName() {
        return validationAgentName;
    }

    public void setValidationAgentName(String validationAgentName) {
        this.validationAgentName = validationAgentName;
    }

    /**
     * We have to extract creationDateTime from Id because the old structure doesn't have creation time field.
     * So this code should work well with both old (no creationDateTime field) & new data (has creationDateTime field)
     *
     * @return the creationDateTime. If not found, it will get data from id.
     */
    public Instant getCreationDateTime() {
        setCreateDateTimeFromIdIfNecessary();
        return creationDateTime;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        if (creationDateTime != null) {
            this.creationDateTime = creationDateTime;
        } else {
            setCreateDateTimeFromIdIfNecessary();
        }
    }

    private void setCreateDateTimeFromIdIfNecessary() {
        if (creationDateTime == null) {
            if (id != null) {
                ObjectId objectId = new ObjectId(id);
                creationDateTime = Instant.ofEpochMilli(objectId.getDate().getTime());
            }
        }
    }

    public Instant getLastUpdateDateTime() {
        return lastUpdateDateTime;
    }

    public void setLastUpdateDateTime(Instant lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Policy policy = (Policy) o;
        return Objects.equals(id, policy.id) &&
                Objects.equals(policyId, policy.policyId) &&
                Objects.equals(quoteId, policy.quoteId) &&
                status == policy.status &&
                Objects.equals(commonData, policy.commonData) &&
                Objects.equals(premiumsData, policy.premiumsData) &&
                Objects.equals(insureds, policy.insureds) &&
                Objects.equals(coverages, policy.coverages) &&
                Objects.equals(payments, policy.payments) &&
                Objects.equals(documents, policy.documents) &&
                Objects.equals(validationAgentCode, policy.validationAgentCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, policyId, quoteId, status, commonData, premiumsData, insureds, coverages, payments, documents, validationAgentCode);
    }

    public Instant getValidationDateTime() {
        return validationDateTime;
    }

    public void setValidationDateTime(Instant validationDateTime) {
        this.validationDateTime = validationDateTime;
    }
}
