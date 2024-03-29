package th.co.krungthaiaxa.api.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.stream.Collectors.toList;

@Component
public class JwtTokenUtil implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);
    private static final long serialVersionUID = -3301605591108950415L;

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_ROLE = "role";
    private static final String CLAIM_KEY_CREATED = "created";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expirationDuration;

    public Optional<List> getRolesFromToken(String token) {
        Optional<Claims> claims = getClaimsFromToken(token);
        if (!claims.isPresent()) {
            return Optional.empty();
        }
        return Optional.of((List) claims.get().get(CLAIM_KEY_ROLE));
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, LocalDateTime.now().format(ISO_DATE_TIME));
        claims.put(CLAIM_KEY_ROLE, 
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(toList()));
        return generateToken(claims);
    }

    public Boolean isTokenExpired(String token) {
        Optional<Claims> claims = getClaimsFromToken(token);
        if (!claims.isPresent()) {
            return TRUE;
        }

        Optional<LocalDateTime> expirationDate = getExpirationDateFromToken(token);
        return expirationDate.map(localDateTime -> localDateTime.isBefore(LocalDateTime.now())).orElse(TRUE);

    }

    Optional<String> getUsernameFromToken(String token) {
        Optional<Claims> claims = getClaimsFromToken(token);
        if (!claims.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(claims.get().getSubject());
    }

    Boolean validateToken(String token, UserDetails userDetails) {
        Optional<LocalDateTime> creationDate = getCreatedDateFromToken(token);
        if (!creationDate.isPresent()) {
            return FALSE;
        }
        Optional<String> userName = getUsernameFromToken(token);
        if (!userName.isPresent()) {
            return FALSE;
        }

        JwtUser user = (JwtUser) userDetails;
        return userName.get().equals(user.getUsername()) && !isTokenExpired(token);
    }

    private Optional<LocalDateTime> getCreatedDateFromToken(String token) {
        Optional<Claims> claims = getClaimsFromToken(token);
        if (!claims.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(LocalDateTime.from(ISO_DATE_TIME.parse((String) claims.get().get(CLAIM_KEY_CREATED))));
    }

    private Optional<LocalDateTime> getExpirationDateFromToken(String token) {
        Optional<Claims> claims = getClaimsFromToken(token);
        if (!claims.isPresent()) {
            return Optional.empty();
        }
        LocalDateTime expiration = Instant.ofEpochMilli(claims.get().getExpiration().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return Optional.of(expiration);
    }

    private Optional<Claims> getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            LOGGER.error("Cannot claims token " + token + ": " + e.getMessage(), e);
            return Optional.empty();
        }
        return Optional.of(claims);
    }

    private LocalDateTime generateExpirationDate() {
        return LocalDateTime.now().plus(expirationDuration * 1000, ChronoUnit.MILLIS);
    }

    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(java.util.Date.from(generateExpirationDate().atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

}