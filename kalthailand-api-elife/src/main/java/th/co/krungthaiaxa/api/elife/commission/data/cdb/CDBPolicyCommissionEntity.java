package th.co.krungthaiaxa.api.elife.commission.data.cdb;

/**
 * @author khoi.tran on 9/14/16.
 */
public class CDBPolicyCommissionEntity {
    private String policyNumber;
    private Double firstYearPremium;
    private Double firstYearCommission;

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public Double getFirstYearPremium() {
        return firstYearPremium;
    }

    public void setFirstYearPremium(Double firstYearPremium) {
        this.firstYearPremium = firstYearPremium;
    }

    public Double getFirstYearCommission() {
        return firstYearCommission;
    }

    public void setFirstYearCommission(Double firstYearCommission) {
        this.firstYearCommission = firstYearCommission;
    }
}
