package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.jsoup.helper.StringUtil;
import th.co.krungthaiaxa.api.common.utils.EncryptUtil;
import th.co.krungthaiaxa.api.elife.model.enums.RegistrationTypeName;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Data concerning nationality-dependent registrations for the party. e.g. Social Security, " +
        "passport, taxes identification number, insurance company registration, driver license")
public class Registration implements Serializable {
    public static final int REGISTRATION_ID_PLAIN_TEXT_MAX_LENGTH = 100;

    private String id;

    private RegistrationTypeName typeName;

    @ApiModelProperty(value = " The registration identifier assigned by the registration authority")
    public String getId() {
        String result = this.id;
        if (!StringUtil.isBlank(this.id) && isEncrypted(this.id)) {
            result = EncryptUtil.decrypt(this.id);
        }
        return result;
    }

    public void setId(String id) {
        String result = id;
        if (!StringUtil.isBlank(id) && !isEncrypted(id)) {
            result = EncryptUtil.encrypt(id);
        }
        this.id = result;
    }

    /**
     * This method is used only to compatible to old data (plaintext).
     *
     * @param registrationId
     * @return
     */
    private boolean isEncrypted(String registrationId) {
        return registrationId.length() > REGISTRATION_ID_PLAIN_TEXT_MAX_LENGTH;
    }

    @ApiModelProperty(value = "The type of registration")
    public RegistrationTypeName getTypeName() {
        return typeName;
    }

    public void setTypeName(RegistrationTypeName typeName) {
        this.typeName = typeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Registration that = (Registration) o;
        return Objects.equals(id, that.id) &&
                typeName == that.typeName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, typeName);
    }
}
