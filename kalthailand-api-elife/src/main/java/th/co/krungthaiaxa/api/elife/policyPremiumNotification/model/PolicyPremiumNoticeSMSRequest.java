package th.co.krungthaiaxa.api.elife.policyPremiumNotification.model;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author khoi.tran on 10/18/16.
 */
public class PolicyPremiumNoticeSMSRequest extends PolicyPremiumNoticeRequest {

    @NotBlank
    private String companyCode;

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
}
