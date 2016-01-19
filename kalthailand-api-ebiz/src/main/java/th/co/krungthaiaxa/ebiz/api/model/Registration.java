package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Data concerning nationality-dependent registrations for the party. e.g. Social Security, " +
        "passport, taxes identification number, insurance company registration, driver license")
public class Registration {
    private String id;
    private String typeName;

    @ApiModelProperty(value = " The registration identifier assigned by the registration authority")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "The localized name of the type of registration")
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
