package dk.skat.ict.sspts.batch.utils;

import dk.skat.ict.sspts.batch.business.Afregning;
import dk.skat.ict.sspts.batch.business.Angivelse;
import dk.skat.ict.sspts.batch.business.Leverance;
import dk.skat.ict.sspts.batch.enums.AfregningType;
import dk.skat.ict.sspts.batch.enums.DeliveryStatus;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import dk.skat.ict.sspts.batch.repository.AfregningRepository;
import dk.skat.ict.sspts.batch.repository.AngivelseRepository;
import dk.skat.ict.sspts.batch.repository.LeveranceRepository;
import dk.skat.ict.sspts.batch.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Værktøj til diverse database operationer.
 */
@Component
public class DBUtils {

    @Autowired
    private AfregningRepository afregningRepository;
    @Autowired
    private AngivelseRepository angivelseRepository;
    @Autowired
    private LeveranceRepository leveranceRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

    /**
     * Hent leverancenummer for ny {@link Leverance}.
     * @param sourceSystem systemet som leverancen skal genereres for
     * @return nyt leverancenummer
     * @see Leverance
     */
    public int getNewLeveranceNummer(SourceSystem sourceSystem) {
        Leverance top = leveranceRepository.findTopBySystemOrderByLeveranceNummerDesc(sourceSystem);
        if(top == null) {
            return 1;
        } else {
            return top.getLeveranceNummer() + 1;
        }
    }

    //TODO: Brug denne så der altid kun sendes en leverance afsted.
    public Leverance getNewLeverance(SourceSystem sourceSystem) {
        Leverance top = leveranceRepository.findTopBySystemOrderByLeveranceNummerDesc(sourceSystem);
        if(top == null) {
            return new Leverance(1,sourceSystem);
        }
        if(top.getLeveranceStatus() != DeliveryStatus.OVERFØRT) {
            return top;
        }
        return new Leverance(top.getLeveranceNummer() + 1,sourceSystem);
    }

    /**
     * Hent {@link Angivelse} udfra referencenummer
     * @param reference referencenummer på den angivelse der ønskes hentet
     * @return angivelsen der matcher referencenummeret
     * @see Angivelse
     */
    public Angivelse getAnigvelseByReference(Long reference, SourceSystem sourceSystem, String procedure) {
        return angivelseRepository.findByReferenceNummerAndSystemAndProcedure(reference, sourceSystem, procedure);
    }


    public List<Afregning> getAfregningByAngivelse(Angivelse angivelse) {
        return afregningRepository.findByAngivelse(angivelse);
    }

    public boolean hasPostering(Angivelse angivelse) {
        return afregningRepository.findByAngivelseAndType(angivelse, AfregningType.POSTERING).size() > 0;
    }

    public boolean hasModpostering(Angivelse angivelse) {
        return afregningRepository.findByAngivelseAndType(angivelse, AfregningType.MODPOSTERING).size() > 0;
    }

    public Afregning getPostering(Angivelse angivelse) {
        List<Afregning> afregningList = afregningRepository.findByAngivelseAndType(angivelse, AfregningType.POSTERING);
        if(afregningList.size() > 0) {
            return afregningList.get(0);
        }
        return null;
    }

    public Afregning getModpostering(Angivelse angivelse) {
        List<Afregning> afregningList = afregningRepository.findByAngivelseAndType(angivelse, AfregningType.MODPOSTERING);
        if(afregningList.size() > 0) {
            return afregningList.get(0);
        }
        return null;
    }

    /**
     * Tjekker om en {@link Angivelse} eksisterer udfra et referencenummer
     * @param reference referencenummer angivelsen
     * @return svar på om angivelsen eksisterer
     * @see Angivelse
     */
    public boolean angivelseExists(Long reference, SourceSystem sourceSystem, String procedure) {
        return angivelseRepository.countByReferenceNummerAndSystemAndProcedure(reference, sourceSystem, procedure) > 0;
    }

    public void saveLeverance(Leverance leverance) {
        leveranceRepository.save(leverance);
    }

    public void saveAfregninger(List<Afregning> afregninger) {
        afregningRepository.save(afregninger);
    }

    public boolean angivelseExistsWithSameReference(Long reference, SourceSystem sourceSystem) {
        return angivelseRepository.countByReferenceNummerAndSystem(reference, sourceSystem) > 0;
    }
}
