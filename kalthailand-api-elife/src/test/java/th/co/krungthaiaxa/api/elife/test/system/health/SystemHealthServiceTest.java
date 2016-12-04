package th.co.krungthaiaxa.api.elife.test.system.health;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.system.health.DiskSpace;
import th.co.krungthaiaxa.api.elife.system.health.SystemHealth;
import th.co.krungthaiaxa.api.elife.system.health.SystemHealthService;

/**
 * @author khoi.tran on 12/4/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SystemHealthServiceTest {
    @Autowired
    private SystemHealthService systemHealthService;

    @Test
    public void test() {
        SystemHealth systemHealth = systemHealthService.loadHealthStatus();
        Assert.assertTrue("getJvmFreeMemory: " + systemHealth.getJvmFreeMemory(), systemHealth.getJvmFreeMemory() > 0);
        Assert.assertTrue("getJvmMaxMemory: " + systemHealth.getJvmMaxMemory(), systemHealth.getJvmMaxMemory() > 0);
        Assert.assertTrue("getJvmTotalMemory: " + systemHealth.getJvmTotalMemory(), systemHealth.getJvmTotalMemory() > 0);
        Assert.assertTrue("getJvmUsedMemory: " + systemHealth.getJvmUsedMemory(), systemHealth.getJvmUsedMemory() > 0);
        Assert.assertTrue("getJvmUsedMemoryPercentage: " + systemHealth.getJvmUsedMemoryPercentage(), systemHealth.getJvmUsedMemoryPercentage() > 0);

        Assert.assertTrue(systemHealth.getDiskSpaces().size() > 0);
        for (DiskSpace diskSpace : systemHealth.getDiskSpaces()) {
            Assert.assertTrue(StringUtils.isNotBlank(diskSpace.getDriverPath()));
            Assert.assertTrue(diskSpace.getTotalSpace() > 0);
            Assert.assertTrue(diskSpace.getUsableSpace() > 0);
            Assert.assertTrue(diskSpace.getUsedSpacePercentage() > 0);
        }
    }
}
