package th.co.krungthaiaxa.api.auth.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.auth.data.User;
import th.co.krungthaiaxa.api.auth.model.RequestActivateUser;
import th.co.krungthaiaxa.api.auth.model.UserDTO;
import th.co.krungthaiaxa.api.auth.service.UserService;
import th.co.krungthaiaxa.api.auth.utils.EmailSender;
import th.co.krungthaiaxa.api.common.annotation.RequiredAdminRole;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.IOUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author tuong.le on 5/31/17.
 */
@RestController
@Api(value = "Users")
public class UserResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

    private static final String SUCCESS = "success";

    private final UserService userService;

    private final EmailSender emailSender;

    @Value("${email.name}")
    private String fromEmail;

    @Inject
    public UserResource(final UserService userService, final EmailSender emailSender) {
        this.userService = userService;
        this.emailSender = emailSender;
    }

    @ApiOperation(value = "Get list of users", notes = "Get list of users", response = UserDTO.class)
    @RequestMapping(value = "/users", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @RequiredAdminRole
    public ResponseEntity getListOfUser() {
        final Pageable pageable = new PageRequest(0, 100);
        return ResponseEntity.ok(userService.getAllUser(pageable));
    }

    @ApiOperation(value = "Create new user", notes = "Create new user", response = UserDTO.class)
    @RequestMapping(value = "/users", produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @RequiredAdminRole
    public ResponseEntity createUser(@RequestBody @Valid final UserDTO userModal, final HttpServletRequest request) {

        if (userService.getUserByUsername(userModal.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(ErrorCode.USER_EXISTS);
        }

        final Optional<User> userOptional = userService.createNewUser(userModal);
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            LOGGER.info("New User {} has been created", user.getUsername());

            final String emailContent =
                    getActivationEmailContent(user, getActivationLink(request, user.getActivationKey()));
            emailSender.sendEmail(fromEmail, user.getEmail(), "Activation Email", emailContent);
            LOGGER.info("Activation Email has been sent to {}", user.getEmail());
            return ResponseEntity.ok(new UserDTO(user));
        }
        LOGGER.error("Could not create user {}", userModal.getUsername());
        return ResponseEntity.badRequest().body(ErrorCode.ERROR_CREATE_USER);
    }

    @ApiOperation(value = "Update user info", notes = "Update user info", response = UserDTO.class)
    @RequestMapping(value = "/users", produces = APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    @RequiredAdminRole
    public ResponseEntity updateUser(@RequestBody @Valid final UserDTO userModal) {
        if (userService.updateUser(userModal).isPresent()) {
            LOGGER.info("User {} has been updated", userModal.getUsername());
            return ResponseEntity.ok(userModal);
        }
        return ResponseEntity.badRequest().body(ErrorCode.ERROR_UPDATE_USER);
    }

    @ApiOperation(value = "Activate user", notes = "Activate user info", response = Map.class)
    @RequestMapping(value = "/activate", produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity activateUser(@RequestBody @Valid final RequestActivateUser activateUserForm) {
        if (activateUserForm.getConfirmPassword().equals(activateUserForm.getPassword())) {
            if (userService
                    .activateRegistration(activateUserForm.getActivationKey(), activateUserForm.getPassword())
                    .isPresent()) {
                return ResponseEntity.ok(Collections.singletonMap(SUCCESS, Boolean.TRUE));
            }
            return ResponseEntity.badRequest().body(ErrorCode.INVALID_ACTIVATION_KEY);
        }
        return ResponseEntity.badRequest().body(ErrorCode.INVALID_CONFIRM_PASSWORD);
    }

    private String getActivationLink(final HttpServletRequest request, final String activationKey) {
        final StringBuffer url = request.getRequestURL();
        final String uri = request.getRequestURI();
        final String host = url.substring(0, url.indexOf(uri));
        return host + "/admin-elife/activate?key=" + activationKey;
    }

    private String getActivationEmailContent(final User user, final String activationLink) {
        String emailContent = IOUtil.loadTextFileInClassPath("/email-content/activation-user-email.html");
        emailContent = emailContent.replaceAll("%USERNAME%", user.getUsername())
                .replaceAll("%ACTIVATION_LINK%", activationLink)
                .replaceAll("%NAME%", user.getFirstName() + " " + user.getLastName());
        return emailContent;
    }

}
