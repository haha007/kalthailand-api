package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(description = "This is the person information which doesn't affect to quote calculation. The client can change at any time.")
public class PersonInfo extends Party implements Serializable {
    private String givenName;
    private String middleName;
    private String surName;
    private String title;
    @Valid
    @NotNull
    private PhoneNumber mobilePhoneNumber;
    private PhoneNumber homePhoneNumber;
    private PhoneNumber workPhoneNumber;

    @NotBlank
    @Email
    private String email;
    private GeographicalAddress currentAddress;
    private GeographicalAddress deliveryAddress;
    private GeographicalAddress registrationAddress;

    @ApiModelProperty(value = "The person's first name")
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    @ApiModelProperty(value = "The person's middle name")
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @ApiModelProperty(value = "The person's last name")
    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    @ApiModelProperty(value = "The person's salutation (Mr, Ms, ...)")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "The person's mobile phone number")
    public PhoneNumber getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(PhoneNumber mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    @ApiModelProperty(value = "The person's home phone number")
    public PhoneNumber getHomePhoneNumber() {
        return homePhoneNumber;
    }

    public void setHomePhoneNumber(PhoneNumber homePhoneNumber) {
        this.homePhoneNumber = homePhoneNumber;
    }

    @ApiModelProperty(value = "The person's work phone number")
    public PhoneNumber getWorkPhoneNumber() {
        return workPhoneNumber;
    }

    public void setWorkPhoneNumber(PhoneNumber workPhoneNumber) {
        this.workPhoneNumber = workPhoneNumber;
    }

    @ApiModelProperty(value = "The person's email address")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ApiModelProperty(value = "The person's current address")
    public GeographicalAddress getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(GeographicalAddress currentAddress) {
        this.currentAddress = currentAddress;
    }

    @ApiModelProperty(value = "The person's address for deliveries")
    public GeographicalAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(GeographicalAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    @ApiModelProperty(value = "The person's address as it is registered")
    public GeographicalAddress getRegistrationAddress() {
        return registrationAddress;
    }

    public void setRegistrationAddress(GeographicalAddress registrationAddress) {
        this.registrationAddress = registrationAddress;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
