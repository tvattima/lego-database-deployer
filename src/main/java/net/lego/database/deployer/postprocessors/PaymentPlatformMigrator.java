package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.PaymentPlatformDao;
import net.lego.data.v2.dto.PaymentPlatform;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentPlatformMigrator implements PostProcessor {

    private final PaymentPlatformDao paymentPlatformDao;

    @Override
    public void execute() {
        log.info("PaymentPlatformMigrator");
        getInitialPaymentPlatforms()
                .forEach(pp -> {
                    log.info("PaymentPlatform [{}]", pp);
                    paymentPlatformDao.findPaymentPlatformById(pp.getPaymentPlatformId())
                            .ifPresentOrElse(existingPaymentPlatform -> {
                                        log.info("Updating existing PaymentPlatform [{}] to [{}]", existingPaymentPlatform, pp);
                                        paymentPlatformDao.update(pp);
                                    },
                                    () -> paymentPlatformDao.insert(pp));
                });
    }

    public List<PaymentPlatform> getInitialPaymentPlatforms() {
        return List.of(
                PaymentPlatform.builder()
                        .paymentPlatformId(1)
                        .paymentPlatformName("PayPal")
                        .paymentPlatformUrl("https://www.paypal.com/")
                        .build(),
                PaymentPlatform.builder()
                        .paymentPlatformId(2)
                        .paymentPlatformName("Credit Card")
                        .paymentPlatformUrl(null)
                        .build()
        );
    }
}