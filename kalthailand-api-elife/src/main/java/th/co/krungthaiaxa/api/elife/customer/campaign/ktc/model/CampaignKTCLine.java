package th.co.krungthaiaxa.api.elife.customer.campaign.ktc.model;

import org.hibernate.validator.constraints.NotBlank;
import th.co.krungthaiaxa.api.elife.export.ReportField;

/**
 * @author tuong.le on 10/17/17.
 */
public class CampaignKTCLine {
    @NotBlank
    private String name;

    @ReportField("Date of Birth")
    @NotBlank
    private String dob;
    
    @NotBlank
    private String gender;

    @ReportField("ID Card")
    @NotBlank
    private String idCard;

    @ReportField("Phone Number")
    @NotBlank
    private String phoneNumber;

    private String beneficiary;

    @ReportField("Submitted Date")
    private String submittedDate;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }
}
