package th.co.krungthaiaxa.api.elife.customer.campaign.ktc.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * @author tuong.le on 7/26/17.
 */
@ApiModel(description = "Model presents entered data of user on KTC Campaign")
public class CampaignKTCForm {

    @ApiModelProperty(value = "Title")
    private String title;

    @ApiModelProperty(value = "Name", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "Surname", required = true)
    @NotBlank
    private String surname;

    @ApiModelProperty(value = "Date of Birth", required = true)
    @DateTimeFormat(pattern = "dd/mm/yyyy")
    private LocalDate dob;

    @ApiModelProperty(value = "ID Card", required = true)
    @NotBlank
    private String idCard;

    @ApiModelProperty(value = "Phone Number", required = true)
    @NotBlank
    private String phoneNumber;

    @ApiModelProperty(value = "Beneficiary")
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
