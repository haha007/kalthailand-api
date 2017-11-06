package th.co.krungthaiaxa.api.elife.customer.campaign.careCoordination.model;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import th.co.krungthaiaxa.api.common.utils.Constants;

import javax.annotation.RegEx;
import javax.validation.constraints.Pattern;

/**
 * @author tuong.le on 10/31/17.
 */
public class CareCoordinationForm {

    @ApiModelProperty(value = "Name", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "Policy Id", required = true)
    @NotBlank
    @RegEx
    @Pattern(regexp = Constants.POLICY_REGEX, message = "incorrect policy format")
    private String policyId;

    @ApiModelProperty(value = "Mobile Phone", required = true)
    @NotBlank
    private String phoneNumber;

    @ApiModelProperty(value = "Email", required = true)
    @NotBlank
    @Email
    private String email;

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
