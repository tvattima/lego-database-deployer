package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.InventoryIndexDao;
import net.lego.data.v2.dto.InventoryIndex;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryIndexMigrator implements PostProcessor {

    private final InventoryIndexDao inventoryIndexDao;
    private final net.lego.data.v1.dao.InventoryIndexDao inventoryIndexDaoV1;

    @Override
    public void execute() {
        log.info("InventoryIndexMigrator");

        List<net.lego.data.v1.dto.InventoryIndex> inventoryIndexList = inventoryIndexDaoV1.findAll();
        PercentCompleteTabulator percentCompleteTabulator = new PercentCompleteTabulator(inventoryIndexList.size(), 1.0d, d -> {
            log.info("percent completed %4.1f".formatted(d * 100));
        });

        inventoryIndexList.forEach(ii -> {
            inventoryIndexDao.findByBoxIdAndBoxIndexAndItemNumber(ii.getBoxId(), ii.getBoxIndex(), ii.getItemNumber())
                    .ifPresentOrElse(inventoryIndex -> {
                                inventoryIndexDao.update(InventoryIndex.builder()
                                        .boxId(ii.getBoxId())
                                        .boxIndex(ii.getBoxIndex())
                                        .itemNumber(ii.getItemNumber())
                                        .boxName(ii.getBoxName())
                                        .boxNumber(ii.getBoxNumber())
                                        .sealed(ii.getSealed())
                                        .quantity(ii.getQuantity())
                                        .description(ii.getDescription())
                                        .active(ii.isActive())
                                        .movedToBoxId(ii.getMovedToBoxId())
                                        .build());
                            },
                            () -> {
                                inventoryIndexDao.insert(InventoryIndex.builder()
                                        .boxId(ii.getBoxId())
                                        .boxIndex(ii.getBoxIndex())
                                        .itemNumber(ii.getItemNumber())
                                        .boxName(ii.getBoxName())
                                        .boxNumber(ii.getBoxNumber())
                                        .sealed(ii.getSealed())
                                        .quantity(ii.getQuantity())
                                        .description(ii.getDescription())
                                        .active(ii.isActive())
                                        .movedToBoxId(ii.getMovedToBoxId())
                                        .build());
                            });
            percentCompleteTabulator.incrementPercentComplete();
        });
    }
}
