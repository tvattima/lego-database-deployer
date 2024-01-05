package net.lego.database.deployer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@EnableConfigurationProperties
@SpringBootApplication(scanBasePackages = {"net.bricklink", "net.lego"}, exclude = DataSourceAutoConfiguration.class)
public class LegoDatabaseDeployerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LegoDatabaseDeployerApplication.class, args);
	}

}
