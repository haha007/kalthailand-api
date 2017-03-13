package th.co.krungthaiaxa.api.elife;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {
    //CDB /////////////////////////////////////
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "datasource.cdb")
    public DataSource cdbDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "cdbTemplate")
    public JdbcTemplate cdbTemplate() {
        return new JdbcTemplate(cdbDataSource());
    }

    //CDB DATA VIEW /////////////////////////////////////
    @Bean
    @ConfigurationProperties(prefix = "datasource.cdb-view")
    public DataSource cdbViewDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "cdbViewTemplate")
    public JdbcTemplate cdbViewTemplate() {
        return new JdbcTemplate(cdbViewDataSource());
    }

    //POLICY-PREMIUM POLICY /////////////////////////////////////
    @Bean
    @ConfigurationProperties(prefix = "datasource.cdb.policy-premium")
    public DataSource policyPremiumCdbDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "policyPremiumCdbTemplate")
    public JdbcTemplate policyPremiumCdbTemplate() {
        return new JdbcTemplate(policyPremiumCdbDataSource());
    }

    //LINE-CB /////////////////////////////////////
    @Bean
    @ConfigurationProperties(prefix = "datasource.linebc")
    public DataSource lineBCDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "linebcTemplate")
    public JdbcTemplate linebcTemplate() {
        return new JdbcTemplate(lineBCDataSource());
    }

}
