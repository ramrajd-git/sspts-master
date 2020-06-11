package dk.skat.ict.sspts.batch.writer;

import dk.skat.ict.sspts.batch.business.*;
import dk.skat.ict.sspts.batch.enums.DRBody;
import dk.skat.ict.sspts.batch.enums.DRFooter;
import dk.skat.ict.sspts.batch.enums.DRHeader;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import dk.skat.ict.sspts.batch.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import java.io.IOException;
import java.io.Writer;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Writer der gernerer den fil der skal overføres til D/R.
 */
public class DRItemWriter implements ItemStreamWriter<Afregning>, FlatFileHeaderCallback, FlatFileFooterCallback {
    private static final int LINE_LENGTH = 96;
    private FlatFileItemWriter<String> delegate;
    private static final Logger log = LoggerFactory.getLogger(DRItemWriter.class);

    private EntityManagerFactory entityManagerFactory;

    private long totalAmount;
    private long totalNumber;
    private int leveranceNummer;

    private SourceSystem system;

    @Autowired
    Konfiguration konfiguration;

    @Autowired
    Clock clock;

    @Autowired
    private DBUtils dbUtils;

    /**
     * Der oprettes en Writer for hvert system.
     * @param system det system filen omhandler
     * @see SourceSystem
     */
    public DRItemWriter(SourceSystem system) {
        this.system = system;
    }


    /**
     * Før batch steppet klargøres fil Writeren
     * @param stepExecution Spring Batch context
     * @see FlatFileItemWriter
     */
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        delegate = new FlatFileItemWriter<String>();
        delegate.setHeaderCallback(this);
        delegate.setFooterCallback(this);
        delegate.setLineAggregator(new PassThroughLineAggregator<>());
        delegate.setName(system.name() + " file writer");
        delegate.setResource(new FileSystemResource(system.getFilename()));
        delegate.setShouldDeleteIfEmpty(true);
        delegate.setAppendAllowed(false);
        delegate.setShouldDeleteIfExists(true);
        leveranceNummer = dbUtils.getNewLeverance(system).getLeveranceNummer();
        totalAmount = 0;
        totalNumber = 0;
    }

    /**
     * Filens body skrives. Hver linje repreæsenterer en {@link Afregning}
     * @param items liste over afregninger der skal skrives til filen
     * @see Afregning
     * @throws Exception
     */
    @Override
    public void write(List<? extends Afregning> items) throws Exception {
        List<String> lines = new ArrayList<>();
        long amountTotal = 0;
        long countTotal = 0;
        for (Afregning afregning : items.get(0).getLeverance().getAfregningList()) {
            FlatFileLine flatFileLine = new FlatFileLine(LINE_LENGTH);
            for (DRBody f : DRBody.values()) {
                if(f.isConstant()) {
                    flatFileLine.addField(f);
                }
            }
            Angivelse angivelse = afregning.getAngivelse();

            flatFileLine.addField(DRBody.TRANS_BLB,afregning.getBeloeb() * 100,true);
            flatFileLine.addField(DRBody.VIRK_NR, angivelse.getSeNummer());
            flatFileLine.addField(DRBody.PER_DTO, angivelse.getPeriodeDato());
            flatFileLine.addField(DRBody.ANTAG_DTO, angivelse.getAntagelseDato());
            flatFileLine.addField(DRBody.KØRSELS_DTO, new Date(clock.millis()));
            flatFileLine.addField(DRBody.TOLD_REF_NR, String.valueOf(angivelse.getReferenceNummer()).substring(2));

            if(afregning.getBeloeb() < 0){
                flatFileLine.addField(DRBody.TRANS_ART, konfiguration.getSletning());
            } else {
                flatFileLine.addField(DRBody.TRANS_ART, konfiguration.getOprettelse());
            }
            flatFileLine.addField(DRBody.KTO_NR, konfiguration.getKontoNummer());
            flatFileLine.addField(DRBody.UKTO_ART, konfiguration.getKontoArt());
            flatFileLine.addField(DRBody.HJEM_DT, konfiguration.getHjemDistrikt());
            flatFileLine.addField(DRBody.TXT_KOD, konfiguration.getTekstKode());
            flatFileLine.addField(DRBody.TOLD_REG_SE_NR, angivelse.getRegistratorSeNummer());
            amountTotal += afregning.getBeloeb() * 100;
            countTotal++;
            lines.add(flatFileLine.toString());
        }

        writeToDB(items.get(0).getLeverance());
        delegate.write(lines);


        // After successfully writing all items
        totalAmount = totalAmount + amountTotal;
        totalNumber = totalNumber + countTotal;
        log.debug("Current amount total: " + totalAmount);
    }

    private void writeToDB(Leverance leverance) {
        EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
        if (entityManager == null) {
            throw new DataAccessResourceFailureException("Unable to obtain a transactional EntityManager");
        }
        entityManager.merge(leverance);
        entityManager.flush();
    }
    /**
     * Filens header skrives.
     * @param writer den Writer der bruges til at skrive filen
     * @throws IOException
     */
    @Override
    public void writeHeader(Writer writer) throws IOException {
        FlatFileLine flatFileLine = new FlatFileLine(LINE_LENGTH);
        for (DRHeader f : DRHeader.values()) {
            if(f.isConstant()) {
                flatFileLine.addField(f);
            }
        }
        flatFileLine.addField(DRHeader.KSL_DTO,new Date(clock.millis()));
        flatFileLine.addField(DRHeader.LEV_NR,leveranceNummer);
        if(system.equals(SourceSystem.MANIFEST)) {
            flatFileLine.addField(DRHeader.SYSTEM, "MANIFEST");
        } else {
            flatFileLine.addField(DRHeader.SYSTEM, "IMPORT  ");
        }
        writer.write(flatFileLine.toString());
    }

    /**
     * Filens footer skrives.
     * @param writer den Writer der bruges til at skrive filen
     * @throws IOException
     */
    @Override
    public void writeFooter(Writer writer) throws IOException {
        FlatFileLine flatFileLine = new FlatFileLine(LINE_LENGTH);
        for (DRFooter f : DRFooter.values()) {
            if(f.isConstant()) {
                flatFileLine.addField(f);
            }
        }
        flatFileLine.addField(DRFooter.INDV_ANT, totalNumber + 2, false);
        flatFileLine.addField(DRFooter.TRANS_BLB_TOT, totalAmount, true);

        writer.write(flatFileLine.toString());
    }

    /**
     * Writeren åbnes.
     * @param executionContext Spring Batch context
     * @throws ItemStreamException
     */
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        delegate.open(executionContext);
    }

    /**
     * Writeren opdateres.
     * @param executionContext Spring Batch context
     * @throws ItemStreamException
     */
    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }

    /**
     * Writeren lukkes.
     * @throws ItemStreamException
     */
    @Override
    public void close() throws ItemStreamException {
        delegate.close();
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
}
