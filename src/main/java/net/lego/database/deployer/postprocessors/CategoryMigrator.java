package net.lego.database.deployer.postprocessors;

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
                                                                           .categoryType(c.getCategoryType())
                                                                           .categoryName(c.getCategoryName())
                                                                           .build());
                                            },
                                            () -> categoryDao.insert(Category.builder()
                                                                             .categoryId(c.getCategoryId())
                                                                             .categoryType(c.getCategoryType())
                                                                             .categoryName(c.getCategoryName())
                                                                             .build()));
                     });
    }
}
