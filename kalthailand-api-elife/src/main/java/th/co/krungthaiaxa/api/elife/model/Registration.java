package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.enums.RegistrationTypeName;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Data concerning nationality-dependent registrations for the party. e.g. Social Security, " +
        "passport, taxes identification number, insurance company registration, driver license")
public class Registration implements Serializable {
    private String id;
    private RegistrationTypeName typeName;

    @ApiModelProperty(value = " The registration identifier assigned by the registration authority")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
