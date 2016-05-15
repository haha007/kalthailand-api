package th.co.krungthaiaxa.api.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@ActiveProfiles("dev")
public class KALApiAuthStart {

    public static void main(String[] args) {
        SpringApplication.run(KALApiAuth.class, args);
    }
}
