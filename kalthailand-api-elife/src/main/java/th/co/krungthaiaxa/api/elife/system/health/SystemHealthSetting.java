package th.co.krungthaiaxa.api.elife.system.health;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author khoi.tran on 11/26/16.
 */
public class SystemHealthSetting {
    @NotNull
    @NotEmpty
    private List<String> warningEmails;
    @NotNull
    private Float usedMemoryPercentageWarning;
    @NotNull
    private Float usedSpacePercentageWarning;

    public List<String> getWarningEmails() {
        return warningEmails;
    }

    public void setWarningEmails(List<String> warningEmails) {
        this.warningEmails = warningEmails;
    }

    public Float getUsedMemoryPercentageWarning() {
        return usedMemoryPercentageWarning;
    }

    public void setUsedMemoryPercentageWarning(Float usedMemoryPercentageWarning) {
        this.usedMemoryPercentageWarning = usedMemoryPercentageWarning;
    }

    public Float getUsedSpacePercentageWarning() {
        return usedSpacePercentageWarning;
    }

    public void setUsedSpacePercentageWarning(Float usedSpacePercentageWarning) {
        this.usedSpacePercentageWarning = usedSpacePercentageWarning;
    }
}
