package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.CostTypeDao;
import net.lego.data.v2.dto.CostType;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CostTypeMigrator implements PostProcessor {

    private final CostTypeDao costTypeDao;

    @Override
    public void execute() {
        log.info("CostTypeMigrator");
        getInitialCostTypes()
                .forEach(ct -> {
                    log.info("CostType [{}]", ct);
                    costTypeDao.findCostTypeByCode(ct.getCostTypeCode())
                               .ifPresentOrElse(costType -> {
                                           log.info("Updating existing CostType [{}] to [{}]", costType, ct);
                                           costTypeDao.update(ct);
                                       },
                                       () -> costTypeDao.insert(ct));
                });
    }

    public List<CostType> getInitialCostTypes() {
        return List.of(
                CostType.builder()
                        .costTypeCode("PRICE")
                        .costTypeName("Price")
                        .costTypeDescription("The for-sale price or purchase cost of an item")
                        .build(),
                CostType.builder()
                        .costTypeCode("SHIPPING")
                        .costTypeName("Shipping")
                        .costTypeDescription("The cost of shipping an item or an entire order")
                        .build(),
                CostType.builder()
                        .costTypeCode("FEE")
                        .costTypeName("Fee")
                        .costTypeDescription("A fee added to an item or entire order")
                        .build(),
                CostType.builder()
                        .costTypeCode("TAX")
                        .costTypeName("Tax")
                        .costTypeDescription("The sales tax amount added to an item or entire order")
                        .build(),
                CostType.builder()
                        .costTypeCode("DISCOUNT")
                        .costTypeName("Discount")
                        .costTypeDescription("A Discount added to an item or entire order")
                        .build(),
                CostType.builder()
                        .costTypeCode("INSURANCE")
                        .costTypeName("Insurance")
                        .costTypeDescription("The cost for insurance to ship an item or an entire order")
                        .build()
        );
    }
}
