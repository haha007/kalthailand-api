package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.time.LocalDate;

@ApiModel(description = "Data concerning the policy")
public class PolicyCDB implements Serializable {
    /**
     * Same as {@link Policy#policyId}
     */
    private String policyNumber;
    /**
     * Note: this is the status from CDB, it's different from {@link th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus}.
     */
    private String status;
    private InsuredCDB mainInsured;
    private Double premiumValue;
    private LocalDate dueDate;

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public InsuredCDB getMainInsured() {
        return mainInsured;
    }

    public void setMainInsured(InsuredCDB mainInsured) {
        this.mainInsured = mainInsured;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Double getPremiumValue() {
        return premiumValue;
    }

    public void setPremiumValue(Double premiumValue) {
        this.premiumValue = premiumValue;
    }

    public static class InsuredCDB {
        private String firstName;
        private String fullName;
        private String mobilePhone;
        private String email;
        private LocalDate dob;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getMobilePhone() {
            return mobilePhone;
        }

        public void setMobilePhone(String mobilePhone) {
            this.mobilePhone = mobilePhone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public LocalDate getDob() {
            return dob;
        }

        public void setDob(LocalDate dob) {
            this.dob = dob;
        }
    }
}
