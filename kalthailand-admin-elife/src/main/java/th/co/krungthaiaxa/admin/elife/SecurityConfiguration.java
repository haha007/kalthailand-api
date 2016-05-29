package th.co.krungthaiaxa.admin.elife;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.DELETE, "/**").denyAll()
                .antMatchers(HttpMethod.GET, "/**").permitAll()
                .antMatchers(HttpMethod.HEAD, "/**").denyAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").denyAll()
                .antMatchers(HttpMethod.PATCH, "/**").denyAll()
                .antMatchers(HttpMethod.POST, "/**").permitAll()
                .antMatchers(HttpMethod.PUT, "/**").denyAll()
                .antMatchers(HttpMethod.TRACE, "/**").denyAll();

        httpSecurity.headers().cacheControl();
    }
}
