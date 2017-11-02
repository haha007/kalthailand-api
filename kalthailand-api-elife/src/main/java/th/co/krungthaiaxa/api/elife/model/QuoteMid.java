package th.co.krungthaiaxa.api.elife.model;

import java.time.LocalDateTime;

/**
 * @author tuong.le on 3/10/17.
 */
public class QuoteMid {

    private String productId;
    private String mid;
    private String lineUserId;
    private LocalDateTime creationDate;

    public QuoteMid() {
        //Empty constructor
    }

    public QuoteMid(final String productId, final String mid, final String lineUserId, final LocalDateTime creationDate) {
        this.productId = productId;
        this.mid = mid;
        this.lineUserId = lineUserId;
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

    public String getLineUserId() {
        return lineUserId;
    }

    public void setLineUserId(String lineUserId) {
        this.lineUserId = lineUserId;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }


}
