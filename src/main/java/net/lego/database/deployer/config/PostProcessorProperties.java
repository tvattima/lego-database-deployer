package net.lego.database.deployer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "postprocessors")
public class PostProcessorProperties {
    Map<String, Set<String>> groups;
}
