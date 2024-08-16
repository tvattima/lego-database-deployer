package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v1.dto.Category;
import net.lego.data.v2.dao.ItemDao;
import net.lego.data.v2.dto.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemMigrator implements PostProcessor {

    private final ItemDao itemDao;
    private final net.lego.data.v1.dao.ItemDao itemDaoV1;

    ExecutorService taskExecutor = Executors.newFixedThreadPool(30);
    CompletionService<String> itemCompletionService = new ExecutorCompletionService<>(taskExecutor);

    @Override
    public void execute() {
        log.info("ItemMigrator");
        AtomicInteger itemCount = new AtomicInteger(0);

        StopWatch timer = new StopWatch();
        timer.start();
        itemDaoV1.findAll()
                .forEach(item -> {
                    itemCount.incrementAndGet();
                    itemCompletionService.submit(new ItemCallable(itemDao, item));
                });

        try {
            for (int i = 0; i < itemCount.get(); i++) {
                itemCompletionService.take().get();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        timer.stop();
        log.info("ItemMigrator completed load of [{}] items in [{}] ms", itemCount.get(), timer.getTotalTimeMillis());
        taskExecutor.shutdown();
        try {
            taskExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiredArgsConstructor
    private class ItemCallable implements Callable<String> {

        private final ItemDao itemDao;
        private final net.lego.data.v1.dto.Item item;

        @Override
        public String call() throws Exception {

            itemDao.findByItemId(item.getItemId())
                    .ifPresentOrElse(existingItem -> {
                        try {
                            itemDao.update(Item.builder()
                                    .itemNumber(item.getItemNumber())
                                    .itemName(item.getItemName())
                                    .notes(item.getNotes())
                                    .categoryId(item.getCategories().stream().findAny().map(Category::getCategoryId).orElse(0))
                                    .build());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, () -> {
                        try {
                            itemDao.insert(Item.builder()
                                    .itemId(item.getItemId())
                                    .itemNumber(item.getItemNumber())
                                    .itemName(item.getItemName())
                                    .notes(item.getNotes())
                                    .categoryId(item.getCategories().stream().findAny().map(Category::getCategoryId).orElse(0))
                                    .build());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
            return item.getItemNumber();
        }
    }
}
