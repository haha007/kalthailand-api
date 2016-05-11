package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "The definition of a geographical / physical address")
public class GeographicalAddress implements Serializable {
    private String streetAddress1;
    private String streetAddress2;
    private String postCode;
    private String district;
    private String subdistrict;
    private String subCountry;
    private String country = "Thailand";

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
        if (StringUtils.isNotEmpty(country)) {
            this.country = country;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeographicalAddress that = (GeographicalAddress) o;
        return Objects.equals(streetAddress1, that.streetAddress1) &&
                Objects.equals(streetAddress2, that.streetAddress2) &&
                Objects.equals(postCode, that.postCode) &&
                Objects.equals(district, that.district) &&
                Objects.equals(subdistrict, that.subdistrict) &&
                Objects.equals(subCountry, that.subCountry) &&
                Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streetAddress1, streetAddress2, postCode, district, subdistrict, subCountry, country);
    }
}
