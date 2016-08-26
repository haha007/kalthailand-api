package th.co.krungthaiaxa.api.elife.model;

/**
 * @author khoi.tran on 8/26/16.
 */
public class SessionQuoteCount {
    private String productId;
    private Long quoteCount;

    public SessionQuoteCount() {}

    public SessionQuoteCount(String productId, Long quoteCount) {
        this.productId = productId;
        this.quoteCount = quoteCount;
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
}
