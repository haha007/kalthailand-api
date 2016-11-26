package th.co.krungthaiaxa.api.elife.system.health;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author khoi.tran on 11/25/16.
 */
@Service
public class SystemHealthService {
    public static final Logger LOGGER = LoggerFactory.getLogger(SystemHealthService.class);

    public SystemHealth loadHealthStatus() {
        SystemHealth healthStatus = new SystemHealth();
        healthStatus.setJvmTotalMemory(Runtime.getRuntime().totalMemory());
        healthStatus.setJvmFreeMemory(Runtime.getRuntime().freeMemory());
        healthStatus.setJvmMaxMemory(Runtime.getRuntime().maxMemory());
        healthStatus.setDiskSpaces(loadDiskSpace());
        return healthStatus;
    }

    private List<DiskSpace> loadDiskSpace() {
        List<DiskSpace> result = new ArrayList<>();
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            DiskSpace diskSpace = new DiskSpace();
            System.out.print(root + ": ");
            try {
                FileStore store = Files.getFileStore(root);
                diskSpace.setDriverPath(store.name());
                diskSpace.setUsableSpace(store.getUsableSpace());
                diskSpace.setUnallocatedSpace(store.getUnallocatedSpace());
                diskSpace.setTotalSpace(store.getTotalSpace());
                result.add(diskSpace);
            } catch (IOException e) {
                LOGGER.error("Cannot load disk space" + e.getMessage(), e);
            }
        }
        return result;
    }
}
