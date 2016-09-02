package th.co.krungthaiaxa.api.elife.commission.data;

/**
 * @author khoi.tran on 8/30/16.
 */
public class CommissionTargetEntity {
    private CommissionTargetEntityType targetEntityType;
    private double percentage;

    public CommissionTargetEntityType getTargetEntityType() {
        return targetEntityType;
    }

    public void setTargetEntityType(CommissionTargetEntityType targetEntityType) {
        this.targetEntityType = targetEntityType;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
