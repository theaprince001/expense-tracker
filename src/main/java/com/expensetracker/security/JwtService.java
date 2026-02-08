package com.expensetracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        log.debug("Generating token for user: {}", username);
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        log.debug("Token created - Issued: {}, Expires: {}", now, expiryDate);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean usernameMatches = username.equals(userDetails.getUsername());
            boolean notExpired = !isTokenExpired(token);

            log.debug("Token validation - Username match: {}, Not expired: {}",
                    usernameMatches, notExpired);

            return usernameMatches && notExpired;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // REMOVE THIS METHOD - it conflicts with the one above
    // public boolean isTokenValid(String token) {
    //     try {
    //         return !isTokenExpired(token);
    //     } catch (Exception e) {
    //         log.error("Token validation error: {}", e.getMessage());
    //         return false;
    //     }
    // }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            boolean expired = expiration.before(new Date());
            log.debug("Token expired check: {} (expires: {})", expired, expiration);
            return expired;
        } catch (Exception e) {
            log.error("Failed to check token expiration: {}", e.getMessage());
            return true; // If we can't check, treat as expired
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public long getTokenValidityInSeconds() {
        return expiration / 1000;
    }

    public void validateToken(String token) {
        if (isTokenExpired(token)) {
            throw new RuntimeException("Expired token");
        }
        // Also validate signature by extracting claims
        extractAllClaims(token);
    }
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token); // validates signature
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}