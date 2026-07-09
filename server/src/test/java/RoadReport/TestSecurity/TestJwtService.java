package RoadReport.TestSecurity;

import RoadReport.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TestJwtService {

    private JwtService jwtService;
    private UserDetails userDetails;
    private String validToken;

    private final String testSecretKey = "dGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5IGZvcnRlc3Rpbmcx";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24 hours

        userDetails = new User("nikushabendo", "password", Collections.emptyList());
        validToken = jwtService.generateToken(userDetails);
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void testExtractUsername() {
        String username = jwtService.extractUsername(validToken);
        assertEquals("nikushabendo", username);
    }

    @Test
    void testIsTokenValid_Success() {
        assertTrue(jwtService.isTokenValid(validToken, userDetails));
    }

    @Test
    void testIsTokenValid_WrongUser() {
        UserDetails wrongUser = new User("wrong", "password", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(validToken, wrongUser));
    }

    @Test
    void testIsTokenValid_Expired() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String expiredToken = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenValid(expiredToken, userDetails));
    }

    @Test
    void testIsTokenValid_Malformed() {
        assertFalse(jwtService.isTokenValid("", userDetails));
    }

    @Test
    void testIsTokenValid_Tampered() {
        String tamperedToken = validToken + "extra";
        assertFalse(jwtService.isTokenValid(tamperedToken, userDetails));
    }
}

