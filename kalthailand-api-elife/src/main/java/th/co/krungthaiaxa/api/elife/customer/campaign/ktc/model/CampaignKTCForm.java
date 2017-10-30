package th.co.krungthaiaxa.api.elife.customer.campaign.ktc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static th.co.krungthaiaxa.api.elife.customer.campaign.ktc.service.CampaignKTCService.DATE_FORMAT;

/**
 * @author tuong.le on 7/26/17.
 */
@ApiModel(description = "Model presents entered data of user on KTC Campaign")
public class CampaignKTCForm {

    @ApiModelProperty(value = "Gender")
    @NotBlank
    private String gender;

    @ApiModelProperty(value = "Name", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "Date of Birth", required = true)
    @DateTimeFormat(pattern = DATE_FORMAT)
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dob;

    @ApiModelProperty(value = "ID Card", required = true)
    @NotBlank
    private String idCard;

    @ApiModelProperty(value = "Phone Number", required = true)
    @NotBlank
    private String phoneNumber;

    @ApiModelProperty(value = "Beneficiary")
    private String beneficiary;

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
