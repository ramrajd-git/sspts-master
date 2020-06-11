package dk.skat.ict.sspts.batch.repository;

import dk.skat.ict.sspts.batch.business.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by mns on 21-03-2017.
 */
@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long>, PagingAndSortingRepository<Transaction, Long> {
}
