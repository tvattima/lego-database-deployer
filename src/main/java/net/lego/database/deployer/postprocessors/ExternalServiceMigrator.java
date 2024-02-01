package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.ExternalServiceDao;
import net.lego.data.v2.dao.ExternalServiceTypeDao;
import net.lego.data.v2.dto.ExternalService;
import net.lego.data.v2.dto.ExternalServiceType;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalServiceMigrator implements PostProcessor {

    private final ExternalServiceDao externalServiceDao;
    private final ExternalServiceTypeDao externalServiceTypeDao;

    @Override
    public void execute() {
        log.info("ExternalServiceMigrator");
        getInitialExternalService()
                .forEach(es -> {
                    log.info("ExternalService [{}]", es);
                    externalServiceDao.findExternalServiceById(es.getExternalServiceId())
                                      .ifPresentOrElse(externalService -> {
                                                  log.info("Updating existing ExternalService [{}] to [{}]", externalService, es);
                                                  externalServiceDao.update(es);
                                              },
                                              () -> externalServiceDao.insert(es));
                });
    }

    public List<ExternalService> getInitialExternalService() {
        return List.of(
                ExternalService.builder()
                               .externalServiceId(1)
                               .externalServiceName("LEGO")
                               .externalServiceUrl("https://www.lego.com")
                               .externalServiceTypeId(getExternalServiceTypeId("LEGO"))
                               .build(),
                ExternalService.builder()
                               .externalServiceId(2)
                               .externalServiceName("BRICKLINK")
                               .externalServiceUrl("https://www.bricklink.com")
                               .externalServiceTypeId(getExternalServiceTypeId("MARKETPLACE"))
                               .build(),
                ExternalService.builder()
                               .externalServiceId(3)
                               .externalServiceName("EBAY")
                               .externalServiceUrl("https://www.ebay.com")
                               .externalServiceTypeId(getExternalServiceTypeId("AUCTION"))
                               .build(),
                ExternalService.builder()
                               .externalServiceId(4)
                               .externalServiceName("CATAWIKI")
                               .externalServiceUrl("https://www.catawiki.com")
                               .externalServiceTypeId(getExternalServiceTypeId("AUCTION"))
                               .build(),
                ExternalService.builder()
                               .externalServiceId(5)
                               .externalServiceName("LAURITZ")
                               .externalServiceUrl("https://www.lauritz.com")
                               .externalServiceTypeId(getExternalServiceTypeId("AUCTION"))
                               .build(),
                ExternalService.builder()
                               .externalServiceId(6)
                               .externalServiceName("QXL")
                               .externalServiceUrl("https://www.qxl.dk")
                               .externalServiceTypeId(getExternalServiceTypeId("AUCTION"))
                               .build()
        );
    }

    private Integer getExternalServiceTypeId(final String externalServiceTypeName) {
        return externalServiceTypeDao.findExternalServiceTypeByName(externalServiceTypeName)
                                     .map(ExternalServiceType::getExternalServiceTypeId)
                                     .orElseThrow(() -> new RuntimeException(String.format("Unable to find unique ExternalServiceType by name [%s]", externalServiceTypeName)));
    }
}