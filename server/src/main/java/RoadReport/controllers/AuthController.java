package RoadReport.controllers;

import RoadReport.security.dto.LoginRequest;
import RoadReport.security.dto.RegisterRequest;
import RoadReport.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * Receives login data from the frontend and returns a JWT.
     * @param request The LoginRequest containing username and password.
     * @return A ResponseEntity containing the generated JWT token in a JwtResponseDTO and HTTP 200 status.
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from("jwt_token", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * Registers a new user in the system.
     * Takes the registration details, creates a new user entity,
     * and returns an initial JWT token so the user is logged in automatically.
     * @param request The DTO containing the new user's username, email, and password
     * @return A ResponseEntity containing the JwtResponseDTO and HTTP 200 status.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        String token = authService.register(request);

        ResponseCookie cookie = ResponseCookie.from("jwt_token", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * Handles the user logout process.
     * In this stateless JWT-based system, this endpoint serves as a confirmation
     * for the client to clear the token from their local storage (localStorage/cookies).
     * @return A ResponseEntity with a success message and HTTP 200 status
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
