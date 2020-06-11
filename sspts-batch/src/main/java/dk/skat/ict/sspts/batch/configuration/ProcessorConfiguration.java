package dk.skat.ict.sspts.batch.configuration;

import dk.skat.ict.sspts.batch.enums.SourceSystem;
import dk.skat.ict.sspts.batch.processor.AfregningAccountingProcessor;
import dk.skat.ict.sspts.batch.processor.AfregningCreationProcessor;
import dk.skat.ict.sspts.batch.processor.DRFileProcessor;
import dk.skat.ict.sspts.batch.processor.TransactionProcessor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by MNS on 15-03-2017.
 */
@Configuration
public class ProcessorConfiguration {
    @Bean
    public DRFileProcessor fileProcessor() {
        return new DRFileProcessor();
    }

    @Bean
    public AfregningCreationProcessor afregningProcessor() {
        return new AfregningCreationProcessor();
    }

    @Bean
    @StepScope
    public AfregningAccountingProcessor afregningImportAccountingProcessor() {
        return new AfregningAccountingProcessor(SourceSystem.IMPORT);
    }

    @Bean
    @StepScope
    public AfregningAccountingProcessor afregningManifestAccountingProcessor() {
        return new AfregningAccountingProcessor(SourceSystem.MANIFEST);
    }

    @Bean
    @StepScope
    public TransactionProcessor transactionNewProcessor() {
        return new TransactionProcessor(true);
    }

    @Bean
    @StepScope
    public TransactionProcessor transactionExistingProcessor() {
        return new TransactionProcessor(false);
    }
}
