package RoadReport.TestSecurity;

import RoadReport.security.filter.JwtFilter;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.security.service.RoadUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestJwtFilter {
    @Mock
    private JwtService jwtService;

    @Mock
    private RoadUserDetails userDetails;

    @Mock
    private RoadUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @InjectMocks
    private JwtFilter filter;

    @BeforeEach
    public void setUp(){
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testTokenBearer() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer mock");

        when(jwtService.extractUsername("mock")).thenReturn("mocked name");
        when(userDetailsService.loadUserByUsername("mocked name")).thenReturn(userDetails);
        when(jwtService.isTokenValid("mock", userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        filter.doFilter(request, response, filterChain);

        verify(jwtService).extractUsername("mock");
        verify(userDetailsService).loadUserByUsername("mocked name");
        verify(jwtService).isTokenValid("mock", userDetails);
        verify(filterChain).doFilter(request, response);

        assertEquals(userDetails,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    public void testCookie() throws ServletException, IOException {
        Cookie cookie = new Cookie("jwt", "mock");
        request.setCookies(cookie);

        when(jwtService.extractUsername("mock")).thenReturn("mocked name");
        when(userDetailsService.loadUserByUsername("mocked name")).thenReturn(userDetails);
        when(jwtService.isTokenValid("mock", userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        filter.doFilter(request, response, filterChain);

        verify(jwtService).extractUsername("mock");
        verify(userDetailsService).loadUserByUsername("mocked name");
        verify(jwtService).isTokenValid("mock", userDetails);
        verify(filterChain).doFilter(request, response);

        assertEquals(userDetails,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    public void testInvalidToken() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer mock");

        when(jwtService.extractUsername("mock")).thenReturn("mocked name");
        when(userDetailsService.loadUserByUsername("mocked name")).thenReturn(userDetails);
        when(jwtService.isTokenValid("mock", userDetails))
                .thenThrow(new RuntimeException("Mocked token"));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"error\": \"Unauthorized\""));

        verify(jwtService).extractUsername("mock");
        verify(userDetailsService).loadUserByUsername("mocked name");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    public void testNoToken() throws ServletException, IOException {
        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}