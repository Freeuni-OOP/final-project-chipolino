package RoadReport.TestSecurity;

import RoadReport.security.dto.LoginRequest;
import RoadReport.security.dto.RegisterRequest;
import RoadReport.security.service.AuthService;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.security.service.RoadUserDetailsService;
import RoadReport.services.core.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestAuthService {
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private RoadUserDetailsService userDetailsService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RoadUserDetails userDetails;

    @InjectMocks
    private AuthService authService;


    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("Giorgi");
        registerRequest.setPassword("1234");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("Giorgi");
        loginRequest.setPassword("1234");
    }

    @Test
    public void testRegister(){
        when(userDetailsService.loadUserByUsername("Giorgi")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mock");

        String token = authService.register(registerRequest);
        assertEquals("mock", token);

        verify(userDetailsService).loadUserByUsername("Giorgi");
        verify(jwtService).generateToken(userDetails);
    }


    @Test
    public void testValidLogin(){
        Authentication auth = null;
        when(authenticationManager.authenticate
                (any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername("Giorgi")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mock");

        String token = authService.login(loginRequest);
        assertEquals("mock", token);

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("Giorgi");
        verify(jwtService).generateToken(userDetails);
    }


    @Test
    public void testInvalidLogin(){
        Authentication auth = null;
        when(authenticationManager.authenticate
                (any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Invalid credentials"){});

        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
