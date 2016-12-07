package th.co.krungthaiaxa.api.elife.commission.data;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author khoi.tran on 8/30/16.
 *         This is the result of comission calculation for one policy.
 *         The class {@link CommissionCalculationSession} store calculation result of all policies in one session.
 */

public class CommissionCalculation {
    @Field("policyNo")
    private String policyNumber;
    private String policyStatus;
    private String planCode;
    private String paymentCode;
    private String agentCode;
    private String customerCategory;
    private String previousPolicyNo;
    private String existingAgentCode1;
    private String existingAgentCode1Status;
    private String existingAgentCode2;
    private String existingAgentCode2Status;

    private Double firstYearPremium;
    private Double firstYearCommission;
    private Double fyAffiliateCommission;
    private Double fyDistribution1Commission;
    private Double fyDistribution2Commission;
    private Double fyTsrCommission;
    private Double fyMarketingCommission;
    private Double fyCompanyCommission;
    private Double ovAffiliateCommission;
    private Double ovDistribution1Commission;
    private Double ovDistribution2Commission;
    private Double ovTsrCommission;
    private Double ovMarketingCommission;
    private Double ovCompanyCommission;

    private Double fyAffiliateRate;
    private Double fyDistributionRate;
    private Double fyTsrRate;
    private Double fyMarketingRate;
    private Double fyCompanyRate;
    private Double ovAffiliateRate;
    //TODO should migrate, original field name is ovDistributiionRate
    @Field("ovDistributiionRate")
    private Double ovDistributionRate;
    private Double ovTsrRate;
    private Double ovMarketingRate;
    private Double ovCompanyRate;

    private String resultCode;
    private String resultMessage;

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(String policyStatus) {
        this.policyStatus = policyStatus;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    public String getCustomerCategory() {
        return customerCategory;
    }

    public void setCustomerCategory(String customerCategory) {
        this.customerCategory = customerCategory;
    }

    public String getPreviousPolicyNo() {
        return previousPolicyNo;
    }

    public void setPreviousPolicyNo(String previousPolicyNo) {
        this.previousPolicyNo = previousPolicyNo;
    }

    public String getExistingAgentCode1() {
        return existingAgentCode1;
    }

    public void setExistingAgentCode1(String existingAgentCode1) {
        this.existingAgentCode1 = existingAgentCode1;
    }

    public String getExistingAgentCode1Status() {
        return existingAgentCode1Status;
    }

    public void setExistingAgentCode1Status(String existingAgentCode1Status) {
        this.existingAgentCode1Status = existingAgentCode1Status;
    }

    public String getExistingAgentCode2() {
        return existingAgentCode2;
    }

    public void setExistingAgentCode2(String existingAgentCode2) {
        this.existingAgentCode2 = existingAgentCode2;
    }

    public String getExistingAgentCode2Status() {
        return existingAgentCode2Status;
    }

    public void setExistingAgentCode2Status(String existingAgentCode2Status) {
        this.existingAgentCode2Status = existingAgentCode2Status;
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

    public Double getFyAffiliateCommission() {
        return fyAffiliateCommission;
    }

    public void setFyAffiliateCommission(Double fyAffiliateCommission) {
        this.fyAffiliateCommission = fyAffiliateCommission;
    }

    public Double getFyDistribution1Commission() {
        return fyDistribution1Commission;
    }

    public void setFyDistribution1Commission(Double fyDistribution1Commission) {
        this.fyDistribution1Commission = fyDistribution1Commission;
    }

    public Double getFyDistribution2Commission() {
        return fyDistribution2Commission;
    }

    public void setFyDistribution2Commission(Double fyDistribution2Commission) {
        this.fyDistribution2Commission = fyDistribution2Commission;
    }

    public Double getFyTsrCommission() {
        return fyTsrCommission;
    }

    public void setFyTsrCommission(Double fyTsrCommission) {
        this.fyTsrCommission = fyTsrCommission;
    }

    public Double getFyMarketingCommission() {
        return fyMarketingCommission;
    }

    public void setFyMarketingCommission(Double fyMarkingCommission) {
        this.fyMarketingCommission = fyMarkingCommission;
    }

    public Double getFyCompanyCommission() {
        return fyCompanyCommission;
    }

    public void setFyCompanyCommission(Double fyCompanyCommission) {
        this.fyCompanyCommission = fyCompanyCommission;
    }

    public Double getOvAffiliateCommission() {
        return ovAffiliateCommission;
    }

    public void setOvAffiliateCommission(Double ovAffiliateCommission) {
        this.ovAffiliateCommission = ovAffiliateCommission;
    }

    public Double getOvDistribution1Commission() {
        return ovDistribution1Commission;
    }

    public void setOvDistribution1Commission(Double ovDistribution1Commission) {
        this.ovDistribution1Commission = ovDistribution1Commission;
    }

    public Double getOvDistribution2Commission() {
        return ovDistribution2Commission;
    }

    public void setOvDistribution2Commission(Double ovDistribution2Commission) {
        this.ovDistribution2Commission = ovDistribution2Commission;
    }

    public Double getOvTsrCommission() {
        return ovTsrCommission;
    }

    public void setOvTsrCommission(Double ovTsrCommission) {
        this.ovTsrCommission = ovTsrCommission;
    }

    public Double getOvMarketingCommission() {
        return ovMarketingCommission;
    }

    public void setOvMarketingCommission(Double ovMarkingCommission) {
        this.ovMarketingCommission = ovMarkingCommission;
    }

    public Double getOvCompanyCommission() {
        return ovCompanyCommission;
    }

    public void setOvCompanyCommission(Double ovCompanyCommission) {
        this.ovCompanyCommission = ovCompanyCommission;
    }

    public Double getFyAffiliateRate() {
        return fyAffiliateRate;
    }

    public void setFyAffiliateRate(Double fyAffiliateRate) {
        this.fyAffiliateRate = fyAffiliateRate;
    }

    public Double getFyDistributionRate() {
        return fyDistributionRate;
    }

    public void setFyDistributionRate(Double fyDistributionRate) {
        this.fyDistributionRate = fyDistributionRate;
    }

    public Double getFyTsrRate() {
        return fyTsrRate;
    }

    public void setFyTsrRate(Double fyTsrRate) {
        this.fyTsrRate = fyTsrRate;
    }

    public Double getFyMarketingRate() {
        return fyMarketingRate;
    }

    public void setFyMarketingRate(Double fyMarkingRate) {
        this.fyMarketingRate = fyMarkingRate;
    }

    public Double getFyCompanyRate() {
        return fyCompanyRate;
    }

    public void setFyCompanyRate(Double fyCompanyRate) {
        this.fyCompanyRate = fyCompanyRate;
    }

    public Double getOvAffiliateRate() {
        return ovAffiliateRate;
    }

    public void setOvAffiliateRate(Double ovAffiliateRate) {
        this.ovAffiliateRate = ovAffiliateRate;
    }

    public Double getOvDistributionRate() {
        return ovDistributionRate;
    }

    public void setOvDistributionRate(Double ovDistributionRate) {
        this.ovDistributionRate = ovDistributionRate;
    }

    public Double getOvTsrRate() {
        return ovTsrRate;
    }

    public void setOvTsrRate(Double ovTsrRate) {
        this.ovTsrRate = ovTsrRate;
    }

    public Double getOvMarketingRate() {
        return ovMarketingRate;
    }

    public void setOvMarketingRate(Double ovMarkingRate) {
        this.ovMarketingRate = ovMarkingRate;
    }

    public Double getOvCompanyRate() {
        return ovCompanyRate;
    }

    public void setOvCompanyRate(Double ovCompanyRate) {
        this.ovCompanyRate = ovCompanyRate;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}
