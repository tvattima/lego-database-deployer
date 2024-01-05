package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.ConditionDao;
import net.lego.data.v2.dto.Condition;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConditionMigrator implements PostProcessor {

    private final ConditionDao conditionDao;
    private final net.lego.data.v1.dao.ConditionDao conditionDaoV1;

    @Override
    public void execute() {
        log.info("ConditionMigrator");
        conditionDaoV1.findAll()
                      .forEach(c -> {
                          log.info("Category [{}]", c);
                          conditionDao.findConditionById(c.getConditionId())
                                      .ifPresentOrElse(condition -> {
                                                  log.info("Updating existing Condition [{}] to [{}]", condition, c);
                                                  conditionDao.update(Condition.builder()
                                                                               .conditionCode(c.getConditionCode())
                                                                               .conditionDescription(c.getConditionDescription())
                                                                               .conditionText(c.getConditionText())
                                                                               .build());
                                              },
                                              () -> conditionDao.insert(Condition.builder()
                                                                                 .conditionId(c.getConditionId())
                                                                                 .conditionCode(c.getConditionCode())
                                                                                 .conditionDescription(c.getConditionDescription())
                                                                                 .conditionText(c.getConditionText())
                                                                                 .build()));
                      });
    }
}
