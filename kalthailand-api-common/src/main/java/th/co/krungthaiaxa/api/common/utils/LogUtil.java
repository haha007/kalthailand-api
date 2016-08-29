package th.co.krungthaiaxa.api.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * @author khoi.tran on 8/29/16.
 */
public class LogUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);

    public static Instant logRuntime(Instant startTime, String msg) {
        Instant now = Instant.now();
        long runTimeMilli = now.toEpochMilli() - startTime.toEpochMilli();
        long runTimeSeconds = runTimeMilli / 1000;
        LOGGER.debug(String.format("%s \t Runtime: %s ms ~ %s s", msg, runTimeMilli, runTimeSeconds));
        return now;
    }
}
