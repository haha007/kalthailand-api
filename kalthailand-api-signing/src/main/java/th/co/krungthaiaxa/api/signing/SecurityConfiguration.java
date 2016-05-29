package th.co.krungthaiaxa.api.signing;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import th.co.krungthaiaxa.api.signing.filter.KalApiTokenFilter;
import th.co.krungthaiaxa.api.signing.filter.UnauthorizedHandler;

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
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and()
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

        httpSecurity.addFilterBefore(kalApiTokenFilter, UsernamePasswordAuthenticationFilter.class);

        httpSecurity.headers().cacheControl();
    }
}
