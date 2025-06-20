package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;


/**
 * Service for handling JWT tokens
 */
@Component
@AllArgsConstructor
public class JwtService {

    private Environment environment;

    public String generateToken(User user) {
        return Jwts.builder()
                .claims(new HashMap<>()) // no additional claims
                .subject(String.valueOf(user.getId()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 *30)) // 30 Minutes
                .signWith(getSignKey())
                .compact();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(environment.getProperty("jwt.secret"));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public UUID extractUserID(String token) {
        return  UUID.fromString(extractClaim(token, Claims.SUBJECT, String.class)); //Because I cannot use UUID.class as requiredType for claims.get()
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims.EXPIRATION, Date.class);
    }

    public <T> T extractClaim(String token, String claim, Class<T> requiredType) {
        final Claims claims = extractAllClaims(token);
        return claims.get(claim, requiredType);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserPrincipal userDetails) {
        final UUID userID = extractUserID(token);
        return (userID.equals(userDetails.getUserID()) && !isTokenExpired(token));
    }

}
