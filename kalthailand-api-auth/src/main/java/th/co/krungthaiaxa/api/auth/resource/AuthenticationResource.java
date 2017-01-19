package th.co.krungthaiaxa.api.auth.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.auth.jwt.JwtTokenUtil;
import th.co.krungthaiaxa.api.auth.model.ErrorCode;
import th.co.krungthaiaxa.api.auth.model.RequestForToken;
import th.co.krungthaiaxa.api.auth.service.AuthenticationService;
import th.co.krungthaiaxa.api.common.model.authentication.AuthenticatedUser;
import th.co.krungthaiaxa.api.common.model.authentication.Token;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static th.co.krungthaiaxa.api.auth.utils.JsonUtil.getJson;

@RestController
@Api(value = "Authentication")
public class AuthenticationResource {
    private final static Logger logger = LoggerFactory.getLogger(AuthenticationResource.class);

    @Value("${jwt.header}")
    private String tokenHeader;

    private final AuthenticationService authService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    public AuthenticationResource(AuthenticationService authService) {this.authService = authService;}

    @ApiOperation(value = "Creates a token", notes = "Creates a JWT token containing user Roles", response = Token.class)
    @RequestMapping(value = "/auth", produces = APPLICATION_JSON_VALUE, method = POST)
    public ResponseEntity<?> createAuthenticationToken(
            @ApiParam(value = "The credentials to get token for", required = true)
            @RequestBody RequestForToken requestForToken) {
        AuthenticatedUser authenticatedUser = authService.authenticate(requestForToken);
        return ok(Token.of(authenticatedUser.getAccessToken()));
    }

    @ApiOperation(value = "Creates a token", notes = "Return authenticated user with roles")
    @RequestMapping(value = "/auth/user", produces = APPLICATION_JSON_VALUE, method = POST)
    public AuthenticatedUser authenticateUser(
            @ApiParam(value = "The credentials to get token for", required = true)
            @RequestBody RequestForToken requestForToken) {
        return authService.authenticate(requestForToken);
    }

    @ApiOperation(value = "Validates a token", notes = "Validates a JWT token for given Role. Token has to be sent in header named 'Authorization'.", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "If token is empty or expired or with no role", response = Error.class),
            @ApiResponse(code = 406, message = "If token does not give access to the role", response = Error.class)
    })
    @RequestMapping(value = "/auth/validate/{roleName}", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResponseEntity<?> validateToken(
            @ApiParam(value = "The role to check the token against on", required = true)
            @PathVariable String roleName,
            HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        if (isEmpty(token)) {
            logger.error("Token is empty");
            return badRequest().body(getJson(ErrorCode.TOKEN_EMPTY));
        }

        if (jwtTokenUtil.isTokenExpired(token)) {
            logger.error("Token has expired");
            return badRequest().body(getJson(ErrorCode.TOKEN_EXPIRED));
        }

        Optional<List> roles = jwtTokenUtil.getRolesFromToken(token);
        if (!roles.isPresent()) {
            logger.error("Token has no role");
            return badRequest().body(getJson(ErrorCode.NO_ROLE));
        }

        if (!roles.get().contains(roleName)) {
            logger.error("Role [" + roleName + "] is not available in provided token");
            return new ResponseEntity<>(ErrorCode.ROLE_NOT_ALLOWED.apply(roleName), NOT_ACCEPTABLE);
        } else {
            logger.info("Role is available in provided token");
            return ok(null);
        }
    }

}
