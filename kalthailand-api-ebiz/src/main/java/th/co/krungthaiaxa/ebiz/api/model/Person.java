package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.ebiz.api.model.enums.GenderCode;
import th.co.krungthaiaxa.ebiz.api.model.enums.MaritalStatus;

import java.time.LocalDate;

@ApiModel(description = "Data concerning specific properties to a Person")
public class Person extends Party {
    private String givenName;
    private String middleName;
    private String surName;
    private String title;
    private PhoneNumber mobilePhoneNumber;
    private PhoneNumber homePhoneNumber;
    private PhoneNumber workPhoneNumber;
    private String email;
    private MaritalStatus maritalStatus;
    private Integer heightInCm;
    private Integer weightInKg;
    private LocalDate birthDate;
    private GenderCode genderCode;
    private GeographicalAddress geographicalAddress = new GeographicalAddress();

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

    @ApiModelProperty(value = "The person's height in centimeters")
    public Integer getHeightInCm() {
        return heightInCm;
    }

    public void setHeightInCm(Integer heightInCm) {
        this.heightInCm = heightInCm;
    }

    @ApiModelProperty(value = "The person's height in kilograms")
    public Integer getWeightInKg() {
        return weightInKg;
    }

    public void setWeightInKg(Integer weightInKg) {
        this.weightInKg = weightInKg;
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

    @ApiModelProperty(value = "The person's address")
    public GeographicalAddress getGeographicalAddress() {
        return geographicalAddress;
    }

    public void setGeographicalAddress(GeographicalAddress geographicalAddress) {
        this.geographicalAddress = geographicalAddress;
    }
}
