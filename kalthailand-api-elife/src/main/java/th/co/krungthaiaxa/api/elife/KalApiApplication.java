package th.co.krungthaiaxa.api.elife;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.util.Date;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
@ComponentScan({ "th.co.krungthaiaxa.api.elife", "th.co.krungthaiaxa.api.common" })
public class KalApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(KalApiApplication.class, args);
    }

    @Value("${tmc.webservice.url}")
    private String tmcWebServiceUrl;

    @Bean
    public Docket configureSwagger() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(paths())
                .build()
                .useDefaultResponseMessages(false)
                .pathMapping("/")
                .apiInfo(metadata())
                .directModelSubstitute(LocalDate.class, Date.class);
    }

    private ApiInfo metadata() {
        return new ApiInfoBuilder()
                .title("eLife API")
                .description("eLife API for Krunghtai Axa")
                .version("1.0.0")
                .license("License")
                .build();
    }

    private Predicate<String> paths() {
        return Predicates.or(
                PathSelectors.regex("/quotes.*")
                , PathSelectors.regex("/session-quotes.*")
                , PathSelectors.regex("/policies.*")
                , PathSelectors.regex("/products.*")
                , PathSelectors.regex("/decrypt.*")
                , PathSelectors.regex("/line.*")
                , PathSelectors.regex("/settings.*")
                , PathSelectors.regex("/documents.*")
                , PathSelectors.regex("/policy-quota.*")
                , PathSelectors.regex("/policy-numbers.*")
                , PathSelectors.regex("/encrypt.*")
                , PathSelectors.regex("/commissions.*")
                , PathSelectors.regex("/payments.*")
        );
    }
}
