package th.co.krungthaiaxa.api.auth.service;

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
import java.time.LocalDateTime;
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

    public User getUserDetailByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> createNewUser(final UserDTO userModal) {
        final User userEntity = new User();
        userEntity.setUsername(userModal.getUsername());
        userEntity.setActivationKey(RandomUtil.generateActivationKey());
        userEntity.setResetKey(null);
        userEntity.setResetDate(null);
        userEntity.setRoles(userModal.getRoles());
        userEntity.setLastName(userModal.getLastName());
        userEntity.setFirstName(userModal.getFirstName());
        userEntity.setEmail(userModal.getEmail());
        userEntity.setPassword(null);
        userEntity.setActivated(Boolean.FALSE);
        return Optional.of(userRepository.save(userEntity));
    }


    /**
     * Create new user from UserDTO.
     *
     * @param userDTO
     * @return UserDTO
     */
    public User addUser(final UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword())); //encode password 
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRoles(userDTO.getRoles());
        user.setActivated(false); // account need to be verified
        user.setActivationKey(RandomUtil.generateActivationKey()); // verify activationKey
        user.setResetDate(null); // 
        user.setResetKey(null); // verify resetPasswordKey
        return userRepository.save(user);
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

    /**
     * Request password reset with email
     *
     * @param email email of user
     * @return UserDTO
     */
    public Optional<User> requestPasswordReset(final String email) {
        return userRepository.findOneByEmail(email)
                .filter(User::isActivated)
                .map(user -> {
                    user.setResetKey(RandomUtil.generateResetKey());
                    user.setResetDate(LocalDateTime.now());
                    userRepository.save(user);
                    return user;
                });
    }


    /**
     * Complete password reset by new password and reset password key
     *
     * @param newPassword new password
     * @param key         reset password key
     * @return UserDTO
     */
    public Optional<User> completePasswordReset(final String newPassword, final String key) {
        LOGGER.debug("Reset user password for reset key {}", key);
        return userRepository.findOneByResetKey(key)
                .filter(user -> {
                    LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
                    return user.getResetDate().isAfter(oneDayAgo);
                })
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setResetKey(null);
                    user.setResetDate(null);
                    userRepository.save(user);
                    return user;
                });
    }

}
