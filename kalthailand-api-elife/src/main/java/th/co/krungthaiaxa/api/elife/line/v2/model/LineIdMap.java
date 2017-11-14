package th.co.krungthaiaxa.api.elife.line.v2.model;

/**
 * @author tuong.le on 10/24/17.
 */
public class LineIdMap {
    /**
     * The ID is used for LINE V2
     */
    private String lineUserId;

    /**
     * The ID is used for LINE V1 that is no longer supported
     */
    private String mid;

    public LineIdMap(final String mid, final String lineUserId) {
        this.lineUserId = lineUserId;
        this.mid = mid;
    }

    public String getLineUserId() {
        return lineUserId;
    }

    public void setLineUserId(String lineUserId) {
        this.lineUserId = lineUserId;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}
