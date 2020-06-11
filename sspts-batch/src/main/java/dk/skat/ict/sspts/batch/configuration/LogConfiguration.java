package dk.skat.ict.sspts.batch.configuration;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.outbound.FtpMessageHandler;
import org.springframework.integration.ftp.session.DefaultFtpsSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Created by mns on 10-03-2017.
 */
@Configuration
public class LogConfiguration {
    public static final String marker = "BATCH";
    public static final String dbMarker = "DATABASE";

    @Bean
    public Marker batchMarker() {
        return MarkerFactory.getMarker(marker);
    }

    @Bean
    public Marker dbMarker() {
        return MarkerFactory.getMarker(dbMarker);
    }
}
