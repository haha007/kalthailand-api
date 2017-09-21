package th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.NumberFormat;

/**
 * @author tuong.le on 7/26/17.
 */
@ApiModel(description = "Model presents entered data of user on Campaign 20th Anniversary")
public class CustomerAnniversary20Form {
    
    @ApiModelProperty(value = "First Name", required = true)
    @NotBlank
    private String givenName;

    @ApiModelProperty(value = "Last Name", required = true)
    @NotBlank
    private String surname;

    @ApiModelProperty(value = "Thai ID", required = true)
    @NotBlank
    private String registration;

    @ApiModelProperty(value = "Email", required = true)
    @NotBlank
    @Email
    private String email;

    @ApiModelProperty(value = "Mobile Number", required = true)
    @NotBlank
    @NumberFormat
    private String mobile;

    @ApiModelProperty(value = "Address")
    private String address;

    @ApiModelProperty(value = "Home Number", required = true)
    @NotBlank
    private String homeNumber;

    @ApiModelProperty(value = "Road", required = true)
    @NotBlank
    private String road;


    @ApiModelProperty(value = "District", required = true)
    @NotBlank
    private String district;

    @ApiModelProperty(value = "Sub district", required = true)
    @NotBlank
    private String subDistrict;

    @ApiModelProperty(value = "Province", required = true)
    @NotBlank
    private String province;

    @ApiModelProperty(value = "Zip code", required = true)
    @NotBlank
    private String zipCode;

    @ApiModelProperty(value = "Purchase reason", required = true)
    @NotBlank
    private String purchaseReason;

    @ApiModelProperty(value = "Is forced changing the address in RLS", required = true)
    private boolean forceChangeAddress;

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(final String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(final String registration) {
        this.registration = registration;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public String getHomeNumber() {
        return homeNumber;
    }

    public void setHomeNumber(final String homeNumber) {
        this.homeNumber = homeNumber;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(final String road) {
        this.road = road;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(final String district) {
        this.district = district;
    }

    public String getSubDistrict() {
        return subDistrict;
    }

    public void setSubDistrict(final String subDistrict) {
        this.subDistrict = subDistrict;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(final String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPurchaseReason() {
        return purchaseReason;
    }

    public void setPurchaseReason(final String purchaseReason) {
        this.purchaseReason = purchaseReason;
    }

    public boolean isForceChangeAddress() {
        return forceChangeAddress;
    }

    public void setForceChangeAddress(final boolean forceChangeAddress) {
        this.forceChangeAddress = forceChangeAddress;
    }
}
