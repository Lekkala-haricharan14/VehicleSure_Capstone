package org.thehartford.willowshield.utility;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtility {

    @Value("${JWT_SECRET:mysecretkeymysecretkeymysecretkey}")
    private String SECRET;
    
    private Key key;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Generate token with ID as subject + username + role
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))   // ID stored as "sub"
                .claim("username", username)          // custom claim
                .claim("role", role)                  // custom claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract user ID (from subject)
    public Long extractUserId(String token) {
        return Long.parseLong(parse(token).getBody().getSubject());
    }

    // Extract username
    public String extractUsername(String token) {
        return parse(token).getBody().get("username", String.class);
    }

    // Extract role
    public String extractRole(String token) {
        return parse(token).getBody().get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}