package th.co.krungthaiaxa.api.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.auth.jwt.JwtTokenUtil;
import th.co.krungthaiaxa.api.auth.model.RequestForToken;
import th.co.krungthaiaxa.api.common.exeption.UnauthenticationException;
import th.co.krungthaiaxa.api.common.model.authentication.AuthenticatedUser;
import th.co.krungthaiaxa.api.common.model.authentication.AuthenticatedUserMapper;
import th.co.krungthaiaxa.api.common.utils.LogUtil;

import java.time.Instant;

/**
 * @author khoi.tran on 11/10/16.
 */
@Service
public class AuthenticationService {
    private final static Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    public AuthenticatedUser authenticate(RequestForToken requestForToken) {
        Instant start = LogUtil.logStarting("Authenticate " + requestForToken.getUserName());
        try {
            // Perform the security
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    requestForToken.getUserName(),
                    requestForToken.getPassword());
            final Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Reload password post-security so we can generate token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(requestForToken.getUserName());
            final String token = jwtTokenUtil.generateToken(userDetails);
            AuthenticatedUser result = AuthenticatedUserMapper.toAuthenticatedUser(userDetails, token);
            // Return the token
            LogUtil.logRuntime(start, "Authenticate " + requestForToken.getUserName());
            return result;
        } catch (AuthenticationException ex) {
            throw new UnauthenticationException("Unauthenticated " + requestForToken.getUserName() + ": " + ex.getMessage(), ex);
        }
    }
}
