package th.co.krungthaiaxa.api.elife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

/**
 * TODO should remove
 *
 * @deprecated This class seem not to be used any more.
 */
@Deprecated
@SpringBootApplication
@ActiveProfiles("dev")
public class KalApiStart {
    @SuppressWarnings("squid:S2095")//Ignore the wrong Sonar check.
    public static void main(String[] args) {
        SpringApplication.run(KalApiElifeApplication.class, args);
    }
}
