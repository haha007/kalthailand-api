package th.co.krungthaiaxa.api.elife.ereceipt;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author khoi.tran on 10/31/16.
 */
public class EreceiptNumber implements Serializable {

    /**
     * Note: the full number doesn't include the prefix character ({@link EreceiptPdfService#ERECEIPT_NUMBER_PREFIX}).
     */
    @Indexed
    @NotBlank
    private String fullNumberBase36;
    @Indexed
    @NotBlank
    private String mainNumberBase36;
    @Indexed
    @NotNull
    private Long mainNumberDecimal;
    @NotBlank
    private String suffixNumberBase36;
    @NotBlank
    private String fullNumberForDisplay;

    @Override
    public String toString() {
        return fullNumberBase36;
    }

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

    public String getFullNumberForDisplay() {
        if (StringUtils.isBlank(fullNumberForDisplay)) {
            if (mainNumberDecimal == null || StringUtils.isBlank(suffixNumberBase36)) {
                return null;
            }
            fullNumberForDisplay = mainNumberDecimal + suffixNumberBase36;
        }
        return fullNumberForDisplay;
    }

    public void setFullNumberForDisplay(String fullNumberForDisplay) {
        this.fullNumberForDisplay = fullNumberForDisplay;
    }
}
