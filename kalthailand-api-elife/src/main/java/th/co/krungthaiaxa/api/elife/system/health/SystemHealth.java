package th.co.krungthaiaxa.api.elife.system.health;

import th.co.krungthaiaxa.api.common.utils.NumberUtil;

import java.util.List;

/**
 * @author khoi.tran on 11/25/16.
 */
public class SystemHealth {
    /**
     * Returns the total memory allocated from the system (which can at most reach the maximum memory value returned by the previous function).
     */
    private long totalMemory;
    /**
     * Returns the maximum amount of memory available to the Java Virtual Machine set by the '-mx' or '-Xmx' flags.
     */
    private long maxMemory;
    /**
     * Returns the free memory *within* the total memory returned by the previous function.
     */
    private long freeMemory;

    private long usedMemory;
    private float usedMemoryPercentage;

    public long getUsedMemory() {
        usedMemory = (long) NumberUtil.consumed((double) totalMemory, (double) freeMemory);
        return usedMemory;
    }

    public float getUsedMemoryPercentage() {
        usedMemoryPercentage = (float) NumberUtil.consumedPercentage((double) totalMemory, (double) freeMemory);
        return usedMemoryPercentage;
    }

    private List<DiskSpace> diskSpaces;

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }

    public List<DiskSpace> getDiskSpaces() {
        return diskSpaces;
    }

    public void setDiskSpaces(List<DiskSpace> diskSpaces) {
        this.diskSpaces = diskSpaces;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public void setUsedMemoryPercentage(float usedMemoryPercentage) {
        this.usedMemoryPercentage = usedMemoryPercentage;
    }
}
