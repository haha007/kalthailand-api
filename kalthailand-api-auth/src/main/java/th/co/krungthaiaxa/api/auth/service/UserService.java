package th.co.krungthaiaxa.api.auth.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 */
@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Inject
    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> getAllUser(final Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> getActiveUserDetailByUsername(final String username) {
        return userRepository.findActiveUsername(username);
    }

    /**
     * Create new user from UserDTO.
     *
     * @param userDTO user data transfer
     * @return Option User Entity
     */
    public Optional<User> createNewUser(final UserDTO userDTO) {
        final User userEntity = new User();
        userEntity.setUsername(userDTO.getUsername());
        userEntity.setRoles(userDTO.getRoles());
        userEntity.setLastName(userDTO.getLastName());
        userEntity.setFirstName(userDTO.getFirstName());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPassword(StringUtils.EMPTY);

        //generate activation key
        userEntity.setActivationKey(RandomUtil.generateActivationKey());
        userEntity.setActivated(Boolean.FALSE);

        return Optional.of(userRepository.save(userEntity));
    }

    /**
     * Activate account by activated key that would be sent in registration email
     *
     * @param key activated key
     * @return UserDTO
     */
    public Optional<User> activateRegistration(final String key) {
        LOGGER.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
                .map(user -> {
                    // activate given user for the registration key.
                    user.setActivated(true);
                    user.setActivationKey(null);
                    userRepository.save(user);
                    LOGGER.debug("Activated user: {}", user);
                    return user;
                });
    }

    /**
     * Update user information exclude password from UserDTO.
     *
     * @param userDTO user data
     * @return UserDTO
     */
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
