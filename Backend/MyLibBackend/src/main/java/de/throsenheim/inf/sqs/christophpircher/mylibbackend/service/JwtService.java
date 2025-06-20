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
 * Service for generating and validating JWT tokens.
 * <p>
 * Encapsulates logic for token creation, parsing claims, validating token expiration,
 * and extracting user-related information from JWT tokens.
 * </p>
 */
@Component
@AllArgsConstructor
public class JwtService {

    private Environment environment;

    /**
     * Generates a JWT token for a given user.
     * Uses the user's UUID as the subject and signs the token using the secret key.
     *
     * @param user the user for whom to generate the token
     * @return signed JWT token string
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .claims(new HashMap<>()) // no additional claims
                .subject(String.valueOf(user.getId()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 *30)) // 30 Minutes
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Decodes the base64-encoded signing key from application properties and builds a {@link SecretKey}.
     *
     * @return HMAC SHA key used to sign/verify tokens
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(environment.getProperty("jwt.secret"));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the user ID (UUID) from a token by reading the `sub` claim.
     *
     * @param token JWT token string
     * @return UUID extracted from the subject claim
     */
    public UUID extractUserID(String token) {
        return  UUID.fromString(extractClaim(token, Claims.SUBJECT, String.class)); //Because I cannot use UUID.class as requiredType for claims.get()
    }

    /**
     * Extracts the expiration date of the token.
     *
     * @param token JWT token string
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims.EXPIRATION, Date.class);
    }

    /**
     * Generic method to extract a custom claim from the token.
     *
     * @param token        the JWT token
     * @param claim        the claim key to extract
     * @param requiredType the expected type of the claim
     * @param <T>          generic return type
     * @return the value of the claim as the specified type
     */
    public <T> T extractClaim(String token, String claim, Class<T> requiredType) {
        final Claims claims = extractAllClaims(token);
        return claims.get(claim, requiredType);
    }

    /**
     * Parses the JWT and extracts all claims.
     *
     * @param token JWT token string
     * @return claims extracted from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Checks whether a token is expired.
     *
     * @param token the token to check
     * @return true if expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates a JWT token by comparing its user ID with the given user and ensuring it has not expired.
     *
     * @param token        JWT token string
     * @param userDetails  the authenticated user's details
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token, UserPrincipal userDetails) {
        final UUID userID = extractUserID(token);
        return (userID.equals(userDetails.getUserID()) && !isTokenExpired(token));
    }

}
