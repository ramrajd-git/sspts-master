package dk.skat.ict.sspts.batch.writer;

import dk.skat.ict.sspts.batch.business.Afregning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

/**
 * Custom Writer til at skrive afregninger til databasen.
 */
public class AfregningDBWriter implements ItemWriter<Afregning>, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(AfregningDBWriter.class);

    private EntityManagerFactory entityManagerFactory;

    /**
     * Set the EntityManager to be used internally.
     *
     * @param entityManagerFactory the entityManagerFactory to set
     */
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Check mandatory properties - there must be an entityManagerFactory.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(entityManagerFactory, "An EntityManagerFactory is required");
    }

    /**
     * Merge all provided items that aren't already in the persistence context
     * and then flush the entity manager.
     *
     * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
     */
    @Override
    public void write(List<? extends Afregning> items) {
        EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
        if (entityManager == null) {
            throw new DataAccessResourceFailureException("Unable to obtain a transactional EntityManager");
        }
        doWrite(entityManager, items);
        entityManager.flush();
    }

    /**
     * Do perform the actual write operation. This can be overridden in a
     * subclass if necessary.
     *
     * @param entityManager the EntityManager to use for the operation
     * @param items the list of items to use for the write
     */
    protected void doWrite(EntityManager entityManager, List<? extends Afregning> items) {

        if (log.isDebugEnabled()) {
            log.debug("Writing to JPA with " + items.size() + " items.");
        }

        if (!items.isEmpty()) {
            long mergeCount = 0;
            for (Afregning afregning : items) {
                if(!entityManager.contains(afregning.getLeverance())) {
                    entityManager.merge(afregning.getLeverance());
                }
                if(!entityManager.contains(afregning.getAngivelse())) {
                    entityManager.merge(afregning.getAngivelse());
                }
                entityManager.merge(afregning);
            if (log.isDebugEnabled()) {
                log.debug(mergeCount + " entities merged.");
                log.debug((items.size() - mergeCount) + " entities found in persistence context.");
            }
        }
    }
}
}
