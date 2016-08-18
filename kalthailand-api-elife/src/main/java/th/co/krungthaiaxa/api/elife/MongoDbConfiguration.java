package th.co.krungthaiaxa.api.elife;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoTypeMapper;

import java.util.Arrays;
import java.util.List;

@Configuration
class MongoDbConfiguration extends AbstractMongoConfiguration {

    @Value("${spring.data.mongodb.username}")
    private String userName;
    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.database}")
    private String database;
    @Value("${spring.data.mongodb.host}")
    private String host;
    @Value("${spring.data.mongodb.port}")
    private Integer port;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    public Mongo mongo() throws Exception {
        ServerAddress serverAddress = new ServerAddress(host, port);
        MongoCredential mongoCredential = MongoCredential.createCredential(userName, database, password.toCharArray());
        List<MongoCredential> credentialsList = Arrays.asList(mongoCredential);
        return new MongoClient(serverAddress, credentialsList);
    }

    @Bean
    @Override
    public MappingMongoConverter mappingMongoConverter() throws Exception {
        MappingMongoConverter mmc = super.mappingMongoConverter();
        mmc.setTypeMapper(customTypeMapper());
        return mmc;
    }

    /**
     * This config will make Spring don't save class name into DB (remove field "_class").
     * Therefore we can refactor & rename those class in the future without causing any problem.
     * View more at http://athlan.pl/spring-data-mongodb-remove-_class-define-explicitly
     *
     * @return
     */
    @Bean
    public MongoTypeMapper customTypeMapper() {
        return new DefaultMongoTypeMapper(null);
    }
}