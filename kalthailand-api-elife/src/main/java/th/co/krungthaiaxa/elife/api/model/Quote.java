package th.co.krungthaiaxa.elife.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "This is the description of a quote and only a quote. It holds all properties gathered from " +
        "user before the quote got transformed into a Policy")
@Document
public class Quote implements Serializable {
    @Id
    private String id;
    @Indexed(unique = true)
    private String quoteId;
    private CommonData commonData;
    private PremiumsData premiumsData;
    private List<Insured> insureds = new ArrayList<>();
    private List<Coverage> coverages = new ArrayList<>();
    private LocalDateTime creationDateTime;
    private LocalDateTime lastUpdateDateTime;

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
    public PremiumsData getPremiumsData() {
        return premiumsData;
    }

    public void setPremiumsData(PremiumsData premiumsData) {
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

    @ApiModelProperty(value = "Creation date and time of the Quote.")
    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    @ApiModelProperty(value = "Last update date and time of the Quote.")
    public LocalDateTime getLastUpdateDateTime() {
        return lastUpdateDateTime;
    }

    public void setLastUpdateDateTime(LocalDateTime lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quote quote = (Quote) o;
        return Objects.equals(id, quote.id) &&
                Objects.equals(quoteId, quote.quoteId) &&
                Objects.equals(commonData, quote.commonData) &&
                Objects.equals(premiumsData, quote.premiumsData) &&
                Objects.equals(insureds, quote.insureds) &&
                Objects.equals(coverages, quote.coverages) &&
                Objects.equals(creationDateTime, quote.creationDateTime) &&
                Objects.equals(lastUpdateDateTime, quote.lastUpdateDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quoteId, commonData, premiumsData, insureds, coverages, creationDateTime, lastUpdateDateTime);
    }
}
