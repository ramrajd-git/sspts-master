package dk.skat.ict.sspts.batch.configuration;

import dk.skat.ict.sspts.batch.business.Afregning;
import dk.skat.ict.sspts.batch.business.Angivelse;
import dk.skat.ict.sspts.batch.business.Leverance;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import dk.skat.ict.sspts.batch.writer.AfregningDBWriter;
import dk.skat.ict.sspts.batch.writer.DRItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.Arrays;

/**
 * Created by MNS on 15-03-2017.
 */
@Configuration
public class WriterConfiguration {
    @Autowired
    LocalContainerEntityManagerFactoryBean entityManagerFactory;

    /**
    @Bean
    public CompositeItemWriter<Afregning> importCompositeItemWriter(){
        CompositeItemWriter<Afregning> writer = new CompositeItemWriter<>();
        AfregningDBWriter jpaWriter = new AfregningDBWriter();
        jpaWriter.setEntityManagerFactory(entityManagerFactory.getObject());
        writer.setDelegates(Arrays.asList(drImportItemWriter(),jpaWriter));
        return writer;
    }

    @Bean
    public CompositeItemWriter<Afregning> manifestCompositeItemWriter(){
        CompositeItemWriter<Afregning> writer = new CompositeItemWriter<>();
        AfregningDBWriter jpaWriter = new AfregningDBWriter();
        jpaWriter.setEntityManagerFactory(entityManagerFactory.getObject());
        writer.setDelegates(Arrays.asList(drManifestItemWriter(),jpaWriter));
        return writer;
    }**/

    @Bean
    public DRItemWriter drImportItemWriter() {
        DRItemWriter writer = new DRItemWriter(SourceSystem.IMPORT);
        writer.setEntityManagerFactory(entityManagerFactory.getObject());
        return writer;
    }

    @Bean
    public DRItemWriter drManifestItemWriter() {
        DRItemWriter writer = new DRItemWriter(SourceSystem.MANIFEST);
        writer.setEntityManagerFactory(entityManagerFactory.getObject());
        return writer;
    }

    /**
     * Nothing special here a simple JpaItemWriter
     * @return
     */
    @Bean
    public JpaItemWriter<Afregning> afregningItemWriter() {
        JpaItemWriter<Afregning> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory.getObject());

        return writer;
    }
    /**
    @Bean
    public AfregningDBWriter afregningDBWriter() {
        AfregningDBWriter writer = new AfregningDBWriter();
        writer.setEntityManagerFactory(entityManagerFactory.getObject());

        return writer;
    }*/

    /**
     * Nothing special here a simple JpaItemWriter
     * @return
     */
    @Bean
    public JpaItemWriter<Angivelse> angivelseItemWriter() {
        JpaItemWriter<Angivelse> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory.getObject());

        return writer;
    }

    /**
     * Nothing special here a simple JpaItemWriter
     * @return
     */
    @Bean
    public JpaItemWriter<Leverance> leveranceItemWriter() {
        JpaItemWriter<Leverance> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory.getObject());

        return writer;
    }
}
