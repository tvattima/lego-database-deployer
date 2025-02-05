package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.PartyDao;
import net.lego.data.v2.dto.Party;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyMigrator implements PostProcessor {

    private final PartyDao partyDao;
    private final net.lego.data.v1.dao.PartyDao partyDaoV1;

    @Override
    public void execute() {
        log.info("PartyMigrator");

        partyDao.setAutoIncrementMode();

        List<net.lego.data.v1.dto.Party> partyList = partyDaoV1.findAll();
        PercentCompleteTabulator percentCompleteTabulator = new PercentCompleteTabulator(partyList.size(), .01d, d -> {
            log.info("percent completed %4.1f".formatted(d * 100));
        });
        partyList.forEach(p -> {
            partyDao.findPartyById(p.getPartyId())
                    .ifPresentOrElse(party -> {
                        log.info("parties found %s".formatted(party));
                                partyDao.update(Party.builder()
                                        .partyFirstName(p.getPartyFirstName())
                                        .partyMiddleInitial(p.getPartyMiddleInitial())
                                        .partyLastName(p.getPartyLastName())
                                        .partyAddress1(p.getPartyAddress1())
                                        .partyAddress2(p.getPartyAddress2())
                                        .partyCity(p.getPartyCity())
                                        .partyState(p.getPartyState())
                                        .partyPostalCode(p.getPartyPostalCode())
                                        .partyCountryCode(p.getPartyCountryCode())
                                        .partyCountry(p.getPartyCountry())
                                        .partyPhone(p.getPartyPhone())
                                        .partyEmail(p.getPartyEmail())
                                        .partyType(p.getPartyType())
                                        .partyActivationDate(p.getPartyActivationDate())
                                        .build());
                            },
                            () -> {
                                Party newParty = Party.builder()
                                        .partyId(p.getPartyId())
                                        .partyFirstName(p.getPartyFirstName())
                                        .partyMiddleInitial(p.getPartyMiddleInitial())
                                        .partyLastName(p.getPartyLastName())
                                        .partyAddress1(p.getPartyAddress1())
                                        .partyAddress2(p.getPartyAddress2())
                                        .partyCity(p.getPartyCity())
                                        .partyState(p.getPartyState())
                                        .partyPostalCode(p.getPartyPostalCode())
                                        .partyCountryCode(p.getPartyCountryCode())
                                        .partyCountry(p.getPartyCountry())
                                        .partyPhone(p.getPartyPhone())
                                        .partyEmail(p.getPartyEmail())
                                        .partyType(p.getPartyType())
                                        .partyActivationDate(p.getPartyActivationDate())
                                        .build();
                                partyDao.migrate(newParty);
                                log.info("new party created %s".formatted(newParty));
                            });
            percentCompleteTabulator.incrementPercentComplete();
        });
    }
}
