package net.lego.database.deployer.postprocessors;

import com.bricklink.api.ajax.PagingBricklinkAjaxClient;
import com.bricklink.api.ajax.model.v1.Type;
import com.bricklink.api.ajax.support.SearchProductResult;
import com.bricklink.web.api.BricklinkWebService;
import com.bricklink.web.model.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.ExternalItemDao;
import net.lego.data.v2.dao.ExternalServiceDao;
import net.lego.data.v2.dto.ExternalItem;
import net.lego.data.v2.dto.ExternalService;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class BricklinkExternalItemMigrator implements PostProcessor {

    private final ExternalServiceDao externalServiceDao;
    private final ExternalItemDao externalItemDao;
    private final BricklinkWebService bricklinkWebService;
    private final PagingBricklinkAjaxClient pagingBricklinkAjaxClient;

    ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
    CompletionService<String> itemCompletionService = new ExecutorCompletionService<>(taskExecutor);
    Integer externalServiceId;

    @Override
    public void execute() {
        log.info("BricklinkExternalItemMigrator");
        final String externalServiceName = "BRICKLINK";
        externalServiceId = externalServiceDao.findExternalServiceByName(externalServiceName).map(ExternalService::getExternalServiceId).orElseThrow();

        loadBooks();
        loadSets();
        loadGear();

        taskExecutor.shutdown();
        try {
            taskExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSets() {
        AtomicInteger itemCount = new AtomicInteger(0);

        // Load all Bricklink Sets
        itemCount.set(0);
        StopWatch timer = new StopWatch();
        timer.start();

        Set<Item> catalogItems = bricklinkWebService.getAllSetTypeCatalogItems();
        catalogItems.forEach(catalogItem -> {
            itemCount.incrementAndGet();
            itemCompletionService.submit(new ItemCallable(catalogItem, externalServiceId, "S"));
        });

        log.info("Loading {} Bricklink Set Catalog items", itemCount.get());
        PercentCompleteTabulator percentCompleteTabulator = new PercentCompleteTabulator(itemCount.get(), .01d, d -> {
            log.info("percent completed %4.1f".formatted(d));
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
        log.info("BricklinkExternalItemMigrator completed load of [{}] items in [{}] ms", itemCount.get(), timer.getTotalTimeMillis());
    }

    private void loadBooks() {
        AtomicInteger itemCount = new AtomicInteger(0);

        // Load all Bricklink Books
        CompletionService<String> itemCompletionService = new ExecutorCompletionService<>(taskExecutor);
        itemCount.set(0);
        StopWatch timer = new StopWatch();
        timer.start();

        Set<Item> catalogItems = bricklinkWebService.getAllBookTypeCatalogItems();
        catalogItems.forEach(catalogItem -> {
            itemCount.incrementAndGet();
            itemCompletionService.submit(new ItemCallable(catalogItem, externalServiceId, "B"));
        });

        log.info("Loading {} Bricklink Book Catalog items", itemCount.get());
        PercentCompleteTabulator percentCompleteTabulator = new PercentCompleteTabulator(itemCount.get(), .01d, d -> {
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
        log.info("BricklinkExternalItemMigrator completed load of [{}] items in [{}] ms", itemCount.get(), timer.getTotalTimeMillis());
    }

    private void loadGear() {
        AtomicInteger itemCount = new AtomicInteger(0);

        // Load all Bricklink Gear
        CompletionService<String> itemCompletionService = new ExecutorCompletionService<>(taskExecutor);
        itemCount.set(0);
        StopWatch timer = new StopWatch();
        timer.start();

        Set<Item> catalogItems = bricklinkWebService.getAllGearTypeCatalogItems();
        catalogItems.forEach(catalogItem -> {
            itemCount.incrementAndGet();
            itemCompletionService.submit(new ItemCallable(catalogItem, externalServiceId, "G"));
        });

        log.info("Loading {} Bricklink Gear Catalog items", itemCount.get());
        PercentCompleteTabulator percentCompleteTabulator = new PercentCompleteTabulator(itemCount.get(), .01d, d -> {
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
        log.info("BricklinkExternalItemMigrator completed load of [{}] items in [{}] ms", itemCount.get(), timer.getTotalTimeMillis());
    }

    @RequiredArgsConstructor
    private class ItemCallable implements Callable<String> {
        private final Item catalogItem;
        private final Integer externalServiceId;
        private final String externalItemType;

        @Override
        public String call() throws Exception {
            final AtomicLong externalUniqueId = new AtomicLong(0);
            externalItemDao.findByExternalNumber(catalogItem.getItemId()).ifPresentOrElse(externalItem -> {
                externalUniqueId.set(externalItem.getExternalUniqueId());
                if ((externalUniqueId.get() == 0L) || (!catalogItem.getItemId().equals(externalItem.getExternalNumber()))) {
                    externalUniqueId.set(lookupBricklinkInternalItemId(catalogItem.getItemId(), externalItemType));
                }
                externalItemDao.update(ExternalItem.builder()
                        .externalItemId(externalItem.getExternalItemId())
                        .externalNumber(externalItem.getExternalNumber())
                        .externalName(catalogItem.getItemName())
                        .externalItemType(externalItemType)
                        .externalUniqueId(externalUniqueId.get())
                        .externalUrl(null)
                        .build());
            }, () -> {
                externalUniqueId.set(lookupBricklinkInternalItemId(catalogItem.getItemId(), externalItemType));
                externalItemDao.insert(ExternalItem.builder()
                        .externalNumber(catalogItem.getItemId())
                        .externalName(catalogItem.getItemName())
                        .externalUniqueId(externalUniqueId.get())
                        .externalItemType(externalItemType)
                        .externalUrl(null)
                        .externalServiceId(externalServiceId)
                        .build());
            });

            return catalogItem.getItemId();
        }
    }

    private Integer lookupBricklinkInternalItemId(String itemId, String externalItemType) {
        Integer externalUniqueId = 0;
        Map<String, Object> params = new HashMap<>();
        params.clear();
        params.put("q", itemId);
        params.put("type", externalItemType);
        SearchProductResult searchProductResult = pagingBricklinkAjaxClient.searchProduct(params);
        List<Type> bricklinkItemSearch = searchProductResult.getResult().getTypeList();

        List<com.bricklink.api.ajax.model.v1.Item> items = bricklinkItemSearch.stream()
                .flatMap(itemResult -> itemResult.getItems().stream())
                .filter(item -> item.getStrItemNo().startsWith(itemId))
                .toList();

        if (items.size() == 1) {
            com.bricklink.api.ajax.model.v1.Item item = items.get(0);
            externalUniqueId = item.getIdItem();
        }
        return externalUniqueId;
    }
}
