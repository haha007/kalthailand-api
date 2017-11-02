package th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.data;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.mapping.Document;
import th.co.krungthaiaxa.api.common.data.BaseEntity;
import th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.model.CareCoordinationForm;

/**
 * @author tuong.le on 10/31/17.
 */
@Document(collection = "campaignCareCoordination")
public class CareCoordinationEntity extends BaseEntity {
    @ApiModelProperty(value = "Name", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "Policy Id", required = true)
    @NotBlank
    private String policyId;

    @ApiModelProperty(value = "Mobile Phone", required = true)
    @NotBlank
    private String phone;

    @ApiModelProperty(value = "Email", required = true)
    @NotBlank
    private String email;

    public CareCoordinationEntity() {
        //Empty constructor
    }

    public CareCoordinationEntity(final CareCoordinationForm form) {
        this.setName(form.getName());
        this.setPolicyId(form.getPolicyId());
        this.setPhone(form.getPhone());
        this.setEmail(form.getEmail());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
