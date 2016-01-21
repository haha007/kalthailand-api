package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The definition of a geographical / physical address")
public class GeographicalAddress {
    private String streetAddress1;
    private String streetAddress2;
    private String postCode;
    private String district;
    private String subdistrict;
    private String subCountry;
    private String country;

    @ApiModelProperty(value = "Street number")
    public String getStreetAddress1() {
        return streetAddress1;
    }

    public void setStreetAddress1(String streetAddress1) {
        this.streetAddress1 = streetAddress1;
    }

    @ApiModelProperty(value = "Street / Road name")
    public String getStreetAddress2() {
        return streetAddress2;
    }

    public void setStreetAddress2(String streetAddress2) {
        this.streetAddress2 = streetAddress2;
    }

    @ApiModelProperty(value = "The postal code")
    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    @ApiModelProperty(value = "The sub disctrict")
    public String getSubdistrict() {
        return subdistrict;
    }

    public void setSubdistrict(String subdistrict) {
        this.subdistrict = subdistrict;
    }

    @ApiModelProperty(value = "The disctrict")
    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    @ApiModelProperty(value = "The province / state of the country")
    public String getSubCountry() {
        return subCountry;
    }

    public void setSubCountry(String subCountry) {
        this.subCountry = subCountry;
    }

    @ApiModelProperty(value = "The country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
