package th.co.krungthaiaxa.elife.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${security.api.user.name}")
    private String apiUserName;
    @Value("${security.api.user.password}")
    private String apiUserPassword;
    @Value("${security.ui.admin.user.name}")
    private String adminUserName;
    @Value("${security.ui.admin.user.password}")
    private String adminUserPassword;
    @Value("${security.ui.autopay.user.name}")
    private String autopayUserName;
    @Value("${security.ui.autopay.user.password}")
    private String autopayUserPassword;
    @Value("${security.ui.validation.user.name}")
    private String validationUserName;
    @Value("${security.ui.validation.user.password}")
    private String validationUserPassword;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(adminUserName).password(adminUserPassword).roles("ADMIN", "UI");
        auth.inMemoryAuthentication()
                .withUser(autopayUserName).password(autopayUserPassword).roles("AUTOPAY", "UI");
        auth.inMemoryAuthentication()
                .withUser(validationUserName).password(validationUserPassword).roles("VALIDATION", "UI");
        auth.inMemoryAuthentication()
                .withUser(apiUserName).password(apiUserPassword).roles("UI");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic()
            .and().authorizeRequests()
                // ADMIN rights
                .antMatchers(HttpMethod.GET, "/admin/**").hasAnyRole("ADMIN", "AUTOPAY", "VALIDATION")
                // USER rights
                .antMatchers(HttpMethod.GET, "/**").authenticated()
                .antMatchers(HttpMethod.POST, "/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/**").authenticated()
            .and().csrf().disable();
    }
}
