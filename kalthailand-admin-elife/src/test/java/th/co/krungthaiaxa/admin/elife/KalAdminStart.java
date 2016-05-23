package th.co.krungthaiaxa.admin.elife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@ActiveProfiles("dev")
public class KalAdminStart {

    public static void main(String[] args) {
        SpringApplication.run(KalAdminApplication.class, args);
    }
}
