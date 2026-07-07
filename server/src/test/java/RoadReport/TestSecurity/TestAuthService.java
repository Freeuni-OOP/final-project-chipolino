package RoadReport.TestSecurity;

import RoadReport.entities.User;
import RoadReport.security.dto.LoginRequest;
import RoadReport.security.dto.RegisterRequest;
import RoadReport.security.service.AuthService;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.security.service.RoadUserDetailsService;
import RoadReport.services.core.UserService;
import RoadReport.services.email.EmailService;
import RoadReport.services.email.VerificationService;
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
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock
    private EmailService emailService;
    @Mock
    private VerificationService verificationService;
    @InjectMocks
    private AuthService authService;


    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("Giorgi");
        registerRequest.setPassword("1234");
        registerRequest.setEmail("giorgi@gmail.com");
        loginRequest = new LoginRequest();
        loginRequest.setUsername("Giorgi");
        loginRequest.setPassword("1234");
    }

    @Test
    public void testRegister(){
        when(verificationService.createVerificationToken(any(User.class))).thenReturn("mock-verification-token");

        String response = authService.register(registerRequest);

        assertEquals("Registration successful! Please check your email to activate your account", response);

        verify(userService).registerUser(any(User.class));
        verify(verificationService).createVerificationToken(any(User.class));
        verify(emailService).sendVerificationEmail(eq("giorgi@gmail.com"), eq("mock-verification-token"));    }


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
