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

    /**
     * This section defines the user account configured in properties file
     * This user will get the USER role that will be used to configure the security
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(adminUserName).password(adminUserPassword).roles("ADMIN", "USER", "UI");
        auth.inMemoryAuthentication()
                .withUser(autopayUserName).password(autopayUserPassword).roles("AUTOPAY", "USER", "UI");
        auth.inMemoryAuthentication()
                .withUser(validationUserName).password(validationUserPassword).roles("VALIDATION", "USER", "UI");
        auth.inMemoryAuthentication()
                .withUser(apiUserName).password(apiUserPassword).roles("USER", "UI");
    }

    /**
     * This section defines the security policy for the app.
     * - USER role is needed for every type of requests to any URL
     * - CSRF headers are disabled since we are only testing the REST interface, not a web one
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().and()
                .authorizeRequests()
                // UI rights
                .antMatchers(HttpMethod.GET, "/**/*.htm").hasRole("UI")
                .antMatchers(HttpMethod.GET, "/**/*.html").hasRole("UI")
                .antMatchers(HttpMethod.GET, "/**/*.css").hasRole("UI")
                .antMatchers(HttpMethod.GET, "/**/*.ttf").hasRole("UI")
                .antMatchers(HttpMethod.GET, "/**/*.woff").hasRole("UI")
                .antMatchers(HttpMethod.GET, "/**/*.js").hasRole("UI")
                // ADMIN rights
                .antMatchers(HttpMethod.GET, "/admin/**").hasAnyRole("ADMIN", "AUTOPAY", "VALIDATION")
                // USER rights
                .antMatchers(HttpMethod.DELETE, "/**").hasRole("USER")
                .antMatchers(HttpMethod.GET, "/**").hasRole("USER")
                .antMatchers(HttpMethod.POST, "/**").hasRole("USER")
                .antMatchers(HttpMethod.PUT, "/**").hasRole("USER")
                .and().csrf().disable();
    }
}
