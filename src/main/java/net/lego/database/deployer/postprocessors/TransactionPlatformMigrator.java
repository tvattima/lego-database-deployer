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
                    transactionPlatformDao.findTransactionPlatformById(tp.getTransactionPlatformId())
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
                                   .transactionPlatformId(1)
                                   .transactionPlatformName("Bricklink")
                                   .build(),
                TransactionPlatform.builder()
                                   .transactionPlatformId(2)
                                   .transactionPlatformName("eBay")
                                   .build(),
                TransactionPlatform.builder()
                                   .transactionPlatformId(3)
                                   .transactionPlatformName("Private")
                                   .build(),
                TransactionPlatform.builder()
                                   .transactionPlatformId(4)
                                   .transactionPlatformName("CataWiki")
                                   .build(),
                TransactionPlatform.builder()
                        .transactionPlatformId(5)
                        .transactionPlatformName("Lauritz")
                        .build()
        );
    }
}
