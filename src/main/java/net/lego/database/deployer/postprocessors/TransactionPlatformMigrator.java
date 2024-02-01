package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.TransactionPlatformDao;
import net.lego.data.v2.dto.TransactionPlatform;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionPlatformMigrator implements PostProcessor {

    private final TransactionPlatformDao transactionPlatformDao;

    @Override
    public void execute() {
        log.info("TransactionPlatformMigrator");
        getInitialTransactionPlatforms()
                .forEach(tp -> {
                    log.info("TransactionPlatform [{}]", tp);
                    transactionPlatformDao.findTransactionPlatformByCode(tp.getTransactionPlatformCode())
                                          .ifPresentOrElse(transactionPlatform -> {
                                                      log.info("Updating existing TransactionPlatform [{}] to [{}]", transactionPlatform, tp);
                                                      transactionPlatformDao.update(tp);
                                                  },
                                                  () -> transactionPlatformDao.insert(tp));
                });
    }

    public List<TransactionPlatform> getInitialTransactionPlatforms() {
        return List.of(
                TransactionPlatform.builder()
                                   .transactionPlatformCode("BRICKLINK")
                                   .transactionPlatformName("Bricklink")
                                   .build(),
                TransactionPlatform.builder()
                                   .transactionPlatformCode("BRICKLINK")
                                   .transactionPlatformName("eBay")
                                   .build(),
                TransactionPlatform.builder()
                                   .transactionPlatformCode("PRIVATE")
                                   .transactionPlatformName("Private")
                                   .build(),
                TransactionPlatform.builder()
                                   .transactionPlatformCode("CATAWIKI")
                                   .transactionPlatformName("CataWiki")
                                   .build()
        );
    }
}
