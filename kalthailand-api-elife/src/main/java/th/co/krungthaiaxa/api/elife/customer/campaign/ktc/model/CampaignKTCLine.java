package th.co.krungthaiaxa.api.elife.customer.campaign.ktc.model;

import org.hibernate.validator.constraints.NotBlank;
import th.co.krungthaiaxa.api.elife.export.ReportField;

/**
 * @author tuong.le on 10/17/17.
 */
public class CampaignKTCLine {

    private String title;

    private String name;

    @NotBlank
    private String surname;

    @ReportField("Date of Birth")
    @NotBlank
    private String dob;

    private String email;

    @ReportField("ID Card")
    @NotBlank
    private String idCard;

    @ReportField("Phone Number")
    @NotBlank
    private String phoneNumber;

    private String beneficiary;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
