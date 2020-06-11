package dk.skat.ict.sspts.batch.repository;

import dk.skat.ict.sspts.batch.business.Afregning;
import dk.skat.ict.sspts.batch.business.Angivelse;
import dk.skat.ict.sspts.batch.enums.AfregningType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by mns on 21-03-2017.
 */
@Repository
public interface AfregningRepository extends CrudRepository<Afregning, Long>, PagingAndSortingRepository<Afregning, Long> {
    List<Afregning> findByAngivelse(Angivelse angivelse);

    List<Afregning> findByAngivelseAndType(Angivelse angivelse, AfregningType type);
}
