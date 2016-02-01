package th.co.krungthaiaxa.ebiz.api;

import com.google.common.base.Predicates;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
public class KalApiApplication {
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


    public static void main(String[] args) {
		SpringApplication.run(KalApiApplication.class, args);
	}

    private ApiInfo metadata() {
        return new ApiInfoBuilder()
                .title("AXA TH DATA API")
                .description("This set of API is done by Krungthai Axa Life")
                .version("1.0.0")
                .contact("")
                .build();
    }

    private com.google.common.base.Predicate<String> paths() {
        return Predicates.or(
                PathSelectors.regex("/quotes.*"),
                PathSelectors.regex("/policies.*"),
                PathSelectors.regex("/decrypt.*"),
                PathSelectors.regex("/watermark.*"));
    }
}
