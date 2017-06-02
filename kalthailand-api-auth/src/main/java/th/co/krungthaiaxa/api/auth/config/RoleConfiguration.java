package th.co.krungthaiaxa.api.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author tuong.le on 6/2/17.
 */
@Configuration
public class RoleConfiguration {

    /**
     * This configuration overrides default role prefix of spring security
     */
    @Bean
    public DefaultRolesPrefixPostProcessor defaultRolesPrefixPostProcessor() {
        return new DefaultRolesPrefixPostProcessor();
    }
}
