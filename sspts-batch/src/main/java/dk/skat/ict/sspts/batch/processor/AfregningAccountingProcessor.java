package dk.skat.ict.sspts.batch.processor;

import dk.skat.ict.sspts.batch.business.Afregning;
import dk.skat.ict.sspts.batch.business.Leverance;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import dk.skat.ict.sspts.batch.utils.DBUtils;
import dk.skat.ict.sspts.batch.writer.DRItemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Processor der opretter en leverance.
 */
public class AfregningAccountingProcessor implements ItemProcessor<Afregning, Afregning> {

    private Leverance leverance;
    private SourceSystem system;

    @Autowired DBUtils dbUtils;

    private static final Logger log = LoggerFactory.getLogger(DRItemWriter.class);

    /**
     * Der oprettes en processor for hvert system.
     * @param sourceSystem systemet hvor angivelsen kommer fra
     * @see SourceSystem
     */
    public AfregningAccountingProcessor(SourceSystem sourceSystem) {
        system = sourceSystem;
    }

    /**
     * Før batch steppet oprettes leverancen.
     * @param stepExecution Spring Batch context
     */
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        leverance = dbUtils.getNewLeverance(system);
    }

    /**
     * Tilknytter en {@link Leverance} til en {@link Afregning}.
     * @param afregning den afregning der skal tilknyttes leverancen
     * @return afregningen med den tilknyttede leverance
     * @see Leverance
     * @see Afregning
     * @throws Exception
     */
    @Override
    public Afregning process(Afregning afregning) throws Exception {
        if(afregning.getLeverance() != null) {
            return null;
        }
        afregning.setLeverance(leverance);

        return afregning;
    }
}
