package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApiModel(description = "This is the description of a quote and only a quote. It holds all properties gathered from " +
        "user before the quote got transformed into a Policy")
public class Quote {
    @Id
    private String technicalId;
    private String quoteId;
    private CommonData commonData;
    private PremiumsDataLifeInsurance premiumsData;
    private List<Insured> insureds = new ArrayList<>();
    private List<Coverage> coverages = new ArrayList<>();

    @ApiModelProperty(required = true, value = "Technical ID of the quote. This should not be displayed to user")
    public String getTechnicalId() {
        return technicalId;
    }

    public void setTechnicalId(String technicalId) {
        this.technicalId = technicalId;
    }

    @ApiModelProperty(required = true, value = "Functional ID of the quote. This may be displayed to user")
    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    @ApiModelProperty(required = true, value = "Data common to all quotes commercial types")
    public CommonData getCommonData() {
        return commonData;
    }

    public void setCommonData(CommonData commonData) {
        this.commonData = commonData;
    }

    @ApiModelProperty(value = "Data concerning premiums (price for the coverage and benefit agreed for the quote)")
    public PremiumsDataLifeInsurance getPremiumsData() {
        return premiumsData;
    }

    public void setPremiumsData(PremiumsDataLifeInsurance premiumsData) {
        this.premiumsData = premiumsData;
    }

    @ApiModelProperty(value = "List of all insured parties stated on the quote")
    public List<Insured> getInsureds() {
        return Collections.unmodifiableList(insureds);
    }

    public void addInsured(Insured insured) {
        insureds.add(insured);
    }

    public List<Coverage> getCoverages() {
        return coverages;
    }

    public void addCoverage(Coverage coverage) {
        coverages.add(coverage);
    }
}
