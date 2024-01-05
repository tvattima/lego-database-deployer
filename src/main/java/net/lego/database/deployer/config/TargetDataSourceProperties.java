package net.lego.database.deployer.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.ibatis.configuration.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "target-database.datasource")
public class TargetDataSourceProperties extends DataSourceProperties {
}
