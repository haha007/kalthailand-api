package th.co.krungthaiaxa.api.signing;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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
@ComponentScan({ "th.co.krungthaiaxa.api.signing", "th.co.krungthaiaxa.api.common" })
public class KALApiSigning {
    @SuppressWarnings("squid:S2095")//Ignore the wrong Sonar check.
    public static void main(String[] args) {
        SpringApplication.run(KALApiSigning.class, args);
    }

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
                .title("Signing API")
                .description("Signing API for Krunghtai Axa")
                .version("1.0.0")
                .license("License")
                .build();
    }

    private Predicate<String> paths() {
        return Predicates.or(
                PathSelectors.regex("/documents.*"));
    }
}
