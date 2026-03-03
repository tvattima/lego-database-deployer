package net.lego.database.deployer.postprocessors;

import com.bricklink.api.rest.client.BricklinkRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.CategoryDao;
import net.lego.data.v2.dto.Category;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.lego.data.v2.dto.ExternalService.ExternalServiceType.BRICKLINK;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryMigrator implements PostProcessor {

    private final CategoryDao categoryDao;
    private final net.lego.data.v1.dao.CategoryDao categoryDaoV1;
    private final BricklinkRestClient bricklinkRestClient;

    @Override
    public void execute() {
        log.info("CategoryMigrator");
        List<net.lego.data.v1.dto.Category> categoryList = categoryDaoV1.findAll();
        PercentCompleteTabulator percentCompleteTabulator = new PercentCompleteTabulator(categoryList.size(), .01d, d -> {
            log.info("percent completed %4.1f".formatted(d));
        });
        categoryDaoV1.findAll()
                .forEach(c -> {
                    log.info("Category [{}]", c);
                    categoryDao.findCategoryByExternalServiceAndCategoryId(BRICKLINK.getExternalServiceId(), c.getCategoryId())
                            .ifPresentOrElse(category -> {
                                        log.info("Updating existing Category [{}] to [{}]", category, c);
                                        categoryDao.update(Category.builder()
                                                .categoryName(c.getCategoryName())
                                                .parentId(0)
                                                .build());
                                    },
                                    () -> categoryDao.insert(Category.builder()
                                            .externalCategoryId(c.getCategoryId())
                                            .categoryName(c.getCategoryName())
                                            .parentId(0)
                                            .build()));
                    percentCompleteTabulator.incrementPercentComplete();
                });
        if (categoryDao.findCategoryByExternalServiceAndCategoryId(BRICKLINK.getExternalServiceId(), 0).isEmpty()) {
            categoryDao.insert(Category.builder()
                    .externalCategoryId(0)
                    .categoryName("UNKNOWN")
                    .parentId(0)
                    .build());
        }

        log.info("Loading Bricklink Categories");
        List<com.bricklink.api.rest.model.v1.Category> bricklinkCategoryList = bricklinkRestClient.getCategories().getData();
        PercentCompleteTabulator percentCompleteTabulator2 = new PercentCompleteTabulator(bricklinkCategoryList.size(), .01d, d -> {
            log.info("percent completed %4.1f".formatted(d));
        });

        bricklinkCategoryList.forEach(bc -> {
            categoryDao.findCategoryByExternalServiceAndCategoryId(BRICKLINK.getExternalServiceId(), bc.getCategory_id())
                    .ifPresentOrElse(existingCategory -> {
                                categoryDao.update(Category.builder()
                                        .externalCategoryId(existingCategory.getExternalCategoryId())
                                        .categoryName(bc.getCategory_name())
                                        .parentId(bc.getParent_id())
                                        .build());
                            },
                            () -> categoryDao.insert(Category.builder()
                                    .externalCategoryId(bc.getCategory_id())
                                    .categoryName(bc.getCategory_name())
                                    .parentId(bc.getParent_id())
                                    .build()));
            percentCompleteTabulator2.incrementPercentComplete();
        });
    }
}
