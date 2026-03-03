package net.lego.database.deployer.postprocessors;

import com.bricklink.api.ajax.PagingBricklinkAjaxClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v1.dao.BricklinkItemDao;
import net.lego.data.v1.dto.BricklinkItem;
import net.lego.data.v2.dao.ExternalItemDao;
import net.lego.data.v2.dao.ExternalServiceItemDao;
import net.lego.data.v2.dao.ItemDao;
import net.lego.data.v2.dto.ExternalItem;
import net.lego.data.v2.dto.ExternalServiceItem;
import net.lego.data.v2.dto.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.lego.data.v2.dto.ExternalService.ExternalServiceType.BRICKLINK;

@Slf4j
@Component
@RequiredArgsConstructor
public class BricklinkExternalServiceItemMigrator implements PostProcessor {

    private final ItemDao itemDao;
    private final PagingBricklinkAjaxClient pagingBricklinkAjaxClient;
    private final ExternalItemDao externalItemDao;
    private final ExternalServiceItemDao externalServiceItemDao;
    private final BricklinkItemDao bricklinkItemDao;


    ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
    CompletionService<String> itemCompletionService = new ExecutorCompletionService<>(taskExecutor);

    @Override
    public void execute() {
        log.info("BricklinkExternalServiceItemMigrator");

        AtomicInteger itemCount = new AtomicInteger(0);

        StopWatch timer = new StopWatch();
        timer.start();

        bricklinkItemDao.findAll()
                .forEach(bricklinkItem -> {
                    itemCount.incrementAndGet();
                    itemCompletionService.submit(new ItemCallable(bricklinkItem));
                });

        log.info("Loading {} Bricklink External Service Items", itemCount.get());

        PercentCompleteTabulator percentCompleteTabulator = new PercentCompleteTabulator(itemCount.get(), 1.0d, d -> {
            log.info("percent completed %4.1f".formatted(d*100));
        });

        try {
            for (int i = 0; i < itemCount.get(); i++) {
                itemCompletionService.take().get();
                percentCompleteTabulator.incrementPercentComplete();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        timer.stop();
        log.info("BricklinkExternalServiceItemMigrator completed load of [{}] items in [{}] ms", itemCount.get(), timer.getTotalTimeMillis());
        taskExecutor.shutdown();
        try {
            taskExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiredArgsConstructor
    private class ItemCallable implements Callable<String> {
        private final BricklinkItem bricklinkItem;

        @Override
        public String call() throws Exception {
            Optional<Item> internalItem = itemDao.findByItemId(bricklinkItem.getItemId());
            internalItem.ifPresentOrElse(item -> {
                Optional<ExternalItem> externalitem = externalItemDao.findByExternalNumber(BRICKLINK.getExternalServiceId(), bricklinkItem.getBlItemNumber());
                externalitem.ifPresentOrElse(externalItem -> {
                    if (externalServiceItemDao.findByExternalItemIdAndItemId(externalItem.getItemId(), bricklinkItem.getItemId()).isEmpty()) {
                        try {
                            externalServiceItemDao.insert(ExternalServiceItem.builder()
                                    .externalItemId(externalItem.getItemId())
                                    .itemId(bricklinkItem.getItemId())
                                    .build());
                        } catch (Exception e) {
                            log.error("Unable to insert into external_service_item values (%s, %s) Error :: %s".formatted(externalItem.getItemId(), bricklinkItem.getItemId(), e.getMessage()), e);
                        }
                    }
                }, () -> {
                    log.warn("External Item Number for Bricklink Item Number {} was not found", bricklinkItem.getBlItemNumber());
                });
            }, () -> {
                log.warn("Item Id {} was not found", bricklinkItem.getItemId());
            });
            return bricklinkItem.getBlItemNumber();
        }
    }
}
