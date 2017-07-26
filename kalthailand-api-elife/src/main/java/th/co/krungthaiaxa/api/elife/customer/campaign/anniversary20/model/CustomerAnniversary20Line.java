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
    private String district;

    @ReportField("Sub District")
    private String subDistrict;

    @ReportField("Province + Zip Code")
    private String provinceZipCode;

    @ReportField("Purchase Reason")
    private String purchaseReason;

    @ReportField("Force Change Address")
    private String forceChange;

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

    public String getThaiId() {
        return thaiId;
    }

    public void setThaiId(final String thaiId) {
        this.thaiId = thaiId;
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

    public String getProvinceZipCode() {
        return provinceZipCode;
    }

    public void setProvinceZipCode(final String provinceZipCode) {
        this.provinceZipCode = provinceZipCode;
    }

    public String getPurchaseReason() {
        return purchaseReason;
    }

    public void setPurchaseReason(final String purchaseReason) {
        this.purchaseReason = purchaseReason;
    }

    public String getForceChange() {
        return forceChange;
    }

    public void setForceChange(final String forceChange) {
        this.forceChange = forceChange;
    }
}
