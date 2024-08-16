package net.lego.database.deployer.postprocessors;

import com.bricklink.api.rest.client.BricklinkRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.CategoryDao;
import net.lego.data.v2.dto.Category;
import org.springframework.stereotype.Component;

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
        categoryDaoV1.findAll()
                .forEach(c -> {
                    log.info("Category [{}]", c);
                    categoryDao.findCategoryById(c.getCategoryId())
                            .ifPresentOrElse(category -> {
                                        log.info("Updating existing Category [{}] to [{}]", category, c);
                                        categoryDao.update(Category.builder()
                                                .categoryName(c.getCategoryName())
                                                .parentId(0)
                                                .build());
                                    },
                                    () -> categoryDao.insert(Category.builder()
                                            .categoryId(c.getCategoryId())
                                            .categoryName(c.getCategoryName())
                                            .parentId(0)
                                            .build()));
                });
        if (categoryDao.findCategoryById(0).isEmpty()) {
            categoryDao.insert(Category.builder()
                    .categoryId(0)
                    .categoryName("UNKNOWN")
                    .parentId(0)
                    .build());
        }

        log.info("Loading Bricklink Categories");
        bricklinkRestClient.getCategories()
                .getData()
                .forEach(bc -> {
                    log.info("Bricklink Category [{}]", bc);
                    categoryDao.findCategoryById(bc.getCategory_id())
                            .ifPresentOrElse(existingCategory -> {
                                        log.info("Updating existing Category [{}] to [{}]", existingCategory, bc);
                                        categoryDao.update(Category.builder()
                                                .categoryId(existingCategory.getCategoryId())
                                                .categoryName(bc.getCategory_name())
                                                .parentId(bc.getParent_id())
                                                .build());
                                    },
                                    () -> categoryDao.insert(Category.builder()
                                            .categoryId(bc.getCategory_id())
                                            .categoryName(bc.getCategory_name())
                                            .parentId(bc.getParent_id())
                                            .build()));
                });
    }
}
