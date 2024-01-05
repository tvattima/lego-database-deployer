package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.TransactionTypeDao;
import net.lego.data.v2.dto.TransactionType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionTypeMigrator implements PostProcessor {

    private final TransactionTypeDao transactionTypeDao;
    private final net.lego.data.v1.dao.TransactionTypeDao transactionTypeDaoV1;

    @Override
    public void execute() {
        log.info("TransactionTypeMigrator");
        transactionTypeDaoV1.findAll()
                            .forEach(tt -> {
                                log.info("TransactionType [{}]", tt);
                                transactionTypeDao.findTransactionTypeByCode(tt.getTransactionTypeCode())
                                                  .ifPresentOrElse(transactionType -> {
                                                              log.info("Updating existing TransactionType [{}] to [{}]", transactionType, tt);
                                                              transactionTypeDao.udpate(TransactionType.builder()
                                                                                                       .transactionTypeDescription(tt.getTransactionTypeDescription())
                                                                                                       .conversionFactor(tt.getConversionFactor())
                                                                                                       .build());
                                                          },
                                                          () -> transactionTypeDao.insert(TransactionType.builder()
                                                                                                         .transactionTypeCode(tt.getTransactionTypeCode())
                                                                                                         .transactionTypeDescription(tt.getTransactionTypeDescription())
                                                                                                         .conversionFactor(tt.getConversionFactor())
                                                                                                         .build()));
                            });
    }
}
