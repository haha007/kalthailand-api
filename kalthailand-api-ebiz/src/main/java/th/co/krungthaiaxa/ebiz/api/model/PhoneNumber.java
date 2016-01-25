package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.ebiz.api.model.enums.PhoneNumberType;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Data concerning Phone numbers")
public class PhoneNumber implements Serializable {
    private PhoneNumberType type;
    private Integer countryCode;
    private Integer areaCode;
    private Integer number;

    public PhoneNumber() {
    }

    @ApiModelProperty(value = "The phone number's type")
    public PhoneNumberType getType() {
        return type;
    }

    public void setType(PhoneNumberType type) {
        this.type = type;
    }

    @ApiModelProperty(value = "The phone number's country code")
    public Integer getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(Integer countryCode) {
        this.countryCode = countryCode;
    }

    @ApiModelProperty(value = "The phone number's are code")
    public Integer getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode;
    }

    @ApiModelProperty(value = "The phone number's remaining numbers")
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return type == that.type &&
                Objects.equals(countryCode, that.countryCode) &&
                Objects.equals(areaCode, that.areaCode) &&
                Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, countryCode, areaCode, number);
    }
}
