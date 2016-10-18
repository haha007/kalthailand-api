package th.co.krungthaiaxa.api.elife.policyPremiumNotification.model;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author khoi.tran on 10/18/16.
 */
public class PolicyPremiumNoticeRequest {
    private String policyNumber;

    @NotNull
    private LocalDate insuredDob;

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public LocalDate getInsuredDob() {
        return insuredDob;
    }

    public void setInsuredDob(LocalDate insuredDob) {
        this.insuredDob = insuredDob;
    }
}
