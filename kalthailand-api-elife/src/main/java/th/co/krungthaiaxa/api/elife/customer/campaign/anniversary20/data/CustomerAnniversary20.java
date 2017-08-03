package th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.data;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.NumberFormat;
import th.co.krungthaiaxa.api.common.data.BaseEntity;
import th.co.krungthaiaxa.api.common.utils.EncryptUtil;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.model.CustomerAnniversary20Form;

/**
 * @author khoi.tran on 12/26/16.
 */
@Document(collection = "customerAnniversary20")
public class CustomerAnniversary20 extends BaseEntity {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String thaiID;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @NumberFormat
    private String mobile;

    private String address;

    @NotBlank
    private String homeNumber;

    @NotBlank
    private String road;

    @NotBlank
    private String district;

    @NotBlank
    private String subDistrict;

    @NotBlank
    private String province;

    @NotBlank
    @NumberFormat
    private String zipCode;

    @NotBlank
    private String purchaseReason;

    @NotBlank
    private boolean forceChangeAddress;

    public CustomerAnniversary20() {
        //Empty constructor
    }

    public CustomerAnniversary20(final CustomerAnniversary20Form form) {
        this.setFirstName(form.getGivenName());
        this.setLastName(form.getSurname());
        this.setThaiID(form.getRegistration());
        this.setEmail(form.getEmail());
        this.setMobile(form.getMobile());
        this.setHomeNumber(form.getHomeNumber());
        this.setRoad(form.getRoad());
        this.setDistrict(form.getDistrict());
        this.setSubDistrict(form.getSubDistrict());
        this.setProvince(form.getProvince());
        this.setZipCode(form.getZipCode());
        this.setPurchaseReason(form.getPurchaseReason());
        this.setForceChangeAddress(form.isForceChangeAddress());
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public void setPurchaseReason(String purchaseReason) {
        this.purchaseReason = purchaseReason;
    }

    public boolean isForceChangeAddress() {
        return forceChangeAddress;
    }

    public void setForceChangeAddress(final boolean forceChangeAddress) {
        this.forceChangeAddress = forceChangeAddress;
    }

    public String getThaiID() {
        if (this.thaiID == null) {
            return null;
        }
        return EncryptUtil.decrypt(this.thaiID);
    }

    public void setThaiID(String thaiID) {
        if (thaiID == null) {
            this.thaiID = null;
        }
        this.thaiID = EncryptUtil.encrypt(thaiID);
    }
}
