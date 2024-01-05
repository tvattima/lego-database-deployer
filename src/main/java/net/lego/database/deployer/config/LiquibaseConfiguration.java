package net.lego.database.deployer.config;

import net.lego.database.deployer.liquibase.LiquibasePostProcessor;
import net.lego.database.deployer.postprocessors.PostProcessor;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties(PostProcessorProperties.class)
public class LiquibaseConfiguration {

    @Bean
    public SpringLiquibase liquibase(@Qualifier("targetDataSource") final DataSource targetDataSource) throws Exception {
        log.info("Configuring Liquibase with DataSource [{}]", targetDataSource.getConnection().getMetaData().getURL());
        if (targetDataSource.getConnection().getMetaData().getURL().contains("_dev_lego")) {
            throw new IllegalStateException("ABORTING : Target DataSource is _dev_lego");
        }
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(targetDataSource);
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.yaml");
        return liquibase;
    }

    @Bean
    @DependsOn("liquibase")
    public LiquibasePostProcessor liquibasePostProcessor(final SpringLiquibase springLiquibase, final List<PostProcessor> postProcessors, final PostProcessorProperties postProcessorProperties, @Qualifier("sourceDataSource") final DataSource dataSource, @Qualifier("targetDataSource") final DataSource targetDataSource) {
        return new LiquibasePostProcessor(postProcessors, postProcessorProperties, dataSource, targetDataSource);
    }
}
