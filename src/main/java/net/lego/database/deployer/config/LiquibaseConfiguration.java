package net.lego.database.deployer.config;

import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
public class LiquibaseConfiguration {
    private static final Logger log = LoggerFactory.getLogger(LiquibaseConfiguration.class);

    @Bean
    @ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SpringLiquibase liquibase(@Qualifier("targetDataSource") final DataSource targetDataSource, final LiquibaseProperties liquibaseProperties) throws Exception {
        log.info("Configuring Liquibase with DataSource [{}]", targetDataSource.getConnection().getMetaData().getURL());
        log.info("Liquibase parameters [{}]", liquibaseProperties.getParameters());
        if (targetDataSource.getConnection().getMetaData().getURL().contains("_dev_lego")) {
            throw new IllegalStateException("ABORTING : Target DataSource is _dev_lego");
        }
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(targetDataSource);
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.yaml");
        return liquibase;
    }
}
