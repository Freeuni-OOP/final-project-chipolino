package RoadReport.security.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generates a new JWT token for a given user.
     *
     * @param userDetails the authenticated user details
     * @return a signed JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder().subject(userDetails.getUsername()).
                issuedAt(new Date(System.currentTimeMillis())).
                expiration(new Date(System.currentTimeMillis() + jwtExpiration)).
                signWith(getSigningKey()).compact();
    }

    /**
     * Validates a token by checking its expiration and matching the username
     * against the provided {@link UserDetails}.
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to compare against
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try{
            return !isTokenExpired(token) && Objects.equals(extractUsername(token), userDetails.getUsername());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the provided token has expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        try{
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Parses the JWT and extracts all claims from the body.
     *
     * @param token the JWT token
     * @return the claims contained in the token
     */
    private Claims extractEveryClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts a specific claim from the token using a resolver function.
     *
     * @param token          the JWT token
     * @param claimsResolver a function to extract the desired claim
     * @param <T>            the type of the claim
     * @return the extracted claim
     */
    private  <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractEveryClaims(token));
    }

    /**
     * Extracts the username (subject) from the token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Decodes the base64-encoded secret key and returns the signing key.
     *
     * @return a {@link SecretKey} used for signing and verifying tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}