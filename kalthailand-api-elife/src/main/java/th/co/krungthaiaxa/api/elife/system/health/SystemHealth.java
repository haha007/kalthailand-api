package th.co.krungthaiaxa.api.elife.system.health;

import th.co.krungthaiaxa.api.common.utils.NumberUtil;

import java.util.List;

/**
 * @author khoi.tran on 11/25/16.
 */
public class SystemHealth {
    /**
     * @see http://stackoverflow.com/questions/3571203/what-are-runtime-getruntime-totalmemory-and-freememory
     * @see https://github.com/oshi/oshi
     * Returns the total memory allocated from the system (which can at most reach the maximum memory value returned by the previous function).
     */
    private long jvmTotalMemory;
    /**
     * Returns the maximum amount of memory available to the Java Virtual Machine set by the '-mx' or '-Xmx' flags.
     */
    private long jvmMaxMemory;
    /**
     * Returns the free memory *within* the total memory returned by the previous function.
     */
    private long jvmFreeMemory;

    private long jvmUsedMemory;
    private float jvmUsedMemoryPercentage;

    public long getJvmUsedMemory() {
        jvmUsedMemory = (long) NumberUtil.consumed((double) jvmTotalMemory, (double) jvmFreeMemory);
        return jvmUsedMemory;
    }

    public float getJvmUsedMemoryPercentage() {
        jvmUsedMemoryPercentage = (float) NumberUtil.percentage((double) jvmMaxMemory, (double) getJvmUsedMemory());
        return jvmUsedMemoryPercentage;
    }

    private List<DiskSpace> diskSpaces;

    public long getJvmTotalMemory() {
        return jvmTotalMemory;
    }

    public void setJvmTotalMemory(long jvmTotalMemory) {
        this.jvmTotalMemory = jvmTotalMemory;
    }

    public long getJvmMaxMemory() {
        return jvmMaxMemory;
    }

    public void setJvmMaxMemory(long jvmMaxMemory) {
        this.jvmMaxMemory = jvmMaxMemory;
    }

    public long getJvmFreeMemory() {
        return jvmFreeMemory;
    }

    public void setJvmFreeMemory(long jvmFreeMemory) {
        this.jvmFreeMemory = jvmFreeMemory;
    }

    public List<DiskSpace> getDiskSpaces() {
        return diskSpaces;
    }

    public void setDiskSpaces(List<DiskSpace> diskSpaces) {
        this.diskSpaces = diskSpaces;
    }

    public void setJvmUsedMemory(long jvmUsedMemory) {
        this.jvmUsedMemory = jvmUsedMemory;
    }

    public void setJvmUsedMemoryPercentage(float jvmUsedMemoryPercentage) {
        this.jvmUsedMemoryPercentage = jvmUsedMemoryPercentage;
    }
}
