package RoadReport.security.service;

import RoadReport.entities.User;
import RoadReport.entities.VerificationToken;
import RoadReport.enums.Role;
import RoadReport.services.core.UserService;
import RoadReport.security.dto.LoginRequest;
import RoadReport.security.dto.RegisterRequest;
import RoadReport.services.email.EmailService;
import RoadReport.services.email.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final RoadUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;
    private final VerificationService verificationService;
    /**
     * Registers a new user in the system with the default USER role
     * and generates an initial JWT token for them.
     *
     * @param request the DTO containing the username and password for registration
     * @return the generated JWT token as a String
     */
    public String register(RegisterRequest request){
        User user = User.builder().
                username(request.getUsername()).
                password(request.getPassword()).
                email(request.getEmail()).
                roles(Role.USER).
                build();
        userService.registerUser(user);

        String token = verificationService.createVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), token);
        return "Registration successful! Please check your email to activate your account";
    }


    /**
     * Authenticates a user using the {@link AuthenticationManager}.
     * Generates and returns a new JWT token if authentication succeeds.
     *
     * @param request the DTO containing the username and password for login
     * @return the generated JWT token as a String
     * @throws org.springframework.security.core.AuthenticationException if authentication fails due to invalid credentials
     */
    public String login(LoginRequest request){
         authenticationManager.authenticate(
                 new UsernamePasswordAuthenticationToken(
                         request.getUsername(),
                         request.getPassword()
                 )
         );

         return getToken(request.getUsername());
    }

    /**
     * @param token verification token string
     * @return true if verification succeeds, false otherwise
     */
    public boolean verifyToken(String token) {
        return verificationService.verifyToken(token);
    }

    private String getToken(String username) {
        RoadUserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        return jwtService.generateToken(userDetails);
    }
}
