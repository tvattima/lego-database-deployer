package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v1.dto.BricklinkInventory;
import net.lego.data.v2.dao.ExternalItemDao;
import net.lego.data.v2.dao.ExternalServiceItemDao;
import net.lego.data.v2.dao.InventoryIndexDao;
import net.lego.data.v2.dao.ItemInventoryDao;
import net.lego.data.v2.dto.ExternalItem;
import net.lego.data.v2.dto.ExternalServiceItem;
import net.lego.data.v2.dto.InventoryIndex;
import net.lego.data.v2.dto.ItemInventory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemInventoryMigrator implements PostProcessor {
    private final net.lego.data.v1.dao.BricklinkInventoryDao bricklinkInventoryDaoV1;
    private final ExternalItemDao externalItemDao;
    private final ItemInventoryDao itemInventoryDao;
    private final InventoryIndexDao inventoryIndexDao;
    private final ExternalServiceItemDao externalServiceItemDao;

    @Override
    public void execute() {
        log.info("ItemInventoryMigrator");

        List<BricklinkInventory> bricklinkInventoryList = bricklinkInventoryDaoV1.findAll();
        PercentCompleteTabulator percentCompleteTabulator = new PercentCompleteTabulator(bricklinkInventoryList.size(), 1.0d, d -> {
            log.info("percent completed %4.1f".formatted(d*100));
        });
        bricklinkInventoryDaoV1.findAll()
                .forEach(bricklinkInventory -> {
                    Optional<InventoryIndex> inventoryIndex = inventoryIndexDao.findByBoxIdAndBoxIndex(bricklinkInventory.getBoxId(), bricklinkInventory.getBoxIndex());
                    inventoryIndex.ifPresentOrElse(invIdx -> {
                        Optional<ExternalItem> externalItem = externalItemDao.findByExternalNumber(bricklinkInventory.getBlItemNo());
                        externalItem.ifPresentOrElse(ei -> {
                            Optional<ItemInventory> itemInventory = itemInventoryDao.findByUuid(bricklinkInventory.getUuid());
                            ExternalServiceItem externalServiceItem = externalServiceItemDao.findByExternalItemId(ei.getExternalItemId()).orElseThrow(() -> new RuntimeException("Unable to find external service item for externalItemId [%d], uuid [%s], bricklink item number [%s]".formatted(ei.getExternalItemId(), bricklinkInventory.getUuid(), bricklinkInventory.getBlItemNo())));
                            itemInventory.ifPresentOrElse(ii -> {
                                itemInventoryDao.update(ItemInventory.builder()
                                        .uuid(bricklinkInventory.getUuid())
                                        .itemId(externalServiceItem.getItemId())
                                        .boxId(bricklinkInventory.getBoxId())
                                        .boxName(invIdx.getBoxName())
                                        .quantity(bricklinkInventory.getQuantity())
                                        .description(bricklinkInventory.getDescription())
                                        .active(invIdx.isActive())
                                        .forSale(bricklinkInventory.getForSale())
                                        .newOrUsed(bricklinkInventory.getNewOrUsed())
                                        .completeness(bricklinkInventory.getCompleteness())
                                        .itemConditionId(null)
                                        .boxConditionId(bricklinkInventory.getBoxConditionId())
                                        .instructionsConditionId(bricklinkInventory.getInstructionsConditionId())
                                        .sealed(bricklinkInventory.getSealed())
                                        .builtOnce(bricklinkInventory.getBuiltOnce())
                                        .build());
                            }, () -> {
                                itemInventoryDao.insert(ItemInventory.builder()
                                        .uuid(bricklinkInventory.getUuid())
                                        .itemId(externalServiceItem.getItemId())
                                        .boxId(bricklinkInventory.getBoxId())
                                        .boxName(invIdx.getBoxName())
                                        .quantity(bricklinkInventory.getQuantity())
                                        .description(bricklinkInventory.getDescription())
                                        .active(invIdx.isActive())
                                        .forSale(bricklinkInventory.getForSale())
                                        .newOrUsed(bricklinkInventory.getNewOrUsed())
                                        .completeness(bricklinkInventory.getCompleteness())
                                        .itemConditionId(null)
                                        .boxConditionId(bricklinkInventory.getBoxConditionId())
                                        .instructionsConditionId(bricklinkInventory.getInstructionsConditionId())
                                        .sealed(bricklinkInventory.getSealed())
                                        .builtOnce(bricklinkInventory.getBuiltOnce())
                                        .build());
                            });
                        }, () -> log.warn("Unable to find external item for bricklink item number [{}]", bricklinkInventory.getBlItemNo()));
                    }, () -> log.warn("Unable to find Inventory Index for Box Id [{}] and Box Index [{}]", bricklinkInventory.getBoxId(), bricklinkInventory.getBoxIndex()));
                    percentCompleteTabulator.incrementPercentComplete();
                });
    }
}
