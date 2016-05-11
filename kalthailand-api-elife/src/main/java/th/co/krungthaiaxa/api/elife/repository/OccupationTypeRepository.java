package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.OccupationType;

@Repository
public interface OccupationTypeRepository extends PagingAndSortingRepository<OccupationType, Integer> {
    OccupationType findByOccId(Integer occId);
}
