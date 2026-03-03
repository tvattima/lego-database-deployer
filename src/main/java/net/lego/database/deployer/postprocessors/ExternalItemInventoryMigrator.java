package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v1.dto.BricklinkInventory;
import net.lego.data.v2.dao.*;
import net.lego.data.v2.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static net.lego.data.v2.dto.ExternalService.ExternalServiceType.BRICKLINK;

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


        List<BricklinkInventory> bricklinkInventoryList = bricklinkInventoryDaoV1.findAll();
        PercentCompleteTabulator percentCompleteTabulator = new PercentCompleteTabulator(bricklinkInventoryList.size(), 1.0d, d -> {
            log.info("percent completed %4.1f".formatted(d * 100));
        });

        bricklinkInventoryList
                .forEach(bricklinkInventory -> {
                    Optional<InventoryIndex> inventoryIndex = inventoryIndexDao.findByBoxIdAndBoxIndex(bricklinkInventory.getBoxId(), bricklinkInventory.getBoxIndex());
                    inventoryIndex.ifPresentOrElse(invIdx -> {
                        Optional<ExternalItem> externalItem = externalItemDao.findByExternalNumber(BRICKLINK.getExternalServiceId(), bricklinkInventory.getBlItemNo());
                        externalItem.ifPresentOrElse(ei -> {
                            Optional<ItemInventory> itemInventory = itemInventoryDao.findByUuid(bricklinkInventory.getUuid());
                            ExternalServiceItem externalServiceItem = externalServiceItemDao.findByExternalItemId(ei.getItemId())
                                    .orElseThrow(() -> new RuntimeException("Unable to find external service item for itemId [%d], uuid [%s], bricklink item number [%s]".formatted(ei.getItemId(), bricklinkInventory.getUuid(), bricklinkInventory.getBlItemNo())));
                            itemInventory.ifPresentOrElse(ii -> {
                                Optional<ExternalItemInventory> externalItemInventory = externalItemInventoryDao.findByExternalItemIdAndItemInventoryId(ei.getItemId(), ii.getItemInventoryId());
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
                    percentCompleteTabulator.incrementPercentComplete();
                });
    }
}
