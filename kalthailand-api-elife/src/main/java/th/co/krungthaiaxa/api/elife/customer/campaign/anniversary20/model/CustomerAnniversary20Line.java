package th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.model;

import org.hibernate.validator.constraints.NotBlank;
import th.co.krungthaiaxa.api.elife.export.ReportField;

/**
 * @author tuong.le on 7/26/17.
 */
public class CustomerAnniversary20Line {
    @ReportField("Given Name")
    private String givenName;

    @NotBlank
    private String surname;

    @ReportField("Thai ID")
    private String thaiId;

    private String email;

    private String mobile;

    private String address;

    @ReportField("Home Number")
    private String homeNumber;

    private String road;

    @ReportField("Sub District")
    private String subDistrict;
    
    private String district;

    @ReportField("Province + Zip Code")
    private String provinceZipCode;

    @ReportField("Purchase Reason")
    private String purchaseReason;

    @ReportField("Force Change Address")
    private String forceChangeAddress;

    public String getGivenName() {
        
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getThaiId() {
        return thaiId;
    }

    public void setThaiId(String thaiId) {
        this.thaiId = thaiId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHomeNumber() {
        return homeNumber;
    }

    public void setHomeNumber(String homeNumber) {
        this.homeNumber = homeNumber;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getSubDistrict() {
        return subDistrict;
    }

    public void setSubDistrict(String subDistrict) {
        this.subDistrict = subDistrict;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvinceZipCode() {
        return provinceZipCode;
    }

    public void setProvinceZipCode(String provinceZipCode) {
        this.provinceZipCode = provinceZipCode;
    }

    public String getPurchaseReason() {
        return purchaseReason;
    }

    public void setPurchaseReason(String purchaseReason) {
        this.purchaseReason = purchaseReason;
    }

    public String getForceChangeAddress() {
        return forceChangeAddress;
    }

    public void setForceChangeAddress(String forceChangeAddress) {
        this.forceChangeAddress = forceChangeAddress;
    }
}
