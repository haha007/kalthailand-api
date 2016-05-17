package th.co.krungthaiaxa.api.elife;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import th.co.krungthaiaxa.api.elife.filter.ClientSideRoleFilter;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Date;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
public class KalApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(KalApiApplication.class, args);
    }

    @Inject
    private ClientSideRoleFilter clientSideRoleFilter;
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

    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(clientSideRoleFilter);
        registration.addUrlPatterns("*");
        registration.setName("Client side role filter");
        return registration;
    }

    private ApiInfo metadata() {
        return new ApiInfoBuilder()
                .title("AXA TH DATA API")
                .description("This set of API is done by Krungthai Axa Life")
                .version("1.0.0")
                .license("License")
                .build();
    }

    private com.google.common.base.Predicate<String> paths() {
        return Predicates.or(
                PathSelectors.regex("/quotes.*"),
                PathSelectors.regex("/policies.*"),
                PathSelectors.regex("/products.*"),
                PathSelectors.regex("/decrypt.*"),
                PathSelectors.regex("/line.*"),
                PathSelectors.regex("/documents.*"));
    }
}