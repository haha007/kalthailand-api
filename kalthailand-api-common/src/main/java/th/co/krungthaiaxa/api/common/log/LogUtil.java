package th.co.krungthaiaxa.api.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * @author khoi.tran on 8/29/16.
 */
public class LogUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);

    public static Instant logFinishing(Instant startTime, String msg) {
        Instant now = Instant.now();
        long runTimeMilli = now.toEpochMilli() - startTime.toEpochMilli();
        long runTimeSeconds = runTimeMilli / 1000;
        LOGGER.debug(String.format("\n%s\n\tStart time: %s, End time: %s\n\tRuntime: %s ms ~ %s s", msg, startTime, now, runTimeMilli, runTimeSeconds));
        return now;
    }

    public static Instant logStarting(String message) {
        Instant startTime = Instant.now();
        StringBuilder sb = new StringBuilder("'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''");
        sb.append("\n").append(message);
        sb.append("\n\tStart time: ").append(startTime);
        LOGGER.debug(sb.toString());
        return startTime;
    }
}
