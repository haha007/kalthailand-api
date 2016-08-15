package th.co.krungthaiaxa.api.elife.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.elife.model.Policy;

@Document(collection = "policyNumber")
public class PolicyNumber {
    @Id
    private String id;
    /**
     * This actually the policy number which input by business team.
     */
    @Indexed(unique = true)
    private String policyId;
    @DBRef
    private Policy policy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }
}
