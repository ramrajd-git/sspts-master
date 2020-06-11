package functional;

import org.springframework.batch.core.Job;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

/**
 * Created by mns on 08-03-2017.
 */
@Configuration
public class BatchTestConfiguration {
    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() {
        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("paymentsJob") Job job) {
                super.setJob(job);
            }
        };
    }
}
