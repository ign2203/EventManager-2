package org.example.eventmanagermodule.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.eventmanagermodule.User.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenManager {
    private final SecretKey key;
    private final long expirationTime;

    public JwtTokenManager(
            @Value("${jwt.secretKey}") String keyString,
            @Value("${jwt.lifetime}") Long expirationTime
    ) {
        this.key = Keys.hmacShaKeyFor(keyString.getBytes());
        this.expirationTime = expirationTime;
    }

    public String generateToken(String login, UserRole role) {
        return Jwts
                .builder()
                .subject(login)
                .issuedAt(new Date())
                .claim("role", role.toString()) // теперь user определён
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    public String getLoginFromToken(String token) {
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key) // твой ключ
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}







