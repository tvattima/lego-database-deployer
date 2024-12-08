package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.*;
import net.lego.data.v2.dto.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BricklinkItemInventoryMigrator implements PostProcessor {
    private final BricklinkItemInventoryDao bricklinkItemInventoryDao;
    private final ExternalItemInventoryDao externalItemInventoryDao;
    private final ItemInventoryDao itemInventoryDao;
    private final net.lego.data.v1.dao.BricklinkInventoryDao bricklinkInventoryDaoV1;
    private final ExternalItemDao externalItemDao;
    private final InventoryIndexDao inventoryIndexDao;
    private final ExternalServiceItemDao externalServiceItemDao;

    @Override
    public void execute() {
        log.info("BricklinkItemInventoryMigrator");

        bricklinkInventoryDaoV1.findAll()
                .forEach(bricklinkInventory -> {
                    log.info("BricklinkInventory [{}]", bricklinkInventory);

                    Optional<InventoryIndex> inventoryIndex = inventoryIndexDao.findByBoxIdAndBoxIndex(bricklinkInventory.getBoxId(), bricklinkInventory.getBoxIndex());
                    inventoryIndex.ifPresentOrElse(invIdx -> {
                        Optional<ExternalItem> externalItem = externalItemDao.findByExternalNumber(bricklinkInventory.getBlItemNo());
                        externalItem.ifPresentOrElse(ei -> {
                            ItemInventory itemInventory = itemInventoryDao.findByUuid(bricklinkInventory.getUuid())
                                    .orElseThrow(() -> new RuntimeException("Unable to find item inventory for uuid [%s]".formatted(bricklinkInventory.getUuid())));
                            ExternalServiceItem externalServiceItem = externalServiceItemDao.findByExternalItemId(ei.getExternalItemId())
                                    .orElseThrow(() -> new RuntimeException("Unable to find external service item for externalItemId [%d], uuid [%s], bricklink item number [%s]".formatted(ei.getExternalItemId(), bricklinkInventory.getUuid(), bricklinkInventory.getBlItemNo())));
                            ExternalItemInventory externalItemInventory = externalItemInventoryDao.findByExternalItemIdAndItemInventoryId(externalServiceItem.getExternalItemId(), itemInventory.getItemInventoryId())
                                    .orElseThrow(() -> new RuntimeException("Unable to find external item inventory for externalItemId [%d], uuid [%s], bricklink item number [%s]".formatted(ei.getExternalItemId(), bricklinkInventory.getUuid(), bricklinkInventory.getBlItemNo())));
                            Optional<BricklinkItemInventory> bricklinkItemInventory = bricklinkItemInventoryDao.findByExternalItemIdAndItemInventoryId(externalItemInventory.getExternalItemId(), itemInventory.getItemInventoryId());
                            bricklinkItemInventory.ifPresentOrElse(bii -> {
                                bricklinkItemInventoryDao.update(
                                        BricklinkItemInventory.builder()
                                                .inventoryId(bii.getInventoryId())
                                                .itemType(bii.getItemType())
                                                .colorId(bii.getColorId())
                                                .colorName(bii.getColorName())
                                                .quantity(bii.getQuantity())
                                                .unitPrice(bii.getUnitPrice())
                                                .bindId(bii.getBindId())
                                                .description(bii.getDescription())
                                                .remarks(bii.getRemarks())
                                                .bulk(bii.getBulk())
                                                .isRetain(bii.getIsRetain())
                                                .isStockRoom(bii.getIsStockRoom())
                                                .stockRoomId(bii.getStockRoomId())
                                                .dateCreated(bii.getDateCreated())
                                                .myCost(bii.getMyCost())
                                                .saleRate(bii.getSaleRate())
                                                .tierQuantity1(bii.getTierQuantity1())
                                                .tierQuantity2(bii.getTierQuantity2())
                                                .tierQuantity3(bii.getTierQuantity3())
                                                .tierPrice1(bii.getTierPrice1())
                                                .tierPrice2(bii.getTierPrice2())
                                                .tierPrice3(bii.getTierPrice3())
                                                .myWeight(bii.getMyWeight())
                                                .build()
                                );
                            }, () -> {
                                bricklinkItemInventoryDao.insert(
                                        BricklinkItemInventory.builder()
                                                .externalItemId(externalItemInventory.getExternalItemId())
                                                .itemInventoryId(itemInventory.getItemInventoryId())
                                                .inventoryId(bricklinkInventory.getInventoryId())
                                                .itemType(bricklinkInventory.getItemType())
                                                .colorId(bricklinkInventory.getColorId())
                                                .colorName(bricklinkInventory.getColorName())
                                                .quantity(bricklinkInventory.getQuantity())
                                                .unitPrice(bricklinkInventory.getUnitPrice())
                                                .bindId(bricklinkInventory.getBindId())
                                                .description(bricklinkInventory.getDescription())
                                                .remarks(bricklinkInventory.getRemarks())
                                                .bulk(bricklinkInventory.getBulk())
                                                .isRetain(bricklinkInventory.getIsRetain())
                                                .isStockRoom(bricklinkInventory.getIsStockRoom())
                                                .stockRoomId(bricklinkInventory.getStockRoomId())
                                                .dateCreated(Optional.ofNullable(bricklinkInventory.getDateCreated()).map(date -> date.atZone(ZoneId.of("America/New_York"))).orElse(null))
                                                .myCost(bricklinkInventory.getMyCost())
                                                .saleRate(bricklinkInventory.getSaleRate())
                                                .tierQuantity1(bricklinkInventory.getTierQuantity1())
                                                .tierQuantity2(bricklinkInventory.getTierQuantity2())
                                                .tierQuantity3(bricklinkInventory.getTierQuantity3())
                                                .tierPrice1(bricklinkInventory.getTierPrice1())
                                                .tierPrice2(bricklinkInventory.getTierPrice2())
                                                .tierPrice3(bricklinkInventory.getTierPrice3())
                                                .myWeight(bricklinkInventory.getMyWeight())
                                                .build()
                                );
                            });
                        }, () -> {
                            //externalItemInventoryDao.insert();
                            log.warn("Cannot insert ExternalItemInventory - no ItemInventory found for uuid {}", bricklinkInventory.getUuid());
                        });
                    }, () -> log.warn("Unable to find external item for bricklink item number [{}]", bricklinkInventory.getBlItemNo()));
                });
    }
}
