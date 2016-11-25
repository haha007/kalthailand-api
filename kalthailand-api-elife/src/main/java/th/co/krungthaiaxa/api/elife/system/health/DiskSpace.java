package th.co.krungthaiaxa.api.elife.system.health;

import th.co.krungthaiaxa.api.common.utils.NumberUtil;

/**
 * @author khoi.tran on 11/25/16.
 */
public class DiskSpace {
    private String driverPath;
    private long totalSpace;
    private long usableSpace;
    private long unallocatedSpace;

    public double getUsedSpacePercentage() {
        return NumberUtil.consumedPercentage((double) totalSpace, (double) usableSpace);
    }

    public String getDriverPath() {
        return driverPath;
    }

    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public long getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(long usableSpace) {
        this.usableSpace = usableSpace;
    }

    public long getUnallocatedSpace() {
        return unallocatedSpace;
    }

    public void setUnallocatedSpace(long unallocatedSpace) {
        this.unallocatedSpace = unallocatedSpace;
    }
}
