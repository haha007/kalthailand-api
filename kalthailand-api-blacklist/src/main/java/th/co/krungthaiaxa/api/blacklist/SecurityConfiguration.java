package th.co.krungthaiaxa.api.blacklist;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import th.co.krungthaiaxa.api.blacklist.filter.KalApiTokenFilter;
import th.co.krungthaiaxa.api.blacklist.filter.UnauthorizedHandler;

import javax.inject.Inject;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Inject
    private UnauthorizedHandler unauthorizedHandler;
    @Inject
    private KalApiTokenFilter kalApiTokenFilter;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // we don't need CSRF because our token is invulnerable
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // All requests should be authorized since validation will be done in filter using token
                .and()
                    .authorizeRequests()
                    .anyRequest().permitAll();

        // Custom filter to check for KAL API token
        httpSecurity.addFilterBefore(kalApiTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // disable page caching
        httpSecurity.headers().cacheControl();
    }
}
