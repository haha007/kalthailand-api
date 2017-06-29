package th.co.krungthaiaxa.api.auth.jwt;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import th.co.krungthaiaxa.api.auth.service.UserDetailsServiceImpl;
import th.co.krungthaiaxa.api.common.log.LogHttpRequestUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;

public class JwtAuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Value("${jwt.header}")
    private String tokenHeader;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Instant startTime = LogHttpRequestUtil.logStarting(httpRequest);
        String authToken = httpRequest.getHeader(this.tokenHeader);

        if (SecurityContextHolder.getContext().getAuthentication() == null && StringUtils.isNotEmpty(authToken)) {
            jwtTokenUtil.getUsernameFromToken(authToken).ifPresent(username -> {
                UserDetails userDetails = this.userDetailsServiceImpl.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            });
        }
        LogHttpRequestUtil.logFinishing(startTime, httpRequest);
        chain.doFilter(request, response);
    }

}