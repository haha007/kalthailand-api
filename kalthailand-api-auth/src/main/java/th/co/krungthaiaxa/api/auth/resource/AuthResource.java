package th.co.krungthaiaxa.api.auth.resource;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.auth.jwt.JwtTokenUtil;
import th.co.krungthaiaxa.api.auth.model.RequestForToken;
import th.co.krungthaiaxa.api.auth.model.Token;
import th.co.krungthaiaxa.api.auth.model.Error;

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
import static th.co.krungthaiaxa.api.auth.model.ErrorCode.*;
import static th.co.krungthaiaxa.api.auth.utils.JsonUtil.getJson;

@RestController
@Api(value = "Authentication")
public class AuthResource {
    private final static Logger logger = LoggerFactory.getLogger(AuthResource.class);

    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @ApiOperation(value = "Creates a token", notes = "Creates a JWT token containing user Roles", response = Token.class)
    @RequestMapping(value = "/auth", produces = APPLICATION_JSON_VALUE, method = POST)
    public ResponseEntity<?> createAuthenticationToken(
            @ApiParam(value = "The credentials to get token for", required = true)
            @RequestBody RequestForToken requestForToken) {
        // Perform the security
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                requestForToken.getUserName(),
                requestForToken.getPassword());
        final Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-security so we can generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(requestForToken.getUserName());
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Return the token
        logger.info("Token has been successfully created");
        return ok(Token.of(token));
    }

    @ApiOperation(value = "Validates a token", notes = "Validates a JWT token for given Role", response = String.class)
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
            return badRequest().body(getJson(TOKEN_EMPTY));
        }

        if (jwtTokenUtil.isTokenExpired(token)) {
            logger.error("Token has expired");
            return badRequest().body(getJson(TOKEN_EXPIRED));
        }

        Optional<List> roles = jwtTokenUtil.getRolesFromToken(token);
        if (!roles.isPresent()) {
            logger.error("Token has no role");
            return badRequest().body(getJson(NO_ROLE));
        }

        if (!roles.get().contains(roleName)) {
            logger.error("Role [" + roleName + "] is not available in provided token");
            return new ResponseEntity<>(ROLE_NOT_ALLOWED.apply(roleName), NOT_ACCEPTABLE);
        } else {
            logger.info("Role is available in provided token");
            return ok(null);
        }
    }

}
