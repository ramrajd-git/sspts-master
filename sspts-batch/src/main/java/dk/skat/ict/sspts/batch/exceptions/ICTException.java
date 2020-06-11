package dk.skat.ict.sspts.batch.exceptions;

import dk.skat.ict.sspts.batch.configuration.LogConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

/**
 * Created by mns on 21-03-2017.
 */
public class ICTException extends Exception{

    private static final Logger log = LoggerFactory.getLogger(ICTException.class);

    public ICTException(String message) {
        super(message);
        log.error(MarkerFactory.getMarker(LogConfiguration.marker), message);
    }
    public ICTException(String message, Throwable cause) {
        super(message, cause);
        log.error(MarkerFactory.getMarker(LogConfiguration.marker), message);
    }
}
