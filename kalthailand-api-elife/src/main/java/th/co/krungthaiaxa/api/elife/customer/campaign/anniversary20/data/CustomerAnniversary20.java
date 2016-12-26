package th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.data;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.NumberFormat;
import th.co.krungthaiaxa.api.common.data.BaseEntity;
import th.co.krungthaiaxa.api.common.utils.EncryptUtil;
import th.co.krungthaiaxa.api.elife.export.ReportField;

/**
 * @author khoi.tran on 12/26/16.
 */
@Document(collection = "customerAnniversary20")
public class CustomerAnniversary20 extends BaseEntity {
    @NotBlank
    @Field("firstName")
    private String givenName;

    @NotBlank
    @Field("lastName")
    private String surName;

    @ReportField("Thai ID")
    @Field("thaiID")
    @NotBlank
    private String registration;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @NumberFormat
    private String mobile;
    @NotBlank
    private String address;
    @NotBlank
    private String purchaseReason;

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
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

    public String getPurchaseReason() {
        return purchaseReason;
    }

    public void setPurchaseReason(String purchaseReason) {
        this.purchaseReason = purchaseReason;
    }

    public String getRegistration() {
        if (this.registration == null) {
            return null;
        }
        return EncryptUtil.decrypt(this.registration);
    }

    public void setRegistration(String registration) {
        if (registration == null) {
            this.registration = null;
        }
        this.registration = EncryptUtil.encrypt(registration);
    }
}
