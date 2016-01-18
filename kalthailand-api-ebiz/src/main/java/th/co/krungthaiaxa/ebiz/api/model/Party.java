package th.co.krungthaiaxa.ebiz.api.model;

import th.co.krungthaiaxa.ebiz.api.model.enums.PartyType;

import java.util.List;

public class Party {
    private PartyType type;
    private List<Registration> registrations;

    public PartyType getType() {
        return type;
    }

    public void setType(PartyType type) {
        this.type = type;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }
}
