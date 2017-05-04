package th.co.krungthaiaxa.api.elife.model;

import java.time.LocalDateTime;

/**
 * @author tuong.le on 3/10/17.
 */
public class QuoteMid {

    private String productId;
    private String mid;
    private LocalDateTime creationDate;

    public QuoteMid() {
        //Empty constructor
    }

    public QuoteMid(final String productId, final String mid, final LocalDateTime creationDate) {
        this.productId = productId;
        this.mid = mid;
        this.creationDate = creationDate;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(final String productId) {
        this.productId = productId;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(final String mid) {
        this.mid = mid;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }


}
