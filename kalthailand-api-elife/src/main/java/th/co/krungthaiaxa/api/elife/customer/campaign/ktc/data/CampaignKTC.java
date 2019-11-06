package th.co.krungthaiaxa.api.elife.customer.campaign.ktc.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.common.data.BaseEntity;
import th.co.krungthaiaxa.api.elife.customer.campaign.ktc.model.CampaignKTCForm;

import java.time.LocalDate;

import static th.co.krungthaiaxa.api.elife.customer.campaign.ktc.service.CampaignKTCService.DATE_FORMAT;


/**
 * @author tuong.le on 10/17/17.
 */
@Document(collection = "campaignKTC")
public class CampaignKTC extends BaseEntity {
    
    @NotBlank
    private String gender;

    @NotBlank
    private String name;

    @NotBlank
    @JsonFormat(pattern = DATE_FORMAT)
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
        this.setGender(form.getGender());
        this.setName(form.getName());
        this.setDob(form.getDob());
        this.setIdCard(form.getIdCard());
        this.setPhoneNumber(form.getPhoneNumber());
        this.setBeneficiary(form.getBeneficiary());
    }

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