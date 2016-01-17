package th.co.krungthaiaxa.ebiz.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@ActiveProfiles("dev")
public class KalApiStart {

    public static void main(String[] args) {
        SpringApplication.run(KalApiApplication.class, args);
    }
}
