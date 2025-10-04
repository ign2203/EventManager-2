package org.example.eventmanagermodule.security.jwt;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

    public String generateToken(String login) {
        return Jwts
                .builder()
                .subject(login)
                .issuedAt(new Date())
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
}







