package functional;

import dk.skat.ict.sspts.batch.Application;
import dk.skat.ict.sspts.batch.business.Angivelse;
import dk.skat.ict.sspts.batch.business.Konfiguration;
import dk.skat.ict.sspts.batch.business.Transaction;
import dk.skat.ict.sspts.batch.configuration.BatchConfiguration;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import dk.skat.ict.sspts.batch.repository.*;
import dk.skat.ict.sspts.batch.utils.DBUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.batch.JobLauncherCommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;
import utils.TestDataUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.Clock;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by mns on 07-03-2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BatchConfiguration.class, Application.class, BatchTestConfiguration.class})
public class EndToEndTests {
    private static final String EXPECTED_FILE_IMPORT = "classpath:data/output/import.txt";
    private static final String EXPECTED_FILE_IMPORT_RE = "classpath:data/output/importre.txt";
    private static final String EXPECTED_FILE_MANIFEST = "classpath:data/output//manifest.txt";
    private static final String OUTPUT_MANIFEST = "DABETT.FTP.TOLD.AD090";
    private static final String OUTPUT_IMPORT = "DABETT.FTP.TOLD.AD091";
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @MockBean
    private Clock clock;

    @MockBean
    private Konfiguration konfiguration;

    private TestDataUtil testDataUtil;

    @Autowired
    private DBUtils dbUtils;

    @Autowired
    private AfregningRepository afregningRepository;
    @Autowired
    private AngivelseRepository angivelseRepository;
    @Autowired
    private LeveranceRepository leveranceRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    KonfigurationRepository konfigurationRepository;

    @Autowired
    JobLauncherCommandLineRunner jobLauncherCommandLineRunner;

    @Before
    public void setup() {
        testDataUtil = new TestDataUtil(entityManagerFactory.getObject());
        Konfiguration config = new Konfiguration("1450","505",10,"19","020","22200","22500",testDataUtil.dateFromString("31-01-2017"));
        konfigurationRepository.save(config);
        setClock(new Date());
        setKonfiguration();
    }

    @After
    public void teardown() {
        try {
            afregningRepository.deleteAll();
        } catch (InvalidDataAccessApiUsageException e) {
            System.out.println(e.getMessage());
        }
        try {
            leveranceRepository.deleteAll();
        } catch (InvalidDataAccessApiUsageException e) {
            System.out.println(e.getMessage());

        }
        try {
            angivelseRepository.deleteAll();
        } catch (InvalidDataAccessApiUsageException e) {
            System.out.println(e.getMessage());

        }
        try {
            transactionRepository.deleteAll();
        } catch (InvalidDataAccessApiUsageException e) {
            System.out.println(e.getMessage());

        }
        try {
            konfigurationRepository.deleteAll();
        } catch (InvalidDataAccessApiUsageException e) {
            System.out.println(e.getMessage());

        }
    }

    @Test
    public void testJob() throws Exception {
        setClock(new Date());
        testDataUtil.insertRandomTransaction(100,0,null,true);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

    @Test
    public void testNoDuplciates() throws Exception {
        setClock(new Date());
        testDataUtil.insertRandomTransaction(1,0,null,false);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        Assert.assertEquals(1,angivelseRepository.count());
        Assert.assertEquals(1, afregningRepository.count());
        jobExecution = jobLauncherTestUtils.launchJob();
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        Assert.assertEquals(1,angivelseRepository.count());
        Assert.assertEquals(1, afregningRepository.count());
    }

    @Test
    public void testRestart() throws Exception {
        setClock(new Date());
        Transaction postering = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "M");
        Transaction postering1 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "DIFFERENT", "M");
        Transaction postering2 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "I");
        transactionRepository.save(postering);
        transactionRepository.save(postering1);
        transactionRepository.save(postering2);
        jobLauncherCommandLineRunner.run();
        Assert.assertEquals(2,angivelseRepository.count());
        Assert.assertEquals(2, afregningRepository.count());
        assertCompletion();
        Transaction postering3 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "DIFFERENT2", "M");
        Transaction postering4 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "DIFFERENT", "I");
        transactionRepository.save(postering3);
        transactionRepository.save(postering4);
        jobLauncherCommandLineRunner.run();
        Assert.assertEquals(3,angivelseRepository.count());
        Assert.assertEquals(3, afregningRepository.count());
        assertCompletion();
    }

    @Test
    public void testSameProcedureImport() throws Exception {
        setClock(new Date());
        Transaction postering = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "I");
        Transaction postering1 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "62", "FORÆDLING", "I");
        Transaction postering2 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "M");
        transactionRepository.save(postering);
        transactionRepository.save(postering1);
        transactionRepository.save(postering2);
        jobLauncherCommandLineRunner.run();
        Assert.assertEquals(2,angivelseRepository.count());
        Assert.assertEquals(2, afregningRepository.count());
        assertCompletion();
        jobLauncherCommandLineRunner.run();
        Assert.assertEquals(2,angivelseRepository.count());
        Assert.assertEquals(2, afregningRepository.count());
        assertCompletion();
    }

    @Test
    public void testSameProcedureManifest() throws Exception {
        setClock(new Date());
        Transaction postering = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "M");
        Transaction postering1 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "62", "FORÆDLING", "M");
        Transaction postering2 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "I");
        transactionRepository.save(postering);
        transactionRepository.save(postering1);
        transactionRepository.save(postering2);
        jobLauncherCommandLineRunner.run();
        Assert.assertEquals(2,angivelseRepository.count());
        Assert.assertEquals(2, afregningRepository.count());
        assertCompletion();
        jobLauncherCommandLineRunner.run();
        Assert.assertEquals(2,angivelseRepository.count());
        Assert.assertEquals(2, afregningRepository.count());
        assertCompletion();
    }

    @Test
    public void testDifferentProcedureImport() throws Exception {
        setClock(new Date());
        Transaction postering = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "I");
        Transaction postering1 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "SOMETHING", "I");
        Transaction postering2 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "ELSE", "I");
        Transaction postering3 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "M");
        transactionRepository.save(postering);
        transactionRepository.save(postering1);
        transactionRepository.save(postering2);
        transactionRepository.save(postering3);
        jobLauncherCommandLineRunner.run();
        Assert.assertEquals(4,angivelseRepository.count());
        Assert.assertEquals(4, afregningRepository.count());
        assertCompletion();
        jobLauncherCommandLineRunner.run();
        Assert.assertEquals(4,angivelseRepository.count());
        Assert.assertEquals(4, afregningRepository.count());
        assertCompletion();
    }

    @Test
    public void testDifferentProcedureManifest() throws Exception {
        setClock(new Date());
        Transaction postering = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "M");
        Transaction postering1 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "SOMETHING", "M");
        Transaction postering2 = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "I");
        transactionRepository.save(postering);
        transactionRepository.save(postering1);
        transactionRepository.save(postering2);
        jobLauncherCommandLineRunner.run();
        Assert.assertEquals(2,angivelseRepository.count());
        Assert.assertEquals(2, afregningRepository.count());
        assertCompletion();
    }

    @Test
    public void testConfiguration() throws Exception {
        int newValue = 20;
        int oldValue = 10;
        int unusedValue = 30;
        Konfiguration newConfig = new Konfiguration("1451","503",newValue,"19","061","22200","22500",testDataUtil.dateFromString("31-05-2017"));
        Konfiguration oldConfig = new Konfiguration("1450","505",oldValue,"19","020","22200","22500",testDataUtil.dateFromString("01-01-2017"));
        Konfiguration unusedConfig = new Konfiguration("1451","503",unusedValue,"19","061","22200","22500",testDataUtil.dateFromString("31-06-2017"));
        konfigurationRepository.save(oldConfig);
        konfigurationRepository.save(newConfig);
        konfigurationRepository.save(unusedConfig);

        setClock(testDataUtil.dateFromString("01-02-2017"));
        setKonfiguration();
        Angivelse angivelseI = new Angivelse("11752446", "15745304", 2017121164754L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("01-01-2017"), 42, SourceSystem.IMPORT, "FORÆDLING");
        testDataUtil.saveAngivelse(angivelseI);
        JobExecution jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_GENERATE_PAYMENTS);
        Assert.assertEquals(1, afregningRepository.count());
        Assert.assertEquals(oldValue, afregningRepository.findByAngivelse(angivelseI).get(0).getBeloeb());

        setClock(testDataUtil.dateFromString("31-05-2017"));
        setKonfiguration();
        Angivelse angivelseM = new Angivelse("56675817", "35451714", 2017101474608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), 42, SourceSystem.IMPORT, "FORÆDLING");
        testDataUtil.saveAngivelse(angivelseM);
        jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_GENERATE_PAYMENTS);
        Assert.assertEquals(2, afregningRepository.count());
        Assert.assertEquals(newValue, afregningRepository.findByAngivelse(angivelseM).get(0).getBeloeb());

    }

    @Test
    public void testImport() throws Exception {
        setClock(testDataUtil.dateFromString("31-01-2017"));
        Angivelse angivelse = new Angivelse("11752446", "15745304", 2017121174754L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("01-01-2017"), 42, SourceSystem.IMPORT, "FORÆDLING");
        testDataUtil.saveAngivelse(angivelse);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        AssertFile.assertFileEquals(new FileSystemResource(ResourceUtils.getFile(EXPECTED_FILE_IMPORT)), new FileSystemResource(OUTPUT_IMPORT));
    }

    @Test
    public void testManifest() throws Exception {
        setClock(testDataUtil.dateFromString("31-01-2017"));
        Angivelse angivelse = new Angivelse("56675817", "35451714", 2017601484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), 42, SourceSystem.MANIFEST, "FORÆDLING");
        testDataUtil.saveAngivelse(angivelse);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        AssertFile.assertFileEquals(new FileSystemResource(ResourceUtils.getFile(EXPECTED_FILE_MANIFEST)), new FileSystemResource(OUTPUT_MANIFEST));
    }

    @Test
    public void testPostering() throws Exception{
        Angivelse angivelse = new Angivelse("56675817", "35451714", 2017691484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), 42, SourceSystem.IMPORT, "FORÆDLING");
        testDataUtil.saveAngivelse(angivelse);
        Assert.assertEquals(dbUtils.getAfregningByAngivelse(angivelse).isEmpty(),true);
        JobExecution jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_GENERATE_PAYMENTS);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        Assert.assertNotNull(dbUtils.getAfregningByAngivelse(angivelse));
    }

    @Test
    public void testModpostering() throws Exception{
        Transaction postering = new Transaction(56675817, 35451714, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "42", "FORÆDLING", "I");
        transactionRepository.save(postering);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        Angivelse angivelse = angivelseRepository.findByReferenceNummerAndSystemAndProcedure(2017681484608L, SourceSystem.IMPORT, "FORÆDLING");
        Assert.assertNotNull(angivelse.getPostering());
        Assert.assertNull(angivelse.getModpostering());
        Transaction modpostering = new Transaction(null, null, 2017681484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), "92", "FORÆDLING", "I");
        transactionRepository.save(modpostering);
        jobExecution =  jobLauncherTestUtils.launchJob();
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        angivelse = angivelseRepository.findOne(angivelse.getId());
        Assert.assertNotNull(angivelse.getPostering());
        Assert.assertNotNull(angivelse.getModpostering());
        Assert.assertEquals(angivelse.getPostering().getBeloeb(),angivelse.getModpostering().getBeloeb() * -1);
    }

    @Test
    public void testNoModposteringWithoutPostering() throws Exception{
        Angivelse angivelse = new Angivelse("56675817", "35451714", 2017671484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), 95, SourceSystem.IMPORT, "FORÆDLING");
        angivelseRepository.save(angivelse);
        Assert.assertEquals(afregningRepository.findByAngivelse(angivelse).isEmpty(), true);
        JobExecution jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_GENERATE_PAYMENTS);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        Assert.assertEquals(afregningRepository.findByAngivelse(angivelse).isEmpty(), true);
        angivelse = angivelseRepository.findOne(angivelse.getId());
        Assert.assertNull(angivelse.getModpostering());
        Assert.assertNull(angivelse.getPostering());
    }

    @Test
    public void testLoad() throws Exception{
        setClock(new Date());
        for(int i = 0; i < 10; i++) {
            testDataUtil.insertRandomTransaction(1000,i * 1000,null,false);
            JobExecution jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_GENERATE_STATEMENTS);
            Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
            jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_UPDATE_STATEMENTS);
            Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
            jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_GENERATE_PAYMENTS);
            Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        }
    }

    @Test
    public void testBigLoad() throws Exception{
        setClock(new Date());
        testDataUtil.insertRandomTransaction(100000,0,null,true);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

    @Test
    public void testSameLeverance() throws Exception {
        setClock(testDataUtil.dateFromString("31-01-2017"));
        Angivelse angivelseI = new Angivelse("11752446", "15745304", 2017121174754L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("01-01-2017"), 42, SourceSystem.IMPORT, "FORÆDLING");
        testDataUtil.saveAngivelse(angivelseI);
        JobExecution jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_GENERATE_PAYMENTS);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_GENERATE_IMPORTFILE);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        AssertFile.assertFileEquals(new FileSystemResource(ResourceUtils.getFile(EXPECTED_FILE_IMPORT)), new FileSystemResource(OUTPUT_IMPORT));
        Angivelse angivelseM = new Angivelse("56675817", "35451714", 2017101484608L, testDataUtil.dateFromString("31-01-2017"), testDataUtil.dateFromString("02-01-2017"), 42, SourceSystem.IMPORT, "FORÆDLING");
        testDataUtil.saveAngivelse(angivelseM);
        jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_GENERATE_PAYMENTS);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        jobExecution = jobLauncherTestUtils.launchStep(BatchConfiguration.STEP_GENERATE_IMPORTFILE);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        AssertFile.assertFileEquals(new FileSystemResource(ResourceUtils.getFile(EXPECTED_FILE_IMPORT_RE)), new FileSystemResource(OUTPUT_IMPORT));
    }

    @Test
    public void generateDR() throws Exception {
        setClock(testDataUtil.dateFromString("01-05-2017"));
        testDataUtil.insertRandomTransactionFromList(1,0,SourceSystem.IMPORT,false);
        testDataUtil.insertRandomTransactionFromList(1,0,SourceSystem.MANIFEST,false);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

    private void setClock(Date d) {
        Mockito.when(clock.millis()).thenReturn(d.getTime());
    }

    private void setKonfiguration() {
        EntityManager entityManager = entityManagerFactory.getNativeEntityManagerFactory().createEntityManager();
        Query q = entityManager.createQuery("SELECT k FROM Konfiguration k");
        List<Konfiguration> list = q.getResultList();
        entityManager.close();
        Konfiguration mockKonfiguration = new Konfiguration(true);
        if(!list.isEmpty()) {
            Collections.reverse(list);
            for(Konfiguration k : list) {
                if(k.getStartDato().getTime() <= clock.millis()) {
                    mockKonfiguration = k;
                    break;
                }
            }
        }
        Mockito.when(konfiguration.getBeloeb()).thenReturn(mockKonfiguration.getBeloeb());
        Mockito.when(konfiguration.getKontoNummer()).thenReturn(mockKonfiguration.getKontoNummer());
        Mockito.when(konfiguration.getHjemDistrikt()).thenReturn(mockKonfiguration.getHjemDistrikt());
        Mockito.when(konfiguration.getKontoArt()).thenReturn(mockKonfiguration.getKontoArt());
        Mockito.when(konfiguration.getTekstKode()).thenReturn(mockKonfiguration.getTekstKode());
        Mockito.when(konfiguration.getOprettelse()).thenReturn(mockKonfiguration.getOprettelse());
        Mockito.when(konfiguration.getSletning()).thenReturn(mockKonfiguration.getSletning());
    }

    private void assertFailure() {
        Assert.assertEquals("FAILED", jobExplorer.getJobExecutions(jobExplorer.getJobInstances("paymentsJob", 0, 1000).stream().max(Comparator.comparingLong(JobInstance::getInstanceId)).get()).get(0).getExitStatus().getExitCode());
    }

    private void assertCompletion() {
        Assert.assertEquals("COMPLETED", jobExplorer.getJobExecutions(jobExplorer.getJobInstances("paymentsJob", 0, 1000).stream().max(Comparator.comparingLong(JobInstance::getInstanceId)).get()).get(0).getExitStatus().getExitCode());
    }
}
