package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.enums.PartyType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Data concerning everything common to any type of Party")
public class Party implements Serializable {
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

    public void addRegistration(Registration registration) {
        this.registrations.add(registration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return type == party.type &&
                Objects.equals(registrations, party.registrations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, registrations);
    }
}
