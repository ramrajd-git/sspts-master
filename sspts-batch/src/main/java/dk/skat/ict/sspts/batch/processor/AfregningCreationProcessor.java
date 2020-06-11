package dk.skat.ict.sspts.batch.processor;

import dk.skat.ict.sspts.batch.business.Afregning;
import dk.skat.ict.sspts.batch.business.Angivelse;

import dk.skat.ict.sspts.batch.business.Konfiguration;
import dk.skat.ict.sspts.batch.enums.AfregningType;
import dk.skat.ict.sspts.batch.repository.AfregningRepository;
import dk.skat.ict.sspts.batch.repository.AngivelseRepository;
import dk.skat.ict.sspts.batch.utils.DBUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Processor der opretter betalinger.
 */
public class AfregningCreationProcessor implements ItemProcessor<Angivelse, Afregning> {

    @Autowired
    Konfiguration konfiguration;


    @Autowired
    DBUtils dbUtils;

    /**
     * Genererer en {@link Afregning} baseret på en {@link Angivelse}. Hvis status er mindre en 90 og der ikke eksisterer en postering genereres denne, hvis status er over 89 og der eksisterer en postering, men ikke en modpostering genereres denne.
     * @param angivelse angivelsen som skal behandles
     * @return den nye postering eller modpostering tilknyttet
     * @see Afregning
     * @see Angivelse
     * @throws Exception
     */
    @Override
    public Afregning process(final Angivelse angivelse) throws Exception {
        if(angivelse.getStatus() < 90 && !angivelse.hasPostering()){
            Afregning afregning = new Afregning(angivelse, konfiguration.getKontoNummer(), konfiguration.getBeloeb(), AfregningType.POSTERING);
            return afregning;
        } else if (angivelse.getStatus() > 89 && angivelse.hasPostering() && !angivelse.hasModpostering()){
            Afregning afregning = new Afregning(angivelse, angivelse.getPostering().getKontoNummer(), angivelse.getPostering().getBeloeb() * -1, AfregningType.MODPOSTERING);
            return afregning;
        }
        return null;
    }
}
