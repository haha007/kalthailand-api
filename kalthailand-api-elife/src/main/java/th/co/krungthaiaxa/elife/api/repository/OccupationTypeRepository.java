package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.data.OccupationType;

@Repository
public interface OccupationTypeRepository extends PagingAndSortingRepository<OccupationType, Integer> {
    OccupationType findByOccId(Integer occId);
}
