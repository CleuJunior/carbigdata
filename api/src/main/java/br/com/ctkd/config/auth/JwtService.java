package br.com.ctkd.config.auth;

import br.com.ctkd.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + properties.expiration()))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return getclaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            getclaims(token);
            return true;
        } catch (JwtException e) {
            log.info(e.getMessage());
            return false;
        }
    }

    private Claims getclaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
