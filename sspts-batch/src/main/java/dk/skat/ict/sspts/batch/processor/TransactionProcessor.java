package dk.skat.ict.sspts.batch.processor;

import dk.skat.ict.sspts.batch.business.Angivelse;
import dk.skat.ict.sspts.batch.business.Konfiguration;
import dk.skat.ict.sspts.batch.business.Transaction;
import dk.skat.ict.sspts.batch.enums.AfregningType;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import dk.skat.ict.sspts.batch.repository.AfregningRepository;
import dk.skat.ict.sspts.batch.utils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Processor til behandling af inkomne transaktioner fra ETL-værktøj.
 */
public class TransactionProcessor implements ItemProcessor<Transaction, Angivelse> {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessor.class);

    @Autowired
    Konfiguration konfiguration;

    @Autowired
    AfregningRepository afregningRepository;

    @Autowired
    DBUtils dbUtils;

    private boolean newStatements;
    private Map<Long, List<String>> knownImports;
    private Map<Long, List<String>> knownManifests;

    /**
     * @param newStatements angiver om det drejer sig om nye eller eksisterende angivelser.
     */
    public TransactionProcessor(boolean newStatements) {
        this.newStatements = newStatements;
        this.knownImports = new HashMap<>();
        this.knownManifests = new HashMap<>();
    }

    /**
     * Genererer en {@link Angivelse} for hver {@link Transaction} hvor kravene er overholdt.
     * @param transaction
     * @return en ny eller opdateret angivelse for hver transaction
     * @see Angivelse
     * @see Transaction
     * @throws Exception
     */
    @Override
    public Angivelse process(Transaction transaction) throws Exception {
        SourceSystem sourceSystem = SourceSystem.getSystem(transaction.getSystem().trim());
        Map<Long, List<String>> map = getMapBySystem(sourceSystem);
        if(newStatements) {
            if (sourceSystem.equals(SourceSystem.MANIFEST) && dbUtils.angivelseExistsWithSameReference(transaction.getReferenceNummer(), sourceSystem)) {
                log.warn("Existing: Another row with same ref number already exists. Ref: " + transaction.getReferenceNummer(), MarkerFactory.getMarker("BATCH"));
                return null;
            }
            if (map.containsKey(transaction.getReferenceNummer())) {
                if (map.get(transaction.getReferenceNummer()).contains(transaction.getToldprocedure())) {
                    return null;
                }
                else {
                    if (sourceSystem.equals(SourceSystem.MANIFEST)) {
                        log.warn("New: Another row with same ref number already exists. Ref: " + transaction.getReferenceNummer(), MarkerFactory.getMarker("BATCH"));
                        return null;
                    } else {
                        insert(map, transaction.getReferenceNummer(), transaction.getToldprocedure());
                    }
                }
            } else {
                insert(map, transaction.getReferenceNummer(), transaction.getToldprocedure());
            }
            return new Angivelse(String.valueOf(transaction.getSeNummer()),String.valueOf(transaction.getRegistratorSeNummer()),transaction.getReferenceNummer(),transaction.getPeriodeDato(),transaction.getAntagelseDato(),Integer.parseInt(transaction.getStatus().trim()), sourceSystem, transaction.getToldprocedure());
        } else {
            boolean previous = dbUtils.angivelseExists(transaction.getReferenceNummer(), sourceSystem, transaction.getToldprocedure());
            if(previous) {
                Angivelse angivelse = dbUtils.getAnigvelseByReference(transaction.getReferenceNummer(), sourceSystem, transaction.getToldprocedure());
                if(Integer.parseInt(transaction.getStatus().trim()) > 89 && afregningRepository.findByAngivelseAndType(angivelse, AfregningType.POSTERING).size() == 1 && afregningRepository.findByAngivelseAndType(angivelse, AfregningType.MODPOSTERING).size() == 0) {
                    angivelse.setStatus(Integer.parseInt(transaction.getStatus().trim()));
                    return angivelse;
                }
            } else {
                if (sourceSystem.equals(SourceSystem.IMPORT)) {
                    insert(map, transaction.getReferenceNummer(), transaction.getToldprocedure());
                    return new Angivelse(String.valueOf(transaction.getSeNummer()),String.valueOf(transaction.getRegistratorSeNummer()),transaction.getReferenceNummer(),transaction.getPeriodeDato(),transaction.getAntagelseDato(),Integer.parseInt(transaction.getStatus().trim()), sourceSystem, transaction.getToldprocedure());
                }
            }
        }
        return null;
    }

    private Map<Long, List<String>> getMapBySystem(SourceSystem sourceSystem) {
        if (sourceSystem.equals(SourceSystem.IMPORT)) {
            return knownImports;
        } else if (sourceSystem.equals(SourceSystem.MANIFEST)) {
            return knownManifests;
        }
        return null;
    }

    private void insert(Map<Long,List<String>> map, Long ref, String tp) {
        if (map.containsKey(ref)) {
            map.get(ref).add(tp);
        } else {
            map.put(ref, new LinkedList<>(Collections.singleton(tp)));
        }
    }
}
