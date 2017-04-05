package th.co.krungthaiaxa.api.elife;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class DataSourceConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfiguration.class);
    private static final String CONNECTED_MSG = "Connected to {}";

    //CDB /////////////////////////////////////
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "datasource.cdb")
    public DataSource cdbDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "cdbTemplate")
    public JdbcTemplate cdbTemplate() throws SQLException {
        final DataSource dataSource = cdbDataSource();
        if (isValidDataSource(dataSource)) {
            LOGGER.info(CONNECTED_MSG, "CDB");
            return new JdbcTemplate(dataSource);
        }
        throw new SQLException("Could not connect to CDB datasource");
    }

    //CDB DATA VIEW /////////////////////////////////////
    @Bean
    @ConfigurationProperties(prefix = "datasource.cdb-view")
    public DataSource cdbViewDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "cdbViewTemplate")
    public JdbcTemplate cdbViewTemplate() throws SQLException {
        final DataSource dataSource = cdbViewDataSource();
        if (isValidDataSource(dataSource)) {
            LOGGER.info(CONNECTED_MSG, "CDB View");
            return new JdbcTemplate(dataSource);
        }
        throw new SQLException("Could not connect to CDB View datasource");
    }

    //POLICY-PREMIUM POLICY /////////////////////////////////////
    @Bean
    @ConfigurationProperties(prefix = "datasource.cdb.policy-premium")
    public DataSource policyPremiumCdbDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "policyPremiumCdbTemplate")
    public JdbcTemplate policyPremiumCdbTemplate() throws SQLException {
        final DataSource dataSource = policyPremiumCdbDataSource();
        if (isValidDataSource(dataSource)) {
            LOGGER.info(CONNECTED_MSG, "CDB Policy-Premium");
            return new JdbcTemplate(dataSource);
        }
        throw new SQLException("Could not connect to CDB Policy-Premium datasource");
    }

    //LINE-CB /////////////////////////////////////
    @Bean
    @ConfigurationProperties(prefix = "datasource.linebc")
    public DataSource lineBCDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "linebcTemplate")
    public JdbcTemplate linebcTemplate() throws Exception {
        final DataSource dataSource = lineBCDataSource();
        if (isValidDataSource(dataSource)) {
            LOGGER.info(CONNECTED_MSG, "Line BC");
            return new JdbcTemplate(dataSource);
        }
        throw new SQLException("Could not connect to Line BC datasource");
    }

    /**
     * Check the data source is valid or not.
     *
     * @param dataSource
     * @return true if it can connect to database with the dataSource,
     * false if can not.
     */
    private boolean isValidDataSource(final DataSource dataSource) {
        try {
            return dataSource.getConnection().isValid(5);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
