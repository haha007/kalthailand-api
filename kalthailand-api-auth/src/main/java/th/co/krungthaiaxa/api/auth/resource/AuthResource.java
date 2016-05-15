package th.co.krungthaiaxa.api.auth.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.auth.jwt.JwtTokenUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
    public ResponseEntity<?> createAuthenticationToken(@RequestParam String userName, @RequestParam String password) throws AuthenticationException {
        // Perform the security
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userName,
                password);
        final Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-security so we can generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Return the token
        return ResponseEntity.ok(token);
    }

    @RequestMapping(value = "/auth/refresh", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = GET)
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        Optional<String> username = jwtTokenUtil.getUsernameFromToken(token);
        if (!username.isPresent()) {
            return ResponseEntity.badRequest().body("Invalid token, unable to get user name");
        }

        if (!jwtTokenUtil.canTokenBeRefreshed(token)) {
            return ResponseEntity.badRequest().body(null);
        }

        Optional<String> refreshedToken = jwtTokenUtil.refreshToken(token);
        if (!refreshedToken.isPresent()) {
            return ResponseEntity.badRequest().body("Invalid refreshed token");
        }

        return ResponseEntity.ok(refreshedToken);
    }

    @RequestMapping(value = "/auth/validate/{roleName}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = GET)
    public ResponseEntity<?> validateToken(@PathVariable String roleName, HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        Optional<List> roles = jwtTokenUtil.getRolesFromToken(token);
        if (!roles.isPresent()) {
            return ResponseEntity.badRequest().body("Invalid token, unable to get roles");
        }

        if (!roles.get().contains(roleName)) {
            return new ResponseEntity<>(null, NOT_ACCEPTABLE);
        } else {
            return ResponseEntity.ok(null);
        }
    }

}
