package th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.model;

import org.hibernate.validator.constraints.NotBlank;
import th.co.krungthaiaxa.api.elife.export.ReportField;

/**
 * @author tuong.le on 10/31/17.
 */
public class CareCoordinationLine {
    @NotBlank
    private String name;

    @ReportField("Mobile Phone")
    @NotBlank
    private String phoneNumber;
    
    @ReportField(value = "Policy Number/Id Card Number")
    private String policyId;

    private String email;

    @ReportField("Submitted Date")
    private String submittedDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }
}
