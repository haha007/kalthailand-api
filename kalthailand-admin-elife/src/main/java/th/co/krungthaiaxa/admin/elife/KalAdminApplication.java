package th.co.krungthaiaxa.admin.elife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import th.co.krungthaiaxa.admin.elife.filter.ClientSideRoleFilter;

import javax.inject.Inject;

@SpringBootApplication
@EnableScheduling
@ComponentScan({ "th.co.krungthaiaxa.admin.elife", "th.co.krungthaiaxa.api.common" })
public class KalAdminApplication {

    @SuppressWarnings("squid:S2095")//Ignore the wrong Sonar check.
    public static void main(String[] args) {
        SpringApplication.run(KalAdminApplication.class, args);
    }

    @Inject
    private ClientSideRoleFilter clientSideRoleFilter;

    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(clientSideRoleFilter);
        registration.addUrlPatterns("*");
        registration.setName("Client side role filter");
        return registration;
    }
}
