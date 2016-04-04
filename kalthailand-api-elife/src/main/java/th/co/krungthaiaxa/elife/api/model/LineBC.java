package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;

/**
 * Created by SantiLik on 4/4/2016.
 */

@ApiModel(description = "Line BC Information")
public class LineBC {

    private String dob;
    private String pid;
    private String mobile;
    private String email;
    private String firstName;
    private String lastName;


    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
