package dk.skat.ict.sspts.batch.repository;

import dk.skat.ict.sspts.batch.business.Leverance;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by mns on 21-03-2017.
 */
@Repository
public interface LeveranceRepository extends CrudRepository<Leverance, Long>, PagingAndSortingRepository<Leverance, Long> {

    Leverance findTopBySystemOrderByLeveranceNummerDesc(SourceSystem system);
}
