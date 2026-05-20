package net.lego.database.deployer.config;

import io.legohunter.lego.data.builder.DataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class TargetDataSourceConfiguration {

    @Bean
    @ConfigurationProperties("lego.databases")
    public Map<String, Object> databasesMap() {
        return new HashMap<>();
    }

    @Bean("targetDataSource")
    @ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DataSource targetDataSource(@Qualifier("databasesMap") Map<String, Object> databasesMap, Environment environment) {
        String databaseKeyName = environment.getProperty("target-database.datasource.database-key-name");
        if (databaseKeyName == null) {
            throw new IllegalStateException("target-database.datasource.database-key-name property not set");
        }

        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
        return dataSourceBuilder.dataSource(databaseKeyName, databasesMap, "target-database.datasource.hikari", environment);
    }
}
