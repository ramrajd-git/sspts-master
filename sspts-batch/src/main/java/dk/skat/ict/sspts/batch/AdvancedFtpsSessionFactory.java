package dk.skat.ict.sspts.batch;

import dk.skat.ict.sspts.batch.processor.DRFileProcessor;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.integration.file.splitter.FileSplitter;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpsSessionFactory;

import java.io.IOException;

/**
 * Created by mns on 11-05-2017.
 */
public class AdvancedFtpsSessionFactory extends DefaultFtpsSessionFactory {

    private static final Logger log = LoggerFactory.getLogger(DRFileProcessor.class);

    protected void postProcessClientBeforeConnect(FTPSClient ftpsClient) throws IOException {
        super.postProcessClientBeforeConnect(ftpsClient);
        log.info("Configuring FTP", MarkerFactory.getMarker("BATCH"));
        ftpsClient.configure(new FTPClientConfig(FTPClientConfig.SYST_UNIX));
    }

    protected void postProcessClientAfterConnect(FTPSClient ftpsClient) throws IOException {
        super.postProcessClientAfterConnect(ftpsClient);
        log.info("FTPS Connected", MarkerFactory.getMarker("BATCH"));
        ftpsClient.enterLocalPassiveMode();
        ftpsClient.setRemoteVerificationEnabled(false);
        ftpsClient.setUseEPSVwithIPv4(true);
        log.info("Listing files.", MarkerFactory.getMarker("BATCH"));
        try {
            for (FTPFile f : ftpsClient.listFiles()) {
                log.info(f.getName(), MarkerFactory.getMarker("BATCH"));
            }
            log.info("Done listing files.", MarkerFactory.getMarker("BATCH"));
        }
        catch (IOException e) {
            log.error(MarkerFactory.getMarker("BATCH"), "Error when trying to list files: ",e);
        }
        for(String r : ftpsClient.getReplyStrings()) {
            log.info("Reply: " + r, MarkerFactory.getMarker("BATCH"));
        }
    }
}