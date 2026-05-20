package net.lego.database.deployer;

import io.legohunter.lego.data.autoconfigure.LegoDataAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, LegoDataAutoConfiguration.class})
public class LegoDatabaseDeployerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LegoDatabaseDeployerApplication.class, args);
	}

}
