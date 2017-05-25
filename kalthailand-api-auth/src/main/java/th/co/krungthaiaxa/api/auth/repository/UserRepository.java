package th.co.krungthaiaxa.api.auth.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.auth.data.User;

import java.util.Optional;

/**
 * @author tuong.le on 5/23/17.
 */

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String> {
    @Query("{ username : ?0 }")
    User findByUsername(String username);

    Optional<User> findOneByEmail(String mail);

    Optional<User> findOneByResetKey(String resetPasswordKey);

    Optional<User> findOneByActivationKey(String activateKey);
}
