package net.lego.database.deployer.postprocessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lego.data.v2.dao.PartyDao;
import net.lego.data.v2.dto.Party;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyMigrator implements PostProcessor {

    private final PartyDao partyDao;
    private final net.lego.data.v1.dao.PartyDao partyDaoV1;

    @Override
    public void execute() {
        log.info("PartyMigrator");
        partyDaoV1.findAll()
                  .forEach(p -> {
                      log.info("Party [{}]", p);
                      partyDao.findPartyById(p.getPartyId())
                              .ifPresentOrElse(party -> {
                                          log.info("Updating existing Party [{}] to [{}]", party, p);
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
                                          partyDao.insert(newParty);
                                          partyDao.decrementPartyId(newParty.getPartyId());
                                      });
                  });
    }
}
