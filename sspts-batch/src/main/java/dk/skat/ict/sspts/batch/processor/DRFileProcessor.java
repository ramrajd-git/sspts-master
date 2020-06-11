package dk.skat.ict.sspts.batch.processor;

import dk.skat.ict.sspts.batch.business.Leverance;
import dk.skat.ict.sspts.batch.enums.DeliveryStatus;
import dk.skat.ict.sspts.batch.exceptions.ICTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.util.Date;

/**
 * Processor til overførsel af leverancer.
 */
public class DRFileProcessor implements ItemProcessor<Leverance, Leverance> {

    private static final Logger log = LoggerFactory.getLogger(DRFileProcessor.class);

    @Autowired
    Clock clock;

    @Autowired
    boolean shouldSend;

    @Resource(name = "toFtpsChannel")
    MessageChannel ftpsMessageChannel;

    /**
     * Overfører leverancen til D/R og opdaterer leverancens status.
     * @param leverance den leverance der skal overføres til D/R
     * @return leverancen med opdateret status
     * @see Leverance
     * @throws ICTException
     */
    @Override
    public Leverance process(final Leverance leverance) throws ICTException {
        if(leverance.getLeveranceDato() == null) {
            File fil = new File(leverance.getLeveranceFilNavn());

            if (fil.exists()) {
                if(shouldSend) {
                    Message<File> message = MessageBuilder.withPayload(fil).build();
                    try {
                        ftpsMessageChannel.send(message,20000);
                        updateLeverance(leverance, fil);
                    } catch (Exception e) {
                        throw new ICTException("Could not send file via FTPS",e);
                    }
                } else {
                    updateLeverance(leverance, fil);
                }
            } else {
                throw new ICTException("No file to send", new FileNotFoundException());
            }
        }

        return leverance;
    }

    private void updateLeverance(Leverance leverance, File file) throws ICTException {
        try {
            leverance.setLeveranceFil(Files.readAllBytes(file.toPath()));
            leverance.setLeveranceStatus(DeliveryStatus.OVERFØRT);
            leverance.setLeveranceDato(new Date(clock.millis()));
        }
        catch (IOException e) {
            throw new ICTException("Error updating Leverance",e);
        }
    }

}
