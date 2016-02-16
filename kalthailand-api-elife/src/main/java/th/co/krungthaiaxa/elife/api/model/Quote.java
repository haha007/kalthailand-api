package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "This is the description of a quote and only a quote. It holds all properties gathered from " +
        "user before the quote got transformed into a Policy")
@Document
public class Quote {
    @Id
    private String id;
    @Indexed
    private String quoteId;
    private CommonData commonData;
    private PremiumsDataLifeInsurance premiumsData;
    private List<Insured> insureds = new ArrayList<>();
    private List<Coverage> coverages = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(required = true, value = "ID of the quote. This may be displayed to user")
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
        return insureds;
    }

    public void addInsured(Insured insured) {
        insureds.add(insured);
    }

    @ApiModelProperty(value = "List of all coverages of the quote. This is calculated by back end API and cannot be set by client.")
    public List<Coverage> getCoverages() {
        return coverages;
    }

    public void addCoverage(Coverage coverage) {
        coverages.add(coverage);
    }
}
