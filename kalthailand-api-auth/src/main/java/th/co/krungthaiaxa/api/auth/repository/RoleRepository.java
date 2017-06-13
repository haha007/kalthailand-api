package th.co.krungthaiaxa.api.auth.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.auth.data.Role;

/**
 * @author tuong.le on 5/23/17.
 */
@Repository
public interface RoleRepository extends PagingAndSortingRepository<Role, String> {
}
