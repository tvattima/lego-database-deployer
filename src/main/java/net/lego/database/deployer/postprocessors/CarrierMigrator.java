package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.CarrierDao;
import net.lego.data.v2.dto.Carrier;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarrierMigrator implements PostProcessor {

    private final CarrierDao carrierDao;

    @Override
    public void execute() {
        log.info("CarrierMigrator");
        getInitialCarriers()
                .forEach(c -> {
                    log.info("Carrier [{}]", c);
                    carrierDao.findCarrierByCode(c.getCarrierCode())
                              .ifPresentOrElse(carrier -> {
                                          log.info("Updating existing Carrier [{}] to [{}]", carrier, c);
                                          carrierDao.update(c);
                                      },
                                      () -> carrierDao.insert(c));
                });
    }

    public List<Carrier> getInitialCarriers() {
        return List.of(
                Carrier.builder()
                       .carrierCode("USPS")
                       .carrierName("United States Postal Service")
                       .trackingUrlPattern("https://tools.usps.com/go/TrackConfirmAction.action?tLabels=%s")
                       .build(),
                Carrier.builder()
                       .carrierCode("FEDEX")
                       .carrierName("Federal Express")
                       .trackingUrlPattern("https://www.fedex.com/fedextrack/?action=track&trackingnumber=%s")
                       .build(),
                Carrier.builder()
                       .carrierCode("DHL")
                       .carrierName("Dalsey, Hillblom and Lynn")
                       .trackingUrlPattern("https://www.dhl.com/us-en/home/tracking.html?tracking-id=%s&submit=1")
                       .build(),
                Carrier.builder()
                       .carrierCode("UPS")
                       .carrierName("United Parcel Service")
                       .trackingUrlPattern("http://wwwapps.ups.com/WebTracking/processRequest?HTMLVersion=5.0&Requester=NES&AgreeToTermsAndConditions=yes&loc=en_US&tracknum=%s")
                       .build());
    }
}
