package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.ebiz.api.model.enums.PhoneNumberType;

@ApiModel(description = "Data concerning Phone numbers")
public class PhoneNumber {
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
}
