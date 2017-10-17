package th.co.krungthaiaxa.api.elife.customer.campaign.ktc.data;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import th.co.krungthaiaxa.api.common.data.BaseEntity;
import th.co.krungthaiaxa.api.elife.customer.campaign.ktc.model.CampaignKTCForm;

import java.time.LocalDate;

/**
 * @author tuong.le on 10/17/17.
 */
@Document(collection = "campaignKTC")
public class CampaignKTC extends BaseEntity {
    private String title;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotBlank
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dob;

    @NotBlank
    private String idCard;

    @NotBlank
    private String phoneNumber;

    private String beneficiary;

    public CampaignKTC() {
        //Empty constructor
    }

    public CampaignKTC(final CampaignKTCForm form) {
        this.setTitle(form.getTitle());
        this.setName(form.getName());
        this.setSurname(form.getSurname());
        this.setDob(form.getDob());
        this.setIdCard(form.getIdCard());
        this.setPhoneNumber(form.getPhoneNumber());
        this.setBeneficiary(form.getBeneficiary());
    }

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

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
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
