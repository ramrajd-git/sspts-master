package dk.skat.ict.sspts.batch.repository;

import dk.skat.ict.sspts.batch.business.Angivelse;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mns on 21-03-2017.
 */
@Repository
public interface AngivelseRepository extends CrudRepository<Angivelse, Long>, PagingAndSortingRepository<Angivelse, Long> {
    Angivelse findByReferenceNummerAndSystemAndProcedure(Long reference, SourceSystem sourceSystem, String procedure);

    long countByReferenceNummerAndSystemAndProcedure(Long reference, SourceSystem sourceSystem, String procedure);

    long countByReferenceNummerAndSystem(Long reference, SourceSystem sourceSystem);
}
