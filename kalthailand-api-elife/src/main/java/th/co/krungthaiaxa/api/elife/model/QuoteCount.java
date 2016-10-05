package th.co.krungthaiaxa.api.elife.model;

/**
 * @author khoi.tran on 8/26/16.
 */
public class QuoteCount {
    private String productId;
    private Long quoteCount;
    private Long sessionQuoteCount;

    public QuoteCount() {}

    public QuoteCount(String productId, Long quoteCount, Long sessionQuoteCount) {
        this.productId = productId;
        this.quoteCount = quoteCount;
        this.sessionQuoteCount = sessionQuoteCount;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Long getQuoteCount() {
        return quoteCount;
    }

    public void setQuoteCount(Long quoteCount) {
        this.quoteCount = quoteCount;
    }

    public Long getSessionQuoteCount() {
        return sessionQuoteCount;
    }

    public void setSessionQuoteCount(Long sessionQuoteCount) {
        this.sessionQuoteCount = sessionQuoteCount;
    }
}
