package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.MaritalStatus;

import java.io.Serializable;
import java.time.LocalDate;

@ApiModel(description = "Data concerning specific properties to a Person")
public class Person extends PersonInfo implements Serializable {

    @ApiModelProperty(value = "Deprecated - The person's line mid (if any). At this moment, this field is also the mid. " +
            "Should use lineUserId sice Line V2")
    @Deprecated
    private String lineId;

    @ApiModelProperty(value = "The User LINE ID that used for LINE V2")
    private String lineUserId;

    private MaritalStatus maritalStatus;
    private LocalDate birthDate;
    private GenderCode genderCode;

    @Deprecated
    public String getLineId() {
        return lineId;
    }

    @Deprecated
    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getLineUserId() {
        return lineUserId;
    }

    public void setLineUserId(String lineUserId) {
        this.lineUserId = lineUserId;
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

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
