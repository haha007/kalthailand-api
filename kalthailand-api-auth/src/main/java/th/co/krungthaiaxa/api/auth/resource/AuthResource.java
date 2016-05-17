package th.co.krungthaiaxa.api.auth.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import th.co.krungthaiaxa.api.auth.jwt.JwtTokenUtil;
import th.co.krungthaiaxa.api.auth.model.RequestForToken;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static th.co.krungthaiaxa.api.auth.model.ErrorCode.*;
import static th.co.krungthaiaxa.api.auth.utils.JsonUtil.getJson;

@RestController
public class AuthResource {

    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @RequestMapping(value = "/auth", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody RequestForToken requestForToken) {
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
        return ok(token);
    }

    @RequestMapping(value = "/auth/validate/{roleName}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = GET)
    public ResponseEntity<?> validateToken(@PathVariable String roleName, HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        if (jwtTokenUtil.isTokenExpired(token)) {
            return badRequest().body(getJson(TOKEN_EXPIRED));
        }

        Optional<List> roles = jwtTokenUtil.getRolesFromToken(token);
        if (!roles.isPresent()) {
            return badRequest().body(getJson(NO_ROLE));
        }

        if (!roles.get().contains(roleName)) {
            return new ResponseEntity<>(ROLE_NOT_ALLOWED.apply(roleName), NOT_ACCEPTABLE);
        } else {
            return ok(null);
        }
    }

}
