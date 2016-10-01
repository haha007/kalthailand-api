package th.co.krungthaiaxa.api.elife.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * @author khoi.tran on 9/30/16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
interface PremiumDetailIfc {

    Boolean getSumInsuredOption();

    void setSumInsuredOption(Boolean sumInsuredOption);

    Amount getSumInsured();

    void setSumInsured(Amount sumInsured);

    Amount getSumInsuredBeforeDiscount();

    void setSumInsuredBeforeDiscount(Amount sumInsuredBeforeDiscount);

    Amount getYearlyTaxDeduction();

    void setYearlyTaxDeduction(Amount yearlyTaxDeduction);

    Amount getTotalTaxDeduction();

    void setTotalTaxDeduction(Amount totalTaxDeduction);

    Amount getEndOfContractBenefit();

    void setEndOfContractBenefit(Amount endOfContractBenefit);

    Amount getDeathBenefit();

    void setDeathBenefit(Amount deathBenefit);

    List<DateTimeAmount> getYearlyDeathBenefits();

    void setYearlyDeathBenefits(List<DateTimeAmount> yearlyDeathBenefits);
}
