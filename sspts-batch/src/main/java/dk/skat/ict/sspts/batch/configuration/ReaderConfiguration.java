package dk.skat.ict.sspts.batch.configuration;

import dk.skat.ict.sspts.batch.business.*;
import dk.skat.ict.sspts.batch.enums.DeliveryStatus;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import dk.skat.ict.sspts.batch.repository.AngivelseRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by MNS on 15-03-2017.
 */
@Configuration
public class ReaderConfiguration {
    @Autowired
    LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @Autowired
    DataSource dataSource;

    @Autowired
    AngivelseRepository angivelseRepository;

    /**
     * Nothing special here a simple JpaItemWriter
     * @return
     */
    @Bean(destroyMethod="")
    public ItemReader<Angivelse> angivelseItemReader() {
        JpaPagingItemReader<Angivelse> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory.getNativeEntityManagerFactory());
        reader.setPageSize(1000000);
        reader.setQueryString("SELECT a FROM Angivelse a WHERE (status < 90 AND NOT EXISTS(SELECT 1 FROM Afregning afr WHERE afr.angivelse = a AND afr.type = 'POSTERING')) OR (status > 89 AND EXISTS(SELECT 1 FROM Afregning afr WHERE afr.angivelse = a AND afr.type = 'POSTERING') AND NOT EXISTS(SELECT 1 FROM Afregning afr WHERE afr.angivelse = a AND afr.type = 'MODPOSTERING')) ORDER BY id");
        reader.setTransacted(false);


        return reader;
    }


    /**
     * Nothing special here a simple JpaItemWriter
     * @return
     */
    @Bean(destroyMethod="")
    public ItemReader<Transaction> transactionNewItemReader() {
        JpaPagingItemReader<Transaction> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory.getNativeEntityManagerFactory());
        reader.setQueryString("SELECT t FROM Transaction t WHERE t.status < 90 AND NOT EXISTS(SELECT 1 FROM Angivelse a WHERE t.referenceNummer = a.referenceNummer AND TRIM(t.system) = a.system AND t.toldprocedure = a.procedure)");
        reader.setPageSize(1000000);
        reader.setTransacted(false);

        return reader;
    }

    /**
     * Nothing special here a simple JpaItemWriter
     * @return
     */
    @Bean(destroyMethod="")
    public JpaPagingItemReader<Transaction> transactionExistingItemReader() {
        JpaPagingItemReader<Transaction> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory.getNativeEntityManagerFactory());
        reader.setQueryString("SELECT t FROM Transaction t WHERE t.status > 89 AND EXISTS(SELECT 1 FROM Angivelse a WHERE t.referenceNummer = a.referenceNummer AND TRIM(t.system) = a.system AND t.toldprocedure = a.procedure AND a.status < 90)");
        reader.setPageSize(1000000);
        reader.setTransacted(false);

        return reader;
    }

    /**
     * Nothing special here a simple JpaItemWriter
     * @return
     */
    @Bean(destroyMethod="")
    public JpaPagingItemReader<Leverance> leveranceItemReader() {
        JpaPagingItemReader<Leverance> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory.getNativeEntityManagerFactory());
        reader.setQueryString("SELECT l FROM Leverance l WHERE l.leveranceStatus = :status)");
        reader.setTransacted(false);
        Map<String, Object> params = new HashMap<>();
        params.put("status", DeliveryStatus.OPRETTET);
        reader.setParameterValues(params);

        return reader;
    }

    /**
     * Nothing special here a simple JpaItemWriter
     * @return
     */
    @Bean(destroyMethod="")
    public JpaPagingItemReader<Afregning> afregningImportItemReader() {
        JpaPagingItemReader<Afregning> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory.getObject());
        reader.setQueryString("SELECT afr FROM Afregning afr JOIN afr.angivelse ang WHERE ang.system = :system AND afr.leverance IS NULL");
        reader.setPageSize(1000000);
        reader.setTransacted(false);
        Map<String, Object> params = new HashMap<>();
        params.put("system", SourceSystem.IMPORT);
        reader.setParameterValues(params);

        return reader;
    }
    /**
     * Nothing special here a simple JpaItemWriter
     * @return
     */
    @Bean(destroyMethod="")
    public JpaPagingItemReader<Afregning> afregningManifestItemReader() {
        JpaPagingItemReader<Afregning> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory.getObject());
        reader.setQueryString("SELECT afr FROM Afregning afr JOIN afr.angivelse ang WHERE ang.system = :system AND afr.leverance IS NULL");
        reader.setPageSize(1000000);
        reader.setTransacted(false);
        Map<String, Object> params = new HashMap<>();
        params.put("system", SourceSystem.MANIFEST);
        reader.setParameterValues(params);

        return reader;
    }
}
