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
import th.co.krungthaiaxa.api.auth.model.UserDTO;
import th.co.krungthaiaxa.api.auth.service.UserService;
import th.co.krungthaiaxa.api.auth.utils.EmailSender;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author tuong.le on 5/31/17.
 */
@RestController
@Api(value = "Users")
public class UserResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

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
    public ResponseEntity<?> getListOfUser() {
        final Pageable pageable = new PageRequest(0, 100);
        return ResponseEntity.ok(userService.getAllUser(pageable));
    }

    @ApiOperation(value = "Create new user", notes = "Create new user", response = UserDTO.class)
    @RequestMapping(value = "/users", produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<?> createUser(@RequestBody @Valid final UserDTO userModal, final HttpServletRequest request) {

        if (userService.getUserByUsername(userModal.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("userexists", "username already in use"));
        }

        final Optional<User> userOptional = userService.createNewUser(userModal);
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            emailSender.sendEmail(fromEmail, user.getEmail(), "Activation Email",
                    "Activation Link: " + getActivationLink(request, user.getActivationKey()));
            return ResponseEntity.ok(new UserDTO(user));
        }
        return ResponseEntity.ok(Collections.singletonMap("success", Boolean.FALSE));
    }

    @ApiOperation(value = "Update user info", notes = "Update user info", response = UserDTO.class)
    @RequestMapping(value = "/users", produces = APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<?> updateUser(@RequestBody final UserDTO userModal) {
        if (userService.updateUser(userModal).isPresent()) {
            return ResponseEntity.ok(userModal);
        }
        return ResponseEntity.ok(Collections.singletonMap("success", Boolean.FALSE));
    }

    private String getActivationLink(final HttpServletRequest request, final String activationKey) {
        StringBuffer url = request.getRequestURL();
        String uri = request.getRequestURI();
        String host = url.substring(0, url.indexOf(uri));
        return host + "/admin-elife/activate/" + activationKey;
    }

}
