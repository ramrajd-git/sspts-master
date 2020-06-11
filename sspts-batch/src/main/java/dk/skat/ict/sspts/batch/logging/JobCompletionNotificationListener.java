package dk.skat.ict.sspts.batch.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.Marker;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by mns on 02-03-2017.
 */
@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Autowired
    private Marker batchMarker;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info(batchMarker, "The batch job completed successfully.");
        }
    }
}
