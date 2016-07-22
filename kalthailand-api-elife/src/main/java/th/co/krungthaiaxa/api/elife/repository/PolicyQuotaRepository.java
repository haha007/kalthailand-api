package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import th.co.krungthaiaxa.api.elife.data.LineToken;
import th.co.krungthaiaxa.api.elife.data.PolicyQuota;

@Repository
public interface PolicyQuotaRepository extends PagingAndSortingRepository<PolicyQuota, Integer> {
	
	PolicyQuota findByRowId(Integer rowId);

}
