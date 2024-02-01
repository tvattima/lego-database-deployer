package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.ExternalServiceTypeDao;
import net.lego.data.v2.dto.ExternalServiceType;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalServiceTypeMigrator implements PostProcessor {

    private final ExternalServiceTypeDao externalServiceTypeDao;

    @Override
    public void execute() {
        log.info("ExternalServiceTypeMigrator");
        getInitialExternalServiceType()
                .forEach(est -> {
                    log.info("ExternalServiceType [{}]", est);
                    externalServiceTypeDao.findExternalServiceTypeById(est.getExternalServiceTypeId())
                              .ifPresentOrElse(externalServiceType -> {
                                          log.info("Updating existing ExternalServiceType [{}] to [{}]", externalServiceType, est);
                                          externalServiceTypeDao.update(est);
                                      },
                                      () -> externalServiceTypeDao.insert(est));
                });
    }

    public List<ExternalServiceType> getInitialExternalServiceType() {
        return List.of(

                ExternalServiceType.builder()
                                   .externalServiceTypeId(1)
                                   .externalServiceTypeName("LEGO")
                                   .externalServiceTypeDescription("LEGO")
                                   .build(),
                ExternalServiceType.builder()
                                   .externalServiceTypeId(2)
                                   .externalServiceTypeName("MARKETPLACE")
                                   .externalServiceTypeDescription("Marketplace")
                                   .build(),
                ExternalServiceType.builder()
                                   .externalServiceTypeId(3)
                                   .externalServiceTypeName("AUCTION")
                                   .externalServiceTypeDescription("Auction")
                                   .build()
        );
    }
}
