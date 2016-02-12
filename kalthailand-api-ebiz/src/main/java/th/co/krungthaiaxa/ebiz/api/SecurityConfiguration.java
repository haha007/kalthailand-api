package th.co.krungthaiaxa.ebiz.api;

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

    @Value("${api.security.user.name}")
    private String apiUserName;
    @Value("${api.security.user.password}")
    private String apiUserPassword;

    /**
     * This section defines the user account configured in properties file
     * This user will get the USER role that will be used to configure the security
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(apiUserName).password(apiUserPassword).roles("USER");
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
                .antMatchers(HttpMethod.DELETE, "/**").hasRole("USER")
                .antMatchers(HttpMethod.GET, "/**").hasRole("USER")
                .antMatchers(HttpMethod.HEAD, "/**").hasRole("USER")
                .antMatchers(HttpMethod.OPTIONS, "/**").hasRole("USER")
                .antMatchers(HttpMethod.PATCH, "/**").hasRole("USER")
                .antMatchers(HttpMethod.POST, "/**").hasRole("USER")
                .antMatchers(HttpMethod.PUT, "/**").hasRole("USER")
                .antMatchers(HttpMethod.TRACE, "/**").hasRole("USER")
                .and().csrf().disable();
    }
}
