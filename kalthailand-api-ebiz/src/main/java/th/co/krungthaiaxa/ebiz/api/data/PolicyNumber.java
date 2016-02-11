package th.co.krungthaiaxa.ebiz.api.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import th.co.krungthaiaxa.ebiz.api.model.Policy;

public class PolicyNumber {
    @Id
    private String id;
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
