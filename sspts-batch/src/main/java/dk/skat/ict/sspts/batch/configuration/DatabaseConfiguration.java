package dk.skat.ict.sspts.batch.configuration;

import dk.skat.ict.sspts.batch.logging.JobCompletionNotificationListener;
import oracle.jdbc.pool.OracleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by MNS on 15-03-2017.
 */
@Configuration
public class DatabaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Autowired
    private Marker dbMarker;

    @Value("${db.embedded}")
    private boolean EMBEDDED;

    @Value("${db.host}")
    private String HOST;

    @Value("${db.port}")
    private String PORT;

    @Value("${db.service}")
    private String SERVICE;

    @Value("${db.user}")
    private String USER;

    @Value("${db.password}")
    private String PASSWORD;

    @Value("${db.ddl}")
    private String DDL;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setPackagesToScan("dk.skat.ict.sspts.batch");
        lef.setDataSource(dataSource());
        lef.setJpaVendorAdapter(jpaVendorAdapter());
        Properties properties = new Properties();
        properties.put("hibernate.hbm2ddl.auto",DDL);
        properties.put("hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl");
        properties.put("hibernate.generate_statistics", false);

        lef.setJpaProperties(properties);
        return lef;
    }


    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        if(EMBEDDED) {
            jpaVendorAdapter.setDatabase(Database.H2);
            jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.H2Dialect");
        }
        else {
            jpaVendorAdapter.setDatabase(Database.ORACLE);
            jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.Oracle12cDialect");
        }
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(false);

        return jpaVendorAdapter;
    }

    @Bean
    public DataSource dataSource() {
        if (EMBEDDED) {
            // no need shutdown, EmbeddedDatabaseFactoryBean will take care of this
            EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
            EmbeddedDatabase db = builder
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
            return db;
        } else {
            try {
                OracleDataSource dataSource = new OracleDataSource();
                dataSource.setUser(USER);
                dataSource.setPassword(PASSWORD);
                dataSource.setURL("jdbc:oracle:thin:" + HOST + ":" + PORT + "/" + SERVICE);
                dataSource.setImplicitCachingEnabled(true);
                dataSource.setFastConnectionFailoverEnabled(true);
                log.info(dbMarker, "Using connection string: " + dataSource.getURL());
                return dataSource;
            }
            catch (SQLException e) {
                throw new RuntimeException("Unable to setup datasource",e);
            }
        }
    }

    @Bean
    JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource());
        return jdbcTemplate;
    }
}
