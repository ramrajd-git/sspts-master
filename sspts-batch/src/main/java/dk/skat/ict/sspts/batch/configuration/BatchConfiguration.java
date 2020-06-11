package dk.skat.ict.sspts.batch.configuration;

import dk.skat.ict.sspts.batch.business.*;
import dk.skat.ict.sspts.batch.logging.JobCompletionNotificationListener;
import dk.skat.ict.sspts.batch.processor.AfregningAccountingProcessor;
import dk.skat.ict.sspts.batch.processor.AfregningCreationProcessor;
import dk.skat.ict.sspts.batch.processor.DRFileProcessor;
import dk.skat.ict.sspts.batch.processor.TransactionProcessor;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import dk.skat.ict.sspts.batch.writer.AfregningDBWriter;
import dk.skat.ict.sspts.batch.writer.DRItemWriter;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.quartz.SimpleThreadPoolTaskExecutor;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.time.Clock;
import java.util.*;

/**
 * Created by mns on 02-03-2017.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    public static final String JOB_NAME = "paymentsJob";
    public static final String STEP_GENERATE_STATEMENTS = "generateStatements";
    public static final String STEP_UPDATE_STATEMENTS = "updateStatements";
    public static final String STEP_GENERATE_PAYMENTS = "generatePayments";
    public static final String STEP_GENERATE_IMPORTFILE = "generateImportFile";
    public static final String STEP_GENERATE_MANIFESTFILE = "generateManifestFile";
    public static final String STEP_TRANSFER_FILE = "transferFile";
    public static final String FLOW_STATEMENTS = "statementSplitFlow";

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public JobCompletionNotificationListener jobCompletionNotificationListener;

    @Autowired
    LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @Autowired
    ItemReader<Angivelse> angivelseItemReader;

    @Autowired
    ItemReader<Transaction> transactionExistingItemReader;

    @Autowired
    ItemReader<Transaction> transactionNewItemReader;

    @Autowired
    ItemReader<Leverance> leveranceItemReader;

    @Autowired
    ItemReader<Afregning> afregningImportItemReader;

    @Autowired
    ItemReader<Afregning> afregningManifestItemReader;

    //Processors
    @Autowired
    DRFileProcessor fileProcessor;

    @Autowired
    AfregningCreationProcessor afregningProcessor;

    @Autowired
    AfregningAccountingProcessor afregningImportAccountingProcessor;

    @Autowired
    AfregningAccountingProcessor afregningManifestAccountingProcessor;

    @Autowired
    TransactionProcessor transactionNewProcessor;

    @Autowired
    TransactionProcessor transactionExistingProcessor;

    //Writers
    /**
    @Autowired
    CompositeItemWriter<Afregning> importCompositeItemWriter;

    @Autowired
    CompositeItemWriter<Afregning> manifestCompositeItemWriter;**/

    @Autowired
    DRItemWriter drImportItemWriter;

    @Autowired
    DRItemWriter drManifestItemWriter;

    @Autowired
    JpaItemWriter<Afregning> afregningItemWriter;

    @Autowired
    JpaItemWriter<Angivelse> angivelseItemWriter;

    @Autowired
    JpaItemWriter<Leverance> leveranceItemWriter;

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()             {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public Job paymentsJob(JobCompletionNotificationListener listener) {
        //insertTestData(); //TODO: Remove
        return jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .preventRestart()
                //.start(statementSplitFlow())
                .flow(generateNewStatements())
                .next(updateExistingStatements())
                .next(generatePayments())
                //.next(fileSplitFlow())
                .next(generateImportFile())
                .next(generateManifestFile())
                .next(transferFile())
                .end()
                .build();
    }

    @Bean
    public Flow fileSplitFlow() {
        return new FlowBuilder<Flow>("File Split Flow")
                .split(new SimpleAsyncTaskExecutor())
                .add(new FlowBuilder<Flow>("importSubflow").from(generateImportFile()).build(), new FlowBuilder<Flow>("manifestSubflow").from(generateManifestFile()).build())
                .build();
    }

    @Bean
    public Flow statementSplitFlow() {
        return new FlowBuilder<Flow>("Statement Split Flow")
                .split(new SimpleAsyncTaskExecutor())
                .add(new FlowBuilder<Flow>("existingSubflow").from(updateExistingStatements()).build(), new FlowBuilder<Flow>("newSubflow").from(generateNewStatements()).build())
                .build();
    }

    @Bean
    public Step generateNewStatements() {
        return stepBuilderFactory.get(STEP_GENERATE_STATEMENTS)
                .<Transaction, Angivelse> chunk(5000)
                .reader(transactionNewItemReader)
                .processor(transactionNewProcessor)
                .writer(angivelseItemWriter)
                .build();
    }

    @Bean
    public Step updateExistingStatements() {
        return stepBuilderFactory.get(STEP_UPDATE_STATEMENTS)
                .<Transaction, Angivelse> chunk(5000)
                .reader(transactionExistingItemReader)
                .processor(transactionExistingProcessor)
                .writer(angivelseItemWriter)
                .build();
    }

    @Bean
    public Step generatePayments() {
        return stepBuilderFactory.get(STEP_GENERATE_PAYMENTS)
                .<Angivelse, Afregning> chunk(5000)
                .reader(angivelseItemReader)
                .processor(afregningProcessor)
                .writer(afregningItemWriter)
                .build();
    }

    @Bean
    public Step generateImportFile() {
        return stepBuilderFactory.get(STEP_GENERATE_IMPORTFILE)
                .listener(drImportItemWriter)
                .<Afregning, Afregning> chunk(500000)
                .reader(afregningImportItemReader)
                .processor(afregningImportAccountingProcessor)
                .writer(drImportItemWriter)
                .build();
    }

    @Bean
    public Step generateManifestFile() {
        return stepBuilderFactory.get(STEP_GENERATE_MANIFESTFILE)
                .listener(drManifestItemWriter)
                .<Afregning, Afregning> chunk(500000)
                .reader(afregningManifestItemReader)
                .processor(afregningManifestAccountingProcessor)
                .writer(drManifestItemWriter)
                .build();
    }

    @Bean
    public Step transferFile() {
        return stepBuilderFactory.get(STEP_TRANSFER_FILE)
                .<Leverance, Leverance> chunk(1)
                .reader(leveranceItemReader)
                .processor(fileProcessor)
                .writer(leveranceItemWriter)
                .build();
    }

    @Bean
    public Konfiguration konfiguration() {
        EntityManager entityManager = entityManagerFactory.getObject().createEntityManager();
        Query q = entityManager.createQuery("SELECT k FROM Konfiguration k");
        List<Konfiguration> list = q.getResultList();
        entityManager.close();
        Konfiguration konfiguration = new Konfiguration(true);
        if(!list.isEmpty()) {
            Collections.reverse(list);
            for(Konfiguration k : list) {
                if(k.getStartDato().getTime() <= clock().millis()) {
                    konfiguration = k;
                    break;
                }
            }
        }

        return konfiguration;
    }
}
