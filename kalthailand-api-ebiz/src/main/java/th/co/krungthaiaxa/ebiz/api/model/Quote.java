package th.co.krungthaiaxa.ebiz.api.model;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Quote {
    @Id
    private String quoteId;
    private QuoteCommonData commonData;
    private PremiumsData premiumsData;
    private List<Insured> insureds = new ArrayList<>();

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public QuoteCommonData getCommonData() {
        return commonData;
    }

    public void setCommonData(QuoteCommonData commonData) {
        this.commonData = commonData;
    }

    public PremiumsData getPremiumsData() {
        return premiumsData;
    }

    public void setPremiumsData(PremiumsData premiumsData) {
        this.premiumsData = premiumsData;
    }

    public List<Insured> getInsureds() {
        return Collections.unmodifiableList(insureds);
    }

    public void addInsured(Insured insured) {
        insureds.add(insured);
    }
}
