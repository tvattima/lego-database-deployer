package net.lego.database.deployer.postprocessors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitialMigrationPostProcessor implements PostProcessor {

    @Override
    public void execute() {
        log.info("InitialMigrationPostProcessor");
    }
}
