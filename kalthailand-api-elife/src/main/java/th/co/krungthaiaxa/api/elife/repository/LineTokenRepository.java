package th.co.krungthaiaxa.api.elife.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.data.LineToken;

@Repository
public interface LineTokenRepository extends PagingAndSortingRepository<LineToken, Integer> {
	
	LineToken findByRowId(Integer rowId);
	
}
