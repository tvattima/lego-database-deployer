package net.lego.database.deployer.liquibase;

import net.lego.database.deployer.config.PostProcessorProperties;
import net.lego.database.deployer.postprocessors.PostProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class LiquibasePostProcessor {
    private final Map<String, PostProcessor> postProcessorMap;

    public LiquibasePostProcessor(final List<PostProcessor> postProcessors, final PostProcessorProperties postProcessorProperties, final DataSource sourceDataSource, final DataSource targetDataSource) {
        try {
            log.info("Configuring LiquibasePostProcessor with DataSources\r\n\t Source DataSource [{}]\n" +
                    "\t Target DataSource [{}]", sourceDataSource.getConnection()
                                                                 .getMetaData()
                                                                 .getURL(), targetDataSource.getConnection()
                                                                                            .getMetaData()
                                                                                            .getURL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        postProcessors.forEach(pp -> {
            log.info("Post Processor [{}]", pp);
        });

        postProcessorProperties.getGroups()
                               .keySet()
                               .forEach(groupName -> {
                                   log.info("Post Processor Group [{}]", groupName);
                                   postProcessorProperties.getGroups()
                                                          .get(groupName)
                                                          .forEach(pp -> {
                                                              log.info("\tPost Processor Name [{}]", pp);
                                                              PostProcessor postProcessor = findPostProcessor(postProcessors, pp);
                                                              postProcessor.execute();
                                                          });
                               });
        postProcessorMap = new HashMap<>();
        log.info("LiquibasePostProcessor initialized");
    }

    private PostProcessor findPostProcessor(final List<PostProcessor> postProcessors, final String postProcessorClassName) {
        return postProcessors.stream()
                             .filter(pp -> pp.getClass()
                                             .getSimpleName()
                                             .startsWith(postProcessorClassName))
                             .findFirst()
                             .orElseThrow(() -> new IllegalArgumentException(String.format("Unable to find PostProcessor class [%s]", postProcessorClassName)));
    }
}
