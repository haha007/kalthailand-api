package th.co.krungthaiaxa.api.auth.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.auth.data.User;

import java.util.List;
import java.util.Optional;

/**
 * @author tuong.le on 5/23/17.
 */

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String> {

    //Check user account that has username case insensitive and been activated.
    @Query(value="{ activated: true, activationKey: {$not: { $exists: true, $ne: ''}}, username: {$regex : '^?0$', $options: 'i'} }")
    List<User> findActiveUsername(final String username, Pageable pageable);

    Optional<User> findOneByActivationKey(final String activateKey);

    Optional<User> findByUsername(final String username);
}
