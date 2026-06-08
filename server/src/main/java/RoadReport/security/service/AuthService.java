package RoadReport.security.service;

import RoadReport.entities.User;
import RoadReport.enums.Role;
import RoadReport.services.core.UserService;
import RoadReport.controllers.dto.LoginRequest;
import RoadReport.controllers.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final RoadUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

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

        return getToken(request.getUsername());
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

    private String getToken(String username) {
        RoadUserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        return jwtService.generateToken(userDetails);
    }
}
