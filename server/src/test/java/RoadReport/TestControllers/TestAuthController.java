package RoadReport.TestControllers;

import RoadReport.controllers.AuthController;
import RoadReport.exceptions.core.UserAlreadyExistsException;
import RoadReport.security.dto.LoginRequest;
import RoadReport.security.dto.RegisterRequest;
import RoadReport.security.service.AuthService;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TestAuthController {
    @Autowired
    private MockMvc mock;

    @Autowired
    private ObjectMapper objMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RoadUserDetailsService roadUserDetailsService;

    @Test
    void loginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("Luka");
        request.setPassword("12345");

        Mockito.when(authService.login(request)).thenReturn("fake-jwt-token");

        mock.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt=")));
    }

    @Test
    void loginUnsuccessfully() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("Luka");
        request.setPassword("wrongPassword");

        Mockito.when(authService.login(request))
                .thenThrow(new BadCredentialsException("Wrong password!"));

        mock.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Luka");
        request.setPassword("12345");

        Mockito.when(authService.register(request)).thenReturn("fake-register-token");

        mock.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt=")));
    }

    @Test
    void registerUnsuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Luka");
        request.setPassword("wrongPassword");

        Mockito.when(authService.register(request))
                .thenThrow(new UserAlreadyExistsException("User already exists!"));

        mock.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void logoutSuccessfully() throws Exception {
        mock.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer fake-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
