package th.co.krungthaiaxa.api.auth.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.auth.data.User;
import th.co.krungthaiaxa.api.auth.model.UserDTO;
import th.co.krungthaiaxa.api.auth.repository.UserRepository;
import th.co.krungthaiaxa.api.auth.utils.RandomUtil;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author tuong.le on 5/23/17.
 * @implNote Please take care the Cachable for any update related to User information, specially update user password
 * @link updateUser function
 * @see CacheEvict
 */
@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Inject
    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> getAllUser(final Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * @param username is unique in system that be used as key for caching
     * @return User
     * @implNote This function is used for Authentication only
     */
    @Cacheable(cacheNames = "userAuthentication", key = "#username")
    public Optional<User> getOneActiveUserDetailByUsername(final String username) {
        final Pageable pageable = new PageRequest(0, 1);
        return userRepository.findActiveUsername(username, pageable).stream().findFirst();
    }

    public Optional<User> getUserByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Create new user from UserDTO.
     *
     * @param userDTO user data transfer
     * @return Optional User Entity
     */
    public Optional<User> createNewUser(final UserDTO userDTO) {
        final User userEntity = new User();
        userEntity.setUsername(userDTO.getUsername());
        userEntity.setRoles(userDTO.getRoles());
        userEntity.setLastName(userDTO.getLastName());
        userEntity.setFirstName(userDTO.getFirstName());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPassword(StringUtils.EMPTY);

        userEntity.setActivationKey(RandomUtil.generateActivationKey());
        userEntity.setActivated(Boolean.FALSE);

        return Optional.of(userRepository.save(userEntity));
    }

    /**
     * Activate account by activated key that would be sent in registration email.
     *
     * @param activationKey activation Key
     * @param password      user password
     * @return Optional User Entity
     */
    public Optional<User> activateRegistration(final String activationKey, final String password) {
        LOGGER.debug("Activating user for activation key {}", activationKey);
        final Optional<User> userOptional = userRepository.findOneByActivationKey(activationKey);
        if (userOptional.isPresent()) {
            return userOptional.map(user -> {
                user.setActivated(true);
                user.setActivationKey(null);
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                LOGGER.debug("Activated username: {}", user.getUsername());
                return user;
            });
        }
        LOGGER.debug("Activation key {} is invalid", activationKey);
        return Optional.empty();
    }

    /**
     * Update user information exclude password from UserDTO.
     *
     * @param userDTO user data
     * @return Optional User Entity
     */
    @CacheEvict(cacheNames = "userAuthentication", key = "#userDTO.username")
    public Optional<User> updateUser(final UserDTO userDTO) {
        //find User by Id
        return Optional.of(userRepository.findOne(userDTO.getId())).map(user -> {
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setRoles(userDTO.getRoles());
            user.setEmail(userDTO.getEmail());
            user.setActivated(userDTO.isActivated());
            return userRepository.save(user);
        });
    }

}
