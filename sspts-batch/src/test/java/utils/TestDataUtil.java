package utils;

import dk.skat.ict.sspts.batch.business.Angivelse;
import dk.skat.ict.sspts.batch.business.Transaction;
import dk.skat.ict.sspts.batch.enums.SourceSystem;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by mns on 08-03-2017.
 */
public class TestDataUtil {

    private EntityManagerFactory entityManagerFactory;

    public TestDataUtil(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void insertRandomAngivelse(int n, int s, SourceSystem type, boolean negative) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Random rnd = new Random(new Date().getTime());
        for(int i = 0; i < n; i++){
            SourceSystem sourceSystem = type;
            if(sourceSystem == null) {
                if(rnd.nextInt(100) > 50){
                    sourceSystem = SourceSystem.IMPORT;
                } else {
                    sourceSystem = SourceSystem.MANIFEST;
                }
            }
            int status = negative ? rnd.nextInt(100) : rnd.nextInt(90);
            Angivelse angivelseImport = new Angivelse(generateSENumber(), generateSENumber(), generateRefNumber(sourceSystem,s+i,6), new Date(), new Date(), status, sourceSystem, "FORÆDLING");
            entityManager.persist(angivelseImport);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public void insertRandomAngivelseWithSENummer(int n, int s, SourceSystem type, boolean negative) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Random rnd = new Random(new Date().getTime());
        for(int i = 0; i < n; i++){
            SourceSystem sourceSystem = type;
            if(sourceSystem == null) {
                if(rnd.nextInt(100) > 50){
                    sourceSystem = SourceSystem.IMPORT;
                } else {
                    sourceSystem = SourceSystem.MANIFEST;
                }
            }
            int status = negative ? rnd.nextInt(100) : rnd.nextInt(90);
            Angivelse angivelseImport = new Angivelse(generateSENumberFromList(), generateSENumberFromList(), generateRefNumber(sourceSystem,s+i,6), new Date(), new Date(), status, sourceSystem, "FORÆDLING");
            entityManager.persist(angivelseImport);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public void insertRandomTransaction(int n, int s, SourceSystem type, boolean negative) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Random rnd = new Random(new Date().getTime());
        for(int i = 0; i < n; i++){
            SourceSystem sourceSystem = type;
            if(sourceSystem == null) {
                if(rnd.nextInt(100) > 50){
                    sourceSystem = SourceSystem.IMPORT;
                } else {
                    sourceSystem = SourceSystem.MANIFEST;
                }
            }
            int status = negative ? rnd.nextInt(100) : rnd.nextInt(90);
            Transaction transaction = new Transaction(Integer.parseInt(generateSENumber()), Integer.parseInt(generateSENumber()), generateRefNumber(sourceSystem,s+i,6), new Date(), new Date(), String.valueOf(status), "FORÆDLING", sourceSystem.getIdentifier());
            entityManager.persist(transaction);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public void insertRandomTransactionFromList(int n, int s, SourceSystem type, boolean negative) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Random rnd = new Random(new Date().getTime());
        for(int i = 0; i < n; i++){
            SourceSystem sourceSystem = type;
            if(sourceSystem == null) {
                if(rnd.nextInt(100) > 50){
                    sourceSystem = SourceSystem.IMPORT;
                } else {
                    sourceSystem = SourceSystem.MANIFEST;
                }
            }
            int status = negative ? rnd.nextInt(100) : rnd.nextInt(90);
            Transaction transaction = new Transaction(Integer.parseInt(generateSENumber()), Integer.parseInt(generateSENumber()),generateRefNumber(sourceSystem,s+i,6), new Date(), new Date(), String.valueOf(status), "FORÆDLING", sourceSystem.getIdentifier());
            entityManager.persist(transaction);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public void saveAngivelse(Angivelse a){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(a);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private String generateSENumber() {
        Random rnd = new Random(new Date().getTime());
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 8; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    private Long generateRefNumber(SourceSystem s, int index, int fixed) {
        Random rnd = new Random(new Date().getTime());
        StringBuilder sb = new StringBuilder();
        sb.append("2017");
        if(s.equals(SourceSystem.IMPORT)) {
            sb.append("1");
        } else if(s.equals(SourceSystem.MANIFEST)) {
            sb.append("6");
        }
        for(int i = 0; i < 8-fixed; i++) {
            sb.append(rnd.nextInt(10));
        }
        sb.append(index);
        return Long.parseLong(sb.toString());
    }

    private String generateSENumberFromList() {
        int[] numbers = {29090017,29090211,29090114,29090416,29090513,29093512,29090718,29090815,29091013,29091110};
        Random r = new Random();
        return String.valueOf(numbers[r.nextInt(numbers.length)]);
    }

    private Long generateRefNumber(SourceSystem s) {
        return generateRefNumber(s,0,0);
    }

    public Date dateFromString(String s) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        try {
            return formatter.parse(s);
        } catch (ParseException e) {
            return new Date();
        }
    }
}
