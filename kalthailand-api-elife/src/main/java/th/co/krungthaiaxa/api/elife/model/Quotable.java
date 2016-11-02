package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModelProperty;
import th.co.krungthaiaxa.api.elife.model.product.PremiumsData;

import java.util.List;

/**
 * @author khoi.tran on 10/28/16.
 */
public interface Quotable {
    @ApiModelProperty(required = true, value = "ID of the quote the policy is coming from")
    String getQuoteId();

    void setQuoteId(String quoteId);

    @ApiModelProperty(required = true, value = "ID of the policy. This may be displayed to user")
    String getPolicyId();

    void setPolicyId(String policyId);

    @ApiModelProperty(required = true, value = "Data common to all policies commercial types")
    CommonData getCommonData();

    void setCommonData(CommonData commonData);

    @ApiModelProperty(value = "Data concerning premiums (price for the coverage and benefit agreed for the policy)")
    PremiumsData getPremiumsData();

    void setPremiumsData(PremiumsData premiumsData);

    @ApiModelProperty(value = "List of all insured parties stated on the policy")
    List<Insured> getInsureds();

    void addInsured(Insured insured);

    @ApiModelProperty(value = "List of all coverages of the policy")
    List<Coverage> getCoverages();

    void addCoverage(Coverage coverage);

}
