package dk.skat.ict.sspts.batch.configuration;

import dk.skat.ict.sspts.batch.AdvancedFtpsSessionFactory;
import org.apache.commons.net.ftp.FTPFile;
import org.hibernate.criterion.SimpleExpression;
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
import org.springframework.integration.ftp.gateway.FtpOutboundGateway;
import org.springframework.integration.ftp.outbound.FtpMessageHandler;
import org.springframework.integration.ftp.session.DefaultFtpsSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Created by mns on 10-03-2017.
 */
@Configuration
public class FtpConfiguration {

    private static String HOST;

    private static int PORT;

    private static String USER;

    private static String PATH;

    private static String PASSWORD;

    private static String KS_PATH;

    private static String KS_PASSWORD;

    private static String KS_KEY_PASSWORD;

    private static boolean SEND_TO_DR;

    @Bean
    public static SessionFactory<FTPFile> ftpsSessionFactory() {
        AdvancedFtpsSessionFactory sf = new AdvancedFtpsSessionFactory();
        sf.setHost(HOST);
        sf.setPort(PORT);
        sf.setUsername(USER);
        sf.setPassword(PASSWORD);
        sf.setClientMode(2);
        sf.setUseClientMode(true);
        sf.setConnectTimeout(20000);
        sf.setDefaultTimeout(20000);
        sf.setDataTimeout(20000);
        sf.setImplicit(false);
        sf.setWantsClientAuth(false);
        sf.setNeedClientAuth(false);

        Pair<KeyManagerFactory,TrustManagerFactory> trust = getTrust();
        sf.setKeyManager(trust.getFirst().getKeyManagers()[0]);
        sf.setTrustManager(trust.getSecond().getTrustManagers()[0]);

        return new CachingSessionFactory<FTPFile>(sf);
    }

    @Bean
    @ServiceActivator(inputChannel = "toFtpsChannel")
    public static MessageHandler handler() {
        FtpMessageHandler handler = new FtpMessageHandler(ftpsSessionFactory());
        handler.setLoggingEnabled(true);
        handler.setShouldTrack(true);
        handler.setRemoteDirectoryExpression(new LiteralExpression(PATH));
        handler.setFileNameGenerator(new FileNameGenerator() {

            @Override
            public String generateFileName(Message<?> message) {
                return ((File) message.getPayload()).getName();
            }

        });

        handler.afterPropertiesSet();


        return handler;
    }

    @MessagingGateway
    public interface FtpsGateway {

        @Gateway(requestChannel = "toFtpsChannel")
        void sendToFtps(File file);

    }

    @Bean(name = "toFtpsChannel")
    public MessageChannel ftpsMessageChannel() {
        return new DirectChannel();
    }

    private static Pair<KeyManagerFactory, TrustManagerFactory> getTrust() {
        final char[] JKS_PASSWORD = KS_PASSWORD.toCharArray();
        final char[] KEY_PASSWORD = KS_KEY_PASSWORD.toCharArray();
        try {
		/* Get the JKS contents */
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            try (final InputStream is = new FileInputStream(KS_PATH)) {
                keyStore.load(is, JKS_PASSWORD);
            }
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                    .getDefaultAlgorithm());
            kmf.init(keyStore, KEY_PASSWORD);
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());
            tmf.init(keyStore);

            return Pair.of(kmf,tmf);

        } catch (final GeneralSecurityException | IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Bean
    public boolean shouldSend() {
        return SEND_TO_DR;
    }

    @Value("${ftp.host}")
    public void setHOST(String host) {
        HOST = host;
    }

    @Value("${ftp.port}")
    public void setPORT(int port) {
        PORT = port;
    }

    @Value("${ftp.path}")
    public void setPATH(String path) {
        PATH = path;
    }

    @Value("${ftp.user}")
    public void setUSER(String user) {
        USER = user;
    }

    @Value("${ftp.password}")
    public void setPASSWORD(String password) {
        PASSWORD = password;
    }

    @Value("${ftp.keystore.path}")
    public void setKsPath(String ksPath) {
        KS_PATH = ksPath;
    }

    @Value("${ftp.keystore.password}")
    public void setKsPassword(String ksPassword){
        KS_PASSWORD = ksPassword;
    }

    @Value("${ftp.keystore.key.password}")
    public void setKsKeyPassword(String ksKeyPassword) {
        KS_KEY_PASSWORD = ksKeyPassword;
    }

    @Value("${ftp.send}")
    public void setSendToDr(boolean sendToDr) {
        SEND_TO_DR = sendToDr;
    }
}
