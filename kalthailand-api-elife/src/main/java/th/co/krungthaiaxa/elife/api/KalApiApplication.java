package th.co.krungthaiaxa.elife.api;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import th.co.krungthaiaxa.elife.api.filter.ClientSideRoleFilter;
import th.co.krungthaiaxa.elife.api.tmc.TMCClient;

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

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("th.co.krungthaiaxa.elife.api.tmc.wsdl");
        return marshaller;
    }

    @Bean
    public TMCClient tmcClient(Jaxb2Marshaller marshaller) {
        TMCClient client = new TMCClient();
        client.setDefaultUri(tmcWebServiceUrl);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        client.setMessageSender(webServiceMessageSender());
        return client;
    }

    @Bean
    public WebServiceMessageSender webServiceMessageSender() {
        HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
        sender.setReadTimeout(120 * 1000);
        sender.setConnectionTimeout(120 * 1000);
        return sender;
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
