package th.co.krungthaiaxa.ebiz.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.util.Date;

@SpringBootApplication
@EnableSwagger2
public class KalApiApplication {
    @Bean
    public Docket configureSwaggerDateAsString() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .build()
                .directModelSubstitute(LocalDate.class, Date.class);
    }

    public static void main(String[] args) {
		SpringApplication.run(KalApiApplication.class, args);
	}
}
