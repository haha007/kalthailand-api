package th.co.krungthaiaxa.api.elife.service.ereceipt;

/**
 * @author khoi.tran on 10/31/16.
 */
public class EreceiptNumber {
    /**
     * Note: the full number doesn't include the prefix character ({@link EreceiptPdfService#ERECEIPT_NUMBER_PREFIX}).
     */
    private String fullNumberBase36;
    private String mainNumberBase36;
    private Long mainNumberDecimal;
    private String suffixNumberBase36;

    public String getFullNumberBase36() {
        return fullNumberBase36;
    }

    public void setFullNumberBase36(String fullNumberBase36) {
        this.fullNumberBase36 = fullNumberBase36;
    }

    public String getMainNumberBase36() {
        return mainNumberBase36;
    }

    public void setMainNumberBase36(String mainNumberBase36) {
        this.mainNumberBase36 = mainNumberBase36;
    }

    public Long getMainNumberDecimal() {
        return mainNumberDecimal;
    }

    public void setMainNumberDecimal(Long mainNumberDecimal) {
        this.mainNumberDecimal = mainNumberDecimal;
    }

    public String getSuffixNumberBase36() {
        return suffixNumberBase36;
    }

    public void setSuffixNumberBase36(String suffixNumberBase36) {
        this.suffixNumberBase36 = suffixNumberBase36;
    }
}
