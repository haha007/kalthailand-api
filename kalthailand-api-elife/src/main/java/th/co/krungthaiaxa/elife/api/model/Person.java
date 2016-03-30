package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.elife.api.model.enums.GenderCode;
import th.co.krungthaiaxa.elife.api.model.enums.MaritalStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@ApiModel(description = "Data concerning specific properties to a Person")
public class Person extends Party implements Serializable {
    private String lineId;
    private String givenName;
    private String middleName;
    private String surName;
    private String title;
    private PhoneNumber mobilePhoneNumber;
    private PhoneNumber homePhoneNumber;
    private PhoneNumber workPhoneNumber;
    private String email;
    private MaritalStatus maritalStatus;
    private LocalDate birthDate;
    private GenderCode genderCode;
    private GeographicalAddress currentAddress;
    private GeographicalAddress deliveryAddress;
    private GeographicalAddress registrationAddress;

    @ApiModelProperty(value = "The person's line mid (if any)")
    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

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

    @ApiModelProperty(value = "The person's marital status")
    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    @ApiModelProperty(value = "The person's birth date")
    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @ApiModelProperty(value = "The person's gender")
    public GenderCode getGenderCode() {
        return genderCode;
    }

    public void setGenderCode(GenderCode genderCode) {
        this.genderCode = genderCode;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Person person = (Person) o;
        return Objects.equals(givenName, person.givenName) &&
                Objects.equals(middleName, person.middleName) &&
                Objects.equals(surName, person.surName) &&
                Objects.equals(title, person.title) &&
                Objects.equals(mobilePhoneNumber, person.mobilePhoneNumber) &&
                Objects.equals(homePhoneNumber, person.homePhoneNumber) &&
                Objects.equals(workPhoneNumber, person.workPhoneNumber) &&
                Objects.equals(email, person.email) &&
                maritalStatus == person.maritalStatus &&
                Objects.equals(birthDate, person.birthDate) &&
                genderCode == person.genderCode &&
                Objects.equals(currentAddress, person.currentAddress) &&
                Objects.equals(deliveryAddress, person.deliveryAddress) &&
                Objects.equals(registrationAddress, person.registrationAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), givenName, middleName, surName, title, mobilePhoneNumber, homePhoneNumber, workPhoneNumber, email, maritalStatus, birthDate, genderCode, currentAddress, deliveryAddress, registrationAddress);
    }
}
