package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.*;
import net.lego.data.v2.dto.*;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalItemInventoryMigrator implements PostProcessor {
    private final ExternalItemInventoryDao externalItemInventoryDao;
    private final ItemInventoryDao itemInventoryDao;
    private final net.lego.data.v1.dao.BricklinkInventoryDao bricklinkInventoryDaoV1;
    private final ExternalItemDao externalItemDao;
    private final InventoryIndexDao inventoryIndexDao;
    private final ExternalServiceItemDao externalServiceItemDao;

    @Override
    public void execute() {
        log.info("ExternalItemInventory");

        bricklinkInventoryDaoV1.findAll()
                .forEach(bricklinkInventory -> {
                    log.info("BricklinkInventory [{}]", bricklinkInventory);

                    Optional<InventoryIndex> inventoryIndex = inventoryIndexDao.findByBoxIdAndBoxIndex(bricklinkInventory.getBoxId(), bricklinkInventory.getBoxIndex());
                    inventoryIndex.ifPresentOrElse(invIdx -> {
                        Optional<ExternalItem> externalItem = externalItemDao.findByExternalNumber(bricklinkInventory.getBlItemNo());
                        externalItem.ifPresentOrElse(ei -> {
                            Optional<ItemInventory> itemInventory = itemInventoryDao.findByUuid(bricklinkInventory.getUuid());
                            ExternalServiceItem externalServiceItem = externalServiceItemDao.findByExternalItemId(ei.getExternalItemId())
                                    .orElseThrow(() -> new RuntimeException("Unable to find external service item for externalItemId [%d], uuid [%s], bricklink item number [%s]".formatted(ei.getExternalItemId(), bricklinkInventory.getUuid(), bricklinkInventory.getBlItemNo())));
                            itemInventory.ifPresentOrElse(ii -> {
                                Optional<ExternalItemInventory> externalItemInventory = externalItemInventoryDao.findByExternalItemIdAndItemInventoryId(ei.getExternalItemId(), ii.getItemInventoryId());
                                externalItemInventory.ifPresentOrElse(eii -> {
                                    externalItemInventoryDao.update(ExternalItemInventory.builder()
                                            .fixedPrice(eii.getFixedPrice())
                                            .orderId(eii.getOrderId())
                                            .extendedDescription(eii.getExtendedDescription())
                                            .extraDescription(eii.getExtraDescription())
                                            .internalComments(eii.getInternalComments())
                                            .updateTimestamp(eii.getUpdateTimestamp())
                                            .lastSynchronizedTimestamp(eii.getLastSynchronizedTimestamp())
                                            .build());
                                }, () -> {
                                    externalItemInventoryDao.insert(ExternalItemInventory.builder()
                                            .externalItemId(externalServiceItem.getExternalItemId())
                                            .itemInventoryId(ii.getItemInventoryId())
                                            .fixedPrice(bricklinkInventory.getFixedPrice())
                                            .orderId(Optional.ofNullable(bricklinkInventory.getOrderId()).map(Integer::valueOf).orElse(null))
                                            .extendedDescription(bricklinkInventory.getExtendedDescription())
                                            .extraDescription(bricklinkInventory.getExtraDescription())
                                            .internalComments(bricklinkInventory.getInternalComments())
                                            .updateTimestamp(bricklinkInventory.getUpdateTimestamp())
                                            .lastSynchronizedTimestamp(bricklinkInventory.getLastSynchronizedTimestamp())
                                            .build());
                                });
                            }, () -> {
                                //externalItemInventoryDao.insert();
                                log.warn("Cannot insert ExternalItemInventory - no ItemInventory found for uuid {}", bricklinkInventory.getUuid());
                            });
                        }, () -> log.warn("Unable to find external item for bricklink item number [{}]", bricklinkInventory.getBlItemNo()));
                    }, () -> log.warn("Unable to find Inventory Index for Box Id [{}] and Box Index [{}]", bricklinkInventory.getBoxId(), bricklinkInventory.getBoxIndex()));
                });
    }
}
