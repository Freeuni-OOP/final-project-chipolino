package RoadReport.services.security;

import RoadReport.entities.User;
import RoadReport.enums.Role;
import RoadReport.services.core.UserService;
import RoadReport.services.security.dto.LoginRequest;
import RoadReport.services.security.dto.RegisterRequest;
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

    public String register(RegisterRequest request){
        User user = User.builder().
                username(request.getUsername()).
                password(request.getPassword()).
                roles(Role.USER).
                build();
        userService.registerUser(user);

        return getToken(request.getUsername());
    }


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
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        return jwtService.generateToken(userDetails);
    }
}
