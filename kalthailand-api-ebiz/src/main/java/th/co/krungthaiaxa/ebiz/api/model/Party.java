package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.ebiz.api.model.enums.PartyType;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Data concerning everything common to any type of Party")
public class Party {
    private PartyType type;
    private List<Registration> registrations = new ArrayList<>();

    @ApiModelProperty(value = "The party's type")
    public PartyType getType() {
        return type;
    }

    public void setType(PartyType type) {
        this.type = type;
    }

    @ApiModelProperty(value = "A set of nationality-dependent registrations for the party. e.g. Social Security, " +
            "passport, taxes identification number, insurance company registration, driver license")
    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }
}
